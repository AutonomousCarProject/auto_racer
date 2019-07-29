package org.avphs.detection;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ObjectData {
    @Deprecated ArrayList<Obstacle> obstacles;
    Obstacle obstacle;

    boolean found = false;


    @Deprecated ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    ObjectData(){
        obstacle = new Obstacle();
    }

    Obstacle getObstacle(){
        return obstacle;
    }

    /**
     * @return If and object was found, or is found this frame
     */
    boolean isFound(){
        return found;
    }




}
