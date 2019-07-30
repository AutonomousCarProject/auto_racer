package org.avphs.driving;

public class VectorPoint {
    private float[] coords = new float[2];
    private float currentOrientation;
    private float currentSpeed;

    public VectorPoint(float[] coords, float currentOrientation, float currentSpeed){
        this.coords = coords;
        this.currentOrientation = currentOrientation;
        this.currentSpeed = currentSpeed;
    }

    public float getX(){
        return coords[0];
    }

    public float getY(){
        return coords[1];
    }

    public float getCurrentOrientation(){
        return currentOrientation;
    }

    public float getCurrentSpeed(){
        return currentSpeed;
    }
}
