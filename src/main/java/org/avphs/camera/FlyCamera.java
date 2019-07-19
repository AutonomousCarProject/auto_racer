// The actual class FlyCamera reads images from camera.. // 2018 May 14

// You also need FlyCapture2_C.dll + FlyCapture2.dll in your Java project folder

package org.avphs.camera; // (same API as fly0cam, same as 2017 Feb 27 but subclassable)

public class FlyCamera extends CameraBase { // (in Java/fly2cam)
    private long stuff; // used for error reporting, or not at all

//   static {System.loadLibrary("fly2cam/FlyCamera");} // comment this line out if no DLLs

    public FlyCamera() { // (in Java/fly2cam)
        try {
            System.loadLibrary("FlyCamera");
            //Runtime.getRuntime().loadLibrary("fly2cam/FlyCamera");

        } catch(Error e){
            System.out.println("FlyCamera not found");
        }

        FrameNum = 0;
        numRows = 0;
        numCols = 0;
        tile = 0;
    }


} //~FlyCamera (fly2cam) (F2)