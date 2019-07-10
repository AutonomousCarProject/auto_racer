package org.avphs.racingline;

import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;

<<<<<<< HEAD
=======
public class RacingLineModule implements CarModule {
    private ArrayList<WallPoint> outerWall = new ArrayList<WallPoint>();
    private ArrayList<WallPoint> innerWall = new ArrayList<WallPoint>();
    private boolean[][] map;
    private boolean[][] visited;
    private boolean[][] added;
    boolean addToOuter;
    int length, width;
    private int[] dx = {-1, 0, 1, 0};
    private int[] dy = {0, 1, 0, -1};
>>>>>>> Added floodfill for inner and outer walls

import java.util.ArrayList;


import java.util.ArrayList;

public class RacingLineModule implements CarModule {
    private ArrayList<WallPoint> outerWall = new ArrayList<WallPoint>();
    private ArrayList<WallPoint> innerWall = new ArrayList<WallPoint>();
    private RacingLine center = new RacingLine();
    private boolean[][] map;
    private boolean[][] visited;
    private boolean[][] added;
    private boolean addToOuter;
    private int length, width;
    private int[] dx = {-1, 0, 1, 0};
    private int[] dy = {0, 1, 0, -1};

    //region Overrides
    @Override

    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Racing Line");
    }
    //endregion

    //region RacingLine
    /**
     * This method creates the racing line. This should be run before getRacingLine is called.
     *
     * @param map The map of the track represented by a 2d boolean array.

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

     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
        return null;
    }
<<<<<<< HEAD
=======

    private void getMiddleLine() {
        getWalls();
    }

    private void getWalls() {
        visited = new boolean[length][width];
        added = new boolean[length][width];
        addToOuter = true;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == false && visited[i][j] == false) {
                    DFS(i, j);
                    addToOuter = false;
                }
            }
        }
    }

    private void DFS(int x, int y) {
        visited[x][y] = true;
        for (int i = 0; i < 4; i++) {
            int tx = x + dx[i];
            int ty = y + dy[i];
            if (tx >= 0 && tx < length && ty >= 0 && ty < width) {
                if (map[tx][ty] == true && added[x][y] == false) {
                    added[x][y] = true;
                    WallPoint newPoint = new WallPoint(x, y);
                    if (addToOuter == true) {
                        outerWall.add(newPoint);
                    } else {
                        innerWall.add(newPoint);
                    }
                }
                if (map[tx][ty] == false && visited[tx][ty] == false) {
                    DFS(tx, ty);
                }
            }
        }
    }
}
>>>>>>> Added floodfill for inner and outer walls



    public class RacingLine {
        public ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
        public RacingLinePoint[] RacingLinePoints;

        public RacingLine() {

        }
    }

    public class RacingLinePoint {
        float x, y, degree;

        RacingLinePoint() {
            setX(0);
            setY(0);
            setDegree(0);
        }

        RacingLinePoint(float x, float y) {
            setX(x);
            setY(y);
            setDegree(0);
        }

        RacingLinePoint(float x, float y, float degree) {
            setX(x);
            setY(y);
            setDegree(degree);
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }

        float getDegree() {
            return degree;
        }

        void setX(float x) {
            this.x = x;
        }

        void setY(float y) {
            this.y = y;
        }

        void setDegree(float degree) {
            this.degree = degree;
        }

    }
}

<<<<<<< HEAD
=======
}

private class WallPoint {
    int x, y;

    public WallPoint(int _x, int _y) {
        x = _x;
        y = _y;
    }
}
>>>>>>> Added floodfill for inner and outer walls
