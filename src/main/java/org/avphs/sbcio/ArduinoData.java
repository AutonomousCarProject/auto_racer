package org.avphs.sbcio;

import java.util.function.Consumer;

public class ArduinoData {
    private int count;
    public Consumer<Void> closeFunc;

    public ArduinoData(int count, Consumer<Void> closeFunc) {
        this.count = count;
        this.closeFunc = closeFunc;
    }

    public int getOdomCount() {
        return count;
    }

    public void setOdomCount(int add) {
        count += add;
    }
}
