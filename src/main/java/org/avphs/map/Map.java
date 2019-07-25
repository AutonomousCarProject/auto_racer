package org.avphs.map;

import javax.swing.*;
import java.awt.*;

public class Map {
    private boolean[][] mapGridData;


    //in MM per index
    public float scale = 10;

    public int startX = 120, startY = 150;

    /**
     * Creates a new map that is pre-made to be a square
     */
    public Map(int x, int y)
    {
        mapGridData = new boolean[x][y];
    }


    public Map(){
        //FAKE DATA: Returns square test track. True = Track, False = Not Track
        //Link to Desmos Graph of Sample Track (7/9/2019 - not created yet): https://www.desmos.com/calculator/obecg5hw5z
        // [x][y]
        mapGridData = new boolean[1500][1500];


        //Square Map
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
        frame.setSize((int)(mapGridData.length*Display.scale), (int)(mapGridData[0].length*Display.scale));
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new Display(mapGridData), BorderLayout.CENTER);

    }

    public void setValueAtIndex(float xlocation, float ylocation, boolean value)
    {
        if( !((xlocation < 0)|| (ylocation < 0)) ){
            int x = Math.round(xlocation); int y = Math.round(ylocation);

            // will fill in a 3x3 box rather than a 1x1
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

