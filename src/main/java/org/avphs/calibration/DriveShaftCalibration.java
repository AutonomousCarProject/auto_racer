package org.avphs.calibration;

import org.avphs.camera.FakeCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.sbcio.ArduinoData;


public class DriveShaftCalibration {
    public static void main(String[] args){
        int throttle = 20;//sets throttle at a moderate speed
        int driveShaftCount = 100;//runs the drive shaft for 100 turns
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


        } while(arduinoData.getOdomCount() < driveShaftCount); //while the odometer is less than 100, update the arduino (and thus the odometer)
        //and ensure the throttle is constant

        car.stop();//once the odometer reaches 100, stop the car
        System.out.println((arduinoData.getOdomCount() * DriveShaft_to_Distance) + " cm traveled, drive shaft scaler = " + DriveShaft_to_Distance);
        //measures the odometer, multiply it by turns per distance to yield distance with the designated scale
    }
}
