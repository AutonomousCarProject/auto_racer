package org.avphs.image;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.window.WindowModule;

public class ImageModule implements CarModule {

    WindowModule window;

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
        var rgb = convertToRGB(camera.getBayerImage(), camera.getCamHeight(), camera.getCamWidth());
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


