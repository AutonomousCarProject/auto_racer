package org.avphs.driving.prerace;

public abstract class Algs {

    protected float changeAmount;
    protected float minDistance;

    public Algs(float changeAmount, float minDistance){
        this.changeAmount = changeAmount;
        this.minDistance = minDistance;
    }

    public abstract int getAngle(float[] distances, float[] lastDistances);

}
