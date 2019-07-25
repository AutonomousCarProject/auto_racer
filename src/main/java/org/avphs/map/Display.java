package org.avphs.map;


import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
class Display extends JPanel {

    static float scale = .25f;

    private boolean[][] map;

    Display(boolean[][] map) {
        this.map = map;
    }

    public void paintComponent(Graphics g) {
        for (int i = 0; i < (int)(map.length*scale); i++)
            for (int j = 0; j < (int)(map[0].length*scale); j++)
                if(map[(int)(i/scale)][(int)(j/scale)])
                    g.drawRect(i, j, 1, 1);
    }

}
