package org.avphs.image;

import org.avphs.coreinterface.*;
import static org.avphs.coreinterface.CarCommand.*;

public class ImageModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return null;
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
    public void update(CarData carData) {
        System.out.println("Image");
    }
}


