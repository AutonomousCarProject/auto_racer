package org.avphs.map;

import org.avphs.core.CarModule;
import org.avphs.image.ImageModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MapModule implements CarModule {

    private ImageModule imageModule;

    @Override
    public Collection<Class> getDependencies() {
        return Arrays.asList(new Class[] {
                ImageModule.class
        });
    }

    @Override
    public void init(CarModule... dependencies) {
        imageModule = (ImageModule) dependencies[0];
        System.out.println("Image Module Found" + imageModule.getClass());
    }

    @Override
    public void update() {
        System.out.println("Map");
    }

    @Override
    public void run() {
        update();
    }
}
