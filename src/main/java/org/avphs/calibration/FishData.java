package org.avphs.calibration;

public class FishData {
    private final float degree;
    private final float distance; //dist to nearest pixel in front of car in cm

    public FishData(float degree, float distance) {
        this.degree = degree;
        this.distance = distance;
    }

    public float getDegree() {
        return degree;
    }

    public float getDistance() {
        return distance;
    }
}
