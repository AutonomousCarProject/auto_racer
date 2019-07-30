package org.avphs.image.fsmgeneration;

import org.avphs.image.ImageProcessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class TomsTubeWall {
    static ImageProcessing.PosterColor[] series;
    static TableState[] states;
    static FailState failState;

    public static void main(String[] args) {
        Table tomsTable = new Table("tomsTable");
        Thing tubeWall = tomsTable.newNamedThing("tube_wall");
        FailState fail = new FailState();
        State success = new ThingEndState(tubeWall, tomsTable);
        failState = fail;

        ImageProcessing.PosterColor c0 = ImageProcessing.PosterColor.BLACK;
        ImageProcessing.PosterColor c1 = ImageProcessing.PosterColor.GREY1;
        ImageProcessing.PosterColor c2 = ImageProcessing.PosterColor.GREY2;
        ImageProcessing.PosterColor c3 = ImageProcessing.PosterColor.GREY2;
        ImageProcessing.PosterColor c4 = ImageProcessing.PosterColor.GREY3;
        ImageProcessing.PosterColor c5 = ImageProcessing.PosterColor.GREY3;
        ImageProcessing.PosterColor c6 = ImageProcessing.PosterColor.GREY4;
        ImageProcessing.PosterColor c7 = ImageProcessing.PosterColor.WHITE;
        
        series = new ImageProcessing.PosterColor[]{c0, c1, c2, c3, c4, c5, c6, c7};
//        series = ImageProcessing.PosterColor.values();

        TableState[] s = new TableState[51];
        for (int i = 0; i < 51; i++) {
            s[i] = tomsTable.newNamedState("state_" + i);
        }
        
        states = s;

        HashSet<Character> numbers = new HashSet<>();
        for (char c : "1234567890".toCharArray()) {
            numbers.add(c);
        }

        try {
            Scanner scanner = new Scanner(new File("C:\\Users\\TEST\\IdeaProjects\\auto_racer\\image\\src\\main\\java\\org.avphs.image\\org\\avphs\\image\\fsmgeneration\\SampleTubeWallFSM.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try {
                    if (line.length() > 0 && numbers.contains(line.charAt(0))) {
                        int dotPos = line.indexOf(".");
                        int eqPos = line.indexOf("=");
                        int endDest = line.indexOf(" ", eqPos + 2);

                        int currentState = Integer.parseInt(line.substring(0, dotPos));
                        int colorValue = Integer.parseInt(line.substring(dotPos + 1, eqPos - 1));
                        State destState = fail;
                        if (numbers.contains(line.charAt(eqPos + 2))) {
                            int destStateNum = Integer.parseInt(line.substring(eqPos + 2, endDest));
                            destState = s[destStateNum];
                            if (line.charAt(endDest+1) == '+') {
                                destState = new ThingStartState(tomsTable, tubeWall, (TableState) destState);
                            }
                        } else {
                            if (line.charAt(eqPos+2) == 'f') {
                                destState = fail;
                            } else if (line.charAt(eqPos+2) == 'a') {
                                destState = success;
                            }
                        }
                        s[currentState].bind(destState, series[colorValue]);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        tomsTable.setInitialState(s[1]);
        System.out.println(tomsTable.debugTableStates());
        tomsTable.generateImage();
        tomsTable.saveToFile("C:\\Users\\TEST\\IdeaProjects\\auto_racer\\image\\out\\foo.table");
    }
    
}
