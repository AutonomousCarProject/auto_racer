package org.avphs.image;

public class WallIdentification {
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

    /**
     *
     * @param codeArray Image where colors are represented by the numbers outlined in ColorArr
     * @param width Width of Image
     * @param height Height of Image
     * Takes image in and searches for tube walls
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
        removeOutliers(wallTops, wallBottoms, newWallTops, newWallBottoms);
        int[][] out = {newWallBottoms, newWallTops};
        fillEmptySpaces(out);
        return out;
    }

    /**
     *
     * @param bayer Bayer array to process (from camera).
     * @param width Width of image.
     * @param height Height of image.
     * @param dt The difference threshold with which to determine color.
     * @return Array of wall tops and bottoms.
     */
    static int[][] magicloop(byte[] bayer, int width, int height, int dt) {
        int[] wallBottoms = new int[width];
        int[] wallTops = new int[width];
        for(int i = 0; i < width; i++){
            int currColor = 0;
            int currTop = -1;
            for(int j = 0; j < height; j++){
                int r = (int)bayer[2*(2*j*width+i)] & 0xFF;
                int g = (int)bayer[2*(2*j*width+i)+1] & 0xFF;
                int b = (int)bayer[2*((2*j+1)*width+i)+1] & 0xFF;
                ImageProcessing.PosterColor posterPix = ImageProcessing.posterizeChannels(r, g, b, dt);
                int currentColor = posterPix.code;
                if((currentColor == 10 || currentColor == 11) && currTop == -1){
                    //Looks for white or light grey as the top of a wall, checks if it has not found the top of a wall
                    currTop = j; // Sets the top of the wall to where it thinks it is
                }else if((currentColor == currColor || currentColor == currColor + 1 || currentColor == currColor -1 || currentColor == currColor + 2 || currentColor == currColor - 2) && currTop != -1){
                    //Makes sure the colors don't jump more than two shades of grey to count as a wall
                    if(currentColor == 6){
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
                currColor = currentColor;
            }
        }
        int[] newWallTops = new int[width];
        int[] newWallBottoms = new int[width];
        removeOutliers(wallTops, wallBottoms, newWallTops, newWallBottoms);
        int[][] out = {newWallBottoms, newWallTops};
        fillEmptySpaces(out);
        return out;
    }

    /**
     *
     * @param inArrayTop Input of top coordinates
     * @param inArrayBottom Input of bottom coordinates
     * @param outArrayTop Output of top coordinates without outliers
     * @param outArrayBottom Output of bottom coordinates without outliers
     * Removes outliers from our detected wall data
     * @see WallIdentification.scanImage()
     */
    static void removeOutliers(int[] inArrayTop, int[] inArrayBottom, int[] outArrayTop, int[] outArrayBottom){
        double topMean = 0, bottomMean = 0;
        int topCount = 0, bottomCount = 0;
        //Calculate the mean for the top and bottom coordinates
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] != 0){
                topMean += inArrayTop[i];
                topCount++;
            }
            if(inArrayBottom[i] != 0){
                bottomMean += inArrayBottom[i];
                bottomCount++;
            }
        }
        topMean /= topCount;
        bottomMean /= bottomCount;
        //Calculate the standard deviation for top and bottom coordinates
        int[] topVariance = new int[inArrayTop.length];
        int[] bottomVariance = new int[inArrayTop.length];
        double topStddev = 0, bottomStddev = 0;
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] != 0){
                topVariance[i] = (inArrayTop[i] - (int)topMean) * (inArrayTop[i] - (int)topMean);
            }else{
                topVariance[i] = 0;
            }
            topStddev += topVariance[i];
            if(inArrayBottom[i] != 0){
                bottomVariance[i] = (inArrayBottom[i] - (int)bottomMean) * (inArrayBottom[i] - (int)bottomMean);
            }else{
                bottomVariance[i] = 0;
            }
            bottomStddev += bottomVariance[i];

        }
        topStddev /= topCount;
        topStddev = Math.sqrt(topStddev);
        bottomStddev /= bottomCount;
        bottomStddev = Math.sqrt(bottomStddev);
        //Finds outliers based on one standard deviation away from the mean
        for(int i = 0; i < inArrayTop.length; i++){
            if(inArrayTop[i] > topMean + topStddev || inArrayTop[i] < topMean - topStddev || inArrayBottom[i] > bottomMean + bottomStddev || inArrayBottom[i] < bottomMean - bottomStddev){
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
    static void fillEmptySpaces(int[][] arr){
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



}
