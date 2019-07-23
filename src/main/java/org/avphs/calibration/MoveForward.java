package org.avphs.calibration;

import fly2cam.FlyCamera;
import org.avphs.camera.Camera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.image.ImageData;
import org.avphs.sbcio.ArduinoData;

import java.util.ArrayList;

public class MoveForward {

    public MoveForward(){

    }

    /*protected void go(){
        testCar.accelerate(true, 20);
        testCar.steer(true, 0);
    }*/

    public static void main(String[] args){
        Camera cam = new FlyCamera();
        Car car = new Car(cam);
        CarData carData = new CarData();
        CalibrationCore core = new CalibrationCore(car, true);
        ImageData imageData = (ImageData) carData.getModuleData("image");

        ArrayList<Integer> wallHeights = new ArrayList<Integer>();
        ArrayList<Integer> distances = new ArrayList<Integer>();
        ArduinoData data;
        int dist;

        car.accelerate(true, 10);
        car.steer(true, 0);
        while(true){
            data = (ArduinoData)carData.getModuleData("arduino");
            dist = data.count;
            wallHeights.add(imageData.wallBottom[320] - imageData.wallTop[320]);
            distances.add(dist);

            if(dist < 1){
                break;
            }
        }
        car.stop();

    }
}