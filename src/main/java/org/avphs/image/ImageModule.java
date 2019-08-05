package org.avphs.image;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.tools.ImageWindowModule;

/** Manages and executes image processing
 *
 * @author Joshua Bromley
 * @author Kenneth Browder
 * @author Kevin "Poo" Tran
 * @see ImageProcessing
 * @see ImageData
 * @see WallIdentification
 */
public class ImageModule implements CarModule {

    ImageWindowModule window;
    public  int WINDOW_WIDTH = 912, WINDOW_HEIGHT = 480;

    byte[] bayerImage = new byte[4*WINDOW_HEIGHT*WINDOW_WIDTH];
    int[] rgbImage = new int[WINDOW_HEIGHT*WINDOW_HEIGHT];
    int[] codeImage = new int[WINDOW_HEIGHT*WINDOW_WIDTH];

    public void init(CarData carData) {
        ImageData data = new ImageData();
        carData.addData("image", data);
    }
    

    @Override
    public void update(CarData carData) {
        window = (ImageWindowModule) carData.getModuleData("window");
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
        wallData = WallIdentification.magicloop(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT,65, 0);
        rgbImage = ImageProcessing.process(bayerImage,WINDOW_WIDTH,WINDOW_HEIGHT);
        ImageProcessing.CodeToRGB(rgbImage,rgbImage);

        data.wallTop = wallData[1];
        data.wallBottom = wallData[0];
        data.wallTypes = wallData[2];


        for(int k = 0; k < wallData[0].length; k++) {
            if(wallData[0][k] > 0 && wallData[1][k] >= 0) {
                for(int m = wallData[1][k]; m < wallData[0][k]; m ++) {
                    if(k < 640) rgbImage[k + wallData[0].length * m] = WallIdentification.ColorArr[4];
                }
            }
        }

        carData.addData("image", data);
        window.setWindowImage(rgbImage);
    }



}