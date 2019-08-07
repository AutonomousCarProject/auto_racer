package org.avphs.image.tools;

import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.PreRaceImageCore;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PreRaceImageClient {
    private PreRaceImageClient() {
        BufferedImage image = null;
        try
        {
            image = ImageIO.read(new File("src/main/java/org/avphs/image/tools/test.png"));

        }
        catch (IOException e)
        {
            System.out.println(e);
        }

        if (image != null)
            new PreRaceImageCore(new Car(new SimCamera()), image);
        else
            new PreRaceImageCore(new Car(new SimCamera()));

    }

    public static void main(String[] args) {
        new PreRaceImageClient();
    }
}
