package org.avphs.map;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import org.avphs.image.ImageModule;

public class MapModule implements CarModule {

    private ImageModule imageModule;

    private Map map = new Map();

    @Override
    public Class[] getDependencies() {
        return new Class[] {
            ImageModule.class
        };
    }
    //this is a test comment
    @Override
    public void init(CarModule... dependencies) {
        imageModule = (ImageModule) dependencies[0];
        System.out.println("Image Module Found" + imageModule.getClass());
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Map");
    }

    public Map getMap(){return map;}


}
