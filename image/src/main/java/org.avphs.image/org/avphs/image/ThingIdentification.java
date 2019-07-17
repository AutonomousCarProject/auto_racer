package org.avphs.image;

public class ThingIdentification {
    static int imageWidth = 0;
    static int imageHeight = 0;

    static int[] stateTable;

    public static void createTable() {
        System.out.println("Hello World");
    }
    
    public void setStateTable(int[] table) {
        stateTable = table;
    }

    public static void setImageWidthHeight(int width, int height) {
        imageWidth = width;
        imageHeight = height;
    }

    public static void identifyWalls(int[] posterizedIntImage, int[] wallTypes, int[] wallStarts, int[] wallHeights) {
        int col = 0;
        int row = imageHeight - 1;

        int currentPixel;
        int state = 0;
        
        // extra data:
        int savedNumber0 = 0;
        int savedNumber1 = 0;
        /*
        state encoding:
        
        [when not special]
                                                               room for pixel value (will be zeroed) ┐
                                                                                                     │
                        state number (which will always be a multiple of 16)                         │
         /------------------------------------------------------------------------------------\ /---------\
        ┌──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┐
        │32│31│30│29│28│27│26│25│24│23│22│21│20│19│18│17│16│15│14│13│12│11│10│ 9│ 8│ 7│ 6│ 5│ 4│ 3│ 2│ 1│ 0│
        ├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
        │ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│ 0│
        └──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┘
        
         first 16 bits are for an instruction for the special state
         next 8 are for a number that is for data (type of wall, etc.)
         */

        while (true) {
            if ((1 & state) == 0) {  // the first bit is for indicating a special state
                // if not special, do regular stuff
                --row;  // move to next pixel (one row above previous)
                if (row < 0) {
                    // top of image reached
                    if (savedNumber0 != 0) {  // if the start of a wall has been seen
                        // record the base (but the type and height are unknown)
                        wallStarts[col] = savedNumber0;
                        wallHeights[col] = -1;
                        wallTypes[col] = -1;

                        state = 0b0000_0010_0000_0000_0000_0000_0000_0001; // ins2: move to next line
                    } else {
                        // otherwise, no wall is recorded
                        state = 0b0000_0001_0000_0000_0000_0000_0000_0001;  // mark unidentified
                    }
                    continue;
                }
                currentPixel = posterizedIntImage[row * imageWidth + col];  // get pixel value

                // combine current state and pixel value
                state = stateTable[state & currentPixel];
            } else {
                if ((2 & state) == 0) { //the second bit is for indicating to use state bits as a state
                    int instruction = (state >> 24); //instructions for what to do next
                    
                    if (instruction == 0) { //first instruction (stop)
                        break;
                    } else if (instruction == 1) { //second instruction (wall cannot be identified)
                        wallTypes[col] = -1;
                        wallHeights[col] = -1;
                        wallStarts[col] = -1;
                        
                        state = 0b0000_0010_0000_0000_0000_0000_0000_0001; // ins2: move to next line
                    } else if (instruction == 2) { //move to the next column
                        col++;
                        if (col == imageWidth)
                            break;
                        
                        // reset variables
                        row = imageHeight - 1;
                        state = 0;
                        savedNumber0 = 0;
                        savedNumber1 = 0;
                    } else if (instruction == 3) {  // record found+finished wall where wall base is in `savedNumber0`
                        int value = (state >> 16) & 0b1111_1111;  // wall type is encoded in bits 24-16
                        
                        wallTypes[col] = value;  // record wall type
                        wallStarts[col] = savedNumber0;  // wall base is stored in `savedNumber0`
                        wallHeights[col] = row - savedNumber0;  // wall height is current row minus base
                        
                        state = 0b0000_0010_0000_0000_0000_0000_0000_0001; // ins2: move to next line
                    } else if (instruction == 4) {
                        // record wall where base is in `savedNumber0` and top is in `savedNumber2`
                        
                        int value = (state >> 16) & 0b1111_1111;  // wall type is encoded in bits 24-16

                        wallTypes[col] = value;  // record wall type
                        wallStarts[col] = savedNumber0;  // wall base is stored in `savedNumber0`
                        wallHeights[col] = savedNumber1 - savedNumber0;  // wall top is in `savedNumber1`

                        state = 0b0000_0010_0000_0000_0000_0000_0000_0001; // ins2: move to next line
                    }
                } else {
                    // state bits contain next state
                    
                    // instruction is in bits 3-1
                    int instruction = (state >> 2) & 0b11;
                    
                    // do stuff
                    if (instruction == 0) {
                        savedNumber0 = row;
                    } else if (instruction == 1) {
                        savedNumber1 = row;
                    }
                    
                    state = state & 0b1111_1111_1111_1111_1111_1111_1111_0000;  // clear special bits
                }
            }
        }
    }
}









