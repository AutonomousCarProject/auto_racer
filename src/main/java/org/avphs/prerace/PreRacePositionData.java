package org.avphs.prerace;

public class PreRacePositionData {
    private float[] position={0,0};
    private float direction=0;
    private float speed=0;

    public void update(float[] position, float direction, float speed){
        this.position=position;
        this.direction=direction;
        this.speed=speed;
    }
    public float[] getPosition(){
        return position;
    }

    public float getDirection(){
        return direction;
    }

    public float getSpeed(){
        return speed;
    }
}
