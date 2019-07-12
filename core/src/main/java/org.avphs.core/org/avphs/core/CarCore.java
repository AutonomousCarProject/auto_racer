package org.avphs.core;

import org.avphs.calibration.CalibrationModule;
import org.avphs.coreinterface.*;
import org.avphs.driving.DrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;
import org.avphs.position.PositionModule;
import org.avphs.racingline.RacingLineModule;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class CarCore {
    private final int FPS = 15;
    public ClientInterface client;
    private Queue<CarCommand> commandQueue = new PriorityQueue<>();
    private final List<CarModule> modules = CarModule.getInstances();
    private DrivingModule drivingModule;
    private ImageModule imageModule;
    private PositionModule positionTrackingModule;
    private RacingLineModule racingLineModule;
    private CalibrationModule calibrationModule;
    private MapModule mapModule;
    private CarData carData;
    private ArrayList<CarModule> updatingCarModules = new ArrayList<>();

    public CarCore(ClientInterface client) {
        this.client = client;
        carData = new CarData();
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
            }
        });

        init();
        startModules();
    }

    public void init() {
        // FIXME: Make this more dynamic
        // INIT Driving
        CarModule[] drivingInit = {calibrationModule, racingLineModule};
        drivingModule.init(drivingInit);
        // INIT Image
        imageModule.init(calibrationModule);
        // INIT Position
        CarModule[] positionInit = {racingLineModule, mapModule};
        positionTrackingModule.init(positionInit);

        updatingCarModules.add(imageModule);
        updatingCarModules.add(positionTrackingModule);
        updatingCarModules.add(drivingModule);

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
        for (CarModule module : updatingCarModules) {
            module.update(carData);
            for (var command : module.commands()) {
                switch (command.command) {
                    case STOP_COMMAND:
                        client.stop();
                        break;
                    case STEER_COMMAND:
                        client.steer((boolean) command.parameters[0], (int) command.parameters[1]);
                        break;
                    case ACCELERATE_COMMAND:
                        client.accelerate((boolean) command.parameters[0], (int) command.parameters[1]);
                        break;
                }
            }
        }
    }

    private void commandListen() {
        modules.forEach(m -> commandQueue.addAll(Arrays.asList(m.commands())));
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
