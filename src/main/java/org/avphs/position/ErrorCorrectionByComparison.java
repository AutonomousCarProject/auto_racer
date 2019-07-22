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
}
