package org.avphs.driving;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;
    private short radius;

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
            return 90;
        } else {
            //radius = currentSegment.getRadius();

        }

        return 180;
    }

}
