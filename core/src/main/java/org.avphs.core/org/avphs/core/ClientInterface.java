package org.avphs.core;

public interface ClientInterface {
    void accelerate(boolean absolute, int angle);

    void steer(boolean absolute, int angle);

    void stop();

    byte[] getBayerImage();
}
