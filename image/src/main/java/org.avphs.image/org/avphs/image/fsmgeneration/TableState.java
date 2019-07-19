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

    @Override
    public int asStateNumber() {
        return (table.getStateNumber(this) << ThingIdentification.STATE_NUMBER_OFFSET);
    }
}
