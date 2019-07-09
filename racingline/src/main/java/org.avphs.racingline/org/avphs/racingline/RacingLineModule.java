package org.avphs.racingline;

import org.java.ArrayList;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import RacingLinePoint;

public class RacingLineModule implements CarModule {
    private RacingLine racingLine;

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
     * @see getRacingLine
     */
    public void makeRacingLine(boolean[][] map) {
        System.out.println("RacingLine.makeRacingLine not implemented");
    }

    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     * @see makeRacingLine
     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
    }
}

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