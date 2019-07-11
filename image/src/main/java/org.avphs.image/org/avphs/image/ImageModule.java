package org.avphs.image;

import org.avphs.camera.FlyCamera;
import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

public class ImageModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return new Class[] {
                FlyCamera.class
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
        System.out.println("Image");
    }
}


