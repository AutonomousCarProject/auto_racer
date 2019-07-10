package org.avphs.driving;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import org.avphs.util.VectorPoint;


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

    //private RacingLine racingLine;


    public VectorPoint getPosition(){ //returns the (x,y) of the car in the map
        return new VectorPoint(0.0,0.0);
    }
    
    public int getDirection(){ //returns the direction of the car in degrees
        return 0;
    }
}
