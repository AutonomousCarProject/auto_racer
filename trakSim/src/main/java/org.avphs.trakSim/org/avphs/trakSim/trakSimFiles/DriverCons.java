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
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but if you are developing your self-driving code on some computer other
 * than LattePanda, you can substitute package noJSSC, which has the same
 * APIs (as used by FakeFirmata) but does nothing.
 */
package org.avphs.trakSim.trakSimFiles;

/**
 * Separate Java file for Driving Simulator constants.
 *
 * These are here to make it easy to change the operation of TrakSim,
 *   (and then recompile). See ReadMe for descriptions.
 *
 * Some of these constants are checked for reasonable values,
 *   and TrakSim won't run unless they are.
 */
public class DriverCons { // TrakSim constant parameters

  public static final boolean // options & diagnostic switches..
    D_LiveCam = false,         // F: omit connecting to FlyCamera
      D_Fault = false,        // T: default start; F: easy run mode
    D_StartLive = !D_Fault,   // T: start in live camera if possible
    D_FixedSpeed = D_Fault,   // ignore speed control, assume fMinSpeed
    D_StayInTrack = D_Fault,  // ignore steering control, stay centered in track
    D_ShoTrkTstPts = false,   // T: show test points used to calc StayInTrack
    D_ShoClikGrid = false,    // T: to see where to click to steer/accelerate
    D_ShowMap = true,         // T: show the map next to the scene
    D_DoCloseUp = true,       // T: show close-up map if there is room
    D_RampServos = false,     // T: servos take time to arrive at setting
    D_TrakNoPix = false,      // T: draw track only, omit artifacts & trees
    D_UseTexTrak = true,      // T: use text file to build track to drive
    D_Reversible = false,     // T: allow reverse (untested)
    D_DeepDark = false,       // T: darken distant background walls (disabled)
    D_Postrize = true,        // T: include Tom Pittman's Speedy Posterize code
    D_TestTimes = false,      // T: run example time-test code
    D_ShoWallz = true,        // T: search back for distant walls (slower)
    D_StartInCalibrate = false, // T: use this to calibrate servo limits
    D_Log_Draw = false, D_Log_Log = false, D_BlurSpeed = false,
    D_NoisyMap = false, D_ShoHedLit = false, D_Fax_Log = false,
    D_MapLogged = false, D_Mini_Log = false;

  public static final int
    D_Qlog = 3, // Global log enabler (=0 kills most log, -1 enables all) bits:
      //   +1: MapLogged enabled
      //   +2: mouseEntered/mouseClicked
      //   +4: "DrListen="
      //   +8: noJSSC log enabled
      //  +16: enables (MapCol) log
      //  +32: logs ShoArt,ReadTiff32Im
      //  +64: logs SiT in InTrackIt
      // +256: logs walls; Qlog>>16 = log this BG+
    D_Vramp = 68, D_Hramp = 172, // Initial pos'n for car, meters from NW
    D_RampA = 300,     // Initial orient'n for car, c-wise degrees from north
    D_Zoom35 = 23,     // 35mm-equivalent focal length for "camera"
      D_RaceLens = 52, // ditto for race conditions
    D_BayTile = 1,     // Bayer8 tiling code (RG/GB) as defined by Pt.Grey
    D_FrameTime = 100, // =10/fps, nominal camera frame rate (must >= 20ms)
    D_AskOdomTime = 500, // how long (ms) between asking for odometer readings
    D_nServoTests = 0, // number of times to run through test (1: no ESC)
    D_ServoMsgPos = 200*0x10000+0, // prerace of warning in image file,
      D_ServoMsgTL = 40*0x10001, D_ServoMsgSiz = 40*0x10000+80, // posn on screen
    D_ImHi = 480, D_ImWi = 640, // Camera image (and display window) size
    D_HalfTall = 100, D_HalfMap = 128,  // 2x2 grid map size (in 2m units)
    D_DrawDash = 12,                    // dashboard height at bottom of image
    D_HorzonStrip = D_ImHi>>4,          // height to look for horizon stuff
    D_SteerServo = 9, D_GasServo = 10,  // FakeArduino output pins for servos
    D_MinESCact = 10, D_MaxESCact = 32, // active range of ESC, in steps +90
    D_LeftSteer = 33, D_RiteSteer = 44, // (measured) full range (-33,+44)
    D_FastBlobs = 32*0,      // >0: Tom Pittman's Speedy Blob Finder, max+
    D_WhitLnColo = 0xFFFFCC, // color of track edge lines (=masking tape)
    D_MarinBlue = 0x0099FF,  // the color of driving info added to image
    D_SteerColo = 0xCC9900, D_DashColo = 0x333300, // st.wheel & dash colors
    D_CreamWall = 0xFFFFCC,  // (indoor) wall&door colors..
    D_DarkWall = 0x999966, D_BackWall = 0x554422, D_PilColo = 0x666666,
    D_CeilingCo = D_PilColo, // default color of ceiling (if any)
    D_CarColo = 0xFF0099, D_ArtiColo = 0xFFCC00, // pink car color, amber a'fact
    D_Transprnt = 0xFEFEFE,  // magical interior image color -> transparent
    D_Anim_Log = 0x50020,    // log artifact +5 for 1st 32 frames if NoisyMap=T
    D_ModelScale = 8,        // presumed 1:8 scale (untested for other)
    D_TweakRx = 0,     // adjust TurnRadius if >0, Zoom35 if <0
    D_xCloseUp = 3,    // 2^x = magnification, x=0 to let TrakSim decide
    D_xTrLiteTime = 3, // 2^x = seconds red time = green time +2secs yellow
    D_Crummy = 255,    // (power of 2) size of BreadCrumbs list for map display
    D_PebblSize = 1,   // default 2^x = size in park cm of carpet/track pebbles
    D_PebContrast = 6, // default pebble texture contrast, 0..9 (<0: none)
    D_CheckerBd = 1;   // (power of 2) =1 to checker 1x1m, =2 for 2x2, =0 off

  public static final double D_TurnRadius = 7.0, // nom. meters in park coords
      // measured at servo prerace = min(LeftSteer,RiteSteer)
    D_fMinSpeed = 4.0, // measured min (8x actual m/s = mph/2) @ MinESCact;
      // 1mph = 0.5m/s park speed = 3"/sec @ 1:8 scale floor speed
      // 1mph floor speed is 8mph park speed = 4m/s
    D_CoefFriction = 0.5,  // default coefficient of friction
    D_CentoGrav = 0.2,     // ratio: height of CoG / wheel base
    D_MetersPerTurn = 1.2, // park meters, = 6" on floor (1:8 scale)
    D_ProxThresh = 8.0,    // in park meters, actual = 1m = 40" (=0 disables)
    D_WhiteLnWi = 0.25,    // in park meters, here 10" (3cm actual)
    D_Acceleration = 0.1,  // time (in secs) to achieve fMinSpeed
    D_Grav = 9.81*((double)D_ModelScale), // gravity, scaled to park meters
    D_CameraHi = 1.2;      // camera height above track in park meters

  public static final String D_SceneFiName = "TrackImg", // +"indx" -> map file
      D_CaptureImageBase = "ScreenCapture_", // +".tiff" -> screen capture file
      PostCoNames = "W,Y,Gy,B,G,R,Bk", // log names for PosterColors
      D_RevDate = "2019 July 2";

  } //~DriverCons // (CC)
