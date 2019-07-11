package org.avphs.driving;

public class VectorPoint {
    private float x;
    private float y;
    private float currentOrientation;
    private float currentSpeed;

    public VectorPoint(float x, float y, float currentOrientation, float currentSpeed){
        this.x = x;
        this.y = y;
        this.currentOrientation = currentOrientation;
        this.currentSpeed = currentSpeed;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getCurrentOrientation(){
        return currentOrientation;
    }

    public float getCurrentSpeed(){
        return currentSpeed;
    }
}
