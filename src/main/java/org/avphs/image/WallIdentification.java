package org.avphs.image;


/**Holds all of the functions for identifying walls in ImageModule
 *
 * @author Joshua Bromley
 * @author Kenneth Browder
 * @author Kevin "Poo" Tran
 * @see ImageModule
 */
public class WallIdentification {

    static final int edgeThreshold = 30;

    static final int wallNum = 2;

    static final int[] ColorArr = {
            0xFF0000, //RED     0
            0x00FF00, //GREEN   1
            0x0000FF, //BLUE    2
            0x00FFFF, //CYAN    3
            0xFF00FF, //MAGENTA 4
            0xFFFF00, //YELLOW  5
            0,        //BLACK   6
            0x333333, //GREY1   7
            0x666666, //GREY2   8
            0x999999, //GREY3   9
            0xCCCCCC, //GREY4   10
            0xFFFFFF  //WHITE   11
    };

    static final int[][] WallColorSeqs = {
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,9,8,6},
            {10,9,10,9,8,6},
            {10,9,10,9,8,6},
            {10,11,10,9,10,11,10,9,8,6},
            {10,11,10,9,10,11,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,11,10,9,8,6},
            {10,9,10,11,10,9,8,6},
            {10,9,10,11,10,9,8,6},
            {10,11,10,9,8,9,8,6},
            {10,11,10,9,8,9,10,9,8,6},
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,9,8,6},
            {10,9,10,9,8,6},
            {10,9,10,9,8,6},
            {10,11,10,9,10,11,10,11,10,9,8,6},
            {10,11,10,9,10,11,10,11,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,11,10,11,10,9,8,6},
            {10,9,10,11,10,11,10,9,8,6},
            {10,9,10,11,10,11,10,9,8,6},
            {10,11,10,9,8,9,8,6},
            {10,11,10,9,8,9,10,11,10,9,8,6},
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,9,8,6},
            {10,9,10,9,10,9,8,6},
            {10,9,10,9,10,9,8,6},
            {10,11,10,9,10,11,10,9,10,9,8,6},
            {10,11,10,9,10,11,10,9,10,9,8,6},
            {10,11,10,9,8,9,8,9,10,11,10,9,10,9,8,6},
            {10,9,10,11,10,9,10,9,8,6},
            {10,9,10,11,10,9,10,9,8,6},
            {10,11,10,9,8,9,10,9,8,6},
            {10,11,10,9,8,9,10,9,10,9,8,6}
    };

    /**Takes image in and searches for tube walls
     *
     * @param codeArray Image where colors are represented by the numbers outlined in ColorArr
     * @param width Width of Image
     * @param height Height of Image
     *
     * @return 2D array with 2 rows, top coordinates of watt and bottom coordinates of wall
     */
    static int[][] scanImage(int[] codeArray, int width, int height) {
        int[] wallBottoms = new int[width];
        int[] wallTops = new int[width];
        for(int i = 0; i < width; i++){
            int currColor = 0;
            int currTop = -1;
            for(int j = 0; j < height; j++){
                if((codeArray[j*width + i] == 10 || codeArray[j*width + i] == 11) && currTop == -1){
                    //Looks for white or light grey as the top of a wall, checks if it has not found the top of a wall
                    currTop = j; // Sets the top of the wall to where it thinks it is
                }else if((codeArray[j*width + i] == currColor || codeArray[j*width + i] == currColor + 1 || codeArray[j*width + i] == currColor -1 || codeArray[j*width + i] == currColor + 2 || codeArray[j*width + i] == currColor - 2) && currTop != -1){
                    //Makes sure the colors don't jump more than two shades of grey to count as a wall
                    if(codeArray[j*width + i] == 6){
                        //Black signifies the end of a wall
                        wallTops[i] = currTop;
                        wallBottoms[i] = j;
                        break;
                    }
                }else{
                    //Resets currTop to signify it has not found a wall
                    currTop = -1;
                }
                //Advances currColor to the color of the pixel
                currColor = codeArray[j*width + i];
            }
        }
        int[] newWallTops = new int[width];
        int[] newWallBottoms = new int[width];
        //removeOutliers(wallTops, wallBottoms, newWallTops, newWallBottoms);
        int[][] out = {newWallBottoms, newWallTops};
        fillEmptySpacesRtL(out);
        checkEdges(out, height, edgeThreshold);
        return out;
    }

    /**
     *
     * @param bayer Bayer array to process (from camera).
     * @param width Width of image.
     * @param height Height of image.
     * @param dt The difference threshold with which to determine color.
     * @param tile The tile pattern of the bayering 0 -> RGGB, 1 -> GBRG
     *
     * @return Array of wall tops and bottoms.
     */
    static int[][] magicloop(byte[] bayer, int width, int height, int dt, int tile) {
        int[] wallBottoms = new int[width];
        int[] wallTops = new int[width];
        int[] wallType = new int[width];
        for(int i = 0; i < 640; i++){
            int currColor = -1;
            int currTop = -1;
            boolean ftcWall = true;
            for(int j = 0; j < 480; j++){
                int r = 0,g = 0,b = 0;
                switch(tile){
                    case 0:
                        r = (int)bayer[2*(2*j*width+i)] & 0xFF;
                        g = (int)bayer[2*(2*j*width+i)+1] & 0xFF;
                        b = (int)bayer[2*((2*j+1)*width+i)+1] & 0xFF;
                        break;
                    case 1:
                        r = (int)bayer[2*(2*(j+1)*width+i)] & 0xFF;
                        g = (int)bayer[2*(2*j*width+i)] & 0xFF;
                        b = (int)bayer[2*((2*j)*width+i)+1] & 0xFF;
                        break;
                }
                ImageProcessing.PosterColor posterPix = ImageProcessing.posterizeChannels(r, g, b, dt);
                int pixelColor = posterPix.code;
                if((pixelColor == 10 || pixelColor == 11) && currTop == -1){
                    //Looks for white or light grey as the top of a wall, checks if it has not found the top of a wall
                    currTop = j; // Sets the top of the wall to where it thinks it is
                }else if((pixelColor == currColor || pixelColor == currColor + 1 || pixelColor == currColor -1 || pixelColor == currColor + 2 || pixelColor == currColor - 2) && currTop != -1){
                    //Makes sure the colors don't jump more than two shades of grey to count as a wall
                    if(pixelColor == 6){
                        //Black signifies the end of a wall
                        wallTops[i] = currTop;
                        wallBottoms[i] = j;
                        wallType[i] = 1;
                        break;
                    }
                }else{
                    //Resets currTop to signify it has not found a wall
                    currTop = -1;
                }
                if(pixelColor == 0 && ftcWall && j > 1){
                    wallTops[i] = 0;
                    wallBottoms[i] = j;
                    wallType[i] = 2;
                    break;
                }else if(currColor != pixelColor && j > 1){
                    ftcWall = false;
                }


                //Advances currColor to the color of the pixel
                currColor = pixelColor;
            }
        }
        int[] newWallTops = new int[width];
        int[] newWallBottoms = new int[width];
        removeOutliers(wallTops, wallBottoms, newWallTops, newWallBottoms,wallType);
        int[][] out = {wallBottoms, wallTops, wallType};
        fillEmptySpacesLtR(out);
        return out;
    }

    /**Removes outliers from our detected wall data
     *
     * @param inArrayTop Input of top coordinates
     * @param inArrayBottom Input of bottom coordinates
     * @param outArrayTop Output of top coordinates without outliers
     * @param outArrayBottom Output of bottom coordinates without outliers
     *
     *
     */
    static void removeOutliers(int[] inArrayTop, int[] inArrayBottom, int[] outArrayTop, int[] outArrayBottom, int[]wallTypes){
        double topMean[] = new double[wallNum + 1], bottomMean[] = new double[wallNum + 1];
        int topCount[] = new int[wallNum + 1], bottomCount[] = new int [wallNum + 1];
        //Calculate the mean for the top and bottom coordinates
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] != 0){
                topMean[wallTypes[i]] += inArrayTop[i];
                topCount[wallTypes[i]]++;
            }
            if(inArrayBottom[i] != 0){
                bottomMean[wallTypes[i]] += inArrayBottom[i];
                bottomCount[wallTypes[i]]++;
            }
        }
        topMean[1] /= topCount[1];
        bottomMean[1] /= bottomCount[1];
        topMean[2] /= topCount[2];
        bottomMean[2] /= bottomCount[2];
        //Calculate the standard deviation for top and bottom coordinates
        int[][] topVariance = new int[wallNum + 1][inArrayTop.length];
        int[][] bottomVariance = new int[wallNum + 1][inArrayTop.length];
        double topStddev[] = new double[wallNum+1], bottomStddev[] = new double[wallNum+1];
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] != 0){
                topVariance[wallTypes[i]][i] = (inArrayTop[i] - (int)topMean[wallTypes[i]]) * (inArrayTop[i] - (int)topMean[wallTypes[i]]);
            }else{
                topVariance[wallTypes[i]][i] = 0;
            }
            topStddev[wallTypes[i]] += topVariance[wallTypes[i]][i];
            if(inArrayBottom[i] != 0){
                bottomVariance[wallTypes[i]][i] = (inArrayBottom[i] - (int)bottomMean[wallTypes[i]]) * (inArrayBottom[i] - (int)bottomMean[wallTypes[i]]);
            }else{
                bottomVariance[wallTypes[i]][i] = 0;
            }
            bottomStddev[wallTypes[i]] += bottomVariance[wallTypes[i]][i];

        }
        topStddev[1] /= topCount[1];
        topStddev[1] = Math.sqrt(topStddev[1]);
        bottomStddev[1] /= bottomCount[1];
        bottomStddev[1] = Math.sqrt(bottomStddev[1]);
        //Finds outliers based on one standard deviation away from the mean
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] > topMean[wallTypes[i]] + topStddev[wallTypes[i]] || inArrayTop[i] < topMean[wallTypes[i]] - topStddev[wallTypes[i]] || inArrayBottom[i] > bottomMean[wallTypes[i]] + bottomStddev[wallTypes[i]] || inArrayBottom[i] < bottomMean[wallTypes[i]] - bottomStddev[wallTypes[i]]){
                outArrayTop[i] = 0;
                outArrayBottom[i] = 0;
            }else{
                outArrayTop[i] = inArrayTop[i];
                outArrayBottom[i] = inArrayBottom[i];
            }
        }
    }

    /**
     * Uses linear interpolation to fill in gaps in wall detection
     * @param arr Top and bottom wall coordinates
     */
    static void fillEmptySpacesRtL(int[][] arr){
        int x1 = 0; int x2 = 0; int y1 = 0; int y2 = 0;
        for(int i = 1; i < arr[0].length - 2; i++){
            if(arr[0][i] == 0){
                if(arr[0][i-1] != 0){
                    x1 = i-1;
                    y1 = arr[0][i-1];
                    int j = i + 1;
                    while(arr[0][j] == 0 && j < arr[0].length - 1){
                        j++;
                    }
                    if(j < arr[0].length - 1) {
                        x2 = j;
                        y2 = arr[0][j];

                        for (int k = x1; k < x2; k++) {
                            arr[0][k] = ((y1 - y2) / (x1 - x2)) * (k - x1) + y1;
                        }

                        y1 = arr[1][i - 1];
                        y2 = arr[1][j];

                        for (int k = x1; k < x2; k++) {
                            arr[1][k] = ((y1 - y2) / (x1 - x2)) * (k - x1) + y1;
                        }
                        i = x2;
                        //drawPixel(a, x1, y1, width, 3);
                        //System.out.println("x1, y1 =" + x1 + " " + y1 + " x2, y2 =" + x2 + " " + y2);
                    }
                }
            }
        }
    }

    /**
     * Uses linear interpolation to fill in gaps in wall detection
     * @param arr Top and bottom wall coordinates
     */
    static void fillEmptySpacesLtR(int[][] arr){
        int x1 = 0; int x2 = 0; int y1 = 0; int y2 = 0;
        for(int i = arr[0].length - 2; i > 1; i--){
            if(arr[0][i] == 0){
                if(arr[0][i-1] != 0){
                    x1 = i-1;
                    y1 = arr[0][i-1];
                    int j = i + 1;
                    while(arr[0][j] == 0 && j < arr[0].length - 1){
                        j++;
                    }
                    x2 = j;
                    y2 = arr[0][j];

                    for(int k = x1; k < x2; k++){
                        arr[0][k] = ((y1-y2)/(x1-x2)) * (k - x1) + y1;
                    }

                    y1 = arr[1][i-1];
                    y2 = arr[1][j];

                    for(int k = x1; k < x2; k++){
                        arr[1][k] = ((y1-y2)/(x1-x2)) * (k - x1) + y1;
                    }
                    i = x2;
                    //drawPixel(a, x1, y1, width, 3);
                    System.out.println("x1, y1 =" + x1 + " " + y1 + " x2, y2 =" + x2 + " " + y2);

                }
            }
        }
    }

    /** Fills in any empty pixels on the left or right of the image that fillEmptySpaces misses
     *
     * @param arr Top and bottom wall coords
     * @param height Height of image
     * @param th Threshold to use to estimate coordinates
     */
    static void checkEdges(int[][] arr, int height, int th){
        int threshold = th;
        int x1 = 0; int x2 = 0; int y1 = 0; int y2 = 0;
        //check left edge
        if(arr[0][0] <= 0){
            int j = 1;

            while(arr[0][j] <= 0 && j < arr[0].length - (1 + threshold)){
                j++;
            }
            if(j < arr[0].length - (1 + threshold)){
                x1 = j; x2 = j + threshold;
                y1 = arr[0][j]; y2 = arr[0][j + threshold];
                for(int k = 0; k < x1; k++){
                    arr[0][k] = (int)((((double)y1-y2)/(x1-x2)) * (k - x1) + y1);
                    if(arr[0][k] > height - 1){
                        arr[0][k] = height - 1;
                    }
                }

                y1 = arr[1][j]; y2 = arr[1][j + threshold];
                for(int k = 0; k < x1; k++){
                    arr[1][k] = (int)((((double)y1-y2)/(x1-x2)) * (k - x1) + y1);
                    if(arr[1][k] < 0){
                        arr[1][k] = 0;
                    }
                }
            }
        }
        if(arr[0][arr[0].length - 1] <= 0) { //check right edge
            int j = arr[0].length - 1;

            while(arr[0][j] <= 0 && j > 0 + threshold){
                j--;
            }
            if(j > 0 + threshold){
                x1 = j; x2 = j - threshold;
                y1 = arr[0][j]; y2 = arr[0][j - threshold];
                for(int k = j; k < arr[0].length - 1; k++){

                    //System.out.println((((y1-y2)/(x1-x2)) * (k - x1) + y1) + " " + k);
                    arr[0][k] = (int)((((double)y1-y2)/(x1-x2)) * (k - x1) + y1);
                    //System.out.println(arr[0][k]);
                    if(arr[0][k] > height - 1){
                        arr[0][k] = height - 1;
                    }
                }

                y1 = arr[1][j]; y2 = arr[1][j - threshold];
                for(int k = j; k < arr[0].length - 1; k++){
                    arr[1][k] = (int)((((double)y1-y2)/(x1-x2)) * (k - x1) + y1);
                    if(arr[1][k] < 0){
                        arr[1][k] = 0;
                    }
                }
            }
        }
    }

}
