package org.avphs.driving.newDriving;

import org.avphs.coreinterface.CarData;

public interface Drivable {

    int getSteeringAngle(CarData carData);

    int getThrottle(CarData carData);
}
