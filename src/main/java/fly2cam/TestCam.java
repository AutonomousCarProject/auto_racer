package fly2cam;

import fly2cam.FlyCamera;

public class TestCam extends FlyCamera {

    private static final int SimTile = 1,
            CamHi = 480, CamWi = 640;

    public TestCam() {

    }

    @Override
    public boolean Connect(int frameRate) {
        rose = CamHi;
        colz = CamWi;
        tile = SimTile;
        FrameNo = 0;
        errn = 0;

        return true;
    }
}
