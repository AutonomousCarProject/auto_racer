package org.avphs.image.fsmgeneration;

import org.avphs.image.ImageProcessing;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * a table describing an FSM.
 */
public class Table {
    /**
     * the {@link TableState}s in this table
     */
    private HashSet<TableState> tableStates;
    /**
     * a mapping of table states to integers. this is needed for converting the table object into an {@code int[]}.
     */
    private HashMap<TableState, Integer> stateNumbers;

    /**
     * things associated with this table. these are what this FSM is supposed to find in an image column
     */
    private HashSet<Thing> things;
    /**
     * mapping of things to numbers. used when the table is serialized
     */
    private HashMap<Thing, Integer> thingNumbers;

    /**
     * the state in which this table starts. when the table is serialized, this state always gets the state number 0
     */
    private TableState initialState;
    /**
     * the state used to fill newly created TableStates associated with this table
     */
    private State defaultState;

    /**
     * name of this table
     */
    private String name;
    
    public Table() {
        this("");
    }

    public Table(String name) {
        this.name = name;
        
        tableStates = new HashSet<>();
        stateNumbers = new HashMap<>();

        things = new HashSet<>();
        thingNumbers = new HashMap<>();

        defaultState = new FailState();

        initialState = newNamedState("initial");
    }

    public String getName() {
        return name;
    }

    public TableState getInitialState() {
        return initialState;
    }

    public void setInitialState(TableState initialState) {
        this.initialState = initialState;
    }

    public void setDefaultState(State s) {
        defaultState = s;
    }

    public State getDefaultState() {
        return defaultState;
    }

    public void addState(TableState state) {
        tableStates.add(state);
    }

    /**
     * make a new TableState with a default name
     * 
     * @return newly created TableState associated with this table
     */
    public TableState newState() {
        String name = "state" + tableStates.size();
        return this.newNamedState(name);
    }

    /**
     * make a new TableState with a specific name
     * 
     * @param name name for the TableState
     * @return newly created state
     */
    public TableState newNamedState(String name) {
        TableState s = new TableState(this, name);
        this.addState(s);
        return s;
    }

    public boolean hasState(TableState tableState) {
        return tableStates.contains(tableState);
    }

    public int getStateNumber(TableState tableState) {
        if (tableStates.contains(tableState)) {
            return stateNumbers.computeIfAbsent(tableState, _ts -> stateNumbers.size());
        } else {
            System.err.println("TableState not properly registered with Table");
            return -1;
        }
    }

    public void addThing(Thing thing) {
        things.add(thing);
    }

    /**
     * create a new thing with a specific name
     * 
     * @param name name for the Thing
     * @return newly created thing
     */
    public Thing newNamedThing(String name) {
        Thing t = new Thing(name);
        this.addThing(t);
        return t;
    }

    public Thing newThing() {
        String name = "unnamed_thing_" + things.size();
        return this.newNamedThing(name);
    }

    public int getThingNum(Thing thing) {
        if (things.contains(thing)) {
            return thingNumbers.computeIfAbsent(thing, _t -> thingNumbers.size());
        } else {
            System.err.println("Thing not properly registered with Table");
            return -1;
        }
    }

    public void clearNumbers() {
        stateNumbers.clear();
        thingNumbers.clear();
    }

    /**
     * convert this table object into an integer array describing the FSM for use in a runner function
     * 
     * @return an integer array that can be used by an FSM runner
     */
    public int[] generateTable() {
        this.clearNumbers();

        int[] intTable = new int[tableStates.size() * (16)];

        stateNumbers.put(initialState, 0);
        addVariants(intTable, initialState);

        for (TableState tableState : tableStates) {
            addVariants(intTable, tableState);
        }

        return intTable;
    }

    /**
     * write the necessary integers into an array to describe a TableState. (used when serializing)
     * 
     * @param intTable integer array into which to write
     * @param tableState the TableState to generate state codes for
     */
    public static void addVariants(int[] intTable, TableState tableState) {
        int stateNum = tableState.asStateNumber();

        for (ImageProcessing.PosterColor posterColor : ImageProcessing.PosterColor.values()) {
            State dest = tableState.getTransition(posterColor);
            if (dest instanceof TableState) {
                if (!tableState.getTable().hasState((TableState) dest)) {
                    System.err.println("TableState points to external Table");
                }
            }
            intTable[stateNum | posterColor.getCode()] = dest.asStateNumber();
        }

    }

    public String debugTableStates() {
        return debugTableStates("");
    }

    /**
     * generate an easier to read display of the TableStates of this table.
     * 
     * @param indent characters with which to indent each line
     * @return a nice string representing the TableStates associated with this table
     */
    public String debugTableStates(CharSequence indent) {
        StringJoiner joiner = new StringJoiner(
                ",\n",
                indent + "TableStates in Table: {\n",
                "\n" + indent + "}");

        for (TableState tableState : tableStates) {
            joiner.add(tableState.debug(indent + "\t"));
        }

        return joiner.toString();
    }

    /**
     * combine two tables (FSMs) to create a single FSM that can do the job of both
     * 
     * @param table1 first table
     * @param table2 second table
     * @return combined table
     */
    public static Table combineTables(Table table1, Table table2) {

        // mapping of two states (from the original tables) to a computed state in the new table
        HashMap<HashSet<TableState>, TableState> tableStateMapping = new HashMap<>();

        // pairing of two states that need to be combined
        abstract class Pair {
            State s1;
            State s2;
            
            abstract void bind(State bindState);
            abstract State getOrigin();
        }
        // pairing branching from a TableState on a color
        class ColorPair extends Pair {
            private TableState origin;
            private ImageProcessing.PosterColor color;

            public ColorPair(TableState origin, ImageProcessing.PosterColor color, State s1, State s2) {
                this.origin = origin;
                this.color = color;
                this.s1 = s1;
                this.s2 = s2;
            }
            
            public void bind(State bindState) {
                origin.bind(
                        bindState,
                        this.color
                );
            }

            State getOrigin() {
                return origin;
            }
        }
        // pairing after branch that recorded the bottom of a wall
        class PairWithBeginThing extends Pair {
            private ThingStartState origin;
            
            public PairWithBeginThing(ThingStartState origin, TableState s1, TableState s2) {
                this.origin = origin;
                this.s1 = s1;
                this.s2 = s2;
            }
            
            public void bind(State bindState) {
                if (bindState instanceof TableState) {
                    origin.setPassThrough((TableState) bindState);
                } else {
                    System.err.println("this design is horrible and you should feel bad");
                }
            }

            State getOrigin() {
                return origin;
            }
        }

        // combined table
        Table newTable = new Table(
                "t("
                        + table1.getName()
                        + "+"
                        + table2.getName()
                        + ")"
        );
        TableState newInitial = newTable.getInitialState();
        TableState initial1 = table1.getInitialState();
        TableState initial2 = table2.getInitialState();

        // this holds all the pairings that need to be resolved (merged)
        ArrayDeque<Pair> queue = new ArrayDeque<>();
        
        // manually add the pairs for the two original initial states
        for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
            queue.add(new ColorPair(newInitial, color, initial1.getTransition(color), initial2.getTransition(color)));
        }
        
        FailState failState = new FailState();
        // a substitute for null that is more convenient to debug 
        // (I use getName() etc. when debugging and that doesn't work with null)
        TableState placeHolder = newTable.newNamedState("PLACEHOLDER");
        
        // ordering for the various different types of state that need to be handled.
        Class[] precedence = {
                ThingEndState.class, 
                ThingStartState.class, 
                TableState.class, 
                FailState.class
        };

        while (!queue.isEmpty()) {
            // get a pair to resolve
            Pair p = queue.pop();
            // every pair resolution ends with a Pair.bind()
            // this holds the destination of the bind
            State endBind = null;
            
            // all this code makes sure that the two states in the pair are ordered in a certain way
            // based on their type, which reduces the number of variations that need to be handled.
            // the ordering is defined in `precedence`
            int s1Rank = -1;
            int s2Rank = -1;
            for (int i = precedence.length - 1; i >= 0; i--) {
                if (precedence[i].isInstance(p.s1)) {
                    s1Rank = i;
                }
                if (precedence[i].isInstance(p.s2)) {
                    s2Rank = i;
                }
            }
            if (s1Rank > s2Rank) {
                State temp = p.s1;
                p.s1 = p.s2;
                p.s2 = temp;
            }
            
            if (p.s1 instanceof ThingEndState) {
                ThingEndState s1ThingEndState = (ThingEndState) p.s1;
                if (p.s2 instanceof ThingEndState) {
                    // the pair is two ThingEndStates.
                    // if this happens, the two tables have an identical algorithm for finding something
                    
                    ThingEndState s2ThingEndState = (ThingEndState) p.s2;
                    System.out.println("merging two `ThingEndState`s");
                    
                    // combine the ThingEndStates into one
                    endBind = new ThingEndState(
                            new Thing(
                                    "comboThing("
                                            + s1ThingEndState.getThing().getName()
                                            + "+"
                                            + s2ThingEndState.getThing().getName()
                                            + ")"
                            ),
                            newTable
                    );
                } 
                else {
                    // the pair is a ThingEndState and something else.
                    // the ThingEndState overrides the other state (because if we are at a ThingEndState, that
                    // means the FSM found the end of a thing (e.g. the top of a wall) so we don't need to keep
                    // going)
                    
                    endBind = new ThingEndState(
                            new Thing("copyThing(" + s1ThingEndState.getThing().getName() + ")"),
                            newTable
                    );
                    
                    if (!(p.s2 instanceof FailState)) {
                        System.out.println("overwriting " + p.s1.getName() + " with " + endBind.getName());
                    }
                }
            }
            else if (p.s1 instanceof ThingStartState) {
                ThingStartState s1ThingStartState = (ThingStartState) p.s1;
                if (p.s2 instanceof ThingStartState) {
                    // the pair is two ThingStartStates.
                    // this means two things in the tables start the same way
                    
                    ThingStartState s2ThingStartState = (ThingStartState) p.s2;
                    System.out.println("merging two `ThingStartState`s");

                    // combine the ThingStartStates
                    ThingStartState newThingStartState = new ThingStartState(
                            newTable,
                            newTable.newNamedThing(
                                    "comboThing("
                                            + s1ThingStartState.getThing().getName()
                                            + "+"
                                            + s2ThingStartState.getThing().getName()
                                            + ")"
                            ),
                            placeHolder
                    );

                    endBind = newThingStartState;

                    // we also need to combine the pass-through TableStates
                    queue.add(
                            new PairWithBeginThing(
                                    newThingStartState,
                                    s1ThingStartState.getPassThrough(),
                                    s2ThingStartState.getPassThrough()
                            )
                    );
                }
                else if (p.s2 instanceof TableState) {
                    // the pair is a ThingStartState and a TableState
                    
                    TableState s2TableState = (TableState) p.s2;
                    
                    // the result of the merge will be a ThingStartState
                    ThingStartState newThingStartState = new ThingStartState(
                            newTable,
                            newTable.newNamedThing("copyThing(" + s1ThingStartState.getThing().getName() + ")"),
                            placeHolder
                    );

                    endBind = newThingStartState;

                    // and the pass-through needs to be merged with the p.s2 TableState
                    queue.add(
                            new PairWithBeginThing(
                                    newThingStartState,
                                    s1ThingStartState.getPassThrough(),
                                    s2TableState
                            )
                    );
                } else {
                    // the pair is a ThingStartState and a FailState
                    
                    // the ThingStartState overwrites the FailState.
                    // this is done (in this algorithm) by pairing the ThingStartState with the placeholder, which
                    // facilitates copying the rest of the tree that branches from this state
                    endBind = placeHolder;
                    p.s2 = placeHolder;
                    queue.add(p);
                }
            }
            else if (p.s1 instanceof TableState) {
                TableState s1TableState = (TableState) p.s1;
                if (p.s2 instanceof TableState) {
                    // the pair is two TableStates
                    
                    TableState s2TableState = (TableState) p.s2;
                    
                    // create a HashSet containing these two states for use in `tableStateMapping`
                    HashSet<TableState> mapKey = new HashSet<>();
                    mapKey.add(s1TableState);
                    mapKey.add(s2TableState);

                    TableState foundExisting = null;
                    // check if these two TableStates have been merged before during this function
                    for (Map.Entry<HashSet<TableState>, TableState> entry : tableStateMapping.entrySet()) {
                        if (entry.getKey().equals(mapKey)) {
                            foundExisting = entry.getValue();
                            break;
                        }
                    }

                    if (foundExisting == null) {
                        // this pairing of TableStates has not been seen before
                        
                        TableState newChildTableState;
                        if (s2TableState.equals(placeHolder)) {
                            // 'merging' with the placeholder is just copying
                            newChildTableState = newTable.newNamedState(
                                    "copied(" + s1TableState.getName(true) + ")"
                            );
                        } else {
                            // make new child state whose transitions are a combination of the parents'
                            newChildTableState = newTable.newNamedState(
                                    "merged(" + s1TableState.getName(true) + "+" + s2TableState.getName(true) + ")"
                            );
                        }
                        // record this pairing as encountered
                        tableStateMapping.put(mapKey, newChildTableState);

                        // the child state is what gets bound in the new table
                        endBind = newChildTableState;

                        // add the transitions to the queue to be resolved
                        for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
                            queue.add(
                                    new ColorPair(
                                            newChildTableState,
                                            color,
                                            s1TableState.getTransition(color),
                                            s2TableState.getTransition(color)
                                    )
                            );
                        }
                    } else {
                        // this pairing of TableStates *has* been seen before
                        
                        // do not add anything more to the queue; this branch is done
                        endBind = foundExisting;
                    }
                } else {
                    // the pair is a TableState and a FailState
                    
                    // use the placeholder to traverse+copy the transitions of this TableState
                    endBind = placeHolder;
                    p.s2 = placeHolder;
                    queue.add(p);
                }
            } else {
                // FailState + FailState = FailState
                endBind = failState;
            }

            if (endBind == null) {
                System.err.println("endBind is null you idiot");
            } else {
                System.out.println(
                        "Binding "
                                + p.getOrigin().getName()
//                                + p.getOrigin().hashCode()
                                + " --> "
                                + endBind.getName()
//                                + endBind.hashCode()
                );
                // do the bind
                p.bind(endBind);
            }
        }

        // table is now all built and bound, so return it
        return newTable;
    }

    /**
     * creates and saves an svg image of this FSM. the table name is used for the file name,
     * and the path is /image/out/images/_
     */
    public void generateImage() {
        this.asDotGraph().outputImage(this.name, System.getProperty("user.dir") + "/image/out/images");
    }

    public void generateImage(String path, String name) {
        this.asDotGraph().outputImage(name, path);
    }

    /**
     * @return a Digraph object representing this fsm
     */
    public Digraph asDotGraph() {
        Digraph digraph = new Digraph();
        for (TableState tableState : tableStates) {
            for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
                State destination = tableState.getTransition(color);
                if (destination instanceof ThingStartState) {
                    // ThingStartStates are displayed as labels on edges instead of as nodes
                    ThingStartState thingStartState = (ThingStartState) destination;
                    
                    // add an edge pointing to the passThrough
                    EdgeStatement entry = new EdgeStatement(
                            tableState.getName(), 
                            thingStartState.getPassThrough().getName()
                    );
                    entry.addColor(color);
                    
                    // and label it as starting a thing
                    entry.addLabel(
                            "+ start("
                            + thingStartState.getThing().getName()
                            + ")"
                    );
                    
                    digraph.addEntry(entry);
                } else if (!(destination instanceof FailState)) {  // don't draw edges for FailStates
                    EdgeStatement entry = new EdgeStatement(tableState.getName(), destination.getName());
                    entry.addColor(color);
                    
                    digraph.addEntry(entry);
                }
            }
        }
        
        // the initial state of the FSM is displayed as a diamond shaped node
        NodeStatement initialAttributes = new NodeStatement(initialState.getName(false));
        initialAttributes.addAttribute("shape", "diamond");
        
        digraph.addNodeStatement(initialAttributes);
        
        return digraph;
    }

    /**
     * save this table to a file as an integer array. (all thing name and state name data is lost)
     * 
     * @param path the path to save the file to
     */
    public void saveToFile(String path) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(
                                    path
                            ),
                            StandardCharsets.UTF_8
                    )
            );
            
            int[] tableData = this.generateTable();
            for (int tableDatum : tableData) {
                writer.append(Integer.toString(tableDatum));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * make a nice string to display the bits of a java int
     * 
     * @param i the int to print (haha that rhymes)
     * @return the string, duh
     */
    public static String niceIntBinaryFormat(int i) {
        StringBuilder builder = new StringBuilder();

        String unjust = Integer.toBinaryString(i);
        String justified = "0".repeat(32 - unjust.length()) + unjust;
        for (int j = 0; j < 7; j++) {
            builder.append(justified, j * 4, (j * 4) + 4);
            builder.append("_");
        }
        builder.append(justified.substring(28));

        return builder.toString();
    }

    /**
     * makes a nicely formatted string for viewing the integer-array-form of a table
     *
     * @param intTable the int array to print today (k that one's maybe a stretch)
     * @return the nice string
     */
    public static String niceFormat(int[] intTable) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < intTable.length; i++) {

            if (i % 16 < ImageProcessing.PosterColor.values().length) {
                builder.append(String.format("%-12s", ImageProcessing.PosterColor.values()[i % 16]));
                builder.append(": ");
                builder.append(niceIntBinaryFormat(intTable[i]));
            } else {
                builder.append("-");
            }

            builder.append("\n");
        }

        return builder.toString();
    }
}
