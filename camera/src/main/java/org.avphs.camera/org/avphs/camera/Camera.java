package org.avphs.camera;

public interface Camera {

    void fetchNextFrame();

    boolean connect(int frameRate);

    int getSteerServoPin();

    int getSpeedServoPin();

    void finish();

    int getCamDimensions();

    int getCamHeight();

    int getCamWidth();

    byte[] getBayerImage();

    int pixTile();

    boolean live();

}
