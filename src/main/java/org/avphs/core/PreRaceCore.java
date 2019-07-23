package org.avphs.core;

import org.avphs.car.Car;

public class PreRaceCore extends CarCore {

    public PreRaceCore(Car car, boolean showWindow) {
        super(car, showWindow);

        // Add Run-time Modules

        init();
        startUpdatingModules();
    }
}
