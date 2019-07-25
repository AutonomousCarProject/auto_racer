package org.avphs.map;

/**
 * This class is to simplify the map making process and allow for more organized data storage.
 * It will contain functions for map creation, and map updating.
 * It will have draw-like functions to enable easy map updating.
 *
 *
 * NOT DONE
 */

public class MapFormatter {

    private Map map; //Map to be formatted

    public MapUtils utils = new MapUtils();

    // private ArrayList<int[]> gapCoords = new ArrayList<int[]>(); // Stores coordinate locations of gaps on track.

    public MapFormatter(Map map){
        this.map = map;
    }

    public MapFormatter(){
        map = new Map();
    }

    /**
     *
     * @param pos size 2 array that contains the x,y point where the car is in relative to start
     * @param angle angle in degrees that the car is in relative to start
     * @param bottomPoints points given from image used to calculate distance from wall
     */
    public void AddData(float[] pos, float angle, int[] bottomPoints){
        //TODO: actually have a way to interpret these points being sent in
        //Assuming map is all false to begin with
        //float c = calcDistance(bottomPoints[319]); //319 is center pixel of wall.
        //testing other distance calc.
        //float c = (float)utils.getRealLifePixelDistance(500, bottomPoints[500]);

        map.setValueAtIndex(pos[0], pos[1], true);

        for (int i = 1; i < MapUtils.IMAGE_SIDE_THRESHOLD; i++)//First 100 pixels on the left side
        {
            if (bottomPoints[i] > MapUtils.Y_HEIGHT_PIXEL_THRESHOLD)//Threshold for lowest pixel height which gives us good data.
            {
                //System.out.println("Good Inside Pixel Height Value (" + (480 - bottomPoints[i]) + ")");
                float[] coords;
                coords = utils.getCoordinatesOnMap(i, MapUtils.IMAGE_HEIGHT - bottomPoints[i], pos[0], pos[1], angle);
                //System.out.println("wtf");
                map.setValueAtIndex(coords[0], coords[1], true);

            }
            else
            {
                //System.out.println("Bad Inside Pixel Height Value (" + (480 - bottomPoints[i]) + ")");
            }

        }
        //System.out.println("InsidePoints Done");
        for (int i = MapUtils.IMAGE_WIDTH-MapUtils.IMAGE_SIDE_THRESHOLD; i < MapUtils.IMAGE_WIDTH; i++)//Last 4 Pixels on the right side.
        {
            if (bottomPoints[i] > MapUtils.Y_HEIGHT_PIXEL_THRESHOLD)
            {
                //System.out.println("Good Outside Pixel Height Value (" + (480 - bottomPoints[i]) + ")");
                float[] coords;
                coords = utils.getCoordinatesOnMap(i, MapUtils.IMAGE_HEIGHT - bottomPoints[i], pos[0], pos[1], angle);
                //System.out.println("wtf");
                map.setValueAtIndex(coords[0], coords[1], true);
            }
            else
            {
                //System.out.println("Bad Outside Pixel Height Value (" + (480 - bottomPoints[i]) + ")");
            }

        }
        //System.out.println("OutsidePoints Done");

        //Note: we probably should be looking at more than just the center or we will have trouble completely building the map.
        //We will also have to implement a way to fill in holes on map
        //Formula: x + csin(theta) , b + ccos(theta)

        //ARGS 1 & 2 MIGHT NEED TO BE +/- DEPENDING ON DIRECTION
        //map.setValueAtIndex((float)( pos[0] + (c * Math.sin(angle))) ,(float)( pos[1] + (c * Math.cos(angle))) , true);

        //c = (float)utils.getRealLifePixelDistance(100, bottomPoints[100]);
        // map.setValueAtIndex((float)( pos[0] + (c * Math.sin(angle))) ,(float)( pos[1] + (c * Math.cos(angle))) , true);

        //7/15/2019: Ok... we have the code working and detecting outside walls, however, we also need to build the inside walls to complete the track.

    }

    /*public int scanTrack()
    {
        scanTrack should scan the outside and inside walls of the track and return the number of gaps in the walls of the track
        in the map we have created. If there are zero gaps we can then go on to fill the track.


        gapCoords.clear(); Clears arraylist to fill with new gap coordinates every time scanTrack is called.

        int numberOfGaps = 0;

        First scan map array and find first instance of outside track.

        Start scanning through wall

        Scan up and down through track.

        If there is a gap in track walls, add one to gap counter and store point in gapCoords.

        Find the next closest occurence of the outside track wall.

        At the end, return total number of gaps in the track.

        return numberOfGaps;
    }

     */
    /*
    public boolean fixTrack(ArrayList<int[]> gapCoordinates)
    {

        fixTrack should be ran if we found gaps in the walls of the track on our map. After fix track tries to fix the track, it
        should return whether the track has been fixed or cannot be fixed.


        boolean fixed = false;// For now I just set as false right away as we MIGHT just be calling this function only when the track is broken to begin with anyways.

        int maxScanDimensions = 20; 20x20 should be the max dimensions for a scan and fix to take place. Any scan larger will
        compromise the accuracy of the map.


        Go through gapCoords arraylist and visit each location of a gap in the track wall.

        Connect ends of track wall gap

        Scan track, and if the track has been fixed, return true. If the track has not been fixed, return false.
        if (scanTrack() == 0)
        {
            fixed = true;
        }

        return fixed;
    }*/




    @Deprecated public void fillTrack()//When the track walls are complete and the track has no gaps, fill in between the walls with track booleans as well.
    {
        //7/16/19: This code needs to be changed so that it fills when the walls are more than 1 unit thick. Raymond will explain.
        boolean trackToggle = false;
        int counter = 0;
        for (int i = 0; i < 1000; i++)
        {
            for (int j = 0; j < 1000; j++)
            {
                if (map.getMap()[i][j])
                {
                    trackToggle = false;
                    counter++;
                }
                if (counter == 2)
                {
                    trackToggle =true;
                    counter = 0;
                }
                map.setValueAtIndex((float)i, (float)j, trackToggle);
            }
        }
    }

    /**
     * main function that will be used to return values
     * May only be called once, but builds everything
     * @return map with the correct data
     */
    //I don't think this method is ever called
    public Map format(){
        //TODO: calcutlate/build map

        return map;

    }


    public void expandTrackFiveCarLengthsToTheLeftAndRightOfCurrentPos(float[] pos, float angle)
    {
        float[] coords = new float[2];

        float diff1 = pos[0]; float diff2 = pos[1];
        pos[0] = 0; pos[1] = 0;

        coords[0] = (float)(pos[0] + (70 * Math.cos(angle)));
        coords[1] = (float)(pos[1] + (70 * Math.sin(angle)));



        map.setValueAtIndex(coords[1] + diff2, (0 - coords[0]) + diff1, true);
        map.setValueAtIndex((0 - coords[1]) + diff2, coords[0] + diff1, true);
        //System.out.println("set");
    }
}
