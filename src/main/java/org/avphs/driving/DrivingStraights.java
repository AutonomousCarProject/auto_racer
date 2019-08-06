package org.avphs.driving;


import org.avphs.coreinterface.CarData;
import org.avphs.image.ImageData;

import java.awt.*;
import java.util.ArrayList;

public class DrivingStraights implements Drivable {

    private ArrayList<Point> leftWall;
    private ArrayList<Point> rightWall;

    private static final int MINIMUM_X_DIFFERENCE = 100;
    private static final int MINIMUM_Y_DIFFERENCE = 100;

    public DrivingStraights() {
        leftWall = new ArrayList<>();
        rightWall = new ArrayList<>();
    }

    @Override
    public int getSteeringAngle(CarData carData) {
        int[] wallBottoms = ((ImageData) carData.getModuleData("image")).wallBottom;
        initWallWallBottoms(wallBottoms);
        Point steerPoint = calculateSteerPoint();

        return 0;
    }

    @Override
    public int getThrottle(CarData carData) {
        return 0;
    }

    private Point calculateSteerPoint() {
        return new Point(0, 0);
    }

    private void initWallWallBottoms(int[] wallBottoms) {
        initWall(wallBottoms, 0, leftWall);
        initWall(wallBottoms, leftWall.get(leftWall.size() - 1).x, rightWall);
    }

    private static void initWall(int[] wallBottoms, int startLook, ArrayList<Point> wall) {
        int lastX = startLook;
        int lastY = wallBottoms[startLook];

        for (int pixelNum = startLook; pixelNum < wallBottoms.length; pixelNum++) {
            if (wallBottoms[pixelNum] == 0) {
                continue;
            }
            if (pixelNum - lastX <= MINIMUM_X_DIFFERENCE && Math.abs(wallBottoms[pixelNum] - lastY) <= MINIMUM_Y_DIFFERENCE) {
                wall.add(new Point(pixelNum, 480 - wallBottoms[pixelNum]));
            } else {
                break;
            }
            lastX = pixelNum;
            lastY = wallBottoms[pixelNum];
        }
    }
}
