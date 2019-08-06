package org.avphs.position;

import org.avphs.coreinterface.CarData;

import java.util.ArrayList;
import java.util.Arrays;

//maybe also run this when we detect a turn (turning position tracking is more complex)
//TODO: !!!!!make an error correction for car direction!!!!
public class ErrorCorrectionByComparison { //random thought: I wonder if introducing randomized error would help somehow
    private boolean[][] map;
    private float mapIndexLength = 1; //temporary, represents 1cm for now
    private float viewAngle = 90; //temporary, in degrees (total camera view angle)
    private PositionData posData;
    private float discrepancyTolerance = 8; //temporary, needs testing to iron out
    private float finalAveragingImageBias = 1; //temporary, change depend on testing


    public float[] initializeVariablesAndRun(CarData carData) {
        return errorCorrect();
        //TODO: add initializer
    }


    //bottom of field of view is 21.5 cm from car
    /*FIXME: NEED TO MAKE SURE DISTANCE IS SYNCED WITH MAP (PositionModule position calc using speed is wrong?)
        ALSO NEED TO MAKE SURE CAR COORDINATE ALIGNS WITH CAMERA (make it center of car and account for that or
            make the 'coordinate' of the car the camera, or make several car coordinates)*/
    //wall heights needs to make sure that distance from wall adjusts to where in the image its taken from (done?)
    private float[] errorCorrect() {
        //STEP 1: find nearest (20?) coordinates of walls in the map (PROBABLY UNNEEDED)
        //STEP 2: convert wall_heights to distances using the mapping group's thing
        //STEP 3: create 'dummy coordinates' of where only image data calculates wall location as
        /*STEP 4: find map wall coordinates in the camera's direction and calculate distance based on only sensor data
            (i.e. how far away from walls in that direction do the sensor coordinates think the car is)*/
        /*steps 3-4 are essentially for matching the wall segments from image data with sensor data so they agree
          on WHAT wall they are talking about, but not necessarily WHERE it is/where the care is relative to it*/
        //STEP 5: (this depends on testing results) average the two estimates of where car is (distance from walls)



        /*//STEP 1
        //PROBABLY UNNEEDED
        //TODO: make it so that it only checks for walls roughly in camera direction
        int nearestX = Math.round(posData.getPosition()[0]);
        int nearestY = Math.round(posData.getPosition()[1]);
        ArrayList<int[]> nearestWalls = new ArrayList<>();
        int squareCheckSize = 1;
        while (nearestWalls.size() < 20) { //TODO: make sure it doesnt crash if map is too small
            checkSquarePerimeterFor(true, nearestWalls, new int[]{nearestX, nearestY}, squareCheckSize);
            squareCheckSize += 1;
        }
         */


        //STEP 2:
        //TODO: ADD CALIBRATION'S CONVERSION
        float distanceFromWallEdgeL = 1; //temporary
        float distanceFromWallCenter = 1; //temporary
        float distanceFromWallEdgeR = 1; //temporary
        //wallAngleDistances contains an angle and the calculated distance from the wall at that angle (currently has placeholders)
        ArrayList<float[]> wallAngleDistances = new ArrayList<>(Arrays.asList(new float[]{viewAngle / 2, 1}, new float[]{0, 1}, new float[]{-viewAngle / 2, 1}));


        //STEP3:
        //imageWallCoordinates contains the dummy x,y coordinates of where image data calculates wall is
        ArrayList<float[]> imageWallCoordinates = new ArrayList<>();
        for (int i = 0; i < wallAngleDistances.size(); i++) {
            imageWallCoordinates.add(calculateImageBasedWall(wallAngleDistances.get(i)[0], wallAngleDistances.get(i)[1]));
        }


        //STEP 4:
        /*TODO: make it do something if it can't locate a wall (done?)
            if large discrepancy detected between image distance and sensor distance (i.e. can't locate wall) do either:
                idea 1:'move' back/forward a bit and search same angle again
                idea 2: start searching square around point on that ray for where it 'thinks' wall should be (done)
                idea 3: ignore it entirely and use angles where it did find a wall (adjust end averaging accordingly)*/
        //sensorWallCoordinates contains the nearest wall coordinates in the same direction as image calculations
        ArrayList<float[]> sensorWallCoordinates = new ArrayList<>();
        for (int i = 0; i < wallAngleDistances.size(); i++) {
            sensorWallCoordinates.add(locateSensorBasedWall(wallAngleDistances.get(i)[0], posData.getPosition()[0], posData.getPosition()[1]));

            if (distanceFormula(imageWallCoordinates.get(i)[0], imageWallCoordinates.get(i)[1],
                    sensorWallCoordinates.get(i)[0], sensorWallCoordinates.get(i)[1]) >= discrepancyTolerance) {
                /*//IDEA 1 (arbitrarily chooses the first wall it finds, may want to change later)
                sensorWallCoordinates.add(sensorLocateFailed1(wallAngleDistances.get(i)[0], posData.getPosition()[0],
                        posData.getPosition()[1], imageWallCoordinates.get(i)[0], imageWallCoordinates.get(i)[1]));
                 */

                /*//IDEA 2 (ARBITRARILY CHOOSES FIRST WALL FOUND, MAY WANT TO CHANGE LATER)
                sensorWallCoordinates.add(sensorLocateFailed2(imageWallCoordinates.get(i)[0], imageWallCoordinates.get(i)[1]).get(0));
                */

                /*//IDEA 3
                sensorLocateFailed3(sensorWallCoordinates, imageWallCoordinates, wallAngleDistances, i);
                i--;
                 */
            }
        }


        //STEP 5:
        //a more complicated averaging might be necessary, depends on testing results
        //(has to be image - sensor I think)
        float adjustedPositionX = posData.getPosition()[0];
        float adjustedPositionY = posData.getPosition()[1];
        for (int i = 0; i < wallAngleDistances.size(); i++) {
            adjustedPositionX += ((imageWallCoordinates.get(i)[0] * (1 / finalAveragingImageBias) - (sensorWallCoordinates.get(i)[0]) * finalAveragingImageBias) / (float) wallAngleDistances.size());
            adjustedPositionY += ((imageWallCoordinates.get(i)[1] * (1 / finalAveragingImageBias) - (sensorWallCoordinates.get(i)[1]) * finalAveragingImageBias) / (float) wallAngleDistances.size());
        }


        return new float[]{adjustedPositionX, adjustedPositionY};
    }


    /*passing the function true checks for walls, passing false checks for road
        this function only ever adds walls that it finds, it doesn't add found roads (no reason to add roads)
        it also doesn't count walls that are not adjacent to any roads as walls*/
    private boolean checkSquarePerimeterFor(boolean checkWalls, ArrayList<int[]> wallList, int[] center, int squareSize) {
        boolean found = false;
        for (int i = -squareSize; i <= squareSize; i++) {
            if (i == -squareSize || i == squareSize) {
                for (int j = -squareSize; j <= squareSize; j++) {
                    if (!map[i + center[0]][j + center[1]] && checkWalls) {
                        found = true;
                        if (checkSquarePerimeterFor(false, wallList, new int[]{i, j}, 1)) {
                            wallList.add(new int[]{i, j});
                        }
                    } else if (map[i + center[0]][j + center[1]] && !checkWalls) {
                        return true;
                    }
                }
            } else {
                if (!map[i + center[0]][-squareSize + center[1]] && checkWalls) {
                    found = true;
                    if (checkSquarePerimeterFor(false, wallList, new int[]{i, -squareSize}, 1)) {
                        wallList.add(new int[]{i, squareSize});
                    }
                } else if (map[i + center[0]][-squareSize + center[1]] && !checkWalls) {
                    return true;
                }
                if (!map[i + center[0]][squareSize + center[1]] && checkWalls) {
                    found = true;
                    if (checkSquarePerimeterFor(false, wallList, new int[]{i, squareSize}, 1)) {
                        wallList.add(new int[]{i, squareSize});
                    }
                } else if (map[i + center[0]][squareSize + center[1]] && !checkWalls) {
                    return true;
                }
            }
        }
        return found;
    }


    //positive angle is 'left' of image, 0 is center, negative angle is 'right' of image
    private float[] calculateImageBasedWall(float angle, float distanceFromWall) {
        return new float[]{posData.getPosition()[0] + ((float) Math.cos(Math.toRadians(posData.getDirection() + angle)) * distanceFromWall),
                posData.getPosition()[1] + ((float) Math.sin(Math.toRadians(posData.getDirection() + angle)) * distanceFromWall)};
    }

    //positive angle is 'left' of image, 0 is center, negative angle is 'right' of image
    private float[] locateSensorBasedWall(float angle, float initialX, float initialY) {
        boolean sensorWallLocated = false;
        float checkingLocationX = initialX;
        float checkingLocationY = initialY;
        while (!sensorWallLocated) {
            checkingLocationX += (float) Math.cos(Math.toRadians(posData.getDirection() + angle)) * mapIndexLength;
            checkingLocationY += (float) Math.sin(Math.toRadians(posData.getDirection() + angle)) * mapIndexLength;
            if (!map[Math.round(checkingLocationX)][Math.round(checkingLocationY)]) {
                sensorWallLocated = true;
            }
        }
        return new float[]{checkingLocationX, checkingLocationY};
    }

    //TODO: idea 1 (arbitrarily chooses first wall within discrepancy tolerance that it finds)
    private float[] sensorLocateFailed1(float angle, float carX, float carY, float imageX, float imageY) {
        int squareSize = 1;
        //TODO: MULTIPLY BY INDEX DISTANCE EQUIVALENT (is 1 atm so makes no difference)
        while (true) {
            for (int i = -squareSize; i <= squareSize; i++) {
                if (i == -squareSize || i == squareSize) {
                    for (int j = -squareSize; j <= squareSize; j++) {
                        float[] newWallCoord = locateSensorBasedWall(angle, carX, carY);
                        if (distanceFormula(imageX, imageY, newWallCoord[0], newWallCoord[1]) <= discrepancyTolerance) {
                            return newWallCoord;
                        }
                    }
                } else {
                    float[] newWallCoord = locateSensorBasedWall(angle, carX, carY);
                    if (distanceFormula(imageX, imageY, newWallCoord[0], newWallCoord[1]) <= discrepancyTolerance) {
                        return newWallCoord;
                    }
                }
            }
            squareSize++;
        }
    }

    //IDEA 2
    private ArrayList<float[]> sensorLocateFailed2(float imageX, float imageY) {
        float adjustXBy = imageX - Math.round(imageX);
        float adjustYBy = imageY - Math.round(imageY);
        int squareSize = 1;
        ArrayList<int[]> wallsFound = new ArrayList<>();
        ArrayList<float[]> wallsFoundPrecise = new ArrayList<>();
        while (wallsFound.size() < 1) {
            checkSquarePerimeterFor(true, wallsFound, new int[]{Math.round(imageX), Math.round(imageY)}, squareSize);
            squareSize += 1;
        }
        for (int[] ints : wallsFound) {
            wallsFoundPrecise.add(new float[]{ints[0] + adjustXBy, ints[1] + adjustYBy});
        }
        return wallsFoundPrecise;
    }

    private float distanceFormula(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    //IDEA 3 TODO: BIAS AVERAGE TOWARDS IMAGE DATA AFTER CALLING THIS MIGHT BE GOOD
    private void sensorLocateFailed3(ArrayList<float[]> sWC, ArrayList<float[]> iWC, ArrayList<float[]> wAD, int removeIndex) {
        sWC.remove(removeIndex);
        iWC.remove(removeIndex);
        wAD.remove(removeIndex);
        //to bias change finalAveragingImageBias
    }


    final float[] turnRadiiForAngles = new float[]{170.1f, 170.2f, 170.292f, 171.99f, 175f, 175.532f, 177.38f, 179.283f, 181.242f, 185f, 185.342f, 187.489f, 189.704f, 191.99f, 194.352f, 196.6f, 199.396f, 201.927f, 204.629f, 206.6f, 210.33f, 213.34f, 216.463f, 219.707f, 224f, 226.585f, 230.236f, 234.039f, 238.005f, 241.4f, 246.466f, 250.987f, 255.718f, 260.676f, 265.877f, 271.338f, 277.081f, 283.128f, 289.503f, 295.3f, 303.351f, 310.889f, 318.887f, 327.386f, 336.437f, 346.095f, 356.423f, 367.493f, 374.99f, 375.5f, 406.053f, 421.064f, 437.389f, 455.211f, 474.743f, 496.245f, 520.031f, 546.484f, 576.081f, 620f, 647.248f, 690.55f, 740.601f, 799.113f, 868.429f, 951.846f, 1054.154f, 1182.59f, 1348.628f, 1571f, 1886.905f, 2366.784f, 3185.64f, 4898.768f, 10733.292f, 11000f, 12000f, 13000f, 14000f, 9000f, 8000f, 7000f, 6000f, 5808.404f, 3940.952f, 2505.715f, 1960.406f, 1614.147f, 1375f, 1199.411f, 1065.412f, 959.686f, 874.138f, 803.49f, 744.174f, 693.655f, 650.114f, 612.199f, 576f, 549.38f, 523.07f, 499.467f, 478.17f, 458.847f, 441.247f, 425.147f, 410.36f, 396.737f, 387f, 372.463f, 361.604f, 351.4826f, 342.0249f, 333.17f, 324.86f, 317.04f, 309.68f, 302.73f, 295f, 289.95f, 284.06f, 278.466f, 273.149f, 268.088f, 263.265f, 258.66f, 254.27f, 253.5f, 253f, 242.12f, 238.51f, 234.96f, 231.56f, 230f, 225.147f, 222.12f, 219.2f, 216.39f, 213.4f, 211.06f, 208.53f, 206.09f, 203.7f, 197.8f, 197.7f, 197.087f, 195f, 193f, 189f, 189.15f, 187.31f, 185.527f, 183.79f, 181.3f, 180.46f, 178.869f, 176.4f}; //-77 <= x <= 80; x != 2

    public float getTurnRadiusOfAngle(int angle) {
        if (angle < -77) {
            angle = -77;
            System.out.println("You requested radius for an angle less than -77. Don't.");
        } else if (angle > 80) {
            angle = 80;
            System.out.println("You requested radius for an angle greater than 80. Don't.");
        }
        angle += 77;
        return (turnRadiiForAngles[angle]);
    }

    public int getAngleForTurnRadius(float radius, boolean turningLeft) { //can be optimized
        if (turningLeft) {
            if (radius > turnRadiiForAngles[78]) { //if radius is greater than 1 deg radius
                return 2; //go straight
            } else {
                for (int i = 1; i >= -77; i--) {
                    if (radius >= turnRadiiForAngles[i + 77]) {
                        if (Math.abs(turnRadiiForAngles[i + 77] - radius) <= Math.abs(turnRadiiForAngles[i + 78] - radius)) {
                            return i;
                        } else {
                            return i + 1;
                        }
                    }

                }
            }
        } else {
            if (radius > turnRadiiForAngles[79]) { //if radius is greater than 3 deg radius
                return 2; //go straight
            } else {
                for (int i = 3; i <= 80; i++) {
                    if (radius >= turnRadiiForAngles[i + 76]) {
                        if (Math.abs(turnRadiiForAngles[i + 76] - radius) <= Math.abs(turnRadiiForAngles[i + 75] - radius)) {
                            return i;
                        } else {
                            return i - 1;
                        }
                    }

                }
            }
        }
        return 2;
    }
}
