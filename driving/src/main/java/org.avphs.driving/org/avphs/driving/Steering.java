package org.avphs.driving;

import org.avphs.util.VectorPoint;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;


    public Steering(VectorPoint currentPos, RoadData currentSegment){
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
    }


    public int getAngle(){
        if (currentSegment instanceof Straight){

        } else {

        }

        return 180;
    }

}
