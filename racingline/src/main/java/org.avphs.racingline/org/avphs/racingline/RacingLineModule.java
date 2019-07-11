package org.avphs.racingline;

import java.util.*;
import java.lang.Math;


import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import java.util.ArrayList;
import java.util.List;


public class RacingLineModule implements CarModule {
    private ArrayList<Point> outerWall = new ArrayList<Point>();
    private ArrayList<Point> innerWall = new ArrayList<Point>();
    private RacingLine center = new RacingLine();
    private boolean[][] map;
    private boolean[][] visited;
    private boolean[][] added;
    private boolean addToOuter;
    private int rows, columns;
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
     * @see RacingLine getRacingLine()
     */
    public void makeRacingLine(boolean[][] map) {
        this.map = map;
        rows = map.length;
        columns = map[0].length;
        getMiddleLine();
        getRacingLine();
    }

    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     * //@see void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
        //RacingLinePoint test = new  RacingLinePoint();
        RacingLinePoint [] middleLine = center.getRacingLinePoints();
        float [][] middleline = new float [center.toString().length()][2];
        int moveCord = 0;
        int moveY;
        for (int x = 0 ; x < center.toString().length(); x++ ) {
            moveY=0;
            middleline [moveCord][moveY] = middleLine[moveCord].getX();
            moveY++;
            middleline [moveCord][moveY] = middleLine[moveCord].getY();
            moveCord++;
        }
        new WindowCurve(middleline);
            return null;
    }
    //endregion
    //region Middle Line
    private RacingLine getMiddleLine() {
        getWalls();
        calcMiddleLine();
        center.sortPoints();
        return center;
    }

    private void getWalls() {
        visited = new boolean[rows][columns];
        added = new boolean[rows][columns];
        addToOuter = true;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
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
            visited[x][y] = true;
            for (int i = 0; i < 4; i++) {
                int tx = x + dx[i];
                int ty = y + dy[i];
                if (tx >= 0 && tx < rows && ty >= 0 && ty < columns) {
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
                        states.add(new Point(tx,ty));
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
            float dist = rows + columns;
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
//endregion --------------------------------------------
class WindowCurve {

    private List list = new ArrayList();
    private Curve curveCurrentObj;

    public WindowCurve(float [][] middlePoints) {
        startLine(middlePoints);
        Curve curveObj;
        for (Object o : list) {
            curveObj = (Curve) o;
            curveObj.StartBezierCurve();
        }
    }


    public void startLine(float [][] middlePoints) {
        int tickA = 0; int tickB = 0;
        while (tickA + 3 < middlePoints.length) {
            curveCurrentObj = new Curve();
            curveCurrentObj.setP1(new Points(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP2(new Points(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP3(new Points(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP4(new Points(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            list.add(curveCurrentObj);
        }
    }
}

class Points {

    private float x;
    private float y;

    public Points(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
class Curve {

    private Points p1;
    private Points p2;
    private Points p3;
    private Points p4;
    private double[] H = {2, 1, -2, 1, -3, -2, 3, -1, 0, 1, 0, 0, 1, 0, 0, 0};

    public Curve() {
    }

    public void setP1(Points p1) {
        this.p1 = p1;
    }

    public void setP2(Points p2) {
        this.p2 = p2;
    }

    public void setP3(Points p3) {
        this.p3 = p3;
    }

    public void setP4(Points p4) {
        this.p4 = p4;
    }

    public void StartBezierCurve() {

        DrawBezierCurve(p1, p2, p3, p4, 140);
    }

    Vector4 GetHermiteCoeff(double x0, double s0, double x1, double s1) {

        Matrix4 basis = new Matrix4(H);
        Vector4 v = new Vector4(x0, s0, x1, s1);
        return basis.multiply(v);
    }

    void DrawHermiteCurve(Points P0, Points T0, Points P1, Points T1, int numpoints) {

        Vector4 xcoeff = GetHermiteCoeff(P0.getX(), T0.getX(), P1.getX(), T1.getX());
        Vector4 ycoeff = GetHermiteCoeff(P0.getY(), T0.getY(), P1.getY(), T1.getY());


        if (numpoints < 2) {
            return;
        }
        double dt = 1.0 / (numpoints - 1);

        for (double t = 0; t <= 1; t += dt) {
            Vector4 vt = new Vector4();
            vt.setValue(3, 1);
            for (int i = 2; i >= 0; i--) {
                vt.setValue(i, vt.getValue(i + 1) * t);
            }
            int x = (int) Math.round(xcoeff.DotProduct(vt));
            int y = (int) Math.round(ycoeff.DotProduct(vt));
            System.out.println((float) x + "," + (float) y);
        }
    }

    void DrawBezierCurve(Points P0, Points P1, Points P2, Points P3, int numpoints) {

        Points T0 = new Points(3 * (P1.getX() - P0.getX()), 3 * (P1.getY() - P0.getY()));
        Points T1 = new Points(3 * (P3.getX() - P2.getX()), 3 * (P3.getY() - P2.getY()));
        DrawHermiteCurve(P0, T0, P3, T1, numpoints);
    }
}

class Vector4 {

    public double[] v = new double[4];

    public Vector4() {
    }

    public Vector4(double a, double b, double c, double d) {
        v[0] = a;
        v[1] = b;
        v[2] = c;
        v[3] = d;
    }

    public double getValue(int index) {
        return v[index];
    }

    public void setValue(int index, double value) {
        v[index] = value;
    }

    public double DotProduct(Vector4 b) {
        return v[0] * b.v[0] + v[1] * b.v[1] + v[2] * b.v[2] + v[3] * b.v[3];
    }
}

class Matrix4 {

    public Vector4[] M = new Vector4[4];

    public Matrix4(double[] A) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            M[i] = new Vector4();
            for (int j = 0; j < 4; j++) {
//                System.out.println(A[count]);
//                System.out.println( M[i].getValue(0));
                M[i].setValue(j, A[count]);
                count++;
            }
        }
    }

    Vector4 multiply(Vector4 b) {
        Vector4 res = new Vector4();
        double count = 0.0d;
        for (int i = 0; i < 4; i++) {

            for (int j = 0; j < 4; j++) {

                count += M[i].getValue(j) * b.getValue(j);
            }
            res.setValue(i, count);
            count = 0;
        }

        return res;
    }
}