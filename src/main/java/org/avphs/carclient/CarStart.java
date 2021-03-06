package org.avphs.carclient;

import fly2cam.FlyCamera;
import org.avphs.core.RacingCore;
import org.avphs.car.Car;

public class CarStart implements Runnable {

    public CarStart() {

        FlyCamera cam = new fly2cam.FlyCamera();
        new RacingCore(new Car(cam), true);
        //core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }

    @Override
    public void run() {

    }
}
