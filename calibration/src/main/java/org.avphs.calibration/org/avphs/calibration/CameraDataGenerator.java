package org.avphs.calibration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Predicate;

import javafx.util.Pair;


public class CameraDataGenerator {
    public static void main(String[] args) {
        try {
            var img = javax.imageio.ImageIO.read(new File("grid.png"));
            var width = img.getWidth();
            var height = img.getHeight();
            var runningTotal = 0;
            var darknesses = new int[width][height];
            var twoColors = new boolean[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    var color = new Color(img.getRGB(x, y));
                    var darkness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    runningTotal += darkness;
                    darknesses[x][y] = darkness;
                }
            }

            var threshhold = .75 * runningTotal / (width * height);
            var isDark = new boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    isDark[x][y] = darknesses[x][y] < threshhold;
                }
            }
            //true = dark!!


            //a list of (line slice x, slice starting y, slice ending y)


            var hLineSlices = new ArrayList[width];


            for (int x = 0; x < width; x++) {


                int lineStart = 0;

                var writingLine = false;
                var window = new ArrayList<Boolean>();
                int length = 9;

                for (int i = 0; i < length; i++) {
                    window.add(false);
                }

                for (int y = 0; y < height; y++) {
                    window.add(isDark[x][y]);
                    window.remove(0);

                    int center = (length + 1) / 2 + y;

                    if (writingLine && majority(window, i -> !i)) {
                        //if most pixels aren't stripes
                        writingLine = false;
                        var lineEnd = center;
                        int[] slice = new int[]{lineStart, lineEnd};
                        hLineSlices[x].add(slice);


                    } else if (!writingLine && majority(window, i -> i)) {
                        writingLine = true;
                        lineStart = center;
                    }


                }
            }

            var hLines = new ArrayList<int[]>();

            for (int x = 0; x < hLineSlices.length; x++) {
                ArrayList slices = hLineSlices[x];
                ArrayList nxtSlices = hLineSlices[x+1];
                while(slices.size()!=nxtSlices.size()){

                }
                //start at middle
                //Radiate from center
                //while segments on left != num on right .....
                //else if left has segment that touches outside and right doesn't
                //else combine narrows
                //get rid of farthest gap
                //



                boolean badLine= true;

                do{

                }while ();
                for (int sliceNum = 0; sliceNum < slices.size(); sliceNum++) {
                    var distanceFromEdge = ;
                    int distanceFromNeigbor = ;
                    if (distanceFromEdge > distanceFromNeigbor) {
                        int midpoint =
                                hLines.add(midpoint);
                    }
                }
            }


            //connect lines to eachother
            //remove lines w/ pixels at edge
            //find centers of each line
            //straighten centers
            //plot points
            //interpolate/smooth
            //generate output!!


            //repeat for other direction


        } catch (IOException e) {

        }


    }

    private static <T> boolean majority(ArrayList<T> list, Predicate<T> p) {
        int pass = 0;
        int fail = 0;

        for (T item : list) {
            if (p.test(item)) {
                pass++;
            } else {
                fail++;
            }
        }

        return pass > fail;
    }

}