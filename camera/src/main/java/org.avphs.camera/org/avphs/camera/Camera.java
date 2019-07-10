package org.avphs.camera;

public interface Camera {

    boolean nextFrame(byte[] pixels);

    boolean connect(int frameRate);

    void finish();

    int dimz();

    int pixTile();

    boolean live();

}
