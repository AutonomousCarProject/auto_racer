package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.traksim.TrakSim;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0);
    private PositionData positionData;
    private TrakSim ts;
    private int angle = 0;
    private float cumulatedDistance = 0;

    float distanceTraveledRadians;
    float distanceTraveledDegrees;


    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{40.0f,56.0f}, 90, 0); //initial position for this map is (40,56)
        ts = new TrakSim();
    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] { //still using CarCommand because this is an older version of master
                CarCommand.accelerate(true, 15),
                CarCommand.steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        if(cumulatedDistance > 33 && cumulatedDistance < 105){ //hard code to make the car run one lap around the track
            angle = 15;
        }
        else if (cumulatedDistance > 105 && cumulatedDistance < 136){
            angle = 0;
        }
        else if(cumulatedDistance > 136 && cumulatedDistance < 150){
            angle = -15;
        }
        else if(cumulatedDistance > 150 && cumulatedDistance < 153){
            angle = 0;
        }
        else if(cumulatedDistance > 153 && cumulatedDistance < 210){
            angle = -15;
        }
        else if(cumulatedDistance > 210){
            angle = 0;
        }

        cumulatedDistance += (float)ts.GetDistance(false); //used only to find when to turn for the hard code
        ComputePosition((float)ts.GetDistance(true), angle, positionData.getDirection());
        carData.addData("position", positionData);

    }

    private void ComputePosition(float distanceTraveled, float turnAngle, float currentDirection){
        float wheelBase = 10f; //not sure if actually 10

        float newPosX = 0;
        float newPosY = 0;
        float newPos[] = {newPosX, newPosY};
        float turnRadius;

        if(turnAngle != 0) {
            turnRadius = ComputeTurnRadius(wheelBase, turnAngle);
        }
        else{
            turnRadius = 0;
        }

        if(turnAngle > 0) {
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "right");
        }
        else if(turnAngle < 0){
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "left");
        }
        else{//turnAngle == 0
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "none");
        }

        if (turnRadius == 0) {
            //just drive straight forward
            convertPosition(0, distanceTraveled);
        } else {
            //if turning
            if (distanceTraveledDegrees < 90 || distanceTraveledDegrees > 270) {
                convertPosition((float) (turnRadius - turnRadius * Math.cos(Math.toRadians(distanceTraveledDegrees))), (float) (turnRadius * Math.sin(Math.toRadians(distanceTraveledDegrees))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

            } else {//if(deltaPositionAngle > 90), turning left
                convertPosition((float) (turnRadius + turnRadius * Math.cos(Math.toRadians(distanceTraveledDegrees))), (float) (turnRadius * Math.sin(Math.toRadians(distanceTraveledDegrees))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
            }
        }

        //System.out.println("direction: " + positionData.getDirection());
        //System.out.println("turn radius" + turnRadius);

        System.out.println("our position = ("+(positionData.getPosition()[0])+","+(positionData.getPosition()[1])+")");
        System.out.println("traksim position = (" + ts.GetPosn(true) + "," + ts.GetPosn(false) + ")");
        System.out.println("");
    }

    private float ComputeTurnRadius(float wheelBase, float turningAngle){ //FIXME THIS IS THE TURN RADIUS OF THE FRONT INSIDE WHEEL ONLY; FIND A WAY TO FIND THE TURN RADIUS OF THE FRONT OUTSIDE WHEEL AND THEN FIND THE AVERAGE OF THEM TO GET THE TURN RADIUS OF THE LOCATION OF THE CAMERA
        float turnRadius = (float) Math.abs(wheelBase / Math.sin(turningAngle));
        return turnRadius;
    }

    private void ComputeDirection(float currentDirection, float turnRadius, float distanceTraveledParkMeters, String turnType){ //returns new direction
        distanceTraveledRadians = distanceTraveledParkMeters / turnRadius;
        distanceTraveledDegrees = (float) (distanceTraveledRadians * 180 / Math.PI);
        float newDirection;

        if(turnType == "right"){
            newDirection = currentDirection + distanceTraveledDegrees;
            if(newDirection >= 360){
                newDirection -= 360;
            }
        }
        else if(turnType == "left"){
            newDirection = currentDirection - distanceTraveledDegrees;
            if(newDirection < 0){
                newDirection += 360;
            }
        }
        else{
            newDirection = currentDirection + 0;
        }

        positionData.updateDirection(newDirection);
    }

    private void convertPosition(float x, float y) {
        //FIXME x and y are currently in cm, not in the virtual world coordinates.
        if(!(x == 0 && y == 0)){
            float[] temp = pol(x, y);
            temp = cart(temp[0], temp[1] - positionData.getDirection());
            positionData.updatePosition(temp);
        }
    }

    private float[] pol(float x, float y){//to polar coordinates
        return new float[] {(float) Math.sqrt(x*x + y*y), (float) Math.toDegrees(Math.atan2(y,x))};
    }

    private float[] cart(float l, float d){//to cartesian
        return new float[] {(float) (l * Math.cos(Math.toRadians(d))), (float) (l * Math.sin(Math.toRadians(d)))};
    }
}