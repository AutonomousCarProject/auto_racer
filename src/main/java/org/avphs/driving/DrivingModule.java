package org.avphs.driving;

import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.driving.newDriving.DrivingTurns;
import org.avphs.image.ImageData;

public class DrivingModule implements CarModule {

    public int currentWheelAngle = 0;

    private ImageData imageData;

    private DrivingTurns turns = new DrivingTurns();

    public DrivingModule() {

    }

    @Override
    public void init(CarData carData) {
        // TODO: Get RacingLine data
        // TODO: Initialize array of Straights and Turns
        System.out.println("hello");
    }

    @Override
    public void update(CarData carData) {
        // TODO: Determine if on straight or turn
        // TODO: Call either DrivingStraights or DrivingTurns
        // TODO: Set speed and steer angle in carData
        //System.out.println("Steering Angle " + turns.getSteeringAngle(carData));
        //System.out.println("Throttle: " + turns.getThrottle(carData));


    }
}
