package org.avphs.detection;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ObjectData {
    ArrayList<Obstacle> obstacles;


    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }



    public ObjectData(){
        obstacles = new ArrayList<>();
    }


}
