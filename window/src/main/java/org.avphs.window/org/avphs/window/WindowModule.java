package org.avphs.window;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class WindowModule extends JFrame implements CarModule {

    @Override
    public Class[] getDependencies() {
        return new Class[0];
    }

    @Override
    public void init(CarModule[] dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[0];
    }

    @Override
    public void run() {

    }
}
