package org.avphs.traksimclient;

import org.avphs.camera.FakeCamera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.RacingCore;

public class TrakSimClient {

    public TrakSimClient() {
        new RacingCore(new Car(new SimCamera()), true);
    }

    public static void main(String[] args) {
        System.out.println("Hello world!");
        new TrakSimClient();
    }
}
