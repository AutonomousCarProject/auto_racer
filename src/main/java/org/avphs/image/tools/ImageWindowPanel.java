package org.avphs.image.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

public class ImageWindowPanel extends JPanel implements MouseListener, MouseMotionListener {

    private int windowWidth, windowHeight;
    private int[] pixels;

    private BufferedImage displayImage;
    private BufferedImage bufferImage;

    private AppState appState;

    private ArrayList<Point> selectionPoints;

    private Point lastPoint;
    private Point mousePos;

    private boolean pictureTaken;

    private BufferedImage takenImage;

    public ImageWindowPanel(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        selectionPoints = new ArrayList<>();

        displayImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        setSize(windowWidth, windowHeight);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void takePicture()
    {
        if (pixels != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(pixels, 0, displayPixels, 0, pixels.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;
        }
    }

    public void setAppState(AppState state) {
        appState = state;
    }

    public void giveCameraImage(int[] image) {
        pixels = image;
    }

    @Override
    public void update(Graphics g) {
        super.update(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //windowWidth = getWidth();
        //windowHeight = getHeight();
        if (pixels != null) {
            Insets insets = getInsets();
            g.drawImage(displayImage, insets.left, insets.top, windowWidth - insets.left - insets.right,
                    windowHeight - insets.top - insets.bottom, null);

        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));

        if (lastPoint != null)
        {
            g.drawLine(lastPoint.x, lastPoint.y, mousePos.x, mousePos.y);
        }

        if (selectionPoints != null)
        {
            g.setColor(Color.BLACK);
            for (Point point : selectionPoints)
            {
                if (lastPoint != null)
                {
                    g.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                }
                lastPoint = point;
            }
        }

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(windowWidth, windowHeight);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        if (appState == AppState.Selection)
        {
            selectionPoints.add(mouseEvent.getPoint());
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        mousePos = mouseEvent.getPoint();
    }

    public enum AppState {
        Default,
        Selection
    }

}
