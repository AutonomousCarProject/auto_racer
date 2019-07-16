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
    private RacingLineCurve[] RacingLineCurves;

    //region Constructors
    public RacingLine() {

    }
    public RacingLine(ArrayList<RacingLinePoint> pointsList) {
        RacingLinePointsList = pointsList;
    }
    //endregion

    //region Getters/Setters
    public RacingLinePoint[] getRacingLinePoints() {
        if (RacingLinePoints == null) {
            RacingLinePoints = new RacingLinePoint[RacingLinePointsList.size()];
            for (int i = 0; i < RacingLinePointsList.size(); i++) {
                RacingLinePoints[i] = RacingLinePointsList.get(i);
            }
        }
        return RacingLinePoints;
    }
    public RacingLineCurve[] getRacingLineCurves() {
        if (RacingLineCurves == null) {
            System.out.println("Need to create RacingLineCurve before using it");
        }
        return RacingLineCurves;
    }
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

    //region Find Curves
    public void FindCurves(float minStraightAngle, float maxDistance) {
        ArrayList<RacingLinePoint> potentialCurves = FindPotentialCurves(minStraightAngle);
        ArrayList<RacingLineCurve> splitCurves = SplitCurves(potentialCurves, maxDistance);
        RacingLineCurves = CurveListToArray(splitCurves);
    }

    private ArrayList<RacingLinePoint> FindPotentialCurves(float maxStraightAngle) {
        ArrayList<RacingLinePoint> newCurves = new ArrayList<>();

        for(int i = 0; i < RacingLinePoints.length - 1; i++)
        {
            if (RacingLinePoints[i].getDegree() < maxStraightAngle) {
                newCurves.add(RacingLinePoints[i]);
            }
        }
        return newCurves;
    }

    private ArrayList<RacingLineCurve> SplitCurves(ArrayList<RacingLinePoint> potentialCurves, float accuracy) {
        ArrayList<RacingLineCurve> sortedCurves = new ArrayList<>();
        ArrayList<RacingLinePoint> checked = new ArrayList<>();
        RacingLineCurve currCurve;

        for (int p = 0; p < potentialCurves.size(); p++) {
            if (RacingLineModule.ContainsPoint(checked, potentialCurves.get(p))) {
                continue;
            }
            currCurve = new RacingLineCurve();
            currCurve.AddPoint(potentialCurves.get(p));
            for (int p2 = p; p2 < potentialCurves.size(); p2++) {
                if (RacingLineModule.distanceBetweenPoints(potentialCurves.get(p), potentialCurves.get(p2)) < accuracy && !checked.contains(p2) && p != p2) {// && RacingLineModule.intersects() == 0) {
                    checked.add(potentialCurves.get(p2));
                    currCurve.AddPoint(potentialCurves.get(p2));
                }
            }
            sortedCurves.add(currCurve);
        }

        return sortedCurves;
    }
    //endregion

    public static RacingLineCurve[] CurveListToArray(ArrayList<RacingLineCurve> list) {
        RacingLineCurve[] array = new RacingLineCurve[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

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

    //region Calculate Angles
    public void setAngles() {
        if (RacingLinePoints == null) {
            System.out.println("setAngles(): Trying to set the points to a line, but no line has been generated");
            return;
        }
        for (int i = 0; i < RacingLinePoints.length; i++) {
            int prevPoint = i == 0 ? RacingLinePoints.length - 1 : i - 1;
            int nextPoint = i + 1 == RacingLinePoints.length ? 0 : i + 1;
            RacingLinePoints[i].setDegree(threePointAngle(RacingLinePoints[prevPoint], RacingLinePoints[i], RacingLinePoints[nextPoint]));
        }
    }
    static float lengthSquare(RacingLinePoint p1, RacingLinePoint p2) {
        float xDiff = p1.getX()- p2.getX();
        float yDiff = p1.getY()- p2.getY();
        return xDiff*xDiff + yDiff*yDiff;
    }
    public static float threePointAngle( RacingLinePoint A, RacingLinePoint B, RacingLinePoint C ) {
        /** Three Point angle takes in 3 RacingLinePoints and calculates the angle with B as the "middle" point using law of cosines
         * This should always return the largest possible angle. Hopefully this does not break
         * @FIXME THIS NEEDS TO BE CALLED SOMEWHERE
         */
            // Square of lengths are a2, b2, c2
            float a2 = lengthSquare(B,C);
            float b2 = lengthSquare(A,C);
            float c2 = lengthSquare(A,B);

            // length of sides be a, b, c
            float a = (float)sqrt(a2);
            float b = (float)sqrt(b2);
            float c = (float)sqrt(c2);
            // From Cosine law
//          float alpha = (float) acos((b2 + c2 - a2)/(2*b*c));
            float betta = (float) acos((a2 + c2 - b2)/(2*a*c));
//          float gamma = (float) acos((a2 + b2 - c2)/(2*a*b));

            // Converting to degree
//          alpha = (float) (alpha * 180 / PI);
            betta = (float) (betta * 180 / PI);
//          gamma = (float) (gamma * 180 / PI);
            return betta;
            // Has the other angles in the supposed triangle but for our sake we are always using the middle
            // Returns largest possible angle always
        }
    //endregion

    public float rateLine(RacingLinePoint [] allPoint) {
        /** Pass in an array of all the racingline and this method will rate the line using a simple metric
         * of the average angle divided by the total distance. This could be used to compare racing lines by brute forcing
         * dynamically or simply by comparing methods
         */

        float averageAngle = 0;
        float totalDistance = 0;
        for(int i = 0; i <= allPoint.length; i++) {
            averageAngle += allPoint[i].getDegree();
            totalDistance += lengthSquare(allPoint[i], allPoint[i+1]);
        }
        averageAngle = averageAngle / allPoint.length;
        return totalDistance/averageAngle;
    }

    public RacingLinePoint getApex(ArrayList <RacingLinePoint> RacingLinecurve ){
        /**
         * This method produces the smallest point in a given curve which will then be used to generate the constraints.
         */
        RacingLinePoint smallestDig = RacingLinecurve.get(0);
        for (int i = 0; i < RacingLinecurve.size(); i++) {
            if (RacingLinecurve.get(i).getDegree()< smallestDig.getDegree()) smallestDig = RacingLinecurve.get(i);
        }
            return smallestDig;

    }
}

class RacingLineCurve{
    public ArrayList<RacingLinePoint> curve;

    public RacingLineCurve() {}
    public RacingLineCurve(ArrayList<RacingLinePoint> _curve) {
        curve = _curve;
    }
    public RacingLineCurve(RacingLinePoint[] _curve) {
        curve = new ArrayList<RacingLinePoint>(Arrays.asList(_curve));
    }

    public RacingLinePoint[] getCurve() {
        return curve.toArray(RacingLinePoint[]::new);
    }

    public void AddPoint(RacingLinePoint newPoint) {
        curve.add(newPoint);
    }
}
