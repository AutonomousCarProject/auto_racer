package org.avphs.driving;

public class Steering {

    private VectorPoint currentPos;
    private RoadData currentSegment;
    private short radius;
    private final float MAX_DIST_FROM_RL;

    public Steering() {
        MAX_DIST_FROM_RL = 10;
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
            distance = Calculator.findStraightDistance(currentPos.getX(), currentPos.getY(), segment.getB(),
                    segment.getSlope());
        } else {
            Turn segment = (Turn)currentSegment;
            distance = Calculator.findTurnDistance(currentPos.getX(),currentPos.getY(), new float[]{segment.getCenterX(),
                    segment.getCenterY()}, segment.getRadius());
        }
        return (distance < MAX_DIST_FROM_RL) || (distance == MAX_DIST_FROM_RL);
    }

    public int getAngle(){
        if (onRacingLine()) {
            if (currentSegment instanceof Straight) {
                return 90;
            } else {
                radius = currentSegment.getRadius();

                //FIXME: Wait for Calibration to implement
                //return CalibrationModule.getAngles(radius);
            }
        } else {
            return -1;
        }
        return -1;
    }
}
