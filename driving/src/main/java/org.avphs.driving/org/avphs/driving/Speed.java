package org.avphs.driving;


import org.avphs.util.VectorPoint;

public class Speed {
    float coefFric;     //get from calibration
    float forceGrav;    //get from calibration
    int targetSpeed;
    boolean isStraight;
    private RoadData roadData;
    public Speed(RoadData input){
        roadData = input;
    }

    public int main(){

        if (isStraight){
            targetSpeed = getTargetSpeedForStraight((Straight)roadData);
            float forceFric = -1*forceGrav*coefFric;
        } else {
            targetSpeed = getTargetSpeedForTurn((Turn)roadData);

        }
        return 1;
    }

    private int getTargetSpeedForStraight(Straight input){
        return 1;
    }

    private int getTargetSpeedForTurn(Turn input) { return 1; }

}

