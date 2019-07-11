package org.avphs.prerace;

import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;
import org.avphs.image.ImageModule;

public class PreRaceModule implements CarModule {

    private ImageModule imageModule;

    @Override
    public Class[] getDependencies() {
    	return new Class[] {
            	ImageModule.class
            };
    }

    @Override
    public void init(CarModule... dependencies) {
        imageModule = (ImageModule) dependencies[0];
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
