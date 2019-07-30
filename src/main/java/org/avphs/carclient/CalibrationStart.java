package org.avphs.carclient;

import fly2cam.FlyCamera;
import org.avphs.core.CalibrationCore;
import org.avphs.car.Car;

public class CalibrationStart implements Runnable {

    public CalibrationStart() {

        FlyCamera cam = new fly2cam.FlyCamera();
        new CalibrationCore(new Car(cam), true);
    }

    public static void main(String[] args) {
        new CalibrationStart();
    }

    @Override
    public void run() {

    }
}
