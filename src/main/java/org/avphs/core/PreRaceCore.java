package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.image.ImageModule;

/**
 * PreRaceCore is run before the race to create the map.
 * @author kevin
 * @see org.avphs.core.CarCore
 */
public class PreRaceCore extends CarCore {

    public PreRaceCore(Car car, boolean showWindow) {
        super(car, showWindow);

        // Add Run-time Modules
        updatingCarModules.add(new ImageModule());

        init();
        startUpdatingModules();
    }
}