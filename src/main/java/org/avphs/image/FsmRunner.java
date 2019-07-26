package org.avphs.image;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class FsmRunner {
    private int[] table;  // int array defining the fsm to run
    private int imageWidth;
    private int imageHeight;
    
    public FsmRunner(int[] table, int imageWidth, int imageHeight) {
        this.table = table;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    public FsmRunner(String pathToTableFile, int imageWidth, int imageHeight) throws IOException {
        ArrayList<Integer> numbers = new ArrayList<>();
        Scanner scanner = new Scanner(
                new File(pathToTableFile),
                "utf-8"
        );
        while (scanner.hasNextInt()) {
            numbers.add(scanner.nextInt());
        }
        this.table = new int[numbers.size()];
        for (int i = 0; i < this.table.length; i++) {
            this.table[i] = numbers.get(i);
        }
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    public void identifyWalls(int[] posterizedIntImage, int[] wallTypes, int[] wallStarts, int[] wallHeights) {
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
            System.out.print(debugState(state));
            if ((1 & state) == 0) {  // the first bit is for indicating a special state
                // if not special, do regular stuff
                --row;  // move to next pixel (one row above previous)
                if (row < 0) {  // if at top of image
                    System.out.println("TOP OF IMAGE");
                    if (savedNumber0 != 0) {  // if the start of a wall has been seen
                        System.out.println("recording wall bottom");
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
                System.out.println(" " + ImageProcessing.PosterColor.values()[currentPixel]);

                // combine current state and pixel value
                state = table[state | currentPixel];
            } else {
                System.out.println();
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
                        System.out.println("\n\nCOLUMN: " + col);
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
    
    public static String debugState(int state) {
        StringBuilder builder = new StringBuilder();
        if ((state & 0b0001) == 0) {
            builder.append("TblState( ").append(state >> 4).append(" )");
        } else {
            if ((state & 0b0010) == 0) {
                int instr = (state >> 24);
                builder.append("InstrState( ")
                        .append("i: ")
                        .append(instr);
                if (instr == 3 || instr == 4) {
                    builder.append(" val: ")
                            .append((state >> 16) & 0b1111_1111);
                }
                builder.append(" )");
                
                String comment;
                switch (instr) {
                    case 0:
                        comment = "stop";
                        break;
                    case 1:
                        comment = "fail";
                        break;
                    case 2:
                        comment = "continue to next line";
                        break;
                    case 3:
                        comment = "end of wall found!!! (variation 1)";
                        break;
                    case 4:
                        comment = "end of wall found!!! (variation 2)";
                        break;
                    default:
                        comment = "???";
                }
                builder.append(" -- ").append(comment);
            } else {
                builder.append("StartWallState( ")
                        .append("pass_through: ")
                        .append(state >> 4)
                        .append(" )");
            }
        }
        
        return builder.toString();
    }
    
    public String[] debugTableAtState(int state) {
        String[] ret = new String[ImageProcessing.PosterColor.values().length];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = FsmRunner.debugState(table[state | i]);
        }
        
        return ret;
    }
    
    public String[] debugImageColumn(int[] posterizedImage, int col) {
        String[] ret = new String[imageHeight];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ImageProcessing.PosterColor.values()[posterizedImage[(i*imageWidth) + col]].toString();
        }
        return ret;
    }
}
