package org.avphs.sbcio;

import java.util.function.Consumer;
import java.util.function.Function;

public class ArduinoData {
    public int count;
    public Consumer<Void> closeFunc;

    public ArduinoData(int count, Consumer<Void> closeFunc) {
        this.count = count;
        this.closeFunc = closeFunc;
    }
}
