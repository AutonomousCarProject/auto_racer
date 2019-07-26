package org.avphs.calibration;

import fly2cam.FlyCamera;
import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.position.PositionData;
import org.avphs.sbcio.ArduinoData;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BrakeTest extends TimerTask {
    float curSpeed = 0;//meters or centimeters per second
    Camera cam = new FlyCamera();
    Car car = new Car(cam);
    CarData carData = new CarData();
    List<double[]> brakeData = new ArrayList<>();
    CalibrationCore core = new CalibrationCore(car, false);

    public void run(){
        curSpeed = ((PositionData)carData.getModuleData("position")).getSpeed();
        System.out.println("curSpeed: "+curSpeed);
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new BrakeTest(), 0, 5000);
    }


    //Given a start and end speed, find braking distance
    public void testBrakeDist(byte startSpeed, byte endSpeed)
    {
        byte throttle = CalibrationModule.getThrottle((byte)0, (short)0, startSpeed);
        boolean decellerate = false;//used to set acceleration/trigger stuff
        int startDist = 0;
        int endDist = 0;
        double brakeDist = 0;

        if(!decellerate)
        {
            car.accelerate(true, throttle);
        }


        if(curSpeed >= startSpeed)
        {
            decellerate = true;
            startDist = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            throttle = CalibrationModule.getThrottle((byte)0, (short)0, endSpeed);
            car.accelerate(true, throttle);
        }
        if(curSpeed <= endSpeed)
        {
            endDist = ((ArduinoData) carData.getModuleData("arduino")).getOdomCount();
            brakeDist = endDist - startDist;
            car.stop();
            //throw start/end speed, and braking distance into arraylist
            double[] data = new double[3];
            data[0] = startSpeed;
            data[1] = endSpeed;
            data[2] = brakeDist;
            brakeData.add(data);
            //System.out.println(brakeDist);
        }
    }
}

/*
class SayHello extends TimerTask {
    public void run() {
       System.out.println("Hello World!");
    }
}

// And From your main() method or any other method
Timer timer = new Timer();
timer.schedule(new SayHello(), 0, 5000);
 */