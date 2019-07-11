package org.avphs.racingline;

import java.util.*;
import java.lang.Math;


import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

import java.util.*;


public class RacingLineModule implements CarModule {
    private ArrayList<Point> outerWall = new ArrayList<Point>();
    private ArrayList<Point> innerWall = new ArrayList<Point>();
    private RacingLine center = new RacingLine();
    private RacingLine bezierCurveLine = new RacingLine();
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
        WindowCurve bezierCurve = new WindowCurve(center);
        bezierCurveLine = new RacingLine(bezierCurve.getPoints());
    }

    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     * //@see void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        if (bezierCurveLine == null) {
            System.out.println("Warning: Racing line has not yet been created. To create a racing line, run getMiddleLine");
        }
        return bezierCurveLine;
    }

    //region Middle Line
    private void getMiddleLine() {
        getWalls();
        calcMiddleLine();
        center.sortPoints();
        //deletePoints();
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
                        states.add(new Point(tx, ty));
                        visited[tx][ty] = true;
                    }
                }
            }
        }
    }

    public static int floor(int num, int den) {
        return (int) -Math.ceil((double) -num / den);
    }

    public static int ceiling(int num, int den) {
        return (int) Math.ceil((double) num / den);
    }

    public int intersect(Point p1, Point p2) {
        int num = 0;
        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;
        boolean horizontal;
        horizontal = (Math.abs(y2 - y1) <= Math.abs(x2 - x1));
        if ((horizontal && x1 > x2) || (!horizontal && y1 > y2)) {
            int swap = x2;
            x2 = x1;
            x1 = swap;
            swap = y2;
            y2 = y1;
            y1 = swap;
        }
        if (horizontal) {
            for (int j = x1; j <= x2; j++) {
                int xstep = j - x1;
                int ystep = (y2 - y1) * xstep;
                int ya = floor(ystep, x2 - x1) + y1;
                int yb = ceiling(ystep, x2 - x1) + y1;
                if (j >= 0 && ya >= 0 && j < rows && ya < columns) {
                    if (!map[j][ya]) {
                        num++;
                    }
                }
                if (j >= 0 && yb >= 0 && j < rows && yb < columns) {
                    if (!map[j][yb]) {
                        num++;
                    }
                }
                if (num > 5)
                    return 6;
            }
        } else {
            for (int j = y1; j <= y2; j++) {
                int ystep = j - y1;
                int xstep = (x2 - x1) * ystep;
                int xa = floor(xstep, y2 - y1) + x1;
                int xb = ceiling(xstep, y2 - y2) + x1;
                if (j >= 0 && xa >= 0 && j < rows && xa < columns) {
                    if (!map[xa][j])
                        num++;
                }
                if (j >= 0 && xb >= 0 && j < rows && xb < columns) {
                    if (!map[xa][j])
                        num++;
                }
                if (num > 5)
                    return 6;
            }
        }
        return num;
    }

    private void deletePoints() {
        RacingLinePoint[] RacingLinePoints = center.getRacingLinePoints();
        RacingLinePoint p1 = RacingLinePoints[0];
        ArrayList<RacingLinePoint> compressedLine = new ArrayList<RacingLinePoint>();
        compressedLine.add(RacingLinePoints[0]);
        int index = 0;
        for (int i = 1; i < RacingLinePoints.length; i++) {
            RacingLinePoint p2 = RacingLinePoints[i];
            Point q1 = new Point(Math.round(p1.getX()), Math.round(p1.getY()));
            Point q2 = new Point(Math.round(p2.getX()), Math.round(p2.getY()));
            int result = intersect(q1, q2);
            if (result > 0 || index >= 30) {
                System.out.println("HOLA " + i + " " + result);
                p1 = RacingLinePoints[i - 1];
                compressedLine.add(RacingLinePoints[i - 1]);
                i--;
                index = 0;
            }
            index++;
        }
        center.setRacingLinePointsList(compressedLine);
    }

    private void calcMiddleLine() {
        ArrayList<Point> longer = outerWall.size() > innerWall.size() ? outerWall : innerWall;
        ArrayList<Point> shorter = outerWall.size() <= innerWall.size() ? outerWall : innerWall;
        for (int i = 0; i < longer.size(); i++) {
            int closePoint = -1;
            float dist = rows + columns;
            for (int j = 0; j < shorter.size(); j++) {
                float testDist = distanceBetweenPoints(longer.get(i), shorter.get(j));
                if (testDist < dist && intersect(longer.get(i), shorter.get(j)) <= 5) {
                    closePoint = j;
                    dist = testDist;
                }
            }
            if (closePoint >= 0) {
                center.addPoint(midPoint(longer.get(i), shorter.get(closePoint)));
            }
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

//--------------------------------------------
class WindowCurve {

    private ArrayList<Curve> curves = new ArrayList<Curve>();
    private Curve curveCurrentObj;
    private ArrayList<RacingLinePoint> allPoints = new ArrayList<RacingLinePoint>();

    public ArrayList<RacingLinePoint> getPoints() {
        return allPoints;
    }

    public WindowCurve(float[][] middlePoints) {
        generateCurves(middlePoints);
    }

    public WindowCurve(RacingLine line) {
        RacingLinePoint[] middleLine = line.getRacingLinePoints();
        float[][] middlePoints = new float[middleLine.length][2];
        for (int i = 0; i < middleLine.length; i++) {
            RacingLinePoint currentPoint = middleLine[i];
            middlePoints[i][0] = currentPoint.getX();
            middlePoints[i][1] = currentPoint.getY();
        }
        generateCurves(middlePoints);
    }

    private void generateCurves(float[][] middlePoints) {
        startLine(middlePoints);
        for (Curve curveObj : curves) {
            curveObj.StartBezierCurve();
        }
        for (Curve c : curves) {
            allPoints.addAll(c.allPoints);
        }
    }

    private void startLine(float[][] middlePoints) {
        int tickA = 0;
        int tickB = 0;
        while (tickA + 3 < middlePoints.length) {
            curveCurrentObj = new Curve();
            curveCurrentObj.setP1(new RacingLinePoint(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP2(new RacingLinePoint(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP3(new RacingLinePoint(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            tickA++;
            curveCurrentObj.setP4(new RacingLinePoint(middlePoints[tickA][tickB], middlePoints[tickA][tickB + 1]));
            curves.add(curveCurrentObj);
        }
    }

}

class Curve {

    private RacingLinePoint p1;
    private RacingLinePoint p2;
    private RacingLinePoint p3;
    private RacingLinePoint p4;
    private double[] H = {2, 1, -2, 1, -3, -2, 3, -1, 0, 1, 0, 0, 1, 0, 0, 0};
    public ArrayList<RacingLinePoint> allPoints = new ArrayList<RacingLinePoint>();

    public Curve() {
    }

    public void setP1(RacingLinePoint p1) {
        this.p1 = p1;
    }

    public void setP2(RacingLinePoint p2) {
        this.p2 = p2;
    }

    public void setP3(RacingLinePoint p3) {
        this.p3 = p3;
    }

    public void setP4(RacingLinePoint p4) {
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

    void DrawHermiteCurve(RacingLinePoint P0, RacingLinePoint T0, RacingLinePoint P1, RacingLinePoint T1,
                          int numpoints) {

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
            float x = (float) xcoeff.DotProduct(vt);
            float y = (float) ycoeff.DotProduct(vt);
            allPoints.add(new RacingLinePoint(x, y));
        }
    }

    void DrawBezierCurve(RacingLinePoint P0, RacingLinePoint P1, RacingLinePoint P2, RacingLinePoint P3,
                         int numpoints) {

        RacingLinePoint T0 = new RacingLinePoint(3 * (P1.getX() - P0.getX()), 3 * (P1.getY() - P0.getY()));
        RacingLinePoint T1 = new RacingLinePoint(3 * (P3.getX() - P2.getX()), 3 * (P3.getY() - P2.getY()));
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
