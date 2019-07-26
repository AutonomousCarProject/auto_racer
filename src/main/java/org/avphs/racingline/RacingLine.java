package org.avphs.racingline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static java.lang.Math.*;
import static java.lang.Math.PI;

/**
 * <p>This class represents a racing line. It contains an array of points which represent the line.</p>
 *
 * @see RacingLinePoint
 */
public class RacingLine {
    private ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
    private RacingLinePoint[] RacingLinePoints;
    //private RacingLineCurve[] RacingLineCurves;

    //region Constructors
    public RacingLine() {

    }
    public RacingLine(ArrayList<RacingLinePoint> pointsList) {
        RacingLinePointsList = pointsList;
    }
    //endregion

    //region Getters/Setters
    /**
     * Returns an array of RacingLinePoint that makes up the racing line.
     */
    public RacingLinePoint[] getRacingLinePoints() {
        if (RacingLinePoints == null) {
            RacingLinePoints = new RacingLinePoint[RacingLinePointsList.size()];
            for (int i = 0; i < RacingLinePointsList.size(); i++) {
                RacingLinePoints[i] = RacingLinePointsList.get(i);
            }
        }
        return RacingLinePoints;
    }
    /**
     * Returns an ArrayList of RacingLinePoint that makes up the racing line.
     */
    public ArrayList<RacingLinePoint> getRacingLinePointsList() {
        return RacingLinePointsList;
    }

    public void setRacingLinePointsList(ArrayList<RacingLinePoint> newLine) {
        RacingLinePointsList = newLine;
        RacingLinePoints = null;
    }
    public void addPoint(RacingLinePoint newPoint) {
        RacingLinePointsList.add(newPoint);
    }
    //endregion
    /**
     * Sorts the racing line.
     */
    public void sortPoints() {
        ArrayList<RacingLinePoint> orderedRacingLine = new ArrayList<RacingLinePoint>();
        HashSet<RacingLinePoint> remainingPoints = new HashSet<RacingLinePoint>(RacingLinePointsList);
        orderedRacingLine.add(RacingLinePointsList.get(0));
        remainingPoints.remove(RacingLinePointsList.get(0));
        while (remainingPoints.size() > 0) {
            RacingLinePoint currentPoint = orderedRacingLine.get(orderedRacingLine.size() - 1);
            RacingLinePoint closestPoint = new RacingLinePoint();
            float minDist = Float.MAX_VALUE;
            for (RacingLinePoint point : remainingPoints) {
                float tempDist = currentPoint.distanceToPoint(point);
                if (tempDist < minDist) {
                    minDist = tempDist;
                    closestPoint = point;
                }
            }
            orderedRacingLine.add(closestPoint);
            remainingPoints.remove(closestPoint);
        }
        RacingLinePointsList = orderedRacingLine;
    }
}