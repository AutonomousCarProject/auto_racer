package org.avphs.calibration;

public class CarTests {
    public static void main(String[] args){
        short x = 1;
        byte y = 1;
        CalibrationModule.getSpeedChangeDist(x, y, y);
    }
}
