package org.avphs.image.fsmgeneration;

/**
 * a state in an FSM
 */
public abstract class State {
    abstract public int asStateNumber();
    abstract public String getName();
}
