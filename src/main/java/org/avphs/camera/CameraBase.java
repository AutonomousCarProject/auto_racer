package org.avphs.camera;

import org.avphs.traksim.DriverCons;

public class CameraBase implements Camera {


    protected byte[] bayerImage = new byte[BaseRose * BaseColz * 4];
    protected int errorNum; // returns an error number, see ErrorNumberText()
    protected int numRows; // (fly2cam) actual number of rows = FlyCap2.fc2Image.rows/2
    protected int numCols; // actual number of columns = FlyCap2.fc2Image.cols/2
    protected int tile; // see FlyCapture2Defs.fc2BayerTileFormat
    int FrameNum; // counts the number of good frames seen (nobody looks)
    static final int FrameRate_30 = 4;
    public static final int BaseRose = 480;
    public static final int BaseColz = 640;
    public static final int BaseTile = 1;

    //static {System.loadLibrary("FlyCamera");}

    public String toString() { // (fly2cam)
        return "fly2cam.FlyCam " + errorNum + ": " + ErrorNumberText(errorNum);

    }

    @Override
    public int getSteerServoPin() {
        return DriverCons.D_SteerServo;
    }

    @Override
    public int getSpeedServoPin() {
        return DriverCons.D_GasServo;
    }

    /**
     * Start a new camera session with the specified frame rate.
     *
     * @param frameRate =4 for 30 fps, =3 for 15, =2 for 7.5 pfs
     * @return True if success, false otherwise
     */
    public native boolean Connect(int frameRate); // required, sets numRows, numCols,tile


    /**
     * Gets one frame from the Chameleon3 or FireFly camera.
     *
     * @return True if success, false otherwise
     */
    public native void NextFrame(); // fills pixels, false if cant

    /**
     * Terminate the session.
     * Required by Flir/Pt.Grey drivers to prevent memory leaks.
     */
    public native void Finish(); // required at end to prevent memory leaks

    /**
     * Gets the image size (rows and columns) for this camera.
     *
     * @return The number of pixel rows in the high 16, columns in low 16
     */
    public int getCamDimensions() {
        return (numRows << 16) + numCols;
    } // access cam image size (fly2cam)

    public int getCamHeight() {
        return numRows;
    }

    public int getCamWidth() {
        return numCols;
    }

    @Override
    public byte[] getBayerImage() {
        return bayerImage;
    }

    public byte[] getRawImage() { return new byte[0]; }

    /**
     * Gets the Bayer8 encoding number for this camera.
     *
     * @return The Bayer8 encoding number 1 (RG/GB) to 4
     */
    public int pixTile() {
        return tile;
    } // Bayer encoding, frex RG/GB =1, GB/RG =3

    /**
     * Tells if this camera is live or not.
     *
     * @return true if this camera is connected, false otherwise
     */
    public boolean live() {
        return tile > 0;
    } // we have a live camera (fly2cam)
    /**
     * Gets a text description of an error number.
     *
     * @param errno The error number from one of the other API calls
     * @return The text description
     */

    static String ErrorNumberText(int errno) { // errorNum -> String (fly2cam)
        // if (errorNum==0) errno = errorNum;
        if (errno == -20) return "ByteArray is not same size as received data";
        switch (errno) {
            case -1:
                return "No camera connected";
            case 0:
                return "No error";
            case 1:
                return "fc2CreateContext failed";
            case 2:
                return "fc2GetNumOfCameras failed";
            case 3:
                return "No cameras detected";
            case 4:
                return "fc2GetCameraFromIndex did not find first camera";
            case 5:
                return "fc2Connect failed to connect to first camera";
            case 6:
                return "fc2StartCapture failed";
            case 7:
                return "fc2CreateImage failed";
            case 8:
                return "No error";
            case 9:
                return "fc2RetrieveBuffer failed";
            case 10:
                return "rawImage.format = 0 (probably unset)";
            case 11:
                return "ByteArray to NextFrame is null";
            case 12:
                return "Connect failed or not called (context == null)";
            case 13:
                return "Something in context corrupted";
            case 14:
                return "ByteArray is way too short or too long";
            case 15:
                return "GetByteArrayElements failed (couldn't access bytes)";
            case 16:
                return "fc2RetrieveBuffer failed, possibly timeout";
            case 17:
                return "fc2GetImageData failed";
            case 18:
                return "No pixel data received";
            case 19:
                return "Unknown camera image size";
            case 20:
                return "No error";
            case 21:
                return "fc2StopCapture failed";
            case 22:
                return "fc2DestroyImage failed";
            case 23:
                return "Both fc2StopCapture and fc2DestroyImage failed";
            case 24:
                return "fc2CreateImage failed (RGB)";
            case 25:
                return "fc2ConvertImageTo (RGB) failed";
            case 26:
                return "fc2GetProperty failed";
            case 27:
                return "Unknown frame rate";
            case 28:
                return "fc2SetProperty failed";
        } //~switch
        return "fc2RetrieveBuffer probably returned some format other than Bayer8";
    } //~ErrorNumberText

}
