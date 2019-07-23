package org.avphs.traksimclient;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.RacingCore;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class TrakSimClient {

    public TrakSimClient() {
        new RacingCore(new Car(new SimCamera()), true);
    }

    public static void main(String[] args) {
        new TrakSimClient();
    }
}
