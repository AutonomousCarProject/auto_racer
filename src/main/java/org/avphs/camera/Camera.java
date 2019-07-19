package org.avphs.camera;

public interface Camera {

    void NextFrame();

    boolean Connect(int frameRate);

    int getSteerServoPin();

    int getSpeedServoPin();

    void Finish();

    int getCamDimensions();

    int getCamHeight();

    int getCamWidth();

    byte[] getBayerImage();

    byte[] getRawImage();

    int pixTile();

    boolean live();

}
