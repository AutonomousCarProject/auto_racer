/*package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.sbcio.ArduinoData;
import org.avphs.traksim.TrakSim;

import java.sql.SQLOutput;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
    private float disBetweenAxle = 0;
    private float distanceTraveled;
    private float wheelAngle;
    private float deltaPositionAngle;
    private TrakSim ts;
    private int angle = 0;
    private float cumulatedDistance = 0;


    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{40.0f,56.0f}, 0, 0); //TEMPORARY
        ts = new TrakSim();
    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] {
                CarCommand.accelerate(true, 15),
                CarCommand.steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        if(cumulatedDistance > 33 && cumulatedDistance < 104){
            angle = 15;
        }
        else if (cumulatedDistance > 104 && cumulatedDistance < 137){
            angle = 0;
        }
        else if(cumulatedDistance > 137){
            angle = -15;
        }

        ArduinoData odom = (ArduinoData) carData.getModuleData("arduino");
        int steer = (int) carData.getModuleData("driving");
        cumulatedDistance += (float)ts.GetDistance(false);
        //System.out.println(ts.GetDistance(false));
        computePosition((float)ts.GetDistance(true), angle);
        carData.addData("position", positionData);

    }

    private void computePosition(float odometerCount, float drivingData){
        float drivingArcRadius;
        disBetweenAxle = 1f;//FIXME: data from calibration
        // find out if this is run before or after driving. If after, good, else: bad.

        wheelAngle = drivingData + 90; //angle of servo
        distanceTraveled = odometerCount; //in park meters

        //FIXME find out the error in the servo value, and add that value to "> 90" and subtract from "< 90",defaulted at 2
        if (wheelAngle > 91) { //if turning right
            drivingArcRadius = (float) (disBetweenAxle / Math.cos(Math.toRadians(-wheelAngle)));
        } else if (wheelAngle < 89) { //if turning left
            drivingArcRadius = (float) (disBetweenAxle / Math.cos(Math.toRadians(wheelAngle)));
        }//
        else {
            drivingArcRadius = 0;
        }
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
            }
            if (wheelAngle < 91) {//if turning left
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
            }
        }
        //computeSpeed(odometerCount);

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());
        System.out.println("Position = ("+(positionData.getPosition()[0])+","+(positionData.getPosition()[1])+")");

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

    private void computeSpeed(int odometerCount) {
        //FIXME: data from calibration
        float speed = odometerCount * .15f * 30f;//*30 because convert odometerCount per 33.33 milliseconds to OdometerCount per second.
        positionData.updateSpeed(speed);
    }

    private void convertPosition(float x, float y) {
        //FIXME x and y are currently in cm, not in the virtual world coordinates.
        if(!(x == 0 && y == 0)) {
            float[] pos = positionData.getPosition();
            float[] temp = pol(x, y);
            temp = cart(temp[0], temp[1] - positionData.getDirection());
            //System.out.println("x"+temp[0]);
            //System.out.println("y"+temp[1]);
            pos[0] += temp[0];
            pos[1] += temp[1];
            positionData.updatePosition(temp);
        }
    }

    private float[] pol(float x, float y){//to polar coordinates
        return new float[] {(float) Math.sqrt(x*x + y*y), (float) Math.toDegrees(Math.atan2(y,x))};
    }

    private float[] cart(float l, float d){//to cartesian
        return new float[] {(float) (l * Math.cos(Math.toRadians(d))), (float) (l * Math.sin(Math.toRadians(d)))};
    }
}*/