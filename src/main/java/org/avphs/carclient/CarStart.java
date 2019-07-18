package org.avphs.carclient;

import org.avphs.camera.FlyCamera;
import org.avphs.core.CarCore;
import org.avphs.car.Car;

public class CarStart implements Runnable {

    public CarStart() {
        CarCore core = new CarCore(new Car(new FlyCamera()));
        core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }

    @Override
    public void run() {

    }
}
