package org.avphs.prerace;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

public class PreRaceModule implements CarModule {

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
        System.out.println("Pre Race");
    }
}
