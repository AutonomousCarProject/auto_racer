package org.avphs.driving.polynomialregression;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolynomialRegression extends PolynomialEquation {

    private int degree;
    private List<Point> dataPoints;
    private Map<Variable, BigDecimal> mapSums;
    private Matrix valueMatrix;
    private Matrix resultMatrix;

    public PolynomialRegression(ArrayList<Point> dataPoints, int degree) {
        super(degree + 1);
        if (dataPoints.size() < degree) {
            throw new RuntimeException("Not enough points to have a polynomial of degree: " + dataPoints.size());
        }
        this.dataPoints = dataPoints;
        this.degree = degree;
        mapSums = new HashMap<>();

        regressPoints();
    }

    private void regressPoints() {
        init();

        BigDecimal[][] coefficients = valueMatrix.getInverse2().getMultiply(resultMatrix).reduceScale().getMatrix();
        for (int idx = 0; idx < coefficients.length; idx++) {
            terms[idx] = coefficients[idx][0];
        }
    }

    private void init() {
        // Calculate x/xy sums
        for (int power = 0; power <= 2 * degree; power++) {
            calculatePowSum(power);
        }

        initValues();
    }

    private void initValues() {
        ArrayList<ArrayList<BigDecimal>> valueMatrix = new ArrayList<>();
        ArrayList<ArrayList<BigDecimal>> resultMatrix = new ArrayList<>();
        for (int row = 0; row <= degree; row++) {
            ArrayList<BigDecimal> rowValues = new ArrayList<>();
            for (int column = 0; column <= degree; column++) {
                rowValues.add(mapSums.get(new Variable("x", row + column)).setScale(Matrix.scale, RoundingMode.HALF_UP));
            }
            ArrayList<BigDecimal> result = new ArrayList<>();
            result.add(mapSums.get(new Variable("xy", row)).setScale(Matrix.scale, RoundingMode.HALF_UP));
            resultMatrix.add(result);
            valueMatrix.add(rowValues);
        }
        this.valueMatrix = new Matrix(valueMatrix);
        this.resultMatrix = new Matrix(resultMatrix);
    }

    private void calculatePowSum(int power) {
        BigDecimal xSum = new BigDecimal("0").setScale(Matrix.scale, RoundingMode.HALF_UP);
        BigDecimal ySum = new BigDecimal("0").setScale(Matrix.scale, RoundingMode.HALF_UP);
        boolean calcYValues = power <= degree;

        for (Point point : dataPoints) {
            BigDecimal powX1 = new BigDecimal(Integer.toString(point.x)).pow(power).setScale(Matrix.scale, RoundingMode.HALF_UP);
            xSum = xSum.add(powX1);
            if (calcYValues) {
                ySum = ySum.add(new BigDecimal(Integer.toString(point.y)).multiply(powX1).setScale(Matrix.scale, RoundingMode.HALF_UP));
            }
        }
        mapSums.put(new Variable("x", power), xSum);
        if (calcYValues) {
            mapSums.put(new Variable("xy", power), ySum);
        }
    }
}
