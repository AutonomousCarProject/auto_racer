package org.avphs.calibration;

import org.avphs.camera.FakeCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.sbcio.ArduinoData;


public class DriveShaftCalibration {
    public static void main(String[] args){
        int throttle = 20;
        int driveShaftCount = 100;
        float DriveShaft_to_Distance = 15;

        FakeCamera cam = new FakeCamera();
        Car car = new Car(cam);
        CarData carData = new CarData();
        new CalibrationCore(car, false);
        car.init(carData);
        ArduinoData arduinoData;

        do {
            car.update(carData);
            arduinoData = (ArduinoData) carData.getModuleData("arduino");
            car.accelerate(true, throttle);


        } while(arduinoData.getOdomCount() < driveShaftCount);

        car.stop();
        System.out.println((arduinoData.getOdomCount() * DriveShaft_to_Distance) + " cm traveled, drive shaft scaler = " + DriveShaft_to_Distance);
    }
}
