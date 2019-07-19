package org.avphs.image;

import fly2cam.FlyCamera;
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
        Camera camera = (Camera) carData.getModuleData("camera");

        WINDOW_HEIGHT = camera.getCamHeight();
        WINDOW_WIDTH = camera.getCamWidth();
        bayerImage = new byte[4*WINDOW_WIDTH*WINDOW_HEIGHT];
        rgbImage = new int[WINDOW_HEIGHT*WINDOW_WIDTH];
        int wallData [][] = new int[2][WINDOW_WIDTH];
        ImageData data = new ImageData();

        bayerImage = camera.getBayerImage();
        rgbImage = ImageProcessing.process(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT);
        wallData = WallIdentification.scanImage(rgbImage,WINDOW_WIDTH,WINDOW_HEIGHT,WallIdentification.WallColorSeqs);

        data.wallTop = wallData[0];
        data.wallBottom = wallData[1];

        carData.addData("image", data);
        window.setWindowImage(rgbImage);
    }



}


