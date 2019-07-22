package org.avphs.car;

import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.ClientInterface;
import org.avphs.sbcio.Arduino;

public class Car implements ClientInterface {
    private Camera camera;
    private Arduino arduino;

    public Car(Camera camera)
    {
        this.camera = camera;
        camera.Connect(4);
<<<<<<< HEAD:src/main/java/org/avphs/car/Car.java

=======
>>>>>>> 31ddbc5906cd77ad53fea00499b6577be08aed5b:src/main/java/org/avphs/car/Car.java
        this.arduino = new ArduinoIO();

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

        arduino.close();

        camera.Finish();
    }
}
