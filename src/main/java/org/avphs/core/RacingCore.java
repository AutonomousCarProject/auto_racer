package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.detection.ObjectDetectionModule;
import org.avphs.driving.DrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapRacingModule;
import org.avphs.passing.PassingModule;
import org.avphs.position.PositionModule;
import org.avphs.racingline.RacingLineModule;

import java.io.IOException;

/**
 * RacingCore is run during the race.
 *
 * @author kevin
 * @see org.avphs.core.CarCore
 */
public class RacingCore extends CarCore {

    public RacingCore(Car car, boolean showWindow) {
        super(car, showWindow);

        // Add Run-time Modules
        updatingCarModules.add(new DrivingModule());
        updatingCarModules.add(new ImageModule());
        try {
            updatingCarModules.add(new PositionModule());
        } catch (IOException e) {
            e.printStackTrace();
        }
        updatingCarModules.add(new RacingLineModule());
        updatingCarModules.add(new MapModule());
        updatingCarModules.add(new ObjectDetectionModule());
        updatingCarModules.add(new PassingModule());

        init();
        startUpdatingModules();
    }


}