package org.avphs.traksim;/* org.avphs.traksim.TrakSim Car Simulator for use with NWAPW Year 3 Autonomous Car Project
 * Use this package for testing with fly2cam.FlyCamera + fakefirm.Arduino
 *
 * This simulator pretends to be a camera using the FlyCamera API, and
 * watches the commands being sent to the Arduino through FakeFirmata,
 * and controls the simulated car based on those commands, then shows
 * what a forward-facing camera on the simulated car would see.
 *
 * org.avphs.traksim.TrakSim copyright 2018 Itty Bitty Computers and released at this time
 * to the public as open source. There are no warranties of any kind.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but if you are developing your self-driving code on some computer other
 * than LattePanda, you can substitute package noJSSC, which has the same
 * APIs (as used by FakeFirmata) but does nothing.
 */

/**
 * Separate Java file for Driving Simulator constants.
 * <p>
 * These are here to make it easy to change the operation of org.avphs.traksim.TrakSim,
 * (and then recompile). See ReadMe for descriptions.
 * <p>
 * Some of these constants are checked for reasonable values,
 * and org.avphs.traksim.TrakSim won't run unless they are.
 */

/**
 * Refactored Key:
 * RP Comment: Comment from student analysis of what Tom code does, will not be 100% reliable, but I tried my best. (Difference between this and Layman is that
 * RP Com tries to explain the comment, Layman attempts to explain what the code does)
 * Layman: Tries to explain in simplest terms what Tom's code does
 * ESC = Electronic Speed Control <3 You're welcome
 * RP: Probably easter eggs for future students, put them all together to email me :3
 */
public class DriverConstants { // org.avphs.traksim.TrakSim constant parameters

    public static final boolean // options & diagnostic switches..
            D_LiveCam = false,         // F: omit connecting to FlyCamera
            Default_Settings = true,        // T: default start; F: easy run mode, also goes forward
            D_Start_Live_Cam = !Default_Settings,   // T: start in live camera if possible
            D_FixedSpeed = Default_Settings,   // ignore speed control, assume fMinSpeed
            D_StayInTrack = Default_Settings,  // ignore steering control, stay centered in track
            D_Show_Track_Test_Pts = false,   // T: show test points used to calc StayInTrack
            D_Show_Drive_Controls = false,    // T: to see where to click to steer/accelerate
            D_ShowMap = true,         // T: show the map next to the scene
            D_Show_Close_Up_Map = true,       // T: show close-up map if there is room
            D_Gradual_Servo_Change = false,     // T: servos take time to arrive at setting
            D_Draw_Track_Only = false,      // T: draw track only, omit artifacts & trees
            D_Use_Text_File_Track = true,      // T: use text file to build track to drive
            D_Allow_Reverse = false,     // T: allow reverse (untested)
            D_Darken_Background = false,       // T: darken distant background walls (disabled)
            D_Postrize = true,        // T: include Tom Pittman's Speedy Posterize code
            D_TestTimes = false,      // T: run example time-test code
            D_Show_Distant_Walls = true,        // T: search back for distant walls (slower)
            D_Start_Servo_Calibration = false, // T: use this to calibrate servo limits
            D_Log_Draw = false, // Draw the log into the console
            D_Log_Log = false, // Log information (Doesn't output to console)
            D_BlurSpeed = false, //
            D_Detailed_Map_Log = false, // Gives more map information to debug with!!
            D_Show_Headlights = false, // Shows headlights in GUI
            D_Factual_Log = false, // Has a more detailed version of the logs
            D_MapLogged = false, // Logs the map
            D_Minimized_Log = false; // Makes the log minimized (Not as verbose?)

    public static final int
            // QLog Is incremented by factor of 2^(QLog)
            // TODO: Can be turned into an ENUM (Sorry future groups :( - RP, 2019), btw, anything called QLog is verbose logging
            D_Verbose_Logging_Options = 3, // Global log enabler (=0 kills most log, -1 enables all) bits:
    //   +1: MapLogged enabled
    //   +2: mouseEntered/mouseClicked
    //   +4: "DrListen="
    //   +8: noJSSC log enabled
    //  +16: enables (MapCol) log
    //  +32: logs ShoArt,ReadTiff32Im
    //  +64: logs SiT in InTrackIt
    // +256: logs walls; Qlog>>16 = log this BG+
    D_Vertical_Car_Position = 68, D_Horizontal_Car_Position = 172, // Initial position for the car, meters from NW, Layman: Distance in units from top left corner of screen
            D_Initial_Car_Clockwise_Orientation = 300,     // Initial orientation for car, clockwise degrees from north
            D_Camera_Zoom_35mm = 23,     // 35mm-equivalent focal length for "camera"
            D_RaceLens = 52, // ditto for race conditions
            D_Bayer_Tiling_Config = 1,     // Bayer8 tiling code (RG/GB) as defined by Pt.Grey
            D_Frame_Rate_Period = 100, // =10/fps, nominal camera frame rate (must >= 20ms), Layman: Delay in between frames in ms
            D_AskOdomTime = 500, // how long (ms) between asking for odometer readings
            D_Run_N_Servo_Tests = 0, // number of times to run through test (1: no ESC)
            D_ServoMsgPos = 200 * 0x10000 + 0, // prerace of warning in image file,
            D_ServoMsgTL = 40 * 0x10001, D_ServoMsgSiz = 40 * 0x10000 + 80, // posn on screen
            Display_Height = 480,
            Display_Width = 640, // Camera image (and display window) size
            D_Half_Map_Height = 100, D_Half_Map_Width = 128,  // 2x2 grid map size (in 2m units), Layman: Does not do what comment says, later on in code this will determine the maps X & Y size
            D_Draw_Dashboard = 12,                    // dashboard height at bottom of image
            D_HorzonStrip = Display_Height >> 4,          // height to look for horizon stuff, bitshift right 4 divides it by 16
            D_Steer_Servo_Pins = 9,
            D_Gas_Servo_Pins = 10,  // FakeArduino output pins for servos, RP Comment: Gas is used later on for acceleration
            D_Min_ESC_Range = 10, D_Max_ESC_Range = 32, // active range of ESC, in steps +90, RP Com: ESC Explained in DriverConstants key
            D_Left_Steer_Range = 33, D_Right_Steer_Range = 44, // (measured) full range (-33,+44)
            D_Fast_Blob_Finder = 32 * 0,      // >0: Tom Pittman's Speedy Blob Finder, max+
            D_White_Line_Color = 0xFFFFCC, // color of track edge lines (=masking tape)
            D_Driving_Info_Color = 0x0099FF,  // the color of driving info added to image... RP Com: Currently Sky Blue
            D_Steer_Wheel_Color = 0xCC9900, D_Dash_Color = 0x333300, // steering wheel & dash colors
            D_Wall_Color = 0xFFFFCC,  // (indoor) wall&door colors..
            D_DarkWall = 0x999966, // Color of a wall
            D_BackWall = 0x554422, // Color of another wall
            D_Pillar_Color = 0x666666, // RP Com: ITS PILLAR COLOR!!!!
            D_Ceiling_Color = D_Pillar_Color, // default color of ceiling (if any)
            D_Car_Color = 0xFF0099, D_Artifact_Color = 0xFFCC00, // pink car color, amber artifact
            D_Interior_Image_Color = 0xFEFEFE,  // magical interior image color -> transparent
            D_Animated_Artifact_Log = 0x50020,    // log artifact +5 for 1st 32 frames if NoisyMap=T
            D_Map_Scale = 8,        // presumed 1:8 scale (untested for other)
            D_Adjust_Turn_Or_Zoom = 0,     // adjust TurnRadius if >0, Zoom35 if <0
            D_Magnification_Factor = 3,    // 2^x = magnification, x=0 to let org.avphs.traksim.TrakSim decide
            D_Traffic_Light_Time = 3, // 2^x = seconds red time = green time +2secs yellow
            D_BreadCrumb_List_Size = 255,    // (power of 2) size of BreadCrumbs list for map display
            D_Track_Pebble_Size = 1,   // default 2^x = size in park cm of carpet/track pebbles---------- Layman: There are apparently pebbles located throughout the track?
            D_Pebble_Contrast = 6, // default pebble texture contrast, 0..9 (<0: none), at least, that's what it seems to do, no comment here before .-.
            D_Map_Checker_Board = 1;   // (power of 2) =1 to checker 1x1m, =2 for 2x2, =0 off, Layman: makes the checker pattern for traksim bigger

    public static final double D_TurnRadius = 7.0, // nom. meters in park coords
    // measured at servo prerace = min(LeftSteer,RiteSteer)
    D_Floor_Min_Speed = 4.0, // measured min (8x actual m/s = mph/2) @ MinESCact----------Layman: the floored speed of the car it seems, or the ESC (Electronic Speed Control) minimum
    // 1mph = 0.5m/s park speed = 3"/sec @ 1:8 scale floor speed
    // 1mph floor speed is 8mph park speed = 4m/s
    D_Friction_Coefficient = 0.5,  // default coefficient of friction
            D_Center_Of_Gravity = 0.2,     // ratio: height of CoG / wheel base
            D_MetersPerTurn = 1.2, // park meters, = 6" on floor (1:8 scale)
            D_ProxThresh = 8.0,    // in park meters, actual = 1m = 40" (=0 disables) -------- TODO: Figure out what this means/changes
            D_White_Line_Width = 0.25,    // in park meters, here 10" (3cm actual)
            D_Acceleration = 0.1,  // time (in secs) to achieve FloorMinSpeed
            D_Gravity = 9.81 * ((double) D_Map_Scale), // gravity, scaled to park meters
            D_Camera_Height_Above_Track = 1.2;      // camera height above track in park meters

    public static final String D_Change_Scene_By_Name_File = "TrackImg", // +"indx" -> map file
            D_CaptureImageBase = "ScreenCapture_", // +".tiff" -> screen capture file
            D_Log_Poster_Color_Names = "W,Y,Gy,B,G,R,Bk", // log names for PosterColors
            D_RevDate = "2019 July 2"; //Dunno what this does, it's a date though

} //~DriverConstants // (CC)
