package org.avphs.racingline;

public class CurvePoint {
    private int x,y;
    private CurvePoint next;
    private CurvePoint previous;
    private float pdx,pdy;
    private float ndx,ndy;
    private float tdx,tdy;

    //region Constructors
    public CurvePoint(int a, int b) {
        x = a;
        y = b;
    }
    //endregion

    //region Getters/Setters
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public float getTargetX() {
        return tdx;
    }
    public float getTargetY() {
        return tdy;
    }
    public CurvePoint getNext() {
        return next;
    }
    public CurvePoint getPrevious() {
        return previous;
    }
    public void setX(int a) {
        x = a;
    }
    public void setY(int a) {
        y = a;
    }
    public void setNext(CurvePoint a) {
        next = a;
    }
    public void setPrevious(CurvePoint a) {
        previous = a;
    }
    //endregion

    /**
     * Initializes values for CurvePoint.
     * Should only be called after previous and next have been set.
     */
    public void start() {
        pdx = x - previous.getX();
        pdy = y - previous.getY();
        ndx = next.getX() - x;
        ndy = next.getY() - y;
        float[] pdn = normalise(pdx,pdy);
        float[] ndn = normalise(ndx,ndy);
        tdx = (pdn[0]+ndn[0])/2;
        tdy = (pdn[1]+ndn[1])/2;
        float[] tdn = normalise(tdx,tdy);
        tdx = tdn[0];
        tdy = tdn[1];
    }

    private float magnitude(float a, float b) {
        return (float)Math.sqrt(a*a+b*b);
    }
    private float[] normalise(float a, float b) {
        float magn = magnitude(a,b);
        return new float[] {a/magn,b/magn};
    }

    /**
     * Converts this CurvePoint to a RacingLinePoint.
     */
    public RacingLinePoint toRacingLinePoint() {
        return new RacingLinePoint(x,y);
    }
}
