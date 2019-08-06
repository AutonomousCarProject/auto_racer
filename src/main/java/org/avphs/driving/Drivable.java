package org.avphs.driving;

import org.avphs.coreinterface.CarData;

public interface Drivable {

    int getSteeringAngle(CarData carData);

    int getThrottle(CarData carData);
}
