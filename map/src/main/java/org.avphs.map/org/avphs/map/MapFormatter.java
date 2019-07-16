package org.avphs.map;

import java.util.ArrayList;

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

    float[] pos = new float[2]; //position of car from position group

    float angle; // angle of car




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
    }

    /**
     * main function that will be used to return values
     * May only be called once, but builds everything
     * @return map with the correct data
     */
    public Map format(){
        //TODO: calcutlate/build map

        return map;

    }
}
