package org.avphs.detection;

import java.awt.geom.Point2D;

public class Obstacle {
    Point[] bb;

    float x, y;//in map, leftmost point


    //MIGHT NOT USE
    float speed; //scalar
    float direction; //in relative to north on the map



    public Point[] getBb() {
        return bb;
    }

    public void setBb(Point[] bb) {
        this.bb = bb;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }



}
