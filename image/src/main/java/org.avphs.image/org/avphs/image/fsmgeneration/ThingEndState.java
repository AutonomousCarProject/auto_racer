package org.avphs.image.fsmgeneration;

import org.avphs.image.ThingIdentification;

public class ThingEndState extends TerminalState {
    Thing thing;
    Table table;
    
    public ThingEndState(Thing thing, Table table) {
        this.thing = thing;
        this.table = table;
    }

    @Override
    public int asStateNumber() {
        int base = baseWithInstruction(ThingIdentification.CODE_SAVE_WALL);
        
        int thingNum = table.getThingNum(thing);
        int mask = (1 << ThingIdentification.WALL_TYPE_SIZE) - 1;
        thingNum = thingNum & mask;
        thingNum = thingNum << ThingIdentification.WALL_TYPE_OFFSET;
        
        return (base | thingNum);
    }
}
