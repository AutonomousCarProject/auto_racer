package org.avphs.image;



import org.avphs.coreinterface.*;
import static org.avphs.coreinterface.CarCommand.*;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class ImageModule implements CarModule {

    public final int WINDOW_WIDTH = 640, WINDOW_HEIGHT = 480;

    byte[] bayerImage = new byte[4*WINDOW_HEIGHT*WINDOW_WIDTH];
    int[] rgbImage = new int[WINDOW_HEIGHT*WINDOW_HEIGHT];

    @Override
    public Class[] getDependencies() {
        return new Class[] {
                //FlyCamera.class
        };
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] {
          
        };
        return null;
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Image");
        bayerImage = (byte[]) carData.getModuleData("client");
        rgbImage = ImageProcessing.debayer(bayerImage, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
}


