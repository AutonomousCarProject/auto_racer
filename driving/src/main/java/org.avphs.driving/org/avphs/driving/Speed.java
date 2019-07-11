package org.avphs.driving;


public class Speed {
    /* Currently, Speed will work like this:
    * The constructor takes your current position, current segment, and next upcoming segment.
    * Each frame, getThrottle will return the throttle that the wheels will run at.
    * Each time the car moves to a new segment, newSegment will run, setting the next segment up.
    */

    //private int targetSpeed;
    //private boolean isStraight;
    private int speedChange;    //negative = slow down, positive = speed up
    private int brakeDist;
    private int max_speed;
    private VectorPoint currentPos;
    private RoadData currentSegment;
    private RoadData nextSegment;

    public Speed(VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;

    }

    public void newSegment(RoadData newNextSeg){
        currentSegment = nextSegment;
        nextSegment = newNextSeg;
        speedChange = getTargetSpeedSegment(nextSegment) - getTargetSpeedSegment(currentSegment);
        brakeDist = 2;
    }

    public int getThrottle(){
        /*if (isStraight){
            //targetSpeed = getTargetSpeedForStraight((Straight)roadData);
            targetSpeed = max_speed;
            return 180;     //currently returning max throttle
        } else {
            //targetSpeed = getTargetSpeedForTurn((Turn)roadData);
            return 1;
        }*/
        return 180;     //returns max throttle for now
    }

    private int getTargetSpeedSegment(RoadData input){
        if (input instanceof Straight){
            return max_speed;
        } else {
            return 4;   //dummy value for now
        }
    }

    private int getTargetSpeedNextSegment(RoadData input) {
        return 1;
    }
}

