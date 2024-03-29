/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.runDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.cscore.CvSink;
import edu.wpi.first.cscore.CvSource;
import edu.wpi.first.cscore.UsbCamera;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

//import com.revrobotics.AnalogInput;

import edu.wpi.first.wpilibj.AnalogInput;


import edu.wpi.first.apriltag.AprilTagDetection;
import edu.wpi.first.apriltag.AprilTagDetector;

import frc.robot.subsystems.SwerveSubsystem;

import frc.robot.subsystems.ArmSubsystem;
import java.util.ArrayList;




/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private Command autonomousCommand;
  private RobotContainer robotContainer;
  
  private SendableChooser<Command> auto;
  private final ArmSubsystem arm = new ArmSubsystem();


  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */

   Thread m_visionThread;
   UsbCamera camera2;

  @Override
  public void robotInit() {
    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    robotContainer = new RobotContainer();
  
    auto = new SendableChooser<>();

    Thread visionThread = new Thread(() -> apriltagVisionThreadProc());
    visionThread.setDaemon(true);
    visionThread.start(); 

    camera2 = CameraServer.startAutomaticCapture(1);

    //auto.setDefaultOption("Simple Auto", robotContainer.getSimpleAuto());
    //auto.addOption("Drive Back Auto", robotContainer.getDriveBackAuto());
    //auto.setDefaultOption("CommandBase Auto", robotContainer.getCommandBaseAuto());
    auto.setDefaultOption("RunDrive Auto", robotContainer.getRunDriveAuto());
    auto.addOption("RunBalance Auto", robotContainer.getRunBalanceAuto());
    SmartDashboard.putData("Auto Mode", auto);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
    
    SmartDashboard.putNumber("Speed modifier: ", robotContainer.getSpeedMod());
    SmartDashboard.putNumber("Turn modifier: ", robotContainer.getTurnMod());

    SmartDashboard.putNumber("claw set point", arm.claw.getSelectedSensorPosition());
    SmartDashboard.putNumber("Elevator set point", arm.elevatorL.getSelectedSensorPosition());


    

    //robotContainer.getPSI();
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   */
  @Override
  public void disabledInit() {
    robotContainer.getSwerve().drive(0, 0, 0);
  }

  @Override
  public void disabledPeriodic() {
  }

  /**
   * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit() {
    autonomousCommand = auto.getSelected();

    // schedule the autonomous command (example)
    if (autonomousCommand != null) {
      autonomousCommand.schedule();
    }
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
    robotContainer.getSwerve().initDrive();
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testInit() {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
 




  }

  /*
   * This function is called periodically during test mode.
   */
  //AnalogInput ZeroEncoder = new AnalogInput(0);
  //AnalogInput OneEncoder = new AnalogInput(1);
  //AnalogInput TwoEncoder = new AnalogInput(2);
  //AnalogInput ThreeEncoder = new AnalogInput(3);

  @Override
  public void testPeriodic() {
    /* 
    SmartDashboard.putNumber("1 Encoder",ZeroEncoder.getValue());
    SmartDashboard.putNumber("2 Encoder",OneEncoder.getValue());
    SmartDashboard.putNumber("3 Encoder",TwoEncoder.getValue());
    SmartDashboard.putNumber("4 Encoder",ThreeEncoder.getValue());
*/


  }

  void apriltagVisionThreadProc() {
    AprilTagDetector detector = new AprilTagDetector();
    detector.addFamily("tag16h5", 0);
  
    // Get the UsbCamera from CameraServer
    UsbCamera camera1 = CameraServer.startAutomaticCapture();
    // Set the resolution
    camera1.setResolution(640, 480);

    // Get a CvSink. This will capture Mats from the camera
    CvSink cvSink = CameraServer.getVideo();
    // Setup a CvSource. This will send images back to the Dashboard
    CvSource outputStream = CameraServer.putVideo("detect", 640, 480);

    // Mats are very memory expensive. Lets reuse this Mat.
    Mat mat = new Mat();
    Mat grayMat = new Mat();
    ArrayList<Integer> tags = new ArrayList<>();

    //
    Scalar outlineColor = new Scalar(0, 255, 0);
    Scalar xColor = new Scalar(0, 0, 255);

    // This cannot be 'true'. The program will never exit if it is. This
    // lets the robot stop this thread when restarting robot code or
    // deploying.
    while (!Thread.interrupted()) {
      // Tell the CvSink to grab a frame from the camera and put it
      // in the source mat.  If there is an error notify the output.
      if (cvSink.grabFrame(mat) == 0) {
        // Send the output the error.
        outputStream.notifyError(cvSink.getError());
        // skip the rest of the current iteration
        continue;
      }

      Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGB2GRAY);

      AprilTagDetection[] detections = detector.detect(grayMat);
      tags.clear();
      for (AprilTagDetection detection : detections) {
        tags.add(detection.getId());

        for (var i = 0; i <= 3; i++) {
          var j = (i + 1) % 4;
          var pt1 = new Point(detection.getCornerX(i), detection.getCornerY(i));
          var pt2 = new Point(detection.getCornerX(j), detection.getCornerY(j));
          Imgproc.line(mat, pt1, pt2, outlineColor, 2);
        }

        var cx = detection.getCenterX();
        var cy = detection.getCenterY();
        var ll = 10;
        Imgproc.line(mat, new Point(cx - ll, cy), new Point(cx + ll, cy), xColor, 2);
        Imgproc.line(mat, new Point(cx, cy - ll), new Point(cx, cy + ll), xColor, 2);
        Imgproc.putText(mat, Integer.toString(detection.getId()), new Point (cx + ll, cy), Imgproc.FONT_HERSHEY_SIMPLEX, 1, xColor, 3);
      }

      SmartDashboard.putString("tag", tags.toString());
      // Give the output stream a new image to display
      outputStream.putFrame(mat);
    }

    detector.close();
  }
}