package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.window.WindowModule;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This Class is the core for both PreRaceCore as well as RacingCore.
 *
 * @author kevin
 * @see PreRaceCore
 * @see RacingCore
 */
public abstract class CarCore {
    public static final int FPS = 15; // Frames per second car Tries to run
    protected CarData carData; // The data from all of the modules.
    protected Car car; // the object that can actually control the car.
    ArrayList<CarModule> updatingCarModules = new ArrayList<>(); // All of the modules that will be run each frame.
    ArrayList<CloseHook> closeHookModules = new ArrayList<>();
    ScheduledExecutorService carExecutorService;

    /**
     * Constructor that instantiates the car.
     *
     * @param car        the object that controls the car.
     * @param showWindow True for the JFrame window to appear, false for it not to appear.
     */
    CarCore(Car car, boolean showWindow) {
        carData = new CarData();
        this.car = car;
        car.init(carData);

        if (showWindow) {
            updatingCarModules.add(new WindowModule(carData));
        }

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public class ShutdownHook extends Thread {
        @Override
        public void run() {
            for (CloseHook carModule : closeHookModules) {
                carModule.onClose();
            }
        }
    }

    /**
     * Begins the threads that update each module in updatingCarModules.
     * This Method is called only once during initialization.
     */
    void startUpdatingModules() {
        //Start Updating Modules
        carExecutorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Module Updater"));
        carExecutorService.scheduleAtFixedRate(this::update, 0, Math.round(1000.0 / FPS), TimeUnit.MILLISECONDS);

        // Start Listening for Commands
        final ScheduledExecutorService commandListeningExecutorService =
                Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("Command Listener"));
        commandListeningExecutorService.scheduleAtFixedRate(this::commandListen, 0, Math.round(1000.0 / FPS),
                TimeUnit.MILLISECONDS);
    }

    /**
     * Goes through each module in updatingCarModules, and initializes it. An image is
     * also fetched from the car, and stored in carData.
     */
    public void init() {
        car.getCameraImage(carData);
        for (CarModule carModule : updatingCarModules) {
            carModule.init(carData);
            if (carModule instanceof CloseHook) {
                closeHookModules.add((CloseHook) carModule);
            }
        }
    }

    /**
     * T-his method is called each frame, and updates all of the car modules. During this
     * process, and image is fetched from the car and stored in carData.
     */
    private void update() {
        car.update(carData);
        car.getCameraImage(carData);
        for (CarModule module : updatingCarModules) {
            module.update(carData);
        }
    }

    /**
     * This function is called once every frame time. It checks for any commands
     * a module has called.
     * NOTE: this function can be out of sync with update, this will likely run faster.
     */
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

    /**
     * This class provides a named thread. Great for profiling code.
     *
     * @author kevin
     * @see java.util.concurrent.ThreadFactory
     */
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
}
