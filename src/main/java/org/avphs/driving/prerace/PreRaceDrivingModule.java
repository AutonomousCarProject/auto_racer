package org.avphs.driving.prerace;

import org.avphs.calibration.CalibrationModule;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.prerace.PreRaceModule;

import static org.avphs.coreinterface.CarCommand.accelerate;
import static org.avphs.coreinterface.CarCommand.steer;

public class PreRaceDrivingModule extends PreRaceModule {

    private int angle = 0;

    //Final variables
    private final int speed = 15;
    private final float minDistance = 10;
    private final float changeAmount = 2;

    //necessary in all cases
    private float[] distances;
    private float[] lastDistances;

    private Algs calcAngle = new Mid(minDistance, changeAmount);
    //private Algs calcAngle = new HugWall(minDistance, changeAmount);

    @Override
    public void init(CarData carData) {
        carData.addData("driving", angle);
    }

    @Override
    public CarCommand[] commands() {
        //Speed will be constant
        return new CarCommand[]{
                accelerate(true, speed),
                steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        lastDistances = distances;
        angle = calcAngle.getAngle(distances, lastDistances);
    }

}
