package org.avphs.camera;

import org.avphs.traksim.DriverCons;
import org.avphs.traksim.TrakSim;

public class FakeCamera extends CameraBase {
    private static final int CamHight = DriverCons.D_ImHi;
    private static final int CamWidth = TrakSim.WinWi;

    public FakeCamera() {

        FrameNum = 0;
        numRows = CamHight;
        numCols = CamWidth;
        tile = 0;
    }

    @Override
    public boolean Connect(int framerate) {
        return true;
    }

    @Override
    public void NextFrame()
    {

    }

    @Override
    public void Finish()
    {

    }
}
