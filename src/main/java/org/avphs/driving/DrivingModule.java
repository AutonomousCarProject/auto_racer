package org.avphs.driving;

import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageData;
import org.avphs.car.Car;

public class DrivingModule implements CarModule {

    public int currentWheelAngle = 0;
    public int currentThrottle = 14;

    private ImageData imageData;
    private final Car car;

    private DrivingTurns turns = new DrivingTurns();

    public DrivingModule(Car car) {
        this.car = car;
    }

    @Override
    public void init(CarData carData) {
        // TODO: Get RacingLine data
        // TODO: Initialize array of Straights and Turns
        carData.addData("driving", currentWheelAngle);

    }


    @Override
    public void update(CarData carData) {
        // TODO: Determine if on straight or turn
        // TODO: Call either DrivingStraights or DrivingTurns
        // TODO: Set speed and steer angle in carData


        //NOTE: EVERYTHING IN DRIVING UPDATE ON THIS BRANCH IS ONLY DESIGNED TO WORK FOR TURNS.
        //8/6/2019 11:45 - UPDATE IS DESIGNED TO ONLY DRIVE ALONG TURNS AND ONLY USES IMG DATA.
        //Update the wheel angle and throttle.

        currentThrottle += turns.getThrottle(carData);
        currentWheelAngle += turns.getSteeringAngle(carData);

        //Makes sure the throttle and wheel angle don't surpass the maximum and minumum values. (Note: currentThrottle is only set to a max of 20 for debugging purposes to keep the car at a slow, steady speed.)
        if(currentThrottle > 20)
        {
            currentThrottle = 20;
        }
        if (currentThrottle < 14)
        {
            currentThrottle = 14;
        }
        if (currentWheelAngle < -74)
        {
            currentWheelAngle = -74;
        }
        if (currentWheelAngle > 88)
        {
            currentWheelAngle = 88;
        }

        //UNCOMMENT STUFF FOR TESTING
        //Move the car now.
        car.steer(true, currentWheelAngle);
        car.accelerate(true, currentThrottle);
        //System.out.println("Throttle: " + currentThrottle);
        // System.out.println("Steering Angle " + currentWheelAngle);



    }
}
