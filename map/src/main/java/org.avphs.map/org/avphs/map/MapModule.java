package org.avphs.map;

import org.avphs.core.CarModule;

public class MapModule implements CarModule {
    @Override
    public void update() {
        System.out.println("Map");
    }

    @Override
    public void run() {
        update();
    }
}
