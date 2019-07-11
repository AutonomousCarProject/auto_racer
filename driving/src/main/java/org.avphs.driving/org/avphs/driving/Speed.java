package org.avphs.driving;

import org.avphs.calibration.CalibrationModule;

public class Speed {
    /* Currently, Speed will work like this:
    * The constructor takes your current position, current segment, and next upcoming segment.
    * Each frame, getThrottle will return the throttle that the wheels will run at.
    * Each time the car moves to a new segment, newSegment will run, setting the next segment up.
    */


    //private int targetSpeed;
    //private boolean isStraight;
    //private int speedChange;    //negative = slow down, positive = speed up
    private int brakeDist;
    private final byte MAX_SPEED;       //maximum attainable speed
    private final byte MAX_HARD_BRAKE;  //max throttle for braking w/o skidding
    private final byte FLOOR;           //floor index
    private VectorPoint currentPos;
    private RoadData currentSegment;
    private RoadData nextSegment;

    public Speed(VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
        FLOOR = (byte)2;            //dummy value
        MAX_SPEED = CalibrationModule.getMaxSpeed(FLOOR, (byte)2);    //dummy values
        MAX_HARD_BRAKE = (byte)80;  //dummy value
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;
        brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, getTargetSpeedSegment(currentSegment), getTargetSpeedSegment(nextSegment));
    }

    public void setCurrentPos(VectorPoint newCurrentPos){
        currentPos = newCurrentPos;
    }

    public void newSegment(RoadData newNextSeg){
        currentSegment = nextSegment;
        nextSegment = newNextSeg;
        //speedChange = getTargetSpeedSegment(nextSegment) - getTargetSpeedSegment(currentSegment);
        brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, getTargetSpeedSegment(currentSegment), getTargetSpeedSegment(nextSegment));
    }

    public int getThrottle(){
        if (currentSegment instanceof Straight){
            if (true){
                return 180;
            } else {
                return MAX_HARD_BRAKE;
            }
        } else {
            return 90;
        }
    }


    private byte getTargetSpeedSegment(RoadData input){
        if (input instanceof Straight){
            return MAX_SPEED;
        } else {
            return 4;   //dummy value for now
        }
    }
}

