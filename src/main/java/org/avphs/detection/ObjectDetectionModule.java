package org.avphs.detection;
//This is for the box
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageData;
import org.avphs.map.Map;
import org.avphs.position.PositionData;

public class ObjectDetectionModule implements CarModule {
    ObjectData data = new ObjectData();
    private ImageData imageData;
    private Map map;
    private PositionData positionData;

    @Override
    public void init(CarData carData) {
        carData.addData("objectDetection", data);
    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[0];
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Detection");
        imageData = (ImageData)carData.getModuleData("image");
        map = (Map)carData.getModuleData("map");
        positionData = (PositionData)carData.getModuleData("position");

        carData.addData("objectDetection", data);
        System.out.println("Detection END");
    }
}
