package org.avphs.map;


import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
class Display extends JPanel {

    private boolean[][] map;

    Display(boolean[][] map) {
        this.map = map;
    }

    public void paintComponent(Graphics g) {
        for (int i = 0; i < map.length; i++) for (int j = 0; j < map[0].length; j++) if(map[i][j]) g.drawRect(i, j, 1, 1);
    }

}
