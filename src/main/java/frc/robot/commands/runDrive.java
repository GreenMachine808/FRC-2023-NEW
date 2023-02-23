package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.subsystems.HangSubsystem;
import frc.robot.RobotContainer;
import frc.robot.commands.runShooter;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveSubsystem;
import edu.wpi.first.wpilibj.Timer;

public class runDrive extends CommandBase{
    // private HangSubsystem hang;
    // private ShooterSubsystem shooter;
    private SwerveSubsystem drive;
    private RobotContainer robotContainer;

    
    private Timer timer = new Timer();

    public runDrive(HangSubsystem hang, ShooterSubsystem shooter, SwerveSubsystem drive){
        // this.hang = hang;
        // this.shooter = shooter;
        this.drive = drive;

        // I think this is really bad practice
        // shooter.getRunShooter();

        
        /* addCommands(
            new InstantCommand( () -> hang.popWeightServo (true) )    
        );
        */
    
    }

    @Override
    public void initialize(){
        timer.reset();
        timer.start();
        drive.initDrive();
        //drive.driveSetDistance(2);
        
        /* do {
            drive.driveSetDistance(-2);
        } while ( !timer.hasElapsed(4) );
        */
        //hang.popWeightServo(true);
    }

    @Override
    public void execute() {
        //hang.popWeightServo(true);
        //new InstantCommand(() -> new runShooter(shooter));
        Timer.delay(1);
        
        // shooter.getRunShooter().initialize();
        // shooter.getRunShooter().execute();

        // timer.delay(1.5);

        // shooter.runConveyorBelt(false);

        // //shooter.runConveyorBelt(isBackwards);
       
        // timer.delay(3);

        // shooter.getRunShooter().end(true);
        // shooter.getRunIntake().end(true);
        
        // timer.delay(0.5);
       //TEst

        //THIS IS IN rotations.
        drive.driveSetDistance(1);

        Timer.delay(2);

        drive.driveSetDistance(1);

        /* 
        Timer.delay(2);

        drive.setAllAzimuth(0.25);

        Timer.delay(2);

        drive.driveSetDistance(2);

        Timer.delay(2);

        drive.driveSetDistance(-1);

        Timer.delay(2);

        drive.driveSetDistance(-2);

        Timer.delay(2);

        drive.setAllAzimuth(0.12);

        Timer.delay(2);

        drive.driveSetDistance(1);

        Timer.delay(2);

        drive.driveSetDistance(-1);

        Timer.delay(2);

        */
        
        /**0
         * Test: Make a program to print the value of a public number,
         * and have a threa change the value of the number after a set time
         * pseudo code:
         * public num_val
         * new Thread()
         * for loop
         *     sleep(100)
         *     print num_val
         * -----threadcode
         * sleep(500)
         * change num_val
         */

         /**
          * or like try to pass an event that runs once the thread
          has been completed
          */

        
    }


    @Override
    public boolean isFinished(){
        return true;
    }

}