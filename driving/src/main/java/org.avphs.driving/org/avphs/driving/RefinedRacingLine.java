package org.avphs.driving;

import org.avphs.util.VectorPoint;

import java.util.*;

public class RefinedRacingLine {
    private ArrayList<RoadData> roadData;
    private VectorPoint currentPos;

    public RefinedRacingLine(ArrayList<RoadData> input, VectorPoint currentPos){
        this.currentPos = currentPos;
        roadData = input;
    }

    public ArrayList<RoadData> getRoadData(){
        return roadData;
    }

    public VectorPoint getCurrentPos(){
        return currentPos;
    }

}
