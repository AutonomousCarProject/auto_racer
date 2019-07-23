package org.avphs.preracetraksimclient;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.PreRaceCore;

public class PreRaceTrakSimClient {

    private PreRaceTrakSimClient() {
        new PreRaceCore(new Car(new SimCamera()), true);
    }

    public static void main(String[] args) {
        new PreRaceTrakSimClient();
    }
}
