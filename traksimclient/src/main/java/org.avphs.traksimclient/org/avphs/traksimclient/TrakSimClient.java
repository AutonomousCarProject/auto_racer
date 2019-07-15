package org.avphs.traksimclient;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CarCore;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class TrakSimClient extends JFrame implements ClientInterface, Runnable, MouseListener {

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


    @Override
    public void accelerate(boolean absolute, int angle) {
        servos.setServoAngle(DriverCons.D_GasServo, angle + 90);
    }

    @Override
    public void steer(boolean absolute, int angle) {
        servos.setServoAngle(DriverCons.D_SteerServo, angle + 90);
    }

    @Override
    public void stop() {
        accelerate(true, 0);
        steer(true, 0);
        servos.close();
        traksim.Finish();
    }
}
