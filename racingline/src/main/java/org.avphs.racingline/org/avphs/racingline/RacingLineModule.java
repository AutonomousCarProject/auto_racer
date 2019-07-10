package org.avphs.racingline;

import java.util.ArrayList;
import java.lang.Math;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

public class RacingLineModule implements CarModule {
    private ArrayList<WallPoint> outerWall = new ArrayList<WallPoint>();
    private ArrayList<WallPoint> innerWall = new ArrayList<WallPoint>();
    private RacingLine center = new RacingLine();
    private boolean[][] map;
    private boolean[][] visited;
    private boolean[][] added;
    boolean addToOuter;
    int length, width;
    private int[] dx = {-1, 0, 1, 0};
    private int[] dy = {0, 1, 0, -1};

    //region Overides
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
     * @see //void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
        return null;
    }
    //endregion

    //region Middle Line
    private void getMiddleLine() {
        getWalls();
        calcMiddleLine();
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

    private void calcMiddleLine() {
        ArrayList<WallPoint> longer = outerWall.size() > innerWall.size() ? outerWall : innerWall;
        ArrayList<WallPoint> shorter = outerWall.size() <= innerWall.size() ? outerWall : innerWall;
        for (int i = 0; i < longer.size(); i++){
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

    private float distanceBetweenPoints(WallPoint start, WallPoint end) {
        int x = Math.abs(end.x - start.x);
        int y = Math.abs(end.y - start.y);
        float h = (float)Math.sqrt(x * x + y * y);
        return h;
    }

    private RacingLinePoint midPoint(WallPoint outer, WallPoint inner) {
        float aveX = (float)((float)(outer.x + inner.x) / 2.0);
        float aveY = (float)((float)(outer.y + inner.y) / 2.0);
        return new RacingLinePoint(aveX, aveY);
    }
    //endregion
}

//region Classes
/**
 * <p>This class represents a racing line. It contains an array of points which represent the line.</p>
 *
 * @see RacingLinePoint
 */
class RacingLine {
    private ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
    private RacingLinePoint[] RacingLinePoints;

    public RacingLine() {

    }

    public RacingLinePoint[] getRacingLinePoints() {
        return RacingLinePoints;
    }

    public void addPoint(RacingLinePoint newPoint) {
        RacingLinePointsList.add(newPoint);
    }

}

class RacingLinePoint {
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

class WallPoint {
    int x, y;

    public WallPoint() {
        x = 0;
        y = 0;
    }
    public WallPoint(int _x, int _y) {
        x = _x;
        y = _y;
    }
}
//endregion