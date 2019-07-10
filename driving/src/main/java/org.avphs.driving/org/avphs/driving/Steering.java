package org.avphs.driving;

import org.avphs.util.VectorPoint;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;
    private RoadData nextSegment;


    public Steering(VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;
    }


    public int getAngle(){
        return 180;
    }

}
