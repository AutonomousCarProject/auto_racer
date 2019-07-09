package org.avphs.carclient;

import org.avphs.client.CarClient;
import org.avphs.core.CarCore;

public class CarStart implements CarClient {

    public CarStart() {
        var core = new CarCore();
        core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }
}
