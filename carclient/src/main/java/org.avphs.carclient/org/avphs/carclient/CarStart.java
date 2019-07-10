package org.avphs.carclient;

import org.avphs.core.CarCore;

public class CarStart {

    public CarStart() {
        var core = new CarCore();
        core.init();
    }

    public static void main(String[] args) {
        new CarStart();
    }
}
