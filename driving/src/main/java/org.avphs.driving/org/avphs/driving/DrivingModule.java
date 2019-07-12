package org.avphs.driving;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.racingline.RacingLine;
import org.avphs.racingline.RacingLinePoint;

import java.util.ArrayList;

import static org.avphs.coreinterface.CarCommand.*;


public class DrivingModule implements CarModule {

    private RacingLine[] racingline;
    private ArrayList<RoadData> roadData;
    private RoadData currentSegment;
    private RoadData nextSegment;
    private int angle = 90;
    private int throttle = 90;
    private int MAX_SPEED;
    private int MAX_HARD_BRAKE;
    private VectorPoint currentPos;
    Speed speed = new Speed(currentPos, currentSegment, nextSegment);
    Steering steer = new Steering(currentPos, currentSegment);

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] {
                accelerate(true, 0),
                steer(false, 10),
                stop()
        };
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Driving");
    }
  
    public void run() {
        System.out.println("Driving");
    }
    
    public DrivingModule(RacingLine[] racingLine){
        this.racingline = racingLine;
        roadData = new ArrayList<RoadData>();
        analyzeRacingLine(racingLine);
    }

    public DrivingModule(){
    }

    public void analyzeRacingLine(RacingLine[] input){
        ArrayList<RacingLinePoint> racingLinePoints = new ArrayList<RacingLinePoint>();
        //racingLine.getRacingLinePoints();
    }

    public void setCurrentPos(VectorPoint input){
        currentPos = input;
    }

    public VectorPoint getPosition(){ //returns the (x,y) of the car in the map
        return null;
    }

    public int getDirection(){ //returns the direction of the car from 0 to 180
        return angle = steer.getAngle();
    }

    public int getThrottle() { //returns the throttle of the car from 0 to 180
        //return throttle = speed.getThrottle();
        return 90;
    }
}
