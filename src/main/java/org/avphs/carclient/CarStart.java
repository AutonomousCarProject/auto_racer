package org.avphs.carclient;

import fly2cam.FlyCamera;
import org.avphs.camera.SimCamera;
import org.avphs.core.CarCore;
import org.avphs.car.Car;

public class CarStart implements Runnable {

    public CarStart() {

        FlyCamera cam = new fly2cam.FlyCamera();
        CarCore core = new CarCore(new Car(cam));
        //core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }

    @Override
    public void run() {

    }
}
