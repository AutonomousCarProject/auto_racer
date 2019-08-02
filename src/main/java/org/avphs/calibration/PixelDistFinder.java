package org.avphs.calibration;

import org.avphs.camera.Camera;
import org.avphs.camera.FlyCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;

public class PixelDistFinder {
    public static void main(String[] args){

        Camera cam = new FlyCamera();
        Car car = new Car(cam);
        CarData carData = new CarData();
        CalibrationCore core = new CalibrationCore(car, false);


    }
}
