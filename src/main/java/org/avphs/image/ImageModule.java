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
    int[] codeImage = new int[WINDOW_HEIGHT*WINDOW_WIDTH];

    public void init(CarData carData) {
        ImageData data = new ImageData();
        carData.addData("image", data);
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Image");

        window = (WindowModule) carData.getModuleData("window");
        Camera camera = (Camera) carData.getModuleData("camera");

        WINDOW_HEIGHT = camera.getCamHeight();
        WINDOW_WIDTH = camera.getCamWidth();
        bayerImage = new byte[4*WINDOW_WIDTH*WINDOW_HEIGHT];
        codeImage = new int[WINDOW_WIDTH*WINDOW_HEIGHT];
        rgbImage = new int[WINDOW_HEIGHT*WINDOW_WIDTH];
        int wallData [][] = new int[2][WINDOW_WIDTH];
        ImageData data = new ImageData();
        ImageData inProgressData = new ImageData();
        inProgressData.processingImage = true;
        carData.addData("image",inProgressData);

        bayerImage = camera.getBayerImage();
        codeImage = ImageProcessing.process(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT);
        wallData = WallIdentification.scanImage(codeImage,WINDOW_WIDTH,WINDOW_HEIGHT,WallIdentification.WallColorSeqs);
        ImageProcessing.CodeToRGB(codeImage, rgbImage);

        data.wallTop = wallData[1];
        data.wallBottom = wallData[0];

        for(int k = 0; k < wallData[0].length; k++) {
            if(wallData[0][k] > 0 && wallData[1][k] > 0) {
                for(int m = wallData[1][k]; m < wallData[0][k]; m ++) {
                    rgbImage[k + wallData[0].length * m] = WallIdentification.ColorArr[4];
                }
            }
        }

        carData.addData("image", data);
        window.setWindowImage(rgbImage);
    }



}


