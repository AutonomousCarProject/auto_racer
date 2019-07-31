package org.avphs.map;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class MapEditor implements MouseMotionListener, ActionListener {//TODO: FIX SCALING
    private Map mapster12345;
    JFrame frame;
    private boolean drawingMode_On = false;
    private int BrushSize = 11;
    public boolean editorOpen = false;


    JButton drawOrErase = new JButton("Erase Mode");
    JButton setBrushSize = new JButton("Brush Size: " + BrushSize);
    JButton saveMap = new JButton("Save");

    public MapEditor(Map map)
    {
        mapster12345 = map;
    }

    public void LaunchMapEditor()
    {
        frame = new JFrame("Map Editor");
        frame.setSize((int)(mapster12345.getXDim()*Display.scale), (int)(mapster12345.getyDim()*Display.scale));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.add(new Display(mapster12345.getMap()), BorderLayout.CENTER);

        Container con = new Container(); con.setLayout(new GridLayout(2, 1));con.add(drawOrErase); con.add(setBrushSize);
        frame.add(con, BorderLayout.NORTH); frame.add(saveMap, BorderLayout.SOUTH);
        drawOrErase.addActionListener(this); setBrushSize.addActionListener(this); saveMap.addActionListener(this);
        frame.addMouseMotionListener(this);
        frame.setVisible(true);
        editorOpen = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(setBrushSize))
        {
            BrushSize += 2;
            if (BrushSize == 41)
            {
                BrushSize = 11;
            }
            setBrushSize.setText("Brush Size: " + BrushSize);
        }
        else if (e.getSource().equals(drawOrErase))
        {
            if (drawingMode_On)
            {
                drawingMode_On = false;
                drawOrErase.setText("Erase Mode On");
            }
            else
            {
                drawingMode_On = true;
                drawOrErase.setText("Draw Mode On");
            }


        }
        else if (e.getSource().equals(saveMap))
        {

        }
    }

    public void smartFix()
    {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.println(e.getX() + ", " + e.getY() + "<----------------------------------");
        mapster12345.setValueAtIndex_XL(e.getX(), (e.getY()) - 100, drawingMode_On, BrushSize);
        frame.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
