package org.avphs.driving;

public class Calculator {

    public static float[] solveSystem(float x, float y, float m, int b){
        float inverse = 1/m;
        float c = x * inverse + y;
        float x2 = (c-b)/(m+inverse);
        return new float[]{x2, m*x2+b};


    }
}
