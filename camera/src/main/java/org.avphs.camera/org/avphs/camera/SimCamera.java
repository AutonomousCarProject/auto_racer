/* TrakSim Car Simulator for use with NWAPW Year 3 Autonomous Car Project
 * Use this package for testing with fly2cam.FlyCamera + fakefirm.Arduino
 *
 * This simulator pretends to be a camera using the FlyCamera API, and
 * watches the commands being sent to the Arduino through FakeFirmata,
 * and controls the simulated car based on those commands, then shows
 * what a forward-facing camera on the simulated car would see.
 *
 * TrakSim copyright 2018 Itty Bitty Computers and released at this time
 * to the public as open source. There are no warranties of any kind.
 *
 * SimCamera is based on fly2cam.FlyCamera, which is in the public domain.
 */
package org.avphs.camera; // (class SimCamera)                   // 2018 May 25

import org.avphs.traksim.DriverCons;
import org.avphs.traksim.HandyOps;
import org.avphs.traksim.TrakSim;

/**
 * FlyCamera Simulator for TrakSim (but different name)
 * Use this for testing in parallel with fly2cam.FlyCamera
 */
public class SimCamera extends CameraBase {
    // public static final int FrameRate_15 = 3, FrameRate_30 = 4,
    //     BaseRose = 480, BaseColz = 640, BaseTile = 1;

    // int numRows, // (protected) actual number of rows = FlyCap2.fc2Image.rows/2
    //     numCols, // actual number of columns = FlyCap2.fc2Image.cols/2
    //     tile, // see FlyCapture2Defs.fc2BayerTileFormat
    //     errorNum, // returns an error number, see ErrorNumberText()
    //     FrameNum, // counts the number of good frames seen (nobody looks)
    //     pending; // >0 after frame look-ahead

    private static final int SimTile = DriverCons.D_BayTile;
    private static final int CamHight = DriverCons.D_ImHi;
    private static final int CamWidth = TrakSim.WinWi;

    private static final boolean
            NoisyFaker = DriverCons.D_Mini_Log && (DriverCons.D_Qlog < 0);

    public TrakSim theSim;

    public SimCamera() {
        if (NoisyFaker) {
            System.out.println(HandyOps.Dec2Log("apw3.SimCamera ", CamHight,
                    HandyOps.Dec2Log("/", CamWidth, "")));
        }
        theSim = new TrakSim();
        numRows = CamHight;
        numCols = CamWidth;
        tile = SimTile;
        FrameNum = 0;
        errorNum = 0;
    }

    /**
     * Gets one frame from the (fake) camera by fetching it from TrakSim.
     *
     * @return True if success, false otherwise
     */
    public void fetchNextFrame() { // fills pixels, false if can't (SimCam)
        theSim.SimStep(1);
        bayerImage = theSim.GetSimFrame(numRows, numCols);
    } //~NextFrame // in apw3.SimCamera

    /**
     * Terminate the (fake) camera session.
     * Required by Flir/Pt.Grey drivers to prevent memory leaks.
     */
    public void finish() { // required at end to prevent memory leaks (SimCam)
        errorNum = 0;
        if (NoisyFaker || errorNum < 0) // NF=Mini_Log
            System.out.println(HandyOps.Dec2Log(" (SimCam) --> Finis ", errorNum, ""));
        numRows = 0;
        numCols = 0;
        tile = 0;
        FrameNum = 0;
    } //~Finish

    // public static String ErrorNumberText(int errno) -> super
    // public int Dimz() {return (numRows<<16)+numCols;} // access cam image size (SimCam)
    // public int PixTile() {return tile;} // Bayer encoding, frex RG/GB =1, GB/RG =3

    /**
     * Start a new (fake) camera session with the specified frame rate.
     *
     * @param frameRate =4 for 30 fps, =3 for 15, =2 for 7.5 pfs
     * @return True if success, false otherwise
     */
    public boolean connect(int frameRate) { // (SimCam) rtns F at eof
        int why = 27;          // sets numRows,numCols,tile frameRate is ignored
        errorNum = 0;
        while (true) {
            if ((frameRate != 0) && (frameRate != FrameRate_15) && (frameRate != FrameRate_30) &&
                    (frameRate != FrameRate_15 - 1) && (frameRate != FrameRate_15 - 2)) {
                break; // why = 27
            }
            why = 4;
            if (theSim == null) {
                break;
            }
            why++; // why = 5
            if (!theSim.StartImage(CamHight, CamWidth, 1)) {
                break;
            }
            numRows = CamHight;
            numCols = CamWidth;
            tile = SimTile;
            FrameNum = 0;
            why = 0;
            break;
        } //~while
        errorNum = why;
        return why == 0;
    } //~Connect

    /**
     * Tells if this camera is live or fake.
     *
     * @return false, because this camera is only a fake
     */
    public boolean live() {
        return false;
    } // this is not a live camera (SimCam)

    public String toString() { // (SimCam)
        return "apw3.SimCamera " + errorNum + ": " + ErrorNumberText(errorNum);
    }
} //~SimCamera (apw3) (SC)
