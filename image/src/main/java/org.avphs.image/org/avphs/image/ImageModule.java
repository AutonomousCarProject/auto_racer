package org.avphs.image;

import org.avphs.core.CarModule;

import java.util.Collection;

public class ImageModule implements CarModule {

    @Override
    public Collection<Class> getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public void update() {
        System.out.println("Image3");
    }

    @Override
    public void run() {
        update();
    }
}
