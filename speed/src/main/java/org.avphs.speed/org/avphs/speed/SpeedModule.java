package org.avphs.speed;

import org.avphs.core.CarModule;

import java.util.Collection;

public class SpeedModule implements CarModule {

    @Override
    public Collection<Class> getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public void update() {
        System.out.println("Speed");
    }

    @Override
    public void run() {
        update();
    }
}
