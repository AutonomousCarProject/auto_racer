package org.avphs.coreinterface;

public interface CarModule {

    /**
     * Called when the Core initializes, before any modules has been run.
     *
     * @param carData Of type CarData that holds data from each module.
     */
    void init(CarData carData);

    /**
     * Called once per frame. This is intended to run any code
     * a module has to run during the race.
     *
     * @param carData Holds all the data the car has.
     */
    void update(CarData carData);
}