package org.avphs.driving.newDriving;

import org.avphs.coreinterface.CarData;
import org.avphs.image.ImageData;

public class DrivingTurns implements Drivable {



    @Override
    public int getSteeringAngle(CarData data) {
        return adjustSteeringAngle( ( (ImageData) data.getModuleData("image") ).wallBottom);
    }

    @Override
    public int getThrottle(CarData data)
    {
        return adjustThrottle( ( (ImageData) data.getModuleData("image") ).wallBottom);
    }

    private int direction = 1; //0 = Right, 1 = Left. 8/5/2019 18:30 - Set to 1 for debugging purposes. TODO: Figure out how I'm getting the turn direction of each turn.

    private final int MAX_STEERING_ANGLE_LEFT = -74; private final int MAX_STEERING_ANGLE_RIGHT = 88;

    public DrivingTurns()
    {

    }

    public int adjustThrottle(int[] imgWallBottoms)//Changes throttle along turns
    {

        //This method adjusts the steering angle of the car based on the danger level (Distance) of the turn's outside wall.
        int newThrottle;
                switch (getWallDanger(imgWallBottoms)) {
                    case 0://Speed up slightly
                        newThrottle = 2;
                        return newThrottle;
                    case 1://Slow down slightly
                        newThrottle = -2;
                        return newThrottle;
                    case 2://Slow down significantly
                        newThrottle = -3;
                        return newThrottle;
                    case 3://Slow down as much as possible
                        newThrottle = -4;
                        return newThrottle;
                    default:
                        return 0;
                }
    }

    /*8/5/2019 18:26 - Currently I'm thinking that using this method of keeping a certain distance from the wall will allow for the car to drive
    around turns, however it will probably drive pretty wobbly. TODO: Add case for which the car is in the sweet spot and can continue on its turn path.
     */

    public int adjustSteeringAngle(int[] imgWallBottoms) //Returns a number to add to the current steering angle of the car.
    {
        //This method adjusts the steering angle of the car based on the danger level (Distance) of the turn's outside wall.
        int newSteeringAngle;
        switch (direction)//Check turn direction
        {
            case 0://If the car is turning right
                switch (getWallDanger(imgWallBottoms)) {
                    case 0://Steer to the left slightly
                        newSteeringAngle = -10;
                        return newSteeringAngle;
                    case 1://Steer to the right slightly
                        newSteeringAngle = 10;
                        return newSteeringAngle;
                    case 2://Steer to the right significantly
                        newSteeringAngle = 40;
                        return newSteeringAngle;
                    case 3://Steer to the right as much as possible
                        newSteeringAngle = MAX_STEERING_ANGLE_RIGHT;
                        return newSteeringAngle;
                        default:
                            break;
                }
                break;

            case 1://If the car is turning left
                switch (getWallDanger(imgWallBottoms))
                {
                    case 0://Steer to the right slightly
                        newSteeringAngle = 10;
                        return newSteeringAngle;
                    case 1://Steer to the left slightly
                        newSteeringAngle = -10;
                        return newSteeringAngle;
                    case 2://Steer to the left significantly
                        newSteeringAngle = -40;
                        return newSteeringAngle;
                    case 3://Steer to the left as much as possible
                        newSteeringAngle = MAX_STEERING_ANGLE_LEFT;
                        return newSteeringAngle;
                        default:
                            break;
                }
                break;

                default:
                    break;
        }
        return 0;
    }


    /*Evaluates the how close the outside wall of the turn is to the car.
    Danger level varies from 0 to 3
    0:Wall is far away and you should probably move a little closer to it.
    1: Wall is somewhat close and you should probably steer a little away from it.
    2: Wall is close and you need to steer away from it.
    3. Wall is very close and you need to steer away from it as much as possible..
     */
    public int getWallDanger(int[] imgWallBottoms)
    {
        int pixHeight;

        switch (direction) {
            case 0://Right Turn
                for (int i = 0; i < 10; i++)//Looks at 10 closest wall pixels to the left edge of the image.
                {
                    pixHeight = 480 - imgWallBottoms[i];

                    //Evaluate how close the wall is
                    if (pixHeight > 200) {
                        return 0;
                    } else if (pixHeight > 140 && pixHeight <= 200) {
                        return 1;
                    } else if (pixHeight > 80 && pixHeight <= 140) {
                        return 2;
                    } else if (pixHeight <= 80) {
                        return 3;
                    }

                }
                break;
            case 1://Left Turn
                for (int i = 639; i > 629; i--)//Looks at 10 closest wall pixels to the right edge of the image.
                {
                    pixHeight = 480 - imgWallBottoms[i];

                    //Evaluate how close the wall is
                    if (pixHeight > 200) {
                        return 0;
                    } else if (pixHeight > 140 && pixHeight <= 200) {
                        return 1;
                    } else if (pixHeight > 80 && pixHeight <= 140) {
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
