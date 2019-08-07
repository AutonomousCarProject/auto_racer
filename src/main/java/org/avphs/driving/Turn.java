package org.avphs.driving;

public class Turn {

    private int radius;
    private int degree;

    public Turn(int radius, int degree) {
        this.radius = radius;
        this.degree = degree;
    }

    public int getRadius() {
        return radius;
    }

    public int getDegree() {
        return degree;
    }
}
