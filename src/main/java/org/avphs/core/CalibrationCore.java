package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.calibration.*;

import java.util.Scanner;

public class CalibrationCore extends CarCore {

    public CalibrationCore(Car car, boolean needsCamera) {
        super(car, false);

        // Add Run-time Modules
        if(needsCamera){
            updatingCarModules.add(new ImageModule());
        }

        init();
        startUpdatingModules();
    }

    public void runTime(){
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
                    System.out.println("'help' prints this little blurb");
                    break;
                case "CameraDataGenerator":
                    //updatingCarModules.add(new CameraDataGenerator());
                    break;
                case "3DInterpolation":
                    break;
                case "MoveForward":
                    break;
                case "BrakeTest":
                    break;
                case "ThrottleDataGenerator":
                    //updatingCarModules.add(new ThrottleDataGenerator(car));
                    break;
                case "stop":
                    System.out.println("Stopping all calibration functions!");
                    break LOOP;
            }
        }
        car.stop();
    }
}
