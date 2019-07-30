package org.avphs.calibration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;


public class CameraDataGenerator {
    private static int height;
    private static int width;
    public static void main(String[] args) {
        try {
            BufferedImage img = javax.imageio.ImageIO.read(new File("grid.png"));
            width = img.getWidth();
            height = img.getHeight();
            int runningTotal = 0;
            int[][] darknesses = new int[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Color color = new Color(img.getRGB(x, y));
                    int darkness = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    runningTotal += darkness;
                    darknesses[x][y] = darkness;
                }
            }

            double threshhold = .75 * runningTotal / (width * height);
            boolean[][] isDark = new boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    isDark[x][y] = darknesses[x][y] < threshhold;
                }
            }
            //true = dark!!


            //a list of (line slice x, slice starting y, slice ending y)


            ArrayList<ArrayList<int[]>> hLineSlices = new ArrayList();

            for (int x = 0; x < width; x++) {


                int lineStart = 0;

                boolean writingLine = false;
                ArrayList<Boolean> window = new ArrayList<Boolean>();
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
                        int lineEnd = center;
                        int[] slice = new int[]{lineStart, lineEnd};
                        hLineSlices.get(x).add(slice);


                    } else if (!writingLine && majority(window, i -> i)) {
                        writingLine = true;
                        lineStart = center;
                    }


                }
            }

            ArrayList<int[]> hLines = new ArrayList<>();


            int minSlices = -1;
            int sliceIdx = -1;
            for (int i = 0; i <width ; i++) {
                int newSize = hLineSlices.get(i).size();
                if(minSlices<newSize){
                    minSlices = newSize;
                    sliceIdx = i;
                }
            }

            boolean linesVanishing = false;

            int linesRemovedBottom = 0;
            int linesRemovedTop = 0;

            int halfway = width/2;
            List<ArrayList<int[]>> firstHalf = hLineSlices.subList(0, halfway-1);
            List<ArrayList<int[]>> secondHalf = hLineSlices.subList(halfway, width-1);

            Collections.reverse(firstHalf);

            int[][] lines = new int[minSlices][];

            lines = new int[minSlices][];

            int[][] firstClean = cleanLines(firstHalf, minSlices);
            int[][] secondClean = cleanLines(firstHalf, minSlices);

            int[][] heightDiffs = new int[minSlices][];

            for (int i = 0; i < firstClean.length; i++) {
                //int diff = firstClean
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

    private static int[][] cleanLines(List<ArrayList<int[]>> lines, int minSlices) {
        //1st num: line it belongs to
        //2nd num: input x, get y
        int[][] result = new int[minSlices][];

        int removeTop = 0;
        int removeBottom = 0;
        for (int x = 0; x < lines.size(); x++) {
            ArrayList<int[]> slices = lines.get(x);
            ArrayList<int[]> nxtSlices = lines.get(x + 1);
            int topRemoved = 0;
            int bottomRemoved = 0;
            if(slices.get(0)[0]>0 && nxtSlices.get(0)[0]<=0){
                removeTop++;
            }
            if(slices.get(0)[0] <= 0){
                removeBottom++;

            }


            while(slices.size()>minSlices){
                int bottomError = removeBottom - bottomRemoved;
                if(removeTop - topRemoved>0) {
                    int topDist = slices.get(0)[0];
                    int bottomDist = height - slices.get(slices.size())[1];
                    if (bottomError > 0 && bottomDist<topDist) {
                        slices.remove(slices.size());

                    }

                }

                else if(bottomError>0){
                    slices.remove(slices.size());
                }
                else {
                    int minDiff = height;
                    int diffIdx = 0;
                    int prev = 0;
                    for (int i = 0; i < slices.size(); i++) {
                        int diff = slices.get(i)[0];
                        prev = slices.get(i)[1];
                    }
                    int[] newLine = new int[]{
                      slices.get(diffIdx)[0],
                      slices.get(diffIdx + 1)[1]
                    };

                    slices.set(diffIdx, newLine);
                    slices.remove(diffIdx + 1);
                }
            }

            for (int i = 0; i < slices.size(); i++) {
                int[] slice = slices.get(i);
                int mid = (slice[0] + slice[0])/2;
                result[i][x] = mid;
            }

        }


        return result;
}

}