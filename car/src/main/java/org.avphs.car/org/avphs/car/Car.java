package org.avphs.car;

import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.ArduinoIO;
import org.avphs.sbcio.PWMController;

public class Car implements ClientInterface {
    private Camera camera;
    private PWMController arduino;

    public Car(Camera camera, CarData cardata) {
        cardata.addData("traksim", ((SimCamera) camera).getTrakSim());

        this.camera = camera;
        camera.connect(4);
        this.arduino = new ArduinoIO();
    }

    public void init(CarData carData) {
        carData.addData("traksim", ((SimCamera) camera).theSim);
    }

    @Override
    public void getCameraImage(CarData carData) {
        camera.fetchNextFrame();
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
        arduino.close();
        camera.finish();
    }
}