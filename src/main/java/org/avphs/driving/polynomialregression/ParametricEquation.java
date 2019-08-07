package org.avphs.driving.polynomialregression;

public class ParametricEquation {

    private PolynomialEquation x;
    private PolynomialEquation y;



    public ParametricEquation(PolynomialEquation x, PolynomialEquation y) {
        this.x = x;
        this.y = y;
    }

    //double getCurvatureAtT(int t) {

    //}

    double getXValueAtT(int t) {
        return x.getYValue(t);
    }

    double getYValueAtT(int t) {
        return y.getYValue(t);
    }
}
