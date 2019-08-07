package org.avphs.driving.polynomialregression;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PolynomialEquation {

    /**
     * The array that holds each coefficient ordered from x^0
     * up to x^n.
     */
    protected BigDecimal[] terms;
    private int numTerms;

    public PolynomialEquation(int numTerms) {
        this.numTerms = numTerms;
        terms = new BigDecimal[numTerms];
    }

    PolynomialEquation(BigDecimal[] terms) {
        this.terms = terms;
        numTerms = terms.length;
    }

    double getYValue(double x) {
        BigDecimal yValue = new BigDecimal("0").setScale(Matrix.scale, RoundingMode.HALF_UP);

        for (int idx = 0; idx < terms.length; idx++) {
            yValue = yValue.add(terms[idx].multiply(new BigDecimal(Double.toString(x)).setScale(Matrix.scale, RoundingMode.HALF_UP).pow(idx)));
        }

        return yValue.doubleValue();
    }

    PolynomialEquation getDerivative(int numberOfDerivative) {
        PolynomialEquation derivative = new PolynomialEquation(numTerms - 1);
        for (int idx = 1; idx < numTerms; idx++) {
            derivative.terms[idx - 1] = terms[idx].multiply(new BigDecimal(idx));
        }
        if (numberOfDerivative - 1 >= 1) {
            derivative = derivative.getDerivative(numberOfDerivative - 1);
        }
        return derivative;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int degree = 0; degree < terms.length; degree++) {
            builder.append(terms[degree].toPlainString());
            builder.append("x^{");
            builder.append(degree);
            builder.append("} + ");
        }

        return builder.toString();
    }
}
