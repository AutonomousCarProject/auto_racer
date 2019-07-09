package org.avphs.position;

import org.avphs.core.CarModule;

import java.util.Collection;

public class PositionModule implements CarModule {

    @Override
    public Collection<Class> getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public void update() {
        System.out.println("Position");
    }

    @Override
    public void run() {
        update();
    }
}
