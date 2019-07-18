package org.avphs.coreinterface;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface PreRaceModule {

    /**
     * Gets all instances of all PreRaceModules, should probably only be called once from the Core.
     * @return A List of all active PreRaceModules.
     */
    static List<PreRaceModule> getInstances() {
        return ServiceLoader.load(PreRaceModule.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
    }

    /**
     * Called when the Core initializes, before any modules has been run.
     * @param carData Of type CarData that holds data from each module.
     */
    void init(CarData carData);

    /**
     * Returns this modules current commands to the core.
     * @return The array of commands to execute.
     */
    CarCommand[] commands();

    void update(CarData carData);
}
