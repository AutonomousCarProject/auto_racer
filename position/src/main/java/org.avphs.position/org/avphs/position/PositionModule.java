package org.avphs.position;

import org.avphs.core.CarModule;

public class PositionModule implements CarModule {

    @Override
    public void update() {
        System.out.println("Position");
    }

    @Override
    public void run() {
        update();
    }
}
