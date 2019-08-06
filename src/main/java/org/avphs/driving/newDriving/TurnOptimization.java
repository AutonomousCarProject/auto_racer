package org.avphs.driving.newDriving;

import org.avphs.coreinterface.CarData;

public class TurnOptimization implements Drivable {

    //TurnOptimization optimizes the driving to make turns as fast as possible.

    @Override
    public int getSteeringAngle(CarData carData)
    {
        return 0;
    }

    @Override
    public int getThrottle(CarData carData)
    {
        return 0;
    }


}
