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

public class CarCore {
    private final int FPS = 15;
    private Queue<CarCommand> commandQueue = new PriorityQueue<>();
    private final List<CarModule> modules;
    private DrivingModule drivingModule;
    private ImageModule imageModule;
    private PositionModule positionTrackingModule;
    private RacingLineModule racingLineModule;
    private CalibrationModule calibrationModule;
    private MapModule mapModule;
    private WindowModule windowModule;
    private CarData carData;
    private Car car;
    private ArrayList<CarModule> updatingCarModules = new ArrayList<>();

    public CarCore(Car car) {
        this.car = car;
        modules = CarModule.getInstances();
        carData = new CarData();
        this.car.init(carData);
        // FIXME: Make this more dynamic
        modules.forEach(carModule -> {
            if (carModule instanceof DrivingModule) {
                drivingModule = (DrivingModule) carModule;
            } else if (carModule instanceof ImageModule) {
                imageModule = (ImageModule) carModule;
            } else if (carModule instanceof PositionModule) {
                positionTrackingModule = (PositionModule) carModule;
            } else if (carModule instanceof MapModule) {
                mapModule = (MapModule) carModule;
            } else if (carModule instanceof RacingLineModule) {
                racingLineModule = (RacingLineModule) carModule;
            } else if (carModule instanceof CalibrationModule) {
                calibrationModule = (CalibrationModule) carModule;
            } else if (carModule instanceof WindowModule) {
                car.getCameraImage(carData);
                windowModule = new WindowModule(carData);
            }
        });

        init();
        startModules();
    }

    public void init() {
        CarModule[] drivingInit = {racingLineModule};
        CarModule[] positionInit = {racingLineModule, mapModule};

        // FIXME: Make this more dynamic
        drivingModule.init(carData);
        imageModule.init(carData);
        positionTrackingModule.init(carData);
        mapModule.init(carData);

        updatingCarModules.add(windowModule);
        updatingCarModules.add(imageModule);
        updatingCarModules.add(positionTrackingModule);
        updatingCarModules.add(drivingModule);
        updatingCarModules.add(mapModule);

    }

    public void startModules() {
        //Start Updating Modules
        final ScheduledExecutorService carExecutorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Module Updater"));
        carExecutorService.scheduleAtFixedRate(this::update, 0, Math.round(1000.0 / FPS), TimeUnit.MILLISECONDS);

        // Start Listening for Commands
        final ScheduledExecutorService commandListeningExecutorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Command Listener"));
        commandListeningExecutorService.scheduleAtFixedRate(this::commandListen, 0, Math.round(1000.0 / FPS),
                TimeUnit.MILLISECONDS);

    }

    private void update() {
        car.getCameraImage(carData);
        for (CarModule module : updatingCarModules) {
            module.update(carData);
        }
    }

    private void commandListen() {
        for (CarModule module : updatingCarModules) {
            if (module.commands() != null) {
                for (var command : module.commands()) {
                    switch (command.command) {
                        case STOP_COMMAND:
                            car.stop();
                            break;
                        case STEER_COMMAND:
                            car.steer((boolean) command.parameters[0], (int) command.parameters[1]);
                            break;
                        case ACCELERATE_COMMAND:
                            car.accelerate((boolean) command.parameters[0], (int) command.parameters[1]);
                            break;
                    }
                }
            }
        }
    }

    public static class NamedThreadFactory implements ThreadFactory {
        private final String name;

        public NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }
}
