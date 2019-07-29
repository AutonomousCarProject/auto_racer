package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.image.ImageModule;

import java.util.Scanner;


public class CalibrationCore extends CarCore {
    public CalibrationCore(Car car, boolean needsCamera) {
        super(car);

        // Add Run-time Modules
        if(needsCamera){
            updatingCarModules.add(new ImageModule());
        }

        init();
        startUpdatingModules();
    }

    @Override
    public void startUpdatingModules(){
        Scanner sc = new Scanner(System.in);
        LOOP:
        while (true){
            System.out.println("Please input a command");
            System.out.println("Type 'help' for help");
            String line = sc.nextLine();
            switch(line){
                case "help":
                    System.out.println("'stop' will stop all functions and close the program");
                    System.out.println("'CameraDataGenerator'");
                    System.out.println("'3DInterpolation'");
                    System.out.println("'MoveForward'");
                    System.out.println("'ThrottleDataGenerator'");
                    System.out.println("'TurnThrottleGenerator'");
                    System.out.println("");
                    System.out.println("'help' prints this little blurb");
                    break;
                case "CameraDataGenerator":
                    break;
                case "3DInterpolation":
                    break;
                case "MoveForward":
                    break;
                case "ThrottleDataGenerator":
                    break;
                case "TurnThrottleGenerator":
                    break;
                case "BrakeTest":
                    break;
                case "stop":
                    System.out.println("Stopping all calibration functions!");
                    break LOOP;
            }
        }
        car.stop();
    }

    private void update(){

    }
}
