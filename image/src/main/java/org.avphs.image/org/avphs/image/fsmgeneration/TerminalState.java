package org.avphs.image.fsmgeneration;

import org.avphs.image.ThingIdentification;

abstract public class TerminalState extends State {
    static int baseWithInstruction(int instr) {
        int base = ThingIdentification.CODE_SPECIAL_TERMINAL;
        int shiftedInstr = instr << ThingIdentification.SPECIAL_INSTR_OFFSET;
        
        return (base | shiftedInstr);
    }
}
