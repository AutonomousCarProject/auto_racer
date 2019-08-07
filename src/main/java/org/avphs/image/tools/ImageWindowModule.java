package org.avphs.image.tools;

import org.avphs.camera.Camera;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.sbcio.ArduinoData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class ImageWindowModule extends JFrame implements CarModule {

    private Camera camera;
    private CarData carData;
    private ImageWindowPanel imageWindowPanel;
    private ImageSidePanel imageSidePanel;

    private int camWidth;
    private int camHeight;

    private BufferedImage givenImage = null;

    public ImageWindowModule(CarData carData) {
        this.carData = carData;
    }

    public ImageWindowModule() {
    }

    public void useGivenImage(BufferedImage image) {
        givenImage = image;
    }

    @Override
    public void init(CarData carData) {
        carData.addData("window", this);
        try {
            camera = (Camera) carData.getModuleData("camera");
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        camWidth = camera.getCamWidth();
        camHeight = camera.getCamHeight();

        imageWindowPanel = new ImageWindowPanel(camWidth, camHeight);
        imageSidePanel = new ImageSidePanel(imageWindowPanel);

        if (givenImage != null)
            imageWindowPanel.useImage(givenImage);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ((ArduinoData) carData.getModuleData("arduino")).closeFunc.accept(null);
                System.exit(0);
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(camWidth, camHeight + 25);
        setResizable(true);
        setLocationRelativeTo(null);
        setIgnoreRepaint(true);

        setLayout(new BorderLayout());

        add(imageSidePanel, BorderLayout.EAST);
        add(imageWindowPanel, BorderLayout.WEST);
        pack();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(info.getClassName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    @Override
    public void update(CarData carData) {
        update(getGraphics());
    }

    public void setWindowImage(int[] image) {
        imageWindowPanel.giveCameraImage(image);
    }
}
