package org.avphs.driving;


import org.avphs.util.VectorPoint;

public class Speed {
<<<<<<< HEAD
    private int targetSpeed;
    private boolean isStraight;
    private VectorPoint currentPos;
    private RefinedRacingLine roadData;
    public Speed(RefinedRacingLine input, VectorPoint currentPos){
=======
    //private int targetSpeed;
    //private boolean isStraight;
    private int speedChange;    //negative = slow down, positive = speed up
    private int max_speed;
    private VectorPoint currentPos;
    private RefinedRacingLine roadData;
    private RoadData currentSegment;
    private RoadData nextSegment;
    public Speed(RefinedRacingLine input, VectorPoint currentPos, RoadData currentSegment, RoadData nextSegment){
>>>>>>> 86de5f9798af01d463fc6aeb10b4ab65c9d3d0e4
        roadData = input;
        this.currentPos = currentPos;
        this.currentSegment = currentSegment;
        this.nextSegment = nextSegment;
    }

    public int getThrottle(){
        /*if (isStraight){
            //targetSpeed = getTargetSpeedForStraight((Straight)roadData);
            targetSpeed = max_speed;

<<<<<<< HEAD
        if (isStraight){
            //targetSpeed = getTargetSpeedForStraight((Straight)roadData);
        } else {
            //targetSpeed = getTargetSpeedForTurn((Turn)roadData);
=======

            return 180;     //currently returning max throttle
        } else {
            //targetSpeed = getTargetSpeedForTurn((Turn)roadData);
            return 1;
        }*/
        speedChange = getTargetSpeedSegment(nextSegment) - getTargetSpeedSegment(currentSegment);
        return 180;     //returns max throttle for now
    }
>>>>>>> 86de5f9798af01d463fc6aeb10b4ab65c9d3d0e4

    private int getTargetSpeedSegment(RoadData input){
        if (input instanceof Straight){
            return max_speed;
        } else {
            return 0;
        }
    }

    private int getTargetSpeedNextSegment(RoadData input) {
        return 1;
    }

<<<<<<< HEAD
    private int getTargetSpeedForTurn(Turn input) {
        return 1;
    }

=======
>>>>>>> 86de5f9798af01d463fc6aeb10b4ab65c9d3d0e4
}

