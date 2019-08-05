package org.avphs.image.tools;

import javax.swing.*;
import java.awt.*;

public class ImageSidePanel extends JPanel {

    public ImageSidePanel(ImageWindowPanel imageWindowPanel) {

        setLayout(new FlowLayout());

        var selectButton = new JButton("Select Wall");
        selectButton.setSize(100, 100);
        selectButton.addActionListener(actionEvent -> {
            imageWindowPanel.setAppState(ImageWindowPanel.AppState.Selection);
        });

        var snapButton = new JButton("Take Picture");
        snapButton.setSize(100, 100);
        snapButton.addActionListener(actionEvent -> {
            imageWindowPanel.takePicture();
        });

        add(snapButton);
        add(selectButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        repaint();
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 300);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
