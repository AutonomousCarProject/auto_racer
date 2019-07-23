package org.avphs.core;

import org.avphs.calibration.CalibrationModule;
import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.driving.DrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;
import org.avphs.position.PositionModule;
import org.avphs.racingline.RacingLineModule;
import org.avphs.window.WindowModule;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.avphs.coreinterface.CarCommandType.*;

public class RacingCore extends CarCore {

    public RacingCore(Car car) {
        super(car);

        this.car.init(carData);

        updatingCarModules.add(new DrivingModule());
        updatingCarModules.add(new ImageModule());
        updatingCarModules.add(new PositionModule());
        updatingCarModules.add(new RacingLineModule());
        updatingCarModules.add(new MapModule());
        updatingCarModules.add(new WindowModule(carData));

        init();
        startModules();
    }


}