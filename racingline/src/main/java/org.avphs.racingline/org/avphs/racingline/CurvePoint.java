package org.avphs.racingline;

public class CurvePoint {
    int x,y;
    CurvePoint next;
    CurvePoint previous;
    float pdx,pdy;
    float ndx,ndy;
    float tdx,tdy;
    public CurvePoint(int a, int b) {
        x = a;
        y = b;
    }
    public void start() {
        pdx = x - previous.x;
        pdy = y - previous.y;
        ndx = next.x - x;
        ndy = next.y - y;
        float[] pdn = normalise(pdx,pdy);
        float[] ndn = normalise(ndx,ndy);
        tdx = (pdn[0]+ndn[0])/2;
        tdy = (pdn[1]+ndn[1])/2;
        float[] tdn = normalise(tdx,tdy);
        tdx = tdn[0];
        tdy = tdn[1];
        //test
    }
    public float magnitude(float a, float b) {
        return (float)Math.sqrt(a*a+b*b);
    }
    public float[] normalise(float a, float b) {
        float magn = magnitude(a,b);
        return new float[] {a/magn,b/magn};
    }
}
