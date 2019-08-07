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
        finishSelectionButton.setSize(100, 100);
        finishSelectionButton.addActionListener(actionEvent -> {
            imageWindowPanel.finishSelection();
        });

        JButton finishImageButton = new JButton("Finished Marking Image");
        finishImageButton.setSize(100, 200);
        finishImageButton.addActionListener(actionEvent -> {
            imageWindowPanel.finishImage();
        });

        add(snapButton);
        add(selectButton);
        add(finishSelectionButton);
        add(finishImageButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

invalidate();

        repaint();
    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 300);
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
