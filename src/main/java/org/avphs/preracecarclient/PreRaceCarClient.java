package org.avphs.preracecarclient;

import fly2cam.FlyCamera;
import org.avphs.car.Car;
import org.avphs.core.PreRaceCore;

public class PreRaceCarClient {

    private PreRaceCarClient() {
        new PreRaceCore(new Car(new FlyCamera()), true);
    }

    public static void main(String[] args) {
        new PreRaceCarClient();
    }
}
