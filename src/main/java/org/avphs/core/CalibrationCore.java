package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.image.ImageModule;

public class CalibrationCore extends CarCore {
    public static enum CalibrationMode {
        TurnRad,
        PixelHeight,
        Fish,
        Throttle,
        TurnSpeed,
        SpeedChange
    }
    public CalibrationCore(Car car, CalibrationMode mode) {
        super(car, false);

        // Add Run-time Modules
        updatingCarModules.add(new ImageModule());

        init();
        startUpdatingModules();
    }
}
