package org.avphs.driving;


import org.avphs.util.VectorPoint;

public class Speed {
    private int targetSpeed;
    private boolean isStraight;
    private VectorPoint currentPos;
    private RefinedRacingLine roadData;
    public Speed(RefinedRacingLine input, VectorPoint currentPos){
        roadData = input;
        this.currentPos = currentPos;
    }

    public int getThrottle(){

        if (isStraight){
            //targetSpeed = getTargetSpeedForStraight((Straight)roadData);
        } else {
            //targetSpeed = getTargetSpeedForTurn((Turn)roadData);

        }
        return 1;
    }

    private int getTargetSpeedForStraight(Straight input){
        return 1;
    }

    private int getTargetSpeedForTurn(Turn input) {
        return 1;
    }

}

