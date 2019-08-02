package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.FakeCamera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.position.PositionData;
import org.avphs.sbcio.ArduinoData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BrakeTest extends TimerTask {
    float curSpeed = 0;//meters or centimeters per second
    FakeCamera cam = new FakeCamera();
    Car car = new Car(cam);
    CarData carData = new CarData();
    List<double[]> brakeData = new ArrayList<>();
    CalibrationCore core = new CalibrationCore(car, false);

    public void run()
    {
        ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
        //curSpeed = ((PositionData)carData.getModuleData("position")).getSpeed();
    }

    //physically test in increments of 50cm/s until 90mph
    //interpolate using data, get points on curve every 5cm/s
    //need to stop and reset placement(move backwards or turn around) every so often

    //all speeds from 0-max 90mph
    //0 to 4035 in increments of 5cm/s
    //4 friction types, 808
    public static void main(String[] args) {
        BrakeTest brakeBoi = new BrakeTest();
        Timer timer = new Timer();
        timer.schedule(brakeBoi, 0, 1000);

        Boolean resetFile = false;
        int maxSpeed = 4050;//rounded up in cm/s from 90mph(~4023.36cm/s)
        int increment = 50;//speed change increment in cm/s
        int startSpeed = 0;
        int endSpeed = 0;
        int numStartSpeeds = maxSpeed/increment;
        int numEndSpeeds = maxSpeed/increment;
        String stringData = "NA";

        if(resetFile)//reset file to be blank if boolean is true
        {
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt")))
            {
                writer.write("");
                System.out.println("reset file");
            }
            catch(Exception e)
            {
                // Handle any I/O problems
                System.out.println("IO problem caught trying to reset file");
            }
        }

        for(int i=0;i<numStartSpeeds;i++)//go through start speeds
        {
            for(int j=0;j<numEndSpeeds;j++)//go through end speeds
            {
                if(endSpeed<=startSpeed)
                {
                    brakeBoi.testBrakeDist((byte)startSpeed,(byte)endSpeed);//test speeds and adds stuff to brakeData

                    //Write the data to file SpeedToDistData.txt
                    try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt")))
                    {
                        for (int var = 0; var < 3; var++) //go through startSpeed, endSpeed, brakeDist
                        {
                            stringData = Double.toString(brakeBoi.brakeData.get(0)[var]);
                            writer.append(stringData + " ");
                        }
                        writer.newLine();
                    }
                    catch(Exception e)
                    {
                        // Handle any I/O problems
                        System.out.println("IO problem caught from startSpeed: "+startSpeed+" and endSpeed: "+endSpeed);
                    }
                }
                endSpeed+=increment;
            }
            startSpeed+=increment;
        }

        //write the data to file SpeedToDistData.txt
        /*try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt"))){
            for(int i = 0;i < numStartSpeeds; i++)//go through start speeds
            {
                for (int j = 0; j < 0; j++)//go through end speeds
                {
                    for (int var = 0; var < numEndSpeeds; var++) //go through startSpeed, endSpeed, brakeDist
                    {
                        stringData = Double.toString(brakeBoi.brakeData.get(0)[var]);
                        writer.append(stringData + " ");
                    }
                    writer.newLine();
                }

            }
            writer.close();
        }
        catch(Exception e) {
            // Handle any I/O problems

        }*/
    }


    /*Given a start and end speed, find braking distance
    *accelerates until hitting or passing start speed, then decelerates
    * after hitting/passing endSpeed, stops car and adds data to brakeData arrayList
     */
    public void testBrakeDist(byte startSpeed, byte endSpeed)
    {
        byte throttle = 0;//CalibrationModule.getThrottle((byte)0, (short)0, startSpeed);
        int startDist = 0;
        int endDist;
        double brakeDist;

        car.accelerate(true, throttle);
        if(curSpeed >= startSpeed)
        {
            startDist = 10;//((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            throttle = 5;//CalibrationModule.getThrottle((byte)0, (short)0, endSpeed);
            car.accelerate(true, throttle);
        }
        if(curSpeed <= endSpeed)
        {
            endDist = 20;//((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            brakeDist = endDist - startDist;
            car.stop();
            double[] data = new double[3];
            data[0] = startSpeed;
            data[1] = endSpeed;
            data[2] = brakeDist;
            brakeData.add(data);
        }
    }
}