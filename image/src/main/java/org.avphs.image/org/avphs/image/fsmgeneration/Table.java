package org.avphs.image.fsmgeneration;

import org.avphs.image.ImageProcessing;

import java.util.*;

public class Table {
    private HashSet<TableState> tableStates;
    private HashMap<TableState, Integer> stateNumbers;

    private HashSet<Thing> things;
    private HashMap<Thing, Integer> thingNumbers;
    private HashMap<Thing, ThingStartState> thingStartStates;
    private HashMap<Thing, ThingEndState> thingEndStates;

    private TableState initialState;
    private State defaultState;
    
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
        thingStartStates = new HashMap<>();
        thingEndStates = new HashMap<>();

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

    public TableState newState() {
        String name = "state" + tableStates.size();
        return this.newNamedState(name);
    }

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

    public static Table combineTables(Table table1, Table table2) {
//        System.out.println(table1.debugTableStates("t1  "));
//        System.out.println(table2.debugTableStates("t2  "));

        System.out.println(table1.getInitialState().debug("t1  "));
        System.out.println(table2.getInitialState().debug("t2  "));

        HashMap<HashSet<TableState>, TableState> tableStateMapping = new HashMap<>();

        abstract class Pair {
            State s1;
            State s2;
            
            abstract void bind(State bindState);
            abstract State getOrigin();
        }
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

        ArrayDeque<Pair> queue = new ArrayDeque<>();
        for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
            queue.add(new ColorPair(newInitial, color, initial1.getTransition(color), initial2.getTransition(color)));
        }
        
        FailState failState = new FailState();
        TableState placeHolder = newTable.newNamedState("PLACEHOLDER");
        
        Class[] precedence = {
                ThingEndState.class, 
                ThingStartState.class, 
                TableState.class, 
                FailState.class
        };

        while (!queue.isEmpty()) {
            Pair p = queue.pop();
            State endBind = null;
            
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
                    ThingEndState s2ThingEndState = (ThingEndState) p.s2;
                    System.out.println("merging two `ThingEndState`s");
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
                    ThingStartState s2ThingStartState = (ThingStartState) p.s2;
                    System.out.println("merging two `ThingStartState`s");

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

                    queue.add(
                            new PairWithBeginThing(
                                    newThingStartState,
                                    s1ThingStartState.getPassThrough(),
                                    s2ThingStartState.getPassThrough()
                            )
                    );
                }
                else if (p.s2 instanceof TableState) {
                    TableState s2TableState = (TableState) p.s2;
                    
                    ThingStartState newThingStartState = new ThingStartState(
                            newTable,
                            newTable.newNamedThing("copyThing(" + s1ThingStartState.getThing().getName() + ")"),
                            placeHolder
                    );

                    endBind = newThingStartState;

                    queue.add(
                            new PairWithBeginThing(
                                    newThingStartState,
                                    s1ThingStartState.getPassThrough(),
                                    s2TableState
                            )
                    );
                } else {
                    endBind = placeHolder;
                    p.s2 = placeHolder;
                    queue.add(p);
                }
            }
            else if (p.s1 instanceof TableState) {
                TableState s1TableState = (TableState) p.s1;
                if (p.s2 instanceof TableState) {
                    TableState s2TableState = (TableState) p.s2;
                    HashSet<TableState> mapKey = new HashSet<>();
                    mapKey.add(s1TableState);
                    mapKey.add(s2TableState);

                    TableState foundExisting = null;
                    for (Map.Entry<HashSet<TableState>, TableState> entry : tableStateMapping.entrySet()) {
                        if (entry.getKey().equals(mapKey)) {
                            foundExisting = entry.getValue();
                            break;
                        }
                    }

                    if (foundExisting == null) {
                        TableState newChildTableState;
                        if (s2TableState.equals(placeHolder)) {
                            newChildTableState = newTable.newNamedState(
                                    "copied(" + s1TableState.getName(true) + ")"
                            );
                        } else {
                            newChildTableState = newTable.newNamedState(
                                    "merged(" + s1TableState.getName(true) + "+" + s2TableState.getName(true) + ")"
                            );
                        }
                        tableStateMapping.put(mapKey, newChildTableState);

                        endBind = newChildTableState;

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
                        endBind = foundExisting;
                    }
                } else {
                    endBind = placeHolder;
                    p.s2 = placeHolder;
                    queue.add(p);
                }
            } else {
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
                p.bind(endBind);
            }
        }

        return newTable;
    }

    public void generateImage() {
        this.asDotGraph().outputImage(this.name, System.getProperty("user.dir") + "/image/out/images");
    }

    public void generateImage(String path, String name) {
        this.asDotGraph().outputImage(name, path);
    }
    
    public Digraph asDotGraph() {
        Digraph digraph = new Digraph();
        for (TableState tableState : tableStates) {
            for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
                State destination = tableState.getTransition(color);
                if (destination instanceof ThingStartState) {
                    ThingStartState thingStartState = (ThingStartState) destination;
                    EdgeStatement entry = new EdgeStatement(tableState.getName(), thingStartState.getPassThrough().getName());
                    entry.addColor(color);
                    entry.addLabel(
                            "+ start("
                            + thingStartState.getThing().getName()
                            + ")"
                    );
                    
                    digraph.addEntry(entry);
                } else if (!(destination instanceof FailState)) {
                    EdgeStatement entry = new EdgeStatement(tableState.getName(), destination.getName());
                    entry.addColor(color);
                    
                    digraph.addEntry(entry);
                }
            }
        }
        
        return digraph;
    }

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
}
