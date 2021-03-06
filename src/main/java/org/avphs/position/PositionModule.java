package org.avphs.position;

import org.avphs.calibration.CalibrationModule;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.sbcio.ArduinoData;

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
        positionData = new PositionData(new float[]{0, 0}, 0, 0); //TEMPORARY


        //FOR TESTING THE CAR
        carData.addData("position", positionData);
    }

    @Override
    public void update(CarData carData) {
        ArduinoData odom = (ArduinoData) carData.getModuleData("arduino");
        int steer = (int) carData.getModuleData("driving");
        computePosition(odom.getOdomCount() - prevOdom, steer);
        prevOdom = odom.getOdomCount();
        carData.addData("position", positionData);


        //FOR CAR TESTING
        try {
            pct.writeToFile(positionData.getPosition()[0], positionData.getPosition()[1], positionData.getDirection(), odom.getOdomCount(), steer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void computePosition(int odometerCount, float drivingData) {
        // find out if this is run before or after driving. If after, good, else: bad.

        wheelAngle = drivingData; //angle of servo
        distanceTraveled = (float) (odometerCount * CalibrationModule.CM_PER_ROTATION); //number of wheel turns

        //FIXME: Get table values from calibraiton for wheel angle to turn radius
        float drivingArcRadius = CalibrationModule.getAngles((short) wheelAngle);

        if (drivingArcRadius == 0) {
            //just drive straight forward
            convertPosition(0, distanceTraveled);
        } else {
            //if turning
            deltaPositionAngle = (float) (360 * distanceTraveled / (Math.PI * Math.pow(drivingArcRadius, 2)));//compute the length of the path around the circle the car has taken, and then get the angle of that
            if (deltaPositionAngle < 90 || deltaPositionAngle > 270) {
                convertPosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (drivingArcRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

            } else {//if(deltaPositionAngle > 90), turning left
                convertPosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (drivingArcRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
            }

            if (wheelAngle > 91) {//if turning right
                computeDirection(deltaPositionAngle);//update direction with delta direction  because clockwise = positive
            } else if (wheelAngle < 91) {//if turning left
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
            }
        }
        computeSpeed(odometerCount);

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());
        //System.out.println("Position = (" + Math.round(positionData.getPosition()[0]) + "," + positionData.getPosition()[1] + ")");

    }

    private void computeDirection(float newDirection) {
        float direction = positionData.getDirection();
        direction += newDirection;
        if (direction >= 360 || direction < 0) {
            direction %= 360;
            if (direction < 360) {
                direction += 360;
            }
        }
        positionData.updateDirection(direction);
    }

    private void computeSpeed(int odometerCount) {
        float speed = (odometerCount - prevOdom) * (float) CalibrationModule.CM_PER_ROTATION * 15f;//*30 because convert odometerCount per 33.33 milliseconds to OdometerCount per second.
        positionData.updateSpeed(speed);
    }

    private void convertPosition(float x, float y) {
        //FIXME x and y are currently in cm, not in the virtual world coordinates.
        if (!(x == 0 && y == 0)) {
            float[] temp = pol(x, y);
            temp = cart(temp[0], temp[1] - positionData.getDirection());
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
    public void onClose() {
        try {
            pct.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}