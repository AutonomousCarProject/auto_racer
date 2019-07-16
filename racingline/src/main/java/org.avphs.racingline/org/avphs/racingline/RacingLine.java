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

    public RacingLine() {

    }

    public RacingLine(ArrayList<RacingLinePoint> pointsList) {
        RacingLinePointsList = pointsList;
    }

    public RacingLinePoint[] getRacingLinePoints() {
        if (RacingLinePoints == null) {
            RacingLinePoints = new RacingLinePoint[RacingLinePointsList.size()];
            for (int i = 0; i < RacingLinePointsList.size(); i++) {
                RacingLinePoints[i] = RacingLinePointsList.get(i);
            }
        }
        return RacingLinePoints;
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
