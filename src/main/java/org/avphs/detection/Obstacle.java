package org.avphs.detection;

public class Obstacle {
    Point[] bbc;
    float x, y;//in map, leftmost point
    //MIGHT NOT USE
    float speed; //scalar
    float direction; //in relative to north on the map



    public Point[] getBBC() {
        return bbc;
    } // Bounding Cox Controller getter

    public void setBbc(Point[] bbc) {
        this.bbc = bbc;
    } // Bounding Box Controller setter

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
