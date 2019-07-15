package org.avphs.driving;

import org.avphs.calibration.*;
//

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;
    private short radius;
    private float maxDistanceFromRacingLine;

    public Steering(VectorPoint currentPos, RoadData currentSegment) {
        this.currentSegment = currentSegment;
        this.currentPos = currentPos;
        maxDistanceFromRacingLine = 10;
    }

    public void changeCurrentPos(VectorPoint newCurrentPos) {
        currentPos = newCurrentPos;
    }

    public void changeCurrentSegment(RoadData newCurrentSegment){
        currentSegment = newCurrentSegment;
    }

    private boolean onRacingLine(){
        float distance;
        if (currentSegment instanceof Straight){
            Straight segment = (Straight)currentSegment;
            distance = Calculator.findStraightDistance(currentPos.getX(), currentPos.getY(), segment.getStartingCoords(), segment.getSlope());
        } else {
            Turn segment = (Turn)currentSegment;
            distance = Calculator.findTurnDistance(0,0, new float[2], (short)0);
        }
        return (distance < maxDistanceFromRacingLine) || (distance == maxDistanceFromRacingLine);
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
