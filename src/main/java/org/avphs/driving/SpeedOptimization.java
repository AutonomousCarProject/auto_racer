package org.avphs.driving;

import org.avphs.coreinterface.CarData;

public class SpeedOptimization implements Drivable {

    @Override
    public int getSteeringAngle(CarData carData) {
        return 0;
    }

    @Override
    public int getThrottle(CarData carData) {
        return 0;
    }
}
