package org.avphs.image.fsmgeneration;

import org.avphs.image.ImageProcessing;
import org.avphs.image.ThingIdentification;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class TableState extends State {
    private Table table;
    private String name;
    
    EnumMap<ImageProcessing.PosterColor, State> transitions;
    
    public TableState(Table table, String name) {
        this.transitions = new EnumMap<ImageProcessing.PosterColor, State>(ImageProcessing.PosterColor.class);
        this.table = table;
        this.name = name;
        
        State defaultState = table.getDefaultState();
        for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
            transitions.put(color, defaultState);
        }
    }
    
    public void copyTransitions(TableState otherState) {
        this.transitions = otherState.getTransitions();
    }

    public void bindPlusLoop(TableState state, ImageProcessing.PosterColor... color) {
        this.bind(state, color);
        state.bind(state, color);
    }
    
    public void bindPlusLoop(ThingStartState startState, ImageProcessing.PosterColor... color) {
        TableState state = startState.getPassThrough();
        
        this.bind(startState, color);
        state.bind(state, color);
    }
    
    public void bind(State state, ImageProcessing.PosterColor... colors) {
        for (ImageProcessing.PosterColor color : colors) {
            transitions.put(color, state);
        }
    }
    
    public void bindAll(State state) {
        bind(state, ImageProcessing.PosterColor.values());
    }
    
    public State getTransition(ImageProcessing.PosterColor color) {
        return transitions.get(color);
    }

    public EnumMap<ImageProcessing.PosterColor, State> getTransitions() {
        return transitions;
    }

    @Override
    public int asStateNumber() {
        return (table.getStateNumber(this) << ThingIdentification.STATE_NUMBER_OFFSET);
    }

    @Override
    public String getName() {
        return getName(false);
    }
    
    public String getName(boolean includeName) {
        if (includeName) {
            return table.getName() + ":" + name;
        } else {
            return name;
        }
    }

    public Table getTable() {
        return table;
    }

    public CharSequence debug(CharSequence indent) {
        StringBuilder builder = new StringBuilder();
        
        builder.append(indent);
        builder.append("TableState: ");
        builder.append(name);
        builder.append(" {\n");
        
        for (ImageProcessing.PosterColor color : ImageProcessing.PosterColor.values()) {
            builder.append(indent);
            builder.append("\t");
            builder.append(String.format("%-8s", color));
            builder.append(" --> ");
            builder.append(transitions.get(color).getName());
            builder.append("\n");
        }
        
        builder.append(indent);
        builder.append("}");
        
        return builder;
    }
}
