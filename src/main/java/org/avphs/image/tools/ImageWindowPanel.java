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
    private ArrayList<Polygon> selections;

    private Point lastPoint;
    private Point mousePos;

    private boolean pictureTaken;

    private BufferedImage subImage;


    public ImageWindowPanel(int windowWidth, int windowHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        selectionPoints = new ArrayList<>();
        selections = new ArrayList<>();

        displayImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        bufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        setSize(windowWidth, windowHeight);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void useImage(BufferedImage image) {
        displayImage = image;
    }

    public void finishImage() {

    }

    public void takePicture() {
        if (pixels != null) {
            int[] displayPixels = ((DataBufferInt) bufferImage.getRaster().getDataBuffer()).getData();
            System.arraycopy(pixels, 0, displayPixels, 0, pixels.length);

            BufferedImage tempImage = displayImage;
            displayImage = bufferImage;
            bufferImage = tempImage;
        }
    }

    public void finishSelection()
    {
        appState = AppState.Default;

        int[] xPoints = new int[selectionPoints.size()];
        int[] yPoints = new int[selectionPoints.size()];

        for (int i = 0; i < selectionPoints.size(); ++i) {
            xPoints[i] = selectionPoints.get(i).x;
            yPoints[i] = selectionPoints.get(i).y;
        }

        Polygon polygon = new Polygon(xPoints, yPoints, xPoints.length);

        int left = selectionPoints.get(0).x;
        int right = selectionPoints.get(0).x;
        int top = selectionPoints.get(0).y;
        int bottom = selectionPoints.get(0).y;

        for (Point point : selectionPoints) {
            left = Math.min(left, point.x);
            right = Math.max(right, point.x);
            top = Math.min(top, point.y);
            bottom = Math.max(bottom, point.y);
        }

        int width = right - left;
        int height = bottom - top;

        int[][] img = new int[width][height];

        //subImage = displayImage.getSubimage(left, top, width, height);

        for (int i = 0; i < displayImage.getWidth(); ++i) {
            for (int j = 0; j < displayImage.getHeight(); ++j) {
                if (polygon.contains(i, j)) {
                    img[width - i][height - j] = displayImage.getRGB(i, j);
                }
            }
        }

        selections.add(polygon);
        lastPoint = null;
        selectionPoints = null;
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

        if (appState == AppState.Selection) {
            if (lastPoint != null) {
                g.drawLine(lastPoint.x, lastPoint.y, mousePos.x, mousePos.y);
            }
        }

        if (selectionPoints != null) {
            g.setColor(Color.BLACK);
            for (Point point : selectionPoints) {
                if (lastPoint != null) {
                    g.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                }
                lastPoint = point;
            }
        }

        if (selections != null) {
            g.setColor(Color.GRAY);
            for (Polygon poly : selections) {
                g.fillPolygon(poly);
            }
        }

        if (subImage != null) {
            Insets insets = getInsets();
            //g.drawImage(subImage, insets.left, insets.top, windowWidth - insets.left - insets.right,
              //      windowHeight - insets.top - insets.bottom, null);
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
        if (appState == AppState.Selection) {
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
