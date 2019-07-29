package org.avphs.map;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

public class MapRacingModule implements CarModule {
    Map map = new Map();
    int rows;
    int columns;

    @Override
    public void init(CarData carData) {
        try {
            BufferedReader bufread = new BufferedReader(new FileReader("src/main/java/org/avphs/map/map.txt"));
            StringTokenizer st = new StringTokenizer(bufread.readLine());
            rows = Integer.parseInt(st.nextToken());
            columns = Integer.parseInt(st.nextToken());
            boolean[][] testMap = new boolean[rows][columns];
            for (int i = 0; i < rows; i++) {
                String currentRow = bufread.readLine();
                for (int j = 0; j < columns; j++) {
                    testMap[i][j] = currentRow.charAt(j) == '1';
                }
            }
            map.setMap(testMap);
        } catch (Exception e){
            System.out.println("Map may not have been initialized or there is no file to read ");
        }
        map.showMap();
        carData.addData("map", map);
    }

    public void update(CarData carData) {

    }
}
