package org.avphs.coreinterface;

public interface ClientInterface {
    void accelerate(boolean absolute, int angle);

    void steer(boolean absolute, int angle);

    void stop();

    byte[] getCameraImage();

}
