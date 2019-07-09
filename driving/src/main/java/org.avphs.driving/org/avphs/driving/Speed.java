package org.avphs.driving;



public class Speed {
    float maxSpeed;
    float targetSpeed;
    private RoadData roadData;
    public Speed(RoadData input){
        roadData = input;
    }

    public float main(){
        return 1;
    }

    private float getTargetSpeedForStraight(){
        return 1;
    }

}
