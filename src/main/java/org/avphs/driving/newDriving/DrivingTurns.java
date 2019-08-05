package org.avphs.driving.newDriving;

import org.avphs.coreinterface.CarData;

public class DrivingTurns implements Drivable {



    @Override
    public int getSteeringAngle(CarData carData) {
        return 0;
    }

    @Override
    public int getThrottle(CarData carData) {
        return 0;
    }

    private int direction = 1; //0 = Right, 1 = Left

    private final int MAX_STEERING_ANGLE_INCREASE = 60;

    public DrivingTurns()
    {

    }

    public int adjustSteeringAngle(int[] imgWallBottoms) {
        int newSteeringAngle;
        switch (getWallDanger(imgWallBottoms)) {
            case 0:
                newSteeringAngle = getSteeringAngle() - (MAX_STEERING_ANGLE_INCREASE >> 2);
                //Lower steering angle
                break;
            case 1:
                newSteeringAngle = getSteeringAngle() + (MAX_STEERING_ANGLE_INCREASE >> 2);
                //Slightly raise steering angle
                break;
            case 2:
                newSteeringAngle = getSteeringAngle() + (MAX_STEERING_ANGLE_INCREASE >> 1);
                //Raise steering angle and slow down a bit.
                break;
            case 3:
                newSteeringAngle = getSteeringAngle() + (MAX_STEERING_ANGLE_INCREASE);
                //Raise steering angle as much as possible and slow down a lot.
                break;
        }
        return 0;
    }


    /*Evaluates the how close the outside wall of the turn is to the car.
    Danger level varies from 0 to 3
    0: Greater than 1.5m away (Far)
    1: Between 1.2 and 1.5m away (Medium)
    2: Between 0.9 and 1.2m away (Close)
    3. Less than 0.9m away (Very Close)
     */
    public int getWallDanger(int[] imgWallBottoms) {
        int pixHeight;

        switch (direction) {
            case 0://Right Turn
                for (int i = 0; i < 10; i++)//Looks at 10 closest wall pixels to the left edge of the image.
                {
                    pixHeight = 480 - imgWallBottoms[i];

                    //Evaluate how close the wall is
                    if (pixHeight > 200) {
                        return 0;
                    } else if (pixHeight < 140 && pixHeight <= 200) {
                        return 1;
                    } else if (pixHeight < 80 && pixHeight <= 140) {
                        return 2;
                    } else if (pixHeight <= 80) {
                        return 3;
                    }

                }
                break;
            case 1://Left Turn
                for (int i = 640; i > 630; i--)//Looks at 10 closest wall pixels to the right edge of the image.
                {
                    pixHeight = 480 - imgWallBottoms[i];

                    //Evaluate how close the wall is
                    if (pixHeight > 200) {
                        return 0;
                    } else if (pixHeight < 140 && pixHeight <= 200) {
                        return 1;
                    } else if (pixHeight < 80 && pixHeight <= 140) {
                        return 2;
                    } else if (pixHeight <= 80) {
                        return 3;
                    }

                }
                break;
            default:
                return 0;
        }

            return  0;
    }

}
