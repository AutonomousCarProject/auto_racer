package org.avphs.core;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface CarModule extends Runnable {

    /**
     * Gets all instances of all CarModules, should probably only be called once from the Core.
     * @return A List of all active CarModules.
     */
    static List<CarModule> getInstances() {
        return ServiceLoader.load(CarModule.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
    }

    /**
     * Requests all needed modules from the Core.
     * @return An array of the requested module classes.
     */
    Class[] getDependencies();

    /**
     * Called when the Core initializes, before any modules has been run.
     * @param dependencies An array of this modules dependencies, provided by the Core.
     */
    void init(CarModule[] dependencies);

    /**
     * Returns this modules current commands to the core.
     * @return The array of commands to execute.
     */
    CarCommand[] commands();
}
