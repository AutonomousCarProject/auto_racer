package org.avphs.prerace;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class PreRaceModule implements CarModule {
    private PreRacePositionData positionData;
    private float[] position = {0,0};
    private float direction=0;
    private float speed=0;

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        positionData.update(position,direction,speed);
        carData.addData("position",positionData);
    }
}
