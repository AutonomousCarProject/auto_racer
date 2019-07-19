package org.avphs.driving;

public class Curve {
    float[] center;
    short radius;
    public Curve(float x, float y, short radius){
        float[] temp = new float[2];
        temp[0] = x;
        temp[1] = y;
        this.radius = radius;
        this.center = temp;
    }

    public float[] getCenter(){
        return center;
    }

    public short getRadius(){
        return radius;
    }
}
