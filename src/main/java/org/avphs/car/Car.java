package org.avphs.car;

import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.Arduino;
import org.avphs.sbcio.ArduinoIO;

public class Car implements ClientInterface {
    private Camera camera;
    private Arduino arduino;

    public Car(Camera camera)
    {
        this.camera = camera;
        camera.Connect(4);
        this.arduino = new Arduino();
        if (arduino.GetFirmwareRev()<0x120000) return; // not HardAta
        arduino.pinMode(11,Arduino.DEADMAN);
        // Set the digital input pin 11 as (PWM) DeadMan switch from xmtr
        arduino.pinMode(10,Arduino.DM_SERVO);
        // Set the digital output pin 10 as ESC servo under DeadMan control
        arduino.servoWrite(10,105); // start servo +15 degrees
        arduino.pinMode(8,Arduino.PULSECOUNT);
        arduino.DoPulseCnt(8,33);
    }

    public void init(CarData carData) {
        if (camera instanceof SimCamera)
            carData.addData("traksim", ((SimCamera) camera).theSim);

    }

    @Override
    public void getCameraImage(CarData carData) {
        camera.NextFrame();
        carData.addData("camera", camera);
    }

    @Override
    public void accelerate(boolean absolute, int angle) {
        arduino.servoWrite(camera.getSpeedServoPin(), angle + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
        arduino.servoWrite(camera.getSteerServoPin(), angle + 90);
    }

    @Override
    public void stop() {
        accelerate(true, 0);
        steer(true, 0);

        arduino.Close();

        camera.Finish();
    }
}
