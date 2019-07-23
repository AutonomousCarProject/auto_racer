package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.window.WindowModule;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

abstract class CarCore {
    private static final int FPS = 15;
    protected CarData carData;
    protected Car car;
    ArrayList<CarModule> updatingCarModules = new ArrayList<>();

    CarCore(Car car, boolean showWindow) {
        carData = new CarData();
        this.car = car;
        car.init(carData);

        if (showWindow) {
            updatingCarModules.add(new WindowModule(carData));
        }
    }

    void startUpdatingModules() {
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

    public void init() {
        car.getCameraImage(carData);
        for (CarModule carModule : updatingCarModules) {
            carModule.init(carData);
        }
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
                for (CarCommand command : module.commands()) {
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

        private NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }


    public static int getFPS() {
        return FPS;
    }
}
