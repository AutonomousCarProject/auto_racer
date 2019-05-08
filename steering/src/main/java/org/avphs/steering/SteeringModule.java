package org.avphs.steering;

import org.avphs.core.CarModule;

public class SteeringModule implements CarModule {
    @Override
    public void update() {
        System.out.println("Steering");
    }
}
