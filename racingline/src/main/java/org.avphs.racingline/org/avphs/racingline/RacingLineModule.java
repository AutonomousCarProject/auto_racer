package org.avphs.racingline;

import java.util.*;
import java.lang.Math;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import java.util.ArrayList;

public class RacingLineModule implements CarModule {
    private ArrayList<Point> outerWall = new ArrayList<Point>();
    private ArrayList<Point> innerWall = new ArrayList<Point>();
    private RacingLine center = new RacingLine();
    private boolean[][] map;
    private boolean[][] visited;
    private boolean[][] added;
    private boolean addToOuter;
    private int length, width;
    private int[] dx = {-1, 0, 1, 0};
    private int[] dy = {0, 1, 0, -1};

    //region RacingLine
    /**
     * This method creates the racing line. This should be run before getRacingLine is called.
     *
     * @param map The map of the track represented by a 2d boolean array.
     * @see RacingLine getRacingLine()
     */
    public void makeRacingLine(boolean[][] map) {
        this.map = map;
        length = map.length;
        width = map[0].length;
        getMiddleLine();
    }

    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     * //@see void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        if (center == null) {
            System.out.println("Warning: No RacingLine has been created yet. Run makeRacingLine to create a RacingLine");
        }
        return center;
    }

    @Override
    public Class[] getDependencies() {
        return new Class[0];
    }

    @Override
    public void init(CarModule[] dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[0];
    }

    @Override
    public void update(CarData carData) {

    }

    public class RacingLine {
        public ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
        public RacingLinePoint[] RacingLinePoints;

        public RacingLine() {        
    }
    //endregion

    //region Middle Line
    private RacingLine getMiddleLine() {
        center = new RacingLine();
        getWalls();
        calcMiddleLine();
        center.sortPoints();
        return center;
    }

    private void getWalls() {
        visited = new boolean[length][width];
        added = new boolean[length][width];
        addToOuter = true;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == false && visited[i][j] == false) {
                    BFS(i, j);
                    addToOuter = false;
                }
            }
        }
    }

    private void BFS(int startx, int starty) {
        Queue<Point> states = new LinkedList<Point>();
        states.add(new Point(startx, starty));
        visited[startx][starty] = true;
        while (states.isEmpty() == false) {
            Point currentPoint = states.remove();
            int x = currentPoint.x;
            int y = currentPoint.y;
            for (int i = 0; i < 4; i++) {
                int tx = x + dx[i];
                int ty = y + dy[i];
                if (tx >= 0 && tx < length && ty >= 0 && ty < width) {
                    if (map[tx][ty] == true && added[x][y] == false) {
                        added[x][y] = true;
                        Point newPoint = new Point(x, y);
                        if (addToOuter == true) {
                            outerWall.add(newPoint);
                        } else {
                            innerWall.add(newPoint);
                        }
                    }
                    if (map[tx][ty] == false && visited[tx][ty] == false) {
                        states.add(new Point(tx, ty));
                        visited[tx][ty] = true;
                    }
                }
            }
        }
    }

    private void calcMiddleLine() {
        ArrayList<Point> longer = outerWall.size() > innerWall.size() ? outerWall : innerWall;
        ArrayList<Point> shorter = outerWall.size() <= innerWall.size() ? outerWall : innerWall;
        for (int i = 0; i < longer.size(); i++) {
            int closePoint = 0;
            float dist = length + width;
            for (int j = 0; j < shorter.size(); j++) {
                float testDist = distanceBetweenPoints(longer.get(i), shorter.get(j));
                if (testDist < dist) {
                    closePoint = j;
                    dist = testDist;
                }
            }
            center.addPoint(midPoint(longer.get(i), shorter.get(closePoint)));
        }
    }

    private float distanceBetweenPoints(Point start, Point end) {
        int x = Math.abs(end.x - start.x);
        int y = Math.abs(end.y - start.y);
        float h = (float) Math.sqrt(x * x + y * y);
        return h;
    }

    private RacingLinePoint midPoint(Point outer, Point inner) {
        float aveX = (float) ((float) (outer.x + inner.x) / 2.0);
        float aveY = (float) ((float) (outer.y + inner.y) / 2.0);
        return new RacingLinePoint(aveX, aveY);
    }
    //endregion
}

//region Classes
class Point {
    int x, y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int _x, int _y) {
        x = _x;
        y = _y;
    }
}
//endregion