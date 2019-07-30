package org.avphs.image;

import org.avphs.image.fsmgeneration.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TomFsmFileFormatParser {
//    public static final Pattern linePattern = Pattern.compile(
//            "^(?<source>\\d+)\\.(?<color>\\d+)\\s*=\\s*(?<destination>\\d+)(?:\\s*\\+\\s*(?<action>(?:(?<wall>\\d+)(?:=(?<extra>\\w+)))|\\w+))?(?:\\s*--\\s*(?<comment>.*))?"
//    );
    public static final Pattern linePattern = Pattern.compile(
            "^(?<source>\\d+)\\.(?<color>\\d+)\\h*=\\h*(?:(?<terminal>[a-zA-Z]+)|(?<destination>\\d+)(?:\\h*\\+\\h*(?<action>(?:(?<wall>\\d+)(?:=(?<extra>\\w+)))|\\w+))?)(?:\\h*--\\h*(?<comment>.*))?"
    );

    /**
     * <p>reads a text file describing a finite state machine in the text format that Tom uses, 
     * and returns a {@link Table} object</p>
     * 
     * @param path file path of the text file
     * @return the table generated from the file
     */
    public static Table parseFile(String path) throws Exception {
        return parseFile(path, "1", ImageProcessing.PosterColor.values());
    }

    /**
     * <p>reads a text file describing a finite state machine in the text format that Tom uses, 
     * and returns a {@link Table} object</p>
     * 
     * @param path file path of the text file
     * @param initialStateName name of the first state in the machine
     * @param colors array of {@link org.avphs.image.ImageProcessing.PosterColor}s 
     *               to map from numbers used in the file to the colors to use in the table
     * @return the table generated from the file
     */
    public static Table parseFile(String path, String initialStateName, ImageProcessing.PosterColor[] colors) throws Exception {
        Scanner scanner = new Scanner(new File(path));
        ArrayList<Matcher> matchingLines = new ArrayList<>();
        
        // sets for collecting all the unique stuff in the file
        HashSet<String> fileSourceStates = new HashSet<>();
        HashSet<String> fileDestinationStates = new HashSet<>();
        HashSet<String> fileColors = new HashSet<>();
        HashSet<String> fileThingIds = new HashSet<>();
        
        // read file
        while (scanner.hasNextLine()) {
            Matcher matcher = linePattern.matcher(scanner.nextLine());
            if (matcher.matches()) {
                // this line has valid stuffs on it
                
                // record stuffs
                fileSourceStates.add(matcher.group("source"));
                fileDestinationStates.add(matcher.group("destination"));
                fileColors.add(matcher.group("color"));
                fileThingIds.add(matcher.group("wall"));
                
                // line will be used later for the linking
                matchingLines.add(matcher);
            }
        }

        if (!fileSourceStates.contains(initialStateName)) {
            // function caller messed up
            throw new Exception("the initial state name given does not appear in the file");
        }

        // map colors in the file to PosterColors
        HashMap<String, ImageProcessing.PosterColor> colorMap = new HashMap<>();
        for (String fileColor : fileColors) {
            int parsedInt = Integer.parseInt(fileColor);
            if (parsedInt < colors.length) {
                colorMap.put(fileColor, colors[parsedInt]);
            } else {
                throw new Exception("a color appears in the file but is not defined in the colors array");
            }
        }

        if (!fileSourceStates.containsAll(fileDestinationStates)) {
            // Tom messed up
            throw new Exception("a destination is referenced but not defined in the file");
        }
        
        // make Table and FailState
        Table tomTable = new Table("tomTable");
        FailState fail = new FailState();
        
        // if the table describes how to find a single type of thing, these objects are used
        Thing singleThing = tomTable.newNamedThing("tomTableThing");
        ThingEndState singleSuccess = new ThingEndState(
                singleThing,
                tomTable
        );
        
        // create TableState objects for every state in the file
        HashMap<String, TableState> tableStateMap = new HashMap<>();
        for (String sourceState : fileSourceStates) {
            tableStateMap.put(sourceState, tomTable.newNamedState("s-" + sourceState));
        }

        // create Thing objects for each thing id in the file
        HashMap<String, Thing> tableThingMap = new HashMap<>();
        for (String thingId : fileThingIds) {
            tableThingMap.put(thingId, tomTable.newNamedThing("thing" + thingId));
        }
        
        // set the initial state for the table
        tomTable.setInitialState(
                tableStateMap.get(initialStateName)
        );

        // now make the bindings described by the lines
        for (Matcher line : matchingLines) {
            
            // every correct line will have a source and a color
            TableState source = tableStateMap.get(line.group("source"));
            ImageProcessing.PosterColor color = colorMap.get(line.group("color"));
            if (line.group("destination") == null) {
                // there isn't a number after the "="
                
                String terminal = line.group("terminal");
                if (terminal.startsWith("s")) {
                    // e.g. "2.4 = success"
                    source.bind(
                            singleSuccess,
                            color
                    );
                } else if (terminal.startsWith("f")) {
                    // e.g. "2.4 = fail"
                    source.bind(
                            fail,
                            color
                    );
                } else {
                    throw new Exception("bad value for terminal group");
                }
            } else {
                // there is a number immediately after the "="
                
                TableState destination = tableStateMap.get(line.group("destination"));
                Thing thing = tableThingMap.get(line.group("wall"));
                if (thing != null) {
                    // a wall id is specified after the destination
                    
                    // e.g. "1.3 = 2 + 8=bot"
                    source.bind(
                            new ThingStartState(
                                    tomTable,
                                    thing,
                                    destination
                            ),
                            color
                    );
                } else {
                    // no wall id is specified
                    
                    String action = line.group("action");
                    if (action != null) {
                        // an action is specified after the destination
                        
                        if (action.startsWith("bot")) {
                            // e.g. "1.2 = 6 + bottom"
                            source.bind(
                                    new ThingStartState(
                                            tomTable,
                                            singleThing,
                                            destination
                                    ),
                                    colors
                            );
                        } else {
                            throw new Exception("bad value for action group");
                        }
                    } else {
                        // the simple case with nothing special after the destination
                        
                        // e.g. "2.7 = 45"
                        source.bind(
                                destination,
                                color
                        );
                    }
                }
            }
        }
        
        return tomTable;
    }
}
