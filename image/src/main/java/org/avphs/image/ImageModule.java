package org.avphs.image;

import org.avphs.core.CarModule;

public class ImageModule implements CarModule {
    @Override
    public void update() {
        System.out.println("Image");
    }
}
