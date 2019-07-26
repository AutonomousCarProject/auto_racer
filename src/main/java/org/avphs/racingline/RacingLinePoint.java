package org.avphs.racingline;

import java.util.Objects;

public class RacingLinePoint {
    private float x, y, degree;
    private Point outer, inner;
    private boolean pass = false;
    private float passx,passy;
    private boolean original = false;
    //region Constructors
    public RacingLinePoint() {
        setX(0);
        setY(0);
        setDegree(0);
    }

    public RacingLinePoint(float x, float y) {
        setX(x);
        setY(y);
        setDegree(0);
    }

    public RacingLinePoint(float x, float y, float degree) {
        setX(x);
        setY(y);
        setDegree(degree);
    }
    //endregion

    int crossProductN(RacingLinePoint last, RacingLinePoint next) {
        float[] vectA = new float[]{getX() - last.getX(), getY() - last.getY()};
        float[] vectB = new float[]{next.getX() - getX(), next.getY() - getY()};

        return (int) Math.signum(vectA[0] * vectB[1] - vectA[1] * vectB[0]);
    }

    public float distanceToPoint(RacingLinePoint other) {
        float diffx = x - other.x;
        float diffy = y - other.y;
        return (float) Math.sqrt(diffx * diffx + diffy * diffy);
    }

    //region Getters/Setters
    public float getX() {
        return x;
    }
    public int getIntX() {
        return Math.round(x);
    }
    public float getY() {
        return y;
    }
    public int getIntY() {
        return Math.round(y);
    }
    public float getDegree() {
        return degree;
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }
    public void setDegree(float degree) {
        this.degree = degree;
    }
    //endregion

    /**
     * Returns a calculated point that is a specified distance towards another point
     * <p>
     * Will return null if other param is the same as the current RacingLinePoint
     *
     * @param other    Give the direction for the calculation
     * @param distance Distance in map mixels in the given direction
     * @return Return new RacingLinePoint at calculated distance
     */
    public RacingLinePoint distanceTowardsPoint(RacingLinePoint other, float distance) {
        if (other.equals(this)) {
            System.out.println("RacingLinePoint.distanceTowardsPoint: [@param]other is equal to current point");
            return null;
        }
        float diffx = other.x - x;
        float diffy = other.y - y;
        int dir = diffx < 0 ? -1 : 1;
        float ang = (float) (Math.atan(diffy / diffx));
        float newX = x + (float) (distance * Math.cos(ang) * dir);
        float newY = y + (float) (distance * Math.sin(ang) * dir);
        return new RacingLinePoint(newX, newY);
    }

    public CurvePoint toCurvePoint() {
        if(!pass) return new CurvePoint((int)x,(int)y);
        else return new CurvePoint((int)passx,(int)passy);
    }

    public String getCoords() {
        return "(" + x + ", " + y + ")";
    }

    //region overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RacingLinePoint that = (RacingLinePoint) o;
        return Float.compare(that.x, x) == 0 &&
                Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    //endregion

    //region Passing
    public void setOuter(Point a) {
        outer = a;
    }
    public void setInner(Point a) {
        inner = a;
    }
    public Point getOuter() {
        return outer;
    }
    public Point getInner() {
        return inner;
    }
    public void setPass(boolean a) {
        pass = a;
    }
    public void setPassX(float a) {
        passx = a;
    }
    public void setPassY(float a) {
        passy = a;
    }
    public boolean getPass() {
        return pass;
    }
    public float getPassX() {
        return passx;
    }
    public float getPassY() {
        return passy;
    }
    public void setOriginal(boolean a) {
        original = a;
    }
    public boolean getOriginal() {
        return original;
    }
    //endregion
}
