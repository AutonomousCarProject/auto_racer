
package org.avphs.coreinterface;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface CarModule {

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

    /**
     * Called once per frame. This is intended to run any code
     * a module has to run during the race.
     * @param carData Holds all the data the car has.
     */
    void update(CarData carData);
}