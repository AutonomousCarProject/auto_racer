package org.avphs.detection;
//This is for the box
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class ObjectDetectionModule implements CarModule {
    ObjectData data = new ObjectData();
    @Override
    public void init(CarData carData) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[0];

    }

    @Override
    public void update(CarData carData) {
        carData.addData("objectDetection", data);
    }
}
