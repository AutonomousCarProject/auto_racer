package org.avphs.driving;


import org.avphs.util.VectorPoint;

public class Speed {
    //private int targetSpeed;
    //private boolean isStraight;
    private int speedChange;    //negative = slow down, positive = speed up
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

