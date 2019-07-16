package org.avphs.driving;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;
    private short radius;
    private float maxDistanceFromRacingLine;

    public Steering() {
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
            distance = Calculator.findStraightDistance(currentPos.getX(), currentPos.getY(), segment.getB(), segment.getSlope());
        } else {
            Turn segment = (Turn)currentSegment;
            distance = Calculator.findTurnDistance(currentPos.getX(),currentPos.getY(), new float[]{segment.getCenterX(), segment.getCenterY()}, segment.getRadius());
        }
        return (distance < maxDistanceFromRacingLine) || (distance == maxDistanceFromRacingLine);
    }

    public int getAngle(){
        if (onRacingLine()) {
            if (currentSegment instanceof Straight) {
                return 90;
            } else {
                radius = currentSegment.getRadius();

                //FIXME: DONT DO THIS!
                //return CalibrationModule.getAngles(radius);
            }
        } else {
            return -1;
        }
        return -1;
    }
}
