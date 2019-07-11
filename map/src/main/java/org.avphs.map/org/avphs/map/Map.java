package org.avphs.map;

import javax.swing.*;
import java.awt.*;

public class Map {
    private boolean[][] mapData;

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
        mapData = new boolean[1000][1000];

        for(int i = 0; i < 1000; i++)
        {
            for(int j = 0; j < 1000; j++)
            {
                mapData[i][j] = false;
            }
        }
        for (int i = 100; i < 900; i++)
        {
            for (int j = 100; j < 900; j++)
            {
                mapData[i][j] = true;
            }
        }
        for (int i = 300; i < 700; i++)
        {
            for (int j = 300; j < 700; j++)
            {
                mapData[i][j] = false;
            }
        }
    }

    /**
     * replaces current map with new map
     * Note:This function may never be used, its more of a formality
     * @param map New map to replace with
     */
    public void setMap(boolean[][] map){
        mapData = map;
    }

    /**
     * @return map stored in instance of class
     */
    public boolean[][] getMap()
    {
        return mapData;
    }

    public void showMap() {
        JFrame frame = new JFrame("Display");
        frame.setSize(mapData.length, mapData[0].length);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new Display(mapData), BorderLayout.CENTER);
    }

}

