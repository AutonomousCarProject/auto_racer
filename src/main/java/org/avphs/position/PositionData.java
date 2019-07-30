package org.avphs.position;

public class PositionData{
    private float[] position; //(x, y)
    private float direction; //in degrees 0<= x < 360, may need adjusting to Tom's direction system
    private float speed; //meters per second

    public PositionData(float[] initPosition, float initDirection, float initSpeed){
        this.position = initPosition;
        this.direction = initDirection;
        this.speed = initSpeed;
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


    public void updateDirection(float newDirection){
        this.direction = newDirection;
    }

    public void updateSpeed(float newSpeed){
        this.speed = newSpeed;
    }

    public void updatePosition(float[] newPosition){ //(x, y)
        this.position[0] += newPosition[0];
        this.position[1] -= newPosition[1];//- because 0,0 is top left
    }

    public void updateAll(float[] newPosition, float newDirection, float newSpeed){
        this.position = newPosition;
        this.direction = newDirection;
        this.speed = newSpeed;
    }


}
