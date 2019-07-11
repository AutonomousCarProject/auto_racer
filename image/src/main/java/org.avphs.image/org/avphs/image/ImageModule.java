package org.avphs.image;

import org.avphs.camera.FlyCamera;
import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;

import static org.avphs.core.CarCommand.*;

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
        return new CarCommand[] {
            accelerate(true, 0),
            steer(false, 10),
            stop()
        };
    }

    @Override
    public void run() {

    }
}


