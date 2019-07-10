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
    private final int MAX_SPEED;
    private final int MAX_HARD_BRAKE; //max throttle for braking w/o skidding
    private VectorPoint currentPos;
    private RoadData currentSegment;
    private RoadData nextSegment;

    public Speed(int MAX_SPEEDin, int MAX_HARD_BRAKEin, VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
        this.MAX_SPEED = MAX_SPEEDin;
        this.MAX_HARD_BRAKE = MAX_HARD_BRAKEin;
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;

        brakeDist = 2;
    }

    public void setCurrentPos(VectorPoint newCurrentPos){
        currentPos = newCurrentPos;
    }

    public void newSegment(RoadData newNextSeg){
        currentSegment = nextSegment;
        nextSegment = newNextSeg;
        speedChange = getTargetSpeedSegment(nextSegment) - getTargetSpeedSegment(currentSegment);
        brakeDist = 2;
    }

    public int getThrottle(){
        if (currentSegment instanceof Straight){
            if (true){
                return 180;
            } else {
                return MAX_HARD_BRAKE;
            }

//            return 180;     //currently returning max throttle
        } else {
            return 90;
        }
    }

    private int getTargetSpeedSegment(RoadData input){
        if (input instanceof Straight){
            return MAX_SPEED;
        } else {
            return 4;   //dummy value for now
        }
    }
}

