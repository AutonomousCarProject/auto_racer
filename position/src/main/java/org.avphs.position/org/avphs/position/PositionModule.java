package org.avphs.position;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

import java.util.Collection;

public class PositionModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Position");
    }
}
