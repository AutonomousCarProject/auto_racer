package org.avphs.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestMain {
    public static void main(String[] args) {
        try {
//            BufferedImage img = ImageIO.read(
//                    new File("C:\\Users\\TEST\\IdeaProjects\\auto_racer-java8\\src\\main\\java\\org\\avphs\\image\\carvroom1.PNG")
//            );
            BufferedImage img = ImageIO.read(
                    new File("C:\\Users\\TEST\\IdeaProjects\\auto_racer-java8\\src\\main\\java\\org\\avphs\\image\\wew.PNG")
            );

            FsmRunner fsmRunner = new FsmRunner(
                    "C:\\Users\\TEST\\IdeaProjects\\auto_racer-java8\\src\\main\\java\\org\\avphs\\image\\foo.table",
                    img.getWidth(),
                    img.getHeight()
            );

            for (int i = 0; i < 45; i++) {
                System.out.println("State " + i);
                String[] transitions = fsmRunner.debugTableAtState(i << 4);
                for (int j = 0; j < transitions.length; j++) {
                    System.out.println(ImageProcessing.PosterColor.values()[j] + ": " + transitions[j]);
                }
                System.out.println();
            }

            int[] rgbArray = img.getRGB(
                    0, 
                    0, 
                    img.getWidth(), 
                    img.getHeight(), 
                    null, 
                    0, 
                    img.getWidth()
            );
            
            int[] posterized = new int[rgbArray.length];
            ImageProcessing.posterizeImageInt(rgbArray, posterized, 65);
            
            int[] wallTypes = new int[img.getWidth()];
            int[] wallBases = new int[img.getWidth()];
            int[] wallHeights = new int[img.getWidth()];
            
            fsmRunner.identifyWalls(posterized, wallTypes, wallBases, wallHeights);

            System.out.println("wew");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void printArrayNicely(int[] array, int width) {
        
    }
}
