package org.avphs.window;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class WindowModule extends JFrame implements CarModule {
    private Camera camera;
    private CarData carData;
    int[] pixels;
    private int windowWidth;
    private int windowHeight;
    private BufferedImage displayImage;
    private BufferedImage bufferImage;

    public WindowModule(CarData carData) {
        this.carData = carData;
        init();
        update(carData);
    }

    public WindowModule() {}

    private void init() {
        try {
            camera = (Camera) carData.getModuleData("camera");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        windowWidth = camera.getCamWidth();
        windowHeight = camera.getCamHeight();

        displayImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight + 25);
        setResizable(true);
        setVisible(true);
        setIgnoreRepaint(true);
    }

    @Override
    public void update(CarData carData) {
        if (camera != null) {
            pixels = convertToRGB(camera.getBayerImage(), camera.getCamHeight(), camera.getCamWidth());
            update(this.getGraphics());
        }
    }

    @Override
    public void paint(Graphics g) {
        if (pixels != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(pixels, 0, displayPixels, 0, pixels.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;

            Insets insets = getInsets();
            g.drawImage(displayImage, insets.left, insets.top, windowWidth - insets.left - insets.right,
                    windowHeight - insets.top - insets.bottom, null);
        }
    }

    private int[] convertToRGB(byte[] bayer, int winHeight, int winWidth) {
        int[] rgb = new int[winWidth * winHeight];
        for (int i = 0; i < winHeight; i++) {
            for (int j = 0; j < winWidth; j++) {
                int r = (int) bayer[2 * (2 * i * winWidth + j)] & 0xFF;
                int g = (int) bayer[2 * (2 * i * winWidth + j) + 1] & 0xFF;
                int b = (int) bayer[2 * ((2 * i + 1) * winWidth + j) + 1] & 0xFF;
                int pix = (r << 16) + (g << 8) + b;
                rgb[i * winWidth + j] = pix;
            }
        }
        return rgb;
    }

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule[] dependencies) {}

    @Override
    public CarCommand[] commands() {
        return null;
    }
}
