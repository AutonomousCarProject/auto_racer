package org.avphs.racingline;

import java.util.ArrayList;
import java.util.HashSet;

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
//    cos = adj over hype
//    Math.acos adj over hyp
    public void threePointAngle( RacingLinePoint [] allpoint ) {
        int totalsize =  allpoint.length;
        for (int i = 0 ; i < totalsize ; i++) {
         allpoint[i].setDegree ((float)(((Math.atan2(allpoint[i+2].getY() - allpoint[i].getY(), allpoint[i+2].getX() - allpoint[i].getX()) -
                    Math.atan2(allpoint[i+1].getY() - allpoint[i].getY(), allpoint[i+1].getX() - allpoint[i].getX())))));
        }
    }
    public float averageAngle(RacingLinePoint [] allPoint) {
        int allPointLength = allPoint.length;
        int i = 0;
        float averageAngle = 0;

        for(i = 0; i <= allPointLength; i++) {
            averageAngle += allPoint[i].getDegree();
        }

        return averageAngle;
    }
}
