package org.avphs.image.fsmgeneration;

import org.avphs.image.ThingIdentification;

/**
 * a state for failure
 */
public class FailState extends TerminalState {
    @Override
    public int asStateNumber() {
        return baseWithInstruction(ThingIdentification.CODE_FAIL);
    }

    @Override
    public String getName() {
        return "[FailState]";
    }
}
