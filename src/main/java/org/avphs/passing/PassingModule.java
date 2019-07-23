package org.avphs.passing;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class PassingModule implements CarModule {
    public void init(CarData carData) {
        System.out.println("Passing init");
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Passing update");
    }

    public PassingModule() {

    }
}
