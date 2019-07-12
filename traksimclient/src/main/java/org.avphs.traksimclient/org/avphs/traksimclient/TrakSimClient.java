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

import static java.lang.Math.abs;

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
        PosterColor [] posterized = new PosterColor[WINDOW_WIDTH*WINDOW_HEIGHT];
        int[] rgbPosterized = new int[WINDOW_HEIGHT*WINDOW_WIDTH];
        for(int i = 0; i < WINDOW_HEIGHT; i++){
            for(int j = 0; j < WINDOW_WIDTH; j++){
                int r = (int)bayer[2*(2*i*WINDOW_WIDTH+j)] & 0xFF;
                int g = (int)bayer[2*(2*i*WINDOW_WIDTH+j)+1] & 0xFF;
                int b = (int)bayer[2*((2*i+1)*WINDOW_WIDTH+j)+1] & 0xFF;
                int pix = (r << 16) + (g << 8) + b;
                rgb[i*WINDOW_WIDTH+j] = pix;
            }
        }
        posterizeImage(rgb,posterized,50);
        PosterToRGB(posterized,rgbPosterized);
        return rgbPosterized;
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

    enum PosterColor {
        RED(0xFF0000, (short)0),
        GREEN(0x00FF00, (short)1),
        BLUE(0x0000FF, (short)2),
        CYAN(0x00FFFF, (short)3),
        MAGENTA(0xFF00FF, (short)4),
        YELLOW(0xFFFF00, (short)5),
        BLACK(0, (short)6),
        GREY1(0x333333, (short)7),
        GREY2(0x666666, (short)8),
        GREY3(0x999999, (short)9),
        GREY4(0xCCCCCC, (short)10),
        WHITE(0xFFFFFF, (short)11);
        final int rgb;
        final short code;
        private PosterColor(int rgb, short code) {
            this.rgb = rgb;
            this.code = code;
        }
    }

    static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    static int combineRGB(int red, int green, int blue) {
        return blue + (green << 8) + (red << 16);
    }


    static PosterColor posterizePixel(int rgb, int dt) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;
        int max = red > blue ? red > green ? red : green : blue > green ? blue : green;
        int min = red < blue ? red < green ? red : green : blue < green ? blue : green;
        int delta = max - min;
        int h = 0;
        if(delta == 0){
            h = 0;
        }else if(max == red){
            h = ((green-blue)/delta) % 6;
        }else if(max == green){
            h = (blue - red)/delta + 2;
        }else{
            h = (red - green)/delta + 4;
        }
        h *= 60;
        int l = (max + min) >> 1;
        if(delta > dt){
            if(h > 330 || h < 30){
                return PosterColor.RED;
            }else if(h > 30 && h < 90){
                return PosterColor.YELLOW;
            }else if( h > 90 && h < 150){
                return PosterColor.GREEN;
            }else if(h > 150 && h < 210){
                return PosterColor.CYAN;
            }else if(h > 210 && h < 270){
                return PosterColor.BLUE;
            }else if(h > 270 && h < 330){
                return PosterColor.MAGENTA;
            }
        }else{
            if(l < 43){
                return PosterColor.BLACK;
            }else if(l > 43 && l < 86){
                return  PosterColor.GREY1;
            }else if(l > 86 && l < 129){
                return PosterColor.GREY2;
            }else if(l > 129 && l < 152){
                return PosterColor.GREY3;
            }else if(l > 152 && l < 195){
                return PosterColor.GREY4;
            }else{
                return PosterColor.WHITE;
            }
        }
        return PosterColor.BLACK;
    }

    static void posterizeImage(int[] rgbArray, PosterColor[] outArray, int diffThreshold) {
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            outArray[i] = posterizePixel(rgbArray[i], diffThreshold);
        }

    }

    static void PosterToRGB(PosterColor[] inArray, int[] outArray) {
        for(int i = inArray.length - 1; i >= 0; i --) {
            outArray[i] = inArray[i].rgb;
        }
    }
}
