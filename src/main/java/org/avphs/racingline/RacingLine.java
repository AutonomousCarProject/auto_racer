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
    public void FindCurves(float minStraightAngle, float maxDistance, int minCurvePoints) {
        ArrayList<RacingLinePoint> potentialCurves = FindPotentialCurves(minStraightAngle);
        ArrayList<RacingLineCurve> splitCurves = SplitCurves(potentialCurves, maxDistance);
        ArrayList<RacingLineCurve> refinedCurves = RefineCurves(splitCurves, minCurvePoints);
        CurveCalculations(refinedCurves);

        RacingLineCurves = CurveListToArray(refinedCurves);
    }

    private ArrayList<RacingLinePoint> FindPotentialCurves(float maxStraightAngle) {
        ArrayList<RacingLinePoint> newCurves = new ArrayList<>();

        for(int i = 0; i < RacingLinePoints.length - 1; i++)
        {
            if (Math.abs(RacingLinePoints[i].getDegree()) < maxStraightAngle) {
                newCurves.add(RacingLinePoints[i]);
            }
        }
        return newCurves;
    }

    private ArrayList<RacingLineCurve> SplitCurves(ArrayList<RacingLinePoint> potentialCurves, float accuracy) {
        ArrayList<RacingLineCurve> sortedCurves = new ArrayList<>();
        ArrayList<RacingLinePoint> checked = new ArrayList<>();
        RacingLineCurve currCurve = new RacingLineCurve();
        RacingLinePoint lastPoint = potentialCurves.get(0);

        //Make sure the first data point isn't in the middle of a curve
        for (int p = potentialCurves.size() - 1; p > 0; p--) {
            if (RacingLineModule.distanceBetweenPoints(lastPoint, potentialCurves.get(p)) > accuracy) {
                break;
            }
            checked.add(lastPoint);
            currCurve.AddPoint(potentialCurves.get(p));
            lastPoint = potentialCurves.get(p);
        }
        currCurve.ReverseCurve();
        lastPoint = potentialCurves.get(0);
        for (int p = 1; p < potentialCurves.size(); p++) {
            if (checked.contains(potentialCurves.get(p))) {
                continue;
            }
            if (RacingLineModule.distanceBetweenPoints(lastPoint, potentialCurves.get(p)) > accuracy) {
                sortedCurves.add(currCurve);
                currCurve = new RacingLineCurve();
            }
            currCurve.AddPoint(potentialCurves.get(p));
            lastPoint = potentialCurves.get(p);
            checked.add((potentialCurves.get(p)));
        }

        return sortedCurves;
    }

    private ArrayList<RacingLineCurve> RefineCurves(ArrayList<RacingLineCurve> curves, int minCurvePoints) {
        ArrayList<RacingLineCurve> refinedCurves = new ArrayList<>();

        for (RacingLineCurve c: curves) {
            if (c.getCurve().length < minCurvePoints) {
                continue;
            }
            c.setEndPoints();
            int start = GetPointIndex(c.getCurveStart());
            int end = GetPointIndex(c.getCurveEnd());
            RacingLineCurve newCurve = new RacingLineCurve();

            if (start > end) {
                for (int p = start; p < RacingLinePointsList.size(); p++) {
                    newCurve.AddPoint(RacingLinePointsList.get(p));
                }
                newCurve.ReverseCurve();
                start = 0;
            }

            for (int p = start; p < end + 1; p++) {
                newCurve.AddPoint(RacingLinePointsList.get(p));
            }
            refinedCurves.add(newCurve);
        }

        return refinedCurves;
    }

    private void CurveCalculations(ArrayList<RacingLineCurve> curves) {
        for (RacingLineCurve rc: curves) {
            rc.setEndPoints();
            rc.setMidPoints();
            rc.setCurveDir();
            rc.setCurveConstraint();
            rc.CalculateBoundedBezier();
        }
    }
    //endregion

    public int GetPointIndex(RacingLinePoint p) {
        for (int i = 0; i < RacingLinePointsList.size(); i++) {
            if (RacingLinePointsList.get(i).equals(p)) {
                return i;
            }
        }
        return -1;
    }

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
        betta = Float.isNaN(betta) ? 180 : betta;
        betta =  betta == 0.0 ? 180 : betta;

        float cp = B.crossProductN(A, C);

        return betta * (cp != 0 ? cp : 1);
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
        RacingLinePoint smallestDeg = RacingLinecurve.get(0);
        for (int i = 0; i < RacingLinecurve.size(); i++) {
            if (RacingLinecurve.get(i).getDegree()< smallestDeg.getDegree()) smallestDeg = RacingLinecurve.get(i);
        }
        return smallestDeg;
    }
}

enum curveDirs { NotSet, Clockwise, Counterclockwise }
class RacingLineCurve{
    private ArrayList<RacingLinePoint> curve;
    private RacingLinePoint curveStart, curveEnd, curveMid, curveApex, curveConstraint;
    private curveDirs curveDir = curveDirs.NotSet;

    //region Constructors
    public RacingLineCurve() { curve = new ArrayList<>(); }
    public RacingLineCurve(ArrayList<RacingLinePoint> _curve) {
        curve = _curve;
    }
    public RacingLineCurve(RacingLinePoint[] _curve) {
        curve = new ArrayList<RacingLinePoint>(Arrays.asList(_curve));
    }
    //endregion

    //region Getters/Setter
    public RacingLinePoint[] getCurve() {
        return RacingLineModule.RacingLinePointListToArray(curve);
    }
    public RacingLinePoint getCurveStart() {
        return curveStart;
    }
    public RacingLinePoint getCurveEnd() {
        return curveEnd;
    }
    public RacingLinePoint getCurveMid() {
        return curveMid;
    }
    public RacingLinePoint getCurveApex() {
        return curveApex;
    }
    public RacingLinePoint getCurveConstraint() {
        return curveConstraint;
    }
    public curveDirs getCurveDir() {
        return curveDir;
    }

    public void setCurve(ArrayList<RacingLinePoint> curve) {
        this.curve = curve;
        setEndPoints();
        setMidPoints();
    }
    public void setCurve(RacingLinePoint[] _curve) {
        ArrayList<RacingLinePoint> newCurve = new ArrayList<>();
        for(RacingLinePoint p: _curve) {
            newCurve.add(p);
        }
        setCurve(newCurve);
    }
    public void setEndPoints() {
        curveStart = curve.get(0);
        curveEnd = curve.get(curve.size() - 1);
    }
    public void setMidPoints() {
        if (curveStart == null || curveEnd == null) { setEndPoints(); }
        int closestPoint = 0;
        int steepestPoint = 0;
        float closestDiff = Float.MAX_VALUE;
        float steepestAngle = Float.MAX_VALUE;

        for (int p = 1; p < curve.size() - 1; p++) {
            float newDiff = Math.abs(RacingLineModule.distanceBetweenPoints(curveStart, curve.get(p)) - RacingLineModule.distanceBetweenPoints(curveEnd, curve.get(p)));
            if (newDiff < closestDiff) {
                closestDiff = newDiff;
                closestPoint = p;
            }

            if (curve.get(p).getDegree() < steepestAngle) {
                steepestPoint = p;
                steepestAngle = curve.get(p).getDegree();
            }
        }
        //System.out.println(steepestPoint + " " + steepestAngle);
        curveMid = curve.get(closestPoint);
        curveApex = curve.get(steepestPoint);
    }
    public void setCurveConstraint() {
        /**
         * This will be taking the apex of a given curve in the middle line and then finding the corresponding point on the wall
         */
        this.curveConstraint = curveConstraint;

        if (curveDir == curveDirs.NotSet) {
            System.out.println("No Curve Dir set; calling setCurveDir()");
            setCurveDir();
        }

        Point apexPoint = new Point(curveApex.getIntX(), curveApex.getIntY());
        Point tempConstraint;
        if (curveDir == curveDirs.Clockwise) {
            tempConstraint = RacingLineModule.ClosestPoint(RacingLineModule.PointListToArray(RacingLineModule.getOuterWall()), apexPoint);
        } else {
            tempConstraint = RacingLineModule.ClosestPoint(RacingLineModule.PointListToArray(RacingLineModule.getInnerWall()), apexPoint);
        }

        curveConstraint = new RacingLinePoint(tempConstraint.x, tempConstraint.y);
    }
    public void setCurveDir() {
        int cc = 0;
        int c = 0;
        for(RacingLinePoint p: curve) {
            if (p.getDegree() == 180) {
                continue;
            }
            if (p.getDegree() < 0) {
                cc++;
            } else {
                c++;
            }
        }

        if (c > cc) {
            curveDir = curveDirs.Clockwise;
        } else {
            curveDir = curveDirs.Counterclockwise;
        }
    }
    //endregion

    public void ReverseCurve() {
        ArrayList<RacingLinePoint>  revCurve = new ArrayList<>();

        for(int i = curve.size() - 1; i >= 0; i--) {
            revCurve.add(curve.get(i));
        }
        curve = revCurve;
    }

    public void AddPoint(RacingLinePoint newPoint) {
        curve.add(newPoint);
    }

    public void CalculateBoundedBezier() {
        RacingLinePoint[] newCurve;
        curve = new ArrayList<>();
        newCurve = RacingLineModule.boundedBezier(curveStart, curveEnd, curveMid, curveConstraint, true).getCurve();
        for(RacingLinePoint p: newCurve) {
            curve.add(p);
        }
    }

    public void CalculatePoints() {
        setEndPoints();
        setMidPoints();
        setCurveDir();
        setCurveConstraint();
        CalculateBoundedBezier();
    }

    public String getCurvePrint() {
        String points = "";
        for (RacingLinePoint p: curve) {
            points += p.getCoords() + ", ";
        }
        return points;
    }
    public String getCurvePointsPrint() {
        return "\nStart: " + curveStart.getCoords() + ",  End: " + curveEnd.getCoords() + ",  Mid: " + curveMid.getCoords() + ",  Constraint: " + curveConstraint.getCoords();
    }
}