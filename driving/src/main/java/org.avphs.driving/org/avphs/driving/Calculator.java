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

    public static float findTurnDistance(float x, float y, float[] center, short radius){
        return 0;
    }

}
