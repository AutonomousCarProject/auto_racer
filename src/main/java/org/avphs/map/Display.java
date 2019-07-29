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
        for (int i = 0; i < (int)(map.length*scale); i++) {
            for (int j = 0; j < (int) (map[0].length * scale); j++) {
                int invScale = (int) (1 / scale);
                int count = 0;
                for (int o = 0; o < invScale; o++) {
                    for(int p = 0; p < invScale; p++){
                        if(map[(int)(i/scale)+o][(int)(j/scale)+p]){
                            count++;
                        }
                    }
                }
                if(count > 0) {
                    int h = 255 - (int) (255 * (count / Math.pow(invScale, 2)));
                    g.setColor(new Color(h,h,h));
                    g.fillRect(i, j, 1, 1);
                }
            }
        }
    }

}
