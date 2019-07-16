package org.avphs.image;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.window.WindowModule;

public class ImageModule implements CarModule {

    WindowModule window;
    public  int WINDOW_WIDTH = 912, WINDOW_HEIGHT = 480;

    byte[] bayerImage = new byte[4*WINDOW_HEIGHT*WINDOW_WIDTH];
    int[] rgbImage = new int[WINDOW_HEIGHT*WINDOW_HEIGHT];

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarData carData) {
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        window = (WindowModule) carData.getModuleData("window");
        var camera = (Camera) carData.getModuleData("camera");
        WINDOW_HEIGHT = camera.getCamHeight(); WINDOW_WIDTH = camera.getCamWidth();
        bayerImage = new byte[4*WINDOW_WIDTH*WINDOW_HEIGHT];
        rgbImage = new int[WINDOW_HEIGHT*WINDOW_WIDTH];
        bayerImage = camera.getBayerImage();
        rgbImage = ImageProcessing.process(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT);
        window.setWindowImage(rgbImage);
    }



}


