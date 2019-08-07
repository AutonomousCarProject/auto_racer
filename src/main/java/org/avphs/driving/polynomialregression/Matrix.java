package org.avphs.driving.polynomialregression;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Matrix {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private BigDecimal[][] matrix;
    private int numColumns;
    private int numRows;
    public static final int scale = 300;
    public static final int finalScale = 200;

    Matrix(ArrayList<ArrayList<BigDecimal>> twoDemArray) {
        numRows = twoDemArray.size();
        numColumns = twoDemArray.get(0).size();
        matrix = new BigDecimal[numRows][numColumns];
        for (int row = 0; row < numRows; row++) {
            BigDecimal[] rowList = new BigDecimal[numColumns];
            for (int col = 0; col < numColumns; col++) {
                rowList[col] = decimalCopy(twoDemArray.get(row).get(col));
            }
            matrix[row] = rowList;
        }
    }

    private Matrix(Matrix matrix) {
        this.numColumns = matrix.getNumColumns();
        this.numRows = matrix.getNumRows();
        this.matrix = new BigDecimal[numRows][numColumns];
        for (int row = 0; row < numRows; row++) {
            BigDecimal[] rowList = new BigDecimal[numColumns];
            for (int col = 0; col < numColumns; col++) {
                rowList[col] = decimalCopy(matrix.getMatrix()[row][col]);
            }
            this.matrix[row] = rowList;
        }
    }

    private Matrix(BigDecimal[][] matrix) {
        this.matrix = matrix;
        numRows = matrix.length;
        numColumns = matrix[0].length;
    }

    void printMatrix() {
        for (BigDecimal[] row : matrix) {
            for (BigDecimal value : row) {
                if (value.doubleValue() == 1) {
                    System.out.printf(ANSI_GREEN + "%20.50f " + ANSI_RESET, value.doubleValue());
                } else if (value.remainder(new BigDecimal("1").setScale(scale, RoundingMode.HALF_UP)).abs().doubleValue() < 0.0000000001) {
                    System.out.printf(ANSI_WHITE + "%20.50f " + ANSI_RESET, value.doubleValue());
                } else {
                    System.out.printf(ANSI_RED + "%20.50f " + ANSI_RESET, value.doubleValue());
                }
            }
            System.out.println();
        }
    }

    Matrix getMultiply(Matrix matrix) {
        if (this.numColumns != matrix.getNumRows()) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        BigDecimal[][] newMatrix = new BigDecimal[matrix.getNumRows()][matrix.getNumColumns()];

        for (int row = 0; row < numRows; row++) {
            Vector rowVector = Matrix.getHorizontalVector(row, this);
            for (int column = 0; column < matrix.getNumColumns(); column++) {
                Vector columnVector = Matrix.getVerticleVector(column, matrix);
                newMatrix[row][column] = rowVector.getDotProduct(columnVector);
            }
        }

        return new Matrix(newMatrix);
    }

    Matrix getInverse2() {
        if (numRows != numColumns) {
            throw new RuntimeException("Matrix is not square.");
        }

        Matrix augmentedMatrix = getIdentityMatrix(numRows);
        Matrix matrixCopy = new Matrix(this);

        for (int row = 0; row < numRows; row++) {
            makeLeadingCoefficientOne(matrixCopy.getMatrix(), augmentedMatrix.getMatrix(), row);
            makeColumnZero(row, matrixCopy.getMatrix(), augmentedMatrix.getMatrix());
        }

        /*this.printMatrix();
        System.out.println("\n-----------------------------------------------------------------------------------------\n");
        (augmentedMatrix).printMatrix();//*/
        //this.getMultiply(augmentedMatrix).printMatrix();//*/

        return augmentedMatrix;
    }

    private void makeColumnZero(int column, BigDecimal[][] matrix, BigDecimal[][] augmentedMatrix) {

        for (int row = 0; row < matrix.length; row++) {
            if (row == column) {
                continue;
            }
            BigDecimal scalar = matrix[row][column];
            BigDecimal[] scaledMatrixRowValues = multiplyEach(getCopyRow(matrix[column]), scalar);
            BigDecimal[] scaledAugmentedMatrixRowValues = multiplyEach(getCopyRow(augmentedMatrix[column]), scalar);
            subtract(matrix[row], scaledMatrixRowValues);
            subtract(augmentedMatrix[row], scaledAugmentedMatrixRowValues);
        }
    }

    private static Matrix getIdentityMatrix(int size) {
        BigDecimal[][] matrixValues = new BigDecimal[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (row == column) {
                    matrixValues[row][column] = new BigDecimal("1").setScale(scale, RoundingMode.HALF_UP);
                } else {
                    matrixValues[row][column] = new BigDecimal("0").setScale(scale, RoundingMode.HALF_UP);
                }
            }
        }
        return new Matrix(matrixValues);
    }

    Matrix reduceScale() {
        BigDecimal[][] newMatrix = new BigDecimal[numRows][numColumns];
        for (int row = 0; row < numRows; row++ ) {
            for (int column = 0; column < numColumns; column++) {
                newMatrix[row][column] = matrix[row][column].setScale(finalScale, RoundingMode.HALF_UP);
            }
        }
        return new Matrix(newMatrix);
    }

    private void subtract(BigDecimal[] matrixRow, BigDecimal[] subtractMatrix) {
        for (int idx = 0; idx < matrixRow.length; idx++) {
            matrixRow[idx] = matrixRow[idx].subtract(subtractMatrix[idx]);
        }
    }

    private void makeLeadingCoefficientOne(BigDecimal[][] matrixRow, BigDecimal[][] augmentedMatrixRow, int row) {
        BigDecimal leadingReciprocal = matrixRow[row][row];

        for (int idx = 0; idx < matrixRow.length; idx++) {
            matrixRow[row][idx] = matrixRow[row][idx].divide(leadingReciprocal, scale, RoundingMode.HALF_UP);
            augmentedMatrixRow[row][idx] = augmentedMatrixRow[row][idx].divide(leadingReciprocal, scale, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal[] getCopyRow(BigDecimal[] matrixRow) {
        BigDecimal[] matrixRowCopy = new BigDecimal[matrixRow.length];
        for (int idx = 0; idx < matrixRow.length; idx++) {
            matrixRowCopy[idx] = decimalCopy(matrixRow[idx]);
        }
        return matrixRowCopy;
    }

    private BigDecimal[] multiplyEach(BigDecimal[] matrixRow, BigDecimal scalar) {
        for (int idx = 0; idx < matrixRow.length; idx++) {
            matrixRow[idx] = matrixRow[idx].multiply(scalar);
        }
        return matrixRow;
    }

    private static Vector getVerticleVector(int columnNum, Matrix matrix) {
        BigDecimal[] dimensions = new BigDecimal[matrix.getNumRows()];
        for (int row = 0; row < matrix.getNumRows(); row++) {
            dimensions[row] = matrix.getValue(row, columnNum);
        }
        return new Vector(dimensions);
    }

    private static Vector getHorizontalVector(int rowNum, Matrix matrix) {
        return new Vector(matrix.getMatrix()[rowNum]);
    }

    private static BigDecimal decimalCopy(BigDecimal original) {
        return original.multiply(new BigDecimal("1").setScale(scale, RoundingMode.HALF_UP));
    }

    BigDecimal[][] getMatrix() {
        return matrix;
    }

    private BigDecimal getValue(int row, int column) {
        return matrix[row][column];
    }

    private int getNumColumns() {
        return numColumns;
    }

    private int getNumRows() {
        return numRows;
    }
}
