package org.avphs.steering;

import org.avphs.core.CarModule;

import java.util.Collection;

public class SteeringModule implements CarModule {

    @Override
    public Collection<Class> getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public void update() {
        System.out.println("Steering3");
    }

    @Override
    public void run() {
        update();
    }
}
