package org.avphs.detection;

import org.avphs.core.CarModule;

public class DetectionModule implements CarModule {

    @Override
    public void update() {
        System.out.println("Detection");
    }
}
