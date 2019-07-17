package org.avphs.map;

class MapUtils {
    private double[] cos = new double[721];
    private double[] sin = new double[721];

    private double[] imageWidthToPixelHeightLookup = new double[481];
    private double[] verticalDistanceLookup = new double[481];

    public void setupSineAndCosine()//Run before car starts so we dont have to calc sines and cosines
    {
        for (int i = 0; i <= 361; i++) {//sets up sin and cosine array for 1 degree intervals. This may not be extremely accurate
            cos[i] = Math.cos(Math.toRadians(i));
            sin[i] = Math.sin(Math.toRadians(i));
        }
    }

    public void setupDistanceLookup() {
        //image width as pixel height increases
        for (int i = 0; i < 220; i++) {
            imageWidthToPixelHeightLookup[i] = (48.8606 * 224.591) / ((224.591) - (double) i);
            verticalDistanceLookup[i] = (5811.09) / (224.329 - (double) i);
        }

        //height
    }

    public int[] getRealLifePixelDistance(int pixelX, int pixelY) {
        //return format is distance ahead y, distance ahead x
        int[] distances = new int[2]; //(x,y) (ahead)
        //Maybe just make into 1 line.
        double imageWidthAtPosition = imageWidthToPixelHeightLookup[pixelY];
        double xdistance = ((double) (pixelX / 640) * imageWidthAtPosition);
        if (pixelX < 320) //if you're going to left
        {
            distances[0] = (0 - ((int) (Math.round(imageWidthAtPosition / 2) - (Math.round(xdistance)))));
            distances[1] = (int) Math.round(verticalDistanceLookup[pixelY]);
        } else if (pixelX > 320) //if you're going to right
        {
            distances[0] = (int) ((Math.round(xdistance)) - (Math.round(imageWidthAtPosition / 2)));
            distances[1] = (int) Math.round(verticalDistanceLookup[pixelY]);
        } else // in centwer
        {
            distances[0] = 0;
            distances[1] = (int) Math.round(verticalDistanceLookup[pixelY]);
        }
        return distances;
    }

        public double getSine ( int angleInDegrees)//get sine value from degree in int from 0 to 360
        {
            double result = sin[angleInDegrees];
            return result;
        }
        public void fixTrack( int[] imageInput){ //TODO: Finish later.
            int previousPoint = imageInput[0]; //390
            int currentPoint; // keeps things in track
            int beforeMissing;
            int afterMissing;
            for (int i = 0; i < imageInput.length; i++) {
                currentPoint = imageInput[i];
                if (currentPoint != 0) {
                    previousPoint = currentPoint; // self explanatory.
                } else {
                    beforeMissing = i - 1; //Keeps track of the previous index before it turned 0.
                    while (imageInput[i] == 0) { // Only when it reaches a missing hole.

                    }
                }
            }
        }
        public double getCosine(int angleInDegrees)//get cosine value from degree in int from 0 to 360
        {
            double result = cos[angleInDegrees];
            return result;
        }

}
