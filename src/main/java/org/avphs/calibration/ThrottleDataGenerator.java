package org.avphs.calibration;


//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.sbcio.ArduinoData;

import java.io.*;
import java.util.*;

import static java.lang.Thread.activeCount;
import static java.lang.Thread.sleep;

public class ThrottleDataGenerator implements CarModule {

    PrintWriter out;
    Car car;
    CarData carData;

    public ThrottleDataGenerator(Car _car) throws IOException {
        car = _car;
        out = new PrintWriter(new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/ThrottleData.txt")));
    }

    //long[] calibrateAcceleration(float[] speedValues) throws InterruptedException {
/**
 * Takes in a float array of the speed values used in @calibrateThrottle and then finds the the time to reach max speed,
 * and additionally  the time to reach each speed from 0. Each index is ordered the time to get from 0 to that
 * corresponding speed in speedValues.length
 * All in Milliseconds
 */
        /*long[] allAccelerationTimes = new long[speedValues.length];
        for (int i = 0; i < speedValues.length; i++) {
            long startTime = System.currentTimeMillis();
            car.accelerate(true, i);
            while (((ArduinoData)carData.getModuleData("arduino")).getOdomCount() <= speedValues[i]) {
//                 Accelerating
            }
            allAccelerationTimes[i] = System.currentTimeMillis() - startTime;
            car.accelerate(true, 0);
            sleep(4000); //Not positive if this sleep will work
        }
        return allAccelerationTimes;
    }

    float[] calibrateThrottle() throws InterruptedException {
        //Throttle angle 60 is the max
        float[] speedValues = new float[61];
        boolean speedChanged;
        //assume throttle 0 is 0
        for (int i = 1; i < speedValues.length; i++) {
            speedChanged = true;
            int lastSpeed = (int) speedValues[i - 1];
            car.accelerate(true, i);
            int sameMaxSpeed = 0;
            while (speedChanged) {
                int lastOdom = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
                System.out.println("Last odom: " + lastOdom);
                sleep(1000);
                int newOdom = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
                System.out.println("New odom: " + newOdom);
                int thisSpeed = newOdom - lastOdom;
                System.out.println("Speed: " + thisSpeed);
                speedChanged = false;
                if (thisSpeed == lastSpeed) {
                    sameMaxSpeed++;
                    if (sameMaxSpeed < 3) {
                        speedChanged = true;
                    } else {
                        System.out.println("Same max speed of " + thisSpeed + " found 3 times in a row");
                    }
                }
                if (thisSpeed > lastSpeed) {
                    speedChanged = true;
                    sameMaxSpeed = 0;
                }
                lastSpeed = thisSpeed;
            }
            speedValues[i] = lastSpeed;
            System.out.println("Throttle: " + i + " = " + lastSpeed + " driveshaft turns");
        }
        car.accelerate(true, 0);
        for (int i = 0; i < speedValues.length; i++) {
            speedValues[i] *= CalibrationModule.CM_PER_ROTATION;
        }
        return speedValues;
    }

    void waitUntilStop() throws InterruptedException {
        while (true) {
            int lastOdom = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            sleep(1000);
            int thisSpeed = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount() - lastOdom;
            if (thisSpeed == 0) {
                return;
            }
        }
    }

    void writeSpeedThrottlesToFile(float[] speedValues) throws IOException {
        System.out.println("Writing to file");
        for (int i = 0; i < speedValues.length; i++) {
            out.println(i + ": " + speedValues[i] + " cm/s");
        }
    }
*/
    @Override
    public void init(CarData carData) {
        /*int middle = CalibrationModule.STRAIGHT_ANGLE;
        int start = middle - (((middle + 33) / 5) * 5);
        HashMap<Integer, float[]> angleThrottleSpeedValues = new HashMap<Integer, float[]>();
        try {
            this.carData = carData;

            for (int i = start; i <= 44; i += 5) {
                System.out.println("Angle being tested: " + i);
                car.steer(true, i);
                sleep(1000);
                float[] speedValues = calibrateThrottle();
                angleThrottleSpeedValues.put(i, speedValues);
                out.println("Steering angle: " + i);
                writeSpeedThrottlesToFile(speedValues);
                waitUntilStop();

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
        System.out.println("Throttle Calibration Done");
        //{ {{angle array},{throttle array}} , {{},{}} ...}
        //speed is the index
//        int[][][] preInterpolation = new int[4024][2][15];
//        for (int i = start; i <= 44; i++) {
//            int angleIndex = 0;
//            float[] speedValues = angleThrottleSpeedValues.get(i);
//            int index = 0;
//            for (int j = 0; j < preInterpolation.length; i++) {
//                float currentDifference = Math.abs(speedValues[index] - i);
//                while (index < 60) {
//                    float newDifference = Math.abs(speedValues[index + 1] - i);
//                    if (newDifference <= currentDifference) {
//                        currentDifference = newDifference;
//                        index++;
//                    } else {
//                        break;
//                    }
//                }
//                preInterpolation[i][0][angleIndex] = i;
//                preInterpolation[i][1][index] = index;
//            }
//            angleIndex++;
//        }
//
//        for (int i = 0; i < preInterpolation.length; i++) {
//            Interpolator interpolate = new Interpolator(intToDoubleArr(preInterpolation[i][0]), intToDoubleArr(preInterpolation[i][1]), 5);
//        }
*/
    }

    private double[] intToDoubleArr(int[] arr) {
        double[] newArr = new double[arr.length];
        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = arr[i];
        }
    }
    public CarCommand[] commands() {
        return new CarCommand[0];
    }

    @Override
    public void update(CarData carData) {

    }
}
