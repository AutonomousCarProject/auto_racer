package org.avphs.traksimclient;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CarCore;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class TrakSimClient {

    private final int WINDOW_WIDTH = 912, WINDOW_HEIGHT = 480;
    private SimCamera traksim;
    private BufferedImage displayImage;
    private JPanel panel;
    //private ArduinoIO servos;
    private int simMode = 1;
    private byte[] cameraImage;

    public TrakSimClient() {
        //temp();

        new CarCore(new Car(new SimCamera()));
    }

    public static void main(String[] args) {
        new TrakSimClient();
    }
}
