package org.avphs.traksimclient;

import org.avphs.core.CarCore;
import org.avphs.trakSimCamera.TrakSimCamera;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TrakSimClient extends JFrame {

    private TrakSimCamera traksim;

    private BufferedImage displayImage;

    private JPanel panel;

    private final int WINDOW_WIDTH = 640, WINDOW_HEIGHT = 480;

    public TrakSimClient() {

        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                g.drawImage(displayImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
            }
        };
        displayImage = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(true);
        setVisible(true);
        add(panel);

        traksim = new TrakSimCamera();
        traksim.connect(4);
        var core = new CarCore();
        core.init();
    }

    public static void main(String[] args) {
        new TrakSimClient();
    }

    public byte[] readCameraImage() {
        byte[] temp = new byte[]{};
        if (traksim.nextFrame(temp)) {
            return temp;
        }
        return null;
    }


}
