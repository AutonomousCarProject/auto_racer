package org.avphs.image.tools;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.PreRaceImageCore;

public class PreRaceImageClient {
    private PreRaceImageClient() {
        new PreRaceImageCore(new Car(new SimCamera()));
    }

    public static void main(String[] args) {
        new PreRaceImageClient();
    }
}
