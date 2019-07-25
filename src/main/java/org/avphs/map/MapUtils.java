package org.avphs.map;

import java.io.FileWriter;
import java.io.IOException;

class MapUtils {

    // private double[] cos = new double[721];
    // private double[] sin = new double[721];
    final static boolean ERROR_LOGGING = true; //turn off for actual race
    private float[] pixelHeightToX = new float[481]; // lookup table for a pixel height, returns width on map
    private float[] pixelHeightToY = new float[481]; // lookup table for a pixel height, returns straight ahead distance on map

    public final static int
            IMAGE_SIDE_THRESHOLD = 5, //the amount of pixels in from the sides of the image we look
            Y_HEIGHT_PIXEL_THRESHOLD = 270, //amount to pixels up we look for wall recognition
            IMAGE_WIDTH = 640,
            IMAGE_HEIGHT = 480;

    // why do we even need this - eric
    /*public void setupSineAndCosine(){ // Run before car starts so we dont have to calc sines and cosines
        for (int i = 0; i <= 361; i++) {//sets up sin and cosine array for 1 degree intervals. This may not be extremely accurate
            cos[i] = Math.cos(Math.toRadians(i));
            sin[i] = Math.sin(Math.toRadians(i));
        }
    }*/

    public static void writeToFile(boolean[][] m){
        try{
            FileWriter f = new FileWriter("src/main/java/org/avphs/map/map.txt");
            f.write(m.length + "  " + m[0].length + "\n");
            for(int i = 0; i < m.length; i++){
                for (int j = 0; j < m[0].length; j++){
                    if(m[i][j])f.write('1');
                    else f.write('0');
                }
                f.write('\n');
            }

            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setupDistanceLookup() { // initializes the values in
        for (int i = 0; i < pixelHeightToX.length - 1; i++) {
            if (i > 210) { //
                pixelHeightToX[i] = -1;
                pixelHeightToY[i] = -1;
            } else {
                pixelHeightToX[i] = (float)((48.8606 * 224.591) / ((224.591) - (float)i)); // y = c1 / (c2 - x) for the widths of the field of vision
                pixelHeightToY[i] = (float)((5811.09) / (224.329 - (float)i)); // y = c1 / (c2 - x) for vertical distances
            }
        }
    }

    public float[] getCoordinatesOnMap(int pixelX, int pixelY, float posX, float posY, float angle) {
        if (pixelY > MapUtils.IMAGE_HEIGHT - MapUtils.Y_HEIGHT_PIXEL_THRESHOLD){//480 - 180 = 300, which is the minimum pixel height to obtain useful data from.
            if (ERROR_LOGGING)
                System.out.println("Y pixel was too high to be accurate, so it was skipped");
            return new float[]{-1,-1};
        }

        //return format is distance ahead y, distance ahead x
        if (angle < 0) {
            angle += 360;
        }

        float[] coordsOnMap = new float[2]; //(x,y) These are the Coordinates of wall that are being exported
        coordsOnMap[0] = posX; coordsOnMap[1] = posY;

        //Y
        float getImageWidthAtGivenPixelHeight = pixelHeightToX[pixelY];
        float distanceToTheLeftOrRight = ((float)(getImageWidthAtGivenPixelHeight / 2));//Because we are only looking at the first and last few pixels
        //System.out.println(pixelHeightToX[10]);

        // System.out.println(pixelX + "," + pixelY + "," + posX + "," +posY + "," +angle + "," + getImageWidthAtGivenPixelHeight + "," + distanceToTheLeftOrRight);
        if (pixelX < 320) //if you're going to left
        {
            distanceToTheLeftOrRight = (0 - distanceToTheLeftOrRight);
        }
        else if (pixelX > 320) //if you're going to right
        {
            //No change, stays positive
        }
        else // in center
        {
            coordsOnMap[0] = posX + 0; coordsOnMap[1] =  pixelHeightToY[pixelY];
            return coordsOnMap;
        }

        float diagonalLength = (float)(Math.sqrt(Math.pow(distanceToTheLeftOrRight, 2) + Math.pow(pixelHeightToY[pixelY], 2)));

        if (angle > 180)
        {
            coordsOnMap[0] = (posX - (float)(diagonalLength * Math.sin(Math.atan(distanceToTheLeftOrRight / pixelHeightToY[pixelY]) + angle)));
        }
        else
        {
            coordsOnMap[0] = (posX + (float)(diagonalLength * Math.sin(Math.atan(distanceToTheLeftOrRight / pixelHeightToY[pixelY]) + angle)));
        }

        //System.out.println("X Pos of Wall on Map: " + coordsOnMap[0]);
        coordsOnMap[1] = (posY + (float)(diagonalLength * Math.cos(Math.atan(distanceToTheLeftOrRight / pixelHeightToY[pixelY]) + angle)));
        // System.out.println("Y Pos of Wall on Map: " + coordsOnMap[1]);
        return coordsOnMap;
    }


    public static void fixTrack(int[] imageInput) { //TODO: Finish later.
        float holeStart = 0, holeStartIndex = 0, holeEnd = 0; // pixel heights of wall at beginning and end of hole
        if (imageInput[0] != 0){
            for (int i = 1; i < imageInput.length; i++) {
                if (imageInput[i] == 0 && imageInput[i - 1] != 0) {
                    holeStart = imageInput[i - 1];
                    holeStartIndex = i - 1;
                } if (imageInput[i] != 0 && imageInput[i - 1] == 0) {
                    holeEnd = imageInput[i];
                    float holeLength = i - holeStartIndex;
                    float step = (holeStart - holeEnd) / holeLength;
                    for (int j = i - 1; j > holeStartIndex; j--) { //Every time he's adding to itself
                        imageInput[j] = (int)(holeEnd += step); //if int it's bad
                    } // CarModule.getModuleData("image")
                    //returns imageData object, returns top, bottom, type
                }
            }
        } else {
            System.out.println("Debug Statement: There is a hole in the beginning of the image, why?");
        }
    }


    /*public double getSine(int angleInDegrees)//get sine value from degree in int from 0 to 360
    {
        double result = sin[angleInDegrees];
        return result;
    }

    public double getCosine(int angleInDegrees)//get cosine value from degree in int from 0 to 360
    {
        double result = cos[angleInDegrees];
        return result;
    }*/
}
