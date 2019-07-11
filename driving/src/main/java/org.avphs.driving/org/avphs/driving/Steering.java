package org.avphs.driving;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;


    public Steering(VectorPoint currentPos, RoadData currentSegment) {
        this.currentSegment = currentSegment;
        this.currentPos = currentPos;
    }

    public void changeCurrentPos(VectorPoint newCurrentPos) {
        currentPos = newCurrentPos;
    }

    public void changeCurrentSegment(RoadData newCurrentSegment){
        currentSegment = newCurrentSegment;
    }

    public int getAngle(){
        if (currentSegment instanceof Straight){
            
        } else {

        }

        return 180;
    }

}
