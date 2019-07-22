package org.avphs.driving;

import org.avphs.calibration.CalibrationModule;
import org.avphs.racingline.RacingLinePoint;

public class Speed {
    /* Currently, Speed will work like this:
    * The constructor takes your current position, current segment, and next upcoming segment.
    * Each frame, getThrottle will return the throttle that the wheels will run at.
    * Each time the car moves to a new segment, newSegment will run, setting the next segment up.
    */

    private final byte MAX_HARD_BRAKE;  //max throttle for braking w/o skidding
    private final byte FLOOR;           //floor index
    private int brakeDist;
    private VectorPoint currentPos;
    private float[] currentPosOnLine;   //where we "should" be on the racing line. Updates with every getThrottle call.
    private RoadData currentSegment;
    private RoadData nextSegment;
    private short throttleForSeg;

    public Speed(){
        FLOOR = (byte)0;            //dummy value
        MAX_HARD_BRAKE = (byte)80;  //dummy value
    }

    public void setCurrentPos(VectorPoint newCurrentPos){
        currentPos = newCurrentPos;
    }

    public void newSegment(RoadData newNextSeg){ /*Sets up all variables for next segment*/
        currentSegment = nextSegment;
        nextSegment = newNextSeg;
        brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, CalibrationModule.getMaxSpeed(FLOOR,
                currentSegment.radius), CalibrationModule.getMaxSpeed(FLOOR, nextSegment.radius));
        if (currentSegment instanceof Straight){
            throttleForSeg = (short)180;
        } else {
            //throttleForSeg = CalibrationModule.getThrottle(FLOOR, currentSegment.radius,
                   // CalibrationModule.getMaxSpeed(FLOOR, currentSegment.radius));
        }
    }

    public void initialize(RoadData startSegment, RoadData nextSegment){
        currentSegment = startSegment;
        this.nextSegment = nextSegment;
        if (currentSegment instanceof Straight){
            throttleForSeg = (short)180;
        } else {
            //throttleForSeg = CalibrationModule.getThrottle(FLOOR, currentSegment.radius,
                  //  CalibrationModule.getMaxSpeed(FLOOR, currentSegment.radius));
        }
    }

    public int getThrottle(){
        if (currentSegment instanceof Straight){
            currentPosOnLine = Calculator.findClosestPoint(currentPos.getX(), currentPos.getY(), //Update
                    ((Straight)currentSegment).getSlope(), ((Straight)currentSegment).getB());     //currentPosOnLine
            if ((int)Math.sqrt(Math.pow(currentSegment.endX - currentPosOnLine[0], 2.0) //If we're not to the brake
                    + Math.pow(currentSegment.endY - currentPosOnLine[1], 2.0)) > brakeDist){ //point yet,
                return 180;     //full throttle
            } else {
                return MAX_HARD_BRAKE;
            }
        } else {
            return throttleForSeg;
        }
    }
}