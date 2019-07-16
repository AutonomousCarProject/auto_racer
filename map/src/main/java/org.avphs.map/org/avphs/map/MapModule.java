package org.avphs.map;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.position.PositionModule;


public class MapModule implements CarModule {

    private ImageModule imageModule;
    private PositionModule positionModule;

    private Map map = new Map();

    @Override
    public Class[] getDependencies() {
        return new Class[] {
            ImageModule.class, PositionModule.class
        };
    }
    @Override
    public void init(CarData carData) {
        imageModule = (ImageModule) carData.getModuleData("image");
        System.out.println("Image Module Found" + imageModule.getClass());
        positionModule = (PositionModule) carData.getModuleData("position");
        System.out.println("Position Module Found" + positionModule.getClass());
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {

    }

    public Map getMap(){return map;}


}
