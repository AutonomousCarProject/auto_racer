package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.FakeCamera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.sbcio.ArduinoData;

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

    public void run(){
        curSpeed = 0;//((PositionData)carData.getModuleData("position")).getSpeed();
        //System.out.println("curSpeed: "+curSpeed);
    }

    public static void main(String[] args) {
        BrakeTest boi = new BrakeTest();
        Timer timer = new Timer();
        timer.schedule(boi, 0, 5000);

        int numStartSpeeds = 3;
        int numEndSpeeds = 3;
        int startSpeed = 5;
        int endSpeed = 0;
        String stringData = "NA";

        for(int i=0;i<numStartSpeeds;i++)//start speeds
        {
            for(int j=0;j<numEndSpeeds;j++)//end speeds
            {
                boi.testBrakeDist((byte)startSpeed,(byte)endSpeed);
                endSpeed+=0;
            }
            startSpeed+=1;
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/java/org/avphs/calibration/SpeedToDistData.txt"))){
            writer.write("startSpeed, endSpeed, brakeDistance");
            writer.newLine();
            for(int i=0;i<numStartSpeeds;i++)//start speeds
            {
                for (int j = 0; j < numEndSpeeds; j++)//end speeds
                {
                    for (int var = 0; var < 3; var++) {
                        stringData = Double.toString(boi.brakeData.get(0)[var]);
                        writer.append(stringData + " ");
                    }
                    writer.newLine();
                }
            }
            writer.close();
        }
        catch(Exception e) {
            // Handle any I/O problems

        }
    }


    //Given a start and end speed, find braking distance
    public void testBrakeDist(byte startSpeed, byte endSpeed)
    {
        byte throttle = CalibrationModule.getThrottle((byte)0, (short)0, startSpeed);
        //boolean decelerate = false;//used to set acceleration/trigger stuff
        int startDist = 0;
        int endDist;
        double brakeDist;

        /*if(!decelerate)
        {
            car.accelerate(true, throttle);
        }*/
        car.accelerate(true, throttle);

        if(curSpeed >= startSpeed)
        {
            //System.out.println("curSpeed >= startSpeed");
            //decelerate = true;
            startDist = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            throttle = CalibrationModule.getThrottle((byte)0, (short)0, endSpeed);
            car.accelerate(true, throttle);
        }
        if(curSpeed <= endSpeed)
        {
            //System.out.println("curSpeed >= startSpeed");
            endDist = 0;//((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            brakeDist = endDist - startDist;
            car.stop();
            //throw start/end speed, and braking distance into arraylist
            double[] data = new double[3];
            data[0] = startSpeed;
            data[1] = endSpeed;
            data[2] = brakeDist;
            brakeData.add(data);
            //System.out.println(brakeDist);
            //curTest +=1;
        }
    }
}