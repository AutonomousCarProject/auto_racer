package org.avphs.driving;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import org.avphs.util.VectorPoint;
import org.avphs.racingline.*;
import java.util.ArrayList;


import java.util.Collection;

public class DrivingModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Driving");
    }

    private RacingLine racingline;
    private RefinedRacingLine refinedRacingLine;
    private VectorPoint currerntPos;

    public DrivingModule(RacingLine racingLine, VectorPoint currentPos){
        this.racingline = racingLine;
        this.currerntPos = currentPos;
    }

    public DrivingModule(){

    }

    public RefinedRacingLine analyzeRacingLine(RacingLine input){
        return new RefinedRacingLine(new ArrayList<RoadData>(), new VectorPoint(0.0,0.0));
    }

    public VectorPoint getPosition(){ //returns the (x,y) of the car in the map
        return new VectorPoint(0.0,0.0);
    }
    
    public int getDirection(){ //returns the direction of the car from 0 to 180
        return 0;
    }

    public int getThrottle() { //returns the throttle of the car from 0 to 180
        return 0;
    }

}
