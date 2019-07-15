package org.avphs.image;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.window.WindowModule;

public class ImageModule implements CarModule {

    WindowModule window;
    public final int WINDOW_WIDTH = 912, WINDOW_HEIGHT = 480;

    byte[] bayerImage = new byte[4*WINDOW_HEIGHT*WINDOW_WIDTH];
    int[] rgbImage = new int[WINDOW_HEIGHT*WINDOW_HEIGHT];

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        window = (WindowModule) carData.getModuleData("window");
        var camera = (Camera) carData.getModuleData("camera");
        bayerImage = camera.getBayerImage();
        var rgb = ImageProcessing.process(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT);
        window.setWindowImage(rgb);
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

}


