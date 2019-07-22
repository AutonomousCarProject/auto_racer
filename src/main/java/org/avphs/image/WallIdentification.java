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

    static int[][] scanImage(int[] codeArray, int width, int height, int[][] wallColors) {
        int[] wallBottoms = new int[width];
        int[] wallTops = new int[width];
        for(int i = 0; i < width; i++){
            int currColor = 0;
            int currTop = -1;
            for(int j = 190; j < height; j++){
                if((codeArray[j*width + i] == 10 || codeArray[j*width + i] == 11) && currTop == -1){
                    currTop = j;
                }else if((codeArray[j*width + i] == currColor || codeArray[j*width + i] == currColor + 1 || codeArray[j*width + i] == currColor -1 || codeArray[j*width + i] == currColor + 2 || codeArray[j*width + i] == currColor - 2) && currTop != -1){
                    if(codeArray[j*width + i] == 6){
                        wallTops[i] = currTop;
                        wallBottoms[i] = j;
                        break;
                    }
                }else{
                    currTop = -1;
                }
                currColor = codeArray[j*width + i];
            }
        }
        int[][] out = {wallBottoms, wallTops};
        return out;
    }

}
