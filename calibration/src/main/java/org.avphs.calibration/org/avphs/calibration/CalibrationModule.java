package org.avphs.calibration;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

public class CalibrationModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule[] dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {

    }
}