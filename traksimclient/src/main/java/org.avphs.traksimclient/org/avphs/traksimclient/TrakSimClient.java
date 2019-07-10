package org.avphs.traksimclient;

import org.avphs.core.CarCore;
import org.avphs.sbcio.fakefirm.ArduinoIO;
import org.avphs.traksim.SimCamera;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;

public class TrakSimClient extends JFrame {

    private SimCamera traksim;

    private BufferedImage displayImage;

    private JPanel panel;

    private final int WINDOW_WIDTH = 640, WINDOW_HEIGHT = 480;

    private ArduinoIO servos;

    public TrakSimClient() {
        displayImage = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);

        traksim = new SimCamera();
        traksim.connect(4);
        servos = new ArduinoIO();

        panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                var img = debayer(readCameraImage());

                displayImage.setData(Raster.createRaster(displayImage.getSampleModel(),
                        new DataBufferInt(img, img.length), new Point())
                );
                g.drawImage(displayImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
            }
        };

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(true);
        setVisible(true);
        getContentPane().add(panel);
        pack();

        servos.open();

        var core = new CarCore();
        core.init();
    }

    public static void main(String[] args) {
        new TrakSimClient();
    }

    public int[] debayer(byte[] bayer) {
        int[] rgb = new int[640 * 480 * 3];
        ImageManipulation.convertToRGBRaster(bayer, rgb, 640, 480, (byte) 1);
        return rgb;
    }

    public byte[] readCameraImage() {
        byte[] temp = new byte[640 * 480 * 4];
        if (traksim.nextFrame(temp)) {
            return temp;
        }
        return null;
    }


}
