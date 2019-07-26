package org.avphs.carclient;

import fly2cam.FlyCamera;
import org.avphs.camera.FakeCamera;
import org.avphs.core.CalibrationCore;
import org.avphs.car.Car;

public class CalibrationStart implements Runnable {

    public CalibrationStart() {

        FakeCamera cam = new FakeCamera();
        new CalibrationCore(new Car(cam), true);
    }

    public static void main(String[] args) {
        new CalibrationStart();
    }

    @Override
    public void run() {

    }
}
