package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.FlyCamera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.sbcio.ArduinoData;

import java.io.*;
import java.util.*;

import static java.lang.Thread.sleep;

public class ThrottleDataGenerator {
    static Car car;
    static CarData carData;
    static PrintWriter out;

    public static void main(String[] args) throws InterruptedException, IOException {
        out = new PrintWriter(new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/ThrottleData.txt")));
        Camera cam;
        try {
            cam = new FlyCamera();
            car = new Car(cam);
            out.println("Car camera found.");
        } catch (Throwable T) {
            System.out.println("Car camera not found. Using sim camera");
            out.println("Car camera not found.");
            cam = new SimCamera();
            car = new Car(cam);
        }
        carData = new CarData();
        car.init(carData);
        CalibrationCore core = new CalibrationCore(car, false);
        HashMap<Integer, float[]> angleThrottleSpeedValues = new HashMap<Integer, float[]>();
        int middle = CalibrationModule.STRAIGHT_ANGLE;
        int start = middle - (((middle + 33) / 5) * 5);
        for (int i = start; i < 44; i += 5) {
            car.steer(true, i);
            //sleep(1000);
            float[] speedValues = calibrateThrottle();
            angleThrottleSpeedValues.put(i, speedValues);
            out.println("Steering angle: " + i);
            writeSpeedThrottlesToFile(speedValues);
            waitUntilStop();
            System.out.println("Angle " + i + " Found");
        }

        out.close();
        System.out.println("Throttle Calibration Done");
        //{ {{angle array},{throttle array}} , {{},{}} ...}
        //speed is the index
        int[][][] preInterpolation = new int[4024][2][15];
        for (int i = start; i < 44; i++) {
            float[] speedValues = angleThrottleSpeedValues.get(i);
            int index = 0;
            for (int j = 0; j < preInterpolation.length; i++) {
                if(index < 60){

                }
            }
        }
    }
    static long[] calibrateAcceleration(float [] speedValues) throws InterruptedException {
/**
 * Takes in a float of the speed values used in @calibrateThrottle and then finds the the time to reach max speed,
 * and additionally  the time to reach each speed from 0. Each index is ordered the time to get from 0 to that
 * corresponding speed in speedValues.length
 * All in Milliseconds
 */
        long [] allAccelerationTimes =  new long [speedValues.length];
         for(int i=0 ; i < speedValues.length ; i++ ) {
             long startTime = System.currentTimeMillis();
             car.accelerate(true, i);
             while (((ArduinoData) carData.getModuleData("arduino")).count <= speedValues[i]) {
                 System.out.println("Accelerating");
             }
             allAccelerationTimes [i] = System.currentTimeMillis() - startTime;
             car.accelerate(true, 0);
             Thread.sleep(4000);
         }
        return allAccelerationTimes;
    }

    static float[] calibrateThrottle() throws InterruptedException {
        //Throttle angle 60 is the max
        float[] speedValues = new float[61];
        boolean speedChanged;
        //assume throttle 0 is 0
        for (int i = 1; i < speedValues.length; i++) {
            speedChanged = true;
            int lastSpeed = (int) speedValues[i - 1];
            car.accelerate(true, i);
            while (speedChanged) {
                int lastOdom = ((ArduinoData) carData.getModuleData("arduino")).count;
                //sleep(1000);
                int thisSpeed = ((ArduinoData) carData.getModuleData("arduino")).count - lastOdom;
                speedChanged = false;
                if (thisSpeed > lastSpeed) {
                    speedChanged = true;
                }
                lastSpeed = thisSpeed;
            }
            speedValues[i] = lastSpeed;
//            System.out.println("Throttle: " + i + " = " + lastSpeed + " cm/s");
        }
        car.accelerate(true, 0);
        for (int i = 0; i < speedValues.length; i++) {
            speedValues[i] *= CalibrationModule.CM_PER_ROTATION;
        }
        return speedValues;
    }

    static void waitUntilStop() throws InterruptedException {
        while (true) {
            int lastOdom = ((ArduinoData) carData.getModuleData("arduino")).count;
            //sleep(1000);
            int thisSpeed = ((ArduinoData) carData.getModuleData("arduino")).count - lastOdom;
            if (thisSpeed == 0) {
                return;
            }
        }
    }

    static void writeSpeedThrottlesToFile(float[] speedValues) throws IOException {
        System.out.println("Writing to file");
        for (int i = 0; i < speedValues.length; i++) {
            out.println(i + ": " + speedValues[i] + " cm/s");
        }
    }
}
