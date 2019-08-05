package org.avphs.driving;

import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class DrivingModule implements CarModule {

    public DrivingModule() {

    }

    @Override
    public void init(CarData carData) {
        // TODO: Get RacingLine data
        // TODO: Initialize array of Straights and Turns
    }

    @Override
    public void update(CarData carData) {
        // TODO: Determine if on straight or turn
        // TODO: Call either DrivingStraights or DrivingTurns
        // TODO: Set speed and steer angle in carData
    }
}
