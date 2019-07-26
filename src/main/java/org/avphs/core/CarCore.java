package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.window.WindowModule;

import java.io.IOException;
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
    long targetMillsPerFrame = Math.round(1000.0 / FPS);
    private ArrayList<CloseHook> closeHookModules = new ArrayList<>();
    private ScheduledExecutorService carExecutorService;
    ArrayList<CarModule> updatingCarModules = new ArrayList<>(); // All of the modules that will be run each frame.
    private boolean closing;

    /**
     * Constructor that instantiates the car.
     * @param car the object that controls the car.
     */
    CarCore(Car car) {

        carData = new CarData();
        this.car = car;
        car.init(carData);


        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    public class ShutdownHook extends Thread {
        @Override
        public void run() {
            closing = true;
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
        if (closing) return;

        car.update(carData);
        car.getCameraImage(carData);
        for (CarModule module : updatingCarModules) {
            module.update(carData);
        }
    }

    /**
     * This class provides a named thread. Great for profiling code.
     *
     * @author kevin
     * @see java.util.concurrent.ThreadFactory
     */
    protected static class NamedThreadFactory implements ThreadFactory {
        private final String name;

        NamedThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, name);
        }
    }
}
