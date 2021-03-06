package org.avphs.car;

import fly2cam.FlyCamera;
import org.avphs.camera.Camera;
import org.avphs.core.CarCore;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.Arduino;
import org.avphs.sbcio.ArduinoData;
import org.avphs.sbcio.fakefirm.UpdateListener;

public class Car implements ClientInterface, CarModule {
    private Camera camera;
    private Arduino arduino;
    private int fps;
    private static final int DRIVESHAFT_PIN = 8;
    private static PulseListener ps = new PulseListener();

    private static class PulseListener implements UpdateListener {

        private int prior;
        private int count;

        public PulseListener() {
            prior = 0;
        }

        @Override
        public void pinUpdated(int pin, int value) {
            if (pin == DRIVESHAFT_PIN) {
                if (value + prior > 0) {
                    prior = value;
                    count++;
                }
            }
        }

        public int getCount() {
            return count;
        }
    }

    public Car(Camera camera) {
        this.camera = camera;
        camera.Connect(4);
        arduino = new Arduino();

        fps = CarCore.FPS;

        if (arduino.GetFirmwareRev() < 0x120000) {
            return; // not HardAta
        }
        arduino.pinMode(11, Arduino.DEADMAN);
        // Set the digital input pin 11 as (PWM) DeadMan switch from xmtr
        arduino.pinMode(10, Arduino.DM_SERVO);
        // Set the digital output pin 10 as ESC servo under DeadMan control
        arduino.servoWrite(10, 105); // start servo +15 degrees
        arduino.addInputListener(Arduino.REPORT_PULSECOUNT, ps);
        arduino.DoPulseCnt(8, 1000 / fps / 2);
    }

    public void init(CarData carData) {
        getCameraImage(carData);
        carData.addData("arduino", new ArduinoData(ps.getCount(), aVoid -> arduino.Close()));
        carData.addData("camera", camera);
    }

    public void update(CarData carData) {
        carData.addData("arduino", new ArduinoData(ps.getCount(),  aVoid -> arduino.Close()));
    }

    @Override
    public void getCameraImage(CarData carData) {
        camera.NextFrame();
    }

    @Override
    public void accelerate(boolean absolute, int angle) {
        if (camera instanceof FlyCamera)
            arduino.servoWrite(camera.getSpeedServoPin(), angle + 90);
        else
            arduino.setServoAngle(camera.getSpeedServoPin(), angle + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
        if (camera instanceof FlyCamera)
            arduino.servoWrite(camera.getSteerServoPin(), angle + 90);
        else
            arduino.setServoAngle(camera.getSteerServoPin(), angle + 90);
    }


    @Override
    public void stop() {
        accelerate(true, 0);
        steer(true, 0);

        arduino.Close();

        camera.Finish();
    }
}
