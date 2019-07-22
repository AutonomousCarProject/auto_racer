package org.avphs.map;

import javax.swing.*;
import java.awt.*;

public class Map {
    private boolean[][] mapGridData;

    //in MM per index
    public float scale = 10;

    public int startX = 150, StartY = 150;

    /**
     * Creates a new map that is pre-made to be a square
     */
    public Map(){
        //FAKE DATA: Returns square test track. True = Track, False = Not Track
        //Link to Desmos Graph of Sample Track (7/9/2019 - not created yet): https://www.desmos.com/calculator/obecg5hw5z
        // [x][y]
        mapGridData = new boolean[1000][1000];

        /*for(int i = 0; i < 1000; i++)
        {
            for(int j = 0; j < 1000; j++)
            {
                mapGridData[i][j] = false;
            }
        }
        for (int i = 100; i < 900; i++)
        {
            for (int j = 100; j < 900; j++)
            {
                mapGridData[i][j] = true;
            }
        }
        for (int i = 300; i < 700; i++)
        {
            for (int j = 300; j < 700; j++)
            {
                mapGridData[i][j] = false;
            }
        }*/
    }

    /**
     * replaces current map with new map
     * Note:This function may never be used, its more of a formality
     * @param map New map to replace with
     */
    public void setMap(boolean[][] map){
        mapGridData = map;
    }

    /**
     * @return map stored in instance of class
     */
    public boolean[][] getMap()
    {
        return mapGridData;
    }

    public void showMap() {
        JFrame frame = new JFrame("Display");
        frame.setSize(mapGridData.length, mapGridData[0].length);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new Display(mapGridData), BorderLayout.CENTER);
    }

    public void setValueAtIndex(float xlocation, float ylocation, boolean value)
    {
        if(!(xlocation == -1 || ylocation == -1)){
            int x = Math.round(xlocation); int y = Math.round(ylocation);
            mapGridData[x][ y] = value;
            mapGridData[x][ y + 1] = value;
            mapGridData[x][ y - 1] = value;

            mapGridData[x + 1][ y] = value;
            mapGridData[x + 1][ y + 1] = value;
            mapGridData[x + 1][ y - 1] = value;

            mapGridData[x - 1][ y] = value;
            mapGridData[x - 1][ y + 1] = value;
            mapGridData[x - 1][ y - 1] = value;

            System.out.println("Value set true at: " + x + "," + y + ".");
        }
    }

}

