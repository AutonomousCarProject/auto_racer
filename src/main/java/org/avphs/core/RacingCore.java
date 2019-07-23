package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.driving.DrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;
import org.avphs.position.PositionModule;
import org.avphs.racingline.RacingLineModule;
import org.avphs.window.WindowModule;

public class RacingCore extends CarCore {

    public RacingCore(Car car, boolean showWindow) {
        super(car, showWindow);

        // Add Run-time Modules
        updatingCarModules.add(new DrivingModule());
        updatingCarModules.add(new ImageModule());
        updatingCarModules.add(new PositionModule());
        updatingCarModules.add(new RacingLineModule());
        updatingCarModules.add(new MapModule());

        init();
        startUpdatingModules();
    }


}