package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.image.ImageModule;

public class CalibrationCore extends CarCore {
    public CalibrationCore(Car car, boolean needsCamera) {
        super(car, false);

        // Add Run-time Modules
        if(needsCamera){
            updatingCarModules.add(new ImageModule());
        }

        init();
        startUpdatingModules();
    }
}
