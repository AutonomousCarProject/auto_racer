package org.avphs.position;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.sbcio.ArduinoData;
import org.avphs.calibration.CalibrationModule;

import java.io.IOException;

public class PositionModule implements CarModule, CloseHook {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
    private float disBetweenAxle = (float) CalibrationModule.WHEEL_BASE;//deprecated
    private float distanceTraveled;
    private float wheelAngle;
    private float deltaPositionAngle;
    private int prevOdom = 0;
    private ArduinoData odom;


    //FOR TESTING THE CAR
    private PositionCarTesting pct = new PositionCarTesting();

    public PositionModule() {
    }


    @Override
    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        //send zeros because starting pos is 0. Change these to customize starting orientation of the car
        positionData = new PositionData(new float[]{0, 0}, 0, 0);


        //FOR TESTING THE CAR
        carData.addData("position", positionData);
    }

    @Override
    public void update(CarData carData) {
        odom = (ArduinoData) carData.getModuleData("arduino");//get arduino for odometer count
        int steer = (int) carData.getModuleData("driving");//get driving for steering angle
        computePosition(odom.getOdomCount() - prevOdom, steer);//calculate new position
        prevOdom = odom.getOdomCount();//set previous odometer count
        carData.addData("position", positionData);//update position in cardata


        //FOR CAR TESTING
        try {
            pct.writeToFile(positionData.getPosition()[0], positionData.getPosition()[1], positionData.getDirection(), odom.getOdomCount(), steer);//write car information to a text file for debugging purposes
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void computePosition(int odometerCount, float drivingData) {
        // find out if this is run before or after driving. If after, good, else: bad.

        wheelAngle = drivingData; //angle of servo
        distanceTraveled = (float) (odometerCount * CalibrationModule.CM_PER_ROTATION); //number of wheel turns * distance per wheelturn (in cm)

        float drivingArcRadius = CalibrationModule.getRadii((short) wheelAngle);//get the turn radius of the car given angle.

        if (drivingArcRadius == 0) {//FIXME: talk to ryan from calibration about straight forward, infinite turn radius
            //just drive straight forward
            convertPosition(0, distanceTraveled);
        } else {
            //if turning
            //distance traveled / circumference give fraction traveled around circle. Multiply by 360 to get the number of degrees around circle we have traveled
            deltaPositionAngle = (float) (360 * distanceTraveled / (Math.PI * Math.pow(drivingArcRadius, 2)));
            if (deltaPositionAngle < 90 || deltaPositionAngle > 270) {//take that angle travelend along the circle, and get an x,y coordinate from that
                convertPosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (drivingArcRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

            } else {//if(deltaPositionAngle > 90), turning left
                convertPosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (drivingArcRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
            }

            //given angle traveled around the circle, update the direction we are facing
            if (wheelAngle > 91) {//if turning right
                computeDirection(deltaPositionAngle);//update direction with delta direction  because clockwise = positive
            } else if (wheelAngle < 91) {//if turning left
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
            }
        }
        computeSpeed(odometerCount);//get the speed we are traveling

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());//update the positiondata

    }

    private void computeDirection(float newDirection) {//adds a new direction to the old direction
        float direction = positionData.getDirection();//get the original direction the car was facing
        direction += newDirection;//add the changed direction
        if (direction >= 360 || direction < 0) {//get the direction between 0 and 360
            direction %= 360;
            if (direction < 360) {//because % returns remainder instead of modulus, we need to add 360 when the angle is < 0
                direction += 360;
            }
        }
        positionData.updateDirection(direction);//add new dir to posdata
    }

    private void computeSpeed(int odometerCount) {
        float speed = (odometerCount - prevOdom) * (float) CalibrationModule.CM_PER_ROTATION * 15f;//*15 because convert odometerCount per 66.67 milliseconds to OdometerCount per second.
        positionData.updateSpeed(speed);
    }

    private void convertPosition(float x, float y) {//convert changes to the x,y position of the car (which are relative to the direction the car is facing) to changes to the x,y position of the car relative to the map.
        //FIXME x and y are currently in cm, not in the map coordinates.
        if (!(x == 0 && y == 0)) {//if it has moved
            float[] temp = pol(x, y);//convert to polar
            temp = cart(temp[0], temp[1] - positionData.getDirection());//rotate by angle of car to get x y coordinates relative to the map
            positionData.updatePosition(temp);
        }
    }

    private float[] pol(float x, float y) {//to polar coordinates
        return new float[]{(float) Math.sqrt(x * x + y * y), (float) Math.toDegrees(Math.atan2(y, x))};
    }

    private float[] cart(float l, float d) {//to cartesian
        return new float[]{(float) (l * Math.cos(Math.toRadians(d))), (float) (l * Math.sin(Math.toRadians(d)))};
    }


    @Override
    public void onClose() {//duncans stuff for printing to a file
        try {
            pct.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}