package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.calibration.*;

import java.util.Scanner;

public class CalibrationCore extends CarCore {

    public CalibrationCore(Car car, boolean needsCamera) {
        super(car);

        // Add Run-time Modules
        if(needsCamera){
            updatingCarModules.add(new ImageModule());
        }
        startUpdatingModules();

        try {
            runTime();
        } catch (Exception e){

        }
    }

    public void runTime() throws Exception{
        Scanner sc = new Scanner(System.in);
        LOOP:
        while (true){
            System.out.println("Please input a command");
            System.out.println("Type 'help' for help");
            String line = sc.nextLine();
            switch(line){
                case "help":
                    System.out.println("'stop' will stop all functions and close the program");
                    //System.out.println("'CameraDataGenerator'");
                    //System.out.println("'3DInterpolation'");
                    //System.out.println("'MoveForward'");
                    System.out.println("'ThrottleDataGenerator'");
                    System.out.println("'help' prints this little blurb");
                    break;
                case "CameraDataGenerator":
                    updatingCarModules.clear();
                    //updatingCarModules.add(new CameraDataGenerator());
                    updatingCarModules.get(0).init(carData);
                    break;
                case "3DInterpolation":
                    updatingCarModules.clear();
                    updatingCarModules.get(0).init(carData);
                    break;
                case "MoveForward":
                    updatingCarModules.clear();
                    updatingCarModules.get(0).init(carData);
                    break;
                case "BrakeTest":
                    updatingCarModules.clear();
                    updatingCarModules.get(0).init(carData);
                    break;
                case "ThrottleDataGenerator":
                    updatingCarModules.clear();
                    updatingCarModules.add(new ThrottleDataGenerator(car));
                    updatingCarModules.get(0).init(carData);
                    break;
                case "stop":
                    System.out.println("Stopping all calibration functions!");
                    break LOOP;
            }
        }
        car.stop();
    }
}
