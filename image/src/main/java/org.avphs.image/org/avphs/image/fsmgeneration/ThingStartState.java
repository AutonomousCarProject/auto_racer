package org.avphs.image.fsmgeneration;

import org.avphs.image.ThingIdentification;

public class ThingStartState extends State {
    Thing thing;
    TableState passThrough;
    Table table;
    
    public ThingStartState(Table table, Thing thing, TableState passThrough) {
        this.table = table;
        this.thing = thing;
        this.passThrough = passThrough;
    }

    public TableState getPassThrough() {
        return passThrough;
    }

    @Override
    public int asStateNumber() {
        int topHalf = passThrough.asStateNumber();
        return (topHalf | ThingIdentification.CODE_SPECIAL_PASSTHROUGH | ThingIdentification.CODE_SAVE_TO_REG_0);
    }
}
