package org.avphs.driving;

public class Calculator {

    public static float[] findClosestPoint(float x, float y, float m, float b){
        float inverse = 1/m;
        float c = x * inverse + y;
        float x2 = (c-b)/(m+inverse);
        return new float[]{x2, m*x2+b};
    }

    public static float findStraightDistance(float x, float y, float[] point, float m){
        float[] closestPoint = findClosestPoint(x,y,m,(-m*point[0])+point[1]);
        return (float)Math.sqrt(Math.pow(x - closestPoint[0], 2.0) + Math.pow(y - closestPoint[1], 2.0));
    }

    public static float findTurnDistance(float x, float y, float[] center, short r){
        float[] point = new float[2];
        double m = (center[1] - y) / (x - center[0]);
        double b = -2 * center[0];
        double c = center[0] * center[0] - r * r / (m * m + 1);
        double root = Math.sqrt(b * b - 4 * c);

        if (x < center[0]) {
            root = root * (-1);
        }
        point[0] = (float)((-b + root) / 2);
        point[1] = (float)Math.sqrt(Math.pow(x - (point[0]) + r * r, 2)) + center[1];

        if (y < center[1]) {
            point[1] = point[1] * (-1) + center[1] * 2;
        }
        return (float)Math.sqrt(Math.pow(x - point[0], 2.0) + Math.pow(y - point[1], 2.0));
    }

}
