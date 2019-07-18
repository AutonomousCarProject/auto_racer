package org.avphs.driving.drivingprerace;

import org.avphs.coreinterface.*;

import org.avphs.image.ImageData;

import static org.avphs.coreinterface.CarCommand.*;

public class PreRaceDrivingModule implements PreRaceModule{

    private int angle = 90;
    private final int speed = 91;

    //necessary in all cases
    private float[] distances = new float[]{1000,1000};
    private final float minDistance = 10;
    private final int changeAmount = 2;
    private float[] lastDistances;

    @Override
    public void init(CarData carData) {

    }

    @Override
    public CarCommand[] commands() {
        //Speed will be constant
        return new CarCommand[]{
                accelerate(true, speed),
                steer(true,angle)
        };
    }

    @Override
    public void update(CarData carData) {
        lastDistances = distances;
        ImageData data = (ImageData)carData.getModuleData("image");
        float leftDistance = (float)Math.abs(data.wallHeights[0] - data.wallPosition[0]);

        float rightDistance = (float)Math.abs(data.wallHeights[639] - data.wallPosition[639]);

        distances = null;
        getAngle();
    }



    private void getAngle(){
        if (distances[0] < minDistance) {
            if (distances[0] < lastDistances[0]) {
                angle += changeAmount;
            } else {
                angle = 90;
            }
        } else if (distances[1] < minDistance) {
            if (distances[1] < lastDistances[1]) {
                angle -= changeAmount;
            } else {
                angle = 90;
            }
        } else {
            angle = 90;
        }



        if (angle < 0){
            angle = 0;
        } else if (angle > 180){
            angle = 180;
        }
    }

}
