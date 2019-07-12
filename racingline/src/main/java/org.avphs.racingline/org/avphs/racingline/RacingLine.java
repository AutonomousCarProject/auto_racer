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
//    @FIXME Make sure to execute at the right time, driving team NEEDS angles to operate. Possible issue exterior vs interior angle
    public void threePointAngle( RacingLinePoint [] allPoint ) {
        int totalsize =  allPoint.length;
        float p1x; float p2x; float p3x; float p1y; float p2y; float p3y;  float p12; float p13; float p23;
        for (int i = 1 ; i < totalsize ; i++) {
            p1x = allPoint[i-1].getX(); p1y=allPoint[i-1].getY(); p2x = allPoint[i].getX(); p2y=allPoint[i].getY(); p3x = allPoint[i+1].getX(); p3y=allPoint[i+1].getY();
            p12 = (float)Math.sqrt(Math.pow((p1x - p2x),2) + Math.pow((p1y - p2y),2));
            p13 = (float) Math.sqrt(Math.pow((p1x - p3x),2) + Math.pow((p1y - p3y),2));
            p23 = (float) Math.sqrt(Math.pow((p2x - p3x),2) + Math.pow((p2y - p3y),2));
            allPoint[i].setDegree((float)(Math.acos(((Math.pow(p12, 2)) + (Math.pow(p13, 2)) - (Math.pow(p23, 2))) / (2 * p12 * p13)) * 180 / Math.PI));
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