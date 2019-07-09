package org.avphs.driving;



public class Speed {
    float coefFric;     //get from calibration
    float forceGrav;    //get from calibration
    float maxSpeed;
    float targetSpeed;
    private RoadData roadData;
    public Speed(RoadData input){
        roadData = input;
    }

    public float main(){
        float forceFric = -1*forceGrav*coefFric;


        return 1;
    }

    private float getTargetSpeedForStraight(){
        return 1;
    }

}
