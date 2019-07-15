package org.avphs.driving;

import org.avphs.calibration.CalibrationModule;
import org.avphs.racingline.RacingLinePoint;

public class Speed {
    /* Currently, Speed will work like this:
    * The constructor takes your current position, current segment, and next upcoming segment.
    * Each frame, getThrottle will return the throttle that the wheels will run at.
    * Each time the car moves to a new segment, newSegment will run, setting the next segment up.
    */

    private int brakeDist;
    private final byte MAX_HARD_BRAKE;  //max throttle for braking w/o skidding
    private final byte FLOOR;           //floor index
    private VectorPoint currentPos;
    private RacingLinePoint brakePoint;
    private RoadData currentSegment;
    private RoadData nextSegment;

    public Speed(VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
        FLOOR = (byte)2;            //dummy value
        MAX_HARD_BRAKE = (byte)80;  //dummy value
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;

        //  Wait for calibration to fix
        //brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, CalibrationModule.getMaxSpeed(FLOOR,
        //        currentSegment.radius), CalibrationModule.getMaxSpeed(FLOOR, nextSegment.radius));

    }

    public void setCurrentPos(VectorPoint newCurrentPos){
        currentPos = newCurrentPos;
    }

    public void newSegment(RoadData newNextSeg){
        currentSegment = nextSegment;
        nextSegment = newNextSeg;
        brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, CalibrationModule.getMaxSpeed(FLOOR,
                currentSegment.radius), CalibrationModule.getMaxSpeed(FLOOR, nextSegment.radius));
    }

    public int getThrottle(){
        if (currentSegment instanceof Straight){
            if (Calculator.findClosestPoint(currentPos.getX(), currentPos.getY(), ((Straight)currentSegment).getSlope(),
                    ((Straight)currentSegment).getB()) == new float[]{(float)0.1, (float)0.1} /*dummy values*/){
                return 180;
            } else {
                return MAX_HARD_BRAKE;
            }
        } else {
            return 90;
        }
    }
}