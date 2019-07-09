package org.avphs.map;

public class Map {
    private boolean[][] mapData;

    //in MM per index
    public float scale = 10;

    public Map(){
        //FAKE DATA: Returns square test track. True = Track, False = Not Track
        //Link to Desmos Graph of Sample Track (7/9/2019 - not created yet): https://www.desmos.com/calculator/obecg5hw5z
        // [x][y]
        mapData = new boolean[1000][1000];

        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 1000; j++)
            {
                mapData[i][j] = false;
            }
        }
        for (int i = 100; i < 900; i++)
        {
            for (int j = 900; j < 1000; j++)
            {
                mapData[i][j] = false;
            }
        }
        for (int i = 100; i < 900; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                mapData[i][j] = false;
            }
        }
        for (int i = 900; i < 1000; i++)
        {
            for (int j = 0; j < 1000; j++)
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

    public void setMap(boolean[][] map){
        mapData = map;
    }

    public boolean[][] getMap()
    {
        return mapData;
    }
}
