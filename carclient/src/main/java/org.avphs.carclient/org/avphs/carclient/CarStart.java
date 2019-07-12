package org.avphs.carclient;

import org.avphs.camera.FlyCamera;
import org.avphs.core.CarCore;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.fakefirm.ArduinoIO;
import org.avphs.traksim.DriverCons;

public class CarStart implements ClientInterface, Runnable {

    private ArduinoIO servos;
    private FlyCamera camera;

    public CarStart() {
        camera = new FlyCamera();
        camera.Connect(4);

        servos = new ArduinoIO();
        servos.open();

        var core = new CarCore(this);
        core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }

    @Override
    public void accelerate(boolean absolute, int angle) {
        servos.setServoAngle(DriverCons.D_GasServo, angle + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
        servos.setServoAngle(DriverCons.D_SteerServo, angle + 90);
    }

    @Override
    public void stop() {
        accelerate(true, 0);
        steer(true, 0);
        servos.close();
        camera.Finish();
    }

    @Override
    public void run() {

    }
}
