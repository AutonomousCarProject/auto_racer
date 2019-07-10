package org.avphs.prerace;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;

public class PreRaceModule implements CarModule {

    @Override
    public Class[] getDependencies() {
    	return new Class[] {
            	ImageModule.class
            };
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Pre Race");
    }
}
