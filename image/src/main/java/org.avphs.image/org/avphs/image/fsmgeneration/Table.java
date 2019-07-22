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

    public Table() {
        tableStates = new HashSet<>();
        stateNumbers = new HashMap<>();

        things = new HashSet<>();
        thingNumbers = new HashMap<>();
        thingStartStates = new HashMap<>();
        thingEndStates = new HashMap<>();

        defaultState = new FailState();

        initialState = newNamedState("initial_state");
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
        String name = "unnamed_state_" + tableStates.size();
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
        
        return null;
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
