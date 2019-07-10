package org.avphs.racingline;

import org.java.ArrayList;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import RacingLinePoint;

public class RacingLineModule implements CarModule {
    private RacingLine racingLine;
    private ArrayList<WallPoint> outerWall = new ArrayList<WallPoint>();
    private ArrayList<WallPoint> innerWall = new ArrayList<WallPoint>();
    private boolean[][] map;
    private boolean[][] visited;
    boolean addToOuter;
    int length, width;
    private int[] dx = {-1, 0, 1, 0};
    private int[] dy = {0, 1, 0, -1};

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
     * @see void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
    }

    private void getMiddleLine() {
        getWalls();
    }

    private void getWalls() {
        visited = new boolean[length][width];
        addToOuter = true;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (map[i][j] == false && visited[i][j] == false) {
                    DFS(i, j);
                    addToOuter = false;
                }
            }
        }
        addToOuter = false;
    }

    private void DFS(int x, int y) {
        visited[x][y] = true;
        for (int i = 0; i < 4; i++) {
            int tx = x + dx[i];
            int ty = y + dy[i];
            if (tx >= 0 && <length && ty >= 0 && ty < width){
                if (map[tx][ty] == true) {
                    WallPoint newPoint = new WallPoint(tx, ty);
                    if (addToOuter == true) {
                        outerWall.add(newPoint);
                    } else {
                        innerWall.add(newPoint);
                    }
                }
                if (visited[tx][ty] == false) {
                    DFS(tx, ty);
                }
            }
        }
    }
}

/**
 * <p>This class represents a racing line. It contains an array of points which represent the line.</p>
 *
 * @see RacingLinePoint
 */
public class RacingLine {
    private ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
    private RacingLinePoint[] RacingLinePoints;

    public RacingLine() {

    }

    public RacingLinePoint[] getRacingLinePoints() {
        return RacingLinePoints;
    }
}

public class RacingLinePoint {
    private float x, y, degree;

    public RacingLinePoint() {
        setX(0);
        setY(0);
        setDegree(0);
    }

    public RacingLinePoint(float x, float y) {
        setX(x);
        setY(y);
        setDegree(0);
    }

    public RacingLinePoint(float x, float y, float degree) {
        setX(x);
        setY(y);
        setDegree(degree);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDegree() {
        return degree;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setDegree(float degree) {
        this.degree = degree;
    }

}

private class WallPoint {
    int x, y;

    public WallPoint(int _x, int _y) {
        x = _x;
        y = _y;
    }
}