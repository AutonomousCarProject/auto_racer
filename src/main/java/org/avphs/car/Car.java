package org.avphs.car;

import org.avphs.camera.Camera;
import org.avphs.core.CarCore;
import org.avphs.core.RacingCore;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.Arduino;
import org.avphs.sbcio.ArduinoData;
import org.avphs.sbcio.fakefirm.UpdateListener;

public class Car implements ClientInterface {
    private Camera camera;
    private Arduino arduino;
    private int fps;
    PulseListener ps = new PulseListener();

    private class PulseListener implements UpdateListener{

        private int prior;

        public PulseListener(){
            prior = 0;
        }

        public void pinUpdated(int pin, int value) {
            if (pin == 8) {
                if (value + prior > 0){
                    prior = value;
                }
            }
        }

        public int getCount(){
            return prior;
        }
    }

    public Car(Camera camera)
    {
        this.camera = camera;
        camera.Connect(4);
        this.arduino = new Arduino();

        fps = CarCore.FPS;

        if (arduino.GetFirmwareRev()<0x120000) return; // not HardAta
        arduino.pinMode(11,Arduino.DEADMAN);
        // Set the digital input pin 11 as (PWM) DeadMan switch from xmtr
        arduino.pinMode(10,Arduino.DM_SERVO);
        // Set the digital output pin 10 as ESC servo under DeadMan control
        arduino.servoWrite(10,105); // start servo +15 degrees
        arduino.addInputListener(Arduino.REPORT_PULSECOUNT, ps);
        arduino.pinMode(8,Arduino.PULSECOUNT);
        arduino.DoPulseCnt(8,1000/fps/2);
    }

    public void init(CarData carData) {
        carData.addData("arduino", new ArduinoData(ps.getCount(),  aVoid -> arduino.Close()));
    }

    @Override
    public void getCameraImage(CarData carData) {
        camera.NextFrame();
        carData.addData("camera", camera);
    }

    @Override
    public void accelerate(boolean absolute, int angle) {
        arduino.setServoAngle(camera.getSpeedServoPin(), angle + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
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
