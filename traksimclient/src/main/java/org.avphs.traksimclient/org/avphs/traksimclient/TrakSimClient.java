package org.avphs.traksimclient;

import org.avphs.core.CarCore;
import org.avphs.sbcio.fakefirm.ArduinoIO;
import org.avphs.traksim.SimCamera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TrakSimClient extends JFrame implements Runnable, MouseListener {

    private SimCamera traksim;

    private BufferedImage displayImage;

    private JPanel panel;

    private final int WINDOW_WIDTH = 912, WINDOW_HEIGHT = 480;

    private ArduinoIO servos;

    private int simMode = 1;

    public TrakSimClient() {
        displayImage = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);

        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this, 0, 1000/30, TimeUnit.MILLISECONDS);

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

        panel.addMouseListener(this);

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
        int[] rgb = new int[WINDOW_WIDTH * WINDOW_HEIGHT ];
        for(int i = 0; i < WINDOW_HEIGHT; i++){
            for(int j = 0; j < WINDOW_WIDTH; j++){
                int r = (int)bayer[2*(2*i*WINDOW_WIDTH+j)] & 0xFF;
                int g = (int)bayer[2*(2*i*WINDOW_WIDTH+j)+1] & 0xFF;
                int b = (int)bayer[2*((2*i+1)*WINDOW_WIDTH+j)+1] & 0xFF;
                int pix = (r << 16) + (g << 8) + b;
                rgb[i*WINDOW_WIDTH+j] = pix;
            }
        }
        return rgb;
    }

    public byte[] readCameraImage() {
        byte[] temp = new byte[WINDOW_WIDTH * WINDOW_HEIGHT * 4];
        if (traksim.nextFrame(temp, simMode))
        {
            return temp;
        }
        return null;
    }


    @Override
    public void run() {
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        simMode++;
        if (simMode > 3)
            simMode = 0;
        System.out.println(simMode);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
