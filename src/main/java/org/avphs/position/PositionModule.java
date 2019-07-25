package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.sbcio.ArduinoData;
import org.avphs.traksim.TrakSim;

import java.util.ArrayList;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
    private float disBetweenAxle = 0;
    private float distanceTraveled;
    private float wheelAngle;
    private float deltaPositionAngle;
    private TrakSim ts;
    private int angle;


    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{0, 0}, 0, 0); //TEMPORARY
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
        ArduinoData odom = (ArduinoData) carData.getModuleData("arduino");
        int steer = (int) carData.getModuleData("driving");
        //computePosition(odom.count,steer);
        computePosition((float)ts.GetDistance(false), angle);
        carData.addData("position", positionData);
        if(ts.GetDistance(false) > 33 && ts.GetDistance(false) < 105){
            angle = 15;
        }
        else if (ts.GetDistance(false) > 105 && ts.GetDistance(false) < 135){
            angle = 0;
        }
        else if(ts.GetDistance(false) > 138){
            angle = -15;
        }
    }

    private void computePosition(float odometerCount, float drivingData){
        float drivingArcRadius;
        disBetweenAxle = 32.5f;//FIXME: data from calibration
        // find out if this is run before or after driving. If after, good, else: bad.

        //wheelAngle = drivingData; //angle of servo
        wheelAngle = drivingData + 90;
        //distanceTraveled = odometerCount * 15f; //number of wheel turns FIXME: data from calibraiton
        distanceTraveled = odometerCount * 8f; //used for tracksim for conversion from park meters to meters. may need to fix

        //FIXME find out the error in the servo value, and add that value to "> 90" and subtract from "< 90",defaulted at 2
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
            convertPosition(0, distanceTraveled);
        } else {
            //if turning
            deltaPositionAngle = (float) (360 * distanceTraveled / (Math.PI * Math.pow(drivingArcRadius, 2)));//compute the length of the path around the circle the car has taken, and then get the angle of that
            if (wheelAngle > 90) {//if turning right
                if (deltaPositionAngle <= 90) {
                    convertPosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

                } else {//if(deltaPositionAngle > 90), turning left
                    convertPosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                }
                computeDirection(deltaPositionAngle);//update direction with delta direction  because clockwise = positive
            }
            if (wheelAngle < 90) {//if turning left
                if (deltaPositionAngle <= 90) {
                    convertPosition((float) (drivingArcRadius + drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                } else {//if(deltaPositionAngle > 90)
                    convertPosition((float) (drivingArcRadius - drivingArcRadius * Math.cos(deltaPositionAngle)), (float) (drivingArcRadius * Math.sin(deltaPositionAngle)));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
                }
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
            }
        }


        //ORDER OF FUNCTION CALLING THAT HAPPENS EVERY TIME
        if (wheelAngle > 90) {
            computeDirection(deltaPositionAngle - 90);
        } else {
            computeDirection(deltaPositionAngle + 90);
        }
        computeSpeed(odometerCount);

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());
        System.out.println("Position = ("+Math.round(positionData.getPosition()[0])+","+positionData.getPosition()[1]+")");

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

    private void computeSpeed(float odometerCount) {
        //FIXME: data from calibration
        float speed = odometerCount * .15f * 30f;//*30 because convert odometerCount per 33.33 milliseoncs to OdometerCount per second.
        positionData.updateSpeed(speed);
    }

    private void convertPosition(float x, float y) {
        //FIXME x and y are currently in cm, not in the virtual world coordinates.
        float[] temp = pol(x,y);
        temp = cart(temp[0],temp[1] - positionData.getDirection());
        positionData.updatePosition(temp);
    }

    private float[] pol(float x, float y){//to polar coordinates
        return new float[] {(float) Math.sqrt(x*x + y*y), (float) Math.toDegrees(Math.atan2(y,x))};
    }

    private float[] cart(float l, float d){//to cartesian
        return new float[] {(float) (l * Math.cos(Math.toRadians(d))), (float) (l * Math.sin(Math.toRadians(d)))};
    }
}