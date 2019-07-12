package org.avphs.driving;

import org.avphs.calibration.*;
//

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

    private boolean onRacingLine(){
        if (currentSegment instanceof Straight){
            //Calculator.solveSystem();
        } else {

        }
        return false;
    }

    public int getAngle(){
        if (onRacingLine()) {
            if (currentSegment instanceof Straight) {
                return 90;
            } else {
                radius = currentSegment.getRadius();
                return CalibrationModule.getAngles(radius);
            }
        } else {
            return -1;
        }
    }

}
