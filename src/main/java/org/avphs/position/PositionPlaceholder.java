/*
        package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.traksim.TrakSim;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
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
        if(cumulatedDistance > 33 && cumulatedDistance < 105){
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

        cumulatedDistance += (float)ts.GetDistance(false);
        ComputePosition((float)ts.GetDistance(true), angle, positionData.getDirection());
        carData.addData("position", positionData);

    }

    private void ComputePosition(float distanceTraveled, float turnAngle, float currentDirection){
        float wheelBase = 10f;

        float newPosX = 0;
        float newPosY = 0;
        float newPos[] = {newPosX, newPosY};
        float turnRadius;
        float newDirection;

        if(turnAngle != 0) {
            turnRadius = ComputeTurnRadius(wheelBase, turnAngle);
        }
        else{
            turnRadius = 0;
        }

        if(turnAngle > 0) {
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "right");
            newDirection = positionData.getDirection();
        }
        else if(turnAngle < 0){
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "left");
            newDirection = positionData.getDirection();
        }
        else{//turnAngle == 0
            ComputeDirection(currentDirection, turnRadius, distanceTraveled, "none");
            newDirection = positionData.getDirection();
        }

        System.out.println("direction: " + newDirection);
        //System.out.println("turn radius" + turnRadius);

        positionData.updatePosition(newPos);

        //System.out.println("Position = ("+(positionData.getPosition()[0])+","+(positionData.getPosition()[1])+")");
    }

    private float ComputeTurnRadius(float wheelBase, float turningAngle){ //FIXME THIS IS THE TURN RADIUS OF THE FRONT INSIDE WHEEL ONLY; FIND A WAY TO FIND THE TURN RADIUS OF THE FRONT OUTSIDE WHEEL AND THEN FIND THE AVERAGE OF THEM TO GET THE TURN RADIUS OF THE LOCATION OF THE CAMERA
        float turnRadius = (float) Math.abs(wheelBase / Math.sin(turningAngle));
        return turnRadius;
    }

    private void ComputeDirection(float currentDirection, float turnRadius, float distanceTraveledParkMeters, String turnType){
        float distanceTraveledRadians = distanceTraveledParkMeters / turnRadius;
        float distanceTraveledDegrees = (float) (distanceTraveledRadians * 180 / Math.PI);
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
}*/