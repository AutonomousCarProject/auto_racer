package org.avphs.test;

import fly2cam.FlyCamera;

import javax.swing.*;

public class DrDemo extends JFrame {

    public static void main(String[] args) {
        new DrDemo();
    }

    private FlyCamera cam = null;

    public DrDemo() {
        FlyCamera flyCam;

        cam = new FlyCamera();
        flyCam = cam;

        boolean success = flyCam.Connect(2);


        setTitle("DriveDemo Example");
        setSize(640 + 18, 480 + 40);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setVisible(true);

        flyCam.NextFrame();
        flyCam.Finish();
    }
}
