package org.avphs.detection;

public class Obstacle {
    Point[] corners;

    //MIGHT NOT USE
    //vector for speed

    float dy;
    float dx;

    public Point[] getClosestCorners(){
        //TODO: make this function lol
        return new Point[]{new Point(), new Point()};
    }

    public Point[] getCorners() {
        return corners;
    } // Bounding Cox Controller getter

    public void setCorners(Point[] corners) {
        this.corners = corners;
    } // Bounding Box Controller setter

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

}
