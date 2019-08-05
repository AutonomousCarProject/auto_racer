package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.FakeCamera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.position.PositionData;
import org.avphs.sbcio.ArduinoData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BrakeTest implements CarModule {
    float curSpeed = 0;//meters or centimeters per second
    List<double[]> brakeData = new ArrayList<>();
    Car car;
    CarData carData;
    boolean keepRunning = true;

    public BrakeTest(Car car){
        this.car = car;
    }
    //if odometer doesn't work. set d_livecams under drivercons to true

    /*Physically test brake distances by accelerating to startSpeed and full braking to endSpeed
     * start and end speeds are incremented by 50cm/s until ~90mph(~4050cm)
     * put data into SpeedToDistData.txt in 3 columns: startSpeed, endSpeed, brakeDist
     */
    public void main() {
        boolean resetFile = true;//set this to false if you want to add new set of data to file, set true if you want to clear file before adding
        int maxSpeed = 4050;//rounded up in cm/s from 90mph(~4023.36cm/s)
        int increment = 50;//speed change increment in cm/s
        int startSpeed = 0;
        int endSpeed = 0;
        int numStartSpeeds = maxSpeed/increment;
        int numEndSpeeds = maxSpeed/increment;
        int numDataValues = 0;
        String stringData = "NA";

        System.out.println("starting BrakeTest");

        //reset/add to SpeedToDistData.txt depending on if resetFile is true/false
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt", !resetFile)))//resets file to blank if resetFile is true
        {
            if(resetFile)//reset file to be blank if boolean is true
            {
                writer.write("");
                System.out.println("reset file");
            }
            else
            {
                writer.newLine();
                writer.append("=");
                writer.newLine();
                System.out.println("add to file");
            }
            writer.close();
        }
        catch(Exception e)
        {
            // Handle any I/O problems
            System.out.println("IO problem caught trying to reset file");
            e.printStackTrace();
        }


        for(int i=0;i<numStartSpeeds;i++)//go through start speeds
        {
            for(int j=0;j<numEndSpeeds;j++)//go through end speeds
            {
                if(endSpeed<=startSpeed)
                {
                    testBrakeDist(startSpeed,endSpeed);//test speeds and adds stuff to brakeData
                    numDataValues++;
                    //System.out.println("endSpeed<=startSpeed at i: "+i+" j: "+j+" startSpeed: "+startSpeed+" endSpeed: "+endSpeed);
                }
                endSpeed+=increment;
                //System.out.println("i: "+i+" j: "+j+" startSpeed: "+startSpeed+" endSpeed: "+endSpeed);
            }
            endSpeed = increment;
            //System.out.println("--i: "+i);
            startSpeed+=increment;
            //System.out.println("startspeed after adding increment: "+startSpeed);
        }

        //System.out.println("TIME TO PRINT!");
        //Write the data from brakeData to file SpeedToDistData.txt
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt", true)))
        {
            //System.out.println("trying to append stuff");
            for(int i=0; i <numDataValues;i++)
            {
                for (int var = 0; var < 3; var++) //go through startSpeed, endSpeed, brakeDist
                {
                    //stringData = Double.toString(brakeData.get(i*10+j+1)[var]);
                    stringData = Double.toString(brakeData.get(i)[var]);
                    writer.append(stringData + " ");
                    //System.out.println("i: "+i+" var: "+var+" stringData: "+stringData);
                }
                writer.newLine();
            }
        }
        catch(Exception e)
        {
            // Handle any I/O problems
            System.out.println("IO problem caught from startSpeed: "+startSpeed+" and endSpeed: "+endSpeed);
            e.printStackTrace();
        }
        keepRunning = false;
    }


    /*Given a start and end speed, find braking distance
     *accelerates until hitting or passing start speed, then decelerates
     * after hitting/passing endSpeed, stops car and adds data to brakeData arrayList
     */
    public void testBrakeDist(int startSpeed, int endSpeed)
    {
        //System.out.println("startSpeed inside testBrakeDist(): "+startSpeed);
        //byte throttle = CalibrationModule.getThrottle((byte)0, (short)0, endSpeed);
        CalibrationModule a = new CalibrationModule();
        short throttle = a.getThrottle((short)0, (byte)0);
        int startDist = 0;
        int endDist;
        double brakeDist;

        car.accelerate(true, throttle);
        while(curSpeed < startSpeed)
        {
            //get odom
            //wait
            //get odom
            //dif = 2nd - 1st odom
            // cur speed: (dif * cm_rotation)/wait time

        }
        throttle = 0;
        startDist = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();

        car.accelerate(true, throttle);

        while(curSpeed > endSpeed)
        {
            //get odom
            //wait
            //get odom
            //dif = 2nd - 1st odom
            // cur speed: (dif * cm_rotation)/wait time

        }
        endDist = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
        brakeDist = (endDist - startDist)*CalibrationModule.CM_PER_ROTATION;
        double[] data = new double[3];
        data[0] = startSpeed;
        data[1] = endSpeed;
        data[2] = brakeDist;
        brakeData.add(data);
        try{
            waitUntilStop();


        }
        catch(Exception e)
        {

        }
    }
    void waitUntilStop() throws InterruptedException {
        while (true) {
            int lastOdom = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            Thread.sleep(1000);
            int thisSpeed = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount() - lastOdom;
            if (thisSpeed == 0) {
                return;
            }
        }
    }

    @Override
    public void init(CarData carData) {

    }

    @Override
    public void update(CarData carData) {
        if(keepRunning)
        {
            this.carData = carData;
            main();
        }
    }
}