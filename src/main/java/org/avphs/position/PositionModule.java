package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
    private float disBetweenAxle = 0;
    private float distanceTraveled;
    private float wheelAngle;
    private float deltaPositionAngle;

    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{0, 0}, 0, 0); //TEMPORARY

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        if (true) return;
        float drivingArcRadius;
        disBetweenAxle = (float) 32.5f;
        // find out if this is run before or after driving. If after, good, else: bad.
        Object drivingData = carData.getModuleData("driving");
        wheelAngle = (float) drivingData; //angle of servo
        distanceTraveled = 0;//getDistance(); //number of wheel turns
        //FIXME find out the error in the servo value, and add that value to "> 90" and subtract from "< 90",defaulted at 2

        //FIXME get data from calibration
        if (wheelAngle > 91) { //if turning right
            drivingArcRadius = (float) (Math.tan(wheelAngle - 90) * disBetweenAxle);
        } else if (wheelAngle < 89) { //if turning left
            drivingArcRadius = (float) (Math.tan(wheelAngle + 90) * disBetweenAxle);
        }//
        else {
            drivingArcRadius = 0;
        }
        if (drivingArcRadius == 0) {
            //just drive straight forward
            computePosition(0, distanceTraveled);
        } else {
            //if turning
            deltaPositionAngle = (float) (360 * distanceTraveled / (Math.PI * Math.pow(drivingArcRadius, 2)));//compute the length of the path around the circle the car has taken, and then get the angle of that
            if (wheelAngle > 90) {//if turning right
                computeDirection(deltaPositionAngle);//update direction with delta direction  because clockwise = positive
                if (deltaPositionAngle <= 90) {
                    computePosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

                } else {//if(deltaPositionAngle > 90), turning left
                    computePosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                }
            }
            if (wheelAngle < 90) {//if turning left
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
                if (deltaPositionAngle <= 90) {
                    computePosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                } else {//if(deltaPositionAngle > 90)
                    computePosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                }
            }
        }


        //ORDER OF FUNCTION CALLING THAT HAPPENS EVERY TIME
        if (wheelAngle > 90) {
            computeDirection(deltaPositionAngle - 90);
        } else {
            computeDirection(deltaPositionAngle + 90);
        }
        computeSpeed();
        carData.addData("position", positionData);

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());

    }

    private void computeDirection(float newDirection) {
        float direction = positionData.getDirection();
        direction += newDirection;
        if (direction >= 360) {
            direction -= 360;
        } else if (direction < 0) {
            direction += 360;
        }
        positionData.updateDirection(direction);
    }

    private void computeSpeed() {
        //GET SPIN COUNT - prevSpins;
        //INSERT calibrationn function to convert shaft spin to distance
        float speed = 0;//calibration.getDistance_Elapsed(); //0 is temporary
        //depending on calibration function, this may need conversion to m/s
        positionData.updateSpeed(speed);
    }

    private void computePosition(float x, float y) {
        float tempdir = positionData.getDirection();
        tempdir = tempdir + (float) (90 - Math.toDegrees(Math.atan2(positionData.getPosition()[1], positionData.getPosition()[0])));
        float tempx = (float) (Math.abs(x) * Math.cos(Math.toRadians(tempdir)) + Math.abs(y) * Math.cos(Math.toRadians(tempdir)));
        float tempy = (float) (Math.abs(x) * Math.sin(Math.toRadians(tempdir)) + Math.abs(y) * Math.sin(Math.toRadians(tempdir)));
        //FIXME x and y are currently in cm, not in the virtual world coordinates.

        positionData.updatePosition(new float[]{tempx, tempy});
    }

    private float getDistance(){
        return 0f;
    }
}
