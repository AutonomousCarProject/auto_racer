package org.avphs.camera;

import fly2cam.FlyCamera;

public class CameraTest {

    public CameraTest() {
        FlyCamera flyCamera = new FlyCamera();
        flyCamera.Connect(2);

        flyCamera.NextFrame();
        flyCamera.Finish();
    }

    public static void main(String[] args) {
        new CameraTest();
    }

}
