package org.avphs.speed;

import org.avphs.core.CarModule;

public class SpeedModule implements CarModule {
    @Override
    public void update() {
        System.out.println("Speed");
    }

    @Override
    public void run() {
        update();
    }
}
