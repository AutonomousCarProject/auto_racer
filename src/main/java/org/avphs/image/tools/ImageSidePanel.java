package org.avphs.image.tools;

import javax.swing.*;
import java.awt.*;

public class ImageSidePanel extends JPanel {

    public ImageSidePanel(ImageWindowPanel imageWindowPanel) {

        setLayout(new FlowLayout());

        JButton snapButton = new JButton("Take Picture");
        snapButton.setSize(100, 100);
        snapButton.addActionListener(actionEvent -> {
            imageWindowPanel.takePicture();
        });

        JButton selectButton = new JButton("Select Wall");
        selectButton.setSize(100, 100);
        selectButton.addActionListener(actionEvent -> {
            imageWindowPanel.setAppState(ImageWindowPanel.AppState.Selection);
        });

        JButton finishSelectionButton = new JButton("Finish Selection");
        snapButton.setSize(100, 100);
        snapButton.addActionListener(actionEvent -> {
            imageWindowPanel.setAppState(ImageWindowPanel.AppState.Default);
        });

        add(snapButton);
        add(selectButton);
        add(finishSelectionButton);
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
