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

    /**Calculates the Vector Cross Product based on the direction the line is trending in, based on the current point
     * Used to set the angle between the 3 points as positive or negative
     *
     * @param last The previous point in the line
     * @param next The next point in the line
     * @return +/-1 based on the Cross product of the three points
     */
    int crossProductN(RacingLinePoint last, RacingLinePoint next) {
        //Convert the points to Vectors
        float[] vectA = new float[]{getX() - last.getX(), getY() - last.getY()};
        float[] vectB = new float[]{next.getX() - getX(), next.getY() - getY()};

        //Calculate the cross product, and cast to a +/-1 integer
        return (int) Math.signum(vectA[0] * vectB[1] - vectA[1] * vectB[0]);
    }

    /**Returns the distance to another RacingLinePoint (Pythagorean Theorem)
     *
     * @param other
     * @return
     */
    public float distanceToPoint(RacingLinePoint other) {
        float diffx = x - other.x;
        float diffy = y - other.y;
        return (float) Math.sqrt(diffx * diffx + diffy * diffy);
    }

    /**
     * Returns a calculated point that is a specified distance towards another point
     * <p>
     * Will return null if other param is the same as the current RacingLinePoint
     *
     * @param other    Give the direction for the calculation
     * @param distance Distance in map pixels in the given direction
     * @return Return new RacingLinePoint at calculated distance
     */
    public RacingLinePoint distanceTowardsPoint(RacingLinePoint other, float distance) {
        //Check the the points are not the same
        if (other.equals(this)) {
            System.out.println("RacingLinePoint.distanceTowardsPoint: [@param]other is equal to current point");
            return null;
        }

        //Determine the direction to move the new point in
        float diffx = other.x - x;
        float diffy = other.y - y;
        int dir = diffx < 0 ? -1 : 1;
        //Get the angle
        float ang = (float) (Math.atan(diffy / diffx));

        //Calculate the new point based on the distance given by the parameter
        float newX = x + (float) (distance * Math.cos(ang) * dir);
        float newY = y + (float) (distance * Math.sin(ang) * dir);

        //Create a RacingLinePoint based on the calculated values
        return new RacingLinePoint(newX, newY);
    }

    /**Convert the Point to a curve point
     *
     * @return
     */
    public CurvePoint toCurvePoint() {
        if(!pass) return new CurvePoint((int)x,(int)y);
        else return new CurvePoint((int)passx,(int)passy);
    }

    /**Returns an (x, y) string of the points coordinates
     *
     * @return
     */
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
