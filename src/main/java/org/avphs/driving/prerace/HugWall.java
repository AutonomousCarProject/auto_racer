package org.avphs.driving.prerace;

public class HugWall extends Algs{

    public HugWall(float changeAmount, float minDistance){
        super(changeAmount, minDistance);
    }

    private final int closeDistance = 5;

    public int getAngle(float[] distances, float[] lastDistances){
        int angle = 0;
        if (distances[1] > minDistance) {
            if (distances[1] > lastDistances[1]) {
                angle += changeAmount;
            } else {
                angle = 0;
            }
        } else if (distances[1] < closeDistance) {
            if (distances[1] < lastDistances[1]) {
                angle -= changeAmount;
            } else {
                angle = 0;
            }
        } else {
            angle = 0;
        }

        if (angle < -33){
            angle = -33;
        } else if (angle > 44){
            angle = 44;
        }
        return angle;
    }

}
