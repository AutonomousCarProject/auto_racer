package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.sbcio.ArduinoData;

import java.io.*;

import static java.lang.Thread.sleep;

public class ThrottleDataGenerator {
    public static void main(String[] args) throws InterruptedException {

        Camera cam = null;
        CarData carData = new CarData();
        Car car = new Car(new SimCamera());
        car.init(carData);
        CalibrationCore core = new CalibrationCore(car, false);

        //NEVER SET THROTTLE WITH ANGLE MORE THAN 60 (im keeping it capped to 59 to be safe)
        float[] speedValues = new float[59];
        boolean speedChanged;
        int lastSpeed = 0;
        int lastOdom;
        for (int i = 1; i < 59; i++) {
            speedChanged = true;
            while (speedChanged) {
                System.out.println(carData.getModuleData("arduino"));
                car.update(carData);
                lastOdom = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
                car.accelerate(true, i);
                sleep(1000);
                car.update(carData);

                int thisSpeed = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount() - lastOdom;
                speedChanged = false;
                if (thisSpeed > lastSpeed) {
                    speedChanged = true;
                } else if (speedValues[i - 1] >= thisSpeed) {
                    speedValues[i] = lastSpeed;
                }
                lastSpeed = thisSpeed;
            }
            //TODO: needs to be converted from driveshaft spins to distance
            /**
             * Wheel Radius is 5 cm therefore wheel circumfrence is 2PiR
             * 2* Math.PI * 5;
             */
            speedValues[i] = lastSpeed;
        }


    }

    //new line
    //increase number by 1
    //first number is throttle to go at 0 cm/s
    //second is throttle to go at .5cm/s
    //third is throttle to go at 1cm/s etc. etc.
    //maxSpeed CHANGES DEPENDS ON UNITS OF SPEEDVALUES (cm/s?) AS DOES desiredSpeed
    private void writeSpeedThrottlesToFile(String filename, float[] speedValues) throws IOException {
        float maxSpeed = 0;
        for (int i = 0; i < speedValues.length; i++) {
            if (speedValues[i] > maxSpeed) {
                maxSpeed = speedValues[i];
            }
        }

        String writeThis = ""; //what ultimately gets added to a new line
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filename));

        String firstLine = bufferedReader.readLine();
        if (firstLine != null) {
            bufferedWriter.write((Integer.parseInt(firstLine) + 1)); //increases first line num by 1
        } else {
            bufferedWriter.write("1");
        }

        bufferedWriter.newLine();
        if (bufferedReader.readLine() == null) {
            bufferedWriter.write("0");
        }

        bufferedWriter.newLine(); //at third line by this point just like bufferedReader
        while (bufferedReader.readLine() != null) { //makes sure it doesn't write over existing things
            bufferedWriter.newLine();
        }


//change the increment to adjust the increment, as the name implies
        float increment = .5f;
        outerloop:
        for (float desiredSpeed = 0; desiredSpeed < maxSpeed + increment; desiredSpeed += increment) {
            for (int i = 0; i < speedValues.length; i++) {
                if (i != 0 && speedValues[i] == 0) {
                    break outerloop;
                } else {
                    if (speedValues[i] >= desiredSpeed) {
                        //curve fitting may be slightly more accurate here
                        if (Math.abs(speedValues[i] - desiredSpeed) <= Math.abs(speedValues[i - 1] - desiredSpeed)) {
                            if (!writeThis.equals("")) {
                                writeThis += " ";
                            }
                            writeThis += i;

                        } else {
                            if (!writeThis.equals("")) {
                                writeThis += " ";
                            }
                            writeThis += i - 1;
                        }
                    }
                }
            }
        }

        bufferedReader.close();
        bufferedWriter.close();

        //TODO: MAKE IT UPDATE THE SECOND LINE WITH THE NEW MAX SPEED (If there is a new max speed found)

    }
}
