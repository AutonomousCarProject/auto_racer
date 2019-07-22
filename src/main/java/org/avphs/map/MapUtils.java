package org.avphs.map;

class MapUtils {
    // private double[] cos = new double[721];
    // private double[] sin = new double[721];

    final static boolean ERROR_LOGGING = true;//turn off for actual race



    private float[] getImageWidthBasedOnPixelHeight_Lookup = new float[481];
    private float[] getStraightAheadDistanceFromPixelHeight_Lookup = new float[481];

    /*public void setupSineAndCosine()//Run before car starts so we dont have to calc sines and cosines
    {
        for (int i = 0; i <= 361; i++) {//sets up sin and cosine array for 1 degree intervals. This may not be extremely accurate
            cos[i] = Math.cos(Math.toRadians(i));
            sin[i] = Math.sin(Math.toRadians(i));
        }
    }*/
    public void setupDistanceLookup()
    {
        //image width as pixel height increases
        for (int i = 0; i < 480; i++)
        {
            if(i > 224){
                getImageWidthBasedOnPixelHeight_Lookup[i] = -1;
                getStraightAheadDistanceFromPixelHeight_Lookup[i] = -1;
            } else {
                getImageWidthBasedOnPixelHeight_Lookup[i] = (float) ((48.8606 * 224.591) / ((224.591) - (float) i));
                getStraightAheadDistanceFromPixelHeight_Lookup[i] = (float) ((5811.09) / (224.329 - (float) i));
            }
        }

        //height
    }

    public float[] getCoordinatesOnMap(int pixelX, int pixelY, float posX, float posY, float angle)
    {
        if(pixelY > 224){
            if (ERROR_LOGGING)
                System.out.println("Y pixel was too high to be accurate, so it was skipped");
            return new float[]{-1,-1};
        }

        //return format is distance ahead y, distance ahead x
        if (angle < 0) {
            angle += 360;
        }

        float[] coordsOnMap = new float[2]; //(x,y) (ahead)
        //Maybe just make into 1 line.
        float getImageWidthAtGivenPixelHeight = getImageWidthBasedOnPixelHeight_Lookup[pixelY];
        float distanceToTheLeftOrRight = ((float)(pixelX / 640)  * getImageWidthAtGivenPixelHeight);
        //System.out.println(getImageWidthBasedOnPixelHeight_Lookup[10]);

        // System.out.println(pixelX + "," + pixelY + "," + posX + "," +posY + "," +angle + "," + getImageWidthAtGivenPixelHeight + "," + distanceToTheLeftOrRight);
        if (pixelX < 320) //if you're going to left
        {
            distanceToTheLeftOrRight = distanceToTheLeftOrRight - getImageWidthAtGivenPixelHeight;
        }
        else if (pixelX > 320) //if you're going to right
        {
            distanceToTheLeftOrRight = getImageWidthAtGivenPixelHeight - distanceToTheLeftOrRight;
        }
        else // in center
        {
            coordsOnMap[0] = posX + 0; coordsOnMap[1] =  getStraightAheadDistanceFromPixelHeight_Lookup[pixelY];
            return coordsOnMap;
        }

        float diagonalLength = (float)(Math.sqrt(Math.pow(distanceToTheLeftOrRight, 2) + Math.pow(getStraightAheadDistanceFromPixelHeight_Lookup[pixelY], 2)));

        coordsOnMap[0] = (posX + (float)(diagonalLength * Math.sin(Math.atan(distanceToTheLeftOrRight / getStraightAheadDistanceFromPixelHeight_Lookup[pixelY]) + angle)));
        System.out.println(coordsOnMap[0]);
        coordsOnMap[1] = (posY + (float)(diagonalLength * Math.cos(Math.atan(distanceToTheLeftOrRight / getStraightAheadDistanceFromPixelHeight_Lookup[pixelY]) + angle)));
        System.out.println(coordsOnMap[1]);
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
