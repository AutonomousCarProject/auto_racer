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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * The main TrakSim Car Simulator class..
 */
public class TrakSim {

    private static final String SceneFiName = DriverCons.D_SceneFiName,
            RevDate = DriverCons.D_RevDate;
    // fGratio cnvrts ESC steps to nominal velocity; fMinSpeed=4.0,MinESC=10
    // ..adjust multiplier so it's correct for your car: *1.0 => fGratio=0.4
    private static final double fMinSpeed = DriverCons.D_fMinSpeed,
            fMinESC = (double) DriverCons.D_MinESCact,
            fGratio = 1.0 * fMinSpeed / fMinESC;
    private static final boolean RampServos = DriverCons.D_RampServos,
            Mini_Log = DriverCons.D_Mini_Log && (DriverCons.D_Qlog != 0),
            Log_Draw = DriverCons.D_Log_Draw && (DriverCons.D_Qlog < 0),
            Log_Log = DriverCons.D_Log_Log && (DriverCons.D_Qlog < 0),
            MapLogged = DriverCons.D_MapLogged && ((DriverCons.D_Qlog & 1) != 0),
            NoisyMap = DriverCons.D_NoisyMap && MapLogged,
            TrakNoPix = DriverCons.D_TrakNoPix, ShowMap = DriverCons.D_ShowMap,
            DoCloseUp = DriverCons.D_DoCloseUp && ShowMap,
            Reversible = DriverCons.D_Reversible, ShoHedLit = DriverCons.D_ShoHedLit,
            ShoClikGrid = DriverCons.D_ShoClikGrid, Fax_Log = DriverCons.D_Fax_Log,
            UseTexTrak = DriverCons.D_UseTexTrak, GoodLog = (DriverCons.D_Qlog < 0);
    private static final int Vramp = DriverCons.D_Vramp, // imported constants,
            ImHi = DriverCons.D_ImHi, ImWi = DriverCons.D_ImWi, // ..local names
            Hramp = DriverCons.D_Hramp, RampA = DriverCons.D_RampA,
            HalfMap = DriverCons.D_HalfMap, HalfTall = DriverCons.D_HalfTall,
            LeftSteer = DriverCons.D_LeftSteer, RiteSteer = DriverCons.D_RiteSteer,
            SteerServo = DriverCons.D_SteerServo, GasServo = DriverCons.D_GasServo,
            MinESCact = DriverCons.D_MinESCact, MaxESCact = DriverCons.D_MaxESCact,
            DrawDash = DriverCons.D_DrawDash, FrameTime = DriverCons.D_FrameTime,
            Zoom35 = DriverCons.D_Zoom35, xCloseUp = DriverCons.D_xCloseUp,
            Back_Wall = DriverCons.D_BackWall,
            CarColo = DriverCons.D_CarColo, ArtiColo = DriverCons.D_ArtiColo,
            AltWalColo = ArtiColo, // = CarColo to see segment ends, else = ArtiColo
            MarinBlue = DriverCons.D_MarinBlue, SteerColo = DriverCons.D_SteerColo,
            DashColo = DriverCons.D_DashColo, CanSkid = -1, // =0 disables
            Truk = 0x40000000,     // in-track flag added to MapColo res
            izBG = 0x20000000,     // background wall flag, ditto
            LumUniShif = 12,       // default track luminance, 2^LUS = nominal 1.0
            unPainted = 0x9999CC,  // background color for SeeOnScrnPaint (steel gray)
            SiTwide = 7,           // 7m side look for StayInTrack
            xTrLiteTime = DriverCons.D_xTrLiteTime, Qlog = DriverCons.D_Qlog,
            BayerTile = DriverCons.D_BayTile, // 1=RG/GB

    MapTall = HalfTall * 2, MapWide = HalfMap * 2, SteerMid = ImWi / 3,
            ImHaf = ImHi / 2, ImMid = ImWi / 2, MapWiBit = 8, Tintx = 0, ArtBase = 8,
            ImHmsk = 4095, zx50 = 28, // = fudge-factor to make Zoom35=50 come out OK
            ZoomPix = ImWi * zx50 / Zoom35, // divide this by distance for pix/meter
            ParkDims = MapTall * 0x10000 + MapWide, CheckerBd = DriverCons.D_CheckerBd,
            ServoStepRate = 0, // = FrameTime/20, // Vscale = DriverCons.D_Vscale,
            MapAsize = MapTall * MapWide, GridSz = HalfTall * HalfMap, MxLayShf = 3,
            MaxLayers = 4, LayStep = ImWi * MaxLayers, LayerSz = LayStep * 2,
            TweakRx = DriverCons.D_TweakRx, Crummy = DriverCons.D_Crummy;
    public static final int WinWi = (ShowMap ? MapWide + 16 : 0) + ImWi, // window width
            Lin2 = WinWi * 2, nPixels = ImHi * WinWi; // + pix in whole window
    private static final double TurnRadius = DriverCons.D_TurnRadius,
            LfDeScaleSt = 1.0, RtDeScaleSt = 1.0, // (no longer used)
            WhiteLnWi = DriverCons.D_WhiteLnWi, // Fby3 = 1.0/3.0,
            ProxyThresh = DriverCons.D_ProxThresh * DriverCons.D_ProxThresh,
            fFtime = (double) FrameTime / 1000.0, fFPS = 1.0 / fFtime, // fps
            Acceleration = DriverCons.D_Acceleration,
            PkGrav = DriverCons.D_Grav * fFtime * fFtime, // now scaled to frame rate
            fImMid = (double) ImMid, // ModelScale = (double)DriverCons.D_ModelScale,
            CameraHi = DriverCons.D_CameraHi, CentoGrav = DriverCons.D_CentoGrav,
            fMapTall = (double) MapTall, fMapWide = (double) MapWide,
    // TurnRadius is measured at servo position = min(LeftSteer,RiteSteer)
    NormdRad = (256.0 / TurnRadius) * ((LeftSteer < RiteSteer)
            ? (double) LeftSteer : (double) RiteSteer),
    // NormdRad: scale factor to calc acceleration from servo steering angle
    fTurn4m = NormdRad * (180.0 / 256.0 / Math.PI), // =180/(r*pi) -> degs/degs/m
    // fTurn4m scales forward advance at given steer into degrees of turn:
    // advance pi*TR meters @ st=max turns car 180, @ st=0 turns 0;
    // assume turn is linear in both steer posn & travel dist (probly not)
    fSweeper = 20.0, // how much to look aside in StayInTrk
            fMaxSpeed = fGratio * ((double) MaxESCact),
            CoFudge = 1.0, MaxRspeed = (Reversible ? -0.5 * fMaxSpeed : 0.0),
            Torque = fFtime / Acceleration, // used to calc speed change
            fTime4mass = fMinESC * fFtime / Acceleration;
    private static final String[] CompNames = {" N  ", " NNE ", " NE ", " ENE ",
            " E  ", " ESE ", " SE ", " SSE ", " S  ", " SSW ", " SW ", " WSW ",
            " W  ", " WNW ", " NW ", " NNW "};
    private static final String StopInfo // see: case '_':
            // [O r c f] v-rng tall wide H-off ppm anim..
            = " 90 128 29 1 30 0~1 -- stop sign full-on (default)\n"
            + " 90 64 15 60 15 0~2 -- stop sign back full-on\n"
            + " 160 128 17 32 30 0~3 -- stop sign front angled\n"
            + " 160 64 9 50 15 0~4 -- stop sign back angled\n"
            + " 180 64 3 76 15 0~5 -- stop sign S-edge-on\n"
            + " 0 64 3 80 15 0~6 -- stop sign P-edge-on\n"
            + " 255 25 84 50~7 \n" // dark traffic lite
            + " 0 255 25 110 50 1~8`5 -- green lite\n"
            + " 0 255 25 136 50 1~9`6 -- yellow lite\n"
            + " 0 255 25 162 50 1~10`7 -- red lite\n"
            + " 0 64 23 55^132 25~11 -- pedestrian standing left\n"
            + " 0 64 29 52^66 25~12 -- pedestrian stepping left\n"
            + " 0 64 23 1^132 25~13 -- pedestrian standing right\n"
            + " 0 64 29 25^132 25~14 -- pedestrian stepping right\n"
            + " 0 83 99 412^172 32~15 -- white painted STOP line";
    private static final int[] CurveSteps = {
            18, 20, 23, 27, 31, 35, 39, 43, 49, 56, 63, 72, 81, 90, 99, 108, 117, 126,
            0x4DA04, 0, 0x82D04, 0x4DA08, 0,
            0x81E02, 0xABC0A, 0x2DA08, 0,         // (2)
            0x101E04, 0x8BC08, 0x4DA10, 0,        // (3)
            0x101E04, 0x10BC10, 0x4DA10, 0,       // (4)
            0x181E06, 0xEBC0E, 0x6DA18, 0,        // (5)
            0x181E06, 0x16BC16, 0x6DA18, 0,       // (6)
            0x100F02, 0x101E07, 0x13BC13, 0x7CB10, 0x2DA10, 0,          // (7)
            0x100F02, 0x101E06, 0x102D0C, 0xCBC10, 0x6CB10, 0x2DA10, 0, // (8)
            0x100F02, 0x181E08, 0x102D0A, 0xABC10, 0x8CB18, 0x2DA10, 0, // (9)
            0x80600, 0x100F04, 0x101E06, 0x102D12, 0x12BC10, 0x6CB10,
            0x4D410, 0x0DA08, 0,                                     // (10)
            0x80600, 0x100F03, 0x181E0B, 0x102D0E, 0xEBC10, 0xBCB18,
            0x3D410, 0x0DA08, 0,                                     // (11)
            0x80600, 0x100F03, 0x181E09, 0x182D10, 0x10BC18, 0x9CB18,
            0x3D410, 0x0DA08, 0,                                     // (12)
            0x80600, 0x100F03, 0x201E0C, 0x182D0D, 0xDBC18, 0xCCB20,
            0x3D410, 0x0DA08, 0,                                     // (13)
            0x80600, 0x180F05, 0x181E09, 0x182D16, 0x16BC18, 0x9CB18,
            0x5D418, 0x0DA08, 0,                                     // (14)
            0x80600, 0x180F04, 0x201E0E, 0x182D12, 0x12BC18, 0xECB20,
            0x4D418, 0x0DA08, 0,                                     // (15)
            0x80600, 0x180F04, 0x201E0D, 0x202D13, 0x13BC20, 0xDCB20,
            0x4D418, 0x0DA08, 0, 0, 0, 0};                           // (16)
    private static final int[] ImWalCurves = {
            36, 38, 40, 44, 48, 52, 57, 61, 66, 71, 77, 81,   // 0..5
            87, 93, 98, 104, 112, 118, 124, 130, 138, 144, 151, 157, // 6..11
            165, 173, 181, 189, 196, 204, 212, 220, 228, 236, 244, 0,   // 12..16
            0x8000D100, 0, 0x80007300, 0,                          // D0 @36, C0 @38
            0x80000000, 0x802DD110, 0x805A0201, 0,                    // D1 @40
            0x80000000, 0x802D7310, 0x805A0601, 0,                    //         C1 @44
            0x80000000, 0x802D5111, 0x805A0211, 0,                    // D2 @48
            0x80000000, 0x801A6310, 0x80407311, 0x805A0601, 0,        //         C2 @52
            0x1000, 0x802D5121, 0x480111, 0,                          // D3 @57
            0x1000, 0x80226320, 0x80387311, 0x480101, 0,              //         C3 @61
            0x1000, 0x80224121, 0x80385111, 0x4C0111, 0,              // D4 @66
            0x1000, 0x801A6320, 0x802D7311, 0x80406311, 0x4C0101, 0,  //         C4 @71
            0x2100, 0x802D5132, 0x401211, 0,                          // D5 @77
            0x2000, 0x801F6330, 0x802D7311, 0x803B6311, 0x440201, 0,  //         C5 @81
            0x1000, 0x121120, 0x802D5122, 0x3B1111, 0x500112, 0,      // D6 @87
            0x3100, 0x80276341, 0x80337311, 0x3B1301, 0,              //         C6 @93
            0x1000, 0x102220, 0x802DD132, 0x332201, 0x520123, 0,      // D7 @98
            0x1000, 0x101120, 0x80226321, 0x802D7311, 0x80386311, 0x401101,
            0x520112, 0,                                          //         C7 @104
            0x2000, 0x142230, 0x802DD132, 0x322201, 0x4C0223, 0,      // D8 @112
            0x1000, 0x0E3220, 0x802D7342, 0x322301, 0x530124, 0,      //         C8 @118
            0x1000, 0x0C3220, 0x802D5143, 0x362311, 0x540124, 0,      // D9 @124
            0x1000, 0x0C2120, 0x201131, 0x802D7321, 0x321101, 0x401212,
            0x540113, 0,                                          //         C9 @130
            0x1000, 0x0B4320, 0x802DD153, 0x323401, 0x540135, 0,      // D10 @138
            0x2000, 0x113230, 0x80296342, 0x80317311, 0x352301, 0x4F0224, 0, // C10 @144
            0x2000, 0x0F4330, 0x802DD153, 0x313401, 0x500235, 0,      // D11 @151
            0x1000, 0x0A2120, 0x1A2231, 0x802D7332, 0x312201, 0x441223,
            0x550113, 0,                                          //        C11 @157
            0x2000, 0x0E1130, 0x182221, 0x802D5133, 0x342211, 0x461123,
            0x500212, 0,                                          // D12 @165
            0x1000, 0x0A2120, 0x183231, 0x802D7342, 0x302301, 0x461224,
            0x550113, 0,                                          //        C12 @173
            0x2000, 0x0D2130, 0x1A2231, 0x802D5133, 0x332211, 0x431223,
            0x510213, 0,                                          // D13 @181
            0x3000, 0x114340, 0x802A6353, 0x80307311, 0x333401, 0x4D0335, 0, // C13 @189
            0x2000, 0x0C3230, 0x1E2242, 0x802DD132, 0x302201, 0x402323,
            0x520224, 0,                                          // D14 @196
            0x2000, 0x0C3230, 0x1E2242, 0x802D7332, 0x302201, 0x402323,
            0x520224, 0,                                          //        C14 @204
            0x2000, 0x0B3230, 0x1C2242, 0x802D5133, 0x332211, 0x412323,
            0x520224, 0,                                          // D15 @212
            0x2000, 0x0B2130, 0x174331, 0x802D7353, 0x303401, 0x461235,
            0x520213, 0,                                          //        C15 @220
            0x3000, 0x0E3240, 0x1E2242, 0x802D5133, 0x322211, 0x402323,
            0x500324, 0,                                          // D16 @228
            0x3000, 0x0E2140, 0x194331, 0x802D7353, 0x303401, 0x441235,
            0x500313, 0, 0};                        // (D17 @244) //        C16 @236
    private static int SceneTall = 0, SceneWide = 0, SceneBayer = 0,
            NuData = 0, ShoSteer = 0, SteerWhee = 0, GasBrake = 0, LookFrame = 2,
            SpecGas = 0, SpecWheel = 0, CarTest = 0, tRadix = 0, t2Radix = 0,
            nClients = 0, ZoomFocus = 0, GroundsColors = 0, nBax = 0,
            ZooMapTopL = 0, ZooMapDim = 0, ZooMapShf = 0, ZooMapBase = 0,
            Wally = 0, Darken = 0, MapIxBase = 0, MapHy = 0, MapWy = 0, Log_AxL = 0,
            ImageWide = 0, nuIxBase = ArtBase + 4, FrameNo = 0, // incr'd each rebuild
            NextFrUpdate = 0, ProcessingTime = 0, OpMode = 0, DroppedFrame = 0,
            FakeTimeBase = 0, RealTimeBase = 0, // time base for artifact animations
            FakeRealTime = 0, RealTimeNow = 0, TimeBaseSeq = 0, PixScale = 0,
            nCrumbs = 0, NumFax = 0, RectMap = 0, // RecenLnLoc = 0, RecenLnPtr = 0,
            TopCloseView = 0, SeePaintTopL = 0, SeePaintSize = 0, SeePaintImgP = 0,
            TmpI = 0, TripLine = 0, SameData = 0, SeePixBox = 0, GotPebz = 0,
            WideRatio = 0, PreLeft = 0, PreRite = 0, RoShift = 0,
            FloorOff = 0, FloorDims = 0, GrasColo = 0x00FF00, GrasDk = 0x009900,
            WhitLnColo = DriverCons.D_WhitLnColo, PebblSize = DriverCons.D_PebblSize,
            PavColo = 0x666666, PavDk = 0x333333,
            CarTall = (int) Math.round(CameraHi * 16.0), Tally, BackWall = Back_Wall,
            WallCo5 = Back_Wall, WallCo6 = unPainted, WallCo7 = unPainted,
            PilasterCo = DriverCons.D_PilColo, CreamWall = DriverCons.D_CreamWall,
            DarkWall = DriverCons.D_DarkWall, CeilingCo = DriverCons.D_CeilingCo,
            PebContrast = DriverCons.D_PebContrast; // LoLumRo = 0, nLumins = 0;
    private static double Velocity = 0.0, VuEdge = 0.0, // SloMotion = 0.0,
            ShafTurns = 0.0, ScaledShaft = 1.0 / DriverCons.D_MetersPerTurn,
            ZoomRatio, effTurnRad = TurnRadius, // effective = nearest we can see
            epsilon = MyMath.Fix2flt(256, 0) - MyMath.Fix2flt(0xFFFFF, 12), // tiny
            CoFriction = DriverCons.D_CoefFriction, // Torque = 0.0, AccelForc = 0.0,
            RubberGrip = DriverCons.D_CoefFriction * PkGrav * CoFudge,
    // skid acc (G=9.81*8)=1 @ cf=50% 5fps
    RubberTurn = RubberGrip * 256.0, // ditto, but for turns (scaled)
            PebBlur = (1 << PebblSize) / 32.0, mpSpix = 0.0,
            WallAim = 0.0, AverageAim = 0.0, Facing = 0.0, Vposn = 0.0, Hposn = 0.0,
            Vcarx, Hcarx, Lcarx, Rcarx, Tcarx, Bcarx, // used to find car position
            fZoom, Dzoom, fSpeed, fSteer, FltWi, fImHaf, fMapWi, WiZoom,
            OdomDx = 0.0, Speedom = 0.0, VcoFace = 0.0, HsiFace = 0.0, CashTan = 0.0,
            CashDx = 0.0, CeilingHi = 0.0, ZooMapScale, ZooMapWhLn,
            WhitLnSz = WhiteLnWi * 1.4, Vsee, Hsee, Voffm, Hoffm;
    private static boolean StepOne = false, SimBusy = false, ClockTimed = false,
            DarkOnce = false, Moved = false, unScaleStee = false, ReloTabs = false,
            Skidding = false, InWalls = false, Braking = false, IsProxim = false,
            SeenHedLite = false, ShowOdometer = false, ValidPixSteps = false,
            SimActive = true, ShoTrkTstPts = DriverCons.D_ShoTrkTstPts,
            SimSpedFixt = DriverCons.D_FixedSpeed,
            SimInTrak = DriverCons.D_StayInTrack, Shifty;
    private static String Save4Log = "", RtnStr, TempStr;
    private static int[] LuminanceMap = null; // each int applies to 1/4 grid
    private static int[] ShoHeadLite = null; // to draw outline, if so
    private static int[] ShoLumiLox = null; // interesting locations to log
    private static int[] RowCeiling = null; // pixel row for ceiling by floor row
    private static int[] PebleTrak = null; // used 0..9 for pebbled track
    private static int[] RangeRow = null; // scrn row indexed by dx in 6cm steps
    private static int[] RowRange = null; // dx in m/16 (6cm units) ix'd by row
    private static int[] MapIndex = null;
    private static int[] WaLayerz = null; // temp, used only in SortLay
    private static int[] WalzTall = null;
    private static int[] SeenWall = null;
    private static int[] WallColoz = null;
    private static int[] theFax = null; // used to sort arts by distance for disp
    private static int[] AnimInfo = null;
    private static int[] DidCells = null;
    private static int[] TrakImages = null;
    private static int[] myScreen = null;
    private static int[] PixelSteps = null;
    private static int[] PrioRaster = null;
    private static int[] BreadCrumbs = null;
    private static int[] GridLocTable = null;
    private static double[] RasterMap = null;
    // + " 60!     55 78 274^172 36~16=16 -- BlueCar front\n"
    // + " 32+45!  54 157 190^2  36~17=16 -- BlueCar right-front\n"
    // + " 60+90!  54 155 352^114 36~18=16 -- BlueCar right\n"
    // + " 32+135! 54 157 352^58 36~19=16 -- BlueCar right-back\n"
    // + " 60+180! 55 78 190^172 36~20=16 -- BlueCar back\n"
    // + " 32+225! 54 157 190^58 36~21=16 -- BlueCar left-back\n"
    // + " 60+270! 54 155 190^114 36~22=16 -- BlueCar left\n"
    // + " 0!      54 157 352^2  36~23=16 -- BlueCar left-front";
    final int[] TinyBits = { //    *       *     * *     * *         *   * * *     * *   * * *
            0x25552, 0x22227,  //  *   *     *         *       *   *   *   *       *           *
            0x61247, 0x61216,  //  *   *     *       *       *     *   *   * *     * *       *
            0x15571, 0x74616,  //  *   *     *     *           *   * * *       *   *   *   *
            0x34652, 0x71244,  //    *     * * *   * * *   * *         *   * *       *     *
            0x25252, 0x25316,  //    *       *     *       *  * * *     * *  *       *  0x11244 *
            0x49D9B9, 0x74647,  //  *   *   *   *   * *     *  *       *      *       *          *
            //    *       * *   *   *   *  * *       *    *   *   *        *
            0x34216, 0x699996,  //  *   *       *   *     * *  *           *  *   *   *      *
            2, 0x00700};  //    *     * *     *       *  * * *   * *      *   *    *   *


    private final int[] Grid_Locns = {6, 12, 18, 20, 28, 31, // (GridLocT) block index
            0, 20, ImHaf - 40, ImHaf - 2, ImHi - DrawDash, ImHi,  // vert div'ns
            0, 20, ImMid - 10, ImMid + 10, ImWi - 20, ImWi, 0, ImWi, // top horz div'ns,
            0, ImWi / 7, ImWi * 2 / 7, ImWi * 3 / 7, ImWi * 4 / 7, ImWi * 5 / 7, ImWi * 6 / 7, ImWi, // mid
            0, 20, ImWi, // ImWi/4, ImWi/2, ImWi*3/4, ImWi, // no gas posns (obsolete)
            0, 20, ImMid, ImWi - 20, ImWi};                   // bottom horz div'ns

    private final int[] TapedWheel = {-2 * 65536 + 0 * 256 + 12,         // 0..0,
            2 * 65536 + 1 * 256 + 17, 5 * 65536 + 3 * 256 + 22, 9 * 65536 + 6 * 256 + 28,       // 1..3,
            12 * 65536 + 10 * 256 + 34, 15 * 65536 + 15 * 256 + 41, 18 * 65536 + 22 * 256 + 48, // 4..6,
            21 * 65536 + 31 * 256 + 56, 23 * 65536 + 42 * 256 + 64, 25 * 65536 + 54 * 256 + 73, // 7..9,
            28 * 65536 + 67 * 256 + 83, 33 * 65536 + 81 * 256 + 92,                   // 10..11
            0x1FE000, 0x1FE000, 0x1FE000, 0x1FE000, 0,            // 0 @12/-2
            0x1FE000, 0x1FE000, 0x1FE000, 0x1F8000, 0,            // 1 @17/+2
            0x6000, 0x1FF000, 0xFF000, 0xFF000, 0xF0000, 0,        // 2 @22/+5
            0xF000, 0xFF800, 0x7F800, 0x7E000, 0x70000, 0,         // 3 @28/+9
            0x1800, 0xFC00, 0x7FC00, 0x3F800, 0x3E000, 0x18000, 0,  // 4 @34/12
            0x600, 0x1E00, 0x7F00, 0x1FC00, 0xF800, 0xE000, 0,      // 5 @41/15
            0x300, 0x780, 0x1F80, 0x7F00, 0x7C00, 0x3800, 0x2000, 0, // 6 @48/18
            0xC0, 0x3E0, 0x7C0, 0x1F80, 0x1F00, 0x0E00, 0x400, 0,    // 7 @56/21
            0x30, 0x78, 0xF8, 0x1F0, 0x3E0, 0x7C0, 0x380, 0x100, 0,   // 8 @64/23
            0x4, 0xE, 0x1F, 0x3E, 0x7C, 0xF8, 0xF0, 0x60, 0x20, 0,     // 9 @73/25
            0x1, 0x3, 0x7, 0xF, 0x1F, 0x1E, 0xC, 0x4, 0,             // 10 @83/28
            1, 3, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // lsb: scrn botm  // 11 @92/33;  [12 @97/38]
    private final int[] PiDigits = {0x31415926, 0x53589793,
            0x23846264, 0x33832795, 0x02884197, 0x16939937, 0x51058209, 0x74944592,
            0x30781640, 0x62862089, 0x98628034, 0x82534211, 0x70679821, 0x48086513,
            0x28230664, 0x70938446, 0x09550582, 0x23172535, 0x94081284, 0x81117450,
            0x28410270, 0x19385211, 0x05559644, 0x62294895, 0x49303819, 0x64428810,
            0x97566593, 0x34461284, 0x75648233, 0x78678316, 0x52712019, 0x09145648,
            0x56692346, 0x03486104, 0x54326648, 0x21339360, 0x72602491, 0x41273724,
            0x58700660, 0x63155881, 0x74881520, 0x92096282, 0x92540917, 0x15364367,

            0x89259036, 0x00113305, 0x30548820, 0x46652138, 0x41469519, 0x41511609,
            0x43305727, 0x03657595, 0x91953092, 0x18611738, 0x19326117, 0x93105118,
            0x54807446, 0x23799627, 0x49567351, 0x88575272, 0x48912279, 0x38183011,
            0x94912983, 0x36733624, 0x40656643, 0x08602139, 0x49463952, 0x24737190,
            0x70217986, 0x09437027, 0x70539217, 0x17629317, 0x67523846, 0x74818467,
            0x66940513, 0x20005681, 0x27145263, 0x56082778, 0x57713427, 0x57789609,
            0x17363717, 0x87214684, 0x40901224, 0x95343014, 0x65495853, 0x71050792,

            0x27968925, 0x89235420, 0x19956112, 0x12902196, 0x08640344, 0x18159813,
            0x62977477, 0x13099605, 0x18707211, 0x34999999, 0x83729780, 0x49951059,
            0x73173281, 0x60963185, 0x95024459, 0x45534690, 0x83026425, 0x22308253,
            0x34468503, 0x52619311, 0x88171010, 0x00313783, 0x87528865, 0x87533208,
            0x38142061, 0x71776691, 0x47303598, 0x25349042, 0x87554687, 0x31159562,
            0x86388235, 0x37875937, 0x51957781, 0x85778053, 0x21712268, 0x06613001,
            0x92787661, 0x11959092, 0x16420198, 0x93809525, 0x06548586, 0x32788659};
    private int Left_X, Rite_X, PreViewLoc, PreViewAim, ScrnRoCo = 0;
    private SimHookBase SerialCalls = null;


    public TrakSim() {
        StartPatty("TrakSimCons");
    }

    private static double Cast_I2F(int whom) {
        // convert int rep'n of fix-point -> float..
        return MyMath.Fix2flt(whom, 12);
    } //~Cast_I2F

    private static int Cast_F2I(double whom) { // cvt to int rep'n of fix-point..
        if (MyMath.fAbs(whom) >= 524288.0) return 0;
        return MyMath.Trunc8(whom * 4096.0);
    } //~Cast_F2I

    public static void LineSlope(double Vat, double Hat, double Vstp,
                                 double Hstp, boolean logy, String msg) { // result -> Voffm,Hoffm; logy=F
        // As if facing northward, V/Hat: S,E coordinates of any point along line
        //   path [M*V+H = K], and V/Hstp: cos,sin of c-wise angle frm north,
        //   so that Vat-Vstp,Hat+Hstp advances (one unit) in direction facing.
        // Voffm is multiplier M, Hoffm is constant K, in line equation along path
        //   where for any point (V,H), M*V+H<K if (V,H) is left of the line
        // Math.abs(Vstp)>Math.abs(Hstp); Use Vstp=0 to repeat previous V/Hstp
        if (Vstp == 0.0) {             // Called only from BuildFram to calc Vcarx etc
            Vstp = Vsee;
            Hstp = Hsee;
        } //~if
        if (Vstp == 0.0) Voffm = Vstp;
        else Voffm = Hstp / Vstp;
        Hoffm = Voffm * Vat + Hat;
        Vsee = Vstp;
        Hsee = Hstp;
        if (Mini_Log) if (logy) // if (Qlog..) -- included in M_L
            System.out.println(HandyOps.Flt2Log("(LinSlope) " + msg, Hoffm,
                    HandyOps.Flt2Log(" *", Voffm, HandyOps.Flt2Log(" (", Vat,
                            HandyOps.Flt2Log("/", Hat, HandyOps.Flt2Log(" ", Vstp,
                                    HandyOps.Flt2Log("/", Hstp, ")")))))));
    } //~LineSlope

    public static int EncoPavEdge(int tall, int wide, int recell, int why) {
        int eps, bitz = 0x80000000; // see bits in (BuildFram)
        // tall = HiWord(xcell)-HiWord(ocell); wide = LoWord(xcell-ocell);
        //  .. tall:wide is a proportion only (the line slope)
        // recell = xcell+ocell, // = 2*(center of line) = 25cm units
        //  .. any point on the line should work the same
        double Mconst, Kconst;     /** might could revise this to use LineSlope? **/
        boolean EW = false, grtr = false; // log only
        if ((MyMath.iAbs(tall) > MyMath.iAbs(wide)) || (why < 0)) { // more N-S than E-W,
            bitz = 0xA0000000;         // ..or else tagged diagonal (west in cell)
            if (tall < 0) bitz = bitz | 0x40000000; // going north, paved to west (<)
            else grtr = true; // otherwise going south, paved to east (>)
            Mconst = -MyMath.Fix2flt(wide, 0); // (coerced to float..)
            if (tall == wide) Mconst = -1.0;
            else if (tall + wide == 0) Mconst = 1.0;
            else if (wide != 0) Mconst = Mconst / tall; // (..so this is float divide)
            Kconst = Mconst * (recell >> 16) + MyMath.SgnExt(recell);
        } //~if // in 25cm units
        else { // edge line more E-W than N-S..       // ..(must decode meters)
            EW = true; // EW: (bitz&0x20000000 ==0)
            if (wide > 0) bitz = bitz | 0x40000000; // going east, paved to north (<)
            else grtr = true; // otherwise going west, paved to south (>)
            Mconst = -MyMath.Fix2flt(tall, 0);
            if (tall == wide) Mconst = -1.0;
            else if (tall + wide == 0) Mconst = 1.0;
            else if (tall != 0) Mconst = Mconst / wide;
            Kconst = Mconst * MyMath.SgnExt(recell) + (recell >> 16);
        } //~else
        eps = Cast_F2I(Mconst);
        if (eps >= 0x1000) Mconst = Cast_I2F(0xFFF);
        else if (why != 0) if (eps + 1 == 0) Mconst = Cast_I2F(eps - 1);
        Kconst = Kconst + MyMath.Fix2flt(32, 12);
        // RatLib floats are 1-bit sign, 19-bit integer, 12-bit fraction..
        //   masking Mconst&0x1FFF gets only signed fraction (&0x1000 is sign)
        //   Kconst has worst case range (45� @ bot corner = 2*(-399..+910))
        //     for 1-bit sign + 11-bit int + [31-2-13-12 =] 4-bit fract;
        //     implicit /4 then +5 shift here puts decode bin.pt in bit 19.
        bitz = ((Cast_F2I(Kconst) & 0xFFFFC0) << 5) // must decode(/4) into meters
                + ((Cast_F2I(Mconst) & 0x1FFF) >> 2) + bitz;           // || Mini_Log
        if (Log_Draw || NoisyMap) if (Qlog < 0) if (SameData != bitz) {
            SameData = bitz;
            System.out.println(HandyOps.Hex2Log("(EPE) = x", bitz, 8,
                    HandyOps.Flt2Log(HandyOps.IffyStr(EW, " EW Kc=", " NS Kc="), Kconst,
                            HandyOps.Flt2Log(" Mc=", Mconst, HandyOps.Dec2Log(" ", tall,
                                    HandyOps.Dec2Log("/", wide, HandyOps.Int2Log(HandyOps.IffyStr(grtr,
                                            " > ", " < "), recell, HandyOps.Dec2Log(" ", why, ""))))))));
        } //~if
        return bitz;
    } //~EncoPavEdge

    /**
     * Changes the simulated coefficient of friction.
     *
     * @param newCoF The new coefficient of friction
     */
    public static void SetCoefFriction(double newCoF) {
        if (newCoF <= 0.0) return; // too low
        if (newCoF > 9.9) return; // too high
        CoFriction = newCoF;     // (PkGrav=9.81*8/25=3.1)
        RubberGrip = newCoF * PkGrav * CoFudge; // skid acc =1.6 @ cf=50% 5fps..
        RubberTurn = RubberGrip * 256.0;
        if (Qlog != 0) System.out.println(HandyOps.Flt2Log(" (SetCoFric) ", newCoF,
                HandyOps.Flt2Log(" ", RubberGrip, HandyOps.Flt2Log("/", RubberTurn,
                        HandyOps.Flt2Log(" G=", PkGrav, HandyOps.Flt2Log(" *", CoFudge,
                                HandyOps.Flt2Log(" (", fFtime,
                                        HandyOps.Flt2Log(" ", fFPS, ")"))))))));
    }

    /**
     * Converts contiguous white RGB pixels into transparent for BuildMap.
     * For best results, images should be semi-convex, where all outside
     * edges are visible from its containing rectangle (serpentine regions
     * take longer).
     *
     * @param tall    The image height, in pixel rows.
     * @param wide    The image width, in pixels.
     * @param theImgs An array of RGB pixels containing artifact images.
     */
    public static void WhiteAlfa(int tall, int wide, int[] theImgs) {
        final int Transprnt = DriverCons.D_Transprnt;
        int info, rx, cx, uppy = 0, here = 0, thar = -wide, tops = tall * wide;
        boolean seen = true;
        if (theImgs == null) return;
        if (tall < 4) return; // not a credible image array
        if (wide < 4) return;
        if (Mini_Log) if (NoisyMap) if (Qlog < 0)
            System.out.println(HandyOps.Dec2Log("  <WhitAlf> ", tall,
                    HandyOps.Dec2Log("/", wide, HandyOps.ArrayDumpLine(
                            theImgs,
                            20, 11))));
        for (rx = tall - 1; rx >= 0; rx += -1)
            for (cx = wide - 1; cx >= 0; cx += -1) { // top-to-bottom..
                if (here >= theImgs.length) break;
                info = theImgs[here];
                if (info >= 0xFFFFFF) { // otherwise either <0 or no hi byte
                    info = info & 0xFFFFFF;
                    if (seen) info = -1;
                    else if (thar < 0) info = -1;
                    else if (thar < theImgs.length) if (theImgs[thar] < 0) info = -1;
                    if (info < 0) uppy++;
                } //~if
                theImgs[here] = info; // either <0 or = 0x00xxxxxx
                if (cx == 0) seen = true;
                else seen = info < 0;
                here++;
                thar++;
            } //~for
        while (uppy != 0) {
            seen = true;
            here = tops;
            uppy = 0;
            thar = tops + wide;
            for (rx = tall - 1; rx >= 0; rx += -1)
                for (cx = wide - 1; cx >= 0; cx += -1) { // then bottom-up..
                    here--;
                    if (here >= theImgs.length) break;
                    info = theImgs[here];
                    thar--;
                    if (info != Transprnt) { // magical interior transparent =0xFEFEFE
                        if (info < 0xFFFFFF) {
                            seen = info < 0;
                            continue;
                        } //~if
                        if (seen) info = -1;
                        else if (thar < 0) info = -1;
                        else if (thar >= theImgs.length) info = -1;
                        else if (theImgs[thar] < 0) info = -1;
                    } //~if
                    else info = -1;
                    if (info < 0) {
                        theImgs[here] = info;
                        seen = true;
                        uppy--;
                    } //~if
                    else seen = cx == 0;
                } //~for
            if (uppy == 0) break;
            here = -1;
            seen = true;
            thar = -1 - wide;
            uppy = 0;
            for (rx = tall - 1; rx >= 0; rx += -1)
                for (cx = wide - 1; cx >= 0; cx += -1) { // then top-down again..
                    here++;
                    if (here >= theImgs.length) break;
                    info = theImgs[here];
                    thar++;
                    if (info < 0xFFFFFF) {
                        seen = info < 0;
                        continue;
                    } //~if
                    if (seen) info = -1;
                    else if (thar < 0) info = -1;
                    else if (thar >= theImgs.length) info = -1;
                    else if (theImgs[thar] < 0) info = -1;
                    if (info < 0) {
                        theImgs[here] = info;
                        seen = true;
                        uppy++;
                    } //~if
                    else seen = cx == 0;
                }
        } //~while               // Mini_Log=F..
        if (Mini_Log) if (NoisyMap) if (Qlog < 0)
            System.out.println(HandyOps.Dec2Log("  (WhitAlf) ",
                    tall, HandyOps.Dec2Log("/", wide, HandyOps.ArrayDumpLine(
                            theImgs,
                            20, 11))));
    } //~WhiteAlfa

    private static void HeadLiSho(int why, double deep,
                                  double Vat, double Hat) {                         // only from CeilingLi
        int whom = 0;
        if (ShoHeadLite == null) return;
        whom = ShoHeadLite[0];
        if (whom == 0) if (why > 0) {
            ShoHeadLite[0] = why;
            ShoHeadLite[1] = Cast_F2I(deep);
            ShoHeadLite[2] = Cast_F2I(Vat);
            ShoHeadLite[3] = Cast_F2I(Hat);
            SeenHedLite = true;
        } //~if   // see also: (HedShoLi)
        if (Qlog < 0) System.out.println(HandyOps.Int2Log("(HedLiSho) ", why,
                HandyOps.Int2Log("/", whom, HandyOps.Flt2Log(" ", deep,
                        HandyOps.Flt2Log(" @", Vat, HandyOps.Flt2Log("/", Hat, " --"
                                + HandyOps.ArrayDumpLine(ShoHeadLite, 0, 40)))))));
    } //~HeadLiSho

    private static void ValiPavColo() {
        // verify & force pavement colors to != non-track colors
        int nx, zx, info = GrasColo & 0xFCFCFC, bitz = GrasDk & 0xFCFCFC,
                more = 0x10101;
        // if ((GrasDk&-8)==0) GrasDk = GrasDk+more; // Color4ix did it
        if (GrasDk + more == GrasColo) more = 0x20202;
        else if (GrasColo + more == GrasDk) more = 0x20202;
        while (true) { // verify that adding works..
            nx = ((info + 0x40404) | (bitz + 0x40404)) & 0x1010100;
            if (nx == 0) break; // simple increment works
            if ((((info + 0x1FDFDFC) | (bitz + 0x1FDFDFC)) & 0x1010100) == 0) {
                more = -more; // simple decrement works
                break;
            } //~if
            nx = nx - (nx >> 8); // now it's a byte mask
            more = more - ((more & nx) << 1);
            break;
        } //~while
        if ((PavColo == GrasColo) || (PavColo == GrasDk)) {
            info = PavColo + more;
            if (MapLogged) if ((Qlog & 256) != 0)
                System.out.println(HandyOps.Colo2Log("^^^ PavColo adjusted from ", PavColo,
                        HandyOps.Hex2Log(" to x", info, 8, "")));
            PavColo = info;
        } //~if
        if ((PavDk == GrasColo) || (PavDk == GrasDk)) {
            info = PavDk + more;
            if (MapLogged) if ((Qlog & 256) != 0)
                System.out.println(HandyOps.Colo2Log("^^^ PavDk adjusted from ", PavDk,
                        HandyOps.Hex2Log(" to x", info, 8, "")));
            PavDk = info;
        } //~if
        for (nx = 0; nx <= 9; nx++) {
            info = PebleTrak[nx];
            if (info != GrasColo) if (info != GrasDk) continue;
            bitz = info + more;
            if (MapLogged) if ((Qlog & 256) != 0)
                System.out.println(HandyOps.Dec2Log("^^^ Peble[", nx,
                        HandyOps.Colo2Log("] adjusted from ", info,
                                HandyOps.Hex2Log(" to x", bitz, 8, ""))));
            PebleTrak[nx] = bitz;
        } //~for
        info = WhitLnColo;
        if ((info & -8) == 0) info = info + 0x10101;
        more = WhitLnColo & 0xC0C0C0;
        more = ((more << 1) | more) & 0x808080;
        bitz = more - (more >> 7); // (byte mask)
        if ((WhitLnColo & 0x808080) == 0) // all color components below 50%,
            more = 0x10101; // ..so general increment
        else if (more == 0x808080) more = -more; // all above 25%, decrement
        else if ((more & -more) == more) // only one component above 25%
            more = 0x10101 & (0xFFFFFF - bitz); // ..so mostly increment
        else more = -(0x10101 & bitz); // otherwise mostly decrement
        bitz = info;
        for (zx = 12; zx >= 0; zx += -1) { // worst case 12x thru through..
            for (nx = 0; nx <= 9; nx++) if (PebleTrak[nx] == bitz) bitz = bitz + more;
            if (PavColo == bitz) bitz = bitz + more;
            if (PavDk == bitz) bitz = bitz + more;
            if (bitz == info) break;
            info = bitz;
        } //~for
        if (info == WhitLnColo) return;
        if (MapLogged) if ((Qlog & 256) != 0)
            System.out.println(HandyOps.Colo2Log("^^^ WhitLnColo adjusted from ",
                    WhitLnColo, HandyOps.Hex2Log(" to x", info, 8, "")));
        WhitLnColo = info;
    } //~ValiPavColo

    private static int GotImgOps(String theList) {
        int res = HandyOps.NthOffset(0, "\nU", theList); // >0: track info has paint
        if (res < 0) if (HandyOps.NthOffset(0, "\nY", theList) < 0) // <0: other images
            if (HandyOps.NthOffset(0, "\nJ", theList) < 0)          // <0: other images
                if (HandyOps.NthOffset(0, "\n@", theList) < 0)
                    if (HandyOps.NthOffset(0, "\nO", theList) < 0) res = 0;
        if (Mini_Log) if (NoisyMap) if (Qlog < 0)    // Mini_Log=T, NoisyMap=F
            System.out.println(HandyOps.Dec2Log("  (GotImOp) ", res, ""));
        return res;
    } //~GotImgOps

    private static int Ad2PntMap(int rx, int cx, int img,
                                 int tall, int wide, int PaintIx, int[] myMap, int size) { // => RectMap
        int nx, yx, zx, abit, info = (rx << 16) + cx, more = (tall << 16) + wide - 0x10001,
                whar = 0, locn = 0, here = 0, thar = 0, why = 0;   // no early exits
        String aLine = "";
        while (true) {
            why++; // why = 1
            if (myMap == null) break;
            here = myMap[ArtBase + 2]; // ArtBase = 8, +3 for preface, needs +3 ditto
            why++; // why = 2
            if (here <= 0) break;
            if (here > myMap.length) break;
            why++; // why = 3
            if (PaintIx < 6) break;
            why++; // why = 4
            if (img == 0) break;
            why++; // why = 5
            here = here + 3; // cuz myMap includes 3-int header not there at runtime
            thar = here + 3 - PaintIx;
            if (thar < 8) break;
            if (thar > myMap.length - 4) break;
            why++; // why = 6
            if (myMap[thar + 2] != 0) break; // last error exit
            why++; // why = 7
            if (more == 0) more++; // 1x1 pix image allowed, but don't fail 0-test
            myMap[thar] = img;
            myMap[thar + 1] = info; // locn&dims both in 25cm units if lo-res,
            myMap[thar + 2] = more; // but if hi-res, dims only in 3cm units
            tall--; // convert to meters-1..
            wide--;
            if ((img & 0x04000000) != 0) { // hi-res..
                tall = tall >> 3;
                wide = wide >> 3;
            } //~if
            wide = ((cx & 3) + wide) >> 2;
            tall = tall >> 2;
            PaintIx = PaintIx - 3;
            cx = cx >> 2; // now in meters (8 bits)
            rx = rx >> 2; // now in meters
            zx = 1 << (31 - (cx & 31));
            locn = (rx << 3) + (cx >> 5) + here; // pack 32/int (3 bits remaining)
            for (yx = tall; yx >= 0; yx += -1) {
                whar = (yx << 3) + locn; // now includes +3 for hdr
                abit = zx;
                for (nx = wide; nx >= 0; nx += -1) {
                    if (whar >= here) if (whar < here + size)
                        myMap[whar] = myMap[whar] | abit;
                    if (abit < 0) abit = 0x40000000;
                    else if (abit < 2) {
                        abit = 0x80000000;
                        whar++;
                    } //~if
                    else abit = abit >> 1;
                }
            } //~for
            why = 0; // success!
            break;
        } //~while               // Mini_Log=F..
        if (Mini_Log) if (Qlog < 0) {
            System.out.println(HandyOps.Dec2Log("(AdPnt2Mp) ", locn,
                    HandyOps.Dec2Log(" +", tall, HandyOps.Dec2Log("/", wide,
                            HandyOps.Dec2Log(" -> ", PaintIx, HandyOps.Dec2Log(" @ ", thar,
                                    HandyOps.Dec2Log(" => ", img & 0xFFFFFF, HandyOps.Dec2Log("+", img >> 24,
                                            HandyOps.Int2Log("/", info, HandyOps.Int2Log("/", more,
                                                    HandyOps.Dec2Log(" @ ", here,
                                                            HandyOps.Dec2Log(" = ", why, aLine))))))))))));
        } //~if   // why =
        return PaintIx;
    } //~Ad2PntMap

    private static double Korner(int shft, int ix0, int logy) {
        int zx = (ix0 >> shft) & 255, kx = zx & 1; // unpack face ends from ix0
        double rez = 0.0;                    // called from BackIm=1, DoWall=2/3
        if (kx != 0) rez = epsilon;
        rez = MyMath.Fix2flt(kx + zx, 0) - rez; // rtns park meters
        if (Mini_Log) if (logy > 0) if ((Qlog < 0) || (logy > 99))
            System.out.println(HandyOps.Dec2Log("  (Korn/", logy,
                    HandyOps.Dec2Log(") ", shft, HandyOps.Hex2Log(" x", ix0, 8,
                            HandyOps.Dec2Log(" ", zx, HandyOps.Flt2Log(" => ", rez, ""))))));
        return rez;
    } //~Korner

    private static void MapBucket(int deep, int rx, int cx,
                                  int colo, int[] myMap) {                                      // "(Bkt) '"
        int thar = rx * HalfMap + cx + nuIxBase, bitz = colo ^ 0x22222222, info = 0;
        boolean seen = false;
        while (true) {
            if (myMap == null) break;
            if (cx < 0) break;
            if (rx < 0) break;
            if (cx >= HalfMap) break;
            if (thar < 0) break;
            if (thar >= myMap.length) break;
            info = myMap[thar];
            if (info == colo) break;
            if (info != 0) if (info != bitz) break;
            seen = true;
            if (Log_Draw || NoisyMap) if (Qlog < 0) {
                if (TmpI > 72) {
                    TempStr = TempStr + "\n    ";
                    TmpI = 0;
                } //~if
                TempStr = TempStr + HandyOps.Dec2Log(" ", rx,
                        HandyOps.Dec2Log("/", cx, HandyOps.IffyStr(info == 0, "", "`")));
                TmpI = TmpI + 8;
            } //~if
            myMap[thar] = colo;
            if (cx <= 0) break;
            if (rx <= 0) break;
            if (thar <= 8) break;
            if (thar > myMap.length - 2) break;
            info = myMap[thar + 1];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx, cx + 1, colo, myMap);
            info = myMap[thar - 1];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx, cx - 1, colo, myMap);
            if (thar > myMap.length - (HalfMap + 2)) break;
            if (thar < HalfMap) break;
            info = myMap[thar + HalfMap];
            if (info != colo) if (info == bitz) info = 0;
            if (info == 0) MapBucket(deep + 1, rx + 1, cx, colo, myMap);
            info = myMap[thar - HalfMap];
            if (info != colo) if (info == bitz) info = 0;
            if (info != 0) break;
            thar = thar - HalfMap;
            rx--;
        } //~while
        if (Log_Draw || NoisyMap) if (Qlog < 0) if (deep == 0) if (!seen)
            TempStr = TempStr + HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                    HandyOps.Colo2Log(" = ", colo, HandyOps.Hex2Log(" / x", info, 8,
                            HandyOps.Dec2Log(" @ ", thar, "")))));
    } //~MapBucket

    private static void LumiLogg() { // logs ShoLumiLox pts
        int rx, cx, here, thar, info, lxx = 0, tops = ShoLumiLox[0];
        int[] theLum = LuminanceMap;
        if (Qlog == 0) return;
        if (theLum == null) return;
        lxx = theLum.length;
        if (!Log_Draw) if (!NoisyMap) return; // NoisyMap=F
        for (here = 1; here <= tops; here++) {
            if (here > 31) break;
            info = ShoLumiLox[here];
            rx = info >> 16;
            cx = info & 0xFFFF;
            info = -1;
            thar = rx * MapWide + cx;
            if (thar >= 0) if (thar < theLum.length) info = theLum[thar];
            System.out.println(HandyOps.Dec2Log("Lumin # ", here, HandyOps.Dec2Log(" ", rx,
                    HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" = ", info,
                            HandyOps.Dec2Log(" @ ", thar, HandyOps.IffyStr(here > 1, "",
                                    HandyOps.Dec2Log("/", lxx, HandyOps.Dec2Log(" ", tops,
                                            " .." + HandyOps.ArrayDumpLine(ShoLumiLox,
                                                    tops + 2, 44))))))))));
        }
    } //~LumiLogg

    private static void LumiAdLogPt(int rx, int cx) { // rx,cx in pk meters
        int here = 1, info = (rx << 16) + cx;
        if (Qlog == 0) return;
        while (true) {
            if (here > ShoLumiLox[0]) break;
            if (here > 31) break;
            if (ShoLumiLox[here] == info) break;
            here++;
        } //~while
        // here = ShoLumiLox[0]+1;
        if (here > 31) return;
        if (here > ShoLumiLox[0]) {
            ShoLumiLox[here & 31] = info;
            ShoLumiLox[0] = here;
        } //~if
        else here = -here; // so it shows different in log
        System.out.println(HandyOps.Dec2Log("+LumAdPt # ", here,
                HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx, ""))));
    } //~LumiAdLogPt

    private static int Luminator(char xCh, double Vat, double Hat,
                                 int nxt, int dimz, int lino, String aLine, String theList) {
        boolean shade, soft;
        int zx, yx, kx, aim, here, thar = MapAsize,
                rpt = -95, tall = dimz >> 16, wide = dimz & 0xFFFF,
                rx = MyMath.Trunc8(Vat), cx = MyMath.Trunc8(Hat),
                info = 0, more = 0, colo = 0, whom = 0, xcell = 0;
        boolean preSc = false;
        String aWord = "", xStr, aStr;
        double Lkon = 0.0, Lmpy = 0.0, Rkon = 0.0, Rmpy = 0.0, dark = 0.0,
                brite = 0.0, Hsn = 0.0, Vcs = 0.0, deep = 0.0, near = 0.0,
                Vstp = 0.0, Hstp = 0.0, whar, hix, radius = 96.0, far = 63.0,
                wix = 310.0, scale = MyMath.Fix2flt(1 << LumUniShif, 0);
        // Vat = MyMath.Fix2flt(rx,0), Hat = MyMath.Fix2flt(cx,0);
        int[] theLum = LuminanceMap;
        while (true) { // once thru..
            if (xCh == '>') info++;
            else if (xCh == '=') info--;
            if (info != 0)
                colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
            if (theLum == null) {
                if (info <= 0) break; // rpt = -96
                if (colo != 0) break;
            } //~if // 0-brite lumin needs no LumMap
            if (xCh == ';') { // log luminance point................................ ;
                rpt = -1;
                if (rx < 0) ShoLumiLox[0] = 0; // clear
                else LumiAdLogPt(rx, cx);
                break;
            } //~if // case '='
            else if (info == 0) break; // not ; or = or >
            rpt++; // rpt = -95
            if (rx <= 0) break; // coords in park meters, not grid units..
            if (cx <= 0) break;
            if (info < 0) { // if (xCh == '=') set flat luminance rect................ =
                rpt = -97;
                if (nxt < rx) break;
                if (nxt >= tall) break;
                yx = colo;
                if (yx < cx) break;
                if (yx >= wide) break;
                rpt--; // rpt = -98
                colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                if (colo > (8 << LumUniShif)) break; // 8x added brightness is too much
                LumiAdLogPt(rx, cx);
                LumiAdLogPt(nxt, yx);
                for (rx = rx; rx <= nxt; rx++) {
                    zx = rx * MapWide + cx;
                    for (rpt = cx; rpt <= yx; rpt++) {
                        if (zx < 0) break;
                        if (zx >= theLum.length) break;
                        theLum[zx] = colo;
                        zx++;
                    }
                } //~for
                LumiLogg();
                rpt = -1; // no error
                break;
            } //~if // case '='
            rpt++; // rpt = -94 // (info>0) add luminance light source.............. >
            if (nxt < 0) break; // +4: height (in park meters) of point-source
            if (nxt > 4000) break; // too high, use flat luminance rect
            rpt++; // rpt = -93
            if (colo < 0) break;                        // +5: brightness
            xStr = HandyOps.NthItemOf(false, 7, aLine); // +7: tall
            aStr = HandyOps.NthItemOf(false, 8, aLine); // +8: wide
            yx = HandyOps.SafeParseInt(xStr);
            zx = HandyOps.SafeParseInt(aStr);         // +10: ppm..
            rpt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 10, aLine));
            kx = rpt * nxt; // -kx is implied file pix below image to ground
            if (yx > 0) if (zx > 0) if (kx > 0) if (kx < 30000) { // else no display..
                aWord = " " + xStr + " " + aStr + " "   // +9: img file posn..
                        + HandyOps.NthItemOf(false, 9, aLine) + " ";
                preSc = true;
            } //~if
            xStr = HandyOps.NthItemOf(false, 6, aLine); // +6: aim+width<radius>minrad
            info = HandyOps.SafeParseInt(xStr);
            if (info == 0) if (HandyOps.SafeCompare(xStr, "0") != 0)
                info = 360; // default view range = 60
            more = info | 512;    // ..otherwise (if unspec'd angle) range = 360
            // if (colo==0) {if (info==0) more = 360; else more = info;} //~if
            if (preSc) { // build display data..
                preSc = false; // done with this
                aim = HandyOps.NthOffset(0, " --", aLine); // preserve comment, if any
                yx = (int) Math.round(Vat * 4.0);
                zx = (int) Math.round(Hat * 4.0);
                aWord = HandyOps.Fixt8th("\nO ", yx, HandyOps.Fixt8th(" ", zx,
                        HandyOps.Dec2Log(" ", more, HandyOps.Dec2Log(" -", kx,
                                HandyOps.Dec2Log(aWord, rpt, " 1")))))
                        + HandyOps.IffyStr(aim <= 0, " -- Luminary",
                        HandyOps.RestOf(aim, aLine));
                theList = HandyOps.RepNthLine(aWord, lino, theList);
            } //~if
            more = 0;
            if (colo == 0) { // special-case no luminance, just draw it
                rpt = -1; // no error
                break;
            } //~if
            LumiAdLogPt(rx, cx);
            if (ShoLumiLox[0] > 1) { // (if cleared & not restored, don't add more)
                if (rx < (tall >> 1)) yx = 5;
                else yx = -5;
                if (cx < (wide >> 1)) kx = 5;
                else kx = -5;
                for (zx = 0; zx <= 111; zx++) { // also log a diagonal out from center..
                    if (rx + yx < 0) break;
                    if (rx + yx >= tall) break;
                    if (cx + kx < 0) break;
                    if (cx + kx >= wide) break;
                    if ((zx < 5) || ((zx & 1) == 0)) LumiAdLogPt(rx + yx, cx + kx);
                    if (yx < 0) yx = yx - 5;
                    else yx = yx + 5;
                    if (kx < 0) kx = kx - 5;
                    else kx = kx + 5;
                }
            } //~if //~for
            if (nxt > 99) { // nxt is altitude in park meters
                zx = nxt * nxt; // colo = relative luminance, 4K=nominal @ 20m..
                if (colo < zx) break; // too dim for this high (no effect)
                if (colo > zx * 8) {
                    if (nxt > 500) break; // too bright all over
                    if (colo > zx * 16) break;
                }
            } //~if // too bright (wash out center)
            if (colo < 500000) { // prescaled..
                brite = MyMath.Fix2flt(colo, 10) * scale;
                preSc = true;
            } //~if
            else brite = MyMath.Fix2flt(colo, 10);
            if (NoisyMap || MapLogged) aStr = "";
            shade = false;
            rpt = 0;
            aim = info;
            if (aim > 0) { // we have directionality/shadows.......................... +
                zx = HandyOps.NthOffset(0, "+", xStr);
                if (zx > 0) more = MyMath.iMax(MyMath.iMin(HandyOps.SafeParseInt(
                        HandyOps.RestOf(zx + 1, xStr)), 180), 0);
                zx = HandyOps.NthOffset(0, "<", xStr);
                if (zx > 0) {
                    wix = HandyOps.SafeParseFlt(HandyOps.RestOf(zx + 1, xStr));
                    if (wix < 1.0) wix = 0.0; // unspecified radius is infinite
                    else if (wix >= 250.0) wix = 310.0;
                } //~if // ignore infinite radius
                if (wix < 256.0) {
                    if (wix == 0.0) { // 0 counts as effectively infinite: max actual..
                        wix = 310.0;     // ..radius^2 = 256^2+200^2 =95536 =310^2,
                        radius = 96.0;   // ..so radius^2/100 = 96
                        far = 63.0;
                    } //~if // soft-edge threshold
                    else {
                        radius = wix * wix * 0.1; // ready to compare..
                        if (wix < 3.0) hix = 0.0;
                        else if (wix > 32.0) hix = wix - wix / 32.0;
                        else hix = wix - 0.75;
                        if (hix > 0.0) far = hix * hix * 0.1;
                        else far = radius;
                    }
                } //~if
                // else radius = 96.0;
                zx = HandyOps.NthOffset(0, ">", xStr);
                if (zx > 0) {
                    deep = HandyOps.SafeParseFlt(HandyOps.RestOf(zx + 1, xStr));
                    if (deep > wix) deep = 0.0; // ignore shadow >= radius
                    else if (deep > 250.0) deep = 0.0; // ignore infinite shadow radius
                    else if (deep < 0.0) deep = 0.0;
                } //~if
                dark = deep - 0.75;
                if (dark >= 1.0) near = dark * dark * 0.01; // soft-edge threshold
                dark = deep * deep * 0.01; // ready to compare
                if (near == 0.0) if (dark > 0.0) near = dark;
                if (far < dark) far = dark;
                if (NoisyMap) if (MapLogged) { // NM=F // if (Qlog..) -- in M_L
                    xStr = "";
                    if (deep > 0.0) {
                        soft = (dark < 10.0); // scaled *100 for log only
                        if (near > 0) {
                            if (soft) aStr = "%-";
                            else aStr = "-";
                            xStr = HandyOps.Flt2Log(aStr, near, "");
                        } //~if
                        if (soft) xStr = HandyOps.Flt2Log(" => ", dark * 100.0, xStr);
                        else xStr = HandyOps.Flt2Log(" => ", dark, xStr);
                        xStr = HandyOps.Flt2Log(" >", deep, xStr);
                    } //~if
                    else xStr = "";
                    if (wix > 0.0) {
                        soft = (radius < 10.0); // scaled *100 for log only
                        if (far > 0) {
                            if (soft) aStr = "%-";
                            else aStr = "-";
                            xStr = HandyOps.Flt2Log(aStr, far, xStr);
                        } //~if
                        if (soft) xStr = HandyOps.Flt2Log(" => ", radius * 100.0, xStr);
                        else xStr = HandyOps.Flt2Log(" => ", radius, xStr);
                        xStr = HandyOps.Flt2Log(" <", wix, xStr);
                    } //~if
                    xStr = " aim=" + HandyOps.Dec2Log("", aim,
                            HandyOps.Dec2Log(" ^", more, xStr));
                } //~if
                if (more == 0) more = 90; // unspecified or 0 view angle is 180
                //   Given a point (r,c) and a ray angle a (c-wise from north)
                // through it {more horizontal than vertical, let h=sin(a)
                // and v=cos(a)} so the equation of the line(ray) is y=m*x+k
                // which can be solved for m and k, by finding a 2nd point
                // (p,q) on the same line, where p=r-v and q=c+h, then we
                // have two equations r=m*c+k and p=m*q+k {r-v=m*(c+h)+k} in
                // two unknowns: k=r-m*c, r-v=m*(c+h)+r-m*c, m*h=-v, m=-v/h.  <<=== H
                // We know h=sin(a) is non-zero, so the result is well-defined.
                // For more vertical rays, flip x/y axes so the equation is
                // x=m*y+k [c=m*r+k and q=m*p+k, => c+h=m*(r-v)+k
                // => k=c-m*r => c+h=m*(r-v)+c-m*r => h=-mv => m=-h/v].       <<=== V
                //   To outline a wedge of light where the beam goes north or
                // east, we run this calc on the two edge rays, and any point
                // x<m*y+k (N, or E: y<m*x+k) for the left edge is in the     <<=== ?
                // shadow; and similarly > for the right edge. The inequalities
                // are reversed for south and west.
                if (more < 180) { // (+180 means no radial shadows)
                    here = aim + more; // aim>0, more >= 0
                    rpt = here;
                    while (here >= 315) here = here - 360;
                    MyMath.Angle2cart(MyMath.Fix2flt(here, 0));
                    Hstp = MyMath.Sine; // = h
                    Vstp = MyMath.Cose; // = v
                    Vcs = Vstp;
                    Hsn = Hstp; // (for log)
                    if (here < 45) { // right edge is mostly V..
                        Rmpy = Hstp / Vstp; // = -m in the equation
                        Rkon = Hat + Rmpy * Vat;
                    } //~if // = k
                    else if (here < 135) { // right edge is mostly H..
                        Rmpy = Vstp / Hstp; // = -m in the equation
                        Rkon = Vat + Rmpy * Hat;
                        whom++;
                    } //~if // whom = 1;
                    else if (here < 225) { // mostly V south..
                        Rmpy = Hstp / Vstp; // = -m in the equation
                        Rkon = Hat + Rmpy * Vat; // = k
                        whom = 2;
                    } //~if
                    else { // if (here<315) { // mostly H west..
                        Rmpy = Vstp / Hstp; // = -m in the equation
                        Rkon = Vat + Rmpy * Hat;
                        whom = 3;
                    } //~else
                    here = aim - more;
                    while (here < 0) here = here + 360;
                    rpt = (rpt << 16) + here; // save for map display if headlights
                    more = more - 90; // <0: AND, >0: OR, =0: 1-test
                    if (more != 0) {
                        while (here >= 315) here = here - 360;
                        MyMath.Angle2cart(MyMath.Fix2flt(here, 0));
                        Hstp = MyMath.Sine; // = h
                        Vstp = MyMath.Cose; // = v
                        if (here < 45) { // right edge is mostly V..
                            Lmpy = Hstp / Vstp; // = -m in the equation
                            Lkon = Hat + Lmpy * Vat;
                        } //~if // = k
                        else if (here < 135) { // right edge is mostly H..
                            Lmpy = Vstp / Hstp; // = -m in the equation
                            Lkon = Vat + Lmpy * Hat;
                            whom = whom + 4;
                        } //~if
                        else if (here < 225) { // mostly V south..
                            Lmpy = Hstp / Vstp; // = -m in the equation
                            Lkon = Hat + Lmpy * Vat; // = k
                            whom = whom + 8;
                        } //~if
                        else { // if (here<315) { // mostly H west..
                            Lmpy = Vstp / Hstp; // = -m in the equation
                            Lkon = Vat + Lmpy * Hat;
                            whom = whom + 12;
                        }
                    }
                } //~if
                if (NoisyMap) if (MapLogged) { // if (Qlog..) -- included in M_L
                    xStr = xStr + HandyOps.Dec2Log("\n    [", whom, ":");
                    if (more < 180) {
                        if (ShoHedLit) { // ShoHedLit=true
                            if (deep <= 0.0)
                                xStr = xStr + HandyOps.Flt2Log(" >", deep, "");
                            else if (nxt >= 4) // too high to be a headlite
                                xStr = xStr + HandyOps.Dec2Log(" +", nxt, "");
                            else if (more > -45)
                                xStr = xStr + HandyOps.Dec2Log(" ^", more, "");
                            else if (wix < 20.0) xStr = xStr + HandyOps.Flt2Log(" <", wix,
                                    HandyOps.Flt2Log("/", radius, ""));
                            else HeadLiSho(rpt, deep, Vat, Hat);
                        } //~if // always logs
                        if (more != 0) xStr = xStr + HandyOps.Flt2Log(" ", Hstp,
                                HandyOps.Flt2Log("/", Vstp, HandyOps.Flt2Log(" Lm=", Lmpy,
                                        HandyOps.Flt2Log(" k=", Lkon, ","))));
                        aStr = xStr + HandyOps.Flt2Log(" s/c: ", Hsn,
                                HandyOps.Flt2Log("/", Vcs, HandyOps.Flt2Log(" Rm=", Rmpy,
                                        HandyOps.Flt2Log(" k=", Rkon, "]"))));
                    } //~if
                    else aStr = xStr + "]";
                    if (ShoHedLit) {
                        zx = whom & 5;
                        yx = whom & 10;
                        if (zx == 5) aStr = aStr + " (H) m*Hat+Vat-k";
                        else if (zx == 0) aStr = aStr + " (V) m*Vat+Hat-k";
                        else yx--;
                        if (yx == 10) aStr = aStr + " < 0";
                        else if (yx == 0) aStr = aStr + " > 0";
                    }
                }
            } //~if
            if (NoisyMap || MapLogged) System.out.println(HandyOps.Flt2Log("  _$_ ", Vat,
                    HandyOps.Flt2Log("/", Hat, HandyOps.Dec2Log(" +", nxt,
                            HandyOps.Dec2Log(" = ", colo, HandyOps.Flt2Log("/", brite,
                                    HandyOps.TF2Log(" ", preSc, HandyOps.RepNthLine(aStr, 1, aWord))))
                                    + "'"))));
            hix = MyMath.Fix2flt(nxt * nxt * 10, 10); // min 0.01 (1m), max 160K (4Km)
            // How it works: a colo=4K lamp (=1<<LumUniShif) elevated nxt=20m
            //  adds a nominal 4K (=unity) to the luminance map directly below,
            //  which means that its nominal actual brightness is 20^2 (=400)x.
            // The distance variables sum hix+wix is distance squared scaled 4%
            //  so nxt=20m becomes hix = 20*20*10/1K = 4.0; brite is (implicitly)
            //  scaled 0.25% then *65K so brite/(hix+wix)=4K.
            for (rpt = MapTall - 1; rpt >= 0; rpt += -1)
                for (kx = MapWide - 1; kx >= 0; kx += -1) { // in pk meters
                    thar--;                   // calc whole lum map: theLum[thar]..
                    if (rpt >= tall) continue;
                    if (kx >= wide) continue;
                    soft = false;
                    xcell = (rpt << 16) + kx;
                    if (NoisyMap) if (MapLogged) for (here = ShoLumiLox[0]; here >= 1; here += -1) // NoisyMap=F
                        if (ShoLumiLox[here & 31] == xcell) {
                            xStr = HandyOps.Dec2Log("  ... ", rpt,
                                    HandyOps.Dec2Log("/", kx, ""));
                            xcell = -1;
                            // aStr = "";
                            break;
                        } //~if
                    shade = false;
                    Vstp = 0.0;
                    Hstp = 0.0;
                    if (aim > 0) while (true) { // test if this point in shadow..
                        Vat = MyMath.Fix2flt(rpt, 0); // =y  // initially,
                        Hat = MyMath.Fix2flt(kx, 0);  // =x  // >0 means (V)east/(H)south..
                        if ((whom & 1) != 0) // right edge, test H (y<m*x+k)..
                            whar = Rmpy * Hat + Vat - Rkon; // ..(=> y-m*x-k<0, but Rmpy=-m)
                        else whar = Rmpy * Vat + Hat - Rkon; // test V (x<m*y+k)
                        Hstp = whar; // for log
                        if ((whom & 2) == 0) { // upper or right 45-quadrant, <0 is OK
                            if (NoisyMap) aStr = "^";
                            whar = -whar;
                        } //~if // now >0 means (V)west/(H)north (OK)
                        else // lower or left 45-quadrant, >0: (V)east/(H)south is OK
                            if (NoisyMap) aStr = "_";
                        whar = whar + 0.75;
                        if (whar < 0.0) shade = true;
                        else if (whar < 0.5) soft = true;
                        if (NoisyMap) if (xcell < 0) xStr = xStr + HandyOps.Flt2Log(" (", Hstp,
                                HandyOps.Flt2Log(HandyOps.IffyStr(shade, ") Rsh ", ") Ron "),
                                        whar, aStr));
                        if (shade) {
                            if (more <= 0) break;
                        } //~if
                        else if (more >= 0) break;
                        if ((whom & 4) != 0) // left edge, test H, >0 means (V)east/(H)south..
                            whar = Lmpy * Hat + Vat - Lkon;
                        else whar = Lmpy * Vat + Hat - Lkon; // test V
                        Vstp = whar; // for log
                        if ((whom & 8) != 0) { // lower or left 45-quadrant, <0 OK
                            if (NoisyMap) aStr = "^";
                            whar = -whar;
                        } //~if // now >0 means (V)west/(H)north (OK)
                        else // upper or right 45-quadrant, >0: (V)east/(H)south is OK
                            if (NoisyMap) aStr = "_";
                        whar = whar + 0.75;
                        if (whar > 0.0) {
                            shade = false;
                            if (whar < 0.5) soft = true;
                        } //~if
                        else shade = true;
                        if (NoisyMap) if (xcell < 0) xStr = xStr + HandyOps.Flt2Log(" (", Vstp,
                                HandyOps.Flt2Log(HandyOps.IffyStr(shade, ") Lsh ", ") Lit "),
                                        whar, aStr));
                        break;
                    } //~while // (this point in shadow)
                    while (true) { // set theLum for this loc..
                        if (shade) break;
                        yx = (kx - cx);
                        zx = (rpt - rx);
                        wix = MyMath.Fix2flt((zx * zx + yx * yx) * 10, 10); // = nominal/400
                        if (NoisyMap) aStr = HandyOps.IffyStr(soft, ")z", ")");
                        if (wix > far) { // far<radius
                            if (NoisyMap) aStr = ")s";
                            soft = true;
                        } //~if
                        else if (wix < dark) if (wix >= near) { // near <= dark
                            if (NoisyMap) aStr = ")S";
                            soft = true;
                        } //~if
                        if (NoisyMap) if (xcell < 0) xStr = xStr + HandyOps.Flt2Log(" => ", wix,
                                HandyOps.Dec2Log(" (", zx, HandyOps.Dec2Log("/", yx, aStr)));
                        if (wix > radius) break;
                        if (wix < near) break;
                        wix = hix + wix;
                        if (preSc) whar = brite / wix; // (prescaled)
                        else whar = (brite / wix) * scale;
                        if (soft) whar = whar * 0.5; // soften edges
                        colo = (int) Math.round(whar);
                        if (NoisyMap) if (xcell < 0) xStr = xStr + HandyOps.Dec2Log(" + ", colo,
                                HandyOps.Flt2Log(" (", whar, ")"));
                        if (thar < 0) break;
                        if (thar >= theLum.length) break;
                        info = theLum[thar] + colo;
                        theLum[thar] = info;
                        if (NoisyMap) if (xcell < 0) xStr = xStr
                                + HandyOps.Dec2Log(" @ ", thar, HandyOps.Dec2Log(": ", info, ""));
                        break;
                    } //~while // (set theLum)         // NoisyMap=F..
                    if (NoisyMap || MapLogged) if (xcell < 0) // if (Qlog..) -- in ML
                        System.out.println(xStr);
                } //~for // (rpt,kx)(calc whole lum map) // "  ... "
            LumiLogg();
            rpt = -1; // no error
            break;
        } //~while // (once thru)
        TempStr = theList;
        return rpt;
    } //~Luminator

    private static int BackImage(char xCh, int rx, int cx,
                                 int engle, int ImgHi, int ImgWi, int whar, int lino, String aLine,
                                 String theList, int[] myMap) {                // prep for DoWall..
        boolean curvd = (xCh < 'G'), whom = (xCh == 'C') || (xCh == 'c');
        int dx = (curvd ? 1 : 0)
                + ((xCh == 'g') ? 5 : 0);
        String aStr = HandyOps.NthItemOf(false, dx + 5, aLine),
                xStr = HandyOps.NthItemOf(false, 4, aLine); // used errn: -120..30,39..44
        char zCh = '_', aCh = HandyOps.CharAt(1, aLine);
        int sofar = nBax >> 16, errn = -120, pie = HandyOps.SafeParseInt(aStr),
                kx = HandyOps.NthOffset(0, "/", xStr), frnt = (rx << 16) + cx, more = -1,
                doing = nBax, zx = 0, px = 0, nx = 0, stub = 0, info = 0, whoz = 0,
                why = 0, here = 0, thar = 0, tall = 0, wide = 0, dimz = 0, ppm = 0,
                offx = 0, aim = 0, skip = 0, sang = 0, rocox = 0, haff = 0, rpt = 0,
                Pmap = 0, tmp = 0, radius = 0, prio = 0, base = 0, Vx = 0, Hx = 0,
                logy = 0, ix0 = 0, ix1 = 0, ix2 = 0, ix3 = 0, nuly = 0, nxt = 0;
        boolean doit = false, centrd = false, didit = false; // fust = false;
        String aWord = "";
        double ftmp = 0.0, Vinc = 0.0, Hinc = 0.0, lxx = 0.0,
                Vstp = 0.0, Hstp = 0.0, Vat = 0.0, Hat = 0.0, mpyr = 1.0;
        if (MapLogged) {
            logy++; // logy = 1
            if (Qlog > 0x10000) whoz = Qlog >> 16;
        } //~if

        // Render only wall face, defined from the perspective of sighting down
        //   the wall from the start cell to the end, the straight line from the
        //   right back corner to the (end cell) far right corner [; or else back
        //   face (from view>180) which is to the left]. Corners are denoted by
        //   low bits of V,H/Q,Z: 0=N/W, 1=S/E. Wall rendering base (rx) is
        //   proportional to the (cx) distance from left front corner.
        // We now run through all the BGs at the start, and build their specs
        //   into SeenWall, which is rendered at the end (so internal plain walls
        //   can cover image walls)
        // BackImag must ensure that walls butt up correctly, corner to corner,
        //   if their ends are spec'd to same grid locn. Left turns do this with
        //   an inserted singleton with a diagonal face. Right turns ditto, but
        //   the "face" is the 0-length front corner (used if a view ray misses
        //   both segments and passes between them).
        // Singleton wall end caps must have angle & view pies. Singletons used
        //   in [left] turns, angle is mid between left & right wall segments,
        //   view pie = 180. No singletons needed in turns (= straight-on joins)
        //   that do not change general compass direction.

        // As seen by the car, the face line advances x units to the right and
        //   +/-y units fwd or back; for position z in the face, rx is z*y/x units
        //   +/-; disregard y in calc'ing z; corners denoted by low bits of V,H/Q,Z.
        // Record only compass dir'n, face starts @ left front corner of anchor,
        //   ends @ start of face in ending anchor (defines y), unless dir'n >45;
        // L: turn left needs a singleton view in front of successor view list,
        //   facing in the came-from dir'n so corner connection works;
        // R: turn right gotta start successor one cell late, w/singleton inside.

        // Preliminary data in SeenWall, four ints indexed by nBax+256i:
        //    +0: r,c (in meters) of anchor, +HiBit if corner insert,
        //      +40 if segment head, +20 if linked, +10 if curve,
        //      +08 gotta recalc angle, +04 if corner insert turns left;
        //  +256: angle from anchor in lo 12, link to next in segment
        //      (temp'ly -lino of spec line) in hi 16;
        //  +512: r,c (in meters) of far end (rocox) +HiBit if spec'd singleton;
        //  +768: temp'ly: -lino of spec line, then (if not head) link to head,
        //      else (link to left segment head, link to right segment head).
        //      Left end caps log'ly follow segment head, right caps are their
        //      own segment. Corner insert immed'ly precedes segment head.

        // Collected data for image walls..
        //  4-word MapIndex item[BG+], built in BackImag, used everywhere..
        //    +0: grid V,H of left end / Q,Z of right end (Q:8/V:8/Z:8/H:8) in m;
        //    +1: (-0:iter*) view pie width:7 / view aim:10/:4 / compass orient:10;
        //    +2: ppm:8 / file offset:24;
        //    +3: (-0:proportion+) pixel H,W of this image

        // Image wall curve table ImWalCurve: index, then 1 int ea seg, G or M:
        //   G: angle,t/w,+v/+h; M-: angle,(t= .LR)/w=angle,+v/+h  (8/4bit each)

        // Manual curves Mi, i: 0=E,1=S,2=W,3=N (side);
        // .. 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag, 9=135 dn, 10/11 west'ly
        // Policy: stop trying to support curves until the straights are working.
        // Policy: try to do as much in the prescan as it makes sense to do. Let
        //   the main pass build the index+cells and not much more.

        while (true) { // uses NuData,Wally,nBax to preserve state across calls
            InWalls = false;
            if (SeenWall == null) break; // errn = -120;
            // xStr = HandyOps.NthItemOf(false,4,aLine);
            if ((xCh == 'v') || (xCh == 'h') || (xCh == 'p')) { // prescan plain wall..
                if (nBax == 0) nBax++;
                nx = nBax + 2;
                if (nx < 256) { // capture this line for log..
                    while (HandyOps.Countem("\n", Save4Log) < nx + 5)
                        Save4Log = Save4Log + "\n\n\n\n";
                    Save4Log = HandyOps.RepNthLine(aLine, nx - 1, Save4Log);
                    Save4Log = HandyOps.RepNthLine("/" + aLine, nx, Save4Log);
                    if (xCh == 'p') {
                        Save4Log = HandyOps.RepNthLine("-" + aLine, nx + 1, Save4Log);
                        nx = nx + 2;
                        Save4Log = HandyOps.RepNthLine("+" + aLine, nx, Save4Log);
                    }
                } //~if
                why = 0;
                nBax = nx;
                doing = nBax;
                curvd = false;
                errn = -1; // no error
                break;
            } //~if // (prescan 'v'/'h'/'p')

            if (xCh != 'V') if (xCh != 'H') if (xCh != 'P') { // not plain walls..
                if (xCh >= 'c') { // if (xCh <= 'm') { // prescan, extract r/c..
                    rx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, aLine));
                    if (rx <= 0) break; // errn = -120
                    if (rx >= HalfTall) break;
                    cx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 3, aLine));
                    if (cx < 0) break;
                    if (cx >= HalfMap) break; // (already did it for 'G')..
                    frnt = (rx << 16) + cx;
                } //~if // (prescan, extract r/c)
                if (kx > 0) { // got ending r/c spec.. // kx=NthOffset(0,"/",xStr)
                    engle = -1; // (mark it invalid)
                    Hx = HandyOps.SafeParseInt(HandyOps.RestOf(kx + 1, xStr));
                    Vx = HandyOps.SafeParseInt(xStr);
                    rocox = (Vx << 16) + Hx;
                    why = 4;
                    if (frnt != rocox) { // find angle (can't from singleton)
                        Vinc = MyMath.Fix2flt(rx - Vx, 0); // (neg)
                        Hinc = MyMath.Fix2flt(Hx - cx, 0);    // aTan0(H=0,V=-1)=0..
                        engle = (int) Math.round(MyMath.aTan0(Hinc, Vinc)); // cw frm N
                        if (engle <= 0) engle = engle + 360;
                    }
                } //~if // (find angle) (got ending)
                else if (xCh >= 'c') { // if (xCh <= 'm') {
                    engle = HandyOps.SafeParseInt(xStr); // dir'n
                    while (engle <= 0) engle = engle + 360;
                } //~if
                else if (engle == 0) engle = engle + 360;
                if (engle > 0) { // else don't have a valid angle
                    while (engle > 360) engle = engle - 360;
                    MyMath.Angle2cart(MyMath.Fix2flt(engle, 0)); // C-wise from north
                    Vstp = -MyMath.Cose;
                    Hstp = MyMath.Sine;
                    if (rocox == 0) if (xCh == 'g') if (aCh == ' ') { // calc end frm len..
                        // pie = HandyOps.SafeParseInt(HandyOps.NthItemOf(false,10,aLine));
                        if (pie <= 0) { // no lxx given, try for wall width..
                            nx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                            pie = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 9, aLine));
                            if (pie > 0) pie = nx / pie;
                        } //~if // im.wi/ppm
                        if (pie > 1) {
                            lxx = MyMath.Fix2flt(pie >> 1, 0); // spec'd as pk meters, need g.u.
                            tall = (int) Math.round(Vstp * lxx);
                            wide = (int) Math.round(Hstp * lxx);
                            Vx = tall + rx;
                            Hx = wide + cx;
                            if ((tall | wide) != 0) if (((Vx | Hx) & -256) == 0) {
                                rocox = (Vx << 16) + Hx;
                                why = why + 2;
                            }
                        }
                    } //~if // (calc end)
                    zx = engle + 45;
                    if (zx == 0) aim = 3; // round odd => prefer E-W wall..
                    else if (zx == 180) aim = 1;
                    else aim = (zx / 90) & 3; // = general compass heading
                    if (xCh == 'm') { // manual corner has 8 different aims, no pref..
                        // dx = 0; // (init'ly)
                        if (HandyOps.NthOffset(0, ".", xStr) > 0) kx = 0;
                        else kx = 8;     // =0 prefers near corner
                        nx = zx + engle - 22; // zx = engle+45, so nx = engle*2+45
                        aim = (nx / 90) & 7; // now 45-deg octants centered on compass points
                        if ((aim & 1) != 0) { // got a diagonal..
                            nx = aim >> 1;
                            if (kx == 0) kx = (nx + 5) & 7; // 4 corners, 4=NE (315deg), etc
                            else kx = nx + 8;
                        } //~if // 8=45 up diag, 9=135 dn, 10/11 west'ly
                        else kx = aim >> 1; // these are the four compass points
                        zx = 0;
                        if (aCh >= '0') if (aCh <= '9') { // aim: 0=E,1=S,2=W,3=N (side);
                            dx++; // .. 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag, 9=135 dn
                            zx = ((int) (aCh)) & 15;
                            if (zx == 1) {
                                aCh = HandyOps.CharAt(2, aLine);
                                if (aCh == '0') kx = 10;
                                else if (aCh == '1') kx = 11;
                                else kx = zx;
                            } //~if
                            else kx = zx;
                        }
                    }
                } //~if
                else if (xCh == 'm') { // corners must have valid angle..
                    errn = -130;
                    break;
                } //~if
                if (rocox == 0) if (xCh >= 'c') { // if (xCh <= 'g') // assume singleton..
                    rocox = frnt; // ..(usually) know by: engle<0
                    why = why + 1;
                }
            } //~if // why = 1..7, depending on source of rocox
            errn--; // errn = -121                 //..~if (not plain walls)
            if (xCh == 'm') { // prescan (all params known/assumed valid)..
                if (nBax == 0) nBax++;
                nx = nBax + 1; // aStr = HandyOps.NthItemOf(false,5,aLine)
                frnt = (frnt << 1) & 0xFE00FE; // 'Mi vv hh dd 0 tt ww xx^yy pp 0*'
                frnt = (frnt << 8) + frnt;     // ** vfy still works for 0/90/..
                kx = kx & 15; // kx (frm engle)/aim (frm Mi): 0=E,1=S,2=W,3=N (side);
                // sang = 0; // .. 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag, 9=135 dn
                if (engle > 0) if (HandyOps.NthOffset(0, "+", aStr) > 0) tmp = engle;
                // if engle & view are given, always use given angle, else recalc
                offx = ((0x06660333 >> kx) & 0x10001) << 8; // end   // .. kx=10/11: westerly
                offx = (0x09630C39 >> kx) & 0x10001 | offx | frnt; // +start
                if (aCh == 'L') sang = 8; // aCh = HandyOps.CharAt(1,aLine);
                else if (aCh != 'R') sang = (kx + 8) & 8; // (kx set above frm engle)
                sang = (sang << 23) + 0x80000000; // +04 if corner turns left
                if (tmp == 0) tmp = (aim & 7) * 45; // recalc angle from face, else use given
                tmp = ((aim & 6) << 9) + tmp; // effectively: aim&3<<10
                skip = -lino; // replace this with left link
                if (nx >= 0) if (nx < 256) { // insert face/corner singleton.. // **
                    SeenWall[nx] = offx & 0xFF00FF | sang; // (not segment head)
                    SeenWall[nx + 256] = tmp; // pie width:7 / view aim:10/:4 / compass:10
                    SeenWall[nx + 512] = ((offx >> 8) & 0xFF00FF) | sang;
                    SeenWall[nx + 768] = (skip << 16) | skip & 0xFFFF; // dunno links, use line+
                    // zx = kx; // xStr = HandyOps.NthItemOf(false,1,aLine);
                    // if (zx>7) zx = zx&9; // now reconstructed 'i' from "Mi"
                    xStr = "G" + kx + aLine; // not gen'd from curve; aCh => kx
                    theList = HandyOps.RepNthLine(xStr, lino, theList);
                    while (HandyOps.Countem("\n", Save4Log) < nx)
                        Save4Log = Save4Log + "\n\n\n\n";
                    if (nx > 0) Save4Log = HandyOps.RepNthLine(xStr, nx, Save4Log);
                    xStr = "";
                    curvd = true;
                    nBax = nx;
                } //~if // (insert corner singleton)
                zx = 0;
                why = 0;
                doing = nBax;
                errn = -1; // no error
                break;
            } //~if // (prescan 'm')

            if (xCh == 'g') { // prescan..
                // aim = ((engle+45)/90)&3; doit = false;
                pie = (NuData >> 24) & 3;
                if (nBax == 0) nBax++;
                nx = nBax + 1;   // allow for inserted singleton in turns >45..
                kx = (aim << 24) + (nx << 8) + frnt; // frnt=r,c in NuDat not used
                haff = Darken;
                xStr = "";        // aCh = HandyOps.CharAt(1,aLine);
                if (aCh >= '0') { // we have an explicit aim..
                    if (HandyOps.CharAt(2, aLine) == 'M') {
                        aCh = 'm';
                        zx = 3;
                    } //~if
                    else if (aCh > 'A') zx = 2; // this one was gen'd by curve
                    else zx = 1;
                    // aim: eeeaaiiii: eee=3-bit engle/45, aa=new aim, iiii=ix0 spec
                    aim = HandyOps.SafeParseInt(HandyOps.Substring(zx, 3, aLine));
                    if (Mini_Log) if ((Qlog & 256) != 0) if (nx == whoz)
                        aLine = aLine + HandyOps.Dec2Log(" [[", zx,
                                HandyOps.Dec2Log(" ", engle, HandyOps.Dec2Log(" ", aim, "]]")));
                    if (engle < 0) {
                        if (aCh == 'm') { // (engle<0 shouldn't be possible)
                            if (aim > 15) engle = (aim >> 6) * 45;
                            else if (aim >= 8) engle = (aim & 3) * 90 + 45;
                            else if (aim < 4) engle = aim * 90;
                            else info = aim * 90 - 45;
                        } //~if // if (aim<8) if (aim >= 4)
                        else if (aim < 5) engle = aim * 90;
                    } //~if
                    if (engle == 0) engle = 360;
                } //~if
                if (aCh == 'M') aCh = 'm';
                // if (aCh == 'm') { // if (TestOptn&0x8000 !=0) System.out.println(...);
                if (engle < 0) if (rocox != frnt) // except singletons(?),
                    break; // errn = -121 // ..error if no angle given nor inferred
                if (NuData > 0) { // aCh = HandyOps.CharAt(1,aLine);
                    nuly = ((aim - pie) & 3) + 1; // =2 if turning right, =4 if left
                    if (((aim + pie) & 1) != 0) // new compass dirn..
                        if (aCh == ' ') doit = true; // else curve has its own corner(s)
                    if (engle >= 0) {
                        Darken = engle;
                        NuData = kx;
                    } //~if
                    else NuData = NuData & -0x01000000 | kx & 0xFFFFFF;
                } //~if
                else if (engle >= 0) {
                    Darken = engle;
                    NuData = kx;
                } //~if
                zx = Wally;
                Wally = rocox;                            // doit = false; ..
                if (aCh == 'm') Wally = Wally | 0x80000000; // no corner needed B4 next
                if (zx <= 0) doit = false;         // if too far from prev..
                else if (doit) if ((MyMath.iAbs((frnt >> 16) - (zx >> 16))
                        | MyMath.iAbs((frnt & 255) - (zx & 255))) > 3) doit = false; // not a bend
                rocox = rocox << 1;
                frnt = frnt << 1;
                zx = zx << 1; // = prior rocox
                if (rocox == frnt) {
                    rocox = rocox | 0x80000000;
                    why = why + 8;
                } //~if
                skip = -lino << 16; // replace this with left link        // (not 'M')..
                if (doit) if (nx > 0) if (nx < 255) { // doit=T: turning (new compass dirn)
                    kx = (nuly & 4) + aim; // <4 if turning right, > left; aim: 0=N,1=E,2=S,3=W
                    // ix0 = (0x63333999>>kx)&0x01010101;
                    switch (kx) { // calculate inserted corner's wall face..
                        case 0: // bends west to north..
                            offx = zx & -0x10000 | frnt & 255; // west's north + north's east face
                            sang = 0x00000101; // NE corner is full 1-pix face
                            break;
                        case 1: // bends north to east..
                            offx = frnt & -0x10000 | zx & 255;
                            sang = 0x01010101; // SE corner is full 1-pix face
                            break;
                        case 2: // bends east to south..
                            offx = zx & -0x10000 | frnt & 255;
                            sang = 0x01010000; // SW corner is full 1-pix face
                            break;
                        case 3: // bends south to west..
                            offx = frnt & -0x10000 | zx & 255;
                            sang = 0x00000000; // NW corner is full 1-pix face
                            break;
                        case 4: // bends east to north..
                            offx = zx & -0x10000 | frnt & 255; // east's south + north's east face
                            sang = 0x00010100; // face from SW corner to NE corner
                            break;
                        case 5: // bends south to east..
                            offx = frnt & -0x10000 | zx & 255;
                            sang = 0x01000100; // face from NW corner to SE corner
                            break;
                        case 6: // bends west to south..
                            offx = zx & -0x10000 | frnt & 255;
                            sang = 0x01000001; // face from NE corner to SW corner
                            break;
                        case 7: // bends north to west..
                            offx = frnt & -0x10000 | zx & 255;
                            sang = 0x00010001; // face from SE corner to NW corner
                            break;
                    } //~switch
                    offx = ((kx & 4) << 24) + offx + 0x80000000;
                    tmp = ((engle + haff) & 0xFFF) >> 1;
                    if (tmp > 360) tmp = tmp - 360;
                    zx = tmp + 45;
                    if (zx == 0) dx = 3; // round odd => prefer E-W wall..
                    else if (zx == 180) dx = 1;
                    else dx = (zx / 90) & 3; // = general compass heading
                    tmp = (dx << 10) + tmp; // view pie width:7 / view aim:10/:4 / compass:10
                    if (nx >= 0) if (nx < 256) { // insert corner singleton..
                        SeenWall[nx] = offx | sang & 0x10001; // (not segment head)
                        SeenWall[nx + 256] = tmp;
                        SeenWall[nx + 512] = offx | (sang >> 8) & 0x10001;
                        SeenWall[nx + 768] = skip + nx + 1; // dunno left link, use line+
                        skip = nx << 16; // new left link for following segment
                        nx++;
                    }
                } //~if // (doit=T: turning)
                zx = aim; // aim = HandyOps.SafeParseInt(HandyOps.Substring(zx,3,aLine))
                if (aim > 3) { // aim: 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag, 9=135 d
                    // aim: eeeeaaiiii: eee=3-bit engle/45, aa=new aim, iiii=ix0 spec
                    if ((aim & 8) != 0) ix0 = (0x0609030C >> (aim & 3)) & 0x01010101;
                    else ix0 = (0x66633339 >> (aim & 7)) & 0x01010101;
                    if (aim > 63) engle = (aim >> 6) * 45;
                    if (aim > 15) aim = (aim >> 4) & 3;
                } //~if           // right side of..
                else ix0 = (0x66633339 >> (aim & 7)) & 0x01010101; // ..0=N,1=E,2=S,3=W
                here = (rocox << 8) | frnt | ix0;      // recalc angle from face..
                if (engle <= 0) engle = engle + 360; // (not yet stable)
                if ((engle + 45) % 90 == 0) { // exact diagonal, nudge it off..
                    if (aCh == 'd') engle++;
                    else if (aCh == 'c') engle--;
                    else if (aCh == 'C') engle++;
                    else if (aCh == 'D') engle--;
                } //~if
                dx = ((aim & 3) << 10) + engle; // pie width:7 / view aim:10/:4 / compass:10
                if (aCh != ' ') dx = dx | 0x1000; // mark it as curve (no post-edits)
                if (nx > 0) if (nx < 256) {
                    SeenWall[nx] = here & 0xFF00FF; // save for later matchup..
                    SeenWall[nx + 256] = dx;
                    SeenWall[nx + 512] = (here >> 8) & 0xFF00FF;
                    SeenWall[nx + 768] = (-lino) & 0xFFFF | skip;
                } //~if // line+ for rt link
                while (HandyOps.Countem("\n", Save4Log) < nx) // if (Qlog..) -- in M_L
                    Save4Log = Save4Log + "\n\n\n\n";
                Save4Log = HandyOps.RepNthLine(aLine, nx, Save4Log);
                if (Mini_Log) if ((Qlog & 256) != 0) {
                    aWord = Save4Log;
                    if (nx > 4) if (nx != whoz) if ((nx & 15) != 0) aWord = "\n  " + aLine;
                    System.out.println(HandyOps.Dec2Log("++++", nx,
                            HandyOps.Dec2Log("/", lino, HandyOps.Int2Log(" ", here,
                                    HandyOps.Int2Log(" (", rocox,
                                            HandyOps.Hex2Log(" |x", ix0, 8,
                                                    HandyOps.Hex2Log(") x", dx, 4,
                                                            HandyOps.Dec2Log(" =", aim, HandyOps.Dec2Log("+", engle,
                                                                    HandyOps.Dec2Log(" (", zx, HandyOps.TF2Log(") ", doit,
                                                                            HandyOps.Int2Log("/", kx, HandyOps.Int2Log(" ", offx,
                                                                                    HandyOps.Int2Log(" +", sang, HandyOps.Int2Log(" ", Wally,  // why =
                                                                                            HandyOps.Dec2Log(" ", (int) aCh,
                                                                                                    HandyOps.Dec2Log(" = ", why, " ++" + aWord)))))))))))))))));
                    aWord = "";
                } //~if
                // xStr = ""; // xStr+
                zx = 0;
                why = 0;
                nBax = nx;
                doing = nBax;
                curvd = false;
                errn = -1; // no error
                break;
            } //~if // (prescan 'g')

            // Image wall curve table ImWalCurve: index, then 1 int ea seg, G or M:
            //   G: angle,t/w,+v/+h; M-: angle,(t= .LR)/w=angle,+v/+h  (8/4bit each)
            why = 0;
            errn--; // errn = -122
            if (xCh >= 'c') { // if (xCh <= 'd') { // prescan & fill in curve.. // **
                Wally = 0;
                nxt = 0;
                // pie = HandyOps.SafeParseInt(HandyOps.NthItemOf(false,5,aLine));
                // engle = SafeParseInt(NthItemOf(false,4,aLine)); // initial dir'n
                // aim = ((engle+45)/90)&3; doit = false;
                if (engle < 0) break; // errn = -122 // error if no angle given
                aStr = HandyOps.NthItemOf(false, 5, aLine);
                skip = MyMath.iMax(HandyOps.NthOffset(0, "-", xStr), 0);
                sang = HandyOps.SafeParseInt(aStr); // end angle
                if (sang < 6) sang = 90;
                else if (sang > 90) sang = 90; // start angle..
                if (skip > 0) skip = MyMath.iMax(HandyOps.SafeParseInt(
                        HandyOps.RestOf(skip + 1, aStr)), 0);
                if (skip + 5 > sang) {
                    sang = 90;
                    skip = 0;
                } //~if // ** shortened curve ends not yet used **
                kx = 0;
                dx = 0;                // 'C/D vv hh dd rr ee-ss tt ww xx^yy pp'
                info = 0;           // => 'GC/D vv hh qq/zz 0 tt ww xx^yy pp 0*'
                offx = 0;           // .. 'ML/R vv hh  dd   0 tt ww xx^yy pp 0*'
                nuly = 0;
                errn = -140;
                pie = pie << 1; // pie is spec'd radius, now 2x radius
                if (whom) { // whom = (xCh == 'c')
                    aim = aim + 4;
                    pie++;
                } //~if
                if (pie <= 33) if (pie >= 0) { // if (NuData >= 0) // unneed prior dir'n
                    kx = ImWalCurves[pie];
                    dx = ImWalCurves[pie + 1] - kx;
                    // nxt = (dx<<16)+kx; // for log (not logged, but dx and kx are)
                    if (kx > ImWalCurves[34]) break; // errn = -140; bad table..
                    if (dx > 15) break;
                } //~if
                if (kx < 34) break;
                if (dx <= 0) break; // convert C/D -> G..            // (in BackImag)
                nx = HandyOps.NthOffset(0, "--", aLine);
                if (nx < 0) nx = HandyOps.NthOffset(0, "//", aLine);
                if (nx > 20) {
                    aWord = " 0* " + HandyOps.Substring(nx, 33, aLine);
                    nx = aWord.length();
                    if (nx < 21) aWord = aWord + "; ";
                } //~if
                else if (nx >= 0) break;
                else aWord = " 0* -- curve "; // whom = (xCh == 'c') // (curve left)..
                if (nx < 16) aWord = aWord + HandyOps.IffyStr(whom, "L @ ", "R @ ");
                for (dx = 10; dx >= 7; dx += -1) // copy back half of line unchanged..
                    aWord = " " + HandyOps.NthItemOf(false, dx, aLine) + aWord;
                aWord = " 0" + aWord;
                errn = -1; // no error
                for (kx = kx; kx <= 999; kx++)
                    if (kx > 0) if (kx < ImWalCurves.length) { // no cont
                        info = ImWalCurves[kx];
                        nuly = (info >> 4) & 15;
                        offx = info & 15;
                        more = info >> 8;
                        tall = (more >> 4) & 15;
                        wide = more & 15;
                        more = (more >> 8) & 511; // nominal angle of this segment's left corner,
                        dx = 2;               // ..init'ly =0, ending 90 (both D&C)
                        if (info < 0) { // (t= .LR) tall: +8 = '.', +4 = L/R, +2 = L, +1 > 45
                            if ((tall & 1) != 0) { // at or past middle (new direction)..
                                curvd = true;
                                aCh = 'm';
                            } //~if
                            else if (curvd) aCh = 'm';
                            else aCh = 'M';
                            /// if ((tall&4)==0) wide = wide<<1; else // (table bug)(not)
                            if (wide == 3) wide = 7; // ** (table bug)
                            wide = (wide + aim + aim) & 7; // now properly oriented per entry angle
                            // more (sb) used to cut & interpolate skip(=start) & sang(=end)
                            //   if (whom) more = 360-more; // whom = (xCh == 'c')
                            //   more = more+aim*90;
                            //   if (more>360) more = more-360; // (not) per curve dir'n & entry
                            doit = true;
                            dx = 8;
                        } //~if
                        else if (info > 0) {
                            if (info < 0x10000) dx = 4;
                            else if (info < 0x01000000) dx = 6;
                            else dx = 8;
                            nxt = ((int) (xCh));
                            if ((tall & 1) == 0) if (!curvd) nxt = nxt & 0xDF;
                            aCh = (char) (nxt);
                        } //~if // 'C'/'D'/'c'/'d'
                        else aCh = ' ';
                        if (MapLogged) if (Mini_Log) xStr =
                                HandyOps.Dec2Log("<**> ", kx, HandyOps.Hex2Log(": x", info, dx,
                                        HandyOps.Dec2Log(" ", nuly, HandyOps.Dec2Log("/", offx,
                                                HandyOps.Dec2Log(" ", tall, HandyOps.Dec2Log("/", wide,
                                                        HandyOps.Dec2Log(" (", more,
                                                                HandyOps.Dec2Log(") ", (int) aCh,
                                                                        HandyOps.Dec2Log(" *", aim, " = ")))))))));
                        if (info != 0) switch (aim) {
                            case 0: // N->R
                                rx = rx - nuly;
                                cx = cx + offx;
                                if (info < 0) break;
                                tall = rx - tall;
                                wide = cx + wide;
                                break;
                            case 4: // N->L
                                rx = rx - nuly;
                                cx = cx - offx;
                                if (info < 0) break;
                                tall = rx - tall;
                                wide = cx - wide;
                                break;
                            case 1: // E->R
                                rx = rx + offx;
                                cx = cx + nuly;
                                if (info < 0) break;
                                dx = wide;
                                wide = cx + tall;
                                tall = rx + dx;
                                break;
                            case 5: // E->L
                                rx = rx - offx;
                                cx = cx + nuly;
                                if (info < 0) break;
                                dx = wide;
                                wide = cx + tall;
                                tall = rx - dx;
                                break;
                            case 2: // S->R
                                rx = rx + nuly;
                                cx = cx - offx;
                                if (info < 0) break;
                                tall = rx + tall;
                                wide = cx - wide;
                                break;
                            case 6: // S->L
                                rx = rx + nuly;
                                cx = cx + offx;
                                if (info < 0) break;
                                tall = rx + tall;
                                wide = cx + wide;
                                break;
                            case 3: // W->R
                                rx = rx - offx;
                                cx = cx - nuly;
                                if (info < 0) break;
                                dx = wide;
                                wide = cx - tall;
                                tall = rx - dx;
                                break;
                            case 7: // W->L
                                rx = rx + offx;
                                cx = cx - nuly;
                                if (info < 0) break;
                                dx = wide;
                                wide = cx - tall;
                                tall = rx + dx;
                                break;
                        } //~switch
                        zx = aim; // aim: 0=N,1=E,2=S,3=W // aim = ((engle+45)/90)&3;
                        if (curvd) { // (on or after the 45-deg midpoint)
                            if (xCh == 'd') zx++;
                            else if (xCh == 'c') zx--;
                        } //~if
                        zx = zx & 3;
                        if (info < 0) { // aim: 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag..
                            // zx=aim: eeeeaaiiii: eee=3-bit engle/45, aa=new aim, iiii=ix0 spec
                            nxt = aim & 3; // singleton iiii based on unbent entry aim
                            aStr = "";   // "ImWall Curve Tst" seems to run all quads correctly
                            if (tall > 7) { // M.. // tall: +8 = '.', +4 = L/R, +2 = L, +1 > 45..
                                aStr = ".";
                                zx = (zx << 4) + nxt + 4;
                            } //~if
                            else if ((tall & 4) != 0) {
                                if (whom) nxt = (nxt - 1) & 3; // whom = (xCh == 'c')
                                zx = (zx << 4) + nxt + 8;
                            } //~if // = +8..11; angle = (zx&3)*90+45
                            else zx = zx * 17;
                            nxt = 0;
                            aStr = HandyOps.Dec2Log(" ", wide * 45, aStr) + aWord;
                            if (wide == 0) wide = 8; // (360)
                            zx = wide * 64 + zx;
                        } //~if // 'm' corners are always *45 (often != more)
                        else aStr =
                                HandyOps.Dec2Log(" ", tall,   // aWord is shared back end..
                                        HandyOps.Dec2Log("/", wide, aWord));
                        aStr =
                                HandyOps.Dec2Log(HandyOps.IffyStr(doit, "G" + aCh, "G"), zx,
                                        HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log(" ", cx,
                                                HandyOps.IffyStr(nx < 16, HandyOps.Dec2Log(aStr, more, ""),
                                                        aStr))));
                        if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0))
                            System.out.println(xStr + aStr);
                        if (info == 0) break; // normal (only) exit
                        aLine = aLine + "\n" + aStr;
                        doit = true;
                    } //~if //~for (kx)
                theList = HandyOps.RepNthLine(aLine, lino, theList);
                if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0))
                    System.out.println("    _._ " + HandyOps.ReplacAll("\n<*>=> ", "\n", aLine));
                aLine = "";
                zx = 0;
                nxt = 0; // (not logged)
                doing = -15; // nothing in SeenWall top show
                curvd = true; // so caller knows to update theList
                // errn = -1; // no error
                break;
            } //~if // (prescan curve)

            // Preliminary data in SeenWall, four ints indexed by nBax+256i:
            //    +0: r,c (2m grid) of anchor, +HiBit if corner insert,
            //      +40 if segment head, +20 if linked, +10 if curve,
            //      +08 gotta recalc angle, +04 if corner insert turns left;
            //  +256: angle from anchor in lo 12, link to next in segment
            //      (temp'ly -lino of spec line) in hi 16;
            //  +512: r,c (2m grid) of far end (rocox) +HiBit if spec'd singleton;
            //  +768: temp'ly: -lino of spec line, then (if not head) link to head,
            //      else (link to left segment head, link to right segment head).
            //      Left end caps log'ly follow segment head, right caps are their
            //      own segment. Corner insert immed'ly precedes segment head.

            NuData = -1;
            if (xCh == ' ') { // build/vfy preliminary data in SeenWall.....(BackImag)
                // doing = nBax;                           // (TestOptn&0xC0000 !=0)
                errn = -errn; // errn = 122 (not an error)
                // prio = 0; // previous segment in this chain
                // base = 0; // the head of this segment
                // ix0 = 0; // anchor coordinate for this segment
                for (skip = 1; skip >= 0; skip += -1)
                    for (base = 1; base <= nBax; base++) { // define segments..
                        if (base < 0) break;
                        if (base > 255) break;
                        frnt = SeenWall[base];
                        if (frnt == 0) continue;
                        if ((frnt & 0xFF00FF00) != 0) continue; // curve or already done
                        rocox = SeenWall[base + 512];
                        if ((rocox < 0) != (skip == 0)) continue; // do singleton heads 2nd pass
                        SeenWall[base] = frnt | 0x40000000;
                        prio = base;
                        for (nx = 1; nx <= nBax; nx++) { // find all walls in this segment (same anchor)..
                            if (nx == base) continue;
                            if (nx < 0) break;
                            if (nx > 255) break;
                            here = SeenWall[nx];
                            if (here != frnt) continue; // not same, or already done
                            if (prio > 0) if (prio < 256) {
                                px = SeenWall[prio + 256]; // (link is never used)
                                if ((px & -0x2000) == 0) // not already linked nor has pie info..
                                    SeenWall[prio + 256] = (nx << 16) | px | 0x2000;
                            } //~if
                            SeenWall[nx] = here | 0x20000000;
                            SeenWall[nx + 768] = SeenWall[nx + 768] & -0x10000 | base;
                            prio = nx;
                        }
                    } //~for // (nx) (base) (skip) // (define segments)
                // why = 0;
                px = 0;             // omit 'vfy preliminary' part 2?
                if (false)
                    for (base = 1; base <= nBax; base++) { // verify corners come just before seg heads..
                        if (base <= 0) break;
                        if (base >= 255) break;
                        info = SeenWall[base + 256];
                        if ((info & 0x1000) != 0) { // ignore curve segments..
                            px = 0;
                            continue;
                        } //~if
                        zx = 0;
                        ix1 = 0;
                        ix2 = 0;
                        ix3 = 0; // ix0 = 0; // (for log)
                        frnt = SeenWall[base];
                        if (frnt >= 0) { // we only look at corners.. // ** ??
                            info = SeenWall[base + 512];
                            if (info < 0) continue;
                            px = base; // but save prior far end going into corner
                            rocox = info;
                            continue;
                        } //~if
                        info = SeenWall[base + 1];
                        more = info ^ frnt; // match o'lap'd cell corners to insert..
                        if ((more & 0xFE00FE) == 0) if ((more & 0x10001) != 0) {
                            if ((frnt & 0x04000000) == 0) more = SeenWall[base + 512];
                            else more = frnt;    // ..right bend must use corner's right end
                            ix1 = more & 0xFF00FF | info & 0xFF00FF00 | 0x08000000;
                            SeenWall[base + 1] = ix1;
                            zx = -1;
                        } //~if
                        more = rocox ^ frnt; // ditto to left..
                        if (px > 0) if (px < 256) if ((more & 0xFE00FE) == 0) if ((more & 0x10001) != 0) {
                            ix3 = SeenWall[px] | 0x08000000; // gotta recalc its angle
                            ix2 = frnt & 0xFF00FF | rocox & 0xFF00FF00 | 0x08000000;
                            SeenWall[px + 512] = ix2;
                            SeenWall[px] = ix3;
                            if (zx < 0) zx = -px;
                            else zx = px;
                            px = 0;
                        } //~if
                        if ((info & 0x40000000) != 0) continue; // good
                        more = SeenWall[base + 768];
                        if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0))
                            if (why < 99) if ((why < 8) || (zx != 0)) {
                                if (more < 0) {
                                    lino = -(more >> 16);
                                    aLine = HandyOps.NthItemOf(true, lino, theList);
                                } //~if
                                else aLine = "";
                                System.out.println(HandyOps.Dec2Log("  (vfy_cor) ", zx,
                                        HandyOps.Dec2Log(" @ ", base, HandyOps.Dec2Log("/", lino,
                                                HandyOps.Int2Log(": ", frnt, HandyOps.Int2Log(" ", ix1,
                                                        HandyOps.Int2Log(" | ", ix3, HandyOps.Int2Log("/", ix2,
                                                                HandyOps.IffyStr(aLine == "", " __",
                                                                        "\n    __ " + aLine)))))))));
                                why++;
                            } //~if
                        errn = ~errn; // errn = -123
                        why = (errn << 16) + base;
                        break;
                    } //~for // (base) (verify corners)
                why = 0;
                if (errn < 0) break; // errn = -123
                errn++; // errn = 123 (not an error) // omit 'vfy preliminary' part 3?..
                if (false)
                    for (skip = 1; skip >= 0; skip += -1)
                        for (base = 1; base <= nBax; base++) { // link segments into chains..
                            if (base <= 0) break;
                            if (base > 255) break;
                            frnt = SeenWall[base];
                            if (skip == 0) {
                                if ((frnt & 0x40000000) == 0) continue;
                            } //~if // do heads in 2nd pass
                            else if (frnt >= 0) continue; // do corners as heads in 1st pass
                            more = SeenWall[base + 768];
                            if (more > 0) continue; // already linked left
                            ix1 = SeenWall[base + 256];
                            aim = (ix1 >> 10) & 3;
                            px = frnt & 0xFE00FE;
                            whar = 0; // look for best far end close to this front..
                            for (nx = 1; nx <= nBax; nx++) {
                                if (nx == base) continue;
                                here = SeenWall[nx];
                                if (here == 0) continue;
                                if (((here ^ frnt) & 0xFE00FE) == 0) continue; // same anchor
                                if ((here & 0x40000000) == 0) continue; // only segment heads
                                prio = SeenWall[nx + 768];
                                if ((prio & 0x8000) == 0) continue; // already linked to somebody
                                haff = SeenWall[nx + 256];
                                dx = (haff >> 10) & 3;     // ** ix1 never <0, but could =0..
                                if (ix1 > 0) tmp = (dx + 2 - aim) & 3; // =1 if bend right, =3 if left..
                                if (tmp == 0) continue; // .. =0 if 180: incompatible aim
                                thar = SeenWall[nx + 512];
                                tmp = thar & 0xFE00FE;
                                if (tmp == 0) continue; // no back-end coord (can't)
                                if (tmp != px) {
                                    if (whar != 0) continue; // already got candidate this close
                                    if ((MyMath.iAbs((tmp >> 16) - (px >> 16))     // too far..
                                            | MyMath.iAbs((tmp & 255) - (px & 255))) > 2) continue;
                                    if (thar < 0) { // singleton must fit close..
                                        if ((((thar & 0x10001) + thar) & 0xFE00FE)
                                                != (((frnt & 0x10001) + frnt) & 0xFE00FE)) continue;
                                        whar = nx; // only one can, take it..
                                        break;
                                    } //~if
                                    whar = nx;
                                    continue;
                                } //~if
                                whar = nx;
                                break;
                            } //~for // (nx)
                            if (whar <= 0) continue;
                            thar = SeenWall[whar + 512]; // link it up..
                            prio = SeenWall[whar + 768];
                            more = (whar << 16) | more & 0xFFFF;
                            SeenWall[base + 768] = more;
                            ix3 = prio & -0x10000 | base;
                            SeenWall[whar + 768] = ix3;
                            px = SeenWall[whar];
                            info = SeenWall[whar + 256];
                            zx = 4;
                            ix2 = 0; // ix0 = 0;
                            if (thar >= 0) if ((info & 0x1000) == 0) { // can't adjust singleton far
                                ix2 = frnt & 0xFF00FF;
                                SeenWall[whar + 512] = ix2;
                                zx = 6;
                                if (skip != 0) // otherwise set corner angle..
                                    if (ix1 < 0) { // (unless already set) // ** (never<0)
                                        haff = SeenWall[base + 257] & 0x3FF;
                                        info = info & 0x3FF;
                                        if (info + 180 < haff) info = info + 360;
                                        else if (haff + 180 < info) haff = haff + 360;
                                        haff = ((haff + info) >> 1);
                                        if (haff > 360) haff = haff - 360;
                                        // tmp = ((haff+45)/90)&3;
                                        zx = haff + 45;
                                        if (zx == 0) tmp = 3; // round odd => prefer E-W wall..
                                        else if (zx == 180) tmp = 1;
                                        else tmp = (zx / 90) & 3; // = general compass heading
                                        ix1 = ix1 & -0x1000 | (tmp << 10) | haff;
                                        SeenWall[base + 256] = ix1; // view pie:7/view aim:10/:4/compass:10
                                        zx = 7;
                                    }
                            } //~if
                            if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0))
                                if (why < 99) { // if (Qlog..) -- in M_L
                                    System.out.println(HandyOps.Dec2Log("  (lnk_segs) ", zx,
                                            HandyOps.Dec2Log(" @ ", base, HandyOps.Int2Log(": ", frnt,
                                                    HandyOps.Int2Log(" ", ix1, HandyOps.Int2Log(" .. ", more,
                                                            HandyOps.Dec2Log(" @ ", whar, HandyOps.Int2Log(": ", px,
                                                                    HandyOps.Int2Log(" .. ", ix2,
                                                                            HandyOps.Int2Log(" ", ix3, ""))))))))));
                                    why++;
                                }
                        } //~for // (base)(skip)
                thar = 0;
                why = 0;
                zx = 0;
                errn = -1; // no error
                // doing = nBax;
                break;
            } //~if // (build/vfy preliminary data)

            // Preliminary data in SeenWall, four ints indexed by nBax+256i:
            //    +0: r,c (2m grid) of anchor, +HiBit if corner insert,
            //      +40 if segment head, +20 if linked, +10 if curve,
            //      +08 gotta recalc angle, +04 if corner insert turns left;
            //  +256: angle from anchor in lo 12, link to next in segment
            //      (temp'ly -lino of spec line) in hi 16;
            //  +512: r,c (2m grid) of far end (rocox) +HiBit if spec'd singleton;
            //  +768: temp'ly: -lino of spec line, then (if not head) link to head,
            //      else (link to left segment head, link to right segment head).
            //      Left end caps log'ly follow segment head, right caps are their
            //      own segment. Corner insert immed'ly precedes segment head.

            doing = 0; // if (xCh == 'G') .. // 'G vv hh dd ff+aa tt ww xx^yy pp nn*+'
            zx = 0;
            errn = -124;
            if (myMap == null) break;
            if (myMap.length < 8) break;
            dimz = myMap[3 + 3]; // park dimensions in scaled meters
            Pmap = myMap[2 + 3] + 3; // = offset to map => MapIxBase, now +hdr
            if (Pmap < 8) break;
            if (Pmap >= myMap.length) break;
            base = myMap[Pmap - 1];
            if (base < 0) break;
            if (base > 0) if (base < 8) break;
            nuly = 0;
            // didit = false; // T: repeat image
            // curvd = (xCh < 'G'); // (as init'd..) = false, cuz only "G" gets here
            whom = (xCh == 'G');
            if (whom) { // (xCh == 'G') // otherwise 'V'/'H'/'P'
                frnt = frnt << 1; // frnt = (rx<<16)+cx; // now in pk meters w/corners
                stub = (frnt << 8) + frnt; // (used for 1-cell end cap)
                // aStr = HandyOps.NthItemOf(false,5,aLine);
                // pie = HandyOps.SafeParseInt(aStr); // pie is current view angle
                xStr = HandyOps.NthItemOf(false, 9, aLine);
                ppm = HandyOps.SafeParseInt(xStr);
                zx = HandyOps.NthOffset(0, "$", xStr);
                if (zx > 0) WideRatio = (int) Math.round( // alternate H-ppm..
                        HandyOps.SafeParseFlt(HandyOps.RestOf(zx + 1, xStr)) * 256.0);
                tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                aWord = HandyOps.NthItemOf(false, 8, aLine);
                xStr = HandyOps.NthItemOf(false, 10, aLine);
                if (HandyOps.NthOffset(0, "*", xStr) >= 0) didit = true; // T: repeat image
                if (HandyOps.NthOffset(0, "%", aWord) > 0) centrd = true;
                px = ppm; // (unused)
                offx = HandyOps.SafeParseInt(aWord); // H-posn of pixels in image
                nx = HandyOps.NthOffset(0, "^", aWord);
                if (nx > 0) nx = HandyOps.SafeParseInt(HandyOps.RestOf(nx + 1, aWord));
                errn--; // errn = -125
                nxt = ppm;
                if (ppm < 2) break; // 1ppm OK with plain wall
                if (ppm > 256) break; // + needed for index +
                nxt = wide + 0x10000; // nxt is logged
                if (wide < 2) break; // + needed for index +
                nxt = offx; // nxt is logged
                if (offx < 0) break;
                nxt = offx + wide + 0x30000; // nxt is logged
                if (offx + wide > ImgWi) break; // errn = -125
                if (nx < 0) nx = 0;
                nx = nx + tall; // V-posn of pixels, nx-1 = bottom row
                nxt = tall + 0x40000; // nxt is logged
                if (tall > 1023) break; // errn = -125
                nxt = nx + 0x50000; // nxt is logged
                if (nx > ImgHi) break; // errn = -125
                offx = (nx - 1) * ImgWi + offx; // + needed for index + sb on botm row of image
                Vat = MyMath.Fix2flt(rx, 0);
                Hat = MyMath.Fix2flt(cx, 0);
                here = whar - nuIxBase; // in caller: whar = rx*HalfMap+cx+nuIxBase;
                if (zx > 0) if (WideRatio > 0) WideRatio = WideRatio / ppm + 0x80000000;
                errn--; // errn = -126
                if (Mini_Log) if ((Qlog & 256) != 0) TempStr = "";
                if (curvd) { // can't happen (not called)
                    lxx = HandyOps.SafeParseFlt(xStr); // wall length in park meters
                    radius = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    lxx = 0.0;
                } //~if
                else { // 'G' straight (also 'M').. // aCh = HandyOps.CharAt(1,aLine);
                    why = 16;
                    if (aCh >= 'c') curvd = true; // after mid-curve
                    else if (aCh == 'M') aCh = 'm';
                    // else if (aCh >= '0') if (aCh <= '9') aCh = 'm';
                    if (((pie & -512) == 0) && (pie <= 360)) { // pie is credible view angle
                        // aStr = HandyOps.NthItemOf(false,5,aLine);
                        // pie = HandyOps.SafeParseInt(aStr); // pie is current view angle
                        haff = HandyOps.NthOffset(0, "+", aStr);
                        if (haff > 0) { // haff is view pie half-width..
                            haff = HandyOps.SafeParseInt(HandyOps.RestOf(haff + 1, aStr));
                            if (haff < 0) haff = 0;
                            else if (haff > 90) haff = 0;  // valid pie setting is req'd,
                            if (haff == 0) pie = 0;        // ..for centered to work..
                            else if (centrd) pie = pie | 512;
                        } //~if // centered flag => 0x800000
                        else pie = 0;
                    } //~if
                    else pie = 0;
                    if (pie > 0) {
                        pie = pie << 2;
                        zx = HandyOps.NthOffset(0, "@", aLine);
                        if (zx > 0) zx = ((int) (HandyOps.CharAt(zx + 1, aLine))) & 0xDF;
                        else zx = 45; // '-' if no @
                        zCh = (char) (zx);
                        if (zCh == 'L') pie = pie + 2; // prefer left fase => 0x2000
                        else if (zCh == 'R') pie++;
                    } //~if // prefer right fase => 0x1000
                    // frnt = frnt<<1; // already in pk meters to match SeenWal
                    errn--; // errn = -127
                    nxt = HandyOps.NthOffset(0, aLine, Save4Log); // nxt is logged
                    if (nxt < 0) break;
                    aStr = HandyOps.Substring(0, nxt + 2, Save4Log);
                    doing = HandyOps.Countem("\n", aStr) + 1;
                    tmp = SeenWall[doing & 255]; // (SeenWall[nx]) be sure we are sync'd..
                    if (doing > 0) if (doing < 256) {
                        // prio = SeenWall[doing+768]; // maybe need to sort these? Naw.
                        rocox = SeenWall[doing + 512] & 0xFF00FF;
                        info = SeenWall[doing + 256];
                    } //~if
                    if (Mini_Log) System.out.println(HandyOps.Dec2Log("  *@ ", doing,
                            HandyOps.Int2Log(" [", tmp, HandyOps.Int2Log(" ", info,
                                    HandyOps.Int2Log("/", rocox, HandyOps.Int2Log("] : ", frnt,
                                            HandyOps.Dec2Log(" ", (int) aCh,
                                                    HandyOps.Dec2Log("/", (int) zCh,
                                                            HandyOps.Dec2Log(" ", nxt, HandyOps.TF2Log(" ", centrd, ""))))))))));
                    if (doing > 255) break; // errn = -127 (too many segments)
                    nxt = frnt;
                    if (frnt == 0) break; // errn = -127
                    if (((tmp ^ frnt) & 0xFE00FE) != 0) break;
                    frnt = tmp & 0xFF00FF;
                    if (zCh == 'C') if (aCh > ' ') { // if (pie>0) // (already known)
                        nxt = frnt ^ rocox; // gotta be on a compass point: same H is V-line,
                        if (((nxt & 255) == 0) || ((nxt & 0xFF0000) == 0)) // .. same V is H-line
                            pie = pie | 1024;
                    } //~if // end-cap flag => 0x400000
                    nxt = info;
                    if (info < 0) break; // ** never
                    engle = info & 0x3FF;
                    aim = (info >> 10) & 3; // aim = ((engle+45)/90)&3; // gen compass heading
                    nxt = 0;
                    errn--; // errn = -128
                    NuData = (((doing << 12) + aim) << 12) + rocox; // to compare next BG to
                    // ix0 = (0x06030309>>aim)&0x01010101;      // recalc angle from face..
                    ix0 = (rocox << 8) + frnt; // now in park meters + corners (used also below)
                    Vinc = Korner(2 * 8, ix0, logy) - Korner(3 * 8, ix0, logy); // logy = 1&&MapLogged
                    Hinc = Korner(1 * 8, ix0, logy) - Korner(0 * 8, ix0, logy); // ..  (Korn/1)
                    info = engle; // default, in case calc fails (but it shouldn't)
                    kx = 0;       // aCh = HandyOps.CharAt(1,aLine);
                    if (aCh == '0') if (HandyOps.CharAt(2, aLine) == 'M') {
                        aCh = HandyOps.CharAt(3, aLine);
                        if (aCh == ' ') aCh = '0';
                    } //~if
                    if (aCh == 'm') { // 'm' corner has its own angle..
                        nxt = HandyOps.SafeParseInt(HandyOps.Substring(2, 3, aLine));
                        // aim: eeeeaaiiii: eee=3-bit engle/45, aa=new aim, iiii=ix0 spec..
                        // .. 4=NE,5=SE,6=SW,7=NW (corner); 8=45 up diag, 9=135 dn, 10/11 W
                        if (nxt > 63) info = (nxt >> 6) * 45; // nxt>>6 = (ImWalCurves[ix]>>8)&7
                        else nxt = 0;
                    } //~if
                    else if (aCh >= '0') if (aCh <= '9') {
                        nxt = ((int) (aCh)) & 15;
                        if (info > 0) nxt = info;
                        else if (nxt == 0) {
                            info = 360;
                            nxt = 4;
                        } //~if
                        else if (nxt > 8) info = 135;
                        else if (nxt == 8) info = 45;
                        else if (nxt < 4) info = nxt * 90;
                        else info = nxt * 90 - 45; // if (nxt<8) if (nxt >= 4)
                        aCh = 'm';
                    } //~if // (so loop will stop)
                    if (nxt == 0) if (rocox != frnt)
                        info = (int) Math.round(MyMath.aTan0(Hinc, Vinc)); // cw from N
                    kx = info << 16;
                    if (info <= 0) info = info + 360;
                    nx = MyMath.iAbs(info - engle);
                    kx = nx & 0xFFFF | kx;
                    if (nx > 180) nx = nx - 360;
                    if (nx != 0) if ((nxt & 8) != 0) { // 'm' diagonals can be 180 out of phase..
                        if (nx >= 180) nx = nx - 180;
                        else if (nx + 180 <= 0) nx = nx + 180;
                    } //~if
                    if (MyMath.iAbs(nx) > 47) InWalls = true; // mark & notify, but keep going
                    // engle = (aim<<10)+info; // pie width:7 / view aim:10/:4 / compass:10
                    // aStr = HandyOps.NthItemOf(false,5,aLine);
                    // haff = HandyOps.SafeParseInt(HandyOps.RestOf(haff+1,aStr)); // '+'
                    // pie = HandyOps.SafeParseInt(aStr); // pie is current view angle
                    ix1 = (aim << 10) + info; // pie width:7 / view aim:10/:4 / compass:10
                    if (pie >= 0) if (haff > 0) if (haff <= 90)
                        ix1 = (((haff << 12) + pie) << 12) + ix1; // pie half-width,view,aim,angle
                    if (WideRatio < 0) {
                        ix1 = ix1 | 0x200;
                        WideRatio = WideRatio & 0xFFFFFF;
                    } //~if
                    ix2 = (ppm << 24) + offx;
                    ix3 = 0;
                    if (didit) { // "*": recycle image (lxx=0 OK)..
                        ix1 = ix1 | 0x80000000;
                        why = why + 2;
                    } //~if     // xStr = HandyOps.NthItemOf(false,10,aLine);
                    else if (HandyOps.NthOffset(0, "!", xStr) >= 0) {
                        if (false) if (lxx > 0.0) {
                            ftmp = MyMath.fMax(MyMath.Fix2flt(wide, 0) / lxx, 1.0);
                            ix3 = MyMath.iMin(MyMath.Trunc8(ftmp), 63);
                        } //~if // (obsolete)
                        ix3 = 0x80000000;
                    } //~if // H-ppm to stretch // was: = ((nx|128)<<24)+ix3;
                    ix3 = (tall << 16) + wide + ix3;
                    if (Qlog != 0) if (InWalls || Mini_Log || ((Qlog & 256) != 0))
                        System.out.println(HandyOps.Dec2Log(HandyOps.IffyStr(InWalls,
                                "  <??> ", "  <==> "), nx,
                                HandyOps.Dec2Log(" ", info, HandyOps.Dec2Log("/", engle,
                                        HandyOps.Int2Log(" ", frnt, HandyOps.Int2Log("/", rocox,
                                                HandyOps.Flt2Log(" ", Vinc, HandyOps.Flt2Log("/", Hinc,
                                                        HandyOps.Int2Log(" ", kx, HandyOps.Hex2Log(" x", nxt, 3, //_10
                                                                HandyOps.Dec2Log(" ", pie, HandyOps.Dec2Log(" ", haff,
                                                                        HandyOps.Int2Log(" => ", ix1, HandyOps.Int2Log(" ", ix2,
                                                                                HandyOps.Int2Log(" ", ix3, HandyOps.IffyStr((ix1 & 0x200) == 0, "",
                                                                                        HandyOps.Dec2Log(" ", WideRatio, HandyOps.Fixt8th("=",
                                                                                                WideRatio >> 13, ""))))))))))))))))));
                    nxt = 0;
                }
            } //~if // ('G' straight)(whom)   (in BackImag)

            else { // if (!whom) = (xCh != 'G') // else 'V'/'H'/'P' (plain wall)..
                errn = -129; // aStr = HandyOps.NthItemOf(false,5,aLine)
                nxt = HandyOps.NthOffset(0, aLine, Save4Log); // nxt is logged
                if (nxt < 0) break;
                aStr = HandyOps.Substring(0, nxt + 2, Save4Log);
                doing = HandyOps.Countem("\n", aStr) + 1;
                tmp = SeenWall[doing & 255]; // be sure we are sync'd..
                if (doing > 0) if (doing < 256) {
                    // prio = SeenWall[doing+768]; // maybe need to sort these? Naw.
                    rocox = SeenWall[doing + 512] & 0xFF00FF;
                    info = SeenWall[doing + 256];
                } //~if
                if (Mini_Log) System.out.println(HandyOps.Dec2Log("  *@_", doing,
                        HandyOps.Int2Log(" ", tmp, HandyOps.Int2Log(" ", info,
                                HandyOps.Int2Log("/", rocox, HandyOps.Int2Log(" : ", frnt,
                                        HandyOps.Dec2Log(" ", nxt, "")))))));
                if (doing > 255) break; // errn = -129 (too many segments)
                // curvd = false; // T: no back side
                nxt = 0;
                errn = -139; // only if omitted back side
                tmp = engle; // ,tmp, is logged at exit
                dx = rx + rx; // rx,cx in grids, dx,kx in pk meters
                kx = cx + cx; // dx,kx are logged at exit
                ix2 = -1; // [ppm/file offset] =-1 if ImgWall=plain
                zx = 0;
                aim = 0;
                if ((engle & 0x8000) != 0) aim = 2; // >1 for S & W
                rpt = engle >> 10;
                ix3 = 0xFFF - (rpt & 7); // [pixel H,W of this image] =colo if plain
                rpt = (rpt >> 6) & 127; // end V/H grid cell
                rocox = frnt; // both still in grid units
                tall = dimz >> 16; // park dimensions in scaled (park) meters
                wide = dimz & 0xFFFF;
                tmp = engle & 511; // ,tmp, is logged at exit
                if (xCh == 'P') { // view pie width:7 / view aim:10/:4 / compass:10
                    xStr = HandyOps.NthItemOf(false, 4, aLine);
                    zx = HandyOps.NthOffset(0, "*", xStr);
                    if (zx > 0) {
                        nx = HandyOps.SafeParseInt(HandyOps.RestOf(zx + 1, xStr)) - 1;
                        zx = nx << 1;
                        if (nx <= 0) zx = 0;
                        else if (nx + dx > tall) zx = 0;
                        else if (nx + kx > wide) zx = 0;
                        else zx++;
                    } //~if
                    nx = HandyOps.SafeParseInt(xStr);
                    if (nx > 0) if (nx < 8) ix3 = 0xFFF - nx;
                    nx = tmp;       // view pie width =90:7 / view aim(:4) +90:10
                    tmp = tmp + 0x5A168000;
                } //~if
                if (xCh == 'V') {
                    Vstp = 1.0; // -MyMath.Cose; (otherwise =0)
                    ix0 = 0x01000000; // default top left to bottom left
                    if (aim == 0) { // back side = South -> North
                        ix0 = 0x00010101; // otherwise bottom right to top right
                        if (kx + 4 > wide) break; // too close to right, so no east side
                        frnt = (rpt << 16) + cx;
                        rx = rpt;
                        Vstp = -Vstp;
                    } //~if
                    else if (kx < 4) break; // too close to left, so no west side
                    else rocox = (rpt << 16) + cx;
                    doit = true;
                } //~if
                else if (xCh == 'H') {
                    ix0 = 0x01010100; // default bottom left to bottom right
                    Hstp = 1.0; // MyMath.Sine; (otherwise =0)
                    if (aim != 0) { // back side = East -> West
                        ix0 = 0x00000001; // otherwise top right to top left
                        if (dx < 4) break; // errn = -139: too close to top so no north side
                        frnt = (frnt & -0x10000) + rpt;
                        cx = rpt;
                        Hstp = -Hstp;
                    } //~if
                    else if (dx + 4 > tall) break; // too close to bottom, so no south side
                    else rocox = (frnt & -0x10000) + rpt;
                    aim++;
                } //~if
                else if (nx == 0) { // (xCh == 'P') South -> North, east side..
                    if (zx > 0) {
                        ix0 = zx * 0x00010101;
                        kx = kx + zx;
                    } //~if
                    else ix0 = 0x00010101;  // .. = bottom right to top right
                    if (kx + 4 > wide) break; // too close to right, so no east side
                    Vstp = -1.0;
                } //~if
                else if (nx == 180) { // (aim=2) North -> South, west side..
                    if (zx > 0) ix0 = zx * 0x01000000;
                    else ix0 = 0x01000000;  // .. = top left to bottom left
                    if (kx < 4) break; // too close to left, so no west side
                    Vstp = 1.0;
                } //~if
                else if (aim == 0) { // (nx==90) West -> East, south side..
                    if (zx > 0) {
                        ix0 = zx * 0x01010100;
                        dx = dx + zx;
                    } //~if
                    else ix0 = 0x01010100;  // .. = bottom left to bottom right
                    if (dx + 4 > tall) break; // too close to bottom, so no south side
                    Hstp = 1.0;
                    aim++;
                } //~if
                else { // (nx==270) (aim=2) // East -> West, north side..
                    if (zx > 0) ix0 = zx * 0x00000001;
                    else ix0 = 0x00000001;  // .. = otherwise top right to top left
                    if (dx < 4) break; // errn = -139: too close to top so no north side
                    Hstp = -1.0;
                    aim++;
                } //~else
                ix1 = (aim << 10) + tmp; // view pie width:7 / view aim:10/:4 / compass:10
                rocox = rocox << 1; // now in pk meters
                frnt = frnt << 1;
                ix0 = (rocox << 8) + frnt + ix0; // now in park meters + corners
                here = rx * HalfMap + cx; // (rx,cx might changed)..
                whar = here + nuIxBase;
                Vat = MyMath.Fix2flt(rx, 0);
                Hat = MyMath.Fix2flt(cx, 0);
                curvd = false;
                doit = false;
            } //~else // (xCh != 'G') (else 'V'/'H'/'P')     (in BackImag)

            errn = -141; // load up index.............................................
            nxt = nBax; // nxt is logged; // nBax = MyMath.iMin(nBax,255)+kx;
            if (MyMath.SgnExt(nBax) <= 0) break; // max 255 wall segments
            if (sofar <= 0) break;
            errn--; // errn = -142
            nxt = frnt; // nxt is logged // (vfy only, unused; => ix0)
            if (frnt <= 0) break; // frnt=(rx*HalfMap+cx)<<1
            if (myMap == null) break; // already checked
            nBax--;
            // if (lxx>0.0) dx = MyMath.Trunc8(lxx*MyMath.Fix2flt(px,0));
            //   else dx = wide;
            // Vinc = 0.5/Hinc; // was ppm, now 2m grid units / pix
            Vinc = 0.25; // quarter-grid steps is good enough
            Hinc = Hstp * Vinc; // to advance 1 pix in direction aimed
            Vinc = Vstp * Vinc; // ..known <1m (Vstp=-Cose, Hstp=Sine)
            info = sofar; // (includes header offset)
            errn--; // errn = -143
            if (info < 0) break;
            if (info > myMap.length - 5) break;
            nBax = nBax + 0x40000; // OK, commit..
            nx = info - 3; // remove header offset
            errn--; // errn = -144
            if (base == 0) { // base = myMap[Pmap-1];
                base = info;
                if (Pmap < 8) break; // already checked
                if (Pmap >= myMap.length) break;
                myMap[Pmap - 1] = nx;
            } //~if // omit hdr
            myMap[info] = ix0;   // +0: map coords in park meters (+ end coords)
            myMap[info + 1] = ix1; // +1: pie width:7/view aim:10/:4/compass:10/(*)
            myMap[info + 2] = ix2; // +2: ppm/pix offset (=-1 if plain)
            myMap[info + 3] = ix3; // +3: height and width (H-ppm) (=colo if plain)
            info = ((info + 4 - base) >> 2) + izBG; // convert to BG+ + flag (log only)
            Pmap = Pmap + GridSz; // now it's a 4-byte offset into map
            didit = false;
            if (MyMath.fAbs(Vinc) > MyMath.fAbs(Hinc)) {
                haff = rocox >> 16;
                didit = true;
            } //~if // didit=T: test V
            else haff = rocox;
            haff = (haff >> 1) & 127; // was meters, need grids to compare
            if (doing > 0) { // if (whom) // if (Qlog..) -- included in M_L
                aWord = HandyOps.NthItemOf(true, doing, Save4Log);
                if (aWord != "") {             // nx = info-3; // -hdr.offs..
                    while (HandyOps.Countem("\n", Save4Log) < doing)
                        Save4Log = Save4Log + "\n\n\n\n";
                    aWord = HandyOps.Dec2Log("@ ", nx,
                            HandyOps.Dec2Log(" #", info & 255, " = ")) + aWord;
                    Save4Log = HandyOps.RepNthLine(aWord, doing, Save4Log);
                    doit = true;
                    if (doing > 4) if (doing != whoz) if ((doing & 15) != 0) {
                        aWord = "\n  " + aWord;
                        doit = false;
                    } //~if
                    if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0))
                        System.out.println(HandyOps.Dec2Log("+-+ ", doing,
                                HandyOps.Int2Log(" = ", info,
                                        HandyOps.IffyStr(doit, Save4Log, aWord))));
                } //~if
                aStr = "/";
                aWord = "\n      @@@ ";
                if (MyMath.fMax(MyMath.fAbs(Vinc), MyMath.fAbs(Hinc)) < 1.0) {
                    aWord = "cm\n      @@@ ";
                    aStr = "cm/";
                    mpyr = 100.0;
                } //~if
                aWord = HandyOps.Flt2Log(aWord, Vstp, HandyOps.Flt2Log("/", Hstp, " "));
                System.out.println(HandyOps.Dec2Log("<BackImg> ", rx,
                        HandyOps.Dec2Log("/", cx, HandyOps.Int2Log(" ", rocox,
                                HandyOps.Dec2Log(HandyOps.IffyStr(curvd, "\\", "/"), haff,
                                        HandyOps.TF2Log(" ", didit, HandyOps.Dec2Log(" ", aim,
                                                HandyOps.Int2Log(" ", engle, HandyOps.Dec2Log("/", pie,
                                                        HandyOps.Dec2Log(" ", doing, HandyOps.Dec2Log(" ", tall,    // _11
                                                                HandyOps.Dec2Log("/", wide, HandyOps.Dec2Log(" ^ ", offx,
                                                                        HandyOps.Dec2Log(" ", ppm, HandyOps.Dec2Log("/", px,
                                                                                HandyOps.Dec2Log(" ", zx, HandyOps.Flt2Log(" +", Vinc * mpyr,
                                                                                        HandyOps.Flt2Log(aStr, Hinc * mpyr,
                                                                                                HandyOps.Dec2Log(aWord, nx, HandyOps.Int2Log("/", info,
                                                                                                        HandyOps.Int2Log(": [", ix0,
                                                                                                                HandyOps.Int2Log(" ", ix1, HandyOps.Int2Log(" ", ix2,
                                                                                                                        HandyOps.Int2Log(" ", ix3, HandyOps.Dec2Log("] ", base,
                                                                                                                                HandyOps.Int2Log(" ", nBax,
                                                                                                                                        HandyOps.Dec2Log(" ", (int) aCh,
                                                                                                                                                "\n    __ '" + aLine + "'")))))))))))))))))))))))))));
            } //~if
            doit = true;
            px = ppm << 17;
            aim = aim & 1;
            // fust = true;
            for (nx = 0; nx <= 9999; nx++) { // lay down this wall in its grid locns................
                prio = 0;
                thar = (here >> 2) + Pmap; // init'ly: here = rx*HalfMap+cx
                kx = (here & 3) << 3;
                if (thar < 0) errn = 0; // 0: exit loop after log (off park)..
                if (rx < 0) errn = 0;
                if (rx >= HalfTall) errn = 0;
                if (cx < 0) errn = 0;
                if (cx >= HalfMap) errn = 0;        // 'm' always stores..
                if (errn != 0) if (aCh != 'm') if (thar >= 0) if (thar < myMap.length) {
                    prio = (myMap[thar] >> kx) & 255;
                    // if (((info-prio)&255)==0) doit = false;} //~if // same BG+
                    if (prio != 0) doit = false;
                } //~if // early bird gets the worm
                // else if ((prio&-0x10000000) != izBG) prio = 0;} //~if // not a BG
                if (Mini_Log) if (MapLogged || ((Qlog & 256) != 0)) {
                    aWord = ": ";               // if (Qlog..) -- included in M_L
                    if (prio != 0) aWord =
                            HandyOps.Dec2Log(".. ", prio, " != ");
                    if (prio == 0 || errn == 0)
                        System.out.println(HandyOps.Dec2Log("  .. ", nx,
                                HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                                        HandyOps.Flt2Log(" (", Vat, HandyOps.Flt2Log("/", Hat,
                                                HandyOps.Dec2Log(") ", prio, HandyOps.TF2Log(" ", doit, // doit: poke
                                                        HandyOps.TF2Log(" ", didit, // errn: off park..
                                                                HandyOps.IffyStr(errn == 0, " **", HandyOps.IffyStr(!doit, "", // _10
                                                                        HandyOps.Dec2Log(" @ ", here, HandyOps.Dec2Log("/", thar,
                                                                                HandyOps.Dec2Log("/", whar, HandyOps.Dec2Log(aWord, doing,
                                                                                        HandyOps.Int2Log("/", info, ""))))))))))))))));
                    aWord = "";
                } //~if
                if (errn == 0) break;
                if (doit) { // T: poke this cell..
                    if (prio == 0) if (whar >= 0) if (whar < myMap.length) {
                        zx = myMap[thar] & ~(255 << kx);
                        if (whom) zx = (doing << kx) | zx;
                        myMap[thar] = zx;
                        myMap[whar] = info;
                        if (aCh == 'm') break;
                    } //~if // always stop after 1st poke of singletn
                    doit = false;
                } //~if
                if (didit) if (rx == haff) break; // test exit even if didn't poke..
                if (cx == haff) break; // didit=T: test V, reached seg end (normal exit)
                Vat = Vat + Vinc; // init'ly: Vat,Hat = rx,cx
                Hat = Hat + Hinc;
                Vx = MyMath.Trunc8(Vat) - rx;
                Hx = MyMath.Trunc8(Hat) - cx;
                if (Vx != 0) {
                    if (Vx == 1) zx = HalfMap;
                    else if (Vx + 1 == 0) zx = -HalfMap;
                    else zx = Vx * HalfMap;
                    here = here + zx;
                    whar = whar + zx;
                    rx = rx + Vx;
                    if (aim == 0) info = info + px;
                    doit = true;
                } //~if
                if (Hx != 0) {
                    here = here + Hx;
                    whar = whar + Hx;
                    cx = cx + Hx;
                    if (aim != 0) info = info + px;
                    doit = true;
                }
            } //~for // (nx)
            nxt = 0;
            errn = -1; // no error
            break;
        } //~while     // why=0 always except 'G' and vfy prelim (' ')..
        if (why == 0) why = errn;  // dunno what failed, log everything..
        didit = false;
        if (curvd) if (errn == -1) if (xCh > ' ') didit = true;
        if (MapLogged) if ((errn <= 0) || Mini_Log || ((Qlog & 256) != 0)) {
            if (aLine != "") {                      // Mini_Log=T
                aLine = HandyOps.Dec2Log("\n    __ ", lino, " '") + aLine + "'";
                if (didit) aLine = aLine + "\n      ==> '"
                        + HandyOps.NthItemOf(true, lino, theList) + "'";
            } //~if
            else aLine = " _''";
            if ((ix1 & -0x10000) == 0) aStr = HandyOps.Hex2Log(" x", ix1, 4, " ");
            else aStr = HandyOps.Hex2Log(" x", ix1, 8, " ");
            aLine = HandyOps.Int2Log(" [", ix0, HandyOps.Int2Log(aStr, ix2,
                    HandyOps.Int2Log(" ", ix3, "]"))) + aLine;
            for (nx = 0; nx <= doing + 8; nx++) {
                if (nx > 255) break;
                if (SeenWall == null) break;
                if (doing != whoz) if (nx > 8) if ((nx & 15) != 0)
                    if ((nx + 1 < whoz) || (nx - 1 > whoz)) continue;
                // Preliminary data in SeenWall, four ints indexed by nBax+256i:
                //    +0: r,c (in meters) of anchor, +HiBit if corner insert,
                //      +40 if segment head, +20 if linked, +10 if curve,
                //      +08 gotta recalc angle, +04 if corner insert turns left;
                //  +256: angle from anchor in lo 12, link to next in segment
                //      (temp'ly -lino of spec line) in hi 16;
                //  +512: r,c (in meters) of far end (rocox) +HiBit if spec'd singleton;
                //  +768: temp'ly: -lino of spec line, then (if not head) link to head,
                //      else (link to left segment head, link to right segment head).
                //      Left end caps log'ly follow segment head, right caps are their
                //      own segment. Corner insert immed'ly precedes segment head.
                ix0 = SeenWall[nx];
                ix1 = SeenWall[nx + 256];
                ix2 = SeenWall[nx + 512];
                ix3 = SeenWall[nx + 768];
                if (ix3 < 0) if ((((ix3 >> 16) - ix3) & 0xFFFF) == 0) ix3 = MyMath.SgnExt(ix3);
                // if ((ix1&0x2000) !=0) ix1 = ix1&0x1FFF; // remove link (unneed)
                if ((ix0 | ix1 | ix2 | ix3) == 0) continue;
                if ((ix1 & -0x1000) == 0) aStr = HandyOps.Hex2Log(" x", ix1, 3, " ");
                else if ((ix1 & -0x10000) == 0) aStr = HandyOps.Hex2Log(" x", ix1, 4, " ");
                else aStr = HandyOps.Hex2Log(" x", ix1, 8, " ");
                aWord = HandyOps.NthItemOf(true, nx, Save4Log);
                if (aWord == "") continue;
                aWord = HandyOps.Dec2Log("\n#", nx, HandyOps.Int2Log(": ", ix0,
                        HandyOps.Int2Log(aStr, ix2, HandyOps.Int2Log(" ", ix3, "; "))))
                        + aWord;
                zx = HandyOps.NthOffset(0, ";", aWord) + 1;
                if (zx > 3) if (aWord.length() > 80)
                    aWord = HandyOps.Substring(0, zx, aWord) + "\n  "
                            + HandyOps.RestOf(zx, aWord);
                aLine = aLine + aWord;
            } //~for // (nx)
            if ((Qlog & 256) != 0) if (why == -125) aLine = aLine + TempStr;
            if (nxt < 0) if (errn == -127) if (nCrumbs == 0) { // (failing in Java..)
                aLine = aLine + "\nSave4Log.." + Save4Log;
                nCrumbs++;
            } //~if
            System.out.println(HandyOps.Int2Log("<BackIm> = ", why,  // why = // errn =
                    HandyOps.Dec2Log(" ", doing, HandyOps.Int2Log("/", NuData,
                            HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                                    HandyOps.Dec2Log(" +", tall, HandyOps.Dec2Log("/", wide,
                                            HandyOps.TF2Log(" ", curvd, HandyOps.TF2Log(" ", whom,   // _10
                                                    HandyOps.Dec2Log(" ", dx, HandyOps.Dec2Log(" ", offx,
                                                            HandyOps.Dec2Log(" ", Pmap, HandyOps.Dec2Log(" ", base,
                                                                    HandyOps.Dec2Log(" ", whar, HandyOps.Dec2Log(" ", radius,
                                                                            HandyOps.Flt2Log(" ", lxx, HandyOps.Dec2Log(" ", nx,
                                                                                    HandyOps.Int2Log(" ", tmp, HandyOps.Int2Log("/", kx,     // _20
                                                                                            HandyOps.TF2Log(" ", didit,
                                                                                                    HandyOps.Int2Log("\n  ", rocox, HandyOps.Int2Log(" ", engle,
                                                                                                            HandyOps.Dec2Log("/", pie, HandyOps.Flt2Log(" ", ftmp,
                                                                                                                    HandyOps.Flt2Log(" ", Vinc, HandyOps.Flt2Log("/", Hinc,
                                                                                                                            HandyOps.Int2Log(" ", info, HandyOps.Dec2Log(" ", thar,
                                                                                                                                    HandyOps.Dec2Log(" ", aim, HandyOps.Dec2Log(" ", skip,   // _31
                                                                                                                                            HandyOps.Flt2Log(" ", Vstp, HandyOps.Flt2Log("/", Hstp,
                                                                                                                                                    HandyOps.Dec2Log(" ", prio, HandyOps.TF2Log(" ", doit,
                                                                                                                                                            HandyOps.Int2Log(" ", more, HandyOps.Dec2Log(" ", ppm,
                                                                                                                                                                    aLine))
                                                                                                                                                    )))))))))))))))))))))))))))))))))));
        } //~if
        TmpI = nxt; // nxt is (sometimes) logged by caller
        if (didit) TempStr = theList;
        else TempStr = "";
        return errn;
    } //~BackImage

    private static int CurvyTrak(boolean whom, int rad, int lim,
                                 int skip, int aim, int prio, int tmp, int lino, String aLine,
                                 String theList) {                           // whom=T: turn left
        int tall, wide, far, nx, kx = prio + tmp, dx = aim,
                rx = kx >> 16, cx = (kx & 0xFFFF), zx = rad, rpt = -111, info = 1,
                Vx = 0, Hx = 0, ink = 0, odds = 0, mpyr = 0, engle = 0, base = 0;
        boolean didit = false, logy = false;
        String aWord = "";
        Moved = false; // borrow this as rtn value
        while (true) { // 12*brk, all errs, most logged here:
            if (rad < 999) while (rad > 16) {
                odds = odds | rad & info; // (not using this yet)
                info = info << 1;
                rad = rad >> 1;
                mpyr++;
            } //~while
            if (skip > 45) {
                if (whom) dx++;
                else dx--;
                dx = dx & 3;
                didit = true;
            } //~if
            if (MapLogged) if (Mini_Log) { // if (Qlog..) -- included in M_L
                aWord = "=" + HandyOps.CharAt(aim & 3, "NESW")
                        + HandyOps.IffyStr(dx == aim, "", "/"
                        + HandyOps.CharAt(dx & 3, "NESW")) + " +";
                System.out.println(HandyOps.TF2Log("<CurvTrak> ", whom,
                        HandyOps.Dec2Log(" ", zx, HandyOps.Dec2Log(" (", lim,
                                HandyOps.Dec2Log("-", skip, HandyOps.Dec2Log(") ", aim,
                                        HandyOps.Dec2Log(aWord, rx, HandyOps.Dec2Log("/", cx,
                                                HandyOps.Int2Log(" (", prio, HandyOps.Int2Log(" +", tmp,
                                                        HandyOps.Dec2Log(") #", lino, HandyOps.Dec2Log(" --> ", rad,
                                                                HandyOps.Dec2Log(" [", odds, HandyOps.Int2Log("/", info,
                                                                        HandyOps.Dec2Log("/", mpyr, "]")))))))))))))));
            } //~if
            if (skip > 0) prio = 256;
            else prio = -256;
            if (lim <= 0) break; // rpt = -111 (shouldn't hap, caller checked)
            if (skip < 0) break;
            if (lim <= skip) break;
            if (aim < 0) break; // (shouldn't hap, ditto)
            if (aim > 3) break;
            rpt--; // rpt = -112
            if (rad < 0) break; // (shouldn't hap, checked by caller)
            if (rad > 16) break;
            base = CurveSteps[rad];
            rpt--; // rpt = -113
            if (base < 16) break; // bad table..
            if (base > CurveSteps[16]) break;
            rpt--; // rpt = -114
            if (rx <= 0) break;
            if (cx == 0) break;
            if (cx >= MapWide * 4) break;
            for (rpt = 99; rpt >= 0; rpt += -1) { // 5*brk, (one at end), 1*cont (same as BackIm)
                if (base < 0) break;
                if (base >= CurveSteps.length) break;
                info = CurveSteps[base];
                engle = (info >> 8) & 127;
                far = engle; // (for log)
                nx = info >> 16;
                zx = info & 255;
                kx = 0;
                tmp = prio & 255;
                if (info == 0) kx--; // normal exit
                else if (lim < engle) { // less than 90 degrees, terminate early
                    kx--;         // interpolation works same as for skip (below)..
                    if (prio < 0) if (lim > tmp)
                        if (engle > tmp) {
                            prio = ((engle - lim) << 8) / (engle - tmp); // =interpolation fraction
                            if (engle > 45) if (lim < 45) engle = lim;
                            kx = 0;
                        }
                } //~if // (do partial then exit)
                else if (engle <= skip) kx++; // not yet past start angle
                else if (prio > 255) if (skip > 0) if (skip > tmp) if (engle > tmp)
                    // engle-skip is the number of degrees to keep in prev segment
                    // prio is prior engle, so skip-prio is the number of degrees skipped
                    // so the discard ratio is (skip-prio)/(engle-prio)..
                    prio = ((skip - tmp) << 8) / (engle - tmp); // =interpolation fraction
                tall = nx;
                wide = zx;
                if (!didit) if (engle > 45) {
                    if (kx == 0) {
                        if (whom) aim--;
                        else aim++;
                        aim = aim & 3;
                    } //~if
                    didit = true;
                } //~if
                if (mpyr > 0) {
                    if (engle < 45) wide = ((wide - 4) << mpyr) + 4;
                    else wide = wide << mpyr;
                    if (engle > 45) tall = ((tall + 4) << mpyr) - 4;
                    else tall = tall << mpyr;
                } //~if
                ink = 0; // face is selected based on new aim..
                if (aim == 1) ink--; // eastward (to h.9)..
                else if (aim == 2) ink++; // southward (to v.9)
                if (prio > 0) if (prio < 256) { // interpolate partial segment..
                    wide = wide - (((wide * prio + 127) >> 8) & -8); // don't change lo3 fract bits
                    tall = tall - (((tall * prio + 127) >> 8) & -8);
                } //~if
                if (whom) wide = -wide;
                switch (dx) { // offsets are added based on original aim..
                    case 0: // northward..
                        Vx = rx - tall;
                        Hx = cx + wide;
                        break;
                    case 1: // eastward..
                        Vx = rx + wide;
                        Hx = cx + tall;
                        break;
                    case 2: // southward..
                        Vx = rx + tall;
                        Hx = cx - wide;
                        break;
                    case 3: // westward..
                        Vx = rx - wide;
                        Hx = cx - tall;
                        break;
                } //~switch
                if ((aim & 1) == 0) { // flush it to face in new aim..
                    Vx = (Vx + 4) & -8; // north or south face
                    tmp = Vx - rx; // = advance in faced dir'n
                    if (aim == 0) tmp = -tmp;
                } //~if // (north)
                else {
                    Hx = (Hx + 4) & -8; // east or west face
                    tmp = Hx - cx; // = advance in faced dir'n
                    if (aim == 3) tmp = -tmp;
                } //~else // (west)
                if (Qlog < 0) if (logy) {
                    aWord = " => ";
                    if (far != engle) aWord =
                            HandyOps.Dec2Log("/", far, aWord);
                    System.out.println(HandyOps.Dec2Log("   .~. ", 99 - rpt,
                            HandyOps.Dec2Log(" ", base, HandyOps.Int2Log(": ", info,
                                    HandyOps.Dec2Log(" = +", nx, HandyOps.Dec2Log("/", zx,
                                            HandyOps.Dec2Log(" % ", engle, HandyOps.Dec2Log(aWord, tall,
                                                    HandyOps.Dec2Log("/", wide, HandyOps.TF2Log(" ", didit,
                                                            HandyOps.Dec2Log(" ", aim, HandyOps.Dec2Log(" ", prio,
                                                                    HandyOps.IffyStr(kx > 0, " ++", HandyOps.IffyStr(kx < 0, " --",
                                                                            HandyOps.Dec2Log(" = ", Vx, HandyOps.Dec2Log(HandyOps.IffyStr(ink > 0,
                                                                                    "-1/", "/"), Hx, HandyOps.IffyStr(ink < 0, "-1", "")
                                                                                    + HandyOps.IffyStr(tmp > 0, "", " =0")
                                                                                    + HandyOps.IffyStr(lim <= engle, " =!", "")))))))))))))))));
                } //~if
                base++;
                prio = engle | 256; // save for proportionality
                if (kx < 0) break; // normal exit
                if (kx > 0) continue; // not yet at start angle
                prio = prio - 512; // past it, disable interpolation
                rx = Vx;
                cx = Hx;
                if (lim < engle) engle = lim;
                if (ink < 0) Hx--;
                else if (ink > 0) Vx--;
                if (whom) aWord = " -- Left ";
                else aWord = " -- Right ";
                if (engle == 90) aWord =
                        HandyOps.Dec2Log(" ", Vx >> 3,
                                HandyOps.Dec2Log(" ", Hx >> 3, aWord));
                else aWord = HandyOps.Fixt8th(" ", Vx, HandyOps.Fixt8th(" ", Hx, aWord));
                if (tmp > 0) aLine = aLine + "\n" + HandyOps.CharAt(aim, "NESW")
                        + HandyOps.Dec2Log(aWord, rad, HandyOps.Dec2Log(" @ ", engle, " degs"));
                if (lim <= engle) break;
            } //~for // (rpt)
            theList = HandyOps.RepNthLine(aLine, lino, theList);
            if (MapLogged) if (Qlog < 0) {
                System.out.println("    __ " + HandyOps.ReplacAll("\n[*]=> ", "\n", aLine));
                Moved = true;
            } //~if
            rpt = -1; // no error
            break;
        } //~while
        TempStr = theList;
        return rpt;
    } //~CurvyTrak

    private static int[] Mappy0(String theList, String aLine,
                                int ImDims, int[] theImgs) {         // aLine is 1st line of theList
        int lino, rpt, ImgHi = ImDims >> 16, ImgWi = MyMath.SgnExt(ImDims),
                tall = ImgHi, wide = ImgWi, prio = -1, kx = 0, yx = 0, zx = 0,
                here = 0, thar = 0, nxt = 0, colo = 0, ImSz = 0,
                nImgs = 0, sofar = 0, xcell = 0, AfterStop = 0, nCells = 0,
                PaintIx = 0, wait = 0, nerrs = 0, info = 0, aim = 0;
        boolean EWx = false, didit = false, rez = false;
        // AnimInfo[5] = myMap[ArtBase(+3)] -> myMap[1stLst] -> 1st obj & posn
        // AnimInfo[6]..[15] = myMap[ArtBase(+3)+1].. -> myMap[2ndLst]..
        // all non-anim8 obj's come next, then 00, then myMap[1stLst]
        // myMap[1stLst+0] -> 1st obj, 1stPosn & time
        // myMap[1stLst+1] -> 1st obj, 2stPosn & time = 1stPosn+5
        int[] myMap = null;
        String aStr = "", xStr = "\n.StopInfo\n", theText = StopInfo,
                aWord = HandyOps.NthItemOf(false, 5, aLine);
        char xCh = HandyOps.CharAt(0, aWord);
        for (lino = 0; lino <= 32; lino++) DidCells[lino] = 0;
        if (SeenWall != null)
            for (lino = 511; lino >= 0; lino += -1) SeenWall[lino] = 0;
        if (theImgs != null) nxt = theImgs.length;
        if (xCh >= '0') if (xCh <= '9') {
            zx = HandyOps.NthOffset(0, ",", aWord);
            if (zx > 0) { // got spec'd pebble contrast..
                zx = HandyOps.SafeParseInt(HandyOps.RestOf(zx + 1, aWord));
                if (zx >= 0) if (zx <= 9) aim = zx << 8;
            } //~if
            zx = HandyOps.SafeParseInt(aWord);
            if ((zx & 127) != zx) zx = (int) Math.round(WhiteLnWi * 100.0); // (default)
            aim = (zx + aim) << 16;
        } //~if // line width in cm
        zx = 0; // nLumins = 0;
        nuIxBase = ArtBase + 3; // 8+3 -> 11
        if (nxt > 0) yx = GotImgOps(theList); // >0: has paint, <0: other ims
        if (yx != 0) while (true) { // we have managed art or paint..
            kx = HandyOps.NthOffset(0, xStr, theList); // xStr = "\n.StopInfo\n"
            xStr = "";
            if (yx > 0) {
                xcell = GridSz >> 3; // sb =6400, the map size
                PaintIx = 3;
            } //~if // init'ly just the terminator
            yx = HandyOps.NthOffset(0, "\n. .", theList);
            if (yx > 0) { // this line at end of data blocks seeing ".StopInfo" beyond..
                aStr = HandyOps.NthItemOf(true, 1, HandyOps.Substring(yx + 1, 99, theList));
                yx = aStr.length() + yx + 2;
                theList = HandyOps.Substring(0, yx, theList);
                didit = true;
            } //~if // T: caller gets to use shortened file
            if (kx > 0) if (kx < yx) { // there is ".StopInfo" in this file, valid?
                xStr = HandyOps.RestOf(kx + 11, theList);
                for (lino = 1; lino <= 99; lino++) {
                    aStr = HandyOps.NthItemOf(true, lino, xStr);
                    if (aStr == "") break;
                    kx = HandyOps.NthOffset(0, "~", aStr);
                    if (kx < 0) continue;
                    zx = ~zx; // = -zx-1
                    if (kx < 10) break;
                    aStr = HandyOps.Substring(kx + 1, 4, aStr);
                    if (HandyOps.SafeParseInt(aStr) != lino) break;
                    zx = -zx;
                } //~for // net effect: zx++
                if (zx < 8) { // didn't see at least 8 numbered lines, so invalid..
                    System.out.println(HandyOps.Dec2Log("** Invalid StopInfo ", zx,
                            HandyOps.Dec2Log("/", kx, HandyOps.Dec2Log(" ", lino, " <??> = '"
                                    + aStr + "'\n  .... '" + xStr + "' ...."))));
                    nerrs++;
                } //~if
                else theText = xStr;
            } //~if
            AfterStop = HandyOps.Countem("\n", theText) + 2;
            theText = theText + "\n\n\n";
            // for lino=0,32 do DidCells[lino] = 0; // did it at front
            NuData = -1; // used to track image wall turns
            for (lino = 2; lino <= 999; lino++) {
                xStr = HandyOps.NthItemOf(true, lino, theList);
                xCh = HandyOps.CharAt(0, xStr);
                if (xCh == '.') break; // normal (only) exit
                switch (xCh) {
                    case '@': // if (xCh == '@') {
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr));
                        if (zx > 4) if (zx < 16) {
                            DidCells[zx]++;
                            zx = 1 << zx;
                            if ((sofar & zx) == 0) sofar = sofar + zx + 1;
                            theText = theText + "\n" + xStr;
                            nCells++;
                        } //~if
                        break;
                    case 'J': // else if (xCh == 'J') {
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr)); // seq+
                        yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, xStr)); // +frms
                        if (yx > 0) if (zx > 4) if (zx < 16) {
                            DidCells[zx] = DidCells[zx] + yx;
                            zx = 1 << zx; // word 4 is seq+
                            if ((sofar & zx) == 0) sofar = sofar + zx + 1;
                            theText = theText + "\n" + xStr;
                            nCells = nCells + yx;
                        } //~if // word 5 is number of animation frames
                        break;
                    case 'Y': // else if (xCh == 'Y') {
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, xStr));
                        if (zx > 15) { // OK if it finds too many..
                            yx = HandyOps.Countem(HandyOps.Dec2Log("=", zx, " "), theList);
                            nImgs = nImgs + yx;
                        } //~if
                        else if (zx == 0) nImgs = nImgs + 6;
                        else if (zx > 1) if (zx < 4) nImgs = nImgs + 8;
                        break;
                    case '>': // else if (xCh == '>') {
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, xStr)); // brite
                        if (zx > 0) EWx = true;
                        // nLumins++;
                        nImgs++;
                        break;
                    case '=': // else if (xCh == '=')
                        EWx = true;
                        break;
                    case 'O': // else if (xCh == 'O')
                    case 'T': // else if (xCh == 'T')
                        nImgs++;
                        break;
                    case 'M': // else if (xCh == 'M') {
                        zx = BackImage('m', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        if (TempStr == "") continue;
                        theList = TempStr;
                        TempStr = "";
                        didit = true;
                        break;
                    case 'G': // else if (xCh == 'G')
                        zx = BackImage('g', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        break;
                    case 'C': // else if (xCh == 'C') {
                        zx = BackImage('c', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        if (TempStr == "") continue;
                        theList = TempStr;
                        TempStr = "";
                        didit = true;
                        break;
                    case 'D': // else if (xCh == 'D') {
                        zx = BackImage('d', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        if (TempStr == "") continue;
                        theList = TempStr;
                        TempStr = "";
                        didit = true;
                        break;
                    case 'V': // else if (xCh == 'V') {
                        zx = BackImage('v', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        break;
                    case 'H': // else if (xCh == 'H') {
                        zx = BackImage('h', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        // nBax = nBax+2; if (nBax==0) nBax++; // gotta do this 1st pass
                        break;
                    case 'P': // else if (xCh == 'P') {
                        zx = BackImage('p', 0, 0, 0, ImgHi, ImgWi, 0, lino, xStr, theList, myMap);
                        break;
                    case 'U': // else if (xCh == 'U')
                        if (PaintIx > 0) PaintIx = PaintIx + 3;
                        break;
                    case 'K': // else if (xCh == 'K') { // "\nK_ " // shouldn't be here, delete it..
                        theList = HandyOps.RepNthLine("--" + xStr, lino, theList);
                        didit = true;
                        break;
                }
            } //~for
            rpt = sofar & 15; // + sequences
            sofar = nuIxBase; // = ArtBase+3 -> 11
            yx = nImgs * 4 + rpt + ArtBase + 1; // = offset to 1st sequence (excl hdr)
            if (nBax > 0) {
                yx = yx + 3;
                kx = yx << 16; // points to start of BG index (incl hdr)
                yx = nBax * 4 + yx;
                nBax = MyMath.iMin(nBax, 255) + kx;
            } //~if // = (->1st,+BGs) used by BackImag
            info = (((rpt * 256) + nCells) * 256 + yx) * 256 + nImgs; // (for log)
            if (nCells > 0) {
                AnimInfo = new int[16];
                // AnimInfo[5] = myMap[ArtBase(+3)] -> myMap[1stLst] -> 1st obj & posn
                // AnimInfo[6]..[15] = myMap[ArtBase(+3)+1].. -> myMap[2ndLst]..
                // all non-anim8 obj's come next, then 00, then myMap[1stLst]
                // myMap[1stLst+0] -> 1st obj, 1stPosn & time
                // myMap[1stLst+1] -> 1st obj, 2stPosn & time = 1stPosn+5
                for (lino = 5; lino <= 15; lino++) {
                    kx = DidCells[lino]; // how many steps in this object's journey
                    if (kx == 0) continue; // this sequence is unused
                    zx = (lino << 28) + yx; // = this object's anchor item
                    DidCells[lino] = zx; // -> start of this object's sequence
                    AnimInfo[lino] = yx + 3; // ditto, but including 3-int hdr
                    DidCells[lino + 16] = kx; // for log
                    yx = yx + kx + 1;
                } //~for // ordinary image specs follow last anchor item
                theText = HandyOps.RepNthLine(HandyOps.Dec2Log("\nK_ ", yx + 3,
                        HandyOps.Dec2Log(" ", nCells, HandyOps.Dec2Log(" ", rpt, ""))),
                        AfterStop, theText);
            } //~if
            nImgs = nCells * 4 + yx; // = total (image) index space
            DidCells[16] = -rpt; // visually marks middle
            aStr = "\n  ==" + HandyOps.ArrayDumpLine(DidCells, 33, 4)     // (in Mappy0)
                    + " (Imgs)\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5)
                    + "\n   -- '";
            // tall = ImDims>>16;
            // wide = MyMath.SgnExt(ImDims);
            nuIxBase++; // leave room for BG list pointer
            ImSz = tall * wide; // should be = nxt
            if (ImDims < 0x100000) ImDims = -1;
                // else if (tall<16) ImDims = 0;
            else if (wide < 16) ImDims = -2;
            else if (wide > 0xFFF) ImDims = -3;
            else if (tall > 0xFFF) ImDims = -4;
            else if (ImSz > nxt) ImDims = -5;
            else if (nImgs <= 0) ImDims = -6;
            else if (nImgs > 9999) ImDims = -6;
            else thar = nImgs + nuIxBase + GridSz + 2; // -> images
            if (thar > 0) {        // nuIxBase = ArtBase+3+1;
                if (nBax > 0) xcell = (GridSz >> 2) + xcell; // now includes BG map size
                if (PaintIx > 0) thar = xcell + PaintIx + thar; // xcell = GridSz>>3;
                here = thar + nxt + 2; // nxt = theImgs.length;
                myMap = new int[here];
                if (myMap != null) DidCells[32] = here;
                here = here - 2;
            } //~if
            if (myMap != null) {
                nuIxBase = nuIxBase + nImgs; // nImgs = img index size
                if (EWx) LuminanceMap = new int[MapAsize + 2];
            } //~if
            else if (ImDims >= 0) ImDims = -8;
            if (!Mini_Log || !NoisyMap) {
                xStr = HandyOps.NthItemOf(true, 1, theText);
                if (xStr.length() > 77) xStr = aStr + HandyOps.Substring(0, 72, xStr)
                        + "..'";
                else xStr = aStr + xStr + "'";
            } //~if
            else xStr = aStr + theText + "' ----";
            wait = nuIxBase + GridSz;
            //
            // myMap contents & where (tag) in log to see it..
            // =-= from 0 to ArtBase-1 are global parameters (ArtBase=8)
            // =-= from ArtBase to nuIxBase-2 is the (3-part) index to artifacts
            //     ..nuIxBase-1 -> background index items
            // from nuIxBase to nuIxBase+GridSz-1 is the grid map (GridSz=12800)
            // ^:^ from nuIxBase+GridSz to +BGsz=3200-1 is the image wall map
            // >>==>> from wait=nuIxBase+GridSz+BGsz to wait+PaintIx-1 is paint index
            // from wait+PaintIx to thar=wait+PaintIx+GridSz/8-1 is paint map
            // from thar=wait+PaintIx+GridSz/8 on is copy of theImgs
            //  .. (which is ignored unless you write it to a file)     // {Mappy0}
            //
            if (Qlog < 0) System.out.println(HandyOps.Int2Log("  (Images) ", ImDims,
                    HandyOps.Dec2Log(" #", nImgs, HandyOps.Dec2Log(" +", sofar,
                            HandyOps.Dec2Log(": ", tall, HandyOps.Dec2Log("/", wide,
                                    HandyOps.Dec2Log(" ", here, HandyOps.Dec2Log("/", thar,
                                            HandyOps.Dec2Log(" [", nuIxBase, HandyOps.Dec2Log(" ", wait,
                                                    HandyOps.Dec2Log(" ", PaintIx, HandyOps.Dec2Log("] ", nxt,
                                                            HandyOps.Int2Log(" ", info, xStr)))))))))))));
            xStr = "";                       // HandyOps.IffyStr(ImDims <= 0,xStr,
            // ImgWi = wide; // (init'd)
            // ImgHi = tall;
            wait = nuIxBase + GridSz + PaintIx + 2; // (includes hdr)
            yx = ImSz;
            if (ImDims > 0) { // here = myMap.length-2 = thar+theImgs.length
                for (lino = here + 1; lino >= 0; lino += -1) { // copy/reformat pixels & clear index..
                    colo = 0;
                    if (lino >= thar) if (lino < here) {
                        yx--;
                        if (yx >= 0) if (yx < theImgs.length) {
                            colo = theImgs[yx];
                            if (colo < 0) colo = -1; // transparent
                            else if (colo < 0x01000000) colo = colo << 8; // (Tiff file format)
                            else colo = -1;
                        }
                    } //~if
                    myMap[lino] = colo;
                } //~for // (copy/reformat pixels)
                for (lino = 5; lino <= 15; lino++) { // copy anchors to index..
                    zx = DidCells[lino];
                    if (zx == 0) continue;
                    if (sofar > 0) if (sofar < myMap.length) myMap[sofar] = zx;
                    sofar++;
                } //~for // (copy anchors to index)
                // xStr = " (" + yx + HandyOps.IffyStr(yx>0,"?)",")");
            } //~if // (ImDims>0)
            else { // no (or in-) valid images..
                nuIxBase = ArtBase + 4;
                sofar = nuIxBase;
                theImgs = null;
                myMap = null;
                PaintIx = 0;
                ImDims = 0;
                ImgWi = 0;
                nImgs = 0;
                ImSz = 0;
            } //~else
            rez = true;
            break;
        } //~while // (we may have managed art)
        // for lino=17,31 do DidCells[lino] = 0;
        // DidCells[32] = myMap.length;
        TempStr = "";
        DidCells[17] = PaintIx; // this is now just a temp transfer vector..
        DidCells[18] = ImDims;
        DidCells[19] = ImgWi;
        DidCells[20] = nImgs;
        DidCells[21] = ImSz;
        DidCells[22] = ImgHi;
        DidCells[23] = ImgWi;
        DidCells[24] = sofar;
        DidCells[25] = wait;
        DidCells[26] = thar;
        DidCells[27] = xcell;
        DidCells[28] = AfterStop;
        DidCells[29] = nerrs;
        DidCells[30] = aim;
        DidCells[31] = yx;
        RtnStr = theText;
        if (!rez) return null;
        if (didit) TempStr = theList;
        return myMap;
    } //~Mappy0

    /**
     * Builds a track map from a text description.
     *
     * @param theList The text description. See ReadMe for details
     * @param ImDims  The height & width of theImgs, packed as (H<<16)+W.
     * @param theImgs An array of RGB pixels containing artifact images.
     * @return The track map ready to write to a file
     */
    public static int[] BuildMap(String theList, int ImDims,
                                 int[] theImgs) { // used BildMap codes: 9:;<=>?@ABC..XYZ[^]_`
        double deep, whar, Vstp, Hstp, Vinc, Hinc;            // see: case '_':
        int lino = HandyOps.Countem("\n", theList), dont = -1, rpt, recell,
                // used errn: rpt = -1..4,9..12,14..51,55..68,70..72..80,83..102;
                // .. -93..98 frm Lumi; -111..14 frm Curv; -120..30,39..44 frm BackI
                tall, wide, rx, cx, kx = 0, yx = 0, zx = 0, nxt = 0,
                here = 0, thar = 0, colo = 0, ImgHi = 0, ImgWi = 0, ImSz = 0,
                nImgs = 0, sofar = 0, xcell = 0, AfterStop = 0, nCells = 0,
                wait = 0, PaintIx = 0, nerrs = 0, info = 0, prio = 0, prev = 0,
                tmp = 0, oops = 0, aim = 0, dimz = 0, why = 0;
        double Vat = 0.0, Hat = 0.0;
        int[] theLum;
        boolean prox = false, EWx = false, fini = false, frax = (theList != "");
        // AnimInfo[5] = myMap[ArtBase(+3)] -> myMap[1stLst] -> 1st obj & posn
        // AnimInfo[6]..[15] = myMap[ArtBase(+3)+1].. -> myMap[2ndLst]..
        // all non-anim8 obj's come next, then 00, then myMap[1stLst]
        // myMap[1stLst+0] -> 1st obj, 1stPosn & time
        // myMap[1stLst+1] -> 1st obj, 2stPosn & time = 1stPosn+5
        int[] myMap = null;
        String aStr = "", xStr = "", theText, aWord,
                aLine = HandyOps.NthItemOf(true, 1, theList);
        char xCh;
        nCrumbs = 0; // (borrow this as boolean, for log)
        if (theImgs != null) nxt = theImgs.length;
        if (MapLogged) if ((Qlog & 32) != 0) why--; // why = -1
        if (!frax) why = why - 2; // if (theList == "")
        if (lino < 3) why = why - 4; // too few lines to be real
        if (why + 1 < 0) { // if (MapLogged) if ((Qlog&32) !=0)
            System.out.println(HandyOps.Dec2Log("++(BldMap) ", lino,
                    HandyOps.Int2Log(" ", ImDims, HandyOps.Dec2Log(" ", (nxt + 999) >> 10,
                            HandyOps.TF2Log("K ", frax, " '" + aLine + "'")))));
            return null;
        } //~if
        // if (!frax) return null; // if (theList == "")
        // if (lino<3) return null; // too few lines to be real
        theList = HandyOps.ReplacAll("", "\r", theList); // cuz Windoze leaves them in
        myMap = Mappy0(theList, aLine, ImDims, theImgs);
        theText = RtnStr;
        RtnStr = "";
        if (TempStr != "") {
            theList = TempStr;
            TempStr = "";
        } //~if
        // nuIxBase = ArtBase+4; // Mappy did it // no (or in-) valid images..
        zx = DidCells[32]; // = myMap.length;
        if (zx == 0) theImgs = null;
        // else myMap = new int[zx];
        PaintIx = DidCells[17]; // this is now just a temp transfer vector..
        ImDims = DidCells[18];
        ImgWi = DidCells[19];
        nImgs = DidCells[20];
        ImSz = DidCells[21];
        ImgHi = DidCells[22];
        ImgWi = DidCells[23];
        sofar = DidCells[24];
        wait = DidCells[25];
        thar = DidCells[26];
        xcell = DidCells[27];
        AfterStop = DidCells[28];
        nerrs = DidCells[29];
        aim = DidCells[30]; // = ((pebble contrast)*256+(line width in cm))<<16
        yx = DidCells[31];
        if (yx != 0) xStr = " (" + yx + HandyOps.IffyStr(yx > 0, "?)", ")");
        else xStr = " (" + yx + HandyOps.IffyStr(yx > 0, "?)", ")"); // (ImDims>0)
        for (lino = 0; lino <= 32; lino++) DidCells[lino] = 0; // done with this
        nCells = 0;
        while (true) {
            why = 0;
            if (myMap == null) {
                why = 8;
                myMap = new int[nuIxBase + GridSz + 4];
            } //~if
            why++; // why = 1/9
            if (myMap == null) break;
            here = nuIxBase;
            nuIxBase++;
            if (here > 0) if (here < myMap.length - 1) {
                myMap[here - 1] = 0; // end of artifact list
                if (false) if (NoisyMap || MapLogged) myMap[here] = 0xDEADBEEF;
                myMap[here] = 0;
            } //~if // visual start of map in log
            dimz = HandyOps.SafeParseInt(aLine); // park dimensions in scaled meters
            why++; // why = 2/10
            if (((dimz - 0x10001) & ~0xFF00FF) != 0) break; // must <= 200x256..
            why++; // why = 3/11
            if (dimz > ParkDims) break;
            why++; // why = 4/12
            if (myMap.length < 16) break;
            why = why & 8;
            break;
        } //~while
        if (why > 0) {
            System.out.println(HandyOps.Dec2Log("(BldMap oops) = ", why,   // why =
                    HandyOps.Int2Log(" ", dimz, "\n  __ '" + aLine + "'")));
            if (why != 8) return null;
        } //~if // last early exit
        colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, aLine)); // GroundsColors
        here = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 3, aLine)); // start posn
        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, aLine));   // & aim
        if (zx > 0) if (zx < 444) aim = zx + aim; // now includes pebble contrast + line width
        if (PaintIx > 0) {
            wait = nuIxBase - 3 + GridSz + PaintIx; // (not counting hdr)
            if (nBax > 0) wait = xcell - (xcell & -xcell) + wait;
        } //~if
        if (myMap != null) if (myMap.length > 12) { // (can't fail, got it above)
            myMap[0] = 0x4C696C45;
            myMap[1] = nuIxBase + GridSz + PaintIx + xcell; // map size + hdr + artifax list
            myMap[2] = thar; // file offset to artifact images
            myMap[0 + 3] = ImDims; // artifact image size
            myMap[1 + 3] = 0; // no textures
            myMap[2 + 3] = nuIxBase - 3; // map offset => MapIxBase // nuIxBase=ArtBase+4
            myMap[3 + 3] = dimz; // park dims in scaled meters (theInx[3] in InitIn)
            myMap[4 + 3] = colo; // GroundsColors = 9ggg1ppp
            myMap[5 + 3] = here; // start posn of car
            myMap[6 + 3] = aim; // (theInx[6]) orientation + line width + pebble contrast
            myMap[ArtBase + 2] = wait;
        } //~if // =0 if no paint, else offset to paint map
        SameData = 0;
        if (nBax > 0) zx = BackImage(' ', 0, 0, 0, ImgHi, ImgWi, 0, 0, "", theList, myMap);
        if (GoodLog) if (NoisyMap) { // if (Qlog..) -- included in GL
            xStr = "=-= (BldMap)" + xStr + HandyOps.ArrayDumpLine(myMap, 22, 22) + " ";
            if (PaintIx > 0) if (wait > PaintIx + 4) aim = wait - 4 - PaintIx;
            System.out.println(xStr);
        } //~if
        // if (nLumins>0) LumiList = new int[nLumins<<2];
        theLum = LuminanceMap;
        zx = 1 << LumUniShif;
        if (theLum != null) for (yx = theLum.length - 1; yx >= 0; yx += -1)
            theLum[yx] = zx;
        recell = dimz - 0x10001;
        zx = recell & 0xFFFF;
        yx = recell >> 16;
        ShoLumiLox[0] = 0;
        LumiAdLogPt(0, 0);
        LumiAdLogPt(0, zx);
        LumiAdLogPt(yx, 0);
        LumiAdLogPt(yx, zx);
        LumiAdLogPt(yx >> 1, zx >> 1);
        NuData = -1; // used to track image wall turns
        aim = 0;
        wait = 0;
        xcell = 0;
        recell = 0;
        xCh = '\0';
        aLine = "";
        for (lino = 2; lino <= 999; lino++) {
            if (MapLogged) if (xCh != '\0') // prev'ly unlogged lines..
                // if (NoisyMap||Mini_Log) // if (Qlog..) -- included in M_L
                System.out.println(HandyOps.Dec2Log(" (Bx)     ", lino - 1,
                        " -> '" + aLine + "'"));
            colo = 0;
            here = 0;
            frax = false;
            aLine = HandyOps.NthItemOf(true, lino, theList);
            xCh = HandyOps.CharAt(0, aLine);
            if (xCh == '.') {
                try {
                    xStr = "";
                    if (!fini) {
                        zx = HandyOps.NthOffset(0, "\nK_ ", theText);
                        if (zx > 0) {
                            xStr = theText;
                            theText = HandyOps.Substring(0, zx, xStr);
                            xStr = HandyOps.RestOf(zx, xStr);
                            sofar = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, xStr));
                        } //~if
                        else fini = true;
                    } //~if
                    if (fini) {
                        aWord = "' (Imgs)\n  =-=";
                        for (zx = 5; zx <= 15; zx++) { // validate sequence lists..
                            yx = AnimInfo[zx] & 0xFFFF;
                            if (yx <= 0) continue;
                            if (yx < myMap.length) if (myMap[yx] != 0) continue;
                            aWord = HandyOps.Dec2Log("' ** Oops! ", zx, " <??> (Imgs)\n  --");
                            nerrs++;
                            break;
                        } //~for
                        aWord = aWord + HandyOps.ArrayDumpLine(myMap, nImgs + 7, 5)
                                + "\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5);
                    } //~if
                    else if (Mini_Log && NoisyMap) {
                        zx = HandyOps.Countem("\n", xStr);
                        aWord = "'\n --" + HandyOps.ArrayDumpLine(myMap, 36, 4)
                                + "\n  _:_" + HandyOps.ArrayDumpLine(AnimInfo, 0, 5)
                                + HandyOps.Dec2Log(" -- next do (", zx, ").. {") + xStr + "}";
                    } //~if
                    else aWord = "'";
                    aWord = " '" + aLine + aWord;
                    if (Qlog < 0)
                        System.out.println(HandyOps.Dec2Log(HandyOps.IffyStr(nImgs == 0, "(done) ",
                                "(done+Art) "), lino, HandyOps.TF2Log(" ", fini,
                                HandyOps.Dec2Log(" ", colo, HandyOps.Dec2Log(" ", WinWi, aWord)))));
                    if (fini) break;
                    // if (colo>0) {
                    theList = HandyOps.RepNthLine(aLine + xStr + "\n... (end)", lino, theList);
                    // theText = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
                    fini = true;
                    xCh = '\0'; // (already logged)
                } catch (Exception ex) {
                    here = -lino;
                    break;
                }
                continue;
            } //~if // (xCh == '.')
            if (xCh < '0') continue; // probly a comment
            Vat = 0.0;
            Hat = 0.0;
            aWord = HandyOps.NthItemOf(false, 2, aLine);
            aStr = HandyOps.NthItemOf(false, 3, aLine); // still valid inside sw
            if (HandyOps.NthOffset(0, ".", aWord) >= 0) frax = true;
            rx = HandyOps.SafeParseInt(aWord);
            Vat = HandyOps.SafeParseFlt(aWord);
            if (HandyOps.NthOffset(0, ".", aStr) >= 0) frax = true;
            cx = HandyOps.SafeParseInt(aStr);
            Hat = HandyOps.SafeParseFlt(aStr);
            aWord = "";
            nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 4, aLine));
            here = rx * HalfMap + cx + nuIxBase;
            recell = 0;
            // colo = 0;
            tall = 0;
            wide = 0;
            rpt = -1;
            kx = 0;
            switch (xCh) { // convert gentle curve specs to straight line segments..
                case ':': // set LookFrame for extra logging..
                    LookFrame = rx;
                    break; // case ':'
                case ';': // log luminance point..
                case '=': // set flat luminance rect..
                case '>': // add luminance light source..
                    if (!frax) { // use mid-cell if not fractional position..
                        Vat = Vat + 0.5;
                        Hat = Hat + 0.5;
                    } //~if
                    rpt = Luminator(xCh, Vat, Hat, nxt, dimz, lino, aLine, theList);
                    if (rpt == -1) theList = TempStr;
                    TempStr = "";
                    break; // case '=' // case '>'
                case '_': // set TripLine (lots more logging on one pixel line)
                    if (cx == 0) TripLine = rx;         // ..(immediate, not in file)
                    else TripLine = (cx << 16) | rx & 0xFFFF;
                    break;
                case '8': // set floor tiles (immediate, not in file)..
                    rpt = -52; // floor tile dimensions: '8 pp tt ww xx^yy
                    if (rx < 0) break; // 2^n pix/pkm, 1ppm .. 256ppm
                    if (rx > 8) break;
                    if (cx < 8) break; // 8..999 pix tall/wide..
                    if (cx > 999) break;
                    if (nxt < 8) break;
                    if (nxt > 999) break;
                    rpt--; // rpt = -53
                    xStr = HandyOps.NthItemOf(false, 5, aLine); // word 5: image r^c
                    kx = HandyOps.NthOffset(0, "^", xStr);
                    if (kx <= 0) break;
                    thar = HandyOps.SafeParseInt(xStr);
                    if (thar < 0) break;
                    if (thar + nxt > ImgWi) break;
                    kx = HandyOps.SafeParseInt(HandyOps.RestOf(kx + 1, xStr));
                    if (kx < 0) break;
                    if (kx + cx > ImgHi) break;
                    kx = kx * ImgWi + thar;
                    FloorOff = (rx << 24) + kx;
                    FloorDims = (cx << 16) + nxt;
                    if (NoisyMap || MapLogged) {
                        System.out.println(HandyOps.Dec2Log("  (floor.tile) 2^", rx,
                                HandyOps.Dec2Log(" ", cx, HandyOps.Dec2Log("/", nxt,
                                        HandyOps.Dec2Log(" @ ", kx, " __ '" + aLine + "'")))));
                        xCh = '\0'; // (already logged)
                        aLine = "";
                    } //~if
                    continue; // case '8'
                case '9': // set ceiling height (immediate, not in file)..
                    if (Vat < 1.0) break; // too low (not an error, just unshow)
                    CeilingHi = Vat;
                    ReloTabs = true;
                    break; // case '9'
                case '<': // set BackWall color (immediate, not in file)..
                    rpt = -100;
                    if (rx < 9) break;
                    if (rx > 0xFFFFFF) break;
                    BackWall = rx;
                    rpt = -1; // bad/missing numbers after BackWall are not errors..
                    if (cx < 8) break;
                    if (cx > 0xFFFFFF) break;
                    PilasterCo = cx;
                    // nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false,4,aLine));
                    if (nxt < 8) break; // nxt is logged
                    if (nxt > 0xFFFFFF) break;
                    CeilingCo = nxt;
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    if (nxt < 8) break;
                    if (nxt > 0xFFFFFF) break;
                    CreamWall = nxt;
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (nxt < 8) break;
                    if (nxt > 0xFFFFFF) break;
                    DarkWall = nxt;
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                    if (nxt < 8) break;
                    if (nxt > 0xFFFFFF) break;
                    WallCo5 = nxt;
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 8, aLine));
                    if (nxt < 8) break;
                    if (nxt > 0xFFFFFF) break;
                    WallCo6 = nxt;
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 9, aLine));
                    if (nxt < 8) break;
                    if (nxt > 0xFFFFFF) break;
                    WallCo7 = nxt;
                    break; // case '<'
                case '`': // set coefficient of friction (immediate, not in file)..
                    rpt = -102;
                    if (Vat <= 0.0) break; // too low
                    if (Vat > 2.0) break; // too high
                    SetCoefFriction(Vat);
                    rpt = -1;
                    break; // case '`'
                case '^': // set miscellaneous options (immediate, not in file)..
                    rpt = -100;
                    if (rx < 0) break;
                    if (rx > 9) break;
                    switch (rx) {
                        case 1: // toggle FixedSpeed..
                            SimSpedFixt = !SimSpedFixt;
                            break;
                        case 2: // toggle StayInTrack..
                            SimInTrak = !SimInTrak;
                            ShoTrkTstPts = SimInTrak;
                            break;
                        case 3: // turn both on(off)..
                            SimSpedFixt = !SimSpedFixt;
                            SimInTrak = !SimInTrak;
                            ShoTrkTstPts = SimInTrak;
                            break;
                        case 4: // set pebble size..
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                            nxt = rx - 3; // nxt is logged
                            PebblSize = nxt; // so it's 1..5
                            PebBlur = (1 << nxt) / 32.0;
                            break;
                        case 9: // log some park (^ 9 r c n)..
                            if (cx < 0) break;
                            if (cx >= HalfTall) break;
                            if (nxt < 0) break;
                            if (nxt >= HalfMap) break;
                            zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                            if (zx <= 0) zx = 7;
                            else zx = MyMath.iMin(zx, HalfMap - 1 - nxt);
                            nxt = cx * HalfMap + nxt + nuIxBase; // nxt is logged
                            aLine = aLine + " =";
                            for (zx = zx; zx >= 0; zx += -1) {
                                if (nxt < 0) break;
                                if (nxt >= myMap.length) break;
                                yx = myMap[nxt];
                                if (yx == 0) aStr = " n0n";
                                else if (yx < 0) aStr = HandyOps.Hex2Log(" 0x", yx, 8, "");
                                else if (yx == 0x66666666) aStr = " flr6";
                                else if (yx == 0x44444444) aStr = " trk4";
                                else if (yx <= 4) aStr = HandyOps.Dec2Log(" wal", yx, "");
                                else aStr = HandyOps.Int2Log(" ?", yx, "?");
                                aLine = aLine + aStr;
                                nxt++;
                            } //~for
                            break;
                        case 0:
                            break;
                    } //~switch
                    if (NoisyMap || MapLogged) {
                        aLine = " __ '" + aLine + "'";
                        System.out.println(HandyOps.Dec2Log("  (misc.opt) ", rx,
                                HandyOps.IffyStr(rx == 9, aLine, HandyOps.TF2Log(" Fx=", SimSpedFixt,
                                        HandyOps.TF2Log(" SiT=", SimInTrak, aLine)))));
                        xCh = '\0'; // (already logged)
                        aLine = "";
                    } //~if
                    continue; // case '^'
                case ']': // load up WhitLnColo..
                    rpt = -101;
                    if ((rx & 0xFFFFFF) != rx) break;
                    WhitLnColo = rx;
                    ValiPavColo();
                    rpt = -1;
                    break; // case '['
                case '[': // load up PebleTrak..
                    aWord = "[]: ";
                    xCh = HandyOps.CharAt(1, aLine);
                    rpt = -99; // default err..
                    if (xCh >= '0') if (xCh <= '9') { // single element, 24-bit color..
                        cx = HandyOps.SafeParseInt(HandyOps.Substring(1, 1, aLine));
                        if (cx < 0) break;
                        if (cx > 9) break;
                        GotPebz = GotPebz | (1 << cx); // LdPebTr will install missing items
                        PebleTrak[cx & 15] = rx & 0xFFFFFF;
                        rpt = -1;
                        break;
                    } //~if
                    for (cx = 0; cx <= 9; cx++) {
                        xStr = HandyOps.NthItemOf(false, cx + 2, aLine);
                        xCh = HandyOps.CharAt(0, xStr);
                        if (xStr.length() == 3) {
                            if (xCh < '0') break;
                            if (xCh > 'F') break;
                            if (xCh < 'A') if (xCh > '9') break;
                            xStr = "0x" + xStr;
                        } //~if
                        else if (xStr.length() != 5) break;
                        else if (xCh != '0') break;
                        rx = HandyOps.SafeParseInt(xStr);
                        rx = ((rx & 0xF00) << 8) + ((rx & 0xF0) << 4) + (rx & 0xF);
                        PebleTrak[cx & 15] = (rx << 4) + rx;
                        GotPebz = GotPebz | (1 << cx);
                    } //~for
                    if (GotPebz == 0x3FF) rpt = -1;
                    break; // case '['
                case 'M': // manual corner // 'M' vv hh dd, dd: 0..315 step 45  (BuildMap)
                case 'C': // (left) curved wall image spec..
                case 'D': // (right) // 'C vv hh dd rr ee-ss tt ww cc^rr pp'
                    // rpt = -1; // (no error) (unneed separate case 'M' cuz => "GM")
                    break; // case 'C'/'D'/'M' (prescan converts them to G's)
                case 'G': // background / wall image spec..
                    rpt = -121;        // 'G vv hh dd ff+aa tt ww cc^rr pp xx*'
                    if (rx < 0) break;
                    if (rx >= HalfTall) break;
                    if (cx < 0) break;
                    if (cx >= HalfMap) break;
                    while (nxt > 360) nxt = nxt - 360;
                    while (nxt < 0) nxt = nxt + 360;
                    // here = here-nuIxBase; // = rx*HalfMap+cx; // naw, nuIxBase is global
                    rpt = BackImage(xCh, rx, cx, nxt,    // used errn: -120..30,39..44
                            ImgHi, ImgWi, here, lino, aLine, theList, myMap);
                    if (InWalls) nerrs++; // mark & notify, but keep going
                    InWalls = false;
                    nxt = TmpI; // nxt is logged
                    if (rpt < -1) break;
                    aLine = "";
                    // if (xCh != 'G') theList = TempStr;
                    TempStr = "";
                    break; // case 'G'
                case 'L': // turn left (CCW, outside)
                case 'R': // turn right (C-wise, inside)
                    rpt--; // rpt = -2
                    if (prio == 0) break; // gotta have a previous endpoint
                    rpt--; // rpt = -3
                    if (rx < 0) break; // we only do curve radii 0..16
                    if (rx > 16) break;
                    if (cx > 90) break; // we only do 90 degrees or less
                    if (cx <= 0) cx = 90;
                    rpt--; // rpt = -4
                    if (prio <= 0) break; // gotta have a previous endpoint & aim
                    if (aim < 0) break;
                    if (aim > 3) break;
                    tmp = (0x40002 >> aim) & 0x10001; // (used to round up to cell edge)
                    zx = HandyOps.NthOffset(0, "-", aStr); // aStr = NthItemOf(false,3,aLine);
                    if (zx > 0) {
                        zx = HandyOps.SafeParseInt(HandyOps.RestOf(zx + 1, aStr));
                        if (zx < 0) zx = 0; // ignore invalid skip angle
                        else if (zx >= cx) zx = 0;
                    } //~if
                    else zx = 0;
                    rpt = CurvyTrak(xCh == 'L', rx, cx, zx, aim, prio, tmp, lino, aLine, theList);
                    if (rpt == -1) theList = TempStr;
                    if (Moved) aLine = ""; // CT logged it
                    Moved = false;
                    TempStr = "";
                    break; // case 'L'/'R'
                case 'A': // advance straight in same direction..
                    rpt = -9;
                    if (prio == 0) break; // gotta have a previous endpoint
                    rpt--; // rpt = -10
                    if (rx <= 0) break;
                    xCh = HandyOps.CharAt(aim, "NESW");
                    nxt = rx;
                    rx = prio >> 19;
                    cx = MyMath.SgnExt(prio) >> 3;
                    rpt--; // rpt = -11
                    if (rx <= 0) break;
                    rpt--; // rpt = -12
                    if (cx <= 0) break;
                    info = 0x341C >> (aim << 2);
                    if ((info & 3) == 0) {
                        info = info >> 2;
                        rx = ((info & 1) - (info & 2)) * nxt + rx;
                    } //~if
                    else cx = ((info & 1) - (info & 2)) * nxt + cx;
                    aWord = "" + xCh + HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log(" ", cx,
                            HandyOps.Dec2Log("   -- advance +", nxt, "")));
                    theList = HandyOps.RepNthLine(aLine + "\n" + aWord, lino, theList);
                    aWord = "[*]=> " + aWord;
                    rpt = -1;
                    break; // case 'A'
                case 'N': // northward right edge, r/c is end (end facing north)
                case 'S': // southward
                case 'E': // eastward   // rounds entering prio up to whichever face;
                case 'W': // westward   // leaves prio inside (pos'bly -1/8 from) new face
                    rpt = -14;
                    if (rx <= 0) break;
                    rpt--; // rpt = -15
                    if (cx <= 0) break;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else {
                        rx = rx << 3;
                        cx = cx << 3;
                    } //~else
                    thar = (rx << 16) | cx; // this run's endpoint..
                    rpt--; // rpt = -16
                    if (NoisyMap || MapLogged) if (Qlog < 0) {
                        if (prio == 0) aWord = "((_->";
                        else aWord = "((" + HandyOps.CharAt(aim, "NESW") + "->";
                        aWord = aWord + xCh + ")) ";
                    } //~if
                    if (((prio ^ thar) & 0x3FF83FF8) == 0) break; // but not to same cell
                    tmp = (0x40002 >> aim) & 0x10001; // (used to round up to cell edge)
                    aim = 3; // calc exit posn..
                    if (xCh == 'N') { // center-top of final cell (@ v.0)..
                        if (frax) thar = thar & 0x3FF83FFF; // (H set by caller)
                        else thar = thar + 4;             // (H @ mid-face)
                        // EWx = false;
                        aim = 0;
                    } //~if
                    else if (xCh == 'S') { // center-bottom (@ v.9)..
                        if (frax) thar = thar | 0x70000;
                        else thar = thar + 0x70004;
                        // EWx = false;
                        aim = 2;
                    } //~if
                    else if (xCh == 'E') { // right
                        if (frax) thar = thar | 7;
                        else thar = thar + 0x40007;
                        aim = 1;
                    } //~if
                    else if (xCh == 'W') { // left (aim=3)
                        if (frax) thar = thar & 0x3FFF3FF8;
                        else thar = thar + 0x40000;
                    } //~if
                    if (prio == 0) { // if no previous endpoint, then this is start..
                        if (MapLogged) if ((Qlog & 32) != 0) {
                            System.out.println(HandyOps.Dec2Log(aWord, lino,
                                    HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                                            HandyOps.Int2Log(" => ", thar, "\n    __ '" + aLine + "'")))));
                            xCh = '\0'; // (already logged)
                            aLine = "";
                        } //~if
                        aWord = "";
                        prio = thar;
                        dont = thar;
                        prev = 0;
                        rpt = -1; // not an error
                        break;
                    } //~if
                    rpt--; // rpt = -17
                    if (prio == thar) break; // end=bgn OK only if dif't facing edge
                    if ((prio & tmp) != 0) prio = prio + tmp; // round up to edge
                    kx = prio >> 16;
                    info = MyMath.SgnExt(prio);
                    rpt--; // rpt = -18
                    if (info <= 0) break;
                    rpt--; // rpt = -19
                    if (kx <= 0) break;
                    rx = thar >> 16; // end cell, now at the correct edge..
                    cx = MyMath.SgnExt(thar);
                    tall = rx - kx;
                    wide = cx - info;
                    nxt = Math.abs(wide);
                    yx = Math.abs(tall);
                    EWx = yx < nxt; // only used to determine end ro/co
                    yx = (yx + nxt) * 3; // max + cells to do
                    colo = EncoPavEdge(tall, wide, prio, 0); // use same line formula for all
                    Vat = MyMath.Fix2flt(tall, 0);
                    Hat = MyMath.Fix2flt(wide, 0);
                    whar = MyMath.aTan0(Hat, -Vat);       // aTan0(y=0,x=1)=0 <- ccw frm E
                    MyMath.Angle2cart(whar); // aim, C-wise from north
                    Hinc = MyMath.Sine * 2.0; // to advance in direction aimed, but <1 cell
                    Vinc = -MyMath.Cose * 2.0;
                    Vat = MyMath.Fix2flt(kx, 0); // 1st cell to do, in 25cm units (Gr/8)
                    Hat = MyMath.Fix2flt(info, 0); // (1st cell now = prio, skipped)
                    if (MapLogged) if (Qlog < 0) {
                        if (NoisyMap) System.out.println(HandyOps.Dec2Log(aWord, lino,
                                HandyOps.TF2Log(" ", EWx, HandyOps.Dec2Log("/", yx,
                                        HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                                                HandyOps.Dec2Log(" - (", kx, HandyOps.Dec2Log("/", info,
                                                        HandyOps.Dec2Log(") = ", tall, HandyOps.Dec2Log("/", wide,
                                                                HandyOps.Int2Log(" (", prio, HandyOps.Flt2Log(") ", whar,
                                                                        HandyOps.Flt2Log(" --> ", Vat, HandyOps.Flt2Log("/", Hat,
                                                                                HandyOps.Flt2Log(HandyOps.IffyStr(Vinc < 0, " ", " +"), Vinc,
                                                                                        HandyOps.Flt2Log("/", Hinc, HandyOps.TF2Log(" ", frax,
                                                                                                HandyOps.Int2Log("\n    __ => ", thar, " -- '" + aLine
                                                                                                        + "'"))))))))))))))))));              // aWord = "((p->n)) "
                        else System.out.println(HandyOps.Dec2Log(aWord, lino, // whar in degs..
                                HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                                        HandyOps.Dec2Log(" - (", kx, HandyOps.Dec2Log("/", info,
                                                HandyOps.Dec2Log(") = ", tall, HandyOps.Dec2Log("/", wide,
                                                        HandyOps.Flt2Log(" ", whar, HandyOps.Int2Log(" => ", thar,
                                                                "\n    __ '" + aLine + "'"))))))))));
                        aWord = "";
                        aLine = "";
                    } //~if
                    prio = thar;
                    // nCells = 0; // DidCells[0]
                    recell = 0;
                    nxt = 0; // <0 done, >0 on last row     // 'N'/'S'/'E'/'W' // (BuildMap)
                    for (zx = 0; zx <= yx; zx++) {
                        xcell = (((int) Math.round(Vat)) << 16) | ((int) Math.round(Hat)) & 0xFFFF;
                        Vat = Vat + Vinc;
                        Hat = Hat + Hinc;
                        if (((xcell ^ recell) & 0x3FF83FF8) == 0) continue;
                        here = (prio ^ recell) & 0x3FF83FF8; // =0 if now doing end cell
                        if (here == 0) nxt = -1; // do this cell then stop
                        else if (nxt > 0) break; // already did end cell in major dir'n
                        else if (EWx) {
                            if ((here & 0xFFFF) == 0) nxt++;
                        } //~if // major dim'n hit..
                        else if ((here & -0x10000) == 0) nxt++; // ..so stop unless next matches
                        rpt = 0;
                        info = 0; // =0: no kitty-corners to do
                        if (wait == 0) if (prev != 0) if (recell != 0) {
                            cx = recell - (prev & -8); // both V&H must change +/-1..
                            rx = recell - (prev & -0x80000);
                            if ((rx & -0x80000) != 0) if ((cx & 0x3FF8) != 0) {
                                rpt = recell & -0x10000 | prev & 0xFFFF;
                                info = recell & 0xFFFF | prev & -0x10000;
                            } //~if
                            else if (EWx) {
                                rx = recell & 0x70000;
                                if (rx > 0x40000) info = recell + 0x80000;
                                else if (rx < 0x40000) info = recell - 0x80000;
                            } //~if
                            else if ((recell & 4) == 0) info = recell - 8;
                            else if ((recell & 3) != 0) info = recell + 8;
                        } //~if
                        if ((recell != 0) || NoisyMap || MapLogged) for (kx = -1; kx <= 1; kx++) {
                            rx = MyMath.iMin(nCells - 1, 255);
                            if (((dont ^ recell) & 0x3FF83FF8) == 0)
                                recell = recell | 0x80000000; // don't do init cell
                            else if (recell > 0) for (rx = rx; rx >= 0; rx += -1) {
                                if (rx < 0) break;
                                if (rx > 255) continue;
                                if (((DidCells[rx] ^ recell) & 0x3FF83FF8) != 0) continue;
                                recell = recell | 0x80000000; // already did this cell
                                break;
                            } //~for // (rx)
                            rx = (recell >> 19) & 0x0FFF; // now in grid (2m) units, same as input
                            cx = MyMath.SgnExt(recell) >> 3;
                            thar = rx * HalfMap + cx + nuIxBase;
                            if (MapLogged) if (NoisyMap) if (Qlog < 0) {
                                if (aLine != "") aLine = "\n    __ '" + aLine + "'";
                                System.out.println(HandyOps.Dec2Log(
                                        HandyOps.IffyStr(recell > 0, " (++) ", " (--) "), lino,
                                        HandyOps.Dec2Log("/", zx, HandyOps.Int2Log(" ", recell & 0x7FFFFFFF,
                                                HandyOps.Dec2Log(" -> ", rx, HandyOps.Dec2Log("/", cx,
                                                        HandyOps.Hex2Log(" = x", colo, 8, HandyOps.Dec2Log(" @ ", thar,
                                                                HandyOps.Int2Log(" ", here, HandyOps.Dec2Log("/", nxt,
                                                                        HandyOps.Dec2Log(" ", wait, HandyOps.Dec2Log(" ", kx,
                                                                                HandyOps.Dec2Log(" #", nCells, HandyOps.IffyStr(info == 0, aLine,
                                                                                        HandyOps.Int2Log(" ", info, HandyOps.IffyStr(rpt == 0, aLine,
                                                                                                HandyOps.Int2Log(" / ", rpt, aLine)))))))))))))))));
                                aLine = "";
                            } //~if
                            if (recell > 0) { // add this edge cell to map..
                                if (myMap != null) if (thar > 0) if (thar < myMap.length)
                                    myMap[thar] = colo;
                                DidCells[nCells & 255] = recell; // note that we did this cell..
                                nCells++;
                                prev = recell;
                                if (nxt < 0) break;
                            } //~if
                            if (kx == 0) recell = rpt;
                            else if (kx < 0) recell = info;
                            if ((recell & -0x10000) == 0) break;
                        } //~for // (kx)
                        recell = xcell; // this is 1st hit in this next cell
                        if (nxt < 0) break;
                    } //~for // (zx)
                    dont = -1; // test init cell only once
                    if (wait > 0) wait--;
                    xCh = '\0'; // (already logged)
                    continue; // case 'N'/'S'/'E'/'W' (already logged)
                case 'U': // painted (line) spec..
                    recell = rx;
                    yx = cx;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else { // otherwise NW corner of cell..
                        rx = rx << 3;
                        cx = cx << 3;
                    } //~else
                    rpt = -83;
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -84
                    if (cx <= 0) break;
                    rpt--; // rpt = -85
                    if (nxt < 0) break; // word 4 is option bits..
                    if (nxt > 7) break; // currently only 90x rotation & hi-res
                    xStr = HandyOps.NthItemOf(false, 8, aLine);
                    thar = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    wide = 0; // word 5 points to index, word 8 is image offset..
                    tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (tall > 0)
                        wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                    else tall = 0;
                    if (thar > 0) {
                        rpt--; // rpt = -86
                        kx = thar; // kx is logged
                        if (thar < 11) break;
                        if (thar > AfterStop) break;
                        xStr = HandyOps.NthItemOf(true, thar, theText);
                        aLine = aLine + "'\n  __ + '" + xStr;
                        zx = HandyOps.NthOffset(0, HandyOps.Dec2Log("~", thar, ""), xStr);
                        rpt--; // rpt = -87
                        if (zx < 0) break; // invalid line+ in index line
                        if ((tall == 0) || (wide == 0)) {
                            tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 2, xStr));
                            wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 3, xStr));
                            if ((nxt & 1) != 0) { // flip H/V..
                                zx = tall;
                                tall = wide;
                                wide = zx;
                            }
                        } //~if
                        xStr = HandyOps.NthItemOf(false, 4, xStr);
                    } //~if
                    thar = HandyOps.SafeParseInt(xStr);
                    kx = HandyOps.NthOffset(0, "^", xStr);
                    rpt = -88;
                    if (tall <= 0) break;
                    if (wide <= 0) break;
                    rpt--; // rpt = -89
                    if (thar <= 0) break;
                    thar = (nxt << 24) + thar;
                    nxt = (nxt << 16) + kx; // nxt is logged
                    if (kx > 0) {
                        kx = HandyOps.SafeParseInt(HandyOps.RestOf(kx + 1, xStr));
                        if (kx > 0) thar = kx * ImgWi + thar;
                    } //~if
                    rpt--; // rpt = -90
                    if (PaintIx < 6) break;
                    zx = PaintIx;
                    rpt--; // rpt = -91
                    if (yx < HalfMap) {
                        PaintIx = Ad2PntMap(rx, cx, thar, tall, wide, PaintIx, myMap, GridSz >> 3);
                        rpt--; // rpt = -92
                        if (PaintIx == zx) break;
                    } //~if // something went wrong in Ad2LnMp
                    // else if (yx+wide>WinWi) break; // rpt = -91: Image Viewer off-screen
                    // else if (recell+tall>ImHi) break; // off-image..
                    else if ((tall - 1) * ImgWi + thar > ImSz) break; // also checks optn=0 (+wide)
                    else if (SeePaintTopL == 0) {
                        cx = yx;
                        rx = recell;
                        SeePaintTopL = (recell << 16) + yx;
                        SeePaintSize = (tall << 16) + wide;
                        SeePaintImgP = thar;
                    } //~if
                    else break; // rpt = -91: ignore more than one (only accept first)
                    rpt = -1;
                    aWord = "";
                    break; // case 'U'
                case 'T': // start up RealTime Base..
                    prox = false;
                    rpt = -20;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -21
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -22
                    if (cx <= 0) break;
                    rpt--; // rpt = -23
                    nxt = nxt & 15; // word 4 test bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<)
                    // word 5 is added seconds; word 6 is restart +
                    yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (yx < 0) yx = 0;
                    if (zx > 99) zx = 0;
                    if (zx > 0) aStr = HandyOps.Hex2Log(" 0x", (zx << 16) + (yx & 0xFFFF), 6,
                            " 0 0 0 0 0 4");
                    else aStr = HandyOps.Dec2Log(" ", yx, " 0 0 0 0 0 4");
                    // nxt bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<)..
                    aWord = "\nO " + HandyOps.NthItemOf(false, 2, aLine) + " ";
                    if (frax) { // allow for fractional position, but add test bits..
                        cx = (MyMath.Trunc8(Hat * 8.0) & 0xFFF) + (nxt << 12);
                        aWord = aWord + HandyOps.Fixt8th(" ", cx, aStr);
                    } //~if
                    else aWord = aWord + HandyOps.Dec2Log(" ", (nxt << 9) + cx, aStr);
                    theList = HandyOps.RepNthLine(aWord, lino, theList);
                    if (MapLogged) if (Qlog < 0) System.out.println(HandyOps.Dec2Log("  {$} ", rx,
                            HandyOps.Dec2Log("/", cx, HandyOps.Substring(0, 66,
                                    HandyOps.ReplacAll("\\", "\n",
                                            HandyOps.RepNthLine(") '", 1, aWord))) + "'")));
                    rpt = -1;
                    aWord = "";
                    break; // case 'T'
                case 'J': // macro to build animation sequence..
                    if (!fini) continue; // time-based objects at end
                    prox = HandyOps.NthOffset(0, "!", aLine) > 0;
                    rpt = -24;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -25
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -26
                    if (cx <= 0) break;
                    rpt--; // rpt = -27
                    if (nxt < 5) break;  // word 4 is sequence+ (5-15), word 5 is + steps
                    if (nxt > 15) break;
                    tmp = HandyOps.NthOffset(0, " --", aLine);
                    // if (tmp<0) tmp = HandyOps.NthOffset(0," //",aLine);
                    if (tmp > 16) {
                        xStr = HandyOps.RestOf(tmp, aLine); // preserve comment
                        aStr = HandyOps.Substring(0, tmp, aLine);
                    } //~if
                    else {
                        xStr = " --";
                        aStr = aLine;
                    } //~else
                    rpt--; // rpt = -28
                    kx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aStr));
                    if (kx <= 0) break;
                    if (kx > 99) break;   // words 6&7 are 1st & last StopInfo ln+s
                    yx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aStr));
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aStr));
                    rpt--; // rpt = -29
                    if (yx < 11) nxt = 0;
                    else if (yx > zx) nxt = 0;
                    else if (zx > AfterStop) nxt = 0;
                    if (nxt == 0) { // nxt is logged..
                        nxt = (((yx << 8) + zx) << 8) + AfterStop;
                        break;
                    } //~if
                    yx = (((zx << 10) + yx) << 10) + yx; // word 8 is aim, word 9 is step size..
                    aim = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 8, aStr));
                    whar = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 9, aStr));
                    MyMath.Angle2cart(aim); // angles are in degrees, not radians
                    Hstp = MyMath.Sine * whar;
                    Vstp = -MyMath.Cose * whar; // word 10 is start time, word 11 increment
                    rpt--; // rpt = -30
                    whar = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 10, aStr));
                    Vinc = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 11, aStr));
                    if (whar < 0.0) break; // we don't do negative time..
                    if (Vinc < 0.0) break;
                    rpt--; // rpt = -31
                    if ((whar == 0) != (Vinc == 0)) break; // end frame cannot be inc'd
                    xcell = 0;
                    if (false) {
                        xcell = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 12, aStr));
                        if (xcell + 180 < 0) xcell = 0;
                        else if (xcell >= 360) xcell = 0;
                        zx = HandyOps.NthOffset(0, "+", aStr);
                        if (zx > 0) aim = HandyOps.SafeParseInt(HandyOps.RestOf(zx + 1, aStr));
                    } //~if
                    else aim = 0;
                    Hinc = 1.0;
                    deep = 0.0;
                    aStr = HandyOps.NthItemOf(false, 12, aStr); // word 12: step thru index
                    if (aStr != "") while (true) {
                        xCh = HandyOps.CharAt(0, aStr);
                        if (xCh != '-') if ((xCh < '0') || (xCh > '9')) break;
                        zx = HandyOps.NthOffset(0, "*", aStr);
                        if (zx > 0) {
                            Hinc = HandyOps.SafeParseFlt(HandyOps.RestOf(zx + 1, aStr));
                            zx = MyMath.Trunc8(Hinc);
                            if (zx >= (yx & 0x3FF)) if (zx <= (yx >> 20)) {
                                deep = Hinc - MyMath.Fix2flt(zx, 0);
                                yx = yx & -0x400 | zx;
                            }
                        } //~if
                        Hinc = HandyOps.SafeParseFlt(aStr);
                        break;
                    } //~while
                    aWord = aLine;
                    zx = 0;
                    for (kx = kx - 1; kx >= 0; kx += -1) {
                        if (prox) aStr = "! ";
                        else aStr = " ";
                        if (aim > 0) aStr = "+" + aim + aStr;
                        aStr = HandyOps.Fixt8th("@ ", MyMath.Trunc8(Vat * 8.0 + 0.5),
                                HandyOps.Fixt8th(" ", MyMath.Trunc8(Hat * 8.0 + 0.5),
                                        HandyOps.Dec2Log(" ", nxt, HandyOps.Dec2Log(" ", yx & 0x3FF,
                                                HandyOps.Fixt8th(aStr, MyMath.Trunc8(whar * 8.0 + 0.5),
                                                        HandyOps.Dec2Log(xStr + " +", zx, ""))))));
                        aWord = aWord + "\n" + aStr;
                        deep = deep + Hinc; // Hinc could be frac'l or multi-line and/or neg
                        if (NoisyMap) if (Qlog < 0)
                            System.out.println(HandyOps.Flt2Log("  {..} ", deep, " " + aStr));
                        whar = whar + Vinc;
                        Vat = Vat + Vstp;
                        Hat = Hat + Hstp;
                        if (xcell != 0) {
                            aim = aim + xcell;
                            while (aim >= 360) aim = aim - 360;
                            while (aim < 0) aim = aim + 360;
                        } //~if
                        while (deep >= 1.0) { // calc next image ref + in yx..
                            deep = deep - 1.0;
                            yx++;
                            if ((yx & 0x3FF) > (yx >> 20)) // wrap around at end of seq
                                yx = (yx >> 10) & 0x3FF | yx & -0x400;
                        } //~while
                        while (deep <= -1.0) {
                            deep = deep + 1.0;
                            yx--;
                            if ((yx & 0x3FF) < ((yx >> 10) & 0x3FF))
                                yx = (yx >> 20) | yx & -0x400;
                        } //~while
                        zx++;
                    } //~for
                    theList = HandyOps.RepNthLine(aWord, lino, theList);
                    if (MapLogged) if (Qlog < 0) System.out.println(HandyOps.Dec2Log("  <$> ", rx,
                            HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" (", nxt,
                                    HandyOps.Substring(0, 66, HandyOps.ReplacAll("\\", "\n",
                                            HandyOps.RepNthLine(") '", 1, aWord))) + "'"))));
                    aim = 0; // now invalid
                    prio = 0;
                    rpt = -1;
                    aWord = "";
                    break; // case 'J' (animation)
                case '@': // specify coords at (frame) time for (animated) artifact
                case 'Y': // macro to insert artifact from standard descriptor
                    if ((xCh == '@') != fini) continue; // time-based objects at end
                    prox = HandyOps.NthOffset(0, "!", aLine) > 0;
                    rpt = -32;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -33
                    if (rx <= 0) break; // words 2&3 are V&H coords, in 2m grid units
                    rpt--; // rpt = -34
                    if (cx <= 0) break;
                    rpt--; // rpt = -35
                    if (AfterStop < 12) break;
                    rpt--; // rpt = -36
                    kx = nxt;            // word 4 is type/sequence + (0-15)
                    if (kx < 0) break;
                    if (kx == 4) break; // use 'T' to start timer
                    if (kx > AfterStop) break;
                    EWx = (kx < 5) || (kx > 15);
                    if (EWx == fini) break; // all&only +5-15 are time-based
                    rpt--; // rpt = -37
                    nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    if (nxt < 0) break;  // word 5 is facing, c-wise degrees from north
                    rpt--; // rpt = -38  // ..or else line+ in StopInfo => (items +4-9)
                    if ((kx < 4) || (kx > 15)) {
                        if (nxt > 359) break;
                        if (nxt < 180) rpt = nxt + 180; // back side
                        else rpt = nxt - 180;
                        if (nxt < 270) yx = nxt + 90; // S-edge side
                        else yx = nxt - 270;
                    } //~if    // (preserve fractional coordinates)..
                    aWord = "\nO " + HandyOps.NthItemOf(false, 2, aLine) + " "
                            + HandyOps.NthItemOf(false, 3, aLine) + " ";
                    // aWord = "\n" + HandyOps.Dec2Log("O ",rx,HandyOps.Dec2Log(" ",cx," "));
                    if (kx == 0) aWord = aLine // stop sign..
                            + HandyOps.Dec2Log(aWord, nxt, HandyOps.NthItemOf(true, 1, theText))
                            + HandyOps.Dec2Log(aWord, rpt, HandyOps.NthItemOf(true, 2, theText))
                            + HandyOps.Dec2Log(aWord, nxt, HandyOps.NthItemOf(true, 3, theText))
                            + HandyOps.Dec2Log(aWord, rpt, HandyOps.NthItemOf(true, 4, theText))
                            + HandyOps.Dec2Log(aWord, yx, HandyOps.NthItemOf(true, 5, theText))
                            + aWord + "0 " + HandyOps.NthItemOf(true, 6, theText);
                    else if (kx > 15) { // use StopInfo line +kx++ for items +5-9..
                        rpt = -39;
                        xStr = HandyOps.NthItemOf(true, kx, theText);
                        if (xStr == "") break; // no such line
                        if (!prox) if (HandyOps.NthOffset(0, "!", aLine) > 0) prox = true;
                        aStr = HandyOps.NthItemOf(false, 5, xStr);
                        zx = HandyOps.SafeParseInt(aStr); // => +9, ppm must be >0, <256..
                        rpt--; // rpt = -40
                        if (zx <= 0) break;
                        if (zx > 255) break;
                        if (HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, xStr)) != 0)
                            break; // rpt = -40 // => +10, anim+ must be 0
                        if (prox) aStr = "! ";
                        else aStr = " ";
                        xStr = aLine + HandyOps.Dec2Log(aWord, nxt, aStr) + xStr;
                        for (zx = 1; zx <= 99; zx++) { // add all linked lines..
                            aStr = HandyOps.NthItemOf(true, kx + zx, theText);
                            yx = HandyOps.NthOffset(0, "=", aStr);
                            if (yx <= 0) break;
                            if (HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aStr)) != kx) break;
                            if (!prox) if (HandyOps.NthOffset(0, "!", aLine) > 0) prox = true;
                            yx = HandyOps.NthOffset(0, "+", aStr);
                            if (yx > 0) { // got a view angle offset..
                                yx = HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aStr));
                                if (yx <= 0) break;
                                if (yx >= 360) break;
                                yx = yx + nxt;
                                while (yx >= 360) yx = yx - 360;
                            } //~if
                            else yx = nxt;
                            xStr = xStr + HandyOps.Dec2Log(aWord, yx, " ") + aStr;
                        } //~for
                        aWord = xStr;
                    } //~if
                    else if (kx > 4) { // gotta insert time slot for '@' into seq list..
                        rpt = -41;
                        if (nxt < 11) break; // +5 is line+ in StopInfo => (items +5-9)
                        rpt--; // rpt = -42
                        if (nxt + 2 > AfterStop) break;
                        xStr = HandyOps.NthItemOf(true, nxt, theText);
                        if (xStr == "") break;
                        zx = HandyOps.NthOffset(0, HandyOps.Dec2Log("~", nxt, ""), xStr);
                        if (zx < 0) break; // rpt = -42 (invalid line+ in index line)
                        if (!prox) if (HandyOps.NthOffset(0, "!", aLine) > 0) prox = true;
                        yx = HandyOps.NthOffset(0, "+", aLine);
                        if (yx <= 0) yx = 0;
                        else if (yx < zx) { // got a view angle (doesn't work)..
                            yx = HandyOps.SafeParseInt(HandyOps.RestOf(yx + 1, aLine));
                            if (yx < 0) yx = 0;
                            else if (yx >= 360) yx = 0;
                        } //~if
                        else yx = 0;
                        if (prox) aStr = "0! ";
                        else aStr = "0 ";
                        aWord = aWord + aStr + xStr; // HandyOps.Dec2Log(aWord,yx," ")
                        nxt = AnimInfo[kx & 15]; // -> this obj's list (++ im's) in myMap
                        zx = MyMath.SgnExt(nxt); // (nxt is logged)
                        rpt--; // rpt = -43
                        if (zx <= 0) break;
                        yx = (nxt >> 16) + zx; // -> current item in seq list, => +11
                        zx = nxt + 0x10000; // (update AnimInfo after last error exit)
                        nxt = HandyOps.NthOffset(0, " --", aLine);
                        if (nxt < 0) nxt = HandyOps.NthOffset(0, " //", aLine);
                        rpt--; // rpt = -44
                        aStr = "";
                        if (nxt >= 0) {
                            if (nxt < 13) break;
                            aStr = HandyOps.RestOf(nxt, aLine); // preserve any comment for log
                            xStr = HandyOps.Substring(0, nxt + 1, aLine);
                        } //~if
                        else xStr = aLine;
                        // nxt = HandyOps.SafeParseInt(HandyOps.NthItemOf(false,6,xStr));
                        nxt = HandyOps.NthOffset(0, " --", aWord);
                        if (nxt < 0) nxt = HandyOps.NthOffset(0, " //", aWord);
                        rpt--; // rpt = -45
                        if (nxt >= 0) {
                            if (nxt < 18) break;
                            if (aStr == "") aStr = HandyOps.RestOf(nxt, aWord);
                            aWord = HandyOps.Substring(0, nxt + 1, aWord);
                        } //~if
                        xStr = HandyOps.NthItemOf(false, 6, xStr); // +6 is exp.time => +12
                        rpt--; // rpt = -46
                        if (xStr == "") break;
                        AnimInfo[kx & 15] = zx;
                        aWord = aLine + HandyOps.Dec2Log(aWord, kx,
                                HandyOps.Dec2Log(" ", yx, " ")) + xStr + aStr;
                    } //~if
                    else if ((kx & -2) == 2) { // traffic light (animated, see Animatro)..
                        xStr = HandyOps.NthItemOf(true, 7, theText); // dark traffic lite..
                        zx = HandyOps.NthOffset(0, " --", xStr); // remove comment, if any..
                        // if (zx<0) zx = HandyOps.NthOffset(0," //",xStr);
                        if (zx >= 0) xStr = HandyOps.Substring(0, zx + 1, xStr);
                        zx = HandyOps.NthOffset(0, " 0 ", xStr);
                        if (zx >= 0) if (zx < 8) xStr = HandyOps.RestOf(zx + 2, xStr);
                        if (nxt < 270) yx = nxt + 90; // left side
                        else yx = nxt - 270;
                        if (nxt < 90) zx = nxt + 270; // right side
                        else zx = nxt - 90;
                        aStr = " 80" + xStr;
                        aWord = aLine
                                + HandyOps.Dec2Log(aWord, nxt, HandyOps.Dec2Log(aStr, kx, " `1"))
                                + HandyOps.Dec2Log(aWord, rpt, HandyOps.Dec2Log(aStr, kx, " `2"))
                                + HandyOps.Dec2Log(aWord, yx, HandyOps.Dec2Log(aStr, 5 - kx, " `3"))
                                + HandyOps.Dec2Log(aWord, zx, HandyOps.Dec2Log(aStr, 5 - kx, " `4"))
                                + aWord + " 0 0" + xStr + "0 `5"
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 8, theText)
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 9, theText)
                                + aWord + " 0 " + HandyOps.NthItemOf(true, 10, theText);
                    } //~if
                    else kx = -1;
                    if (kx >= 0) {
                        theList = HandyOps.RepNthLine(aWord, lino, theList);
                        if (MapLogged) if (Qlog < 0) System.out.println(HandyOps.Dec2Log("  [$] ", rx,
                                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" (", kx,
                                        HandyOps.Substring(0, 66, HandyOps.ReplacAll("\\", "\n",
                                                HandyOps.RepNthLine(") '", 1, aWord))) + "'"))));
                        rpt = -1;
                    } //~if
                    else rpt = -47;
                    aWord = "";
                    break; // case '@'/'Y'
                case 'K': // ("\nK_ ") do back end of mobile artifact list..
                    rpt = -48;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -49
                    if (rx <= 0) break;
                    rpt--; // rpt = -50
                    sofar = rx; // = resume object specs here
                    if (cx <= 0) break; // = + img-times (nCells)
                    rpt--; // rpt = -51
                    if (nxt <= 0) break; // = + sequences
                    rpt = -1;
                    break; // case 'K'
                case 'O': // artifact info (see ShoArtif) (not postponed)..
                    prox = HandyOps.NthOffset(0, "!", aLine) > 0;
                    rpt = -55;
                    if (ImDims == 0) break;
                    rpt--; // rpt = -56
                    if (rx <= 0) break;
                    rpt--; // rpt = -57
                    if (cx <= 0) break;
                    rpt--; // rpt = -58
                    if (nxt < 0) break;
                    rpt--; // rpt = -59               // +10: anim'n seq +
                    kx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 10, aLine));
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine));
                    if ((nxt & 512) != 0) { //  could be (lit) luminary..
                        if (zx >= 0) break;
                        if (kx != 1) break;
                        nxt = nxt & 511;
                    } //~if
                    if (nxt > 360) break; // +4: view angle, 0= centered facing north
                    // xcell = 0;
                    // if (kx==4) if (nxt==1) xcell = 0x800;
                    // nxt = nxt<<4;
                    if (frax) { // allow for fractional position..
                        rx = MyMath.Trunc8(Vat * 8.0);
                        cx = MyMath.Trunc8(Hat * 8.0);
                    } //~if
                    else { // otherwise centered in cell..
                        rx = (rx << 3) + 4;
                        cx = (cx << 3) + 4;
                    } //~else
                    if (kx != 4) { // not: (kx=4: start up RealTime Base)
                        if (zx == 0) nxt = 0; // no range = no aim
                        else if (zx < 0) { // probly luminary..
                            if (kx == 1) nxt = (zx << 16) + nxt; // negative height
                            else nxt = 0;
                        } //~if
                        else if (zx <= 360) nxt = (zx << 16) + nxt; // +5: range of view, 0=full
                        else nxt = 0;
                    } //~if
                    tall = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    wide = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 7, aLine));
                    zx = 0;                   // +6,7: height & width in pixels
                    aStr = HandyOps.NthItemOf(false, 8, aLine); // +8: image offset
                    yx = HandyOps.NthOffset(0, "^", aStr);
                    here = HandyOps.SafeParseInt(aStr);
                    rpt--; // rpt = -60
                    if (yx > 0) if (theImgs != null) {
                        xStr = HandyOps.RestOf(yx + 1, aStr);
                        yx = nxt;
                        nxt = HandyOps.SafeParseInt(xStr); // nxt is logged
                        if (nxt <= 0) break; // rpt = -60
                        if (nxt >= ImgHi) break;
                        nxt = nxt * ImgWi + here;
                        if (nxt > here) if (nxt < theImgs.length) here = nxt;
                        nxt = yx;
                    } //~if // (restore prior)
                    yx = 0;
                    rpt--; // rpt = -61
                    if (kx != 4) {              // +10: (kx) anim'n seq +, +9: ppm
                        if (tall <= 0) break;
                        rpt--; // rpt = -62
                        if (wide <= 0) break;
                        yx = wide >> 1;
                        rpt--; // rpt = -63
                        if (here <= 0) break;
                        here = (tall - 1) * ImgWi + yx + here; // now -> bottom middle
                        rpt--; // rpt = -64
                        if (here + yx >= ImSz) break;
                        zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 9, aLine));
                        rpt--; // rpt = -65
                        if (zx <= 0) break;
                        rpt--; // rpt = -66
                        if (zx > 255) break;
                        zx = (zx << 24) + here;
                    } //~if
                    else here = 0;
                    rpt = -67;
                    if (myMap == null) break;
                    if (kx < 0) kx = 0;
                    else if (kx > 15) kx = 0;    // +11: seq list ptr, +12: exp'n time..
                    else if (kx > 4) {
                        thar = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 11, aLine));
                        if (thar > 0) if (thar < myMap.length) {
                            Vat = HandyOps.SafeParseFlt(HandyOps.NthItemOf(false, 12, aLine));
                            tmp = MyMath.Trunc8(Vat * 8.0);
                            myMap[thar] = ((sofar - 3) << 16) + (tmp & 0xFFFF);
                        } //~if // (in case 'O')
                        nxt = 0;
                    } //~if
                    yx = (tall << 16) + 65536 - yx;
                    xcell = (((kx << 12) + rx) << 16) + cx; // +xcell
                    if (NoisyMap || MapLogged) if (Qlog != 0) {
                        if (aLine != "") aLine = "\n    '" + aLine + "'";
                        // if (aLine.length()<30) aLine = " '" + aLine + "'";
                        //   else aLine = "\n    '" + aLine + "'";} //~if
                        if (kx > 4) aLine = " t=" + aStr + aLine;
                        System.out.println(HandyOps.Dec2Log("(Art) ", lino,
                                HandyOps.Dec2Log("/", sofar, HandyOps.Dec2Log(" < ", nImgs,
                                        HandyOps.Dec2Log(" -> ", rx, HandyOps.Dec2Log("/", cx,
                                                HandyOps.Dec2Log(" @ ", tall, HandyOps.Dec2Log("/", wide,
                                                        HandyOps.Dec2Log(" ", kx, HandyOps.Int2Log(" [", xcell,
                                                                HandyOps.Int2Log(" ", nxt, HandyOps.Int2Log(" ", zx,
                                                                        HandyOps.Int2Log(" ", yx, HandyOps.Dec2Log("] -> ", here,
                                                                                HandyOps.Hex2Log("/x", here, 5, aLine)))))))))))))));
                        aLine = "";
                    } //~if
                    rpt--; // rpt = -68
                    if (sofar >= nImgs) break;
                    if (sofar < 0) break;
                    if (sofar > myMap.length - 5) break;
                    if (prox) if (nxt >= 0) nxt = nxt | 0x8000;
                    myMap[sofar] = xcell; // +0: map coords in pk meters *4 (25cm) +anim
                    myMap[sofar + 1] = nxt; // +1: range of view, view angle (or TimeBase+s)
                    if (kx != 4) {
                        myMap[sofar + 2] = zx;  // +2: ppm/pix offset
                        myMap[sofar + 3] = yx;  // +3: height and width
                        sofar = sofar + 4;
                    } //~if
                    else sofar = sofar + 2; // (start RealTime Base needs only +2 words)
                    xCh = '\0'; // (already logged)
                    continue; // case 'O'
                case 'I': // initialize as no prior
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    rpt = -1;
                    break; // case 'I'
                case 'X': // erase overstrike memory
                    dont = prio; // ..but don't overstrike endpoint
                    if (rx > 0) wait = rx; // disable kitty-corner fill (wait) advances
                    else wait = 0;
                    if (wait == 0) prev = 0; // enable kitty-corner fill close to prev end
                    nCells = 0;
                    break; // case 'X'
                case 'B': // bucket fill
                    rpt = -72;
                    if (rx <= 0) break;
                    rpt--; // rpt = -73
                    if (cx <= 0) break;
                    if (nxt == 0) colo = 0x66666666; // 0 -> floor
                    else colo = 0x44444444;      // 1 -> track
                    if (Log_Draw || NoisyMap || MapLogged) if (Qlog < 0) {
                        TempStr = "(Bkt) '" + aLine + "'";
                        TmpI = TempStr.length();
                    } //~if
                    MapBucket(0, rx, cx, colo, myMap);
                    if (Log_Draw || NoisyMap || MapLogged) if (Qlog < 0) {
                        System.out.println(TempStr);
                        TempStr = "";
                    } //~if
                    dont = -1;
                    aLine = "";
                    nCells = 0;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    rpt = -1;
                    break; // case 'B'
                case 'F': // rect fill w/grass (2x2m grid coords) or track
                    rpt = -74;
                    if (rx <= 0) break;
                    rpt--; // rpt = -75
                    if (cx <= 0) break;
                    nxt = nxt - rx;
                    info = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine)) - cx;
                    zx = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 6, aLine));
                    if (zx == 0) colo = 0x66666666; // 0 -> floor
                    else if (zx < 0) colo = 0;      // -1 -> virgin (non-park)
                    else colo = 0x44444444;     // 1 -> track
                    for (nxt = nxt; nxt >= 0; nxt += -1) {
                        thar = here;
                        here = here + HalfMap;
                        for (yx = info; yx >= 0; yx += -1) {
                            if (myMap == null) break;
                            if (thar < 0) break;
                            if (thar < myMap.length) myMap[thar] = colo;
                            thar++;
                        }
                    } //~for // (yx)
                    nCells = 0;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    rpt = -1;
                    break; // case 'F'/'T'
                case 'P': // pilaster (like 'V')
                    rpt = -70;
                    if (rx <= 0) break;
                    rpt--; // rpt = -71
                    if (cx <= 0) break;
                    zx = 4 * 1024; // -> PilasterCo in WallColoz
                    yx = BackImage(xCh, rx, cx, zx, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap);
                    yx = BackImage(xCh, rx, cx, zx + 90, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap);
                    zx = zx + 180 + 0x8000;
                    yx = BackImage(xCh, rx, cx, zx, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap);
                    yx = BackImage(xCh, rx, cx, zx + 90, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap);
                    colo = 0x77777777;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    rpt = 0; // rpt = -1;
                    break; // case 'P'
                case 'V': // (plain) N-S wall
                case 'H': // E-W
                    rpt = -76;
                    if (rx < 0) break;
                    rpt--; // rpt = -77
                    if (cx < 0) break;
                    colo = HandyOps.SafeParseInt(HandyOps.NthItemOf(false, 5, aLine)) & 7;
                    rpt--; // rpt = -78
                    if (colo == 0) break;
                    // if (nBax==0) nBax++; // gotta do it 1st pass
                    zx = 0; // if (xCh == 'V')
                    if (xCh == 'H') zx = 90;
                    zx = (((nxt << 6) + colo) << 10) + zx;
                    yx = BackImage(xCh, rx, cx, zx, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap); // +1==0) nBax++;
                    zx = zx + 180 + 0x8000; // do back side, if useful..
                    yx = BackImage(xCh, rx, cx, zx, ImgHi, ImgWi, here,
                            lino, aLine, theList, myMap);
                    rpt = nxt; // (not: rpt = -1;)
                    nxt = 1;
                    if (xCh == 'V') {
                        nxt = HalfMap;
                        rpt = rpt - rx;
                    } //~if
                    else rpt = rpt - cx;
                    if (rpt < 0) {
                        rpt = -79;
                        break;
                    } //~if
                    if (false) { // double-thick no longer supported..
                        if (colo < 4) break; // single thinkness, go do it
                        colo = colo & 3;
                        recell = nxt;
                    } //~if
                    nCells = 0;
                    dont = -1;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    break; // case 'V'/'H'
                case 'Q': // convert track edge to grass (no border yet)
                    rpt = -80;
                    if (myMap == null) break;
                    thar = myMap.length - 1;
                    for (thar = thar; thar >= nuIxBase; thar += -1)
                        if (myMap != null) if (thar > 0)
                            if (thar < myMap.length) {
                                nxt = myMap[thar];
                                if (nxt >= 0) if (nxt != 0x44444444) continue;
                                myMap[thar] = 0x66666666;
                            } //~if
                    dont = -1;
                    wait = 0;
                    prev = 0;
                    prio = 0;
                    aim = 0;
                    rpt = -1;
                    break; // case 'Q'
                default:
                    rpt = -1;
                    nxt = 0;
                    break;
            } //~switch
            if (MapLogged) { // if (Qlog..) -- included in M_L
                if (aLine != "") aLine = " '" + aLine + "'";
                else if (((rpt + 1) | rx | cx | nxt) == 0) continue;
                if (aLine.length() > 24) aLine = "\n  __ " + aLine;
                if (aWord != "") {
                    if (HandyOps.NthOffset(0, "[*]=>", aWord) < 0)
                        aLine = aLine + "\n  + + '" + aWord + "'";
                    else aLine = aLine + "\n" + aWord;
                } //~if
                if (rpt < 0) {
                    if (rpt + 1 < 0) { // see 'used errn:' above
                        aLine = ") <??>" + aLine;
                        nerrs++;
                    } //~if
                    else aLine = ")" + aLine;
                } //~if
                xCh = HandyOps.CharAt(aim, "NESW");
                aWord = HandyOps.IffyStr(nImgs == 0, "(BildMap)", "(BildArt)")
                        + HandyOps.Dec2Log(" ", lino, HandyOps.Dec2Log(" -> ", rx,
                        HandyOps.Dec2Log("/", cx, HandyOps.Int2Log(" => ", (prio >> 3) & 0xFF00FF,
                                HandyOps.Dec2Log(" ", aim,
                                        HandyOps.Dec2Log(" ", kx, HandyOps.Dec2Log(" ", nImgs,
                                                HandyOps.IffyStr(nImgs == 0, " (",
                                                        HandyOps.Dec2Log("/", sofar, " (")))))))));
                System.out.println(HandyOps.Int2Log(aWord, prio, HandyOps.Int2Log(" +", nxt,
                        HandyOps.IffyStr(rpt == -1, aLine, HandyOps.Dec2Log(" *", rpt,
                                HandyOps.IffyStr(rpt < 0, aLine, HandyOps.Dec2Log(") @ ", here,
                                        HandyOps.Hex2Log(" = x", colo, 8, aLine))))))));
                aWord = "";
            } //~if
            xCh = '\0'; // (already logged)
            for (rpt = rpt; rpt >= 0; rpt += -1) {
                if (here < 0) break;
                if (here >= myMap.length) break;
                myMap[here] = colo;
                if (colo == 0) break;
                if (false) if (recell != 0) if (here < myMap.length - (HalfMap + 2)) {
                    myMap[here + 1] = colo; // double-thick no longer supported
                    myMap[here + HalfMap] = colo;
                    myMap[here + HalfMap + 1] = colo;
                } //~if
                here = here + nxt;
            } //~for // (rpt)
            here = 0;
        } //~for // (lino)
        prio = nuIxBase;
        here = 0;
        nxt = 0;
        if (myMap != null) { // stub in unused paint items..
            if (myMap.length > ArtBase + 3) here = myMap[ArtBase + 2] + 2; // needs +3..
            if (here > 8) if (PaintIx > 0) if (here < myMap.length) if (myMap[here] == 0)
                for (thar = here; thar >= here + 6 - PaintIx; thar += -3) {         // shouldn't happen
                    if (myMap[thar] != 0) break;
                    myMap[thar] = 0x80000000;
                    nxt--;
                }
        } //~if
        if (MapLogged) if ((Qlog & 256) != 0) {
            LumiLogg();
            aLine = "";
            if (here > PaintIx + 12) {
                aLine = "+3";
                thar = here + 3 - 12 - PaintIx;
            } //~if
            else thar = 0;
            if (myMap != null) {
                aLine = aLine + "  =-=" + HandyOps.ArrayDumpLine(myMap, 57, 32);
                if (false) if (prio > 0) if (prio < myMap.length - 1)
                    if (myMap[prio] == 0xDEADBEEF) myMap[prio] = 0;
            } //~if
            System.out.println(HandyOps.Dec2Log("--(BldMap) ", lino,
                    HandyOps.Dec2Log(" ", PaintIx, HandyOps.Dec2Log(" ", AfterStop,
                            HandyOps.Dec2Log(" ", nxt, HandyOps.Dec2Log(" ", here, aLine))))));
        } //~if
        NuData = 0;
        nCrumbs = 0;
        return myMap;
    } //~BuildMap

    /**
     * Returns an angle (in non-negative degrees) halfway between here and thar.
     * Corrects for large and negative angles. If here and thar are 180 degrees
     * out of phase, returns the lowest non-negative result.
     *
     * @param here First angle in dregrees
     * @param thar Second angle in dregrees
     * @return The angle of a ray bisecting the acute vertext
     */
    public static double MidAngle(double here, double thar) {
        double aim;
        while (here < 0.0) here = here + 360.0; // normalize them..
        while (here > 360.0) here = here - 360.0;
        while (thar < 0.0) thar = thar + 360.0;
        while (thar > 360.0) thar = thar - 360.0;
        if (here > thar) {
            aim = thar;
            thar = here;
            here = aim;
        } //~if
        aim = (thar + here) * 0.5;
        if (here + 180.0 < thar) {
            if (aim < 180.0) aim = aim + 180.0;
            else aim = aim - 180.0;
        } //~if
        return aim;
    } //~MidAngle

    /**
     * Gets a single digit 0..9 from a 1K array of random digits.
     *
     * @param whom Which digit to get (mod 1K).
     * @return The pseudo-random digit from that array index
     */
    public int GetPiDigit(int whom) {
        int nx = (whom & 7) << 2;
        whom = whom >> 3;
        whom = PiDigits[whom & 127] >> nx;
        return whom & 15;
    } //~GetPiDigit

    /**
     * Gets a reference to 1K array of random digits, packed 8/int.
     *
     * @return The PiDigits array reference
     */
    public int[] GetPiDigs() {
        return PiDigits;
    } //~GetPiDigs

    /**
     * Gets a reference to the current image TrakSim is drawing on.
     * <p>
     * TrakSim maintains its own integer array to be used as a screen buffer,
     * but a client can "borrow" TrakSim's drawing tools by saving the screen
     * buffer, then restoring it when done.
     *
     * @return The current screen buffer
     */
    public int[] GetMyScreenAry() {
        return myScreen;
    } // so to trade out multiple..

    /**
     * Gets the size of the current image TrakSim is drawing on.
     * <p>
     * TrakSim maintains its own integer array to be used as a screen buffer,
     * but a client can "borrow" TrakSim's drawing tools by saving the screen
     * buffer, then restoring it when done.
     *
     * @return The height and width of screen buffer packed into an integer
     */
    public int GetMyScreenDims() {
        return (SceneTall << 16) + SceneWide;
    }

    /**
     * Gets the current frame count. TrakSim increases this number each time
     * some data changes and it is redrawn. Screen buffers requested between
     * changes do not increase the frame count.
     *
     * @return The current frame count
     */
    public int GetFrameNo() {
        return FrameNo;
    }

    /**
     * Gets the width of the image file (if loaded), or else =0. Use this
     * to convert pixel row & column into position for SeeOnScrnPaint
     *
     * @return The image width
     */
    public int GetImgWide() {
        return ImageWide;
    }

    /**
     * Gets the inverse of the current focal length.
     * = Dzoom, approx = 1/4 for 100mm lens = 1 for 25mm
     *
     * @return = Dzoom
     */
    public double DenomZoom() {
        return Dzoom;
    }

    /**
     * Gets the output of proximity detector.
     *
     * @return true if marked artifact is closer than ProxThresh
     */
    public boolean IsItProxim() {
        return IsProxim;
    }

    /**
     * Converts a coordinate position (in park meters) to screen pixel.
     * If not visible from the car, gives an indication of why not.
     * <p>
     * This uses (class variable) VuEdge, which is the angle of the raster
     * right end compared to straight-on (as set by BuildFrame, shown on
     * close-up map as diagonal blue lines) so it returns 0 before first
     * time the screen has been drawn.
     * <p>
     * This also returns in (class variable) TmpI the screen pixel resolution
     * in pixels*256/meter at this distance, and if pie>0, the actual compass
     * angle from this point to the car in (class variable) WallAim.
     * <p>
     * TrakSim uses this method for finding pixels along the front side of
     * a wall segment, and it is assumed that this wall segment crosses the
     * center of view (perhaps only by extension) at some angle. The calcs
     * are designed so that the wall surface appears as a straight line on
     * the screen. If we just figured it on the distance alone, the objects
     * would look like it was photographed with a fish-eye lens. The trig
     * is explained in www.ittybittycomputers.com/APW2/TrackSim/Map2scrn.htm
     *
     * @param Vat   The vertical coordinate in park meters from north edge
     * @param Hat   The horizontal coordinate in park meters from west edge
     * @param engle The relative orientation angle (of this image wall),
     *              which is used to normalize the screen resolution result.
     *              Use 0.0 for objects considered square-on, >0 degrees
     *              (clockwise) for objects where the right side is closer.
     * @param pie   If >0, returns also compass angle to car in WallAim.
     * @param clok  True: allow coordinates off-screen in front
     * @param nuly  True: force recalculation of the center-view distance
     *              cached in (class variable) CashDx. Use false for finding
     *              additional screen coordinates of the same object.
     * @param logy  >0: enables diagnostic logging
     * @return The r,c screen coordinate packed into an int = (r<<19)+c
     * (zero-based at horizon, ImHaf not included),
     * or -1 (behind car), -2 (off to left), -3 (off to right),
     * -4 (in front, but too close to see), or else =0
     * (invalid coordinate, or screen uninitialized).
     * <p>
     * (return)     (class variable) TmpI = screen pixel resolution
     * in pixels*256/meter at this distance.
     * <p>
     * (return)     (class variable) WallAim = actual compass angle from
     * this point to the car (if pie>0).
     */
    public int Map2screen(double Vat, double Hat, double engle, int pie,
                          boolean clok, boolean nuly, int logy) {   // fisheye lens probably fail
        // engle is deviation from normal to wall where Facing crosses wall face,
        // .. engle = (ix1&0x1FF)-90.0-Facing; =90 if wall || Facing
        boolean keep = false;
        int kx = 0, nx = 0, zx = 0, rx = 0, cx = 0, dx = 0, ppm = 0, why = 0;
        double sinA = 0.0, sinB = 0.0, xtmp = 0.0, ftmp = 0.0, fdst = 0.0,
                Vx = Vat - Vposn, Hx = Hat - Hposn, aim = MyMath.aTan0(Hx, -Vx) - Facing;
        // fdst=Vx,Hx: Vat,Hat position relative to car (in park meters)
        // aim is deviation in degrees from center-view (=Facing), -90<aim<90
        TmpI = 0; // = scr.px/m
        if (nuly) WallAim = 0.0;
        else if (pie > 0) WallAim = 0.0;
        else keep = true;
        if (Mini_Log) if (logy > 0) TempStr = "\n (... =";
        while (true) {
            why++; // why = 1
            // VuEdge is angle of raster right end, compared to straight-on
            if (VuEdge == 0.0) break; // can't do anything until after calc'd
            why++; // why = 2
            if (Vat < 0.0) break; // invalid coordinate..
            if (Hat < 0.0) break;
            if (Vat >= fMapTall) break;
            if (Hat >= fMapWide) break;
            // aim is angle of deviation from center-view
            while (aim > 180.0) aim = aim - 360.0;
            while (aim + 180.0 < 0.0) aim = aim + 360.0;
            why++; // why = 3
            dx--; // = -1
            if (aim >= 90.0) break; // behind car
            if (aim + 90.0 <= 0.0) break;
            dx--; // = -2
            if (aim + VuEdge <= 0.0) break; // off-screen to left
            dx--; // = -3
            if (aim > VuEdge) break; // off-screen to right
            why++; // why = 4 // OK, commit.. (gotta also cache center in Vsee, Hsee)
            if (!keep) {
                if (pie > 0) // client wants actual compass angle to car from this end..
                    ftmp = aim + Facing + 180.0;
                else ftmp = engle + Facing + 180.0; // default: aim of face normal
                if (ftmp < 0.0) ftmp = ftmp + 360.0;
                while (ftmp > 360.0) ftmp = ftmp - 360.0;
                WallAim = ftmp;
            } //~if // >0, c-wise frm N
            fdst = Math.sqrt(Vx * Vx + Hx * Hx); // = actual distance to Vat,Hat
            if (Mini_Log) if (logy > 0) TempStr = HandyOps.Flt2Log("\n (", Vx,
                    HandyOps.Flt2Log("/", Hx, HandyOps.Flt2Log("=", fdst, " ")));
            if (nuly) { // 1st time this wall seen, cache some data..
                // rtns aim of face (90 = square-on) at logical center
                // Law of sines: a/sin(A)=b/sin(B)=c/sin(C) -- side a is opp angle A
                // ..so let a=fdst and A=dd-Facing (dd in BG spec) =engle+90;
                // ..let C=aim = angle of deviation from center; we want b=CashDx,
                // ..and A+B+C=180, so (theory) B=180-A-aim; both A,B: 0<x<180, but..
                // ... if aim=0 (corner @ctr) then CashDx = fdst; if aim<0 B=180-A+aim;
                // ... if aim>0 (corner R of ctr) then A'=180-A, B'=180-A'-aim =A-aim;
                // ..so CashDx/sin(B) = fdst/sin(A); so CashDx = fdst*sin(B)/sin(A)
                ftmp = engle + 90.0; // normalize angle A..
                while (ftmp >= 180.0) ftmp = ftmp - 180.0; // (shouldn't hap)
                while (ftmp < 0.0) ftmp = ftmp + 180.0;      // (ditto)
                MyMath.Angle2cart(ftmp);
                sinA = MyMath.Sine; // always >0 unless engle=90
                CashTan = -MyMath.Cose; // = sin(engle)
                if (aim != 0.0) { // (otherwise sinB=0)
                    if (aim < 0.0) xtmp = 180.0 - ftmp + aim; // normalize angle B..
                    else xtmp = ftmp - aim;
                    while (xtmp >= 180.0) xtmp = xtmp - 180.0;
                    while (xtmp < 0.0) xtmp = xtmp + 180.0;
                    MyMath.Angle2cart(xtmp);
                    sinB = MyMath.Sine;
                } //~if // always >0
                if (Mini_Log) if (logy > 0) TempStr = TempStr
                        + HandyOps.Flt2Log("s'", ftmp, HandyOps.Flt2Log("=", sinA,
                        HandyOps.Flt2Log(" s'", xtmp, HandyOps.Flt2Log("=", sinB, " .. ="))));
                if (aim == 0.0) CashDx = fdst; // square-on this end
                else if (sinA < 0.01) // the wall parallels centerline, no Law of Sines..
                    CashDx = 0.0; // WallAim=ftmp=90 is still good for pies near 90
                else CashDx = fdst * sinB / sinA;
            } //~if // = dist in m to: wall hits cntr, >0
            else if (Mini_Log) if (logy > 0) TempStr = TempStr + "... =";
            if (CashDx > 0.01) ftmp = CashDx;
            else ftmp = fdst;
            zx = (int) Math.round(ftmp * 16.0); // zx = distance to wall in 6cm steps
            ftmp = aim * fImMid / VuEdge; // fImMid=ImWi/2
            xtmp = 1.0;
            if (aim != 0.0) {
                why = why | 0x40000; // why = +4,x
                MyMath.Angle2cart(aim); // off-center, adjust..
                xtmp = MyMath.Cose;
                if (false) ftmp = ftmp * xtmp;
            } //~if // shortens too much // cx = pix H-posn..
            cx = MyMath.iMax(MyMath.iMin((int) Math.round(ftmp + fImMid), ImWi - 1), 0);
            ftmp = fImHaf * fZoom * 342.25; // 120|240*1.9 (need 1.3x fudge) ~ 70K
            // see log: '(Refocus) 52 (389 52. 28. 320.) = 1.9 0.3 +0.0=26.9/K T'
            if (CashDx > 0.5) { // not too close to pixelate (CashDx>0.4 is safe@52mm)..
                why = why | 0x20000; // why = +2,x
                ppm = (int) Math.round(ftmp / CashDx);
            } //~if // = scr.pix*256/m, always >0
            else if (fdst > 0.5) ppm = (int) Math.round(ftmp / fdst); // use corner dist
            if (ppm > 65535) ppm = 0;                  // aim already known <90..
            if (CashDx > 0.0) if (engle != 0.0) if (aim != 0.0) if (xtmp < 1.0) {
                why = why | 0x10000; // why = +1,x
                Hx = CashDx / xtmp; // along corner ray to centerview plane
                Vx = (fdst / Hx) * CashDx;
                zx = (int) Math.round(Vx * 16.0); // now = distance adjusted for slant
                if (Vx > 0.5) nx = (int) Math.round(ftmp / Vx);
                else nx = 0; // too close to pixelate
                if (nx > 0) if (nx < 65536) ppm = (nx << 16) + ppm;
            } //~if // (engle != 0.0)
            TmpI = ppm; // = scr.pix*256/m, always >0
            dx--; // = -4 // only possible if clok=false
            if (zx <= 0) {
                if (clok) zx = 1;
                else break;
            } //~if // why = 4
            if (zx > 2047) zx = 2047; // zx in 6cm units (from cam)
            rx = RangeRow[zx & 2047]; // rx = screen pix V-posn (slant, away from cam)
            if (!clok) if (rx >= ImHi) break; // why = 4, dx = -4
            dx = (rx << 16) + cx; // rx is 0-based frac'l scrn row (ImHaf not incl)
            why = why & -0x10000; // why = x,0
            break;
        } //~while
        if ((why & 0xFFFF) != 0) if (!keep) WallAim = 0.0; // invalidate if err
        if (Mini_Log) if (logy > 0) { // if (Qlog..) -- included in M_L
            if (Vat < 10.0) if (Math.floor(Vat) != Vat) {
                Vat = Vat * 100;
                kx = -4;
            } //~if
            if (Hat < 10.0) if (Math.floor(Hat) != Hat) {
                Hat = Hat * 100;
                kx++;
            } //~if
            nx = (ppm >> 21) & 0x7FF;
            if (nx > 0) TempStr = HandyOps.Fixt8th(" (", nx, ")") + TempStr;
            System.out.println(HandyOps.Dec2Log("(Map2scrn/", logy,
                    HandyOps.Int2Log(") => ", dx, HandyOps.Int2Log(" / ", ppm,
                            HandyOps.Fixt8th("=", (ppm >> 5) & 0x7FF,
                                    HandyOps.Flt2Log(" / ", WallAim, HandyOps.Flt2Log(" ", Vat,
                                            HandyOps.Flt2Log(HandyOps.IffyStr(kx < 0, "c/", "/"), Hat,
                                                    HandyOps.Flt2Log(HandyOps.IffyStr((kx & 1) == 0, " ", "c "), engle,
                                                            HandyOps.TF2Log(" ", clok, HandyOps.TF2Log(" ", nuly,        // _11
                                                                    HandyOps.Int2Log(" ", rx, HandyOps.Int2Log("/", cx,
                                                                            HandyOps.Flt2Log(" ", VuEdge, HandyOps.Flt2Log(" ", aim,
                                                                                    HandyOps.Flt2Log(TempStr, CashDx,  // TempStr = "\n (..Vx/Hx.. ="
                                                                                            HandyOps.Flt2Log(" ", Vx, HandyOps.Flt2Log("/", Hx,
                                                                                                    HandyOps.Dec2Log(") ", zx, HandyOps.Flt2Log(" ", fdst,       // _20
                                                                                                            HandyOps.Flt2Log(" ", ftmp, HandyOps.Flt2Log(" ", xtmp,
                                                                                                                    HandyOps.Dec2Log(" ", pie, HandyOps.Int2Log(" = ", why,   // why =
                                                                                                                            ""))))))))))))))))))))))));
            TempStr = "";
        } //~if
        return dx;
    } //~Map2screen

    /**
     * Returns true if TrakSim is running in fixed-speed mode.
     *
     * @return true if TrakSim is running in fixed-speed mode
     */
    public boolean IsFixtSped() {
        return SimSpedFixt;
    }

    /**
     * Gets the current focal length. =Zoom35 if unchanged.
     *
     * @return 50mm equivalent focal length
     */
    public int LensFocus() {
        return ZoomFocus;
    }

    /**
     * Gets the number of driveshaft turns.
     *
     * @param reset true to reset the count to 0 (restart for next call)
     * @return the number of driveshaft turns since last reset
     */
    public int DriveShaftCount(boolean reset) {
        int whom = MyMath.Trunc8(ShafTurns);
        if (reset) ShafTurns = ShafTurns - MyMath.Fix2flt(whom, 0);
        return whom;
    } //~DriveShaftCount

    /**
     * Sets/overrides the top row for the close-up map (if showing).
     *
     * @param here The image row to start map on
     */
    public void SetCloseTop(int here) {
        int nx, info, why = 0; // final int xCloseUp = 3; // xC=2..5
        while (true) {
            why++; // why = 1
            if (!ShowMap) break;
            why++; // why = 2
            if (!DoCloseUp) break;
            why++; // why = 3
            if (here < 0) break;
            why++; // why = 4
            if (here == 0) {
                TopCloseView = 0;
                ZooMapBase = 0;
                ZooMapDim = 0;
                break;
            } //~if
            if (here > ImHi - 32) break;
            why++; // why = 5
            if (RasterMap == null) {
                RasterMap = new double[ImHaf * 4]; // for V,H at each row end
                if (RasterMap == null) break;
            } //~if
            why++; // why = 6 (commit)
            TopCloseView = here;
            if (((xCloseUp - 2) & 0xFF) < 4) nx = MapWiBit - (xCloseUp & 7);
            else if (TurnRadius < 7.5) nx = 4;
            else nx = 5; // = log2(displayed close-up width in park meters)
            ZooMapShf = MapWiBit - nx; // MapWiBit=log2(MapWide=256)=8, so ZSf= 4 or 3
            // zx = 1<<ZooMapShf; // MapWide=256, so zx=8 or =16, =pix/m in c-u view
            info = (ImHi - here) >> ZooMapShf; // = close-up height in pix, now pk meters
            ZooMapBase = (here << 16) + ImWi + 2; // ZooMapShf cvts c-u(pix) <-> meters..
            ZooMapDim = (info << 16) + (1 << nx); // = displayed close-up size in meters
            ZooMapScale = MyMath.Fix2flt(1 << (ZooMapShf + 1), 0); // cv 2m grid -> img pix
            if (WhitLnSz == 0.0) ZooMapWhLn = 0.0;
            else ZooMapWhLn = 2.8 / ZooMapScale; // 1pix = white line width (meters) in c-u
            why = 0;
            break;
        } //~while
        if ((Qlog < 0) || (why > 4)) System.out.println(HandyOps.Dec2Log("(SetCloTop) ", here,
                HandyOps.Int2Log("  ZMD=", ZooMapDim, HandyOps.Dec2Log(" ZSf=", ZooMapShf,
                        HandyOps.Int2Log(" ZB=", ZooMapBase, HandyOps.Flt2Log(" ZSc=", ZooMapScale,
                                HandyOps.Flt2Log(" ZW=", ZooMapWhLn,
                                        HandyOps.Dec2Log(" = ", why, ""))))))));
    } //~SetCloseTop

    /**
     * Sets the position and direction of the simulated car.
     *
     * @param Vat The vertical (southward) component of the position in meters
     * @param Hat The horizontal (eastward) component of the position in meters
     * @param aim The direction facing, in degrees clockwise from north
     */
    public void SetStart(int Vat, int Hat, int aim) {
        System.out.println(HandyOps.Dec2Log(" (SetStart) ", Vat, HandyOps.Dec2Log("/", Hat,
                HandyOps.Dec2Log(" -> ", aim, ""))));
        Vposn = MyMath.Fix2flt(Vat, 0);
        Hposn = MyMath.Fix2flt(Hat, 0);
        Facing = MyMath.Fix2flt(aim, 0); // aim is in degrees clockwise from North
        SteerWhee = 0; // re-init steering latency..
        SpecWheel = 0;
        fSteer = 0.0;
        fSpeed = 0.0; // make sure there's no residual motion..
        GasBrake = 0;
        Velocity = 0.0;
        Skidding = false;
        ValidPixSteps = false;
        AverageAim = Facing;
    } //~SetStart

    /**
     * Converts a row/column click location on the close-up map (if showing)
     * to a true (park) position in 64ths of a meter, packed into an integer.
     *
     * @param aim2 If true, also points the car to be facing the click point
     * @param rx   The image row clicked on
     * @param cx   The image column clicked on, relative to the window top/left
     * @return A packed integer, the vertical (southward) component
     * in the high 16 bits, horizontal (eastward) in the low 16;
     * the components are fixed-point, with 6-bit fractions.
     */
    public int ZoomMap2true(boolean aim2, int rx, int cx) { // rtns (r,c)<<6
        int base = (rx << 16) - ZooMapBase + cx, zx = base << (6 - ZooMapShf),
                rez = (ZooMapTopL << 6) + zx;
        double Vat = 0, Hat = 0; // get posn: Vat = MyMath.Fix2flt(rez&-0x10000,22);
        if (aim2) while (true) { //    Hat = MyMath.Fix2flt(MyMath.SgnExt(rez),6);
            Vat = MyMath.Fix2flt(rez & -0x10000, 22) - Vposn;
            Hat = MyMath.Fix2flt(MyMath.SgnExt(rez), 6) - Hposn;
            if (MyMath.fAbs(Vat) + MyMath.fAbs(Hat) < 1.5) break;
            Facing = MyMath.aTan0(Hat, -Vat);
            ValidPixSteps = false;
            AverageAim = Facing;
            // NuData++; // caller does
            break;
        } //~while   // ZMD=12,16 ZSf=4 ZB=34,322 ZSc=32. ZW=0.1 ZT=10,8 // *
        if (Mini_Log) if (Qlog < 0)
            System.out.println(HandyOps.TF2Log("(ZooMp2tru) ", aim2,
                    HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", cx,
                            HandyOps.Int2Log(" ", base, HandyOps.Int2Log(" ", zx,
                                    HandyOps.Hex2Log(" = x", rez, 8, // HandyOps.IffyStr(!aim2,"",
                                            HandyOps.Flt2Log(" - ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                                    HandyOps.Flt2Log(" -> ", Vat, HandyOps.Flt2Log("/", Hat,
                                                            HandyOps.Flt2Log(" => ", Facing, ""))))))))))));
        return rez;
    } //~ZoomMap2true

    /**
     * Converts a row/column position on the perspective view generated by TrakSim
     * to an image pixel position of the close-up map (if shown) or zero otherwise.
     * <p>
     * This is useful for adding to the close-up map display information derived
     * from examining the perspective view, which is the normal way a self-driving
     * car program would be operating. Alternatively (yx=true) you can convert
     * direct map coordinates (in park meters) to screen pixel in the close-up map.
     * <p>
     * The close-up map jumps around dynamically depending on the position and
     * direction of the car. This allows your software to add information to the
     * map view. Use ZoomMap2true to reverse the calculation.
     *
     * @param yx  If true, converts a position in meters instead of screen coord
     * @param Vat The image row in pixels, or map vertical in park meters
     * @param Hat The image column in pixels, or map horizontal in park meters
     * @return A packed integer, the pixel row in the high 16 bits,
     * the column in the low 16, or else 0 if not visible.
     */
    public int ZoomMapCoord(boolean yx, double Vat, double Hat) {
        // Vat,Hat in screen img posn, rtn map pix posn, or =0 if off-map
        double Vinc = 0.0, Hinc = 0.0, Vstp = 0.0, Hstp = 0.0, deep = 0.0;
        int nx = 0, kx = 0, zx = 0, rx = 0, cx = 0, why = 0;
        while (true) {
            why++; // why = 1
            if (!ShowMap) break;
            why++; // why = 2
            if (!DoCloseUp) break;
            why++; // why = 3
            if (ZooMapDim == 0) break;
            why++; // why = 4
            if (ZooMapBase == 0) break;
            why++; // why = 5
            if (ZooMapScale == 0.0) break; // ZMS*(2m grid) -> img pix in close-up
            why++; // why = 6
            if (Vat < 0.0) break;
            if (Hat < 0.0) break;
            if (!yx) { // yx=false converts image coordinate '(ZoMaCo) F' ..........
                why++; // why = 7
                if (RasterMap == null) break;
                rx = (int) Math.round((Vat - fImHaf) * 4.0);
                why++; // why = 8
                if (rx <= 0) break;
                why++; // why = 9
                if (rx >= RasterMap.length - 4) break;
                why++; // why = 10
                deep = Hat / FltWi; // = fractional position in image (1.0 = right edge)
                Vinc = RasterMap[rx]; // left end of raster (in grid=2m)
                Hinc = RasterMap[rx + 1];
                Vstp = RasterMap[rx + 2] - Vinc; // (distance to) right end
                Hstp = RasterMap[rx + 3] - Hinc;     // convert to close-up pix scale..
                rx = ((int) Math.round((Vstp * deep + Vinc) * ZooMapScale));
                // ZMD=17,32 ZSf=3 ZB=98,322 ZSc=16. ZW=0.2 ZT=80,30
                if (rx <= 0) break; // why = 10
                if (rx >= (MapHy << ZooMapShf)) break;
                cx = ((int) Math.round((Hstp * deep + Hinc) * ZooMapScale));
                why++; // why = 11
                if (cx <= 0) break;
                if (cx >= (MapWy << ZooMapShf)) break;
                why++;
            } //~if // why = 12
            else { // yx=true converts park coordinate '(ZoMaCo) T' ................
                why = 15;
                if (Hat >= fMapWi) break;
                why++; // why = 16
                deep = ZooMapScale * 0.5; // scales from meters instead of 2m grid
                rx = (int) Math.round(Vat * deep); // in close-up pix..
                cx = (int) Math.round(Hat * deep);
            } //~else
            // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
            kx = (rx << 16) + cx; // (in c-u pix) then back to pk meters for bounds test..
            zx = ((kx >> ZooMapShf) & 0xFFF0FFF) - ZooMapTopL; // ZooMapShf cvts c-u(pix)..
            if ((zx & 0x80008000) != 0) nx = 0; // above or left       // .. <-> meters
            else if (((ZooMapDim - zx) & 0x80008000) != 0) nx = 0; // below or right
            else nx = kx + ZooMapBase - (ZooMapTopL << (ZooMapShf));
            if (nx != 0) why = 0; // else why = 13/16
            break;
        } //~while
        return nx;
    } //~ZoomMapCoord

    /**
     * Replaces the TrakSim pixel buffer with your own array.
     * You are responsible for saving the previous screen and restoring it
     * before calling any TrakSim method that expects to be using its own
     * screen buffer, such as NextFrame.
     *
     * @param theAry The pixel array, at least tall*wide in length
     * @param tall   The number of pixel rows
     * @param wide   The number of pixel columns
     * @param tile   Should be 1, because that's what TrakSim knows
     */
    public void SetMyScreen(int[] theAry, int tall, int wide, int tile) {
        int size = tall * wide;
        if (theAry == null) return;
        if (size > theAry.length) return;
        if (tall <= 0) return;
        if (wide <= 0) return;
        myScreen = theAry;
        SceneBayer = tile;
        SceneTall = tall;
        SceneWide = wide;
        PixScale = 0;
    } //~SetMyScreen

    /**
     * Gets the TrakSim pixel at the specified row and column.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param rx The pixel row
     * @param cx The pixel column
     * @return The pixel in that position, = 0x00RRGGBB
     */
    public int PeekPixel(int rx, int cx) { // <- myScreen
        int here = SceneWide, didit = 0;
        int[] myPix = myScreen;
        if (rx >= 0) if (rx <= 0x4000) if (here > 0) if (here <= 0x4000) {
            here = rx * here + cx; // T68 compiler uses fast multiply
            didit++;
        }
        if (didit == 0) here = rx * WinWi + cx;
        if (myPix != null) if (here < myPix.length)
            if (here >= 0) return myPix[here];
        return 0;
    } //~PeekPixel

    /**
     * Sets the TrakSim pixel at the specified row and column.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel to go in that position, = 0x00RRGGBB
     * @param rx   The pixel row
     * @param cx   The pixel column
     */
    public void PokePixel(int colo, int rx, int cx) { // -> myScreen
        int here = SceneWide, didit = 0;
        int[] myPix = myScreen;
        if (rx >= 0) if (rx <= 0x4000) if (here > 0) if (here <= 0x4000) {
            here = rx * here + cx; // T68 compiler uses fast multiply
            didit++;
        }
        if (didit == 0) here = rx * WinWi + cx;
        if (myPix != null) if (here < myPix.length)
            if (here >= 0) myPix[here] = colo;
    } //~PokePixel

    /**
     * Sets a whole rectangle of TrakSim pixels from one corner of the
     * rectangle in pixel coordinates, to the opposite corner.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel color to fill that rectangle, = 0x00RRGGBB
     * @param rx   The pixel row in one corner
     * @param cx   The pixel column in the same corner
     * @param rz   The pixel row in the other corner (inclusive)
     * @param cz   The pixel column there
     */
    public void RectFill(int colo, int rx, int cx, int rz, int cz) { // BR not sz
        int here, thar, whar;        // top,  left,  bottom,  right
        int[] myPix = myScreen;
        if (myPix == null) return;
        if (rz < rx) {
            here = rz;
            rz = rx;
            rx = here;
        }
        if (cz < cx) {
            here = cz;
            cz = cx;
            cx = here;
        }
        if (rx < 0) rx = 0;
        if (rz >= SceneTall) rz = SceneTall - 1;
        if (cx < 0) cx = 0;
        if (cz >= SceneWide) cz = SceneWide - 1;
        whar = rx * SceneWide + cx;
        for (rx = rx; rx <= rz; rx++) {
            thar = whar;
            whar = whar + SceneWide;
            for (here = cx; here <= cz; here++) {
                if (thar >= 0) if (thar < myPix.length) myPix[thar] = colo;
                thar++;
            }
        }
    } //~RectFill

    /**
     * Draws a line of TrakSim pixels.
     * This might be your own buffer, if you called SetMyScreen.
     *
     * @param colo The pixel color to draw that line, = 0x00RRGGBB
     * @param rx   The pixel row at one end
     * @param cx   The pixel column at the same end
     * @param rz   The pixel row at the other end
     * @param cz   The pixel column there
     */
    public void DrawLine(int colo, int rx, int cx, int rz, int cz) {
        int nx;
        double here, step;
        if (SceneBayer > 0) if ((rz == rx) || (cz == cx)) {
            RectFill(colo, rx, cx, rz, cz);
            return;
        } //~if
        rz = rz - rx;
        cz = cz - cx;
        if (MyMath.iAbs(rz) > MyMath.iAbs(cz)) { // more vert than horiz..
            if (rz < 0) {
                cx = cx + cz;
                cz = -cz;
                rx = rx + rz;
                rz = -rz;
            } //~if
            step = MyMath.Fix2flt(cz, 0) / MyMath.Fix2flt(rz, 0);
            here = MyMath.Fix2flt(cx, 0);
            for (nx = 0; nx <= rz; nx++) {
                PokePixel(colo, nx + rx, MyMath.Trunc8(here));
                here = here + step;
            } //~for
            return;
        } //~if
        if (cz < 0) {
            rx = rx + rz;
            rz = -rz;
            cx = cx + cz;
            cz = -cz;
        } //~if
        step = MyMath.Fix2flt(rz, 0) / MyMath.Fix2flt(cz, 0);
        here = MyMath.Fix2flt(rx, 0);
        for (nx = 0; nx <= cz; nx++) {
            PokePixel(colo, MyMath.Trunc8(here), nx + cx);
            here = here + step;
        }
    } //~DrawLine

    /**
     * Draws a tiny decimal digit (or one of six other special characters)
     * on the TrakSim pixel buffer. The digit is 5 pixels high and 4 wide.
     * This might be drawn on your own buffer, if you called SetMyScreen.
     *
     * @param whom The digit to draw, a 4-bit index into "0123456789NESW.-"
     * @param rx   The pixel row at the bottom right corner
     * @param cx   The pixel column there
     * @param colo The color to draw that digit, = 0x00RRGGBB
     */
    public void ShoDigit(int whom, int rx, int cx, int colo) {
        int ro, co, bitz = TinyBits[whom & 15], wide = bitz >> 20, thar = wide;
        for (ro = 4; ro >= 0; ro += -1) {
            for (co = 3; co >= 0; co += -1) {
                if ((bitz & 1) != 0) {
                    if (PixScale > 0) RectFill(colo, rx - PixScale, cx - PixScale, rx, cx);
                    else PokePixel(colo, rx, cx);
                } //~if
                cx = cx - 1 - PixScale;
                bitz = bitz >> 1;
                if (wide == 0) continue;
                if (co != 2) continue;
                if ((thar & 1) != 0) {
                    if (PixScale > 0) RectFill(colo, rx - PixScale, cx - PixScale, rx, cx);
                    else PokePixel(colo, rx, cx);
                } //~if
                cx = cx - 1 - PixScale;
                thar = thar >> 1;
            } //~for // (co)
            if (PixScale > 0) {
                if (wide != 0) cx = cx - 4;
                cx = cx + (PixScale << 2);
                rx = rx - PixScale;
            } //~if
            cx = cx + 4;
            if (wide != 0) cx++;
            rx--;
        }
    } //~ShoDigit

    /**
     * Draws a text string (which must be only the characters: " 0123456789NESW.-")
     * on the TrakSim pixel buffer.
     *
     * @param aLine The text string to draw
     * @param rx    The pixel row at the bottom (right corner)
     * @param cx    The pixel column there, or the left end if negative
     * @param colo  The color to draw that text, = 0x00RRGGBB
     */
    public void LabelScene(String aLine, int rx, int cx, int colo) {
        int nx, bitz, doit = aLine.length(), zx = doit, more = PixScale + 1;
        char xCh;
        if (cx < 0) cx = ((more * doit) << 2) - cx; // H-posn<0 places left end of number
        cx = (more << 2) + cx;
        for (nx = doit - 1; nx >= 0; nx += -1) {
            xCh = HandyOps.CharAt(nx, aLine);
            cx = cx - (more << 2);
            if (xCh < ' ') break;
            if (xCh == ' ') {
                zx--;
                continue;
            } //~if
            if (xCh == '-') xCh = '/'; // for now, 'cuz we need '-' but not '/'
            bitz = (int) xCh;
            if (xCh > 'A') switch (bitz & 7) {
                case 3: // 'S'
                    bitz = 12;
                    break;
                case 5: // 'E'
                    bitz = 11;
                    break;
                case 6: // 'N'
                    bitz = 10;
                    break;
                case 7: // 'W'
                    bitz = 13;
                    break;
            } //~switch
            ShoDigit(bitz, rx, cx, colo);
            zx--;
            if (xCh != 'N') if (xCh != 'W') continue;
            cx = cx - (more + more);
        } //~for // (nx)
    } //~LabelScene

    /**
     * Sets (enlarged) pixel size for ShoDigit.
     * You should restore it to 0 (no enlargement) when done.
     *
     * @param size The number of screen pixels per digit pixel
     */
    public void SetPixSize(int size) {
        if (size < 0) return;
        if (size * 8 > ImHi) return;
        if (size > 0) size--;
        PixScale = size;
    } //~SetPixSize

    /**
     * Draws a red "X" in the lower left corner of the TrakSim pixel buffer.
     */
    public void DrawRedX() { // to show it crashed
        int rx;
        for (rx = 0; rx <= 4; rx++) { // draw red (crashed) "X" in lower left corner..
            PokePixel(0xFF0000, ImHi - 8 + rx, 4 + rx);
            PokePixel(0xFF0000, ImHi - 8 + rx, 8 - rx);
        } //~for
        if (Qlog == 0) return;
        System.out.println(HandyOps.Dec2Log(" (DrRedX) o", OpMode, ""));
    } //~DrawRedX

    /**
     * Draws a tan steering wheel slightly to the left of center at the bottom
     * of the TrakSim pixel buffer. Optionally also shows the dashboard.
     * <p>
     * The steering wheel may be drawn with a piece of "white tape" on the top,
     * so it's easy to see when it it turned off-center.
     * The wheel has 23 positions from -11 to +11 where this tape is shown,
     * and it is not shown if the position is out of range.
     * Alternatively, you can give it a signed angle -127 to +127 and have it
     * scaled non-linearly to fit in the shorter range.
     *
     * @param posn   The position to draw the white tape, -11 to +11,
     *               or -127 to +127 if scaled
     * @param scaled True if the position is to be scaled
     * @param dash2  Also draw the dashboard from available information
     */
    public void DrawSteerWheel(int posn, boolean scaled, boolean dash2) {
        // with white tape (if posn in range) + steer/speed servo settings..
        int prio = posn, info, nx, doit, here, thar, whar, rx, cx, bitz = 0;
        String myDash = "", LefDash = "";
        if (DrawDash > 0) if (dash2) { // show dashboard..
            if (OpMode == 3) bitz = 0xCC0000; // red when crashed,
            else if (MyMath.iAbs(SpecWheel) > 88) bitz = 0xFF; // blue when crimped
            RectFill(bitz, SceneTall - 1 - DrawDash, 0, SceneTall, SceneWide);
            LefDash = " " + SpecWheel + " " + FrameNo;
            LabelScene(LefDash, SceneTall - 4, -12, 0xFFFFFF);
            myDash = HandyOps.Fixt8th(" ", RealTimeNow, " ");
            LabelScene(myDash, SceneTall - 4, myDash.length() * 2 + ImMid, 0xFF9900);
            if (Log_Draw) LefDash = LefDash + " t=" + myDash;
            myDash = HandyOps.Flt2Log(" ", Velocity * fFPS, " "); // (5/fps) fFPS=5
            LabelScene(myDash, SceneTall - 4, myDash.length() * 2 + SteerMid, 0xFF9999);
            if (Log_Draw) myDash = " '" + LefDash + "/" + myDash + "'";
        } //~if // (dash2)
        if (scaled) if (posn < 127) { // scale it to TapedWheel..
            info = MyMath.iAbs(posn);
            if (info != 0) for (nx = (TapedWheel[0] & 255) - 1; nx >= 0; nx += -1) {
                if (nx > 0) if (((TapedWheel[nx & 15] >> 8) & 255) > info) continue;
                if (posn < 0) info = -nx;
                else info = nx;
                break;
            } //~for // always exits here, with info set
            posn = info;
        } //~if // (scaled)
        for (doit = 0; doit <= 1; doit++)
            for (here = doit; here <= 11; here++) { // draw steering wheel..
                thar = TapedWheel[here & 15];
                whar = thar >> 16;
                info = SteerColo; // SteerColo=0xCC9900;
                if (doit == 0) {
                    if (here == posn) info = 0xFFFFFF; // PixWhit;
                    whar = whar + SteerMid;
                } //~if
                else {
                    if (here + posn == 0) info = 0xFFFFFF; // PixWhit;
                    whar = SteerMid - 1 - whar;
                } //~else
                if (Log_Draw) myDash = "";
                for (thar = thar & 255; thar <= 99; thar++) {
                    if (thar >= TapedWheel.length) break;
                    bitz = TapedWheel[thar]; // lsb is bottom of screen
                    if (bitz == 0) break;
                    for (cx = 0; cx <= 31; cx++) {
                        rx = ImHi - 1 - cx;
                        if ((bitz & 1) != 0) PokePixel(info, rx, whar);
                        bitz = bitz >> 1;
                        if (bitz == 0) break;
                    } //~for // (cx)
                    if (doit == 0) whar++;
                    else whar--;
                }
            }
    } //~DrawSteerWheel

    /**
     * Draws a rectangular grid on the screen according to the specification
     * array given to NewGridTbl.
     */
    public void DrawGrid() { // to show where to click..
        // private final int[] Grid_Locns = {6,12,16,18,26,28,  0,16,80,126,228,240,
        //   0,20,300,320,  0,320,  0,45,91,137,182,228,274,320, 0,320,  0,20,300,320};
        int here, thar, whar, nx, prio, fini, lft, rit,
                tops, info = 0, next = 0, botm = ImHi - 1;
        int[] grids = GridLocTable;
        if (grids == null) return;
        if (grids.length < 4) return;
        thar = grids[1]; // (end of) 1st group, vert'l divisions
        whar = grids[0]; // (next) top of vert'l group
        fini = whar - 1; // end of index
        if (fini <= 0) return;
        if (whar >= thar) return;
        if (whar > 0) if (whar < grids.length) next = grids[whar];
        rit = thar;
        for (here = 2; here <= fini; here++) {
            if (grids == null) break;
            whar++;
            tops = next;
            next = botm;
            if (whar < thar) if (whar > 0) if (whar < grids.length)
                next = grids[whar];
            if (tops >= botm) break;
            if (next > botm) next = botm;
            if (tops >= next) continue;
            if (tops > 0) for (nx = 0; nx <= ImWi - 1; nx++) if ((nx & 8) == 0) PokePixel(0xFF0000, tops, nx);
            if (grids == null) break;
            lft = rit;
            if (here > 0) if (here < grids.length) rit = grids[here];
            prio = 0;
            while (lft < rit) {
                if (grids == null) break;
                if (lft > 0) if (lft < grids.length) info = grids[lft];
                if (prio > 0) if (prio < info) for (nx = tops; nx <= next - 1; nx++)
                    if (((nx - tops) & 8) == 0) PokePixel(0xFF0000, nx, prio);
                prio = info;
                if (prio > ImWi - 2) break;
                lft++;
            }
        }
    } //~DrawGrid

    /**
     * Converts a click location on the window to a row&column number
     * corresponding to the rectangular specification given to NewGridTbl.
     *
     * @param rx The image row clicked on
     * @param cx The image column clicked on, relative to the window top/left
     * @return A packed integer, the grid row in the high 16 bits,
     * and the horizontal cell number in the low 16, or 0 if none
     */
    public int GridBlock(int rx, int cx) { // find click rgn of screen..
        int nx, fini, here, thar = 0, zx = 0, lino = 0, posn = 0;
        int[] grids = GridLocTable;
        if (grids == null) return 0;
        if (grids.length < 4) return 0;
        here = grids[1]; // (end of) 1st group, vert'l divisions
        fini = grids[0]; // top of 1st vert'l group, = end of index
        if (fini <= 0) return 0;
        if (fini >= here) return 0;
        for (here = here - 1; here >= fini; here += -1) {
            if (grids == null) break;
            if (here > 0) if (here < grids.length) zx = grids[here];
            if (rx >= zx) lino = here + 1 - fini;
            if (rx >= zx) break;
        } //~for
        if (grids == null) return 0;
        here = lino;
        if (here > 0) if (here < fini) if (here < grids.length - 2) {
            if (here + 1 == fini) thar = grids.length;
            else thar = grids[here + 1];
            nx = grids[here];
            for (thar = thar - 1; thar >= nx; thar += -1) {
                if (grids == null) break;
                if (thar > 0) if (thar < grids.length) zx = grids[thar];
                if (cx >= zx) posn = thar + 1 - nx;
                if (cx >= zx) break;
            }
        } //~if
        return (lino << 16) + posn;
    } //~GridBlock

    /**
     * Uses a specially formatted integer array to specify a grid of click
     * regions on the window. The grid consists of three or more ordered
     * sequences of integers, the first listing the beginnings (offsets into
     * the array) of each of the rest. The second sequence gives the pixel row
     * for the top each horizontal group (all the rest), which in turn give
     * left edge of each cell in sequence for  not replace it with your own) is the array
     * Grid_Locns, seeing which is probably the easy to understand the format.
     * There are seven sequences, the index starting at 0, then the six
     * sequences starting at the offsets it points to: 6,12,16,18,26,29.
     * The first of those, starting a offset 6, defines five regions vertically,
     * The top of the first region is the screen top =0, and extends for 16
     * pixel rows, the second to the middle of the screen less 40 pixel rows,
     * then the middle, then the top edge of the dashboard, which ends at the
     * bottom. The horizontal cells of the top group are the left 20 pixels as
     * row +1 cell +1, the right 20 pixels (+3), and everything in between as
     * cell +2 in row +1. The second group has only one cell, from the left
     * edge (0) to the right edge (ImWi); any click in that region is (2,1).
     * The next row, just above the middle of the screen, has seven equal cells.
     * The third row, from the middle down to the dashboard, has a little sliver
     * on the left (click there to properly shut down the serial port and exit).
     * And so on. Your only constraint is that all the cells on a horizontal row
     * are the same height.
     *
     * @param grids The array (default = Grid_Locns)
     * @return True if properly formatted, or false if rejected.
     */
    public boolean NewGridTbl(int[] grids) { // to replace default screen grid..
        int nx, lxx, info = 0, fini = 0, here = 0, thar = 0, prio = 0, why = 0;
        // private final int[] Grid_Locns = {4,8,12,20,  0,16,228,240,
        //    0,20,300,320,   0,45,91,137,182,228,274,320,  0,20,300,320};
        while (true) {
            why++; // why = 1
            if (grids == null) break; // why = 1: null table
            why++; // why = 2
            lxx = grids.length;
            if (lxx < 4) break; // why = 2: not a useful size
            why++; // why = 3
            thar = grids[0];
            if (thar < 2) break; // why = 3: not enough division sets
            why++; // why = 4
            fini = thar - 1;
            for (here = 1; here <= fini; here++) {
                if (grids == null) break;
                nx = thar;
                thar = lxx;
                if (here > 0) if (here < grids.length) thar = grids[here];
                why++; // why = 5
                if (nx > thar) break; // why = 5: table index out of order
                why++; // why = 6
                prio = 0;
                for (nx = nx; nx <= thar; nx++) { // null division set OK
                    if (nx == thar) { // done with this group,
                        why = 4; // ..do next
                        break;
                    } //~if
                    if (grids != null) if (nx > 0) if (nx < grids.length) info = grids[nx];
                    if (info < prio) break; // why = 6: group item out of order (null OK)
                    prio = info;
                } //~for
                if (why > 4) break;
            } //~for
            if (why > 4) break;
            GridLocTable = grids;
            why = 0;
            break;
        } //~while
        if (Qlog < 0) if (why > 0)
            System.out.println(HandyOps.Dec2Log("NewGridTb failed, why = ", why,
                    HandyOps.Dec2Log(" @ ", here, "")));
        return why == 0;
    } //~NewGridTbl

    /**
     * Requests TrakSim to set its simulation mode to one of three states.
     * mode=0: (initially) Paused, the simulated car does not move.
     * This is required to clear a crashed condition (OpMode==3).
     * mode=1: Single-step, the simulated car is updated this once only;
     * call SimStep(1) after each steering&gas update if your driving code
     * takes up more than FrameTime ticks (including TrakSim time).
     * This does not clear a crashed condition (OpMode==3).
     * mode=2: Real-time, the simulated car is updated on every call to
     * GetSimFrame (through camera.NextFrame), as many times as
     * necessary to catch up to the real-time count of FrameTime ticks;
     * calling before the next tick (or if the simulated car is not moving
     * unless FreshImage was called) returns the previous image. Using this
     * mode when your driving software is too slow will miss frames (same
     * as happens with a real camera). Does nothing if crashed (OpMode==3).
     * mode=3: Crashed, same as Paused, but draws a red "X" on the dashboard.
     * Call SimStep(0) to clear the crashed condition.
     *
     * @param mode True to get the horizontal (east-west) coordinate
     */
    public void SimStep(int mode) {
        int nuly = mode, prio = OpMode;
        boolean doit = true;
        if (prio == 3) if (nuly > 0) doit = false;
        if (nuly < 0) {
            if (DriverCons.D_StartInCalibrate)
                if (SpecWheel == 0) unScaleStee = true; // used to calibrate servos
            nuly = 0;
        } //~if
        else if (nuly >= 3) {
            nuly = 3;
            NuData++;
        } //~if
        else if (nuly == 0) unScaleStee = false;
        else if (nuly == 2) ClockTimed = true;
        if (doit) {
            OpMode = nuly; // set only here
            StepOne = (nuly == 1);
            NextFrUpdate = 0;
        } //~if // in mode=2 this is when to update image
        if (Qlog < 0) System.out.println(HandyOps.Dec2Log(" (SimStep) o", mode,
                HandyOps.Dec2Log("/", prio, HandyOps.IffyStr(prio == 3, " (Crashed)",
                        HandyOps.IffyStr((nuly == 3) && (prio < 3), " ($$ CrashMe $$)",
                                HandyOps.TF2Log(" Fx=", SimSpedFixt, HandyOps.TF2Log(" Tr=", SimInTrak,
                                        HandyOps.IffyStr(unScaleStee, " (cal)", "")))))))
        );
        NuData++;
    } //~SimStep

    private void HeadShoLines() { // only from BF
        int rx, cx, nx, zx, deep, here, thar, lxx = 15, whom = 0, why = 0;
        double dark, Vbase, Hbase, Vat, Hat, Vstp, Hstp, Vcs, Hsn;
        String aWord = "; ";
        if (ShoHeadLite == null) return;
        whom = ShoHeadLite[0];
        if (whom == 0) return; // nothing to log
        while (true) { // now show it on-screen..
            if (whom > 0) { // convert the data..
                SeenHedLite = false;
                dark = Cast_I2F(ShoHeadLite[1]);
                Vbase = Cast_I2F(ShoHeadLite[2]);
                Hbase = Cast_I2F(ShoHeadLite[3]);
                aWord = HandyOps.Flt2Log(aWord, dark, HandyOps.Int2Log(" ", whom,
                        HandyOps.Flt2Log(" ", Vbase, HandyOps.Flt2Log("/", Hbase, " "))));
                lxx = aWord.length() + lxx;
                why--; // why = -1
                for (nx = 0; nx <= 2; nx += 2) { // why = -1/-4/(-8)
                    zx = whom & 0x3FF;
                    whom = whom >> 16;
                    MyMath.Angle2cart(MyMath.Fix2flt(zx, 0));
                    Hstp = MyMath.Sine * dark; // = h
                    Vstp = MyMath.Cose * dark; // = v
                    here = ZoomMapCoord(true, Vbase - Vstp, Hbase + Hstp);
                    aWord = HandyOps.Flt2Log(aWord, Vstp, HandyOps.Flt2Log("/", Hstp,
                            HandyOps.Int2Log(" = ", here, HandyOps.Int2Log(" (", zx,
                                    HandyOps.Int2Log(") #", nx, " ")))));
                    lxx = lxx + 30;
                    why--; // why = -2/-5 => -16/-40
                    if (here == 0) break;
                    ShoHeadLite[nx & 3] = here;
                    Vat = Vbase - Vstp * 16.0;
                    Hat = Hbase + Hstp * 16.0;
                    for (thar = 15; thar >= 0; thar += -1) {
                        here = ZoomMapCoord(true, Vat, Hat);
                        if (lxx > 57) {
                            aWord = aWord + "\n    ";
                            lxx = 4;
                        } //~if
                        aWord = HandyOps.Flt2Log(aWord, Vat, HandyOps.Flt2Log("/", Hat,
                                HandyOps.Int2Log(" = ", here, HandyOps.Int2Log(" #", thar, " "))));
                        lxx = lxx + 24;
                        if (here > 0) break;
                        Vat = Vat + Vstp;
                        Hat = Hat - Hstp;
                    } //~for
                    why--; // why = -3/-6 => -24/-48
                    if (here == 0) break;
                    ShoHeadLite[(nx + 1) & 3] = here;
                    why--; // why = -4/-7
                    if (nx == 0) continue; // why = -4
                    why--; // why = -8 => -64+4 = -60 (good exit)
                    whom = ShoHeadLite[0] | 0x80000000;
                    ShoHeadLite[0] = whom;
                    SeenHedLite = true;
                    break;
                }
                why = why << 3;
            } //~if // why = -64 (good)
            why++; // why = +1 // now show it on-screen..
            if (whom > 0) SeenHedLite = false;
            if (!SeenHedLite) break;
            // Vbase = MyMath.Fix2flt(rx,0);
            // Hbase = MyMath.Fix2flt(cx,0);
            whom = whom & 0x7FFFFFFF;
            here = ShoHeadLite[1];
            deep = ShoHeadLite[2];
            thar = ShoHeadLite[3];
            if (lxx > 55) {
                aWord = aWord + "\n    ";
                lxx = 4;
            } //~if
            aWord = HandyOps.Int2Log(aWord, whom, HandyOps.Int2Log(" ", here,
                    HandyOps.Int2Log(" ", deep, HandyOps.Int2Log(" ", thar, ""))));
            lxx = aWord.length() + lxx;
            why++; // why = +2
            // if ((rx|cx) !=0)
            //   if (MyMath.fAbs(Vbase-Vposn)+MyMath.fAbs(Hbase-Hposn)>4.0) break;
            why++; // why = +3
            if (whom == 0) break;
            if (here == 0) break;
            if (deep == 0) break;
            if (thar == 0) break;
            rx = whom >> 16;
            cx = whom & 0xFFFF;
            nx = here >> 16;
            zx = here & 0xFFFF;
            DrawLine(ArtiColo, rx, cx, nx, zx); // 1st ray
            nx = deep >> 16;
            zx = deep & 0xFFFF;
            DrawLine(ArtiColo, rx, cx, nx, zx); // base line
            rx = thar >> 16;
            cx = thar & 0xFFFF;
            DrawLine(ArtiColo, rx, cx, nx, zx); // 2nd ray
            why++; // why = +4
            break;
        } //~while
        lxx = lxx + 13;
        if (lxx > 55) lxx = 7;     // see also: (HedLiSho)
        if (Qlog < 0) System.out.println(HandyOps.Int2Log("(HedShoLi) = ", why,
                HandyOps.Int2Log(aWord, whom, HandyOps.TF2Log(" ", SeenHedLite,
                        HandyOps.IffyStr(lxx < 8, "\n    --", " --")
                                + HandyOps.ArrayDumpLine(ShoHeadLite, 0, lxx)))));
        if (!SeenHedLite) ShoHeadLite[0] = 0;
    } //~HeadShoLines // final log

    private int ViewAngle(int whar, int locn, int info, boolean prox) {
        boolean doit = (ProxyThresh > 0.0) && prox; // locn is artifact posn in 25cm u
        double Vstp = 0.0, Hstp = 0.0, dx = ProxyThresh;
        int res, aim, Hx, Vx = info >> 16, locx = locn & 0x0FFF0FFF, Vz = locx >> 16,
                Hz = locx & 0xFFFF;
        if (PreViewLoc != locx) {
            Vstp = MyMath.Fix2flt(Vz, 2) - Vposn; // Vz/Hz in 25cm grid/8 units,
            Hstp = Hposn - MyMath.Fix2flt(Hz, 2); // ..Vposn/Hposn in 1m park coords
            if (doit) dx = Vstp * Vstp + Hstp * Hstp; // check proximity on this artifact
            aim = (int) Math.round(MyMath.aTan0(Hstp, Vstp)); // degs c-wise from north
            PreViewLoc = locx;                          // .. from artifact to car
            PreViewAim = aim;
        } //~if
        else aim = PreViewAim;
        Hx = info & 0x1FF; // view angle, 0= center facing as spec'd (N=0)
        if (Vx < 0) { // (un)lit luminary..
            if (Hx == 0) Vx = 360; // unspec'd direction = full view range
            else Vx = 60;
        } //~if // otherwise assume 60 degrees
        else if (Vx < 6) Vx = 360; // view range < 6 degs, assume 360
        Vx = Vx >> 1;
        Hx = Hx - aim; // view angle, 0= center facing as spec'd (N=0)
        while (Hx > 180) Hx = Hx - 360; // ..so Hx is angle from center to right
        while (Hx + 180 < 0) Hx = Hx + 360; // ..=angle from mid-view (f.north)
        res = (Vx - Hx) | (Vx + Hx); // Vx>0 so never: res=0
        if (doit) if (!IsProxim) // check proximity on this artifact..
            if (dx < ProxyThresh) IsProxim = true; // if (res>0) <-- gotta sense unseen!
        return res;
    } //~ViewAngle // rtn >0 if in view

    private int Animatron(int whar, int locn, boolean logy) { // for animated art
        String aLine = ""; // used if logz=T  // may also retn TmpI = updated whar
        final int Anim_Log = DriverCons.D_Anim_Log;
        boolean uppy = false, logz = false, skipper = false, prox = false;
        int why = 0, kx = 0, zx = 0, nx = 0, here = 0, thar, info,
                seen = locn, anim = (locn >> 28) & 15;
        int[] theInx = MapIndex;
        locn = locn & 0x0FFFFFFF; // whar = nx*4+ArtBase; whar -> locn
        TmpI = 0; // (Anim8) anim whar == locn = why +FrameNo k=kx z=zx n=nx
        // ../TimeBaseSeq t=RealTimeNow/FakeRealTime ClockTimed/TimeOnly -> TmpI
        // \n  skipper o OpMode/StepOne  xTrLiteTime ---> AnimInfo \n @@ aLine
        if (anim > 0) while (true) {
            why++; // why = 1
            if (anim > 4) { // one-shot indexed movable stuff (setup @ [%]f)..
                why = 3;
                if (MapLogged) if ((Qlog & 32) != 0)
                    if (Anim_Log > 0) if ((Anim_Log >> 16) == anim)
                        if ((Anim_Log & 0xFFFF) > FrameNo) logz = Mini_Log; // change? to GoodLog;
                TmpI = (whar << 16) + whar + 1; // anchors are only one word long
                locn = -5;
                here = AnimInfo[anim & 15]; // -> this object's sequence list
                kx = here; // (for log)
                if (here <= 0) break; // why = 3
                why++; // why = 4
                while (true) { // fetch img ref(w/locn) from anim'n list by frame +..
                    if (here <= 0) break; // why = +4
                    why++; // why = +5
                    if (theInx == null) break;
                    why++; // why = +6
                    if (here >= theInx.length) break;
                    zx = theInx[here]; // -> 1st/next image in this sequence, exp.time
                    if (logz) {
                        if (aLine == "") aLine = "\n  @@";
                        aLine = aLine + HandyOps.Dec2Log(" ", here,
                                HandyOps.Int2Log(":", zx, ""));
                    } //~if
                    kx = MyMath.SgnExt(zx); // expiration time in secs/8 (<1hr)
                    why++; // why = +7
                    if (kx > 0) if (kx < RealTimeNow) { // if (RealTimeNow>0)
                        why = (why & -32) + 36; // why = *32 +4
                        here++;
                        kx = here;
                        uppy = true; // " =^= "
                        logy = true;
                        continue;
                    } //~if
                    why++; // why = +8
                    if (uppy) if (here > 0) AnimInfo[anim & 15] = here;
                    here = zx >> 16; // -> image in separate animated artifact images
                    if (here <= 0) break;
                    why++; // why = +9
                    if (theInx == null) break;
                    if (here > theInx.length - 4) break;
                    zx = theInx[here] & 0x0FFFFFFF; // location of image in grid
                    info = theInx[here + 1];        // orientation & view range
                    if (info > 0) if ((info & 0x8000) != 0) {
                        prox = true;
                        info = info & ~0x8000;
                    } //~if
                    why++; // why = +10
                    if (zx == 0) break;
                    locn = zx;
                    thar = 0;
                    if (info == 0) thar = here;
                    else while (true) {
                        if (ViewAngle(here, locn, info, prox) > 0) { // >0 if good
                            thar = here;
                            break;
                        } //~if
                        here = here + 4;
                        if (here <= 0) break;
                        if (here > theInx.length - 4) break;
                        zx = theInx[here] & 0x0FFFFFFF;
                        if (zx != locn) break;
                        info = theInx[here + 1];
                        if (info != 0) continue;
                        thar = here;
                        break;
                    } //~while
                    why++; // why = +11
                    if (thar > 0) {
                        TmpI = (thar << 16) + whar + 1;
                        why++;
                    } //~if // why = +12 (good)
                    else locn = -6; // why = +11 (none visi)
                    break;
                } //~while
                break;
            } //~if // (one-shot indexed movable stuff)
            else if (anim == 4) { // start up RealTime Base
                why = 32; // [from Fig-8] 0x48A4A0A4 = 4.A4=164.A.A4=164=20*8+4=20.5
                TmpI = (whar << 16) + whar + 2; // it's two words long: locn/time
                zx = (locn >> 12) & 15; // bits: +8=(V>), +4=(V<), +2=(H>), +1=(H<) [=8+2]
                kx = locn & 0xFFF0FFF;
                locn = -4;
                nx = 0;
                if (whar > 0) if (whar < theInx.length - 1) nx = theInx[whar + 1]; // [=1]
                why++; // why = 33
                if ((nx & -0x10000) > TimeBaseSeq) break;
                nx = (nx & 0xFFFF) << 3; // nx: restart +, added seconds, now s/8
                if (RealTimeBase != 0) break;
                seen = zx;
                why++; // why = 34
                if (zx > 3) {
                    info = ((int) Math.round(Vposn * 8.0)) - (kx >> 16);
                    if (zx > 7) { // bits: +8=(V>)
                        if (info > 0) seen = zx - 16;
                    } //~if
                    else if (info < 0) seen = zx - 16;
                } //~if // bits: +4=(V<)
                zx = zx & 3;
                if (zx > 0) {
                    info = ((int) Math.round(Hposn * 8.0)) - MyMath.SgnExt(kx);
                    if (zx > 1) { // bits: +2=(H>)
                        if (info > 0) zx = -zx;
                    } //~if
                    else if (info < 0) zx = -zx; // bits: +1=(H<)
                    if (zx < 0) { // if both H&V are tested, both must fire..
                        if (seen < 4) seen = seen | -16;
                    } //~if
                    else if (seen < 0) seen = seen & 15;
                } //~if // only V fired, so untrigger
                if (zx < 0) {
                    zx = TimeBaseSeq >> 16;
                    TimeBaseSeq = TimeBaseSeq + 0x10000;
                    FakeTimeBase = FakeRealTime / 125 - nx; // FTB,RTB both in 1/8th secs..
                    RealTimeBase = HandyOps.TimeSecs(true) / 125 - nx;
                    if (GoodLog) System.out.println(HandyOps.Dec2Log("** TimBase ", FrameNo,
                            HandyOps.Fixt8th(" = ", FakeTimeBase,
                                    HandyOps.Fixt8th(" / ", RealTimeBase, HandyOps.Fixt8th(" +", nx,
                                            HandyOps.Dec2Log(" #", zx, HandyOps.PosTime(" @ ")))))));
                    zx = seen;
                } //~if
                else why++; // why = 35
                break;
            } //~if // why = 34/35 // (start up RealTime Base)
            else if (anim > 1) { // traffic light (see StopInfo)
                why = 13;
                locn = -2;
                if (theInx == null) break;
                why++; // why = 14
                if (whar < ArtBase) break; // whar should -> blanked-out light
                why++; // why = 15
                // if (((whar-ArtBase)&3) !=0) break; // (obsolete)
                skipper = true;
                if (ClockTimed) zx = HandyOps.TimeSecs(false);
                else zx = FakeRealTime >> 10; // FRT in ms
                zx = zx & ((2 << xTrLiteTime) - 1);
                why++; // why = 16
                for (nx = 0; nx <= 9; nx++) {
                    whar = whar + 4;
                    if (whar >= theInx.length) break; // why = +16
                    why++; // why = 17
                    info = theInx[whar];
                    if (info == 0) why++; // why = +18 (locn = -2)
                    else if (((seen - info) & 0x0FFFFFFF) != 0) why = why + 2; // why = +19
                    else if ((info & -0x10000000) != 0x10000000) {
                        // skip over different aspects (a=2/3)..
                        why = (why & -32) + 48; // why = *32 +16
                        continue;
                    } //~if
                    else locn = info & 0x0FFFFFFF; // why = +17 (good, 1st anim+1)
                    break;
                } //~for
                kx = (1 << xTrLiteTime) - 2;
                if ((((zx >> xTrLiteTime) + anim) & 1) == 0) kx = 8; // red
                else if ((zx & kx) == kx) kx = 4; // yellow
                else kx = 0; // green
                kx = whar + kx;
                if (locn > 0) {
                    why = why + 4; // => +21
                    if (kx > 0) if (kx < theInx.length) {
                        why = why + 4; // => +25 (good result)
                        info = theInx[kx];
                        if (info > 0) if ((info & -0x10000000) == 0x10000000)
                            TmpI = (kx << 16) + kx + 4;
                    } //~if
                    if (TmpI == 0) locn = locn - 0x80000000;
                }
            } //~if // (traffic light)
            else locn = -1; // if (anim==1) // animation skipover (unused phases)
            break;
        } //~while
        if (locn < 0) uppy = false;
        if (skipper) if (TmpI == 0) for (nx = 9; nx >= 0; nx += -1) {
            whar = whar + 4;
            if (whar < theInx.length) if (((theInx[whar] - seen) & 0x0FFFFFFF) == 0)
                continue;
            TmpI = (whar << 16) + whar;
            break;
        } //~for                // GoodLog = true, logy = Fax_Log = T..
        if (Mini_Log) if (logy) System.out.println(HandyOps.Dec2Log("(Anim8) ", anim,
                HandyOps.Int2Log(HandyOps.IffyStr(uppy, " =^= ", " =#= "), locn,
                        HandyOps.Dec2Log(" ", whar, HandyOps.Dec2Log(" = ", why,  // why =
                                HandyOps.Dec2Log(" #", FrameNo, HandyOps.IffyStr(anim == 0, "",
                                        HandyOps.Int2Log(" k=", kx, HandyOps.Int2Log(" z=", zx,
                                                HandyOps.Int2Log(" n=", nx, HandyOps.Int2Log("/", TimeBaseSeq,
                                                        HandyOps.Fixt8th(" t=", RealTimeNow, HandyOps.Dec2Log("/", FakeRealTime,
                                                                HandyOps.TF2Log(" ", ClockTimed,
                                                                        HandyOps.Int2Log(" -> ", TmpI, HandyOps.IffyStr(TmpI == 0, aLine,
                                                                                HandyOps.Int2Log("\n    ", seen, HandyOps.TF2Log(" ", skipper,
                                                                                        HandyOps.Int2Log(" ", here, HandyOps.Dec2Log(" o", OpMode,
                                                                                                HandyOps.TF2Log("/", StepOne, HandyOps.Dec2Log(" ", xTrLiteTime,
                                                                                                        " ---> " + HandyOps.ArrayDumpLine(AnimInfo, 0, 36)
                                                                                                                + aLine))))))))))))))))))))));
        return locn;
    } //~Animatron

    private void ShoArtifax() { // sort by distance, then display far-to-near..
        double fudge = 6.0; // (patch as needed)
        boolean doit = (OpMode == 2) || StepOne, prox, logz;
        // ZoomRatio = (50*64.0)/(Zoom35*fImMid); fImMid = ImWi/2 // zx50 = 28
        int Vbase = (int) Math.round(Vposn * 4.0), Hbase = (int) Math.round(Hposn * 4.0),
                pint = (int) Math.round(Facing), whar = ArtBase, here, Pmap, oops,
                topper, tole, tall, wide, rotst, nx, wx, yx, zx, looky = FrameNo - 1,
                dx = 0, kx = 0, seen = 0, ImgWi = 0,
                Vx = 0, Hx = 0, rx = 0, cx = 0, lxx = 0, far = 0, anim = 0, ppm = 0,
                tops = 0, lino = 0, whom = 0, locn = 0, info = 0, why = 0;
        boolean logy = false;
        String aLine = "";
        double step = 0.0, Vat = 0.0, Hat = 0.0, aim = 0.0, RoWiM = 0.0, fox = 0.0,
                ftmp;
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;
        int[] myFax = theFax;
        if (LookFrame > 0) looky = FrameNo - LookFrame;
        if (looky == 0) logy = Fax_Log; // Fax_Log=T: detailed log
        while (true) {
            why++; // why = 1
            if (NumFax <= 0) break; // calc'd by InitIndx
            why++; // why = 2
            if (theInx == null) break;
            PreViewAim = 0;
            PreViewLoc = 0;
            far = theInx.length;
            ImgWi = MyMath.SgnExt(theInx[0]);
            if (theInx.length > 4) dx = theInx[2];
            why++; // why = 3
            if (ImgWi < 64) break;
            if ((ImgWi & 15) != 0) break;
            why++; // why = 4
            if (myFax == null) break;
            why++; // why = 5
            if (theImgs == null) break;
            why++; // why = 6
            if (VuEdge == 0.0) break; // = half-width of view, in degs
            why++; // why = 7
            if (TrakNoPix) break;
            why++; // why = 8
            fox = ZoomRatio * fudge;
            lxx = theImgs.length;
            if (RealTimeBase != 0) if (doit) { // RTN in seconds/8..
                if (StepOne) RealTimeNow = (FakeRealTime / 125 - FakeTimeBase);
                else RealTimeNow = HandyOps.TimeSecs(true) / 125 - RealTimeBase;
            } //~if
            while (pint > 360) pint = pint - 360; // normalize range for later use
            while (pint < 0) pint = pint + 360;
            if (dx == 0) yx = 11;
            else yx = dx + 2;           // if (Qlog..) -- included in M_L..
            if (Mini_Log) if (logy) if ((Qlog & 1) != 0) if ((Qlog & 768) != 0)
                System.out.println("An8 =-=" + HandyOps.ArrayDumpLine(theInx, yx, 5));
            for (nx = NumFax - 1; nx >= 0; nx += -1) { // calc in-view, distance
                prox = false;
                why++; // why = 9
                here = whar; // whar = nx*4+ArtBase;
                whar = whar + 4;
                if (here < 0) break;
                why++; // why = 10
                if (here > theInx.length - 8) break; // can't
                why++; // why = 11
                locn = theInx[here]; // locn in 25cm units; anim = locn>>28
                info = theInx[here + 1]; // range of view, view angle in degrees
                if (info > 0) if ((info & 0x8000) != 0) {
                    prox = true;
                    info = info & ~0x8000;
                } //~if
                yx = theInx[here + 4]; // look-ahead for alternate view
                seen = locn;
                anim = (locn >> 28) & 15; // is this in view angle?..
                if (anim < 4) if ((info > 0) || ((info & 0x1FF) != 0))
                    if (ViewAngle(here, locn, info, prox) < 0) {
                        if (((yx - locn) & 0x0FFFFFFF) == 0) continue;
                        locn = -1; // last one not visi, hide it
                        anim = 0;
                    } //~if
                if (info < 0) locn = locn & 0x0FFFFFFF; // ceiling light
                else if (anim != 0) {
                    locn = Animatron(here, locn, logy); // (see StopInfo)
                    if (TmpI > 0) {
                        whar = TmpI & 0xFFFF; // next up
                        here = TmpI >> 16;
                    }
                } //~if // here -> actual data
                why = 8;
                if (locn == 0) break; // (normal exit); <0: obj del'd by Animatro
                step = 0.0;
                aim = 0.0;
                cx = 0;
                dx = 0;
                Vx = 0;
                Hx = 0;
                tall = 0;
                wide = 0;
                if (locn > 0) if (locn != whom) { // duplicate locn = alt view
                    whom = locn;
                    // info = locn-(Vbase<<16)-Hbase; // (for log) V/Hbase in 25cm units
                    tall = (locn >> 16) - Vbase; // position of artifact, relative to car
                    wide = (locn & 0xFFF) - Hbase; // ..measured in half-meters (50cm)
                    aim = MyMath.aTan0(MyMath.Fix2flt(wide, 0), -MyMath.Fix2flt(tall, 0));
                    aim = aim - Facing; // aim is angle of deviation from center-view
                    while (aim > 180.0) aim = aim - 360.0;
                    while (aim + 180.0 < 0.0) aim = aim + 360.0;
                    Vx = tall * tall; // (includes 4 bits of fractional square meters)
                    Hx = wide * wide;
                    if (aim > VuEdge) dx = -8; // not in view
                    else if (aim + VuEdge >= 0.0) { // dx = radial distance, cx = pix H-posn..
                        step = Math.sqrt(MyMath.Fix2flt(Vx + Hx, 4)) * 16.0; // fImMid=ImWi/2..
                        ftmp = aim * fImMid / VuEdge;
                        if (true)
                            if ((aim > 4.0) || (aim + 4.0 < 0.0)) {
                                MyMath.Angle2cart(aim); // off-center, adjust distance..
                                ftmp = ftmp * MyMath.Cose;
                                step = step * MyMath.Cose;
                            } //~if // so same pix row as if center-view
                        cx = (int) Math.round(ftmp + fImMid);
                        if (cx < 0) {
                            cx = 0;
                            dx--;
                        } //~if // dx = -1 (don't show)
                        else dx = (int) Math.round(step);
                    } //~if // (incl 4 bits frac'l pk m)
                    else dx = -8;
                    if (lino > myFax.length - 2) break; // (can't)
                    myFax[lino] = here;
                    myFax[lino + 1] = (dx << 16) + cx; // dx in 6cm units, cx in pixels
                    lino = lino + 2;
                } //~if // (duplicate locn = alt view)
                if (Mini_Log) if (logy) if ((Qlog & 32) != 0)
                    System.out.println(HandyOps.Dec2Log("  (;;) ", nx,
                            HandyOps.Dec2Log(" +", lino, HandyOps.Dec2Log(" = ", here,
                                    HandyOps.Dec2Log(" ", dx, HandyOps.Dec2Log("/", cx,
                                            HandyOps.Int2Log(" => ", locn, // HandyOps.Int2Log(" ",info,
                                                    HandyOps.Dec2Log(" ", tall, HandyOps.Dec2Log("/", wide,
                                                            HandyOps.Dec2Log(" ", Vx, HandyOps.Dec2Log("/", Hx,
                                                                    HandyOps.Flt2Log(" ", aim, HandyOps.Flt2Log(" = ", step,
                                                                            HandyOps.Int2Log(" ", seen, ""))))))))))))));
            } //~for // (nx)
            if (why > 8) break;                        // (nx: calc in-view)
            if (Mini_Log) if (logy) {
                aLine = "  (sort) ";
                if (Qlog < 0)
                    aLine = aLine + HandyOps.ArrayDumpLine(myFax, lino, 9) + "\n   .... ";
            } //~if
            why = 12;
            whar = lino - 2; // whar is + arts *2
            if (whar > 0) for (nx = whar; nx >= 2; nx += -2)
                for (lino = 2; lino <= nx; lino += 2) { // sort..............
                    if (lino < 2) break;
                    if (lino > myFax.length - 2) break; // (can't)
                    Vx = myFax[lino - 1];
                    Hx = myFax[lino + 1];
                    if (Vx <= Hx) continue; // bubble sort is good enough for few arts..
                    myFax[lino - 1] = Hx;
                    myFax[lino + 1] = Vx;
                    info = myFax[lino];
                    oops = myFax[lino - 2];
                    if (Mini_Log) if (logy)
                        aLine = aLine + HandyOps.Dec2Log(" ", lino,
                                HandyOps.Int2Log(":", info, HandyOps.Int2Log("`", Hx,
                                        HandyOps.Int2Log("/", oops, HandyOps.Int2Log("`", Vx, "")))));
                    myFax[lino - 2] = info;
                    myFax[lino] = oops;
                } //~for
            if (Mini_Log) if (logy) if ((Qlog & 32) != 0) {
                aLine = aLine + "\n    ";
                System.out.println(aLine + " => "
                        + HandyOps.ArrayDumpLine(myFax, whar + 2, 8));
            } //~if
            why++; // why = 13
            RoWiM = 0.0; // (whazzis? logged, but unused)
            for (nx = whar; nx >= 0; nx += -2) { // draw them (no cont, 3brk @ front, -1brk @ end)..
                step = 0.0;
                oops = 0;
                why++; // why = 14
                if (nx > myFax.length - 2) break; // (can't)
                whar = myFax[nx]; // now it points to this artifact
                why--; // why = 13
                dx = myFax[nx + 1];   // oops=1 step<0           01000 off left end
                if (dx < 0) {         //  00002 step>64          02000 off top of screen
                    oops = 0x80000;   //  00004 Pmap+wide >= lxx 04000 miscalc: off im top
                    dx = 0;
                } //~if       //  00008 tops<0           08000
                Pmap = 0;           //  00010 tall <= 0        10000 wall went to ceil
                tops = 0;           //  00020 wide <= 0        20000 art below wall
                anim = 0;           //  00040 ppm==0           40000 behind wall
                tole = 0;           //  00080 + H-cols < 0     80000 dx<0 (not visi)
                tall = 0;           //  00100 off right end
                wide = 0;           //  00200
                ppm = 0;            //  00400
                kx = 0;             //  00800 ceiling lite off top
                Vx = 0;
                cx = dx & 0xFFFF;
                dx = dx >> 16; // in 6cm units, max 6.4K
                rx = MyMath.iMin(dx, 2047); // RR[] indexed by park 6cm units..
                rx = RangeRow[rx & 2047]; // convert dist => frac'l screen row (MkRngTb)
                rotst = rx << 20;
                rx = (rx >> 3) + ImHaf; // RnRo in frac'l scrn rows, rx>>3 is one actual ro
                yx = 0; // >0 is a good ref
                if (whar > 0) if (whar < theInx.length - 8) {
                    yx++; // >0 is a good ref
                    locn = theInx[whar];
                    anim = (locn >> 28) & 15; // [logged only] now also sele's ceiling lite
                    locn = locn & 0x0FFFFFFF;
                    if (anim == 1) {
                        kx = theInx[whar + 1];
                        if (kx < 0) kx = kx >> 16; // <0, = implied img pix below
                        else kx = 0;
                    } //~if
                    Pmap = theInx[whar + 2]; // image offset & ppm (pix/meter)
                    tall = theInx[whar + 3];
                } //~if // image H & W
                if (Mini_Log) {
                    if (logy) aLine = "  (::)" + HandyOps.Dec2Log(" ", nx,
                            HandyOps.Dec2Log(" = ", dx, HandyOps.Dec2Log(" => ", rx,
                                    HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" @ ", whar,
                                            HandyOps.Dec2Log(" #", anim, HandyOps.Dec2Log(" ", kx,
                                                    HandyOps.Dec2Log(" o", OpMode,
                                                            HandyOps.IffyStr(oops > 0, " d<0", "")))))))));
                    TempStr = "";
                } //~if
                if (whar > 0) { //                                             (ShoArtifx)
                    if (oops == 0) if (((anim - 1) & 31) > 3) // can't crash into traffic lights
                        if (dx < 7) SimStep(5); // should also crash if grazes corner (CrashMe)
                    if (ShowMap) { // map coord in meters, cell center..
                        info = ZoomMapCoord(true, MyMath.Fix2flt(locn >> 16, 2),
                                MyMath.Fix2flt(locn & 0xFFF, 2));
                        zx = info & 0xFFF;
                        info = info >> 16;
                        if (info > 0) { // put amber dot there in close-up map..
                            PokePixel(ArtiColo, info, zx + 1);
                            PokePixel(ArtiColo, info, zx);
                            info++;
                            PokePixel(ArtiColo, info, zx + 1);
                            PokePixel(ArtiColo, info, zx);
                        }
                    } //~if // (whar>0)
                    zx = 0;
                    if (DoCloseUp) if (RoWiM == 0.0) {
                        zx = (rx - ImHaf) << 2; // ImHaf = ImHi/2
                        if (pint < 8) zx++; // car facing N/S (use H)
                        else if (pint < 82) zx = 0; // not square-on
                        else if (pint < 98) {
                        } // car facing E/W (use V)
                        else if (pint < 172) zx = 0;
                        else if (pint < 188) zx++;
                        else if (pint < 262) zx = 0;
                        else if (pint < 278) {
                        } else if (pint < 352) zx = 0;
                        else zx++;
                    } //~if // (RoWiM=0) // (RM[] in grid=2m)..
                    if (zx > 0) if (RasterMap != null) if (zx < RasterMap.length - 3)
                        RoWiM = fImMid / MyMath.fAbs(RasterMap[zx + 2] - RasterMap[zx]);
                    wx = kx; // (for log)
                    if (tall > 0) { // calc artifact drawing spex..
                        ppm = (Pmap >> 24) & 255; // SS ppm = 44
                        Pmap = Pmap & 0x00FFFFFF;
                        wide = -MyMath.SgnExt(tall);
                        zx = tall >> 16;
                        // anim = zx>>12; // logged only (also test for unreasonableness)
                        zx = zx & 255;                   // ImMid = ImWi/2..
                        if (ppm > 0) tole = (zx << 4) / ppm; // pk meters*16
                        tops = Pmap - (zx - 1) * ImgWi - wide;   // dx = dist in 6cm units..
                        // ZoomRatio = (50*64.0)/(Zoom35*fImMid) = 3200/(35*160) = 20/35
                        // [dx=13] (3x1m dist) st = f*(13*44=572*20/35)/1024 = 5720/(280*64)..
                        step = MyMath.Fix2flt(dx * ppm, 12) * fox; // ..fox=ZR*2; st=20*2/64 =5/8
                        // step = fractional im.pix per scr.pix
                        if (kx < 0) { // -kx is implied file pix below img to grnd (else kx=0)
                            if (ppm > 0) tole = (-kx << 4) / ppm + tole; // pk meters*16
                            kx = (int) Math.round(MyMath.Fix2flt(kx, 0) / step);
                        }
                    } //~if // kx: scrn px
                    if (Mini_Log) if (logy) if (Qlog < 0)
                        System.out.println(HandyOps.Dec2Log("(``) ", nx,
                                HandyOps.Dec2Log(" @ ", whar, HandyOps.Int2Log(" => ", locn,
                                        HandyOps.Flt2Log(" (", fImMid, HandyOps.Flt2Log("/", VuEdge,
                                                HandyOps.Flt2Log(" ", step, HandyOps.Dec2Log(" ", tole,
                                                        HandyOps.Int2Log(") ", tall, HandyOps.Int2Log(" -> ", Pmap,
                                                                HandyOps.Dec2Log(" ", kx, HandyOps.Dec2Log("/", wx,
                                                                        HandyOps.IffyStr(RoWiM == 0.0, "",
                                                                                HandyOps.Flt2Log(" p/m=", RoWiM, "")))))))))))))
                                + HandyOps.IffyStr(oops > 0, " d<0", ""));
                    // whar = whar-ppm;
                    Hat = 0.0;
                    if (tall > 0) { // draw this artifact (unless oops>0)..
                        tall = zx; // = tall>>16;
                        if (kx + rx < 0) oops = oops | 2048; // ceiling light is off top
                        if (ppm == 0) oops = oops | 64;
                        if (wide <= 0) oops = oops | 32;
                        if (tall <= 0) oops = oops | 16;
                        if (tops < 0) oops = oops + 8;
                        if (Pmap + wide >= lxx) oops = oops + 4;
                        if (step > 64.0) oops = oops + 2;
                        if (step < 0.01) oops++;
                        tall = MyMath.Trunc8(MyMath.Fix2flt(tall, 0) / step) + 1; // + V-rows
                        zx = MyMath.Trunc8(MyMath.Fix2flt(wide, 0) / step) + 1; // + H-cols/2
                        cx = cx - 1 - zx; // off left end of image on screen (cx = dx&0xFFFF)
                        Hat = 4096.5 - MyMath.Fix2flt(zx, 0) * step; // +MyMath.Fix2flt(Pmap,0)
                        if (zx < 0) {
                            oops = oops | 128;
                            zx = 0;
                        } //~if
                        for (zx = zx + zx; zx >= 0; zx += -1) { // outer loop is horizontal..
                            cx++;                // if (oops>0) log only, then exit
                            Vx = MyMath.Trunc8(Hat) + Pmap - 4096;
                            Hat = Hat + step;
                            Vat = 0.0;
                            wx = 0;
                            topper = 0;
                            if (cx >= ImWi) oops = oops | 256; // off right end;  off left end..
                            else if (cx < 0) oops = oops | 0x1000; // kill inner loop, not outer
                            else if (SeenWall != null) if (SeenWall[LayerSz] > 0) {
                                for (yx = cx; yx <= LayerSz - 1; yx += ImWi) {
                                    wx = SeenWall[yx];
                                    if (wx == 0) break;    // tole = (tall<<4)/ppm; rotst = rx<<20..
                                    if (wx < rotst) continue; // wall is behind, might be another
                                    if ((wx & -0x100000) == rotst) // at same distance..
                                        if (tole > 0) if (WalzTall != null) {
                                            // (probly can't tole=0, cuz tall,ppm>0)
                                            wx = WalzTall[(wx >> 12) & 255]; // max wx = 0x2xxxx (@ pk edge)
                                            if (wx > 0x10000) continue; // wall at pk edge, art is front
                                            if (wx <= 0) continue; // always wx>0, so shouldn't hap
                                            if (tole > (wx & 0xFFFF)) wx = wx | 0x80000;
                                        } //~if // art is taller
                                    info = SeenWall[yx + ImWi * MaxLayers]; // MaxLayers = 4 (wall top)
                                    if (info < 0) {
                                        // (wx&-0x100000)!=0 if wall is in front;
                                        // assume art is front of ceil-hi wall at same distance
                                        if (info + 1 == 0) if ((wx & -0x100000) == 0) continue;
                                        if (Mini_Log) if (logy) { // (looky==0) incl in logy
                                            if (TempStr == "") TempStr = "\n    ";
                                            TempStr = TempStr + HandyOps.Dec2Log(" Lyr=", yx / ImWi,
                                                    HandyOps.Dec2Log(" +", cx, HandyOps.Dec2Log(" [", yx,
                                                            HandyOps.Hex2Log("] ", rotst, 8, HandyOps.Int2Log(":", wx,
                                                                    HandyOps.Dec2Log(" ", info, ""))))));
                                        } //~if
                                        if (topper == 0) topper = info;
                                        else if (topper < info) topper = info;
                                        continue;
                                    } //~if // saved wall top overrules WalzTall
                                    if (wx > 0x40000) continue; //
                                    oops = oops | 0x40000; // behind wall (no exact wall hi)
                                    break;
                                } //~for
                                if (topper < 0) if (oops < 0x10000) { // art behind/below wall?
                                    topper = ~topper;
                                    if (topper == 0) oops = oops | 0x10000; // wall went to ceiling
                                    else if (topper + tall <= rx + kx) oops = oops | 0x20000;
                                    else topper = topper - 1 - (rx + kx); // >=0 draws
                                    if (Mini_Log) if (TempStr != "") TempStr =
                                            TempStr + HandyOps.Dec2Log(" => ", topper, "");
                                }
                            } //~if
                            if ((oops == 0) || Mini_Log && logy) for (dx = 0; dx <= tall + 12; dx++) {
                                yx = rx + kx - 0 - dx;          // do this column up (brk after log)..
                                if (yx < 0) oops = oops | 0x2000; // off top of screen
                                if (Vx < tops) oops = oops | 0x4000; // (miscalc: off top of img)
                                info = 0;
                                if (Vx > 0) if (Vx < theImgs.length) info = theImgs[Vx];
                                if (Mini_Log) if (logy) { // (looky==0) incl in logy
                                    logz = ((dx & 15) == 0); // always log 1st line, unless..
                                    if ((oops & 0x7007F) != 0) logz = true; // (I need to see this)
                                    else if (yx == TripLine) logz = true;
                                    else if (oops == 0) if (dx > 3) if (dx + 5 < tall) if (info < 0)
                                        logz = false;
                                    if (logz) if ((Qlog & 32) != 0)
                                        System.out.println(HandyOps.Dec2Log("  _ _ ", dx,
                                                HandyOps.Dec2Log("/", zx, HandyOps.Dec2Log(" ", rx,
                                                        HandyOps.Dec2Log(" (", topper, HandyOps.Dec2Log(") ", yx,
                                                                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" @ ", Vx,
                                                                        HandyOps.Flt2Log(" ", Vat * 16.0, HandyOps.Int2Log(" ", wx,
                                                                                HandyOps.Dec2Log(" ", tall, HandyOps.Hex2Log(" x", oops, 5,
                                                                                        HandyOps.IffyStr(oops != 0, " = x",  // tagged err: oops =
                                                                                                HandyOps.IffyStr(yx >= ImHi - DrawDash, " = ^", // off botm
                                                                                                        HandyOps.IffyStr(topper + yx + 1 < 0, " = ~",    // behind wall
                                                                                                                HandyOps.IffyStr(info < 0, " = _",           // transparent
                                                                                                                        HandyOps.Colo2Log(" = ", info, ""))))))))))))))))
                                                + TempStr);
                                    TempStr = "";
                                } //~if
                                if (oops != 0) break; // otherwise commit..
                                topper++; // topper<0 behind a wall (=0 if no wall)
                                if (topper > 0) if (info >= 0) if (yx < ImHi - DrawDash)
                                    PokePixel(info, yx, cx);
                                Vat = Vat + step;
                                while (Vat >= 1.0) {
                                    Vx = Vx - ImgWi;
                                    Vat = Vat - 1.0;
                                }
                            } //~for // dx (this column up)
                            if ((oops & 0xC0FFF) != 0) break;
                            oops = 0;
                        }
                    } //~if //~for zx (outer loop is horizontal) (tall>0)
                    if (Mini_Log) if (logy) aLine = aLine + HandyOps.Dec2Log(" ++ ", anim,
                            HandyOps.Dec2Log(" ", tall, HandyOps.Dec2Log("/", wide,
                                    HandyOps.Dec2Log(" *", ppm, HandyOps.Dec2Log(" @ ", Pmap,
                                            HandyOps.Dec2Log(" ", ImgWi, HandyOps.Dec2Log(" ", tops,
                                                    HandyOps.Flt2Log(" st=", step * 16.0, HandyOps.IffyStr(oops == 0, "/16",
                                                            HandyOps.Hex2Log(" x", oops, 4, ""))))))))));
                } //~if
                if (Mini_Log) {
                    if (aLine != "") if ((Qlog & 32) != 0)
                        if (looky == 0) System.out.println(aLine); // "  (::) "
                    TempStr = "";
                } //~if
                if (oops >= 0x80000) break; // all the rest are out of view
                if (false) if (OpMode == 3) break;
            } //~for // nx (draw them)
            if (why > 13) break;
            why = 0;
            break;
        } //~while              // HandyOps.IffyStr(!logy,"",   // why =
        if ((why > 0) || Mini_Log) if (((Qlog & 32) != 0) || ((Qlog & 2048) != 0) && (looky == 0))
            if (looky == 0) System.out.println(HandyOps.Dec2Log(" (ShoArt) ", why,
                    HandyOps.Flt2Log(" ", ZoomRatio * 256.0, HandyOps.Dec2Log("/256 ", whar,
                            HandyOps.Dec2Log(" ", NumFax, HandyOps.Dec2Log(" ", lino,
                                    HandyOps.Dec2Log(" ", ImgWi, HandyOps.Dec2Log(" ", far,
                                            HandyOps.Flt2Log(" ", fudge, HandyOps.Dec2Log(" o", OpMode,
                                                    HandyOps.TF2Log("/", StepOne, HandyOps.Fixt8th(" t=", RealTimeNow,
                                                            HandyOps.Dec2Log("=", FakeRealTime, HandyOps.Fixt8th("-", FakeTimeBase,
                                                                    HandyOps.Flt2Log(" ", fox, "")))))))))))))));
    } //~ShoArtifax

    private int TossLayer(int bakk, int info, int inv, int skey, int cx,
                          boolean logz) {     // whar: insert here; whom: delete here..
        int whar = MaxLayers + 1, whom = bakk, ix0, ix1, ix2, tall = 0, thar = 0,
                why = 0;
        if (WaLayerz == null) whar--;
        else try {
            if (info == 0) whar = 0; // whar is insertion layer as spec'd or discovered
            // while there are more img walls than unused layers,
            // .. remove backmost shorter than front layer wall,
            // ..   or if all are taller, remove backmost
            // if info>0 then whar is the prior layer to push back & replace,
            // .. (or whar>MaxLayers if behind all); whom=whar removes info,
            // .. otherwise remove whom (whom<whar<MaxLayers removes whom+1).
            // .. info=0 means we want to insert at back (push others forward to deln)
            for (thar = MaxLayers - 1; thar >= bakk; thar += -1) { // 2 brk, 2 cont  // known: deep=MaxLayers
                if (thar < 0) break; // can't
                if (thar >= MaxLayers) break;
                ix2 = WaLayerz[thar + MaxLayers * 2]; // = sort key for this layer
                ix1 = WaLayerz[thar + MaxLayers] & -0x10000; // = nominal height of this wall
                if (((skey ^ ix2) & -256) == 0) { // same row, same wall height: ignore new
                    whar = MaxLayers; // no-insert = no-delete
                    why = ~why;
                } //~if
                if (Mini_Log) if (logz) if (cx > 0) if (((TripLine + cx) & 0xFFFF) == 0)
                    System.out.println(HandyOps.Dec2Log("     _._. ", why,
                            HandyOps.Dec2Log("/", thar, HandyOps.Int2Log(": ", ix1,
                                    HandyOps.Int2Log(" ", ix2, HandyOps.Int2Log(" : ", skey,
                                            HandyOps.Dec2Log(" => ", whar, HandyOps.Dec2Log("/", whom,
                                                    HandyOps.Int2Log(" ", tall, "")))))))));
                if (why < 0) break; // same row, same wall height: done
                if (ix1 == 0) continue; // (shouldn't hap) ix1: either m*16 or 0x3000=inf
                if (whar > MaxLayers) if (skey > ix2) // del or push behind ins'n..
                    whar = thar; // thar = 0<=bakk..MaxLayers-1
                if (tall == 0) { // always only 1st iteration (thar=MaxLayers-1)..
                    tall = ix1;
                    if (info == 0) continue; // (plain wall insertion: unknown)
                    if (whar == thar) {
                        tall = inv & -0x10000;
                        if (ix1 <= tall) {
                            whom = thar;
                            tall = ix1;
                        }
                    } //~if
                    else if (inv < tall) {
                        tall = inv & -0x10000;
                        whom = whar;
                    }
                } //~if
                else if (ix1 <= tall) { // every other iteration can be del'n candidate..
                    whom = thar; // ..dele shortest (or last =) behind & shorter than front
                    tall = ix1;
                } //~if
                why++;
            } //~for
            if (why >= 0) { // don't need to delete if not inserting
                if (whar > MaxLayers) whar = bakk; // inserting behind all (bakk = 0/1)
                if (whom != whar) Shifty = true;
                if (whom < whar) for (thar = whom + 1; thar <= whar; thar++) { // shift from ins'n to del'n..
                    if (thar <= 0) break; // can't
                    if (thar >= MaxLayers) break;
                    WaLayerz[thar + MaxLayers * 2 - 1] = WaLayerz[thar + MaxLayers * 2];
                    WaLayerz[thar + MaxLayers - 1] = WaLayerz[thar + MaxLayers];
                    WaLayerz[thar - 1] = WaLayerz[thar];
                    why = why + 0x10000;
                } //~for
                else if (whom > whar) for (thar = whom - 1; thar >= whar; thar += -1) {
                    if (thar < 0) break; // can't
                    if (thar > MaxLayers - 2) break;
                    ix1 = WaLayerz[thar + MaxLayers];
                    WaLayerz[thar + MaxLayers * 2 + 1] = WaLayerz[thar + MaxLayers * 2];
                    WaLayerz[thar + MaxLayers + 1] = ix1;
                    WaLayerz[thar + 1] = WaLayerz[thar];
                    why = why - 0x10000;
                } //~for
            } //~if
            if (Mini_Log) if (logz) System.out.println(HandyOps.Dec2Log("(TosLay) ", cx,
                    HandyOps.Dec2Log(" (", bakk,
                            HandyOps.Int2Log(") ", info, HandyOps.Int2Log(" ", inv,
                                    HandyOps.Int2Log(" ", skey, HandyOps.Int2Log(" => ", whar,
                                            HandyOps.Dec2Log("/", whom, HandyOps.TF2Log(" ", Shifty,
                                                    HandyOps.Int2Log(" ", tall, HandyOps.Dec2Log(" ", thar,
                                                            HandyOps.Int2Log(" # ", why,      // why counts for-loop iter'ns
                                                                    "\n    => " + HandyOps.ArrayDumpLine(WaLayerz, 0, 7)))))))))))));
        } catch (Exception ex) {
            whar = MaxLayers + 3;
        }
        return whar;
    } //~TossLayer // whar = 0..MaxLayers+3|0x10000

    private int SortLayer(boolean ImgWall, int info, int cx, int inv,
                          int doing, int whoz, int logy) { // only from DoWall (logy=3 if looky=0)
        int whar = MaxLayers, rx = info & -0x100000, ix3 = (info >> 12) & 255,
                ix0 = TripLine >> 16, ix1 = 0, ix2 = 0, colo = 0, here = 0, thar = 0,
                deep = 0, more = 0, skey = 0, doit = 0, logx = 0, xx = 0, nx = 0,
                whom = 0, why = 0;                             // inv>0 = ip*256/sp
        boolean logz = false;
        try {
            if (Mini_Log) if ((Qlog & 256) != 0)
                if ((logy > 0) || (TripLine < 0) || (doing == whoz)) {
                    if ((cx & 15) == 0) logz = true;
                    else if (((TripLine + cx) & 0xFFFF) == 0) { // (TripLine+cx==0)
                        logz = true;
                    } //~if
                    else if (ix0 + 1 < 0) if (ix0 + cx == 0) logz = true;
                } //~if
            Shifty = false; // (object var) T: layer(s) mod'd, copy WaLayerz -> SeenWall
            while (true) {
                why++; // why = 1
                if (info <= 0) break;
                if (ix3 == 0) break; // no BG+
                why++; // why = 2 // (already checked by caller..)
                if (SeenWall == null) break;
                if (cx < 0) break;
                if (cx >= ImWi) break;
                ix0 = SeenWall[cx];
                // inv = MyMath.iAbs(inv); // obsolete)
                why++; // why = 3 // new layer spex..
                whom = 0;
                // (new) General strategy: layers sorted by frontness, layer 0 is backmost
                // rx > defines front; if rx =, then + > usually defines front,
                // .. except walls w/rx= not near park edge, shorter is deemed front.
                // Plain wall hides any lower rx, so always goes in layer 0;
                // .. frontmost img wall goes in layer 2;
                // sort remaining img walls by frontness;
                // while there are more img walls than unused layers,
                // .. remove backmost shorter than front layer wall,
                // ..   or if all are taller, remove backmost

                if (WalzTall == null) break; // why = 3 (see "(WalzTal)")
                if (WaLayerz == null) { // alloc only once, for speed..
                    WaLayerz = new int[MaxLayers * 3 + 1];
                    if (WaLayerz == null) break;
                } //~if
                why++; // why = 4
                for (nx = MaxLayers * 3; nx >= 0; nx += -1) WaLayerz[nx] = 0;
                for (nx = cx; nx <= cx + ImWi * (MaxLayers - 1); nx += ImWi) { // copy SeenWal -> WaLayer..
                    if (nx < 0) break;
                    if (nx >= LayStep) break; // LayStep=ImWi*MaxLayers
                    whom = SeenWall[nx];
                    if (whom == 0) break;
                    colo = SeenWall[nx + LayStep];
                    ix0 = (whom >> 12) & 255;
                    skey = whom & -0x100000 | ix0; // plain walls & near-edge sort by rx+BG+
                    if (ix0 > 0) { // closer (larger) row + (=rx) counts higher..
                        ix1 = WalzTall[ix0 & 255]; // shorter (less tall) counts higher
                        if (ix1 < 0x3000) // otherwise: both plain walls & near-edge
                            // format for plain wall: 12K = (BGn<<4)+0x3000 => rx000BGn
                            // img wall: (<4K) = pk meters*16, +0x20000 if close to park edge
                            skey = ((0xFFF & ~ix1) << 8) + skey;
                    } //~if // rrrhhh++
                    if (deep > 0) if (colo >= 0x30000000) logx = logx | 1;
                    if (logz)
                        System.out.println(HandyOps.Dec2Log("    ,,, ", cx, // <=== log prior walls
                                HandyOps.Dec2Log("/", nx, HandyOps.Dec2Log(" [", deep,
                                        HandyOps.Int2Log("] = ", whom, HandyOps.Int2Log(" ", colo,
                                                HandyOps.Int2Log(" ", skey, HandyOps.Int2Log(" ", ix1,
                                                        HandyOps.IffyStr((deep & -MaxLayers) == 0, "", " !")))))))));
                    if (deep < 0) break;
                    if (deep >= MaxLayers) break;
                    WaLayerz[deep] = whom;
                    WaLayerz[deep + MaxLayers] = colo;
                    WaLayerz[deep + MaxLayers * 2] = skey;
                    deep++;
                } //~for // (nx: copy SeenWal -> WaLayer)
                if (!ImgWall) { // plain wall goes in layer 0..
                    nx = 0;
                    xx++; // xx = 1;
                    colo = info & 0xFFF;
                    inv = (colo | 0x3000) << 16; // infinite wall height
                    whom = WaLayerz[0]; // whom is logged
                    if (deep > 0) { // deep is the number of prior layers
                        if ((whom & 0xFFF) >= 4080) { // existing plain wall..
                            if (rx <= whom) break; // why = 4: new is behind (unshow)
                            nx++;
                        } //~if // new replaces prior layer 0, but check the rest
                        else if (rx > whom) nx++; // ditto (new plain wall is in front)
                        else nx--;
                    } //~if // push layer 0 up
                    why++; // why = 5
                    whar = 0;
                    if (nx == 0) break; // no other walls to check (whom=WaLayerz[0])
                    why++; // why = 6
                    Shifty = true; // T: layer(s) mod'd, copy WaLayerz -> SeenWall
                    if (nx < 0) { // new wall is behind prior walls..
                        if (deep >= MaxLayers) {
                            whom = TossLayer(0, 0, 0, 0, cx, logz); // = 0..MaxLayers+3|0x10000
                            if (whom > MaxLayers * 2) {
                                whom = whom & 255;
                                logx = logx | 2;
                            }
                        } //~if
                        else for (nx = deep - 1; nx >= 0; nx += -1) {
                            whom = nx; // whom is logged
                            if (nx < 0) break;
                            if (nx >= MaxLayers) break;
                            ix1 = WaLayerz[nx + MaxLayers];
                            if (ix1 >= 0x30000000) logx = logx | 8;
                            WaLayerz[nx + MaxLayers * 2 + 1] = WaLayerz[nx + MaxLayers * 2];
                            WaLayerz[nx + MaxLayers + 1] = ix1;
                            WaLayerz[nx + 1] = WaLayerz[nx];
                        } //~for
                        break;
                    } //~if // why = 6
                    whom = 0; // if (nx>0): new is in front of [0] (and maybe more)..
                    why++; // why = 7
                    thar++; // thar = 1;
                    here = deep;
                    for (nx = 1; nx <= deep; nx++) {
                        if (here == 0) break; // no more layers to kill
                        if (nx >= MaxLayers) break; // can't
                        whom = WaLayerz[nx];
                        if (rx < whom) { // keep this layer, it's more front..
                            if (thar >= MaxLayers) break; // can't
                            if (thar < 0) break;
                            WaLayerz[thar] = whom;
                            WaLayerz[thar + MaxLayers] = WaLayerz[nx + MaxLayers];
                            WaLayerz[thar + MaxLayers * 2] = WaLayerz[nx + MaxLayers * 2];
                            thar++;
                        } //~if
                        else here--;
                    } //~for // kill this layer
                    if (deep > here) for (nx = here; nx <= deep; nx++) { // list shrank, clear old stuff..
                        if (nx >= MaxLayers) break; // can't
                        if (nx < 0) break;
                        WaLayerz[nx] = 0;
                        WaLayerz[nx + MaxLayers] = 0;
                        WaLayerz[nx + MaxLayers * 2] = 0;
                    } //~for
                    deep = here;
                    break;
                } //~if // why = 7 // (plain wall)
                why = why * 10; // why = 40..70
                xx = 6; // thar = 0; // new is image wall..
                nx = WalzTall[ix3 & 255] << 8; // tall in low 14 bits (see "(WalzTal)")
                inv = (nx << 8) + inv;   // ..trimmed to max 4K (255m), shorter counts higher
                skey = rx + ix3; // both plain walls & near-edge
                if (nx < 0x300000) skey = (0xFFF00 & ~nx) + skey; // rrrhhh++
                why++; // why = x1     // .. closer (larger) row + (=rx) counts higher
                whom = WaLayerz[0]; // whom is logged
                if (deep == 0) { // new is 1st in this coln..
                    whar = 0;
                    break;
                } //~if
                why++; // why = x2
                if ((whom & 0xFFF) >= 4080) { // existing plain wall in layer 0..
                    if (skey < WaLayerz[MaxLayers * 2]) break; // new is behind it (unshow)
                    nx = whom & -0x100000;
                    if (rx < nx) break; // new (rx=info&-0x100000) is behind it (unshow)
                    thar++;
                } //~if
                why++; // why = x3
                whar = MaxLayers + 2; // =6: default, in case it fails (but shouldn't)
                if (deep <= 0) break; // (can't)
                if (deep >= MaxLayers) { // too many, toss something..
                    whar = TossLayer(thar, info, inv, skey, cx, logz);
                    if (whar > MaxLayers * 2) { // = 0..MaxLayers+3|0x10000
                        whar = whar & 255; // = 0..MaxLayers+3=7
                        logx = logx | 4;
                    } //~if
                    why++; // why = x4
                    break;
                } //~if
                for (nx = deep; nx >= thar; nx += -1) { // find where to insert new..
                    if (nx == thar) { // insert into (now vacated) bottom..
                        whar = nx; // nx = 0<=thar..deep<MaxLayers
                        break;
                    } //~if // why = x3
                    if (nx <= 0) break;
                    if (nx >= MaxLayers) break; // (can't)
                    ix1 = WaLayerz[nx - 1 + MaxLayers];
                    whom = WaLayerz[nx - 1 + MaxLayers * 2];
                    if (Mini_Log) if (logz) if (cx > 0) if (((TripLine + cx) & 0xFFFF) == 0)
                        System.out.println(HandyOps.Dec2Log("     ._._ ", nx,
                                HandyOps.Int2Log(": ", ix1, HandyOps.Int2Log(" ", whom,
                                        HandyOps.Int2Log(" : ", skey, "")))));
                    if (skey > whom) { // insert in front of this one (normal exit)
                        whar = nx; // nx = 0<=thar..deep<MaxLayers
                        break;
                    } //~if // why = x3
                    if (ix1 >= 0x30000000) logx = logx | 16;
                    WaLayerz[nx + MaxLayers * 2] = whom; // shove this forward, try behind it..
                    WaLayerz[nx + MaxLayers] = ix1;
                    WaLayerz[nx] = WaLayerz[nx - 1];
                    Shifty = true;
                } //~for // T: layer(s) mod'd, copy WaLayerz -> SeenWall
                if (whar >= MaxLayers) why = why + 2; // why = (x3)/x5
                break;
            }
        } catch (Exception ex) {
            whar = MaxLayers + 3;
        }
        if (whar >= 0) if (whar < MaxLayers) { // put item into its SeenWal layer..
            thar = 0;                          // (for result see "(SeeWal)" in log)
            if (whar > 0) if (inv >= 0x30000000) logx = logx | 128;
            if (logz || Shifty) { // only for log..
                WaLayerz[whar] = info;
                WaLayerz[whar + MaxLayers] = inv;
                WaLayerz[whar + MaxLayers * 2] = skey;
            } //~if
            ix0 = whar * ImWi + cx;
            if (Shifty) for (nx = cx; nx <= cx + ImWi * (MaxLayers - 1); nx += ImWi) { // copy WaLayer..
                if (nx < 0) break;
                if (nx >= LayStep) break; // LayStep=ImWi*MaxLayers
                if (thar < 0) break;
                if (thar >= MaxLayers) break;
                ix1 = WaLayerz[thar + MaxLayers];
                if (ix1 >= 0x30000000) if (nx >= ImWi) logx = logx | 32;
                SeenWall[nx + LayStep] = ix1;
                SeenWall[nx] = WaLayerz[thar];
                thar++;
            } //~for // (copy WaLayer)
            else if (ix0 >= 0) if (ix0 < LayStep) { // LayStep=ImWi*MaxLayers
                if (ix0 >= ImWi) if (inv >= 0x30000000) logx = logx | 64;
                SeenWall[ix0] = info;
                SeenWall[ix0 + LayStep] = inv; // = tall, vertical ipx*256/spx
                if (why < 0) why = why - 100;
                else why = why + 100;
            } //~if
            SeenWall[LayerSz]++;
        } //~if // (into its SeenWal layer) xx = 1/6
        else xx = 0;
        if ((Qlog & 256) != 0) {
            if (cx > 0) if (((TripLine + cx) & 0xFFFF) == 0) if (cx < ImWi)
                if (SeenWall[cx + ImWi + LayStep] >= 0x30000000) logx = logx | 256;
            if (logx > 0) logz = true;
        } //~if
        if (logy > 0) if (logz) {
            if (ImgWall) TempStr = HandyOps.Fixt8th("=", (inv >> 5) & 0x7FF, "] ");
            else TempStr = "] ";               // rx = info&-0x100000
            if (doing != whoz) RtnStr =
                    HandyOps.Dec2Log("/", whoz, " ");
            else RtnStr = " ";
            RtnStr = HandyOps.TF2Log(RtnStr, ImgWall, HandyOps.Dec2Log(" ", cx,
                    HandyOps.Int2Log(": ", info, HandyOps.Int2Log(" [", inv,
                            HandyOps.Int2Log(TempStr, skey, HandyOps.Dec2Log(" ", deep, //_10
                                    HandyOps.Int2Log(" ", nx, HandyOps.Int2Log(" ", whom,
                                            HandyOps.TF2Log(" ", Shifty, HandyOps.Dec2Log(" => ", xx,
                                                    HandyOps.Dec2Log("/", whar, " = ")))))))))));
            TempStr =
                    HandyOps.IffyStr(logx == 0, "\n     >> ",   // logx = (reason bits)..
                            HandyOps.Hex2Log("\n??   x", logx, 2, " >> "))
                            + HandyOps.ArrayDumpLine(WaLayerz, 0, 10);
            System.out.println(HandyOps.Dec2Log("(SrtLayr) ", doing,
                    HandyOps.Dec2Log(RtnStr, why, TempStr))); // why =
            TempStr = "";
            RtnStr = "";
        } //~if // (logy>0 && logz)
        if (Mini_Log) if (logy > 0) if (logx > 0) if ((Qlog & 256) != 0)
            if (cx > 0) if (cx < ImWi) {
                ix0 = SeenWall[cx + ImWi];
                ix1 = SeenWall[cx + ImWi + LayStep];
                System.out.println(HandyOps.Dec2Log("   (.?.) ", doing,
                        HandyOps.Dec2Log(" ", cx, HandyOps.Int2Log(": ", info,
                                HandyOps.Int2Log(" [", inv, HandyOps.Dec2Log("] => ", whar,
                                        HandyOps.Int2Log(": ", ix0, HandyOps.Int2Log(" ", ix1,
                                                HandyOps.IffyStr(ix1 < 0x30000000, "", " (.??.)")))))))));
            } //~if
        return (xx << MxLayShf) + whar;
    } //~SortLayer // whar = 0..MaxLayers+3=7

    private void DoWalls(boolean doit) { // no early rtn "{DW} "           // nBax
        final int HalfLoaf = Crummy / 2 - 3, CapDims = ImHi * 0x10000 + ImWi;
        boolean ImgWall = GroundsColors < 0, sawn;           // data frm BackImag
        int looky = FrameNo - 1, ImH8 = ImHaf * 8, ImgHi, ImgWi, ImgSz, stip, doing,
                topper, inv, nuly, full,
                xx = TmpI, rx, cx, yx = 0, zx = 0, kx = 0, nx = 0, pz = 0,
                fz = 0, maxy = 0, maxz = 0, ipx = 0, Pmap = 0, logy = 0, logz = 0,
                Vx = 0, Hx = 0, Vz = 0, Hz = 0, whom = 0, whoz = 0, pie = 0,
                here = 0, thar = 0, aim = 0, ink = 0, ibase = 0, abit = 0, zero = 0,
                info = 0, ixp = 0, ix0 = 0, ix1 = 0, ix2 = 0, ix3 = 0, logx = 0,
                prior = 0, orient = 0, offx = 0, tall = 0, wide = 0, why = 0;
        boolean EWx = false, gotit = false, rpt = false, ShoNos = false,
                frnt = false;
        double ppm = 0.0, Vbase = 0.0, Hbase = 0.0, roff = 0.0, ftmp = 0.0,
                engle = 0.0, pct = 0.0, Hpm = 0.0, Vstp = 0.0, Hstp = 0.0, fase = 0.0,
                Vtmp, Htmp, Vinc, Hinc, Vtop, Htop, Vbtm, Hbtm, lift;
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;

        // Render only wall face, defined from the perspective of sighting down
        //   the wall from the start cell to the end, the straight line from the
        //   right back corner to the (end cell) far right corner; or else back
        //   face (from view>180) which is to the left. Corners are denoted by
        //   low bits of V,H/Q,Z: 0=N/W, 1=S/E. Wall rendering base (rx) is
        //   proportional to the (cx) distance from left front corner.
        // We now run through all the BGs at the start, and build their specs
        //   into SeenWall, which is rendered at the end (so internal plain walls
        //   can cover image walls)
        // BackImag must ensure that walls butt up correctly, corner to corner,
        //   if their ends are spec'd to same grid locn.
        // Singleton wall ends must have angle & view pies. Singletons used in
        //   [left] turns, angle is mid between left & right wall segments, view
        //   pie = 180. No singletons needed in turns (= straight-on joins) that
        //   do not change general compass direction.

        // As seen by the car, the face line advances x units to the right and
        //   +/-y units fwd or back; for position z in the face, rx is z*y/x units
        //   +/-; disregard y in calc'ing z; corners denoted by low bits of V,H/Q,Z.
        // Record only compass dir'n, face starts @ left front corner of anchor,
        //   ends @ start of face in ending anchor (defines y), unless dir'n >45;
        // L: turn left needs a singleton view in front of successor view list,
        //   facing in the came-from dir'n so corner connection works;
        // R: turn right gotta start successor one cell late, w/singleton inside.

        // Collected data for image walls..
        //  4-word MapIndex item[BG+], built in BackImag, used everywhere..
        //    +0: grid V,H of left end / Q,Z of right end (Q:8/V:8/Z:8/H:8) in m;
        //    +1: (-0:iter*) view pie width:7 / view aim:10/:4 / compass orient:10;
        //    +2: ppm:8 / file offset:24;
        //    +3: (-0:proportion+) pixel H,W of this image
        //  SeenWall[cx]: 4 layers, ea: rx:12/BG+:8/cx:12, temp'ly anchor in prescan

        if (LookFrame > 0) looky = FrameNo - LookFrame;          // (prep'd by BackImag)
        if (looky == 0) {
            if (Qlog > 0x10000) {
                whoz = Qlog >> 16;
                logz = ImWi;
            } //~if
            logy = 2;
        } //~if
        // if (!ImgWall) if (BackWall != Back_Wall) ImgWall = true; // naw, both
        TmpI = 0;
        while (true) { // once through, precalc & render exit separately..
            why++; // why = 1
            if (nBax == 0) break;
            why++; // why = 2
            if (theInx == null) break;
            why++; // why = 3
            if (TrakNoPix) break;
            why++; // why = 4
            if (SeenWall == null) break;
            why++; // why = 5
            if (theImgs == null) break;
            ImgWi = theInx[0];
            ImgHi = ImgWi >> 16; // -> height of images frame
            ImgWi = ImgWi & 0xFFFF; // width of images frame
            ImgSz = ImgHi * ImgWi;
            why++; // why = 6
            if (ImgHi < 32) break; // too small for BG images
            if (ImgWi < 128) break;
            ipx = theInx[2] - 1;
            if (ipx > 0) if (ipx < theInx.length) Pmap = theInx[ipx] - 4;
            why++; // why = 7
            if (Pmap < 4) break; // (in theInx, -> 1st index item) now -> 0th
            if (Pmap > ipx) break;

            if (doit) { // render the precalc'd image walls (no early exits)..........
                if (ShowMap) if (BreadCrumbs != null) if (nCrumbs < HalfLoaf)
                    for (doing = BreadCrumbs[Crummy]; doing <= Crummy - 1; doing++)
                        if (doing > 0)
                            if (doing < Crummy) { // Crummy = 255, HalfLoaf = Crummy/2-3
                                ix0 = BreadCrumbs[doing];
                                // BreadCrumbs[doing] = 0;
                                ink = ix0 >> 8;
                                info = ink >> 8; // draw amber line there in full (small) map..
                                kx = ink & 255;
                                zx = (info >> 8) & 255;
                                yx = ix0 & 255;
                                info = info & 255;
                                if ((doing & 1) == 0) ink = ArtiColo;
                                else ink = AltWalColo;
                                if (Mini_Log) if (logy > 0) TempStr = HandyOps.Dec2Log("   '' ", doing,
                                        HandyOps.Dec2Log("/", 255 - doing, HandyOps.Int2Log(": ", ix0,
                                                HandyOps.Colo2Log(" = ", ink, HandyOps.Dec2Log(" ", info,
                                                        HandyOps.Dec2Log(" ", yx, HandyOps.Dec2Log(" ", zx,
                                                                HandyOps.Dec2Log(" ", kx, ""))))))));
                                DrawLine(ink, info, yx + ImWi + 2, zx, kx + ImWi + 2); // map coords in meters..
                                Vbase = Korner(2 * 8, ix0, logy); // rtns (float) park meters
                                Hbase = Korner(0 * 8, ix0, logy); // logy = 2; // ..  (Korn/2)
                                Vstp = Korner(3 * 8, ix0, logy);
                                Hstp = Korner(1 * 8, ix0, logy);
                                // * can we sho partial V/H-seg walls in map?
                                info = ZoomMapCoord(true, Vbase, Hbase);
                                yx = ZoomMapCoord(true, Vstp, Hstp);
                                if (info > 0) if (yx > 0) { // ..also in close-up map..
                                    kx = yx & 0xFFF;
                                    zx = yx >> 16;
                                    yx = info & 0xFFF;
                                    info = info >> 16;
                                    if (Mini_Log) if (logy != 0) TempStr = TempStr + " / "
                                            + HandyOps.Dec2Log(" ", info, HandyOps.Dec2Log(" ", yx,
                                            HandyOps.Dec2Log(" ", zx, HandyOps.Dec2Log(" ", kx, ""))));
                                    DrawLine(ink, info, yx, zx, kx);
                                } //~if // (also in close-up)
                                if (Mini_Log) if (logy != 0) { // if (Qlog..) -- included in M_L
                                    System.out.println(TempStr);                    // "   '' "
                                    TempStr = "";
                                } //~if
                                yx = 0;
                            } //~if // (ShowMap)
                if (xx > 0) { // = TmpI = rx for 1st ink on floor
                    if (!ImgWall) xx = 0; // no back walls, no fill if outdoors
                    else if (xx >= ImHi) xx = 0; // (can't)
                    else xx = (xx << 20) + 5;
                } //~if // default to BackWall, farthest floor
                why = 15;
                if (Mini_Log) if ((Qlog & 256) != 0) {
                    if (nBax > 0) if ((FrameNo < 5) || (logy > 0)) // if (Qlog) in M_L // (DoWall)
                        System.out.println(HandyOps.Dec2Log("(SeeWal^) ", FrameNo, // logy = 2
                                HandyOps.Dec2Log("=", LookFrame, HandyOps.Int2Log(" ", TripLine,
                                        HandyOps.Dec2Log(" ", whoz, HandyOps.IffyStr(looky != 0, "",
                                                " --" + HandyOps.ArrayDumpLine(SeenWall, 0, 18)))))));
                    if (logy > 0) if (CeilingHi > 0.0)
                        System.out.println(HandyOps.Flt2Log("  (", CeilingHi,
                                HandyOps.Dec2Log(" f=", ZoomFocus, ") -- <RowCeil>"))
                                + HandyOps.ArrayDumpLine(RowCeiling, 0, -20));
                    if (logy > 0) System.out.println(HandyOps.Dec2Log(" (WalzTal) ", nBax,
                            HandyOps.Dec2Log(" ", CarTall, " =::= "
                                    + HandyOps.ArrayDumpLine(WalzTall, nBax + 2, 22))));
                    TempStr = "";
                } //~if
                for (ipx = 0; ipx <= MaxLayers - 1; ipx++) { // 4 layers, do each separately (doit=T)..
                    why = 16;    // MaxLayers = 4 = depth of image wall layers
                    for (cx = 0; cx <= ImWi - 1; cx++) { // 2 brk (err), 3 cont (when done)
                        fz = 0;
                        why = 0;
                        ixp = 0;
                        ix0 = 0;
                        full = 0; // default no ceiling, stop at screen top
                        orient = 0;
                        gotit = false;
                        info = SeenWall[cx];
                        ink = SeenWall[cx + ImWi * 4]; // in pix*256 so fractional steps
                        SeenWall[cx] = SeenWall[cx + ImWi];
                        SeenWall[cx + ImWi] = SeenWall[cx + ImWi * 2];
                        SeenWall[cx + ImWi * 2] = SeenWall[cx + ImWi * 3];
                        SeenWall[cx + ImWi * 3] = info; // (restore moved info for ShoArt)
                        SeenWall[cx + ImWi * 4] = SeenWall[cx + ImWi * 5];
                        SeenWall[cx + ImWi * 5] = SeenWall[cx + ImWi * 6];
                        SeenWall[cx + ImWi * 6] = SeenWall[cx + ImWi * 7];
                        SeenWall[cx + ImWi * 7] = ink;
                        topper = cx + ImWi * 7; // put ~walltop there
                        offx = info & 0xFFF; // offset to pix in botm row of this image,
                        whom = info >> 12;   //   ..or plain wall color (whom=0)
                        rx = whom >> 8;     // 0-based fractional row to start on
                        if (CeilingHi > 0.0) if (rx > 0) if (RowCeiling != null)
                            if (CeilingCo > 7) if (CeilingCo < 0x01000000) // color is def'd
                                full = RowCeiling[rx & ImHmsk]; // actual px row @ ceil meets wall
                        whom = whom & 255; // index of this BG item // ..see "<RowCeil>" in log
                        ixp = (whom << 2) + Pmap;
                        rx = rx >> 3;
                        rx = rx + ImHaf; // now it's a screen row..
                        if (whom > 0) if (ixp > 0)
                            if (ixp < theInx.length - 4) {
                                ix0 = theInx[ixp]; // (coords, log only)
                                ix1 = theInx[ixp + 1]; // pie width:7 / view aim:10/:4 / compass:10
                                ix2 = theInx[ixp + 2]; // ppm/file offset (=-1 if plain)
                                ix3 = theInx[ixp + 3]; // pixel H,W of this image (=colo if plain)
                                if (ix2 == 0) break;
                                if (ix3 == 0) break;
                            } //~if
                        if (ipx == 0) {
                            if (offx >= 4080) {
                                offx = 7 - (offx & 7);
                                orient = WallColoz[offx & 7];
                                whom = 0;
                                fz = rx;
                            } //~if
                            else if (ImgWall) if (xx > 0) { // insert backwall behind..
                                fz = xx >> 20;     // closer only if near park edge "(WalzTal)"..
                                if (whom > 0) if (rx > fz) if (WalzTall != null) {
                                    tall = WalzTall[whom & 255];
                                    zx = tall;
                                    ibase = WalzTall[0]; // gotta be less than half median height,
                                    if (ibase > 0x40000) // and at park edge to pull down BackWall..
                                        if (MyMath.SgnExt(tall + tall - ibase) > 0) tall = 0;
                                    if (Mini_Log) if ((Qlog & 256) != 0) TempStr =
                                            HandyOps.Int2Log(" ", zx, HandyOps.Int2Log(":", ibase,
                                                    HandyOps.IffyStr(tall == 0, "!", "")));
                                    if ((tall & 0x20000) != 0) fz = rx;
                                } //~if // T: close to park edge
                                if (topper > 0) if (topper < ImWi * 8) {
                                    SeenWall[topper] = -1;
                                    topper = 0;
                                } //~if
                                orient = BackWall;
                                gotit = true;
                            } //~if // (insert backwall behind)
                            if (fz > ImHi - DrawDash) fz = ImHi - DrawDash;
                        } //~if // (ipx=0)
                        if (whoz > 0) { // whoz = Qlog>>16;
                            logy = 0;
                            if (looky == 0) {
                                zx = TripLine >> 16;
                                if (whom == whoz) logy++; // logy = 2;
                                else if (TripLine != 0) if (cx > 0) {
                                    if ((cx & 63) == 0) logy++;
                                    else if (cx + 3 > ImWi) logy++;
                                    else if (((TripLine + cx) & 0xFFFF) == 0) logy++;
                                    else if (zx + 1 < 0) if (zx + cx == 0) logy++;
                                } //~if
                                if (logy > 0) {
                                    logy++;
                                }
                            }
                        } //~if
                        if (info == 0) {
                            if (!gotit) continue;
                        } //~if
                        else if (rx < ImHaf) continue;
                        ink = ink & 0xFFFF;
                        ibase = 0;
                        ixp = 0;
                        if (whom > 0) while (true) { // (ImgWall=T)
                            why++; // why = 1
                            if (ink <= 0) break;
                            ixp = (whom << 2) + Pmap;
                            why++; // why = 2
                            if (ixp < 0) break;
                            why++; // why = 3
                            if (ixp > theInx.length - 5) break;
                            why++; // why = 4
                            ix0 = theInx[ixp]; // (coords, log only)
                            ix1 = theInx[ixp + 1]; // pie width:7 / view aim:10/:4 / compass:10
                            ix2 = theInx[ixp + 2]; // ppm/file offset (=-1 if plain)
                            ix3 = theInx[ixp + 3]; // pixel H,W of this image (=colo if plain)
                            if (ix2 == 0) break;
                            if (ix3 == 0) break;
                            tall = (ix3 >> 16) & 0xFFF; // start rendering at ground level, up
                            wide = MyMath.SgnExt(ix3);
                            why++; // why = 5
                            if (tall < 4) break;
                            if (wide <= 0) break; // artifacts are handled elsewhere
                            aim = (ix1 >> 10) & 3;
                            why++; // why = 6
                            zx = (ix2 & 0xFFFFFF) + 0; // image file offset (botm left)
                            why++; // why = 7
                            if (zx + wide >= ImgSz) break; // past botm of imgs frame
                            why++; // why = 8
                            ibase = zx - (tall - 1) * ImgWi; // image top, so end-testing (OK) -- ibase
                            offx = offx + zx;
                            kx = 0;
                            here = 0;
                            why = 0;
                            break;
                        } //~while
                        pie = 0;
                        if (why != 0) pie++;
                        else if (info == 0) pie++;
                        else if (offx < ibase) pie--;
                        else if (offx <= 0) pie--;
                        else if (offx >= ImgSz) pie--;
                        rpt = false;
                        if (Mini_Log) if ((Qlog & 256) != 0)
                            if (logy > 0) if (maxy < 555) if ((maxy < 8) || (whom > 0)) {
                                stip = 0;
                                if (rx == TripLine) stip = 32;
                                if (whom > 0) if (whom == whoz) stip = stip | 16;
                                if (cx > 0) if (((TripLine + cx) & 0xFFFF) == 0) stip = stip + 8;
                                if (cx + 3 > ImWi) stip = stip + 4;
                                if (cx == (ImWi >> 1)) stip = stip + 2;
                                if (cx == 1) stip++;
                                maxy++;
                                if (stip > 15) if ((stip & 15) != 0) rpt = true;
                                TempStr = TempStr + HandyOps.Dec2Log(HandyOps.IffyStr(ixp > 0,
                                        "\n     @@@ ", " @@ "), ixp, HandyOps.IffyStr(ixp <= 0, ":[",
                                        HandyOps.Int2Log(": [", ix0, HandyOps.Int2Log(".. ", ix2,
                                                HandyOps.Int2Log(" ", ix3, ""))))) + "] = ";
                                if (gotit) TempStr = HandyOps.Dec2Log("=", fz,
                                        HandyOps.Colo2Log("/", orient, "")) + TempStr;
                                System.out.println(HandyOps.Dec2Log("  . : . ", ipx,   // (DoWall/T)
                                        HandyOps.Dec2Log(" ", whom, HandyOps.Dec2Log(" ", rx,    // why =
                                                HandyOps.Dec2Log("/", cx, HandyOps.Int2Log(": ", info,
                                                        HandyOps.Colo2Log("=", orient, HandyOps.Dec2Log(" (", cx + ImWi * 3,
                                                                HandyOps.Fixt8th(") +", ink >> 5, HandyOps.Dec2Log(" @ ", offx,
                                                                        HandyOps.Dec2Log("..", ibase, HandyOps.Dec2Log(" *", ImgWi,
                                                                                HandyOps.TF2Log(" ", gotit, HandyOps.Dec2Log(TempStr, why,
                                                                                        HandyOps.Dec2Log(" ", full, HandyOps.TF2Log(" ", rpt,
                                                                                                ""))))))))))))))));
                                TempStr = "";
                            } //~if // (Mini_Log)
                        if (gotit) if (cx < ImWi) for (fz = fz; fz >= 0; fz += -1) { // do BackWall behind..
                            if (full > 0) if (fz == full) {
                                orient = CeilingCo; // show ceiling above
                                full = 0;
                            } //~if
                            PokePixel(orient, fz, cx);
                        } //~for // (fz)
                        if (pie > 0) continue; // if (why!=0) continue; if (info==0) continue;
                        // if (rx<ImHaf) continue;
                        if (whom > 0) { // (ImgWall=T)
                            fz = 0;
                            for (rx = rx; rx >= 0; rx += -1) {
                                if (rx < full) break; // stop at ceiling
                                if (offx < ibase) break;
                                info = -1; // = transparent
                                if (offx > 0) if (offx < theImgs.length) info = theImgs[offx];
                                // if (logy>0) if (Mini_Log) if (whom==whoz) if (TripLine+cx==0)
                                if (rpt) {
                                    System.out.println(HandyOps.Dec2Log("        ... ", rx,
                                            HandyOps.Dec2Log("/", cx, HandyOps.Hex2Log(": @ x", offx, 6,
                                                    HandyOps.Int2Log(": ", info, HandyOps.Dec2Log(" ", kx,
                                                            HandyOps.Dec2Log("/", here, "")))))));
                                } //~if
                                if (info >= 0) if (rx < ImHi - DrawDash) { // -1 = transparent
                                    if (info < 8) info = 0x010101;
                                    fz = rx; // top pix row actually filled
                                    PokePixel(info, rx, cx);
                                } //~if
                                here = here + ink;
                                zx = (here >> 8) - kx;
                                if (zx == 0) continue;
                                kx = kx + zx;
                                if (zx == 1) offx = offx - ImgWi; // omit multiply..
                                else if (zx == 2) offx = offx - (ImgWi + ImgWi);
                                else offx = offx - zx * ImgWi;
                            } //~for // (rx)
                            if (topper > 0) if (topper < ImWi * 8) {
                                SeenWall[topper] = ~fz;
                                topper = 0;
                            }
                        } //~if // (topper>0) (ImgWall=T)
                        else if (orient > 8) if (cx < ImWi) if (fz >= ImHaf) for (rx = fz; rx >= 0; rx += -1) {
                            if (full > 0) if (rx == full) {
                                orient = CeilingCo; // show ceiling above
                                full = 0;
                            } //~if
                            if (topper > 0) if (topper < ImWi * 8) {
                                SeenWall[topper] = -1;
                                topper = 0;
                            } //~if
                            PokePixel(orient, rx, cx);
                        }
                    }
                } //~for // (rx) (cx) (ipx: 4 layers)
                why = -2;
                whom = pz; // (to log backwall fill count)
                zx = 0;
                if (ShoNos) { // display img wall +s on-scrn..
                    if (Mini_Log) TempStr = "(ShoNos)";
                    for (nx = 0; nx <= nBax + 1; nx++) {
                        pz = pz >> 1;
                        if ((nx & 31) == 0) pz = DidCells[(nx >> 5) & 7];
                        if (Mini_Log) if ((Qlog & 256) != 0)
                            if (looky == 0) System.out.println(HandyOps.Dec2Log("      (SNs) ", nx,
                                    HandyOps.Dec2Log(" @ ", zx, HandyOps.Int2Log(" ", pz, ""))));
                        if ((pz & 1) == 0) continue;
                        zx = zx + 16;
                        if (zx > ImWi - 16) break;
                        if (Mini_Log) TempStr = TempStr + HandyOps.Dec2Log(" ", nx, "");
                        LabelScene(HandyOps.Dec2Log("", nx, ""), 24, zx, 0xFFFFFF);
                    } //~for // (nx)
                    if (Mini_Log) {
                        if ((Qlog & 256) != 0) System.out.println(TempStr);
                        TempStr = "";
                    }
                } //~if // (ShoNos)
                info = 0;
                ink = 0; // (render the precalc'd image walls)
                break;
            } //~if

            // if (!doit) { // (precalc) find all visible walls.......................
            if (ShoNos) for (nx = 8; nx >= 0; nx += -1) DidCells[nx] = 0;
            if (logy > 0) logy++; // logy = 3;     // (all early exits are errors)
            if (HandyOps.NthOffset(0, "\n\n", Save4Log) >= 0) {
                xx = Save4Log.length();
                while (true) { // remove unneeded blank lines from log info
                    Save4Log = HandyOps.ReplacAll("\n", "\n\n", Save4Log);
                    zx = Save4Log.length();
                    if (zx == xx) break;
                    xx = zx;
                }
            } //~if //~while // (remove unneeded blank lines)
            if (Mini_Log) if (logy > 0) if (MapLogged || ((Qlog & 256) != 0))
                System.out.println("++ ++" + Save4Log);
            why++; // why = 8
            if (theInx == null) break; // (can't fail, already checked)
            if (theImgs == null) break;
            if (VuEdge == 0.0) break; // can't do anything until after calc'd
            // zero = 0;
            why++; // why = 9
            for (doing = 1; doing <= 255; doing++) { // each wall.. (6 brk, 23 cont)            // (doing)
                Tally = doing; // (used by Map2scr)
                frnt = false; // T: use WideRatio
                sawn = false;
                // fase = 0.0; // (first log prev)
                topper = 0;
                full = 0;
                stip = 0;
                abit = 0;
                logx = 0;
                if (looky != 0) logy = 0;
                else if (doing > 15) logy = 0;
                else if (whoz > 0) if (doing > 4) logy = 0;
                if (looky == 0) if (doing + 3 > whoz) if (doing < whoz + 3) {
                    if (doing == whoz) logx = 100;
                    logy = 3;
                } //~if
                Pmap = Pmap + 4;
                if (Pmap < 8) break;
                if (Pmap > theInx.length - 6) break;
                ix0 = theInx[Pmap]; // V,H map coords for this image (bottom left)
                ix1 = theInx[Pmap + 1]; // pie width:7 / view aim:10/:4 / compass:10 / (*)
                ix2 = theInx[Pmap + 2]; // ppm/file offset (=-1 if ImgWall=plain)
                ix3 = theInx[Pmap + 3]; // pixel H,W of this image (=colo if plain)
                // ix1&0x1FF: wall advance angle, northward = 360 (face on east side)
                //  &0x200: (frnt=T) WideRatio applies to this wall segment
                //  &0xC00>>10: (0..3) normalized angle (unused)
                //  &0x3000: (0..2) use 2=left / 1=right end of visi segment for fase
                //  &0x3FC000>>14: (0..180) view wedge center angle, rel to wall adv
                //  &0x400000: end cap, for when car is between (extended) wall faces
                //  &0x800000: image is centered in segment flag
                //  &0x7F000000>>24: half-width of view wedge
                //  &0x80000000: (ix1<0) image wrap (repeat) flag
                // Tally = (ix3>>16)&0xFFF; // (used by Map2scr)
                ImgWall = (ix2 + 1 != 0);
                if (ImgWall) {
                    stip = (ix2 >> 16) & 0xFF00; // = ppm *256
                    ppm = MyMath.Fix2flt(stip, 0); // = image ppm *256
                    if (ppm > 0.0) topper = (ix3 >> 8) & 0xFFF00; // in img pix *256
                    if (topper > 0) { // (calc now for log)..
                        Vtmp = MyMath.Fix2flt(topper, 0) / ppm - CameraHi; // = wall top > CamHi
                        topper = (int) Math.round(MyMath.fMax(Vtmp * 1024.0, 0.0));
                    }
                } //~if // pkmm
                else Tally = 0;
                abit = (((ix1 >> 12) + 2) & 3) - 2; // <0 prefers left fase, >0 prefers right
                if ((ix1 & 0x200) != 0) if (WideRatio > 2) frnt = true;
                engle = MyMath.Fix2flt(ix1 & 0x1FF, 0) - 90.0 - Facing; // = actual view angle
                while (engle < 0.0) engle = engle + 360.0;           // 0 degs = square-on
                while (engle > 180.0) engle = engle - 360.0;
                aim = ix1 >> 14;
                pie = (aim >> 10) & 0x7F; // half-width of view wedge
                aim = aim & 0x0FF; // spec's view angle (pie wedge center), default = 90
                if (Mini_Log) if ((Qlog & 256) != 0)
                    if ((logy > 0) || (TripLine < 0)) { // if (Qlog..) -- included in M_L
                        zx = whom | yx | Hx | Hz | ibase | ink | pz | fz;
                        if (zx == 0) { // ** this is leftover info to log from prev doing **..
                            if (EWx) zx++; // EWx = Htmp>Vtmp; // (set below)
                            else if (rpt) zx++; // right end is off-screen
                            else if (Vbase != 0.0) zx++;
                            else if (Hbase != 0.0) zx++;
                            else if (Vstp != 0.0) zx++;
                            else if (Hstp != 0.0) zx++;
                            else if (ftmp != 0.0) zx++;
                            else if (roff != 0.0) zx++;
                        } //~if // = rt end face length reduction
                        if (TmpI != 0) TempStr =
                                HandyOps.Int2Log("/", TmpI, " ");
                        else TempStr = " ";
                        if (zx != 0) TempStr =
                                HandyOps.Int2Log(" {", whom, HandyOps.Dec2Log(TempStr, yx,
                                        HandyOps.Dec2Log(": ", Hx, HandyOps.Dec2Log("/", Hz,
                                                HandyOps.Flt2Log(" ", Vbase, HandyOps.Flt2Log("/", Hbase,
                                                        HandyOps.Flt2Log(" ", Vstp, HandyOps.Flt2Log("/", Hstp,
                                                                HandyOps.Int2Log(" ", ibase, HandyOps.Int2Log(" ", ink, // _10
                                                                        HandyOps.TF2Log(" ", EWx, HandyOps.Flt2Log(" ", ftmp, // face len
                                                                                HandyOps.TF2Log(" ", rpt, HandyOps.Flt2Log("/", roff,
                                                                                        HandyOps.Int2Log(" ", pz, HandyOps.Dec2Log(" ", fz,
                                                                                                HandyOps.Flt2Log("\n     ", Hpm, HandyOps.IffyStr(whom + 20 > 0, "} ",
                                                                                                        HandyOps.Flt2Log(" ", Vposn, HandyOps.Flt2Log("/", Hposn, // _20
                                                                                                                "} "))))))))))))))))))));
                        else TempStr = " {00} ";
                        if ((WideRatio > 0) || frnt) TempStr = TempStr
                                + HandyOps.Fixt8th(" ", WideRatio >> 13, HandyOps.TF2Log("/", frnt, " "));
                        zx = ix0 | ix1 | ix2 | ix3;
                        xx = (ix0 ^ ink) & 0xFF00FF;
                        ink = ix0 >> 8;
                        System.out.println(HandyOps.Dec2Log(" _  _ ", doing,
                                HandyOps.TF2Log(" ", ImgWall, HandyOps.Flt2Log(TempStr, engle,
                                        HandyOps.Dec2Log(" @ ", Pmap, HandyOps.Int2Log(" [", ix0,
                                                HandyOps.IffyStr(zx == 0, "]", HandyOps.Int2Log("/", prior,
                                                        HandyOps.Int2Log(" ", ix1, HandyOps.Int2Log(" ", ix2,      //_10
                                                                HandyOps.Int2Log(" ", ix3, HandyOps.Dec2Log("] ", pie,
                                                                        HandyOps.Flt2Log(" {", fase, HandyOps.Dec2Log(" ", zero,
                                                                                HandyOps.Dec2Log("} ", aim, // HandyOps.Dec2Log(" ",topper,
                                                                                        HandyOps.IffyStr(xx == 0, "", " *"))))))))))))))));
                        pz = HandyOps.NthOffset(0, HandyOps.Dec2Log("@ ", Pmap, " "), Save4Log);
                        if (pz > 0) System.out.println("    __ " + HandyOps.NthItemOf(true, 1,
                                HandyOps.Substring(pz, 99, Save4Log)));
                        sawn = true;
                        TempStr = "";
                    } //~if // (Mini_Log)                         // ink = 0;
                TmpI = 0;
                whom = 0;
                ibase = 0;
                yx = 0;
                Hx = 0;
                Hz = 0;
                roff = 0.0; // (for log)
                Vbase = 0.0;
                Hbase = 0.0;
                Vstp = 0.0;
                Hstp = 0.0;
                ftmp = 0.0;
                Hpm = 0.0;
                fase = 0.0;
                fz = 0;
                pz = 0;
                EWx = false;
                rpt = false;
                if (ix0 == 0) { // normal exit from for(doing)..
                    if (doing == 1) why = 12;
                    break;
                } //~if
                whom--; // whom = -1 // for log: whom<0 identifies an early continue
                if ((ix1 & 0x400000) != 0) prior = 0; // end-cap cancels "already did"
                if (((ix0 ^ prior) & 0xFE00FE) == 0) continue; // already did this anchor
                why++; // why = 10
                if (ix2 == 0) break; // shouldn't hap
                if (ix3 == 0) break;
                whom--; // whom = -2 // 2nd cont of 23..
                if (ppm == 0.0) continue; // no ppm? // if (ImgWall)
                ink = ix0 >> 8;
                zx = 255 - doing;
                if (ShowMap) if (BreadCrumbs != null) if (zx > 0)
                    if (zx > nCrumbs) if (nCrumbs < HalfLoaf) if (((ix0 ^ ink) & 0xFF00FF) != 0)
                        if (zx < BreadCrumbs.length) { // Crummy = 255, HalfLoaf = Crummy/2-3
                            BreadCrumbs[zx] = ix0;
                            BreadCrumbs[Crummy] = zx;
                        } //~if
                whom--; // whom = -3
                ink = 0;
                Vbase = Korner(2 * 8, ix0, logy + logx); // anchor; rtns park meters
                Hbase = Korner(0 * 8, ix0, logy + logx); // logy = 3; // ..  (Korn/3)
                Vstp = Korner(3 * 8, ix0, logy + logx); // far end // logx = 100 if doing=whoz
                Hstp = Korner(1 * 8, ix0, logy + logx); // (rtns larger dim -epsilon)
                why++; // why = 11
                if ((ix1 & 0x400000) != 0) { // this is an end-cap, are we between sides?
                    whom = -21;
                    xx = ix0 >> 16;
                    if (pie == 0) pie--; // (non-zero: default failed this segment)
                    ink = pie; // (for log)
                    zx = 0;
                    kx = ix1 & 0x1FF;
                    if (kx < 45) zx--; // wall advance is lower coord
                    else if (kx < 225) zx++; // wall advance is higher coord
                    else zx--;
                    fz = ((int) Math.round(engle));
                    if (fz != 0) zx = 0; // not square-on
                    if ((((ix0 >> 8) - ix0) & 255) == 0) { // E or W cap, look at V..
                        whom--; // whom = -22
                        if (MyMath.fMin(Vbase, Vstp) > Vposn) // car is north of wall
                            zero = -zx;
                        else if (MyMath.fMax(Vbase, Vstp) + epsilon < Vposn) // car is south
                            zero = zx;   // ..Korn rtns larger dim -epsilon, so add back on
                        else pie = 0;
                    } //~if
                    else if ((((xx >> 8) - xx) & 255) == 0) { // N or S cap, look at H..
                        whom++; // whom = -20
                        if (MyMath.fMin(Hbase, Hstp) > Hposn) // car is west of wall..
                            zero = -zx;
                        else if (MyMath.fMax(Hbase, Hstp) + epsilon < Hposn) // car is east
                            zero = zx;   // ..add epsilon back on to round up to wall face
                        else pie = 0;
                    } //~if
                    ink = (pie << 16) | ink & 0xFFFF; // (for log)
                    xx = 0;
                    if (pie > 0) if (aim > 0) if (engle < 60.0) if (engle + 60.0 > 0) {
                        xx = aim - 90 - fz;
                        if (false) { // NG: car can be distant but parallel **
                            whom = -23; // not in wall, try spec'd view wedge
                            if (xx < pie) if (xx + pie > 0) pie = 0;
                        }
                    } //~if // good enough, use it
                    if (Mini_Log) if (logy > 0) if (doing + 3 > whoz) if (doing < whoz + 3)
                        System.out.println(HandyOps.Dec2Log("    (zero) ", doing,
                                HandyOps.Dec2Log(" ", kx, HandyOps.Flt2Log(" ", engle,
                                        HandyOps.Dec2Log(" ", fz, HandyOps.Dec2Log(" ", zx,
                                                HandyOps.Dec2Log(" (", whom, HandyOps.Dec2Log(") ", pie,
                                                        HandyOps.Int2Log("/", ink, HandyOps.Dec2Log(" ", aim,
                                                                HandyOps.Dec2Log(" ", xx,
                                                                        HandyOps.Flt2Log(" [", Vposn, HandyOps.Flt2Log("/", Hposn,
                                                                                HandyOps.Flt2Log(" : ", Vbase, HandyOps.Flt2Log("/", Hbase,
                                                                                        HandyOps.Flt2Log(" ", Vstp, HandyOps.Flt2Log("/", Hstp,
                                                                                                HandyOps.Dec2Log("] = ", zero, ""))))))))))))))))));
                    if (pie < 0) continue; // (pie>0) tested later, using fase
                    whom = -3;
                } //~if
                orient = 0; // temp bool: >0 = true => Map2scr(..nuly..)
                zx = (logy >> 1) + logx;
                fz = pie;
                if (fz == 0) if (ix1 >= 0) if (MyMath.fAbs(engle) > 86.0) fz++;
                ibase = Map2screen(Vbase, Hbase, engle, fz, true, true, zx); // (2scrn/1)
                kx = TmpI; // = scr.pix*256/m
                if (ibase == 0) break; // invalid coordinates
                if (ibase < 0) orient++; // >0 if not yet seen on-screen (to do CashDx)
                // if (WallAim != 0.0) if (abit <= 0) {
                lift = WallAim; // temp left version of fase
                if (zx > 0) zx++;
                ink = Map2screen(Vstp, Hstp, engle, fz, true, orient > 0, zx); // far (2scrn/2)
                zx = TmpI;
                if (ink == 0) break; // if valid, ibase/ink are scrn row,coln of ends
                if (ink > 0) orient = 0; // got good CashDx
                fase = WallAim;
                why = why - 2; // why = 9
                if (ibase < 0) if (ink < 0) { // both ends not visible, maybe can't show..
                    // whom = -3 // -2 (off to left), -3 (off to right) 3rd cont of 23..
                    if (ibase == ink) continue; // both off same side or both behind car
                    whom--; // whom = -4
                    if (ibase + 1 == 0) continue; // either one behind
                    whom--; // whom = -5
                    if (ink + 1 == 0) continue;
                } //~if // 5th cont of 23
                whom = -6; // 6th&7th cont of 23..
                if (ibase > 0) if (ink + 2 == 0) continue; // near visi, far off lft => back
                if (ink > 0) if (ibase + 3 == 0) continue; // far visi, near off rt => back
                Vinc = Vstp - Vbase; // moving away from anchor (full dx in pk meters)
                Hinc = Hstp - Hbase;
                Vtmp = MyMath.fAbs(Vinc);
                Htmp = MyMath.fAbs(Hinc);
                ftmp = MyMath.fMax(Vtmp, Htmp); // full face length in meters
                whom--; // whom = -7
                if (ftmp <= 0.0) continue; // probly can't
                EWx = Htmp > Vtmp;
                whom--; // whom = -8
                while (true) { // exit when both ends visible, or can't..
                    roff = 0.0; // => reduction in face length on right end
                    rpt = false;
                    Vtmp = Vbase;
                    Htmp = Hbase;
                    if ((kx & -0x10000) != 0) kx = (kx >> 16) & 0xFFFF;
                    if ((zx & -0x10000) != 0) zx = (zx >> 16) & 0xFFFF;
                    if (ink > 0) if (zx == 0) ink = 0;
                    if (ibase > 0) if (kx == 0) ibase = 0;
                    if (ibase > 0) if (ink > 0) break; // whom = -8 // both OK
                    whom--; // whom = -9
                    if (ink <= 0) { // binary search for far visible end..
                        xx = ink; // if M2s res !=, it means we passed edge (possibly over)
                        yx = 12; // max iterations
                        maxz = -1;
                        Vtop = Vstp;
                        Htop = Hstp;
                        Vbtm = Vbase;
                        Hbtm = Hbase;
                        zx = logy * 3;
                        while (true) { // (far binary search loop)
                            Vinc = (Vtop + Vbtm) * 0.5; // midpoint this iteration
                            Hinc = (Htop + Hbtm) * 0.5;
                            rpt = true; // no longer at end
                            yx--;                                    // zx = 9/12/15..
                            ink = Map2screen(Vinc, Hinc, engle, fz, true, orient > 0, zx);
                            if (ink + 2 == 0) { // now off to left, back off..
                                if (yx < 0) break; // can't get there, we failed
                                Vbtm = Vinc;
                                Hbtm = Hinc;
                                zx = logy * 6;
                                continue;
                            } //~if
                            if (ink > 0) orient = 0; // got good CashDx
                            if (WallAim != 0.0) {
                                if (fase == 0.0) fase = WallAim;
                                else if (fase != WallAim) fase = (fase + WallAim) * 0.5;
                            } //~if
                            zx = TmpI;
                            pz = ink & 0xFFFF;
                            gotit = (pz == ImWi - 1);
                            if (!gotit) if (ink > 0) {
                                if (yx < 0) gotit = true; // out of tries, stop here
                                else if (maxz < 0) maxz = pz;
                                else if (pz == maxz) gotit = true; // not getting closer
                                else if (pz > maxz) maxz = pz;
                            } //~if
                            if (!gotit) if (yx < 0) if (maxz < 0) // can't get there, but..
                                gotit = true;                   // ..none better
                            if (gotit) { // right on edge, or can't get there, but visi..
                                if (EWx) roff = roff + Hinc - Hstp; // = accum'd trim off rt
                                else roff = roff + Vinc - Vstp;
                                Vstp = Vinc;
                                Hstp = Hinc;
                                if (ink > 0) if (pz < ImWi - 1) // pretend we hit the end..
                                    ink = ink & -0x10000 | (ImWi - 1);
                                break;
                            } //~if // 1st of 2 exits
                            if (yx < 0) { // can't get there, back up to best known..
                                if (EWx) roff = roff + Hbtm - Hstp; // = accumulated reduction
                                else roff = roff + Vbtm - Vstp;
                                Vstp = Vbtm;
                                Hstp = Hbtm;
                                ink = Map2screen(Vbtm, Hbtm, engle, fz, true, orient > 0, logy * 12); // 36
                                if (ink > 0) orient = 0;
                                zx = TmpI;
                                break;
                            } //~if // 2nd of 2 exits
                            if (ink < xx) if (xx + 1 == 0) xx = ink; // was behind, now off side
                            if (ink == xx) { // still off same end..
                                Vtop = Vinc;
                                Htop = Hinc;
                                zx = logy * 4;
                                continue;
                            } //~if
                            Vbtm = Vinc; // this end is on-screen, or off anchor end..
                            Hbtm = Hinc;
                            zx = logy * 5;
                        } //~while // (far binary search loop)
                        if (zx == 0) ink = 0; // zx = scr.pix*256/m (=0: off-scrn)
                        if (ink <= 0) break;
                    } //~if // whom = -9 // failed (far binary search)
                    whom--; // whom = -10
                    if (ibase <= 0) { // binary search for near visible end..
                        xx = ibase; // to compare M2s res to
                        yx = 12; // max iterations
                        maxz = -1;
                        Vtop = Vstp; // (far end) now known visible
                        Htop = Hstp;
                        Vbtm = Vbase;
                        Hbtm = Hbase;
                        kx = logy * 7;
                        while (true) { // (near binary search loop)
                            Vinc = (Vtop + Vbtm) * 0.5; // midpoint this iteration
                            Hinc = (Htop + Hbtm) * 0.5;
                            yx--;                                     // kx = logy*7..
                            ibase = Map2screen(Vinc, Hinc, engle, fz, true, false, kx);
                            if (ibase + 3 == 0) break; // now off to right, so we failed
                            if (WallAim != 0.0) {
                                if (lift == 0.0) lift = WallAim;
                                else if (lift != WallAim) lift = (lift + WallAim) * 0.5;
                            } //~if
                            kx = TmpI;
                            pz = ibase & 0xFFFF;
                            gotit = (pz == 0);
                            if (!gotit) if (ibase > 0) {
                                if (yx < 0) gotit = true; // out of tries, stop here
                                else if (maxz < 0) maxz = pz;
                                else if (pz == maxz) gotit = true; // not getting closer
                                else if (pz < maxz) maxz = pz;
                            } //~if
                            if (!gotit) if (yx < 0) if (maxz < 0) // can't get there, but..
                                gotit = true;                   // ..none better
                            if (gotit) { // right on edge, or can't get there, but visi..
                                Vtmp = Vinc;
                                Htmp = Hinc;
                                if (ibase > 0) if (pz > 0) // pretend we hit 0..
                                    ibase = ibase & -0x10000;
                                break;
                            } //~if // 1st of 2 exits
                            if (yx < 0) { // can't get there, back up to best known..
                                Vtmp = Vtop;
                                Htmp = Htop;
                                ibase = Map2screen(Vtmp, Htmp, engle, fz, true, false, logy * 10);
                                // if (fase == 0.0) if (WallAim != 0.0) fase = WallAim; // unneed
                                kx = TmpI;
                                break;
                            } //~if // 2nd of 2 exits
                            if (ibase < xx) if (xx + 1 == 0) xx = ibase; // was behind, now off side
                            if (ibase == xx) { // still off same end..
                                Vbtm = Vinc;
                                Hbtm = Hinc;
                                kx = logy * 8;
                                continue;
                            } //~if
                            Vtop = Vinc; // this end is on-screen (can't be off)..
                            Htop = Hinc;
                            kx = logy * 9;
                        } //~while // (near binary search loop)
                        if (kx == 0) ibase = 0;
                    } //~if // failed (near binary search)
                    break;
                } //~while // (exit when both ends visible)
                pz = 0; // whom = -8/-9/-10
                orient = 0; // (done with this)
                if (ibase <= 0) continue; // didn't succeed, try next..
                if (ink <= 0) continue; // 9th cont of 23..
                whom = -11; // as visible, left map coord: V/Htmp, right: V/Hstp
                if (abit < 0) fase = lift; // abit<0 prefers left fase, >0 prefers right..
                else if (abit == 0) fase = (lift + fase) * 0.5;
                if ((kx & -0x10000) != 0) kx = (kx >> 16) & 0xFFFF; // = near end scr.pix*256/m
                if ((zx & -0x10000) != 0) zx = (zx >> 16) & 0xFFFF; // zx = (far) scr.pix*256/m
                if (kx <= 0) continue; // sb never =0 unless wall is too far to draw
                whom--; // whom = -12
                if (zx <= 0) continue; // never <0 // 12th cont of 24
                Hx = ibase & 0xFFFF; // left end coln
                Hz = ink & 0xFFFF;  // right end coln
                yx = Hz - Hx; // seeing front if left end (Hx) is to left of right end..
                if (Mini_Log) if (looky == 0) TempStr =
                        HandyOps.Dec2Log(" ", yx, "}");
                whom--; // whom = -13
                if (yx <= 0) continue; // if (yx==0) ignore 1-pix flash & back side
                whom--; // whom = -14
                if (aim >= 180) continue; // if (frnt) sb no back view of front
                // whom--; // whom = -15
                if (pie > 0) { // pie = (ix1>>24)&0x7F = half-width of view wedge
                    // xaim is the direction of wall face advance L->R, (N=0, E=90)
                    // aim = ix1>>14, (relative to xaim) the center of the visible wedge
                    // engle = (ix1&0x1FF)-90.0-Facing (=0 square-on), not useful near 90,
                    // .. so use fase = actual angle to car, from approx mid between ends
                    // .. of visible wall (from WallAim as rtn'd by Map2scr)
                    // fz = pie; // (for log) already = pie (above) // xaim=ix1&0x1FF..
                    xx = ix1 & 0x1FF;
                    pz = ((int) Math.round(fase)) - xx - aim; // =0 at view wedge cntr
                    while (pz + 180 < 0) pz = pz + 360;
                    while (pz > 180) pz = pz - 360;
                    if (Mini_Log) if (logy > 0) if (doing + 5 > whoz) if (doing < whoz + 5)
                        System.out.println(HandyOps.Dec2Log("    (pied) ", doing,
                                HandyOps.Flt2Log(" ", fase, HandyOps.Dec2Log(" ", xx,
                                        HandyOps.Dec2Log(" ", aim, HandyOps.Dec2Log(" = ", pz,
                                                HandyOps.Dec2Log(": ", pie, HandyOps.Dec2Log(" ", zero, ""))))))));
                    whom--; // whom = -15 // 15th cont of 24..
                    xx = pz;   // if ((ix1&0x400000)==0) // but not end-cap (no effect)..
                    if (zero > 0) xx++; // no test= at left edge
                    if (xx > pie) continue; // car is off to left of wedge (seen frm wall)
                    whom--; // whom = -16 // (for log)
                    xx = pz;
                    if (zero < 0) if ((ix1 & 0x400000) == 0) xx--; // no test= at right edge
                    if (pz + pie < 0) continue; // (pz<-pie) off to right // 16th cont of 23
                    pz = 0;
                    fz = 0;
                } //~if // then or otherwise it's in view..
                whom = -17;         // log: "_  _ doing ImgWall {.. pz fz\  Hpm} aim .."
                wide = ix3 & 0xFFF; // = img width, used in offx end test
                gotit = false;
                if (ImgWall) { // image wall..                               // (DoWall)
                    gotit = (ix1 & 0x800000) != 0; // centered
                    // stip = (ix2>>16)&0xFF00;
                    Hpm = ppm; // ppm = MyMath.Fix2flt(stip,0); // stip = (image ppm)*256
                    fz = stip >> 8; // now is img.pix/m, can't =0 (tested above)
                    stip = 0;
                    if (engle != 0.0) if (!gotit) { // don't oblique-compress centered
                        if (ix1 >= 0) Hpm = 0; // img not recycled, need step rate
                        else if (frnt) Hpm = 0;
                    } //~if // ditto if WideRatio is active
                    if (Hpm == 0) { // need to know step rate..
                        Hpm = MyMath.fAbs(engle); // engle = (ix1&0x1FF)-90.0-Facing;
                        if (Hpm > 88.0) if (fase > 0.0) { // if (near) parallel, use fase..
                            // fase is true compass, need rel:xaim, so 0 degs = square-on..
                            // fase = fase-90.0-MyMath.Fix2flt(ix1&0x1FF,0);
                            fase = fase - engle - 180.0 - Facing; // (-engle incl +90+Facing, subt)
                            while (fase + 180.0 < 0.0) fase = fase + 360.0;
                            if (fase > 180.0) fase = fase - 360.0;
                            Hpm = MyMath.fAbs(fase);
                        } //~if // (near parallel)
                        // whom--; // whom = -17
                        if (Hpm >= 90.0) continue; // (shouldn't hap) looking at back side
                        MyMath.Angle2cart(Hpm); // Hpm=engle=0 if square-on
                        Hpm = MyMath.Cose; // cos(engle) => 0 as view angle becomes oblique
                        whom--; // whom = -18
                        if (Hpm < 0.001) continue; // too oblique to show (or behind)
                        if (Hpm < 0.01) stip = (int) Math.round(1.0 / Hpm) << 8;
                        else stip = (int) Math.round(256.0 / Hpm); // = slant step increase
                        xx = fz * stip;
                        if (kx > zx) xx = xx / kx; // kx = near end scr.pix*256/m; zx = far..
                        else xx = xx / zx;
                        whom--; // whom = -19
                        if (xx > 55) continue; // steps thru image too fast to see anything
                        if (Hpm < 0.15) {
                            whom--; // whom = -20
                            if (Hpm < 0.001) continue; // too oblique to show (or behind)
                            if (fz > 99) if (Hpm < 0.05) continue;
                            if (fz > 32) if (Hpm < 0.02) continue;
                            if (fz > 16) if (Hpm < 0.01) continue;
                        } //~if // +24 of 24
                        Hpm = ppm / Hpm;
                    } //~if // Hpm>0 always (need to know step rate)
                    if (frnt)   // WideRatio = H-ppm*256/V-ppm..
                        Hpm = MyMath.Fix2flt(WideRatio << 2, 10) * Hpm;
                } //~if // (ImgWall)
                prior = ix0; // OK, commit (lock in this anchor)................. commit
                whom = (-whom - 16) << 4; // = 16/48/64 (was -17/-19/-20)
                inv = 0;
                if (EWx) Vinc = Htmp - Hbase; // (pct) = how much adjusted left to deduct
                else Vinc = Vtmp - Vbase;
                Hinc = roff; // (for log)
                pct = MyMath.fAbs(Vinc); // in pk meters, >=0 (save Vinc for log)
                roff = MyMath.fAbs(roff);
                lift = roff + pct; // how much this wall is shortened (clipped) in pk m
                if (ix3 < 0) if (ftmp > 0.0) if (lift > 0.0) if (lift < ftmp)
                    full = MyMath.Trunc8(lift * 256.0 / ftmp); // =ratio (hid pix / full face)
                ftmp = ftmp - (roff + pct); // now visible face length in pk meters
                // now ftmp is visible face (in meters) matching yx (in H-pixels)
                // we will step across the screen from cx=ibase to ink,
                // ..fractionally incrementing both rx and offx proportional
                // ..to ftmp (now = visible face length) // if (Qlog..) -- incl in M_L..
                if (Mini_Log) if (looky == 0) TempStr = HandyOps.Flt2Log(" .. ", Vinc,
                        HandyOps.Flt2Log("|", Hinc, HandyOps.Dec2Log(" ", wide, // Hinc=roff
                                HandyOps.Dec2Log(" ", kx, HandyOps.Dec2Log("|", zx,
                                        HandyOps.Dec2Log(" ", full, TempStr))))));
                if (ImgWall) { // image wall..  // kx = scr.pix*256/m (frm Map2scr)..
                    maxz = ibase >> 16; // on-screen pix row (rx frm Map2sc in frac'l rows)..
                    // = rx-ImH8; // = CameraHi at this distance, in frac'l pixel rows
                    if (CarTall > 0) if (maxz > 0) if (Mini_Log) if ((Qlog & 256) != 0)
                        while (true) if (WalzTall != null) {
                            pz = WalzTall[0] & 0xFFFF; // >0: majority wall height in pk meters*16
                            if (pz == 0) break; // only check majority walls "(WalzTal)"..
                            if ((WalzTall[doing] & 0xFFFF) != pz) break; // CarTall = CameraHi*16..
                            orient = ((pz << 7) + (CarTall >> 1)) / CarTall; // (rnd'd) ratio of hi *128
                            xx = pz * kx; // now pixel height on screen *4K, as calc'd thru Map2sc
                            inv = (orient * maxz) << 6; // correct wall height, in scrn rows *65K
                            offx = 0;
                            Vx = 0;
                            if (xx > 63) Vx = inv / (xx >> 4); // corrected kx (scr.pix*256/m)
                            Vz = pz * zx; // ditto other end..
                            if (Vz > 63) Vz = inv / (Vz >> 4); // corrected zx
                            else Vz = 0;
                            if (Mini_Log) if (logy > 0) {
                                if (Vx < (kx >> 3)) offx++;
                                else if (Vz < (zx >> 3)) offx++;
                                cx = xx / maxz; // wall hi, as ratio to CameraHi for log (*4K/8)
                                xx = xx >> 12;
                                rx = (maxz >> 3) + ImHaf; // (for log)
                                inv = inv >> 16;
                                System.out.println(HandyOps.Dec2Log("    [WallHi] ", doing,
                                        HandyOps.Dec2Log(" @ ", rx, HandyOps.Dec2Log(" ", pz,
                                                HandyOps.Dec2Log(": ", xx, HandyOps.Dec2Log("/", maxz,
                                                        HandyOps.Fixt8th(" = ", cx >> 6, HandyOps.Fixt8th("/", orient >> 4,
                                                                HandyOps.Dec2Log(" CT=", CarTall, HandyOps.Fixt8th("=", CarTall >> 1,
                                                                        HandyOps.Dec2Log(" sppm=", kx, HandyOps.Fixt8th("=", kx >> 5,
                                                                                HandyOps.Dec2Log("\n      => ", Vx, HandyOps.Fixt8th("=", Vx >> 5,
                                                                                        HandyOps.Dec2Log(" | ", zx, HandyOps.Dec2Log(" => ", Vz,
                                                                                                HandyOps.Fixt8th("=", Vz >> 5, HandyOps.Dec2Log(" ", inv,
                                                                                                        HandyOps.Dec2Log(" top=", rx - inv, HandyOps.Dec2Log("/", rx - xx,
                                                                                                                HandyOps.IffyStr(offx > 0, " =", " +")))))))))))))))))))));
                            } //~if
                            if (true) break;
                            if (offx > 0) break; // (no correction for now)
                            kx = Vx;
                            zx = Vz;
                            break;
                        } //~while
                    inv = 0;
                    orient = 0; // fz = fz>>8; // now is img.pix/m, can't =0
                    if (fz > 0) { // kx = near end scr.pix*256/m; zx = far..
                        kx = (kx << 4) / fz; // now it's sc.pix*4K/im.pix (kx:left, zx:right)
                        zx = (zx << 4) / fz;
                    } //~if
                    fz = 0;
                    rx = 0; // (borrow this as bool, >0=T: square up)
                    if (zx != kx) {
                        rx = zx - kx; // = change in spx*4K/ipx from anchor to far end
                        fz = rx / yx; // = spx*4K/ipx change rate // yx = Hz-Hx;
                        // scrn pix/m = yx/ftmp, so fz (V-step) = offx H-step = ipm/spm
                        pz = (zx + kx + 1) >> 1; // = average scp/imp step rate **4K
                        rx = MyMath.iAbs(rx);
                        if (rx < 333) rx = 0;
                    } //~if // (math is wrong, so unused)
                    else pz = kx; // pz is avg step rate spx/ipx (kx is lft, zx is rt end)
                    if (Mini_Log) if (looky == 0) TempStr = HandyOps.Fixt8th("\n     ", kx >> 9,
                            HandyOps.Fixt8th("|", zx >> 9, HandyOps.Fixt8th(" ", pz >> 9,
                                    HandyOps.Dec2Log("=", pz, HandyOps.Dec2Log(" / ", yx,
                                            HandyOps.TF2Log(" ", rx > 0, HandyOps.Dec2Log(" ", fz,
                                                    HandyOps.Dec2Log(" ", stip, TempStr))))))));
                    if (false) if (rx > 0) { // step rate change <= 1px full width,
                        if (pz < 55) pz = 0; // make it square-on (this no longer works)..
                        kx = pz; // pz is avg step rate spx/ipx
                        zx = pz;
                        fz = 0;
                    }
                } //~if // (ImgWall)
                else fz = MyMath.iAbs(ibase - ink);
                // offx = MyMath.iAbs(fz)&-0x20000; // =0 if (nearly) square-on
                // xxx = (step*yx)>>5; // sb net visi face length in im.pix
                rx = (ibase << 4) & -0x100000; // 0-based fractional pix row as shown..
                Vz = (ink << 4) & -0x100000;  // (so no need to adjust for lost ends)
                if (fz == 0) { // square-on, rx is the same all the way across..
                    rx = ((Vz + rx + 0x100000) >> 1) & -0x100000; // Vz = rx; // (Vz unused)
                    Vx = 0;
                } //~if
                else Vx = (Vz - rx) / yx; // = fractional row step rate for on-screen rx
                offx = 0; // image pixel offset from left end, ++ for each scrn pix
                if (ImgWall) { // image wall.. (ppm=(image ppm)*256, pct=left trim in m)
                    orient = zx >> 9; // sp/ip step rate at the other end (for log)
                    wide = wide << 8; // now = offx end test
                    // Hpm = ppm/cos(engle)
                    offx = ((int) Math.round(Hpm)) * ((int) Math.round(pct)); // (large value)
                    if ((offx & -0x40000) == 0) // (usually) use fractional parts in eval
                        offx = (int) Math.round(Hpm * pct); // = pixels*256 off left end of img
                    // info = (int)Math.round(Hpm*roff); // (unused) pixels*256 off right
                    if (gotit) { // centered..
                        ix1 = ix1 & 0x7FFFFFFF; // (so it stops instead of wrap at end)
                        inv = ((pz >> 1) + 0x100000) / pz; // spx*4K/ipx => ip*256/sp
                        stip = 0; // pz is avg step rate (oblique calc not apply)
                        xx = (yx + 1) * inv; // = img width we need (yx = Hz-Hx)
                        if (xx > wide) { // too close, gotta stretch (unneed?)
                            stip = (wide << 8) / xx;
                            xx = wide;
                        } //~if
                        xx = (wide - xx) >> 1; // TempStr = " .. pct.f|roff.f wide" ... " yx}"..
                        if (Mini_Log) if (looky == 0) TempStr = HandyOps.Dec2Log(" C:", xx,
                                HandyOps.Dec2Log(" *", stip, "")) + TempStr;
                        if (xx > 0) offx = offx + xx;
                    } //~if // can't <0 (centered)
                    else if (ix3 < 0) { // gotta exactly fill the frame..
                        // nominal step rate full = wide*256/yx (unscaled for persp)
                        // ..but yx omits hidden pix (in full>0), so.. // yx>0 = Hz-Hx;
                        nx = full; // (for log)
                        if (full > 0) xx = (256 - full) * wide; // = 256* vis pix in image shown
                        else xx = wide << 8; // wide already *256, now *65K
                        full = (xx / yx + 128) >> 8; // = ip*256/sp (step rate in image, per scpx)
                        if (Mini_Log) if (looky == 0) TempStr = HandyOps.Dec2Log(" FF:", nx,
                                HandyOps.Dec2Log(" ", xx, HandyOps.Dec2Log(" => ", full, "")))
                                + TempStr;
                    } //~if
                    else if (ix1 < 0) if (wide > 8) if (offx >= wide) { // accurately adjust..
                        nx = offx;
                        if (offx > (wide << 4)) offx = offx % wide;       // ..pixels cut off left
                        else while (offx >= wide) offx = offx - wide;
                        if (Mini_Log) if (looky == 0) TempStr = HandyOps.Dec2Log(" AA:", nx,
                                HandyOps.Dec2Log(" => ", offx, "")) + TempStr;
                    } //~if
                    kx = kx - fz;
                } //~if // (ImgWall) (image wall)
                else if (Mini_Log) if (looky == 0) TempStr = TempStr + " ||"; // (plain w)
                maxz = 0;
                for (cx = Hx; cx <= Hz; cx++) { // each visible H-pix (1*brk, 4*cont after log)..
                    TmpI = 0; // (borrow this for log)
                    if (ImgWall) {
                        kx = kx + fz; // kx is fractional coln step rate in image, spx*4K/ipx
                        // ..used only for V-step in some cases
                        inv = ((kx >> 1) + 0x100000) / kx; // spx*4K/ipx => ip*256/sp
                        // if (gotit) inv = -inv;
                        if (offx >= wide) { // both offx,wide are *256
                            TmpI = offx;
                            if (ix1 >= 0) break; // image not iterated
                            offx = 0;
                        } //~if
                        info = (offx + 128) >> 8;
                    } //~if
                    else info = ix3;   // rx is 0-base frac'l row (=ibase)..
                    info = (rx + 0x80000) & -0x100000 | (doing << 12) | info & 0xFFF;
                    // rx = rx+Vx; offx = offx+inv; // (inc'd after log) // logy = 3..
                    ink = SortLayer(ImgWall, info, cx, inv, doing, whoz, logy); // inserts it
                    xx = ink >> MxLayShf; // ink = 0..MaxLayers+3=7, MxLayShf = 3
                    ink = ink & ((1 << MxLayShf) - 1); // 0..3: saved in layer xx; >3 if unsaved
                    nx = doing >> 5;
                    if (ShoNos) if (nx >= 0) if (nx < 8) {
                        ipx = DidCells[nx];
                        nuly = 1 << (doing & 31);
                        if ((ipx & nuly) == 0) { // ink<MaxLayers if can see this wall..
                            if (ink < MaxLayers) DidCells[nx] = ipx | nuly;
                            if (looky == 0) if (Mini_Log) if ((Qlog & 256) != 0)
                                System.out.println(HandyOps.Dec2Log(" [ShoNos] ", doing,
                                        HandyOps.Dec2Log(" +", cx, HandyOps.Dec2Log(": ", ink,
                                                HandyOps.Dec2Log(" [", nx, HandyOps.Int2Log("] = ", ipx | nuly,
                                                        HandyOps.Int2Log("/", ipx, "")))))));
                        }
                    } //~if
                    if (looky == 0) if (xx > 0) if (Mini_Log) if ((Qlog & 256) != 0) {
                        if (maxz > 333) xx = 0;  // rx is 0-base frac'l row (from horizon)..
                        else if ((rx >> 23) + ImHaf != TripLine) if (zx == 0) if (offx < wide)
                            if (doing != whoz) if (doing != TripLine) if (maxz > 33)
                                if (cx != Hx) if (cx + 1 < Hz) if (((TripLine + cx) & 0xFFFF) == 0)
                                    if (cx != (ImWi >> 1)) xx = 0;
                        if (xx > 0) { // show details for this column..
                            if (!sawn) {
                                sawn = (xx > 1); // didn't see doing line, show partial here..
                                xx = ix0 | ix1 | ix2 | ix3;
                                System.out.println(HandyOps.Dec2Log(" _  __", doing,
                                        HandyOps.TF2Log(" ", ImgWall, HandyOps.Dec2Log(" .. @ ", Pmap,
                                                HandyOps.Int2Log(" [", ix0,
                                                        HandyOps.IffyStr(xx == 0, "]",
                                                                HandyOps.Int2Log(" ", ix1, HandyOps.Int2Log(" ", ix2,
                                                                        HandyOps.Int2Log(" ", ix3, "]")))))))));
                                xx = HandyOps.NthOffset(0, HandyOps.Dec2Log("@ ",
                                        Pmap, " "), Save4Log);
                                if (xx > 0) {
                                    RtnStr = HandyOps.NthItemOf(true, 1,
                                            HandyOps.Substring(xx, 99, Save4Log));
                                    if (RtnStr != "") {
                                        xx = HandyOps.NthOffset(0, "=", RtnStr);
                                        if (RtnStr.length() < 72) xx = 0;
                                        if (xx > 33) RtnStr = " __ "
                                                + HandyOps.Substring(0, xx, RtnStr)
                                                + "\n    " + HandyOps.RestOf(xx, RtnStr);
                                        else RtnStr = "   __ " + RtnStr;
                                        System.out.println(RtnStr);
                                    }
                                } //~if // (didn't see doing line)
                                xx = 1;
                                if (sawn) xx = 6;
                                sawn = false;
                            } //~if
                            else sawn = (cx > Hx); // T: omit 2nd line (cuz no inner loop)
                            RtnStr = HandyOps.Int2Log(" [", inv,   // if (!gotit) if (inv>0)
                                    HandyOps.Fixt8th("=", inv >> 5, "]")) + " | ";
                            if ((WideRatio > 0) || frnt) if (TempStr != "")
                                RtnStr = HandyOps.Dec2Log(" ", WideRatio, HandyOps.Fixt8th("=",
                                        WideRatio >> 13, HandyOps.TF2Log("/", frnt, " "))) + RtnStr;
                            if (TmpI > 0) RtnStr = HandyOps.Dec2Log(" (", TmpI,
                                    HandyOps.Dec2Log(">", wide, ")")) + RtnStr;
                            if ((offx & -0x01000000) != 0) xx = 8;
                            else if ((offx & -0x10000) == 0) xx = 4;
                            if (ImgWall) if (offx < 256) xx = 2;              // (DoWall/F)
                            System.out.println(HandyOps.Dec2Log("    . . ", cx,
                                    HandyOps.Fixt8th(": ", rx >> 17,
                                            HandyOps.Hex2Log(" @ x", offx, xx, HandyOps.Int2Log(": ", info,
                                                    HandyOps.Dec2Log(" +", kx, HandyOps.Fixt8th("=", kx >> 5,
                                                            HandyOps.TF2Log(" ", gotit, HandyOps.Dec2Log(" ", full,  //_9
                                                                    HandyOps.TF2Log(RtnStr, ImgWall, HandyOps.IffyStr(sawn, TempStr,
                                                                            HandyOps.Dec2Log("\n     ..{", Hx, HandyOps.Dec2Log("..", Hz,
                                                                                    HandyOps.Int2Log(" +", Vx, HandyOps.Fixt8th("=", (Vx * 100) >> 17,
                                                                                            HandyOps.Dec2Log("% +", fz, HandyOps.Flt2Log(" ", Hpm,
                                                                                                    HandyOps.Fixt8th(" (|", orient, HandyOps.Dec2Log(" ", ink,
                                                                                                            HandyOps.TF2Log(") ", rpt, HandyOps.Flt2Log("/", roff,   //_21
                                                                                                                    HandyOps.Flt2Log(" ", pct, HandyOps.Flt2Log(" ", ppm,
                                                                                                                            HandyOps.Flt2Log("(=", ppm / 256.0, "*256)}"
                                                                                                                                    + TempStr))))))))))))))))))))))));
                            sawn = true;          // HandyOps.Dec2Log("*256) ",stip,
                            TempStr = ""; // (TmpSt is left over from prep B4 loop)
                            RtnStr = "";
                            maxz++;
                        }
                    } //~if // (show details) (logy>0||-TripLine=cx)
                    TmpI = 0; // (done)
                    rx = rx + Vx; // step the base of this (doing) wall segment
                    if (!ImgWall) continue;
                    nx = 0x10000;
                    xx = inv; // xx: ip*256/sp
                    if (ix3 < 0) xx = full; // gotta exactly fill the frame
                    else if (frnt) while (true) { // frnt = (WideRatio>2)
                        nx = 0x20000;
                        xx = xx * WideRatio;
                        if (!gotit) if (stip > 0) {
                            xx = (xx * stip + 0x8000) >> 16;
                            break;
                        } //~if
                        xx = (xx + 128) >> 8;
                        break;
                    } //~while
                    else if (!gotit) if (stip > 0) {
                        xx = (xx * stip + 128) >> 8;
                        nx = 0x30000;
                    } //~if
                    if (xx != inv) whom = xx + nx; // if (Mini_Log) if (looky==0) // (for log)
                    offx = offx + xx;
                } //~for // (cx: each visible H-pix)
                kx = logz & 0xFFF;
                zx = (logz >> 16) - kx;
                if (zx > 0) {
                    zx = zx + 4;
                    kx = Math.max(kx - 2, 0);
                }
                logz = 0;
                if (looky == 0) if (whoz > 0) logz = ImWi;
            } //~for // (doing: each wall)
            if (GroundsColors < 0) if (SeenWall[LayerSz] == 0) SeenWall[LayerSz]++;
            why = 0; // (find all visible walls)
            break;
        } //~while // (precalc) (once through, precalc & render)
        if (Mini_Log) if ((Qlog & 0x900) != 0) { // Qlog&2048|Qlog&256
            TempStr = "]"; // if (Qlog..) -- included in M_L
            if (looky == 0) {                  // see "<RowCeil>" in log..
                if (doit) TempStr = TempStr + HandyOps.Flt2Log(" ", CeilingHi, "");
                else TempStr = TempStr + Save4Log;
            } //~if
            if ((FrameNo < 5) || (why > 7) || (looky == 0))
                System.out.println(HandyOps.TF2Log("{DW} ", doit,
                        HandyOps.Dec2Log("/", FrameNo, HandyOps.Dec2Log("/", looky,
                                HandyOps.Int2Log(" ", nBax, HandyOps.Dec2Log(" = ", why,  // why =
                                        HandyOps.Int2Log(" ", whom, HandyOps.Dec2Log(" ", whoz,
                                                HandyOps.Int2Log(" ", looky,
                                                        HandyOps.Flt2Log(" ", engle, HandyOps.Flt2Log("+", Facing,
                                                                HandyOps.Dec2Log(" @ ", Pmap, HandyOps.Dec2Log("/", ipx,
                                                                        HandyOps.IffyStr(Pmap < 8, "", HandyOps.Int2Log(": [", ix0,
                                                                                HandyOps.IffyStr((ix0 | ix1 | ix2 | ix3) == 0, TempStr,
                                                                                        HandyOps.Int2Log(" ", ix1, HandyOps.Int2Log(" ", ix2,
                                                                                                HandyOps.Int2Log(" ", ix3, TempStr)))))))))))))))))));
            if (nBax > 0) if (looky == 0) // if (!doit)
                System.out.println(HandyOps.Dec2Log("(SeeWal) ", FrameNo,
                        HandyOps.Dec2Log("=", LookFrame, HandyOps.TF2Log(" ", doit,
                                " " + HandyOps.ArrayDumpLine(SeenWall, 0, 15)))));
            TempStr = "";
            RtnStr = "";
        }
    } //~DoWalls

    /**
     * Converts a park coordinate in meters to the scenery color to display
     * there, usually track pavement or non-track "grass" (or carpet or whatever).
     * The coordinates are given in floating-point park meters, because you get
     * a very precise edge of the track with a resolution something on the order
     * of 3mm (1/8th inch) so that the white line looks credible, even very close
     * to the car.
     *
     * @param optn The bits of this parameter specify options:
     *             +1 gives walls as small negative numbers,
     *             so caller can branch off to build vertical walls;
     *             +2 returns the car color on those locations covered by it;
     *             +4 darkens walls (for map, so they are distinct from white);
     *             +5 ignores CheckerBd (flat track)
     *             +8 returns track values directly, all else as 0
     * @param Vat  The vertical coordinate, in park meters
     * @param Hat  The horizontal coordinate, in park meters
     * @param ink  The minimum width of the white line so it's visible
     *             even at distances (this should be slightly wider than
     *             your pixel width in park meters)
     * @return An 0x00RRGGBB color, or zero if outside the park, or
     * a number -4 to -1 for walls with optn=1.
     */
    public int MapColor(int optn, double Vat, double Hat, double ink) {
        double whom = WhitLnSz,     // optn: +1 rtns walls<0, +8 rtns non-trk=0
                lxx, ledge = MyMath.fMin(MyMath.fMax(ink, whom), 1.4), // Vat,Hat: pk m's
                Vstp = Vsee, Hstp = Hsee, deep = 0.0, Mconst = 0.0, Kconst = 0.0;
        final boolean ShoWallz = DriverCons.D_ShoWallz;
        boolean BGed = false;
        int why = 0, thar = 0, whar = 0, Vx = 0, Hx = 0, info = 0, colo = 0,
                more = 0, Wallz = -1, rx = MyMath.Trunc8(Vat * 128.0), yx = rx >> 7,
                cx = MyMath.Trunc8(Hat * 128.0), px = (rx << 16) + cx, kx = (px >> 5) & 0x3FF03FF,
                zx = cx >> 7, nx,
                looky = FrameNo - 1,
                Pmap = (yx >> 1) * HalfMap + (zx >> 1) + MapIxBase, // max(Pmap) ~ 24K?
                kolo = PavColo | Truk, optx = optn; // rcx = optn&0xFFFFFF0,
        boolean car2 = (optn & 2) != 0, flat = (optn & 5) == 5, tkonly = (optn & 8) != 0,
                tript = (TripLine != 0) && GoodLog, logy = optn < 0;
        // TripLine specifies park meters south (here) or pixel row (in caller)
        int[] theLum = LuminanceMap;                 // MapIxBase (approx) = 12821
        int[] theImgs = TrakImages;
        int[] theInx = MapIndex;
        // rcx = rcx&0xFFF0000|(rcx>>4)&0xFFF; // now: ScrnRoCo
        if (tript) if (yx != TripLine) if (zx + TripLine != 0) tript = false;
        rx = yx; // rx,cx now both in pk meters, same as Vat,Hat..     // " (MapCol) "
        cx = zx;
        zx = ScrnRoCo & 0xFFFF;
        yx = ScrnRoCo >> 16;
        if (LookFrame > 0) looky = FrameNo - LookFrame;
        if (Mini_Log) if (looky == 0) if (!logy) if (yx > 0) if (zx > 0) {
            if (yx == TripLine) logy = true;
            else if (zx + TripLine == 0) logy = true;
            else {
                if (zx + 1 == ImWi) logy = true;
                else if (zx == (ImWi >> 1)) logy = true;
            }
        }
        // tnt = 0; if ((optn&4) !=0) tnt = -0x333333; // darkens walls in map // **
        TmpI = Pmap; // returned cell+ (some callers care)
        optn = optn & 1; // =1 when building camera view, so to get wall info
        while (true) { // once thru (exit when done)..
            why++; // why = 1
            if (rx < 0) break;
            if (rx >= MapHy) break;
            why++; // why = 2
            if (cx < 0) break;
            if (cx >= MapWy) break; // ........................................... car
            why++; // why = 3
            if (car2) while (true) { // CarTest, Vcarx,Hcarx,Lcarx,Rcarx,Tcarx,Bcarx,
                car2 = tript; // (for log)
                whar = (rx << 16) - CarTest + cx; // = 0..5,0..5 if near/on car
                if ((whar & 0x80008000) != 0) break; // above or left (negative)
                why = why + 8; // why = +8 => 15/16
                if (((0x50005 - whar) & 0x80008000) != 0) break; // below or right
                why = why + 8; // why = +16 => 23/24
                car2 = true;
                whom = Vat * Vcarx + Hat;
                if (whom < Lcarx) break;
                why = why + 8; // why = +24 => 31/32
                if (whom > Rcarx) break;
                why = why + 8; // why = +32 => 39/40
                whom = Hat * Hcarx + Vat;
                if (whom < Tcarx) break;
                why = why + 8; // why = +40 => 47/48
                if (whom > Bcarx) break;
                why = why + 8; // why = +48 => 51 "-> F09"
                if (CarColo > 7) colo = CarColo;
                else colo = 0x10101;
                kolo = 0; // enable luminance
                break;
            } //~while // (CarTest)
            if (colo != 0) break; // why = +3 (why = 51)
            why++; // why = +4
            if (theInx == null) break;
            if (theInx.length > 8) thar = theInx[2] + GridSz; // GridSz=12800
            why++; // why = +5                            // theInx[2]=355
            if (Pmap < 0) break;
            if (Pmap >= theInx.length) break;
            info = theInx[Pmap]; // ...................................... info=[Pmap]
            if (optx == 0) return info;
            why++; // why = +6
            if (tkonly) { // tkonly = (optn&8)
                if (info < 0) colo = info;
                else if (info > 0x40000000) if (info < 0x60000000) colo = kolo;
                break;
            } //~if // why = +6/14/.. // all others default =0
            if (info == 0) { // outside the park..
                why++; // why = +7
                if (car2) break; // no back walls in map
                if (GroundsColors > 0) break; // no back walls if outdoors
                // if (Wally==0) break; // still behind last BG (unset)
                if (OpMode == 3) if (FrameNo < 2) break; // bad car placement
                why++; // why = +8
                info = 5;
            } //~if // indoors, so put BackWall there (outside the park)
            else if (info > 0x77000000) info = 7; // (obsolete) unPainted
            // zx = ScrnRoCo&0xFFFF;
            // yx = ScrnRoCo>>16;
            if (info > 0) if (info < 0x40000000) // old style wall..
                BGed = true; // just darken the grass under wall
            why++; // why = +7/+8/+9 ........................................... paint
            if (!flat) if (RectMap > 0) while (true) { // we have paint (see Ad2PntMap)
                if (info >= 0) if (info < 0x40000000) break; // ..but not through walls
                why = why + 0x10000; // why = 1,x // rx,cx both in meters..
                thar = (rx << 3) + (cx >> 5) + RectMap;
                if (thar <= 0) break;
                if (thar >= theInx.length) break;
                why = why + 0x10000; // why = 2,x
                whar = theInx[thar];
                if (whar == 0) break; // no paint near here..
                why = why + 0x10000; // why = 3,x
                if (theImgs == null) { // can't
                    theImgs = TrakImages;
                    if (theImgs == null) break;
                } //~if
                why = why + 0x10000; // why = 4,x
                nx = whar << (cx & 31);
                if (nx >= 0) break; // 'if ((whar<<(cx&31)) >= 0)' failed in T68
                thar = RectMap;
                // RectMap=theInx[ArtBase-1] = nuIxBase-3+GridSz+PaintIx // ArtBase=8
                //   nuIxBase=ArtBase+4+1; GridSz=12800; PaintIx=(nPaints+1)*3
                // >>==>> from wait=nuIxBase+GridSz to wait+PaintIx-1 is paint index
                // from wait+PaintIx to thar=wait+PaintIx+GridSz/8-1 is paint map
                // from thar=wait+PaintIx+GridSz/8 on is copy of theImgs
                why = why + 0x30000; // why = +7,x
                while (true) { // look for paint that covers Vat,Hat (3x cont)..
                    why = why + 0x10000; // why = +0(+8n),x
                    thar = thar - 3;
                    why = why + 0x10000; // why = +1(+8n),x
                    if (thar < 0) break;
                    Hx = theInx[thar + 2]; // hi&wi as displayed
                    why = why + 0x10000; // why = +2,x
                    if (Hx == 0) break; // normal exit if none
                    if (Hx < 0) continue; // (deleted)
                    more = theInx[thar]; // = offset of this pix in image +opt
                    Vx = kx - theInx[thar + 1]; // map posn of img => rel coord of pix in img
                    why = why + 0x10000; // why = +3,x
                    if ((Vx & 0x80008000) != 0) continue; // pix outside bounds rect..
                    if ((more & 0x04000000) != 0) { // hi-res (32ppm) image..
                        nx = (MyMath.Trunc8(Vat * 32.0) & 7) << 16; // get low-order fract bits..
                        Vx = (MyMath.Trunc8(Hat * 32.0) & 7) + (Vx << 3) + nx;
                    } //~if // so 8x better res
                    why = why + 0x10000; // why = +4 =12,x
                    if (((Hx - Vx) & 0x80008000) != 0) continue;
                    zx = Vx & 0xFFFF;
                    Vx = Vx >> 16;
                    nx = 0;
                    if ((more & 0x03000000) != 0) switch ((more >> 24) & 3) {
                        case 0: // no rotate..
                            Hx = zx;
                            break;
                        case 1: // rotate img 90 degs c-wise..
                            whar = Vx;
                            Vx = (Hx & 0xFFFF) - zx;
                            Hx = whar;
                            break;
                        case 2: // rotate img 180 degs..
                            Vx = (Hx >> 16) - Vx;
                            Hx = (Hx & 0xFFFF) - zx;
                            break;
                        case 3: // rotate img 90 degs c-c-wise..
                            whar = (Hx >> 16) - Vx;
                            Vx = zx;
                            Hx = whar;
                            break;
                    } //~switch
                    else Hx = zx;
                    more = (more & 0xFFFFFF) + Vx * ImageWide + Hx;
                    why = why + 0x10000; // why = +5,x
                    if (more < 0) break;
                    why = why + 0x10000; // why = +6,x
                    if (theImgs == null) break;
                    if (more >= theImgs.length) break;
                    nx = theImgs[more];
                    if (nx > 7) colo = nx; // unneed: &0xFFFFFF, cuz did it in WhiteAlf
                    else if (nx >= 0) colo = 0x010101; // (<0 is transparent)
                    why = why + 0x10000; // why = +7,x
                    if (colo == 0) continue;
                    kolo = 0; // enable luminance
                    why = why | 64;
                    break;
                } //~while // found paint (look for paint that covers)
                break;
            } //~while // (we have paint)
            if (colo != 0) break; // why = 71..115
            if (info > 0x70000000) { // no artifact (or tree)
                why = why | 0x100;
                info = 7;
            } //~if
            else if (flat) if (info > 7) if (info < 0x40000000) info = 7; // (doing BG)
            colo = GrasColo;
            if (info < 0) { // pavement edge crosses this square.................. track
                // Track edge consists of straight-line segments, mx+ny=k,
                //   starting and ending on cell edges; any vertex within
                //   a cell is bevelled to a straight line in that cell.
                // Every line segment is defined either more horiz or vert
                //   as the greater dimension (45 can be either), so inside
                //   the track in that cell is x+my<k (for m<1) or mx+y.
                // Two bits control whether to multiply m* V or H, and
                //   whether to compare > or <; two fix-point constants
                //   |m|<1.0 and |k|<2047.0 complete the table info.
                // Adjacent cells may share the same formula, which only
                //   defines a polarized abstract line on the map.
                // The white line is nominally 10" (25cm) wide inside the edge,
                //   but never less than one pixel for 1m pixels or smaller.
                //
                why = why | 128;
                Kconst = MyMath.Fix2flt((info & 0x0FFFF800) - (info & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((info & 0x03FF) - (info & 0x400), 10);
                // Horz = (info&0x20000000) ==0;
                // Grtr = (info&0x40000000) ==0;
                if ((info & 0x20000000) == 0) { // H-line..
                    deep = Hat * Mconst + Vat; // in meters if Mcon=0
                    if ((info & 0x40000000) == 0) { // > is in
                        if (deep > Kconst + ledge) colo = kolo; // gray
                        else if (deep >= Kconst) colo = WhitLnColo;
                    } //~if // white line
                    else if (deep + ledge < Kconst) colo = kolo; // gray
                    else if (deep <= Kconst) colo = WhitLnColo;
                } //~if // white
                else {
                    deep = Vat * Mconst + Hat; // V-line..
                    if ((info & 0x40000000) == 0) { // > is in
                        if (deep > Kconst + ledge) colo = kolo; // gray
                        else if (deep >= Kconst) colo = WhitLnColo;
                    } //~if // white
                    else if (deep + ledge < Kconst) colo = kolo; // gray
                    else if (deep <= Kconst) colo = WhitLnColo;
                } //~else
                if (ledge == 0.0) if (colo == WhitLnColo) colo = kolo; // (info<0)
                kolo = 0;
            } //~if // enable luminance
            else if (info > 0x40000000) { // grass or pavement
                why = why | 0x200;
                if (info < 0x60000000) colo = kolo; // gray
                // else colo = GrasColo; // (grass)
                kolo = 0;
            } //~if // enable luminance
            else if ((ScrnRoCo < 0x10000) || car2) { // any wall in map view..
                if (info >= WallColoz.length) colo = unPainted;
                else colo = WallColoz[info];
            } //~if
            else BGed = true; // (plain or image) wall
            if (FloorDims > 0) while (true) { // ............................. floor tile
                why = why | 0x2000; // px = ((rx<<16)+cx)*128
                yx = FloorDims >> 16;
                if (yx == 0) break;
                zx = FloorDims & 0xFFFF;
                if (zx == 0) break;
                nx = FloorOff >> 24; // 2^j pix/pkm
                kx = px & 0xFFFF;
                if (nx < 7) kx = kx >> (7 - nx);
                else if (nx == 8) kx = kx << 1;
                more = (FloorOff & 0xFFFFFF) + ((px >> (23 - nx)) % yx) * ImageWide + (kx % zx);
                if (more < 0) break;
                if (theImgs == null) break;
                if (more >= theImgs.length) break;
                zx = theImgs[more];
                if (zx < 8) break; // transparent
                colo = zx | Truk;
                kolo = 0; // enable luminance
                break;
            } //~while
            else if (!flat) { // if (!tkonly) { // (in MapColo) ............... pebble
                why = why | 0x1000;
                if (colo < 0) px = 0;
                else if (ink > PebBlur) px = 0;
                else if ((colo & Truk) == 0) px = 0;
                else if ((GroundsColors & 0x8000) == 0) px = 0;
                else if (ink == 0.0) if (WhitLnSz > 0.0) px = 0;
                if (px > 0) { // px = (rx<<16)+cx;
                    why = why | 0x2000;
                    yx = px >> (PebblSize + 2); // PebblSize = rx-3: 1..5, 2^ps in pk cm
                    zx = (yx >> 14) & -4 | yx & 3; // 5 bits of rx + 2 bits of cx
                    nx = yx & 28; // a different 3 bits of cx (total 5+5 bits -> 1K PiDig's
                    px = PiDigits[zx & 127] >> nx;
                    colo = PebleTrak[px & 15] | Truk;
                    kolo = 0;
                } //~if // enable luminance
                else if (colo > 0) px--;
                if (px != 0) kolo = 0; // enable luminance
                if (px < 0) if (CheckerBd > 0) if (((rx ^ cx) & CheckerBd) != 0) {
                    why = why | 0x4000;
                    if ((colo & Truk) != 0) colo = PavDk | Truk;
                    else if (colo == GrasColo) colo = GrasDk;
                }
            } //~if
            why++; // why = 8/9/10
            break;
        } //~while // (once thru)
        if (optx == 0) return colo; // unneed log/lum for recursive call
        if (BGed) colo = colo - ((colo & 0xF0F0F0) >> 2); // -25% under image wall
        px = 0; // ....................................................... luminance
        if (kolo == 0) if (theLum != null) while (true) {
            if (colo <= 0) break;
            px = rx * MapWide + cx;
            if (px < 0) break;
            if (px >= theLum.length) break;
            why = why | 0x8000;
            yx = theLum[px];
            if (yx == (1 << LumUniShif)) break;
            px = 1 << (LumUniShif - 1); // for rounding
            zx = colo >> 8;
            kx = (((zx >> 8) & 255) * yx + px) >> LumUniShif;
            nx = ((zx & 255) * yx + px) >> LumUniShif;
            zx = ((colo & 255) * yx + px) >> LumUniShif;
            px = MyMath.iMax(zx - 255, 0) + MyMath.iMax(nx - 255, 0) + MyMath.iMax(kx - 255, 0);
            px = px >> 2; // distribute 1/2 excess luminance to other colors..
            zx = (((MyMath.iMin(kx + px, 255) << 8) + MyMath.iMin(nx + px, 255)) << 8)
                    + MyMath.iMin(zx + px, 255);
            if ((zx & -8) == 0) zx = 0x10101;
            colo = colo & -0x01000000 | zx;
            break;
        } //~while // (luminance)
        if ((Qlog & 16) != 0) {
            if (GoodLog) if (tript) { // if (rx>0) // GoodLog=true ... TripLine
                if (RectMap == 0) logy = (colo != 0) || logy; // no paint (see Ad2PntMap)
                else if (NoisyMap)
                    if (why > 0x40000) logy = true; // (paint)
                if (((colo + 1) & 255) == 0) colo = colo & 0x7F7F9E; // show TripLine in blue..
                else colo = colo & -2 | 254;
            } //~if // (tript)
            if (logy) {
                System.out.println(HandyOps.Dec2Log(" (MapCol) ", rx,      // info=theInx[Pmap]
                        HandyOps.Dec2Log(" ", cx, HandyOps.Dec2Log(" ", Pmap,
                                HandyOps.Hex2Log(" = x", info, 8, HandyOps.Colo2Log(" -> ", colo,
                                        HandyOps.Int2Log(" .. ", whar, HandyOps.Dec2Log("/", thar,
                                                HandyOps.Dec2Log("_", RectMap, HandyOps.Flt2Log(" ", Kconst,
                                                        HandyOps.Flt2Log("/", Mconst, HandyOps.Flt2Log(" ", ledge,
                                                                HandyOps.Flt2Log(" ", deep, HandyOps.Flt2Log("/", whom,
                                                                        HandyOps.Flt2Log("/", ink, HandyOps.Int2Log(HandyOps.IffyStr((colo < 0)
                                                                                        || ((colo & Truk) == 0) && !BGed, "\n   -tk (",
                                                                                HandyOps.IffyStr(BGed, "\n   +Bg (", "\n   +Tk (")), ScrnRoCo,
                                                                                HandyOps.Int2Log(") = ", why,                                  // why =
                                                                                        HandyOps.TF2Log(" c2=", car2, HandyOps.IffyStr(kolo == 0, " *lum",
                                                                                                "")))))))))))))))))));
                TempStr = "";
            }
        } //~if // (Qlog&16)
        return colo;
    } //~MapColor // TmpI = Pmap = cell+

    /**
     * Draw on-screen an image portion from TrakImages.
     *
     * @param rx   The top pixel row of the image on the screen
     * @param cx   The left pixel column
     * @param tall The image height in pixels
     * @param wide The image width in pixels
     * @param here The image offset in TrakImages, in ints/pix from top-left
     * @param colo The border color, -1 omits
     */
    public void SeeOnScrnPaint(int rx, int cx, int tall, int wide, int here,
                               int colo) { // show image portion on-screen..
        int thar, lino, posn, info; //  here = SeePaintImgP,
        //  rx = SeePaintTopL>>16, cx = SeePaintTopL&0xFFFF,
        //  tall = (SeePaintSize>>16)-1, wide = (SeePaintSize&0xFFFF)-1,
        //  int more = SceneWide, didit = 0; int[] myPix = myScreen;
        int[] theImgs = TrakImages;
        if (theImgs == null) return;
        if (Qlog < 0) System.out.println(HandyOps.Dec2Log(" (SeePint) ", rx,
                HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" ", tall,
                        HandyOps.Dec2Log("/", wide, HandyOps.Dec2Log(" ", here, ""))))));
        if (rx < 0) return;
        if (cx < 0) return;
        if (here < 0) return;
        if (here >= theImgs.length) return;
        for (lino = 0; lino <= tall; lino++) {
            thar = here;
            here = here + ImageWide;
            for (posn = 0; posn <= wide; posn++) {
                if (thar < 0) break;
                if (thar >= theImgs.length) break;
                info = theImgs[thar];
                thar++;
                if (info < 0) {
                    if (colo < 0) continue;
                    info = colo;
                } //~if
                PokePixel(info, rx + lino, cx + posn);
            }
        } //~for
        if (colo < 0) return;
        if (rx == 0) return;
        if (rx + tall + 2 > ImHi) return;
        if (cx + wide + 2 > WinWi) return;
        DrawLine(MarinBlue, rx - 1, cx - 1, rx - 1, cx + wide + 1); // frame it..
        DrawLine(MarinBlue, rx + tall + 1, cx - 1, rx + tall + 1, cx + wide + 1);
        DrawLine(MarinBlue, rx, cx - 1, rx + tall, cx - 1);
        DrawLine(MarinBlue, rx, cx + wide + 1, rx + tall, cx + wide + 1);
    } //~SeeOnScrnPaint

    private void DoPixSteps() { // only needed when we do fisheye lens..
        int why = 0; // pre-calc PixelSteps m/px vector @1m..
        while (true) {
            why++; // why = 1
            break;
        } //~while
        // **
        ValidPixSteps = true;
    } //~DoPixSteps

    private void BuildFrame() { // no early return, always logs; frm GetSimFrame
        final boolean DeepDark = false; // DriverCons.D_DeepDark;
        double ledge, Vat = Vposn, Hat = Hposn, fHafIm = FltWi * 0.5 + 1.0,
                GridTall = MyMath.Fix2flt(HalfTall, 0), // Mconst, Kconst,
                GridWide = MyMath.Fix2flt(HalfMap, 0), Vbase, Hbase, Vpx, Hpx, ftmp;
        int zx, r8, info, Vmap, Hmap, Pmap, looky = FrameNo - 1, // chekt, whar, rotst,
                bitz = -1, optn = 0,
                why = 0, robe = 0, Lww = 0, Rww = 0, LwL = 0, RwL = 0, Mtrk = 0,
                rx = 0, cx = 0, kx = 0, dx = 0, nx = 0, yx = 0, doit = 0, colo = 0,
                step = 0, floor = 0, far = 0, here = 0, thar = nPixels; // tBase = 0,
        double deep = 0.0, Vinc = 0.0, Hinc = 0.0, Vstp = 0.0, Hstp = 0.0;
        String aWord = "", myDash = "", LefDash = "", MayLog = "", OopsLog = "";
        boolean logy = false, seen = false, solidGry = false; // Horz, Grtr;
        int[] theInx = MapIndex;
        int[] myPix = myScreen;
        try {
            why--; // why = -1 // (only seen if exception)
            FakeRealTime = FakeRealTime + FrameTime; // steadily increments
            FrameNo++;
            Wally = 0;
            PixScale = 0;
            if (LookFrame + FrameNo == 0) LookFrame = 0;
            else if (LookFrame > 0) looky = FrameNo - LookFrame;
            while (Facing < 0.0) Facing = Facing + 360.0;
            while (Facing > 360.0) Facing = Facing - 360.0;
            why--; // why = -2
            if ((DrawDash > 0) || (ZooMapDim != 0)) {
                kx = (MyMath.Trunc8(Facing + Facing) + 22) / 45; // if (DrawDash>0) {
                aWord = CompNames[kx & 15];
                myDash = HandyOps.Flt2Log(" ", Vposn, HandyOps.Flt2Log("S ", Hposn,
                        HandyOps.Flt2Log("E -- ", Facing, HandyOps.Dec2Log(aWord, GasBrake, ""))));
                if (DroppedFrame > 0) LefDash = " (-" + DroppedFrame + ") ";
                else LefDash = " ";
                DroppedFrame = 0;
                LefDash = " " + HandyOps.Dec2Log("", SteerWhee,
                        HandyOps.Dec2Log(LefDash, FrameNo, HandyOps.IffyStr(LookFrame == 0, "",
                                HandyOps.Dec2Log(" (", LookFrame, ")"))));
            } //~if
            why--; // why = -3
            if ((Qlog & 0x980) != 0) // (Qlog&128)|(Qlog&256)|(Qlog&2048)
                System.out.println(HandyOps.Dec2Log("(..BF..) ", FrameNo, // (**frozen format**)
                        HandyOps.Dec2Log(" ", NuData, HandyOps.Int2Log(" ", ZooMapDim, " [ s="
                                + HandyOps.Dec2Log(LefDash + HandyOps.IffyStr(OpMode == 3, "# ! ", "# / ")
                                + myDash + " =g ] ", ImHi - DrawDash, HandyOps.Int2Log(" ", TripLine,
                                HandyOps.Flt2Log(HandyOps.IffyStr(SimSpedFixt, " [Fx ", " [v="), Velocity,
                                        HandyOps.Dec2Log("] ", optn, HandyOps.PosTime(" @ ")))))))));
            if (false) if (Mini_Log) if (Qlog < 0) if (theInx != null) if (FrameNo == 1)
                System.out.println("  =-=" + HandyOps.ArrayDumpLine(theInx, 123, 5)); // (An8 does)
            why--; // why = -4
            if (Log_Draw) OopsLog = " %";
            Vmap = MyMath.Trunc8(Vposn); // = current posn in park meters (2x map grid)
            Hmap = MyMath.Trunc8(Hposn);
            why = 0;
            while (true) { // (once through, exit early if error)
                NuData = 0;
                IsProxim = false;
                why++; // why = 1
                if (myPix == null) break; // (only early exit)
                if (PrioRaster == null) break;
                for (cx = 0; cx <= ImWi - 1; cx++) PrioRaster[cx] = 0;
                why++; // why = 2 // if (DoScenery||ShowMap) {
                MyMath.Angle2cart(Facing); // angles are in degrees, not radians
                HsiFace = MyMath.Sine; // sin grows to right, step E to edge
                VcoFace = -MyMath.Cose; // cos shrinks down (sin^2+cos^2 = 1m) step S
                if (SeenWall != null) {
                    for (nx = LayerSz; nx >= 0; nx += -1) SeenWall[nx] = 0;
                    if (VuEdge == 0.0) NuData++;
                    else DoWalls(false);
                    if (Mini_Log) if (looky == 0) if ((Qlog & 256) != 0)
                        if (nBax > 0) System.out.println(HandyOps.Int2Log("(SeeWal_) ", nBax,
                                " --" + HandyOps.ArrayDumpLine(SeenWall, 0, 16)));
                }
                // if (!ValidPixSteps) DoPixSteps(); // only for fisheye lens
                // MyMath.Angle2cart(Facing+90.0); // this now points left-to-right,
                Vinc = HsiFace; // -MyMath.Cose; // for stepping across the view screen
                Hinc = -VcoFace; // MyMath.Sine;

                ZooMapTopL = 0;                    // calc edges of close-up map..
                yx = ZooMapDim >> 16; // close-up size, in park meters
                zx = ZooMapDim & 0xFFF; // park size: MapHy,MapWy; Vmap=Vposn
                if (ShowMap) if (DoCloseUp) if (ZooMapDim != 0) while (true) {
                    if (RasterMap != null)
                        for (nx = RasterMap.length - 1; nx >= 0; nx += -1) RasterMap[nx] = 0.0;
                    // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
                    if (zx >= MapWy) if (yx >= MapHy) break; // no offset needed
                    if (MyMath.Trunc8(TurnRadius * 2.0) + 4 > yx) step = 2;
                    else step = 1; // in park meters, how much car to show (at edge)
                    kx = (step << 16) + kx; // was: =MyMath.Trunc8((Facing+22.5)/45.0)
                    switch (kx & 15) {
                        case 0:
                        case 1:
                        case 15: // facing north..
                            rx = Vmap + step - yx; // now top of close-up, in park meters
                            cx = Hmap - (zx >> 1); // left edge of close-up, ditto
                            break;
                        case 2: // facing north-east..
                            rx = Vmap + step - yx;
                            cx = MyMath.iMin(Hmap - step, MapWide - zx);
                            break;
                        case 3:
                        case 4:
                        case 5: // facing east..
                            rx = Vmap - (yx >> 1);
                            cx = MyMath.iMin(Hmap - step, MapWide - zx);
                            break;
                        case 6: // facing south-east..
                            rx = MyMath.iMin(Vmap - step, MapTall - yx);
                            cx = MyMath.iMin(Hmap - step, MapWide - zx);
                            break;
                        case 7:
                        case 8:
                        case 9: // facing south..
                            rx = MyMath.iMin(Vmap - step, MapTall - yx);
                            cx = Hmap - (zx >> 1);
                            break;
                        case 10: // facing south-west..
                            rx = MyMath.iMin(Vmap - step, MapTall - yx);
                            cx = Hmap + step - zx;
                            break;
                        case 11:
                        case 12:
                        case 13: // facing west..
                            rx = Vmap - (yx >> 1);
                            cx = Hmap + step - zx;
                            break;
                        case 14: // facing north-west..
                            rx = Vmap + step - yx;
                            cx = Hmap + step - zx;
                            break;
                    } //~switch
                    bitz = (rx << 16) + cx; // for log
                    if (rx > 0) if (rx + yx > MapHy) rx = MapHy - yx;
                    if (rx < 0) rx = 0;
                    if (cx > 0) if (cx + zx > MapWy) cx = MapWy - zx;
                    if (cx < 0) cx = 0;
                    if (rx + cx == 0) break; // offset is 0
                    ZooMapTopL = (rx << 16) + cx; // in park meters
                    break;
                } //~while // (DoCloseUp) // ZooMapShf cvts c-u(pix) <-> meters

                // calc: Vcarx, Hcarx, Lcarx, Rcarx, Tcarx, Bcarx, CarTest..
                //   Vat,Hat is in car if:  CarTest<(Vat,Hat)<(CarTest+5,5)
                //     && Lcarx<Vat*Vcarx+Hat<Rcarx && Tcarx<Hat*Hcarx+Vat<Bcarx
                logy = Log_Draw && (looky == 0);
                if (Qlog == 0) logy = false;
                else if (!Mini_Log) logy = false;
                else if (looky == 0) if ((Qlog & 2048) != 0) logy = true;
                Vbase = Vposn + VcoFace + Vinc; // right front corner of car
                Hbase = Hposn + HsiFace + Hinc;
                Vpx = Vposn - (VcoFace * 3.0 + Vinc); // left rear corner
                Hpx = Hposn - (HsiFace * 3.0 + Hinc);
                deep = MyMath.fMin(MyMath.fMin(Vbase - 2.0 * Vinc, Vbase),
                        MyMath.fMin(Vpx + Vinc * 2.0, Vpx)); // northmost corner
                ledge = MyMath.fMin(MyMath.fMin(Hbase - 2.0 * Hinc, Hbase),
                        MyMath.fMin(Hpx + Hinc * 2.0, Hpx)); // westmost corner
                CarTest = (MyMath.Trunc8(deep) << 16) + MyMath.Trunc8(ledge); // ...........
                if (logy) {
                    System.out.println(HandyOps.Int2Log(" (CarTst) ", CarTest,
                            HandyOps.Flt2Log(" ", Facing, HandyOps.Flt2Log(" @ ", Vposn,
                                    HandyOps.Flt2Log("/", Hposn, HandyOps.Flt2Log(" + ", VcoFace,
                                            HandyOps.Flt2Log("/", HsiFace, HandyOps.Flt2Log(" > ", Vinc,
                                                    HandyOps.Flt2Log("/", Hinc, HandyOps.Flt2Log(" -> ", Vbase,
                                                            HandyOps.Flt2Log("/", Hbase, HandyOps.Flt2Log(" .. ", Vpx,
                                                                    HandyOps.Flt2Log("/", Hpx, aWord)))))))))))));
                    if ((Qlog & 2048) != 0) logy = false;
                    aWord = "";
                } //~if
                // Northward facing, calc Lc,Rc,Tc,Bc, same (VcoFace,HsiFace)?
                // Southward facing, calc Pc,Dc,Ac,Zc, all numbers =N
                // Westward facing, calc Sc,Nc,Wc,Ec, same (VcoFace,HsiFace)?
                // Eastward facing, calc Cc,Oc,Yc,Fc, all numbers =W

                if (MyMath.fAbs(VcoFace) > MyMath.fAbs(HsiFace)) { // more N-S than E-W..
                    if (VcoFace < 0.0) { // (car aimed north)..
                        LineSlope(Vpx, Hpx, -VcoFace, HsiFace, logy, "Lc="); // using left-rear corner
                        Lcarx = Hoffm - 0.1; // = HsiFace/VcoFace*Vpx+Hpx (HsiFace=0 if directly N)
                        Vcarx = Voffm;
                        LineSlope(Vbase, Hbase, 0.0, 0.0, logy, "Rc="); // using right-front
                        Rcarx = Hoffm + 0.1;
                        LineSlope(Hbase, Vbase, Hinc, -Vinc, logy, "Tc="); // also as north
                        Tcarx = Hoffm - 0.1;                    // ..(just flip V <-> H)
                        Hcarx = Voffm;
                        LineSlope(Hpx, Vpx, 0.0, 0.0, logy, "Bc="); // using left-rear as S
                        Bcarx = Hoffm + 0.1;
                    } //~if // (aimed north)
                    else { // (car aimed south, same as N, but sin/cos both neg'd)..
                        LineSlope(Vbase, Hbase, VcoFace, -HsiFace, logy, "Pc="); // using right-front
                        Lcarx = Hoffm - 0.1;          // ..cuz passenger side is facing west
                        Vcarx = Voffm;
                        LineSlope(Vpx, Hpx, 0.0, 0.0, logy, "Dc="); // driver side is east
                        Rcarx = Hoffm + 0.1;
                        LineSlope(Hpx, Vpx, -Hinc, Vinc, logy, "Ac="); // using left-rear as N
                        Tcarx = Hoffm - 0.1;
                        Hcarx = Voffm;
                        LineSlope(Hbase, Vbase, 0.0, 0.0, logy, "Zc="); // right-front as S
                        Bcarx = Hoffm + 0.1;
                    }
                } //~if // (aimed south) (more N-S)
                else if (HsiFace < 0.0) { // more E-W than N-S (car aimed west)..
                    LineSlope(Hpx, Vpx, -HsiFace, VcoFace, logy, "Sc="); // left-rear faces S
                    Bcarx = Hoffm + 0.1;
                    Hcarx = Voffm;
                    LineSlope(Hbase, Vbase, 0.0, 0.0, logy, "Nc="); // other corner faces N
                    Tcarx = Hoffm - 0.1;
                    LineSlope(Vbase, Hbase, -Vinc, Hinc, logy, "Wc="); // also west
                    Lcarx = Hoffm - 0.1;
                    Vcarx = Voffm;
                    LineSlope(Vpx, Hpx, 0.0, 0.0, logy, "Ec="); // left-rear is east
                    Rcarx = Hoffm + 0.1;
                } //~if // (aimed west)
                else { // (car aimed east, same as W, but sin/cos both neg'd)
                    LineSlope(Hbase, Vbase, HsiFace, -VcoFace, logy, "Cc="); // right-front as S
                    Bcarx = Hoffm + 0.1;
                    Hcarx = Voffm;
                    LineSlope(Hpx, Vpx, 0.0, 0.0, logy, "Oc="); // other corner faces N
                    Tcarx = Hoffm - 0.1;
                    LineSlope(Vpx, Hpx, Vinc, -Hinc, logy, "Yc="); // back also west
                    Lcarx = Hoffm - 0.1;
                    Vcarx = Voffm;
                    LineSlope(Vbase, Hbase, 0.0, 0.0, logy, "Fc="); // right-front is east
                    Rcarx = Hoffm + 0.1;
                } //~else // (aimed east, more E-W)
// ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
// (car) +1 90. +D 0./1. +W 1./0. CT=23,29 Vc=0./0. Tc=22.9/25.1 Lc=28.9/33.1 0
//   .. 1,4 8,31 ZT=8,31 23./29. 23./29.
                if (!Mini_Log) logy = false;
                else if (looky != 0) logy = false;
                else if ((Qlog & 2048) != 0) logy = true;
                else if (Qlog == 0) logy = false;
                else if (Qlog < 0) logy = !logy;
                if (logy) System.out.println(HandyOps.Dec2Log("// (car) #", FrameNo,
                        HandyOps.TF2Log(" ", looky == 0,          // looky = FrameNo-LookFrame;
                                HandyOps.Flt2Log(" ", Facing, HandyOps.Flt2Log(" +D ", VcoFace,
                                        HandyOps.Flt2Log("/", HsiFace, HandyOps.Flt2Log(" +W ", Vinc,
                                                HandyOps.Flt2Log("/", Hinc, HandyOps.Int2Log(" CT=", CarTest,
                                                        HandyOps.Int2Log(" ", GroundsColors, HandyOps.Int2Log(" [", Left_X,
                                                                HandyOps.Int2Log("/", Rite_X, HandyOps.PosTime("] @ ")))))))))))));
                // seen = false; // if (DoScenery) {
                if (theInx == null) break; // why = 2
                why++; // why = 3
                if (theInx.length < 8) break;
                // PxWi = ImageWide; // theInx[0]&0xFFFF; // width of images frame
                // iBase = theInx[3]; // map dimensions (unneed, fixed at 256x200)
                // eBase = theInx[2]; // 1st edge spec (obsolete, now in index)
                // MapIxBase = theInx[2]; // front of 2x2m map (InitIndx got it)
                // tBase = theInx[1]; // front of texture map
                step = (0xFF00 - 0x3300) / (ImHaf >> 1);
                why = 4;
                if (TopCloseView == 0) kx = MapHy;
                else if (TopCloseView <= MapHy) kx = TopCloseView - 1;
                else kx = MapHy;
                for (rx = ImHi - 1; rx >= 0; rx += -1) { // fill defaults..................................
                    logy = false;
                    if (Mini_Log) if (TripLine > 0) if (looky == 0) { // Log_Draw=F, GoodLog=T..
                        if (rx == TripLine) logy = Log_Draw || GoodLog || ((Qlog & 2048) != 0);
                        else if (Qlog < 0) if (rx + 3 > TripLine) if (rx - 3 < TripLine)
                            logy = GoodLog;
                    } //~if
                    // if (rx<4) logy = Log_Draw; else
                    // if (rx>ImHi-5) logy = Log_Draw;
                    // else if (rx>ImHaf+2) logy = false;
                    // else if (rx>ImHaf-8) logy = Log_Draw;
                    // else if ((rx&15)==0) logy = Log_Draw;
                    if (rx >= ImHaf) bitz = GrasColo;
                    else if (!InWalls) { // outdoor sky..
                        bitz = 0xFFFFFF - ((colo >> 1) & 0xFF00) - ((colo & 0xFF00) << 8);
                        zx = colo + step;
                        if (zx < 0xFF00) colo = zx;
                        else colo = 0xFFFF;
                    } //~if // aiming for 3399FF in highest sky
                    else bitz = DarkWall; // darkened wall color, in case overshot (DW=996)
                    // else bitz = 0xFF00; // green // 0xFFCC99; // not dirt
                    if (DrawDash > 0) if (rx > ImHi - 1 - DrawDash) bitz = DashColo; // dk.brown
                    if (ZooMapDim == 0) dx = -1;
                    else dx = rx - (ZooMapBase >> 16); // <0 if above close-up view (in c-u pix)
                    // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
                    if (dx >= 0) { // meters, close-up res..
                        Vat = MyMath.Fix2flt(dx + (ZooMapTopL >> (16 - ZooMapShf)), ZooMapShf);
                        // ZooMapShf cvts c-u(pix) <-> meters; Vat is pk meters, close-up res
                        if (optn > 1) if (Vat + 1.6 > Vposn) if (Vat - 1.6 < Vposn) // optn=opt+shf+lok
                            if (!logy) logy = Log_Draw;
                    } //~if                 // Log_Draw=false
                    if (logy) {
                        System.out.println(HandyOps.Dec2Log("   (BF..) ", rx,
                                HandyOps.Colo2Log(" ", bitz, HandyOps.Hex2Log(" ", colo, 8,
                                        HandyOps.Dec2Log(" ", WinWi, HandyOps.Dec2Log(" ", kx,
                                                HandyOps.Dec2Log("=", MapHy, HandyOps.Dec2Log("/", MapWy,
                                                        HandyOps.Dec2Log(" ", dx, HandyOps.Dec2Log("/", nx,
                                                                HandyOps.TF2Log(" ", InWalls, HandyOps.PosTime(" @ "))))))))))));
                        if ((Qlog & 2048) != 0) logy = false;
                    } //~if
                    for (cx = WinWi - 1; cx >= 0; cx += -1) {
                        info = 0; // default black separator, letterbox around map(s)
                        if (logy) info = 0x0000FF; // (TripLine)
                        zx = cx - 2 - ImWi;
                        deep = 0.0;
                        far = 0;
                        if (logy) if (rx == zx) far = 0x80000000;
                        if (cx < ImWi) {
                            if (logy) if ((rx == cx) || (cx < 8) && (rx < 5) || (cx > ImWi - 3))
                                System.out.println(HandyOps.Dec2Log("   ..  .. ", rx,
                                        HandyOps.Dec2Log("/", cx, HandyOps.Colo2Log(" => ", bitz,
                                                HandyOps.Colo2Log("/", info, "")))));
                            info = bitz;
                        } //~if
                        else if (ShowMap) if (zx >= 0) {
                            if (logy) if (far == 0) if (zx < 4) far = 0x80000000; // ||(cx>WinWi-8)
                            if (rx < kx) { // map shown 1 pix = 1m
                                if (WhitLnSz > 0.0)
                                    deep = 1.4; // 1m pixels, allow extra for diagonal white line
                                Vat = MyMath.Fix2flt(rx, 0);
                                Hat = MyMath.Fix2flt(zx, 0); // in park meters
                                if (zx < MapWy) // zx = (rx>>1)*HalfMap+(zx>>1)+MapIxBase;
                                    if (logy) if (far == 0) if (zx > MapWy - 4) far = 0x80000000;
                            } //~if
                            else if (dx >= 0) {     // ZMTL is in close-up pix coords..
                                Hat = MyMath.Fix2flt(zx + ((ZooMapTopL & 0xFFF) << ZooMapShf),
                                        ZooMapShf); // in park meters, close-up resolution
                                deep = ZooMapWhLn;
                            } //~if // ZooMapShf cvts c-u(pix) <-> meters
                            else zx = -1;
                            if (logy) if (far == 0) {
                                // if ((zx&15)==0) far = 0x80000000; else
                                if (optn > 1) if (Vat + 1.6 > Vposn) if (Vat - 1.6 < Vposn)
                                    if (Hat + 1.6 > Hposn) if (Hat - 1.6 < Hposn) far = 0x80000000;
                            } //~if
                            if (Qlog < 0) if (far < 0) System.out.println(HandyOps.Dec2Log("   . .. . ", rx,
                                    HandyOps.Flt2Log(" ", Vat, HandyOps.Flt2Log("/", Hat,
                                            HandyOps.Dec2Log(" ", zx, HandyOps.Flt2Log(" ", deep, ""))))));
                            if (zx >= 0) {
                                ScrnRoCo = 0;
                                info = MapColor(far + 6, Vat, Hat, deep); // flat
                                if (info > 0) info = info & 0xFFFFFF;
                                else info = 0;
                            }
                        } //~if
                        if (myPix == null) break;                       // (in side map)
                        thar--;
                        if (thar < 0) break;
                        if (thar < myPix.length)
                            myPix[thar] = info;
                    }
                } //~for //~for (cx) (rx: fill defaults)
                far = 0;
                if (ShowMap) { // add trail of breadcrumbs...................
                    thar = MyMath.iMin(nCrumbs - 1, Crummy);
                    if (Mini_Log) if (looky == 0) if ((Qlog & 2048) != 0)
                        System.out.println(HandyOps.Dec2Log("  (ShoMap) ", thar,
                                HandyOps.TF2Log(" ", ShoTrkTstPts, HandyOps.PosTime(" @ "))));
                    for (thar = thar; thar >= 0; thar += -1) {
                        info = BreadCrumbs[thar & Crummy]; // Crummy = 255
                        if (info == 0) continue;
                        rx = info >> 16;
                        if (TopCloseView > 0) if (rx >= TopCloseView) continue;
                        cx = (info & 0xFFF) + ImWi + 2;
                        if (PeekPixel(rx, cx) != CarColo) // car avatar already there
                            PokePixel(ArtiColo, rx, cx);
                    } //~for // ArtiColo=F90
                    if (ShoTrkTstPts) { // show stay-in-track points as small blue "x"s..
                        if (Left_X != 0) {
                            rx = Left_X >> 16;
                            cx = Left_X & 0xFFF;
                            PokePixel(MarinBlue, rx, cx);
                            PokePixel(MarinBlue, rx - 1, cx - 1);
                            PokePixel(MarinBlue, rx - 1, cx + 1);
                            PokePixel(MarinBlue, rx + 1, cx - 1);
                            PokePixel(MarinBlue, rx + 1, cx + 1);
                        } //~if
                        if (Rite_X != 0) {
                            rx = Rite_X >> 16;
                            cx = Rite_X & 0xFFF;
                            PokePixel(MarinBlue, rx, cx);
                            PokePixel(MarinBlue, rx - 1, cx - 1);
                            PokePixel(MarinBlue, rx - 1, cx + 1);
                            PokePixel(MarinBlue, rx + 1, cx - 1);
                            PokePixel(MarinBlue, rx + 1, cx + 1);
                        }
                    } //~if
                    zx = MyMath.iMin(kx + 8, MapTall - 8); // MapHy
                    for (thar = 0; thar <= zx; thar += 8) { // some yellow tics..
                        PokePixel(0xFFFF00, thar, ImWi + 2);          // ..along the top & left..
                        if (thar == 0) continue;
                        if ((thar & 31) != 0) continue;
                        PokePixel(0xFFFF00, thar, ImWi + 1);
                        PokePixel(0xFFFF00, thar, ImWi);
                    } //~for
                    zx = MyMath.iMin(MapWy + 8, MapWide - 8);
                    for (thar = 8; thar <= zx; thar += 8) {
                        cx = thar + ImWi + 2;
                        PokePixel(0xFFFF00, 0, cx);
                        if ((thar & 31) != 0) continue;
                        PokePixel(0xFFFF00, 1, cx);
                        PokePixel(0xFFFF00, 2, cx);
                    } //~for
                    info = ZoomMapCoord(true, Vposn, Hposn);
                    rx = info >> 16;
                    cx = info & 0xFFF;    // car avatar sb already there..
                    if (info > 0) if (PeekPixel(rx, cx) == CarColo) {
                        DrawLine(MarinBlue, rx - 1, cx, rx + 1, cx);
                        DrawLine(MarinBlue, rx, cx - 1, rx, cx + 1);
                    }
                } //~if // (info) (ShowMap)
                if (DrawDash > 0) { // DrawDash=12 to draw it
                    if (Mini_Log) if (looky == 0) if ((Qlog & 2048) != 0)
                        System.out.println(HandyOps.Dec2Log("  (DrawDash) ", DrawDash,
                                HandyOps.TF2Log(" ", ShoClikGrid, HandyOps.PosTime(" @ "))));
                    if (ShoClikGrid) DrawGrid();
                    LabelScene(myDash, ImHi - 4, ImWi - 8, -1);
                    myDash = HandyOps.Flt2Log(" ", Velocity * fFPS, " "); // (5/fps) fFPS=5
                    LabelScene(myDash, ImHi - 4, myDash.length() * 2 + SteerMid, 0xFF9999);
                    info = -1;
                    if (StepOne) info = 0x66FFFF; // cyan for 1-step
                    else if (OpMode == 2) info = 0x66FF00; // green for real-time
                    else if (OpMode > 2) info = 0xFF0099; // red for crashed
                    LabelScene(LefDash, ImHi - 4, -12, info);
                    LefDash = HandyOps.Fixt8th(" ", RealTimeNow, " ");
                    LabelScene(LefDash, SceneTall - 4, LefDash.length() * 2 + ImMid, 0xFF9900);
                    myDash = myDash + " t=" + LefDash;
                } //~if // (DrawDash>0)
                // Vmap = MyMath.Trunc8(Vposn); // = current posn in park meters (2x grid)
                // Hmap = MyMath.Trunc8(Hposn);
                info = -1;
                if (Vmap > 3) if (Hmap > 3) if (Vmap < MapTall - 4) if (Hmap < MapWide - 4) info = 0;
                if (info != 0) {
                    if (OpMode < 3) System.out.println(HandyOps.Dec2Log("** Crashed ** ", Vmap,
                            HandyOps.Dec2Log("/", Hmap, "")));
                    SimStep(4);
                } //~if // (CrashMe)
                // else { // if (DoScenery) // draw scenery back-to-front.................
                Pmap = (Vmap >> 1) * HalfMap + (Hmap >> 1) + MapIxBase;
                if (Pmap > 0) if (theInx != null) if (Pmap < theInx.length)
                    info = theInx[Pmap];
                if (OpMode < 3) if (info >= 0)
                    if (!SimSpedFixt) if ((info & 0x60000000) != 0x40000000) SimStep(6);
                if (Mini_Log) if (looky == 0) if ((Qlog & 2048) != 0)
                    System.out.println(HandyOps.Dec2Log("  (DrawDash) ", DrawDash,
                            HandyOps.TF2Log(" ", ShoClikGrid, HandyOps.PosTime(" @ "))));
                if (Qlog < 0) System.out.println(HandyOps.Dec2Log(HandyOps.IffyStr(OpMode == 3,
                        "(DoSc $$) ", "(DoSc) "), DrawDash, HandyOps.Dec2Log(" ", Vmap,
                        HandyOps.Dec2Log("/", Hmap, HandyOps.Hex2Log(" = ", info, 8,
                                HandyOps.Dec2Log(" ", LookFrame, HandyOps.Int2Log(" ", TripLine,
                                        HandyOps.PosTime(" @ "))))))));
                //
                // The car is at (Vposn,Hposn), facing (init'ly NW) -> Facing (Fa)
                // The screen is ImWi pixels wide, which imaged at distance dx
                //   is dx/fZoom meters wide, and the map position of the left edge
                //   of the image plane at distance dx is -90 degrees to the left
                //   (=Fc-90 in degrees C-wise from North) to map point Vat/Hat
                //   = (VIc-dx*cos(Lp)/(2*fZoom),HIc+dx*sin(Lp)/(2*fZoom)).
                // Stepping from the left, each pixel adds dx*cos(Lp)/(ImWi*fZoom)
                //   to V and subtracts dx*sin(Lp)/(ImWi*fZoom) from H.
                // We calculate Vbase = -cos(Fa)-cos(Lp)/(2*fZoom) and Hbase sim'ly,
                //   then each row, add dx (=deep)*Vbase to Vposn for the V part of Lx;
                //   and step Vstp = dx*cos(Lp)/(ImWi*fZoom) and Hstp sim'ly.
                //
                // MyMath.Angle2cart(Facing); // angles are in degrees, not radians
                // HsiFace = MyMath.Sine; // sin grows to right, step E to edge
                // VcoFace = -MyMath.Cose; // cos shrinks down (sin^2+cos^2 = 1m) step S
                // MyMath.Angle2cart(Facing-90.0); // this now points left-to-right,
                // Vinc = MyMath.Cose; // for stepping across the view screen
                // Hinc = -MyMath.Sine;
                // deep = 256.0;
                // Vbase = VcoFace*256.0; // about double the park width (in 2m grid locs),
                // Hbase = HsiFace*256.0; // so guaranteed to be outside the park
                doit = 0;
                r8 = RangeRow[2047] - 1; // at horizon, mid-screen
                rx = (r8 >> 3) + ImHaf; // r8 in frac'l scrn rows, rx=r8>>3+ImHaf is raster
                for (dx = 2047; dx >= -2; dx += -1) {    // convert depth to raster line number..
                    if (dx < 0) break; // dx: nominal depth in park 6cm units
                    robe = (RangeRow[dx & 2047] >> 3) + ImHaf;
                    if (Mini_Log) if (looky == 0) if ((Qlog & 2048) != 0)
                        if ((dx & 7) == 0) System.out.println(HandyOps.Dec2Log("  ?..? ", dx,
                                HandyOps.Dec2Log(" ", rx, HandyOps.Dec2Log("/", robe,
                                        HandyOps.Dec2Log("  ", nBax, HandyOps.PosTime(" @ "))))));
                    if (rx >= ImHi - DrawDash) break; // normal exit at bottom of screen
                    // far = far&0xFFFFFF|(robe<<24);
                    // if (Vscale>0) robe = (robe-ImHaf)*Vscale+ImHaf;
                    if (robe <= rx) continue;
                    step = ImWi;
                    while (rx < robe) {
                        rx++; // ..1 cont, 1 brk
                        // rotst = rx<<23;
                        far = 1;
                        zx = 0;
                        if (Mini_Log) if (looky == 0) {
                            if (((rx - TripLine) & -2) == 0) zx++;
                            else if ((Qlog & 2048) != 0) if ((rx & 7) == 0) zx = zx + 2;
                            if (zx != 0) {
                                if (rx == TripLine) {
                                    if (LookFrame >= 0) far = far | 0x80000000;
                                    System.out.println(HandyOps.Int2Log("---TripLin=", TripLine,
                                            HandyOps.Dec2Log("/", LookFrame, "---")));
                                    if (nBax > 0) aWord = "(SeeWalx) ";
                                    seen = false;
                                } //~if
                                else if (nBax > 0) aWord = HandyOps.IffyStr(rx == TripLine + 1,
                                        "---TripLin+1---\n(SeeWalx) ", "(SeeWalx)_");
                                if (nBax > 0) if (SeenWall != null)
                                    System.out.println(HandyOps.Dec2Log(aWord, rx,
                                            HandyOps.Dec2Log("/", robe, HandyOps.Int2Log(" ", nBax,
                                                    HandyOps.IffyStr((Qlog & 2048) != 0, "", " --"
                                                            + HandyOps.ArrayDumpLine(SeenWall, 0, 24))))));
                            }
                        } //~if
                        cx = rx - ImHaf; // RowRange is bottom half of screen only // ImHaf=240
                        if (cx >= ImHaf - DrawDash) break;
                        cx = cx << 3;
                        doit = RowRange[cx & ImHmsk];
                        deep = MyMath.Fix2flt(doit + doit, 6); // RR: m*16 (6cm), deep: 2m
                        // deep is grid units from camera to center of screen @ this raster
                        // = nominal (50mm) width of half-screen at this distance in meters
                        mpSpix = deep * 0.125 * WiZoom; // WiZoom = Dzoom*32/FltWi = 16/(ImWi*fZoom)
                        // so mpSpix is ("meters")GU/scr.pixel?
                        Vbase = deep * VcoFace + Vposn * 0.5; // current center of the view at this dx,
                        Hbase = deep * HsiFace + Hposn * 0.5; // ..in grid coords (2m)
                        Vpx = Vinc * mpSpix; // step size across image (in grid/pix)..
                        Hpx = Hinc * mpSpix; // .. = {deep/(ImWi/2)/zoom}*(sin|cos)
                        Voffm = Vpx * fHafIm; // fHafIm = ((double)FltWi)/2+1.0 (+1 for extra +Vpx)
                        Hoffm = Hpx * fHafIm;
                        Vat = Vbase - Voffm; // start here at left edge of screen (in grid=2m)
                        Hat = Hbase - Hoffm;
                        Voffm = Vbase + Voffm; // (end here, for bounds check)
                        Hoffm = Hbase + Hoffm;
                        cx = cx >> 1; // now it's rx*4
                        if (cx >= 0) // if (DoCloseUp) if (ShowMap) if (ZooMapDim !=0)
                            if (RasterMap != null) if (cx < RasterMap.length - 4) {
                                RasterMap[cx + 3] = Hoffm; // right end of raster (in grid=2m)
                                RasterMap[cx + 2] = Voffm;
                                RasterMap[cx + 1] = Hat;   // left end
                                RasterMap[cx] = Vat;
                            } //~if
                        doit = 0; // (didn't work, so disabled)
                        Darken = 0;
                        if (false) if (InWalls) { // calc shade for distant walls..
                            cx = 0;
                            if (DeepDark) {
                                doit = doit >> 3; // = distance in half-meters (=deep*4)
                                if (doit > 63) Darken = 0x3F3F3F;      // I don't believe this..
                                else if (doit > 0) { // ZoomPix=ImWi*50/Zoom35..
                                    cx = ZoomPix * 2 / doit; // = pix/meter (12cm @ 1:8), = baseboard
                                    Darken = doit * 0x10101;
                                }
                            } //~if
                            Wally = (cx << 24) + CreamWall;
                        } //~if // (InWalls) ..near-white cream
                        thar = rx * WinWi - 1;
                        if (CheckerBd == 0) if (solidGry) if (rx > ImHaf + 32) {
                            if (Log_Draw) if (optn > 1)
                                OopsLog = OopsLog + HandyOps.Dec2Log(" Sr", rx, "");
                            for (cx = 0; cx <= ImWi - 1; cx++) {
                                thar++;
                                if (myPix != null) if (thar > 0) if (thar < myPix.length)
                                    myPix[thar] = PavColo;
                            } //~for
                            continue;
                        } //~if // (solidGry)
                        if (ShowMap) {
                            if (Mtrk != 0) if (ZooMapDim != 0) { // find view trapezoid corners
                                if (LwL == 0) {
                                    LwL = ZoomMapCoord(true, Vat + Vat, Hat + Hat);
                                    if (LwL != 0) seen = false;
                                } //~if
                                if (RwL == 0) {
                                    RwL = ZoomMapCoord(true, Voffm + Voffm, Hoffm + Hoffm);
                                    if (RwL != 0) seen = false;
                                }
                            } //~if // (Mtrk)
                            if (rx == ImHi - 2 - DrawDash) { // DrawDash=12
                                if (ZooMapDim != 0) { // draw view trapezoid..
                                    zx = ZoomMapCoord(false, MyMath.Fix2flt(rx, 0), 1.0); // sb = Lww
                                    Lww = ZoomMapCoord(true, Vat + Vat, Hat + Hat); // should be visible
                                    Rww = ZoomMapCoord(true, Voffm + Voffm, Hoffm + Hoffm);
                                    if (LwL > 0) if (Lww > 0)
                                        DrawLine(MarinBlue + Tintx, Lww >> 16, Lww & 0xFFF, LwL >> 16, LwL & 0xFFF);
                                    if (RwL > 0) if (Rww > 0)
                                        DrawLine(MarinBlue + Tintx, Rww >> 16, Rww & 0xFFF, RwL >> 16, RwL & 0xFFF);
                                    if (Lww > 0) if (Rww > 0) // MarinBlue=0x0099FF
                                        DrawLine(MarinBlue + Tintx, Lww >> 16, Lww & 0xFFF, Rww >> 16, Rww & 0xFFF);
                                } //~if
                                zx = MyMath.Trunc8(Vat + Vat);
                                cx = MyMath.Trunc8(Voffm + Voffm);
                                if (TopCloseView > 0) {
                                    if (zx >= TopCloseView) cx = 0;
                                    else if (cx >= TopCloseView) cx = 0;
                                } //~if
                                if (cx > 0) DrawLine(MarinBlue + Tintx * 2, zx,
                                        MyMath.Trunc8(Hat + Hat) + ImWi + 2, cx,
                                        MyMath.Trunc8(Hoffm + Hoffm) + ImWi + 2);
                            }
                        } //~if // (DrawDash) (ShowMap)
                        if (VuEdge == 0.0) {
                            if (Mtrk != 0) {
                                VuEdge = MyMath.aTan0(Hoffm + Hoffm - Hposn, Vposn - Voffm - Voffm) - Facing;
                                // VuEdge is angle of raster right end, compared to straight-on
                                while (VuEdge > 180.0) VuEdge = VuEdge - 360.0;
                                while (VuEdge + 180.0 < 0.0) VuEdge = VuEdge + 360.0;
                                if (VuEdge < 0.0) VuEdge = -VuEdge;
                            } //~if // .. = view half-wi, in degs
                            seen = false;
                        } //~if
                        cx = 0; // log bounds check..
                        if (MyMath.fMin(Vat, Voffm) > GridTall) cx = 8;
                        else if (MyMath.fMax(Vat, Voffm) < 0.0) cx = 4;
                        if (MyMath.fMin(Hat, Hoffm) > GridWide) cx = cx + 2;
                        else if (MyMath.fMax(Hat, Hoffm) < 0.0) cx++;
                        if (cx != 0) seen = false; //                            (in BuildFram)
                        // if (TrakNoPix) if (rx<ImHaf+4) cx = cx|16; // optn=opt+shf+lok..
                        seen = true; // if (TrakNoPix) if (rx<ImHaf+4) cont_inue;
                        solidGry = true;
                        info = 0;
                        if ((cx & 15) == 0) for (cx = 0; cx <= ImWi - 1; cx++) { // .. repeat until off right..
                            bitz = PrioRaster[cx]; // also used to pro-rate pix in image wall
                            thar++;
                            Vat = Vat + Vpx;
                            Hat = Hat + Hpx;
                            if (WhitLnSz > 0) ftmp = Vpx + Hpx; // white line width = minimum step
                            else ftmp = 0.0;
                            info = 0;
                            if (Vat < 0.0) info++;
                            if (Hat < 0.0) info++;
                            if (Vat >= GridTall) info++;
                            if (Hat >= GridWide) info++;
                            if (info > 0) { // often outside park at first, so..
                                solidGry = false;
                                step--;
                                if (Log_Draw) if (optn > 1) if (step > 0) if (step < 3) {
                                    aWord = " Or" + rx;
                                    OopsLog = OopsLog + aWord;
                                } //~if // (step)
                                continue;
                            } //~if // (often outside park)
                            // Vmap = MyMath.Trunc8(Vat+Vat);
                            // Hmap = MyMath.Trunc8(Hat+Hat);
                            yx = far; // (so MapCo can log it)
                            // if (TripWall>0) yx = yx|0x80000000; // far|1; TripLine: far<0
                            Vsee = VcoFace; // = 1 unit step in direction Facing..
                            Hsee = HsiFace;
                            ScrnRoCo = (rx << 16) + cx;
                            colo = MapColor(yx, Vat + Vat, Hat + Hat, ftmp); // <<=========== MapColo
                            PrioRaster[cx] = TmpI; // TmpI = Pmap = cell+ (only compared =)
                            zx = 0;
                            if (colo < 8) continue;
                            if (Mtrk == 0) {
                                Mtrk = rx; // 1st row where a pixel of track or non-track was seen
                                seen = false;
                            } //~if
                            if (WhitLnSz > 0.0) {
                                zx = 0; // insert line if it got lost between track & grass..
                                if (cx > 0) zx = PeekPixel(rx, cx - 1);
                                if ((colo & Truk) != 0) {
                                    // if (Mtrk==0) Mtrk = cx;
                                    if (zx == GrasColo) colo = WhitLnColo; // grass next to paved..
                                    else if (zx == GrasDk) colo = WhitLnColo;
                                    zx = PeekPixel(rx - 1, cx); // if grass above paved..
                                    if (zx == GrasColo) colo = WhitLnColo; // ..white to replace gray
                                    else if (zx == GrasDk) colo = WhitLnColo;
                                    // else if (chekt>0) colo = PavDk;
                                    colo = colo & 0xFFFFFF;
                                } //~if
                                else if ((colo == GrasColo) || (colo == GrasDk)) { // (fails if pebbled)
                                    if (zx == PavColo) colo = WhitLnColo; // paved next to grass..
                                    else if (zx == PavDk) colo = WhitLnColo;
                                    // else if (chekt>0) colo = GrasDk; // dk.grn
                                    solidGry = false;
                                } //~if // (GrasColo)
                                else solidGry = false;
                            } //~if
                            else solidGry = false;
                            if (rx == TripLine) { // show TripLine in blue..
                                if (Mini_Log) if (Qlog < 0) if (looky == 0) // looky = FrameNo-LookFrame
                                    if ((cx & 7) == 0) System.out.println(HandyOps.Dec2Log("   >>--> ", rx,
                                            HandyOps.Dec2Log("/", cx, HandyOps.Dec2Log(" @ ", thar,
                                                    HandyOps.Colo2Log(" = ", colo, "")))));
                                if (((colo + 1) & 255) == 0) colo = colo & 0x7F7F77;
                                else colo = colo | 255;
                            } //~if
                            if (colo <= 0) continue;
                            if (floor == 0) floor = rx;
                            if (thar > 0) if (myPix != null) if (thar < myPix.length)
                                myPix[thar] = colo;
                        }
                    }
                } //~for(cx)~while(rx)~for(dx)
                if (!TrakNoPix) if (TrakImages != null) {
                    if (Mini_Log) if (looky == 0) if ((Qlog & 2048) != 0)
                        System.out.println(HandyOps.Dec2Log("    .. ", rx,
                                HandyOps.Dec2Log("/", robe, HandyOps.PosTime(" @ "))));
                    // yx = far;  if (TripLine>0) yx = yx|0x80000000; // unused
                    yx = nBax;
                    if ((Qlog & 256) != 0) yx++;
                    if (yx > 0) if (SeenWall != null) {
                        if (nBax == 0) yx = 5;
                        else yx = 12;            // Qlog&2048|Qlog&256..
                        if (looky == 0) if (Mini_Log) if ((Qlog & 0x900) != 0)
                            System.out.println(HandyOps.Dec2Log("(SeeWalz) ", FrameNo,
                                    HandyOps.Dec2Log("=", LookFrame, HandyOps.Dec2Log(" ", nBax, " --"
                                            + HandyOps.ArrayDumpLine(SeenWall, yx, 20)))));
                        TmpI = floor; // =rx for 1st ink on floor (default BackWall bottom)
                        if (SeenWall[LayerSz] > 0) DoWalls(true);
                    } //~if // --------> "DW}"
                    TmpI = 0;
                    if (NumFax > 0) ShoArtifax();
                } //~if // -------------------> "ShoArt"
                if (Mini_Log) if ((Qlog & 0x900) != 0) // Qlog&2048|Qlog&256
                    System.out.println(HandyOps.Dec2Log(" (StWh) ", rx,
                            HandyOps.Dec2Log("/", floor, HandyOps.Dec2Log(" ", ShoSteer,
                                    HandyOps.Dec2Log("/", SteerWhee, HandyOps.Dec2Log(" ", NumFax,
                                            HandyOps.Int2Log(" [", Lww, HandyOps.Int2Log("/", Rww, // btm scrn cor
                                                    HandyOps.Int2Log(" ", LwL, HandyOps.Int2Log("/", RwL, // top scrn edges
                                                            HandyOps.Dec2Log("] ", TopCloseView, HandyOps.Int2Log(" ", ZooMapTopL,
                                                                    HandyOps.Int2Log(" ", far, HandyOps.TF2Log(" ", TrakImages != null,
                                                                            HandyOps.Dec2Log(" ", NuData, HandyOps.Flt2Log("/", VuEdge,
                                                                                    HandyOps.PosTime(" @ ")))))))))))))))));
                // if (why != 8) break; // if scenery went bad, still do steering wheel..
                if (DrawDash > 0) DrawSteerWheel(ShoSteer, false, false);
                why = 7;
                if (OpMode == 3) DrawRedX();
                if (SeePaintTopL > 0) SeeOnScrnPaint(SeePaintTopL >> 16, SeePaintTopL & 0xFFFF,
                        SeePaintSize >> 16, SeePaintSize & 0xFFFF, SeePaintImgP, unPainted);
                why = 0;
                break;
            } //~while // (once through)
            ScrnRoCo = 0;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        if (Mini_Log || (why != 0)) { // M_L = true
            if (looky != 0) OopsLog = "";
            else if (OopsLog.length() == 2) OopsLog = "";
            OopsLog = OopsLog + HandyOps.PosTime(" @ ") + " @";
            if (ShoHedLit) if (SeenHedLite) HeadShoLines();
            myDash = myDash + " = ";
            System.out.println(HandyOps.Dec2Log(" (BildFram) ", FrameNo, // why =
                    HandyOps.Dec2Log(myDash, why, OopsLog)));
        }
    } //~BuildFrame

    private void InTrackIt(int looky) { // calc new posn & aim to stay in track
        boolean fOK = true, doit = true, logy = Mini_Log; // if (Qlog..) -- in M_L
        int whom = MapColor(8, Vposn, Hposn, 0), // trak = whom&Truk, // =0 if off-trk
                // Vat = MyMath.Trunc8(Vposn), Hat = MyMath.Trunc8(Hposn),   // ML=LM=T
                ledge = 0, ridge = 0, fudge = 0, more = 0, info = 0, seen = 0, dun = 0,
                nuLft = 0, nuRit = 0, optn = 0, nx = 0, kx = 0, zx = 0, why = 0;
        String aStr = "", aLine = "";         // PreLeft = 0, PreRite = 0,
        double nuly = 0.0, Vstp = 0.0, Hstp = 0.0, Vinc = 0.0, Hinc = 0.0,
                Vfwd = 0.0, Hfwd = 0.0, Vat = 0.0, Hat = 0.0, Atmp = 0.0, Ztmp = 0.0,
                Ccon = 0.0, Tcon = 0.0, Mconst = 0.0, Kconst = 0.0, gude = 0.0,
                Vlft = Vposn, Hlft = Hposn, Vrit = Vposn, Hrit = Hposn,
                Vsf = Vposn, Hsf = Hposn, aim = Facing, avg = AverageAim, ftmp;
        if (looky > 0) logy = false; // looky = (FrameNo+2-LookFrame)&-4;
        else if ((Qlog & 64) == 0) logy = false;
        while (true) { // find edges (once thru)..
            why++; // why = 1                 // Grtr: (whom&0x40000000)==0 // > is in
            while (aim < 0.0) aim = aim + 360.0;  // Horz: (whom&0x20000000)==0
            while (aim > 360.0) aim = aim - 360.0; // (last asn)
            nuly = aim;
            gude = aim;
            MyMath.Angle2cart(nuly);
            Hfwd = MyMath.Sine;
            Vfwd = -MyMath.Cose;
            if (whom < 0) { // on edge, which?
                if ((whom & 0x20000000) == 0) { // more H than V..
                    if (aim < 180.0) { // car facing east..
                        if ((whom & 0x40000000) != 0) {
                            ridge = whom;
                            whom = -whom;
                        } //~if
                        else ledge = whom;
                    } //~if
                    else if ((whom & 0x40000000) == 0) { // else facing west..
                        ridge = whom;
                        whom = -whom;
                    } //~if
                    else ledge = whom;
                } //~if
                else if ((aim < 90.0) || (aim > 270.0)) { // car facing north..
                    if ((whom & 0x40000000) != 0) {
                        ridge = whom;
                        whom = -whom;
                    } //~if
                    else ledge = whom;
                } //~if
                else if ((whom & 0x40000000) == 0) { // else facing south..
                    ridge = whom;
                    whom = -whom;
                } //~if // whom>0: starting on right edge
                else ledge = whom;
            } //~if // whom<0: starting on left edge
            else if (whom > 0) whom = 0; // whom=0: not starting on any edge
            else doit = false; // not starting on track
            MyMath.Angle2cart(aim + 90.0);
            Hstp = MyMath.Sine;
            Vstp = -MyMath.Cose;  // 3*brk, 1*cont, all near end.. // SiTwide = 7m..
            for (nx = SiTwide; nx >= -5; nx += -1) { // stepping out each side, +5m if off-track
                if (ridge <= 0) { // right edge still unseen..
                    kx = kx + 0x100;
                    Vat = Vrit + Vstp;
                    Hat = Hrit + Hstp;
                    info = MapColor(8, Vat, Hat, 0); // rtns info<0 if on line, =0 if off-trk
                    if (info == 0) {                // = PavColo|Truk = 0x40666666 if inside
                        if ((whom == 0) && !doit) { // was/still off-track,
                            Vrit = Vat; // keep looking..
                            Hrit = Hat;
                        } //~if
                        else ridge = ridge & 0x7FFFFFFF;
                    } //~if // off-track, prior is best
                    else if ((whom == 0) && !doit) { // was off-track, this is left edge..
                        Vlft = Vat + Vstp;
                        Hlft = Hat + Hstp;
                        Vrit = Vlft;
                        Hrit = Hlft;
                        if (info > 0) ledge = 0; // overshot edge         // 5: flat
                        else if ((MapColor(5, Vlft, Hlft, 0) & Truk) != 0) ledge = info; // inside
                        else ledge = info & 0x7FFFFFFF;
                        whom--;
                    } //~if
                    else if (info > 0) { // (inside track)
                        Vrit = Vat; // Vrit,Hrit are inside, closest to right edge
                        Hrit = Hat;
                    } //~if
                    else if (((info - ledge) & 0x7FFFFFFF) == 0) { // edge = left (ignore)..
                        Vrit = Vat;
                        Hrit = Hat;
                    } //~if
                    else if ((MapColor(5, Vrit, Hrit, 0) & Truk) != 0) { // still inside..
                        ridge = info;
                        Vrit = Vat;
                        Hrit = Hat;
                    } //~if
                    else if (ridge == 0) ridge = info & 0x7FFFFFFF; // just past edge
                    else ridge = ridge & 0x7FFFFFFF;
                } //~if // prior was best edge
                if (ledge <= 0) { // left edge still unseen..
                    kx = kx + 0x10000;
                    Vat = Vlft - Vstp;
                    Hat = Hlft - Hstp;
                    more = MapColor(8, Vat, Hat, 0); // 8: tkonly
                    if (more == 0) {
                        if ((whom == 0) && !doit) { // was/still off-track,
                            Vlft = Vat; // keep looking..
                            Hlft = Hat;
                        } //~if
                        else ledge = ledge & 0x7FFFFFFF;
                    } //~if // off-track, prior is best
                    else if ((whom == 0) && !doit) { // was off-track, this is right edge..
                        Vrit = Vat - Vstp;
                        Hrit = Hat - Hstp;
                        Vlft = Vrit;
                        Hlft = Hrit;
                        if (more > 0) ridge = 0; // overshot edge
                        else if ((MapColor(5, Vrit, Hrit, 0) & Truk) != 0) ridge = more; // inside
                        else ridge = more & 0x7FFFFFFF;
                        whom++;
                    } //~if
                    else if (more > 0) { // (inside track)
                        Vlft = Vat; // Vlft,Hlft are inside, closest to left edge
                        Hlft = Hat;
                    } //~if
                    else if (((more - ridge) & 0x7FFFFFFF) == 0) { // edge = rite (ignore)..
                        Vlft = Vat;
                        Hlft = Hat;
                    } //~if
                    else if ((MapColor(5, Vlft, Hlft, 0) & Truk) != 0) { // still inside..
                        ledge = more;
                        Vlft = Vat;
                        Hlft = Hat;
                    } //~if
                    else if (ledge == 0) ledge = more & 0x7FFFFFFF; // just past edge
                    else ledge = ledge & 0x7FFFFFFF;
                } //~if // prior was best edge
                if (doit) if (nx < 3) if (fudge <= 0) { // looking fwd, edge unseen..
                    kx++;
                    Vat = Vsf + Vstp;
                    Hat = Hsf + Hstp;
                    seen = MapColor(8, Vat, Hat, 0);
                    if (seen == 0) {
                        if ((whom == 0) && !doit) { // was/still off-track,
                            Vsf = Vat; // keep looking..
                            Hsf = Hat;
                        } //~if
                        else fudge = fudge & 0x7FFFFFFF;
                    } //~if // off-track, prior is best
                    else if (seen > 0) { // (inside track)
                        Vsf = Vat; // Vsf,Hsf are inside, closest to right edge
                        Hsf = Hat;
                    } //~if
                    else if ((MapColor(5, Vsf, Hsf, 0) & Truk) != 0) { // still inside..
                        fudge = seen;
                        Vsf = Vat;
                        Hsf = Hat;
                    } //~if
                    else if (fudge == 0) fudge = seen & 0x7FFFFFFF; // just past edge
                    else fudge = fudge & 0x7FFFFFFF;
                } //~if // prior was best edge
                if (ledge > 0) if (nuLft == 0) nuLft = SiTwide + 1 - nx;
                if (ridge > 0) if (nuRit == 0) nuRit = SiTwide + 1 - nx;    // if (Qlog<0)..
                if (looky == 0) if (logy) System.out.println(HandyOps.Dec2Log("  .:. ", nx,
                        HandyOps.Flt2Log(" L=", Vlft, HandyOps.Flt2Log("/", Hlft,
                                HandyOps.Int2Log(" ", more, HandyOps.Flt2Log(" R=", Vrit,
                                        HandyOps.Flt2Log("/", Hrit, HandyOps.Int2Log(" ", info,
                                                HandyOps.IffyStr(whom > 0, " + ", HandyOps.IffyStr(whom < 0, " - ", " = "))
                                                        + HandyOps.IffyStr(ledge < 0, "l", HandyOps.IffyStr(ledge > 0, "L", "."))
                                                        + HandyOps.IffyStr(ridge < 0, "_r",
                                                        HandyOps.IffyStr(ridge > 0, "_R", "_,"))
                                                        + HandyOps.IffyStr(fudge == 0, "_'",
                                                        HandyOps.Flt2Log(HandyOps.IffyStr(fudge < 0, " f=", " F="), Vsf,
                                                                HandyOps.Flt2Log("/", Hsf,
                                                                        HandyOps.Int2Log(" ", seen, ""))))))))))));
                if (ledge > 0) if (ridge > 0) break; // stop when both sides found (1st brk)
                if (nx > 0) continue;
                if (whom == 0) break; // still no edges seen @ SiTwide
                if (doit) if (fudge > 0) break;
            } //~for // good enough (stepping out)
            // if (nuLft==0) nuLft = SiTwide+SiTwide;
            // if (nuRit==0) nuRit = SiTwide+SiTwide;
            if (ShowMap) { // show farthest test points regardless..
                Left_X = ZoomMapCoord(true, Vlft, Hlft);
                Rite_X = ZoomMapCoord(true, Vrit, Hrit);
            } //~if
            if (!doit) if (whom == 0) { // no track, no edges ever seen,
                SimStep(7); // so crash it (CrashMe)
                // NuData++; // (SS did)
                break;
            } //~if // why = 1   // else if no edges seen, don't place car..
            nx = 0;
            if (!doit) nx = 4; // doit=T: started search on track
            else if (ledge != 0) nx++;
            else if (ridge != 0) nx++;
            if (nx > 0) { // can/should center the car in the track..
                if (logy) aStr = HandyOps.IffyStr(ledge == 0, " L=0", "")
                        + HandyOps.IffyStr(ridge == 0, " R=0 ", "")
                        + HandyOps.IffyStr(fudge == 0, " F=0", " ");
                Vat = (Vlft + Vrit) * 0.5;
                Hat = (Hlft + Hrit) * 0.5;
                ftmp = MyMath.fAbs(Vposn - Vat);
                Ztmp = MyMath.fAbs(Hposn - Hat);
                if (ftmp + Ztmp > 0.0) dun = 8;
                if (nx > 3) { // was off-track..
                    Vposn = Vat;
                    Hposn = Hat;
                    zx = 16;
                    dun++;
                } //~if
                else if (ftmp + Ztmp > 1.5) {
                    Vposn = (Vat + Vposn) * 0.5; // in track but not close..
                    Hposn = (Hat + Hposn) * 0.5;
                    zx = 32;
                    dun++;
                } //~if
                else if (ftmp + Ztmp == 0.0) zx--;
                if (dun > 8) dun--; // car newly moved (dun=8)
                else dun = 0;
                if (zx > 0) { // car shifted, pretend we saw both edges (if close)..
                    if (ledge == 0) if ((ridge > 0) || (fudge > 0)) {
                        // if (MyMath.fAbs(Vposn-Vrit)+MyMath.fAbs(Hposn-Hrit)<2.0) {
                        if (ridge == 0) {
                            ledge = fudge;
                            kx = kx | 0x80000000;
                            zx = zx + 4;
                        } //~if
                        else if (fudge == 0) ledge = ridge;
                        else if (((kx >> 8) & 255) > (kx & 255)) {
                            ledge = fudge;
                            kx = kx | 0x80000000;
                            zx = zx + 8;
                        } //~if
                        else ledge = ridge;
                        zx = zx + 2;
                    } //~if
                    if ((zx & 3) == 0) if (ridge == 0) if ((ledge > 0) || (fudge > 0)) {
                        // if (MyMath.fAbs(Vposn-Vlft)+MyMath.fAbs(Hposn-Hlft)<1.5) {
                        if (ledge == 0) {
                            ridge = fudge;
                            kx = kx | 0x80000000;
                            zx = zx + 4;
                        } //~if
                        else if (fudge == 0) ridge = ledge;
                        else if ((kx >> 16) > (kx & 255)) {
                            ridge = fudge;
                            kx = kx | 0x80000000;
                            zx = zx + 8;
                        } //~if
                        else ridge = ledge;
                        zx++;
                    }
                } //~if
                if (looky == 0) if (logy) System.out.println(HandyOps.Dec2Log("  :_: ", nx,
                        HandyOps.Dec2Log(" ", zx,                      // if (Qlog<0)
                                HandyOps.Hex2Log(HandyOps.IffyStr(kx < 0, " -", " x"), kx, 6,
                                        HandyOps.Flt2Log(aStr, Vat, HandyOps.Flt2Log("/", Hat,
                                                HandyOps.Flt2Log(" ", ftmp, HandyOps.Flt2Log("/", Ztmp,
                                                        HandyOps.IffyStr(ledge == 0, " L=0", "")
                                                                + HandyOps.IffyStr(ridge == 0, " R=0", "")))))))));
                Ztmp = 0.0;
            } //~if
            why++; // why = 2 // optn = 0;
            if (ledge == 0) {
                if (ridge == 0) break; // why = 2: didn't find any edges, don't steer
                why = 5; // saw only right edge
                optn = 4;                                                   // o=04
                if ((ridge & 0x20000000) != 0) optn++;
            } //~if // (N-S)            // o=05
            else if (ridge == 0) {
                why = 6; // saw only left edge
                optn = 4;                                                   // o=04
                if ((ledge & 0x20000000) != 0) optn++;
            } //~if // (N-S)            // o=05
            if (ledge != 0) {
                Kconst = MyMath.Fix2flt((ledge & 0x0FFFF800) - (ledge & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((ledge & 0x03FF) - (ledge & 0x400), 10);
            } //~if
            else {
                Kconst = MyMath.Fix2flt((ridge & 0x0FFFF800) - (ridge & 0x10000000), 19);
                Mconst = MyMath.Fix2flt((ridge & 0x03FF) - (ridge & 0x400), 10);
            } //~else
            if (logy) aLine = HandyOps.Flt2Log(" K=", Kconst,       // aLine: " M="
                    HandyOps.Flt2Log(" M=", Mconst, "; "));
            why++; // why = 3/6/7
            if (optn == 0) { // why = 3: saw both edges, calculate midline..
                Ccon = MyMath.Fix2flt((ridge & 0x0FFFF800) - (ridge & 0x10000000), 19);
                Tcon = MyMath.Fix2flt((ridge & 0x03FF) - (ridge & 0x400), 10);
                if (logy) aLine = HandyOps.Flt2Log(aLine, Ccon,           // aLine..
                        HandyOps.Flt2Log("/", Tcon, " "));
                if (((ledge ^ ridge) & 0x20000000) != 0) { // incompatible sides..
                    Atmp = MyMath.fAbs(Tcon);
                    Ztmp = MyMath.fAbs(Mconst);       // why = 3 (don't steer) cuz..
                    if (Atmp < 0.125) if (Ztmp < 0.125) break; // ..both too close to axis
                    why++; // why = 4
                    if (Atmp > Ztmp) { // convert right edge before take average..
                        if ((ledge & 0x20000000) != 0) optn = 18; // left Vert     // o=12
                        else optn = 16;                                       // o=10
                        // change: [x*t+y = c] => [y*n+x = z]; n = 1/t, z = c/t
                        Ztmp = 1.0 / Tcon;
                        Kconst = (Ztmp * Ccon + Kconst) * 0.5; // (logged only)
                        Mconst = (Mconst + Ztmp) * 0.5; // (used to calc new aim)
                        why++;
                    } //~if // why = 5
                    else { // convert left edge..
                        if ((ridge & 0x20000000) != 0) optn = 34; // right Vert    // o=22
                        else optn = 32;                                       // o=20
                        Ztmp = 1.0 / Mconst;
                        Kconst = (Ztmp * Kconst + Ccon) * 0.5; // (logged only)
                        Mconst = (Tcon + Ztmp) * 0.5;
                    }
                } //~if // why = 4 // (used in new aim)
                else { // both sides aimed the same compass quadrant (+45)..
                    if ((ledge & 0x20000000) != 0) optn = 50; // both Vert       // o=32
                    else optn = 48; // both Horz                            // o=30
                    Kconst = (Kconst + Ccon) * 0.5; // (logged only)
                    Mconst = (Tcon + Mconst) * 0.5;
                } //~else // why = 3 // (used in new aim)
                if (ledge != 0) if (ridge != 0) if (((ledge - ridge) & 0x7FFFFFFF) != 0)
                    optn = optn + 8;
            } //~if // why = 3/4/5; // else why = 6/7
            // turn equation into angle & point near posn (nuly <- Ztmp <- Atmp)..
            if ((optn & 3) == 0) { // preferred aim is Horiz..
                why = why + 5; // why = 8/9/10/11/12 => 24/../28
                Atmp = MyMath.aTan0(1.0, Mconst);
            } //~if // Mc>0 is 1st quadrant
            else // preferred aim is Vert.. // why = 3/4/5/6/7 => 19/../23
                Atmp = MyMath.aTan0(Mconst, 1.0); // ..(Mc>0 is still 1st quadrant)
            ftmp = aim + 90.0 - Atmp; // aim: 0..360, Atmp: -45..135; ftmp: -45..495
            if (((optn >> 1) & optn & 0x10) != 0) // both edges in same quadrant..
                optn = optn | 0x80;
            else if ((optn & 0x30) == 0) // one edge only, probly no change..
                optn = optn | 0x80;
            else if (MyMath.fAbs(Tcon) + MyMath.fAbs(Mconst) > 1.8)
                optn = optn | 0x80; // or else both edges near/@ 45, so
            if ((optn & 0x80) != 0) { // ..probably good..
                if (ftmp > 405.0) Ztmp = Atmp; // ftmp=90: Atmp=aim, =270: Atmp=aim-180
                else if (ftmp > 315.0) optn = optn | 0x40; // ftmp=0/180: Atmp worthless..
                else if (ftmp > 225.0) Ztmp = Atmp + 180.0;  // ..(450=360+90) +/-45: good
                else if (ftmp < 45.0) optn = optn | 0x40;
                else if (ftmp < 135.0) Ztmp = Atmp;
                else optn = optn | 0x40;
            } //~if
            else optn = optn | 0x40;
            if ((optn & 0x70) == 0) gude = Ztmp; // (one edge) 1st alt if straight NG
            else if ((optn & 0x40) == 0) { // else ignore divergent edges
                if (kx < 0) zx = zx | 64; // captured fudge, always update aim
                else if ((ledge | ridge) > 0) zx = zx | 64;
                else gude = Ztmp; // some edge is oblique or far
                if (zx > 63) {
                    if (Ztmp != aim) dun = dun + 4; // got turn
                    nuly = Ztmp;
                    fOK = false;
                }
            } //~if
            if (looky == 0) if (logy) { // if (Qlog<0)
                aStr = HandyOps.TF2Log(" ", fOK, HandyOps.Dec2Log(" (", zx,
                        ") "));
                if (gude != aim) aStr = HandyOps.Flt2Log(" G=", gude, aStr);
                if (nuly != aim) aStr = HandyOps.Flt2Log(" N=", nuly, aStr); // <-- nuly
                System.out.println(HandyOps.Dec2Log("  :::: #", FrameNo,
                        HandyOps.Hex2Log(" x", optn, 2, HandyOps.Flt2Log(aStr, aim,
                                HandyOps.Flt2Log(" a=", Atmp, HandyOps.Flt2Log(" ", Mconst,
                                        HandyOps.Flt2Log(" ", ftmp, HandyOps.Flt2Log(" z=", Ztmp,
                                                HandyOps.Dec2Log(" ", nx, HandyOps.Dec2Log(" = ", why, ""))))))))));
                nx = 0;
            } //~if
            why = why | 16; // why = 19/../28
            break;
        } //~while // (find edges)
        if (why > 1) { // not steering (or already did), but still moving..
            Vat = Vposn;
            Hat = Hposn;
            ftmp = nuly - aim;
            while (true) { // 2*brk, 1*cont (+rpt, all after log)
                info = 0;
                if (!fOK) { // fOK=T if Vfwd/Hfwd are valid (prev'ly from aim)
                    MyMath.Angle2cart(nuly); // Facing is in degrees, not radians
                    Hfwd = MyMath.Sine;
                    Vfwd = -MyMath.Cose;
                    fOK = true;
                } //~if
                Atmp = Hfwd;
                Ztmp = Vfwd;
                Vinc = Ztmp * Velocity;
                Hinc = Atmp * Velocity;
                Vposn = Vat + Vinc;
                Hposn = Hat + Hinc;
                if (Velocity != 0.0) dun = dun | 8; // got fwd motion
                if (!doit) { // omit look-ahead 2nd pass thru..
                    Vinc = 0.0;
                    Hinc = 0.0;
                } //~if
                if (why > 16) if (why < 127) // look ahead an extra step..
                    info = MapColor(5, Vposn + Vinc, Hposn + Hinc, 0); // omit paint
                if (looky == 0) if (logy) System.out.println(HandyOps.Dec2Log("  =.= ", why,
                        HandyOps.Flt2Log(" ", nuly, HandyOps.Flt2Log("/", aim,
                                HandyOps.Flt2Log(" => ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                        HandyOps.Flt2Log(" v=", Velocity, HandyOps.Flt2Log(" a=", Atmp,
                                                HandyOps.Flt2Log(" z=", Ztmp, HandyOps.Flt2Log(" ", fSweeper,
                                                        HandyOps.Int2Log(" ", info, HandyOps.IffyStr(nx == 0, "",
                                                                HandyOps.Dec2Log(" *", nx, "*")))))))))))));
                nx = 0;
                if (info == 0) break; // set posn & get out (no test)
                if (info > 0xFFFFFF) break; // posn is good
                why = why + 32;
                if (why < 64) { // failed why<32, try for 1-edge..
                    if (gude != aim) { // got 1st alt to try..
                        dun = dun | 4; // got turn
                        nuly = gude;
                        fOK = false;
                        continue;
                    } //~if
                    why = why + 32;
                } //~if
                if (why < 96) { // failed why<64 or didn't have 1st alt, try +sweep..
                    nuly = ftmp + aim;
                    if (ftmp > 0.0) nuly = nuly + fSweeper;
                    else if (ftmp + 180.0 < 0.0) nuly = nuly + fSweeper;
                    else nuly = nuly - fSweeper;
                } //~if
                else if (why < 127) // failed why<96, try -sweep..
                    nuly = MidAngle(avg, nuly);
                else if (doit) { // failed why<128, try again not looking ahead..
                    nuly = ftmp + aim;
                    why = why - 128;
                    doit = false;
                } //~if
                else nuly = aim;
            } //~while // keep aim (& fail, but out)
            ValidPixSteps = (Facing == nuly) && ValidPixSteps; // only for fisheye lens
            Facing = nuly;
            why = -why;
        } //~if
        if (dun == 8) { // got forward motion, no turn, but maybe should..
            zx = 0;
            nuly = aim;
            nx = PreRite & 255;
            if (nuRit > 0) if (nx < SiTwide) if (nuRit < nx) {
                zx = 4;
                if (nuRit < 4) zx = 8;
            } //~if
            nx = PreLeft & 255;
            if (nuLft > 0) if (nx < SiTwide) if (nuLft < nx) {
                if (nuLft > 3) {
                    zx--;
                    if (zx == 3) {
                        if (nuLft + 1 < nuRit) zx = -1;
                        else if (nuLft < nuRit + 2) zx = 0;
                    }
                } //~if
                else if (zx < 8) {
                    nuly = aim + 40.0;
                    zx = -2;
                } //~if
                else if (nuLft < nuRit) zx = -1;
                else if (nuLft == nuRit) zx = 0;
            } //~if
            if (zx + 1 == 0) nuly = aim + 13.0; // angles not even divisor of 90
            else if (zx > 4) nuly = aim - 40.0;
            else if (zx > 0) nuly = aim - 13.0;
            if (zx != 0) {
                // if (why>0) why = -why;
                Facing = nuly;
                if (zx < 0) zx = -zx; // zx = 1/2 <= -1/-2
                dun = zx * 100 + 11;
            }
        } //~if
        PreLeft = (PreLeft << 8) + nuLft;
        PreRite = (PreRite << 8) + nuRit;
        if (!logy) return; // logy=Mini_Log=F [Last_Minit=T]   // aLine: " M="
        // if (Qlog==0) return; // already implied in logy
        if (aLine == "") aLine = " ";
        System.out.println(HandyOps.Dec2Log("(InTrx) ", FrameNo,
                HandyOps.Flt2Log(" ", aim, HandyOps.Hex2Log(" ", optn, 2,
                        HandyOps.Flt2Log(" => ", Vposn, HandyOps.Flt2Log("/", Hposn,
                                HandyOps.Flt2Log(" ", Facing, HandyOps.Int2Log(aLine, whom,
                                        HandyOps.TF2Log(" ", SimInTrak, HandyOps.Flt2Log(" ", avg,
                                                HandyOps.Dec2Log(" ", nuLft, HandyOps.Dec2Log("/", nuRit,       // why =
                                                        HandyOps.Dec2Log(" = ", why, HandyOps.Dec2Log("/", dun, ""))))))))))))));
        if ((optn & 4) == 0) aLine = HandyOps.Flt2Log(" k=", Kconst,
                HandyOps.Flt2Log("/", Mconst, ""));
        else aLine = "";
        System.out.println(HandyOps.Int2Log("  [", Left_X, HandyOps.Int2Log("/", Rite_X,
                HandyOps.Int2Log("] ", ledge, HandyOps.Int2Log("/", ridge,
                        HandyOps.Flt2Log(" ", Vlft, HandyOps.Flt2Log("/", Hlft,
                                HandyOps.Flt2Log("|", Vrit, HandyOps.Flt2Log("/", Hrit,
                                        HandyOps.Flt2Log(" +", Vstp, HandyOps.Flt2Log("/", Hstp,
                                                HandyOps.Flt2Log(" a=", Atmp,
                                                        HandyOps.Flt2Log(" z=", Ztmp, aLine)))))))))))));
    } //~InTrackIt

    private void MoveCar() { // called once for each frame (not nec'ly sync'sly)
        final double MperTurn = DriverCons.D_MetersPerTurn;
        boolean logy = Log_Log || Log_Draw; // =F
        int zx, here, thar = -1, prio = SteerWhee, info = SpecWheel - prio,
                prev = GasBrake, more = SpecGas - prev,
                nx = -1, looky = 0, why = 0;
        double axel = 0.0, d1 = 0.0, d2 = 0.0, d3 = 0.0, d4 = 0.0, d5 = 0.0,
                doing = 0.0, t1 = 0.0, t2 = 0.0, t3 = 0.0, t4 = 0.0, t5 = 0.0,
                x1 = 0.0, x2 = 0.0, x3 = 0.0, x4 = 0.0, x5 = 0.0, oV = Velocity,
                MPF = fMinSpeed * fFtime, // m/frame; fFtime=0.2, fMinSpeed=4.0
                best = MyMath.Fix2flt(MyMath.iMax(SpecGas, prev), 0),
                test = MyMath.fAbs(oV);
        // " (MovCar) FrameNo OpMode +NuData s: SpecWheel=prio fSteer
        //   g: SpecGas=prev fSpeed Velocity/oV doing f: Facing = why
        if (LookFrame > 0) looky = (FrameNo + 2 - LookFrame) & -4;
        while (true) { // no early rtns
            why++; // why = 1
            Left_X = 0;
            Rite_X = 0;
            if (OpMode == 3) { // (crashed)
                GasBrake = 0;        // GasBrake is servo angle speed setting in degs
                fSpeed = 0.0;        // fSpeed is the nominal meters/frame (m/s/fps)
                Velocity = 0.0;      // Velocity is simulated, after physics
                Skidding = false;
                why = why | 256;
                break;
            } //~if
            why++; // why = 2
            if (ShowMap) if (nCrumbs == 0) {
                here = (MyMath.Trunc8(Vposn) << 16) + MyMath.Trunc8(Hposn);
                BreadCrumbs[nCrumbs & Crummy] = here; // Crummy = 255
                nCrumbs++;
            } //~if       // MPF = 0.8 = fMinSpeed*fFtime (in pk m/fr)..
            if ((SpecGas | prev) == 0) if (test + test < MPF) {
                GasBrake = 0;        // fSpeed doesn't track perfectly thru 0..
                fSpeed = 0.0;
                Velocity = 0.0;
                Braking = false;
                Skidding = false;
                why = why | 512;
                nx = 0;
            } //~if
            else if (SpecGas < 0) if (oV > 0.0) Braking = true;
            if ((SpecWheel | prio) == 0) { // neither does fSteer..
                SteerWhee = 0;
                ShoSteer = 0;
                fSteer = 0.0;
                if (nx == 0) break;
            } //~if // why = 2 (stopped)
            // if ((info|more) !=0) while (true) {
            why++; // why = 3
            if (RampServos) if (ServoStepRate > 0) { // ServoStepRate=0
                if (info > ServoStepRate) info = ServoStepRate;
                else if (info + ServoStepRate < 0) info = -ServoStepRate;
                if (more > ServoStepRate) more = ServoStepRate;
                else if (more + ServoStepRate < 0) more = -ServoStepRate;
            } //~if
            GasBrake = GasBrake + more; // (only) new setting (as ramped)
            if (SimSpedFixt) {                                      // =FixedSpeed
                if (SpecGas <= 0) fSpeed = 0.0; // (5/fps)
                else fSpeed = MPF; // fFtime=0.2, fMinSpeed=4.0, MPF = 0.8
                doing = fSpeed; // (5/fps) so doing=0.8 = 4m/s = 8mph park
                Velocity = doing;
            } //~if // V is in meters/frame (after physics)
            // else if (GasBrake<MinESCact) fSpeed = 0.0;
            else fSpeed = MyMath.Fix2flt(prev + GasBrake, 1) * fGratio * fFtime;
            // if (info==0) break; // why = 3
            why++; // why = 4
            SteerWhee = SteerWhee + info; // new setting (as ramped)
            fSteer = MyMath.Fix2flt(prio + SteerWhee, 1); // avg new+old
            info = MyMath.iAbs(SteerWhee);      // scale it to TapedWheel..
            zx = info;
            if (zx != 0) for (nx = (TapedWheel[0] & 255) - 1; nx >= 0; nx += -1) {
                if (nx > 0) if (((TapedWheel[nx & 15] >> 8) & 255) > zx) continue;
                if (SteerWhee < 0) zx = -nx;
                else zx = nx;
                break;
            } //~for // always exits here, with zx set
            ShoSteer = zx;
            // break;} //~while
            if (!SimSpedFixt) { // (acceleration math is probly wrong)  // !FixedSpeed
                why++; // why = 5 => 8/11/27
                // oV = test = Velocity;
                t1 = fSpeed; // fSpeed = (prev+GasBrake)*fGratio*fFtime/2 (ramp up)
                if (t1 < MPF) if (SpecGas < MinESCact) { // stopping..
                    why++; // why = 6 => 9/12; MPF = 0.8 = fMinSpeed*fFtime (in pk m/fr)
                    fSpeed = fSpeed * 0.5; // can be negative if active braking
                    if (fSpeed <= 0.1) if (test > 0.0) if (test + test < MPF) { // test=abs(oV)
                        why = why + 768;       // count this as stopped..
                        Velocity = 0.0;
                        fSpeed = 0.0;
                    } //~if
                    if (Velocity == 0.0) break;
                } //~if // why = 774 if stopped, = 6 if already
                if (true) {
                    // fMaxSpeed = fGratio*Float4int(MaxESCact), // =13m/s
                    // fSpeed = (prev+GasBrake)*fGratio*fFtime/2 // in pk m/fr
                    if (Braking) doing = MyMath.fMax(MyMath.fMin(fSpeed, 0.0), -oV);
                    else doing = MaxRspeed; // =0 cuz Reversible=F
                    x1 = doing;

                    // accel is in m/s/s, calc'd by measuring time to fMinSpeed = 4.0 m/s
                    //  @ MinESCact, = T2MinSp (guess'd) = 500ms, Tq=fFtime/T2MinSp=0.4
                    // so acc (sb 8m/s/s) = Tq*(GasBrake/MinESCact)=0.4, we need m/ft/ft,
                    // (how much to increase the frame speed in one frame) = 8/25 = 0.32
                    // so Tq*doing is delta-V, but result V must <= GasBrake*Vceiling
                    // (Vceiling=fMinSpeed/MinESC=fGratio/10); Torque = fFtime/Accel = 0.4

                    doing = MyMath.fMax(MyMath.fMin(fSpeed, fMaxSpeed), doing); // in pk m/f
                    x2 = doing;
                    doing = doing - oV; // how much we need to accelerate in m/f
                    x3 = doing;
                    t2 = doing * fTime4mass; // (m/f)*(a*s/m) -> a, a=servo step angle (=27)
                    // if (t2>fFtime) doing = doing*fFtime; // (I don't believe this)
                    // oV = Velocity;               // only set !=0 (except FixSp)..
                    Velocity = MyMath.fMin(doing * Torque + oV, best); // in pk m/fr;
                    if (SpecGas > 0) if (Velocity <= 0.0) why = why | 16; // if (oV>0.0)
                    if (!Reversible) if (Velocity < 0.0) Velocity = 0.0; // G=9.81*8/fps^2..
                    axel = Velocity - oV; // RubberGrip = CoFriction*PkGrav, // ..PkG=3
                    // a uniform actual accel= 3 m/s/s w/CoF=0.3, car is at verge of skid
                    // in park terms, axel = 24 pk m/s/s @ 5fps = 0.9, so RG must = 0.96
                    if ((CanSkid & 1) != 0) if (!Skidding) if (MyMath.fAbs(axel) > RubberGrip) {
                        Skidding = true;                // Skidding while accel/braking.. +F+
                        logy = true; // we want to see this
                        why = why | 32;
                    }
                } //~if
                else { // simplified speed control, until I get the messy one working..
                    doing = MyMath.fMax(MyMath.fMin(fSpeed, fMaxSpeed), 0.0); // in pk m/fr
                    Velocity = doing; // in pk m/fr (5/fps)
                    why = why | 1024;
                    x4 = doing;
                } //~else
                if (doing == 0.0) doing = Velocity; // doing is in meters/frame
                else doing = (Velocity + oV) * 0.5;
            } //~if // (!SimSpedFixt)
            why = why + 3; // why = 7/8/9
            if (OpMode == 3) break;
            if (OpMode == 0) break;         // SloMotion needs to apply also to Velocity
            // if (SloMotion>0.0) if (SloMotion<1.0) doing = doing*SloMotion; // (5/fps)
            Speedom = doing * fFPS; // in park meters/second
            OdomDx = doing + OdomDx;
            ScrnRoCo = 0;
            if (!SimInTrak) {
                why = why + 3; // why = 10/11/12
                nx = 0;
                t3 = doing * fSteer * fTurn4m; // = Facing change as fn(travel) T4m=2/(TR*pi)
                t4 = Facing + t3; // = new Facing for this frame // @(5/fps)
                d1 = (Facing + t4) * 0.5; // use avg direction to calc new forward position
                MyMath.Angle2cart(d1); // Facing is in degrees, not radians
                d2 = MyMath.Sine;
                d3 = MyMath.Cose;
                Facing = t4; // @(5/fps)
                if (t3 != 0.0) { // fTurn4m = 2/(TurnRadius*pi), =180/(r*pi) -> dg/dg/m
                    // TurnRadius is measured at servo position = min(LeftSteer,RiteSteer)
                    // NormdRad = 256.0/(TurnRadius*((double)Math.min(LeftSteer,RiteSteer))),
                    x5 = fSteer * NormdRad; // fSteer*NormdRad = 256/(turn radius in pk m)
                    axel = Velocity * Velocity * x5; // axel = 256*V*V/rad // V in pk m/fr
                    // stable actual velocity 1 m/s, radius 1m, CoF=0.1 on verge of skid
                    // in park, Velo = 1.6 pk m/fr, x5 = 256/8=32, so RT=axel = 2.5*32=80
                    if (CanSkid < 0) if (!Skidding) if (MyMath.fAbs(axel) > RubberTurn) {
                        why = why | 64;                        // RubberTurn = RubberGrip*256;
                        Skidding = true;                     // Skidding while turning.. +T+
                        logy = true; // we want to see this
                        nx = GetPiDigit(nCrumbs + FrameNo); // +/- random 0-20 degs..
                        d4 = MyMath.Fix2flt(nx + nx, 0) * MyMath.Signum(t3);
                        Facing = Facing + d4;
                        if (Mini_Log) if ((looky == 0) || (Qlog < 0)) if (Log_AxL != 0) {
                            zx = 1 << (info >> 1); // info = MyMath.iAbs(SteerWhee)
                            if ((Log_AxL & zx) != 0) {
                                System.out.println(HandyOps.Dec2Log(" (Log_AxL) ", info,
                                        HandyOps.Flt2Log(" a=", axel, HandyOps.TF2Log(" ", Skidding,
                                                HandyOps.Flt2Log(" >", RubberTurn, HandyOps.Dec2Log(" + ", nx,
                                                        HandyOps.Flt2Log(HandyOps.IffyStr(d4 < 0, " = ", " = +"), d4,
                                                                HandyOps.Flt2Log(" (", t3, HandyOps.Flt2Log(" ", Velocity,
                                                                        HandyOps.Flt2Log(" ", NormdRad,
                                                                                HandyOps.Hex2Log(") ", Log_AxL, 4, "")))))))))));
                                Log_AxL = Log_AxL - zx;
                            }
                        }
                    } //~if // (Log_AxL)(Mi_Log)(Skidding)
                    ValidPixSteps = false;
                } //~if // (t3) VPS: only for fisheye lens
                t5 = doing * ScaledShaft;
                ShafTurns = t5 + ShafTurns;
                // OdomDx = t5*MperTurn+OdomDx;
                if (Skidding) {
                    zx = GetPiDigit((nCrumbs + FrameNo) << 1);
                    d5 = MyMath.Fix2flt(zx << 1, 6);
                    doing = doing * (1.16 - d5);      // RubberGrip = CoFriction*PkGrav..
                    SimStep(8); // so crash it (CrashMe)       // ..see (SetCoFric) in log
                    if (logy || Mini_Log) // if (Qlog..) -- included in M_L
                        System.out.println(HandyOps.Dec2Log("** Skid ** ", why >> 5,
                                HandyOps.Dec2Log(" + ", nx, HandyOps.Dec2Log("/", zx,
                                        HandyOps.Flt2Log(" = ", d5 * 100.0, HandyOps.Flt2Log("c ", axel,
                                                HandyOps.Flt2Log(" >", RubberGrip,
                                                        HandyOps.Flt2Log("/", RubberTurn, ""))))))));
                } //~if // (Skidding)
                Hposn = d2 * doing + Hposn;
                Vposn = Vposn - d3 * doing;
            } //~if // (!SimInTrak)
            else InTrackIt(looky); // logs if Mini_Log & looky=0 & Qlog&64 !=0
            AverageAim = MidAngle(Facing, AverageAim);
            NuData++;
            if (ShowMap) {
                here = (MyMath.Trunc8(Vposn) << 16) + MyMath.Trunc8(Hposn);
                thar = (BreadCrumbs[(nCrumbs - 1) & Crummy] + 0x20002 - here) & 0xFFC0FFC;
                if (thar == 0) break; // adjacent crumbs must be in dif't grid cells
                BreadCrumbs[nCrumbs & Crummy] = here; // Crummy = 255
                nCrumbs++;
            } //~if
            break;
        } //~while // why = 7/8/9/10/11/12, +16+32+64
        if (logy || Mini_Log) if ((looky == 0) || (Qlog < 0)) // ** nobody sets Moved=false ??
            if (((SpecGas | SpecWheel) != 0) || (looky == 0) || Skidding || !Moved) {
                System.out.println(HandyOps.Dec2Log(" (MovCar) ", FrameNo,
                        HandyOps.Dec2Log(HandyOps.IffyStr(SimSpedFixt, " Fs o", " o"), OpMode,
                                HandyOps.Dec2Log(" +", NuData, HandyOps.Dec2Log(" s:", SpecWheel,
                                        HandyOps.Dec2Log("=", prio, HandyOps.Flt2Log(" ", fSteer,
                                                HandyOps.Dec2Log(" g:", SpecGas, HandyOps.Dec2Log("=", prev,
                                                        HandyOps.Flt2Log(" ", fSpeed, HandyOps.Flt2Log(" ", Velocity,
                                                                HandyOps.Flt2Log("/", oV, HandyOps.Flt2Log(" ", doing,
                                                                        HandyOps.TF2Log(" ", Braking, HandyOps.TF2Log("/", Skidding,
                                                                                HandyOps.Flt2Log(" f:", Facing, HandyOps.Dec2Log(" = ", why, // why =
                                                                                        HandyOps.PosTime(HandyOps.IffyStr(SimInTrak, " SiT @ ",
                                                                                                " @ ")))))))))))))))))));           // logy = Mini_Log+lok..
                if (logy) { // RubberGrip = CoFriction*PkGrav..
                    System.out.println(HandyOps.Flt2Log("      ", fTurn4m,
                            HandyOps.Flt2Log(" ", RubberGrip, HandyOps.Flt2Log(" ", Torque,
                                    HandyOps.Flt2Log(" ", test, HandyOps.Flt2Log(" -- ", t1,
                                            HandyOps.Flt2Log(" ", t2, HandyOps.Flt2Log(" ", t3,
                                                    HandyOps.Flt2Log(" / ", d1, HandyOps.Flt2Log(" ", d2,
                                                            HandyOps.Flt2Log("/", d3, HandyOps.Flt2Log(" .. ", x1,
                                                                    HandyOps.Flt2Log(" ", x2, HandyOps.Flt2Log(" ", x3, // axel = V*V/rad..
                                                                            HandyOps.Flt2Log(" ", x4, "")))))))))))))));
                    // TurnRadius is measured at servo position = min(LeftSteer,RiteSteer)
                    // NormdRad = 256.0/(TurnRadius*((double)Math.min(LeftSteer,RiteSteer))),
                    // NormdRad*fSteer = 256/(turn radius in pk meters), Velocity in pk m/fr
                    // RubberTurn = RubberGrip*256 -- see (SetCoFric) in log
                    if (t3 == 0.0) axel = 0.0; // only valid if turning: axel = 256*V*V/rad
                    if (false)
                        System.out.println(HandyOps.Dec2Log("   (", CanSkid,
                                HandyOps.Flt2Log(") t=", axel, HandyOps.Flt2Log(" = ", Velocity,
                                        HandyOps.Flt2Log("*", fSteer, HandyOps.Flt2Log("*", NormdRad,
                                                HandyOps.Flt2Log("=", x5, HandyOps.Flt2Log(" > ", RubberTurn,
                                                        HandyOps.Flt2Log(" G=", PkGrav, HandyOps.Flt2Log(" fx=", Velocity - oV,
                                                                HandyOps.Flt2Log(" >", RubberGrip, "")))))))))));
                } //~if
                Moved = true;
            }
    } //~MoveCar

    /**
     * Essentially the same as NextFrame in fly2cam.FlyCamera.
     * Camera properties are independently defined, so they are verified.
     * In single-step mode SimStep(1) the scene is updated as needed and
     * returned immediately; in real-time mode SimStep(2) the FrameTime
     * is compared to the system clock, and if greater, excess frames are
     * processed and discarded, or if less, processing stalls.
     *
     * @param rose   The number of image rows
     * @param colz   The number of image columns
     * @param pixels The array to fill with Bayer8-coded pixels
     * @return True if success, otherwise false
     */
    public boolean GetSimFrame(int rose, int colz, byte[] pixels) {
        int rx, cx = 0, info = 0, here = 0, thar = 0;
        byte aByte;                 // from GetCameraIm -> boolean NextFrame
        int[] myPix = myScreen;
        if (Log_Log || Mini_Log) if (Qlog < 0)
            if ((NuData > 0) || (FrameNo < 2) || SimBusy || DarkOnce)
                System.out.println(HandyOps.Dec2Log(" (GetSmFram) ", FrameNo,
                        HandyOps.Dec2Log(" o", OpMode, HandyOps.Dec2Log(" ", NuData,
                                HandyOps.TF2Log(" ", SimBusy, HandyOps.Flt2Log(" {", Vposn,
                                        HandyOps.Flt2Log(" ", Hposn, HandyOps.Flt2Log(" ", Facing,
                                                HandyOps.PosTime(HandyOps.IffyStr(DarkOnce, "}*D @ ", "} @ "))))))))));
        if (!SimBusy) {
            SimBusy = true;
            try {
                if (OpMode == 2) {
                    cx = HandyOps.TimeSecs(true);
                    if (NextFrUpdate > 0) if (ProcessingTime > 0) while (true) {
                        if (cx > NextFrUpdate + FrameTime + ProcessingTime) {
                            DroppedFrame++;
                            MoveCar();
                            rx = HandyOps.TimeSecs(true) - cx;
                            ProcessingTime = (ProcessingTime + rx - cx) >> 1;
                            NextFrUpdate = NextFrUpdate + FrameTime;
                            cx = rx;
                        } //~if
                        else if (ProcessingTime + cx > NextFrUpdate) break;
                        else cx = HandyOps.TimeSecs(true);
                    } //~while
                    MoveCar();
                    rx = HandyOps.TimeSecs(true) - cx;
                    if (ProcessingTime == 0) ProcessingTime = rx;
                    else ProcessingTime = (ProcessingTime + rx) >> 1;
                    if (NextFrUpdate == 0) NextFrUpdate = cx + rx;
                    else if (ProcessingTime + ProcessingTime > FrameTime) // FT too fast..
                        NextFrUpdate = cx + rx + FrameTime;
                    else NextFrUpdate = NextFrUpdate + FrameTime - ProcessingTime;
                } //~if
                else if (OpMode == 1) MoveCar(); // could crash, so..
                if (NuData > 0) {
                    BuildFrame(); // sets NuData=0, FrameNo++, NuData++ if needed VuEdge
                    if (NuData > 0) if (FrameNo < 4) BuildFrame();
                } //~if
            } catch (Exception ex) {
                System.out.println(ex);
            }
            if (OpMode == 1) OpMode = 0;
            SimBusy = false;
            StepOne = false;
        }
        if (rose != ImHi) return false;
        if (colz != WinWi) return false;
        if (pixels == null) return false;
        if (myPix == null) return false;
        for (rx = 0; rx <= ImHi - 1; rx++) {                   // BayerTile=1 = RG/GB..
            for (cx = 0; cx <= WinWi - 1; cx++) {
                if (myPix == null) break;
                if (here < 0) break;
                if (!DarkOnce) if (here < myPix.length)
                    info = myPix[here]; // each int: xxRRGGBB
                here++;
                aByte = (byte) ((info >> 8) & 255); // green
                if (pixels == null) break;
                if (thar < 0) break;
                if (thar < pixels.length - (Lin2 + 1)) {
                    pixels[thar + Lin2 + 1] = (byte) (info & 255); // red
                    pixels[thar + Lin2] = aByte;
                    pixels[thar + 1] = aByte;
                    pixels[thar] = (byte) ((info >> 16) & 255);
                } //~if
                thar = thar + 2;
            } //~for
            thar = thar + Lin2;
        } //~for
        DarkOnce = false;
        return true;
    } //~GetSimFrame

    public boolean StartImage(int rose, int colz, int tile) {
        final int myHi = ImHi;
        int why = 0;
        while (true) {
            why++; // why = 1 // if (DoScenery) {
            if (MapIndex == null) break;
            why++; // why = 2
            if (false) if (!TrakNoPix) if (TrakImages == null) break;
            why++; // why = 3
            if (rose != myHi) break;
            why++; // why = 4
            if (colz != WinWi) break;
            why++; // why = 5
            if (tile != 1) break;
            if (myScreen == null) SetMyScreen(new int[nPixels], rose, colz, tile);
            // sets: myScreen = new int[nPixels];
            //       SceneTall = rose;  SceneWide = colz;  SceneBayer = tile;
            why++; // why = 6
            if (myScreen == null) break;
            why = 0;
            break;
        } //~while
        return why == 0;
    } //~StartImage

    public void SetServo(int pin, int set2) {
        String msg = "";
        int why = 0, prio = 0, info = 0, centered = set2 - 90;
        double valu = MyMath.Fix2flt(centered, 0), tmp = valu;
        if (!SimActive) return;
        if (!unScaleStee) if (pin == SteerServo) { // because steering is not full-scale
            why = 8;
            info = centered; // (for log)
            if (false) {
                if (centered < 0) { // LfDeScaleSt = 1.0 // = 90.0/LeftSteer..
                    why = 14;
                    if (LfDeScaleSt > 1.0) tmp = LfDeScaleSt * valu; // now unscaled
                    else why = 12;
                } //~if
                else if (centered > 0) {
                    why = 6;
                    if (RtDeScaleSt > 1.0) tmp = RtDeScaleSt * valu;
                    else why = 4;
                }
            } //~if
            centered = MyMath.iMin(MyMath.iMax(centered, -LeftSteer), RiteSteer);
        }
        if (set2 >= 0) if (set2 <= 180) while (true) { // once through
            if (pin == GasServo) { // GasServo = 10
                prio = SpecGas;
                if (prio == centered) break;
                SpecGas = centered;
                NuData++;
            } //~if // (pin=GasServo)
            else if (pin == SteerServo) { // SteerServo = 9
                why++;
                prio = SpecWheel;
                if (prio == centered) break;
                SpecWheel = centered;
                why = -why;
                NuData++;
            } //~if // (pin=SteerServo)
            break;
        } //~while (once)
        if (GoodLog) { // HandyOps.Flt2Log(" {",valu,
            if (pin == SteerServo) msg =
                    HandyOps.Dec2Log("=", info,
                            HandyOps.Flt2Log(" {", valu, "} (Steer) +"));
                // HandyOps.Flt2Log("/",LfDeScaleSt,
                // HandyOps.Flt2Log("/",RtDeScaleSt,"} (Steer) +")));
            else if (pin == GasServo) msg = " (Speed) +";
            else msg = " (?) ";
            System.out.println(HandyOps.Dec2Log("(TSsServo) ", pin, HandyOps.Dec2Log(" = ",
                    set2, HandyOps.Dec2Log(" #", FrameNo, HandyOps.Dec2Log(", ", prio,
                            HandyOps.Flt2Log("=", centered,
                                    HandyOps.Dec2Log(msg, NuData, HandyOps.Dec2Log("/", why,
                                            HandyOps.PosTime(" @ ")))))))));
        }
    } //~SetServo

    /**
     * Disables (or re-enables) TrakSim, so it uses less CPU time.
     * Use this when running your software with a live image. If you call
     * GetSimFrame, TrakSim will still do all its processing to move the car
     * and deliver the next image, so you also need to refrain from asking
     * GetSimFrame for the next image when you are running a real track, if
     * your software needs to use the processor time for other tasks.
     *
     * @param activ False disables TrakSim from watching servo commands
     */
    public void Activate(boolean activ) {
        if (SimActive == activ) return;
        System.out.println(HandyOps.IffyStr(activ, "### TrakSim Active ###",
                "### TrakSim Not Looking at Servos ###"));
        SimActive = activ;
    } //~Activate

    /**
     * Requests TrakSim to return its next NextFrame->GetFrame image black.
     * Use this to test briefly covering the lens as a signal to your software.
     */
    public void DarkFlash() {
        DarkOnce = true;
    }

    /**
     * Requests TrakSim to redraw its scene on next NextFrame->GetFrame call.
     * Use this if you are drawing on the image and you need a fresh copy.
     */
    public void FreshImage() {
        NuData++;
    } // to redraw the scene

    /**
     * Returns true if TrakSim is running in stay-in-track mode.
     *
     * @return true if TrakSim is running in stay-in-track mode
     */
    public boolean StinTrak() {
        return SimInTrak;
    }

    /**
     * Returns true if currently in a skid (too much acceleration).
     */
    public boolean InSkid() {
        return Skidding;
    }

    /**
     * TrakSim will crash the car (stop simulation) if it runs off the track
     * (or other Bad Things Happen), this tells you if it did.
     *
     * @return True if crashed, otherwise false
     */
    public boolean IsCrashed() {
        return (OpMode == 3);
    }

    public double ScaleSpeed() {
        return Velocity * fFPS;
    } // in m/s park, 8x actual

    public double GetFacing() {
        return Facing;
    } // in degrees clockwise from north

    /**
     * Gets the actual map size as read in from the file.
     *
     * @return The map height (north-south) and width (east-west)
     * in park meters, packed into a single integer.
     */
    public int GetMapSize() {
        if (TopCloseView == 0) return (MapHy << 16) + MapWy; // in park meters
        return (MyMath.iMin(MapHy, TopCloseView) << 16) + MapWy;
    } //~GetMapSize

    /**
     * Gets one coordinate of the current car position.
     *
     * @param horz True to get the horizontal (east-west) coordinate
     * @return The east-west coordinate of the car position in park meters
     * if horz=true, otherwise the north-south coordinate
     */
    public double GetPosn(boolean horz) { // T: get (east-west) coord
        if (horz) return Hposn;
        return Vposn;
    } //~GetPosn

    /**
     * TrakSim will crash the car (stop simulation) if it runs off the track
     * (or other Bad Things Happen). This is one of those Bad Things.
     *
     * @param seen True to draw the red "X" on the screen
     */
    public void CrashMe(boolean seen) { // caller logs
        SimStep(3);
        if (seen) DrawRedX();
        NuData++;
    } //~CrashMe

    /**
     * Gets the distance travelled (like REPORT_PULSECOUNT).
     *
     * @param clr True to reset the count
     * @return The distance travelled since last reset.
     */
    public double GetDistance(boolean clr) {
        double dx = OdomDx;
        if (clr) OdomDx = 0.0;
        return dx;
    } //~GetDistance

    /**
     * Gets the current speed in park meters/second.
     *
     * @return The current speed.
     */
    public double GetSpeed() {
        return Speedom;
    } //~GetSpeed

    /**
     * Gets the pixel row corresponding to the car turn radius, and also
     * twice the turn radius. Your software can use this to make decisions
     * (but you probably shouldn't, because it 's not available except during
     * simulation) or to annotate the screen for debugging purposes. Note that
     * the perspective of the screen viewing angle means these numbers are not
     * linear. If the focal length of the simulated camera is too long, or the
     * turn radius too short, one or both numbers could be below the bottom of
     * the screen, or possibly approximated at the horizon (screen middle).
     *
     * @return The row position of the turn radius relative to the car
     * in the low half, and twice the turn radius in the high 16.
     */
    public int TurnRadRow() { // image row+ at turn radius & 2x turn radius
        return t2Radix * 0x10000 + tRadix;
    } //~TurnRadRow

    private void MakeRangeTbl(int why) { // what row+ is far?
        // SOHCAHTOA = "Some Old Hags Cant Always Have Their Own Aspirin"
        //   SOH,CAH,TOA: sin=opp/hyp; cos=adj/hyp; tan=opp/adj
        double whom, tmp, far = 1.0 / 256.0, // fZoom=Zoom35/50.0
                // CameraHi = 1.2, // Camera height above track in park meters
                // ZoomPix = ImWi*zx50/Zoom35, // divide this by distance for pix/meter
                ratio = fImHaf * 256.0 * fZoom, // sb = (120 or)240*256*1.9=117K
                z16 = fZoom * CameraHi * 16.0, // sb = 1.3*1.2*16=27 // ImHaf=ImHi/2
                aim = 0.0;
        int kx = 0, xx = 0, dx = 0, nx = 0, px = 0, rx = 0, cx = 0, yx = 0,
                zx = 2047, ImH8 = ImHaf * 8, btn = 0,
                valu, tx = MyMath.Trunc8(effTurnRad * 16.0 + 0.5), t2x = tx + tx;
        boolean logz = (Qlog > 8);   // effTurnRad=7.0
        int[] xData = new int[2048];
        int[] zData = new int[ImHmsk + 1];
        if (zData == null) return;
        if (xData == null) return;
        RowRange = zData;
        RangeRow = xData;
        if (RowCeiling == null) if (CeilingHi > 0.0) RowCeiling = new int[ImHmsk + 1];
        if (RowCeiling == null) CeilingHi = 0.0;
        if (CameraHi == 0.0) CeilingHi = 0.0;
        else if (CeilingHi > CameraHi) // cx is ratio CeilingHi/CameraHi
            cx = (int) Math.round(CeilingHi * 16.0 / CameraHi);
        else CeilingHi = 0.0;
        // ++ M.R.T. ++ 1.3 120 +0.0 7. => 38400. 24. 112
        // ++ M.R.T. ++ 1.9 120 0.4/c 7. => 57045. 35.7 112
        if (Mini_Log) if ((Qlog > 8) || (btn != 0)) // +op+sh
            System.out.println(HandyOps.Dec2Log("++ M.R.T. ++/", why,
                    HandyOps.TF2Log(" ", ReloTabs, HandyOps.Flt2Log(" ", fZoom,
                            HandyOps.Dec2Log("=", ZoomFocus, HandyOps.Dec2Log("mm ", ImHaf,
                                    HandyOps.Flt2Log(" ", far * 100.0, HandyOps.Flt2Log("/c ", effTurnRad,
                                            HandyOps.Flt2Log(" => ", ratio, HandyOps.Flt2Log(" ", z16,
                                                    HandyOps.Dec2Log(" ", tx, HandyOps.Flt2Log(" ", CameraHi,
                                                            HandyOps.Flt2Log(" ", CeilingHi, HandyOps.Dec2Log(" ", cx,
                                                                    HandyOps.Fixt8th("=", cx >> 1, "")))))))))))))));
        ReloTabs = false;
        // if (Vscale>0) ratio = MyMath.Fix2flt(Vscale,0)*ratio;
        tRadix = 0;
        t2Radix = 0;
        for (nx = 0; nx <= 999; nx++) { // 0 to 75 down, in quarter-degrees
            if (nx > 0) {
                if (aim >= 75.0) break;
                if (aim < 4.0) aim = aim + 0.03125;
                else if (aim < 16.0) aim = aim + 0.0625;
                else aim = aim + 0.25;
            } //~if
            MyMath.Angle2cart(aim); // r=CameraHi/sin(aim), dx=r*cos(aim)*zoom
            if (MyMath.Sine < far) continue;
            tmp = z16 / MyMath.Sine; // = slant height (radius) *16
            dx = (int) Math.round(tmp * MyMath.Cose); // = track distance in meters*16
            // if (dx >= 512) continue;
            // if (dx<0) break;
            // xxx = 2.0/MyMath.Cose; // nominal radius to screen @ 2m
            px = ((int) Math.round(ratio * MyMath.Sine / MyMath.Cose));
            rx = (px + 128) >> 8;
            xx = ((px + 16) >> 5); // 0-based rx<<3 (in table), ImHaf not included
            if (nx > 99) logz = false;
            if (logz) {
                if (aim < 1.0) whom = aim * 100;
                else whom = aim;
                System.out.println(HandyOps.Flt2Log(" (M.R.T.) ", whom,
                        HandyOps.Dec2Log(" d=", dx, HandyOps.Dec2Log(" p=", px,
                                HandyOps.Dec2Log(" r=", rx, HandyOps.Dec2Log(" y=", yx,
                                        HandyOps.Dec2Log("/", zx, HandyOps.Flt2Log(" ", tmp,
                                                HandyOps.Flt2Log(" c=", MyMath.Cose, HandyOps.Flt2Log("/", MyMath.Sine,
                                                        HandyOps.Dec2Log(" ", kx, "")))))))))));
            }
            if (tRadix == 0) {
                if (dx == tx) tRadix = rx + ImHaf;
                else if (dx < tx) tRadix = rx + ImHaf - 1;
            } //~if
            if (t2Radix == 0) {
                if (dx == t2x) t2Radix = rx + ImHaf;
                else if (dx < t2x) t2Radix = rx + ImHaf - 1;
            } //~if
            while (yx <= xx) { // while (yx+ImHaf <= rx)
                if (yx >= ImH8) break;
                if (yx < 0) break; // (can't)
                RowRange[yx & ImHmsk] = dx; // meters*16 // RowRange: dx=m*16=RR[rx*8]
                if (CeilingHi > 0.0) if (RowCeiling != null) {
                    // CeilingHi is in pk m, cx = CeilingHi*16/CameraHi, yx = CamHi @ yx
                    kx = (((cx * yx) >> 4) - yx + 4) >> 3; // kx = + rows ceil-floor at dist yx
                    // yx/8 is CamHi at that dx, so RowCeil[yx] is screen row
                    // ..where wall based at yx hits ceil, = horizon @ horizon
                    RowCeiling[yx & ImHmsk] = MyMath.iMax(ImHaf - kx, 0);
                } //~if
                // if RoCeil[yx]>0 then ceiling is at row RoCeil[yx]
                yx++;
            } //~while
            // dx = (dx+2)>>2;
            while (zx > dx) { // dx is in park 6cm units to index this table..
                if (zx < 0) break;
                valu = xx;
                RangeRow[zx & 2047] = valu; // RangeRow: rx*8 = RR[dx=m*4]
                zx--;
            } //~while
            if (zx <= 0) if (yx >= ImHaf) break;
        } //~for // (only exit) rx valid, >ImHi
        zx = 0;
        yx = MyMath.iMax(xx, ImH8);
        while (RangeRow[zx & 2047] == 0) {
            RangeRow[zx & 2047] = yx;
            zx++;
        } //~while
        dx = 0;
        if ((Qlog > 8) || (why > 0)) System.out.println(HandyOps.Dec2Log(" (MkRngTb/", why,
                HandyOps.Dec2Log(") ", tRadix, HandyOps.Dec2Log("/", tx,
                        HandyOps.Flt2Log(" h=", CameraHi, HandyOps.Dec2Log(" f=", ZoomFocus,
                                HandyOps.Dec2Log(" c=", cx, " --"
                                        + HandyOps.ArrayDumpLine(RangeRow, 0, 40) + "  <-- Rng2Row\n<RowRng>"
                                        + HandyOps.ArrayDumpLine(RowRange, ImH8, 10) + "\n<RowCeil>"
                                        + HandyOps.ArrayDumpLine(RowCeiling, ImH8, -10))))))));
        t2Radix = MyMath.iMax(t2Radix, ImHaf + 1);
        if (tRadix == 0) tRadix = ImHi + 2;
    } //~MakeRangeTbl

    /**
     * Changes the focal length of the simulated camera.
     *
     * @param newFoc35 The new focal length in 35mm-equivalent millimeters
     */
    public void Refocus(int newFoc35) { // set new lens focal length (see Zoom35)
        double fc35 = 0.0, fx50 = 0.0;
        boolean didit = false;
        while (true) {
            if (ZoomFocus != 0) {
                if (newFoc35 < 20) break; // ignore unreasonable later requests..
                if (newFoc35 > 250) break;
            }
            VuEdge = 0.0; // recalc next display (=> half-width of view, in degs)
            ZoomFocus = newFoc35;
            fx50 = MyMath.Fix2flt(zx50, 0); // zx50=28 = fudge-factor so Zoom35=50 OK
            fc35 = MyMath.Fix2flt(newFoc35, 0);
            fZoom = fc35 / fx50; // = 2x for 100mm-equivalent lens (NOT) [=1.3 for fc35=35]
            Dzoom = fx50 * 0.5 / fc35; // "denom-zoom" = 1/(2*fZoom)
            WiZoom = (Dzoom * 32.0) / FltWi; // FltWi = (double)ImWi;
            ZoomRatio = 3200.0 / (fc35 * FltWi); // 50*64 = 3200
            didit = true;
            break;
        } //~while
        if (Qlog != 0) System.out.println(HandyOps.Dec2Log(" (Refocus) f=", newFoc35,
                HandyOps.Dec2Log(" (", ZoomPix, HandyOps.Flt2Log(" ", fc35,
                        HandyOps.Flt2Log(" ", fx50, HandyOps.Flt2Log(" ", FltWi,
                                HandyOps.Flt2Log(") = ", fZoom, HandyOps.Flt2Log(" ", Dzoom,
                                        HandyOps.Flt2Log(" ", WiZoom, HandyOps.Flt2Log("=", (WiZoom * 1000),
                                                HandyOps.TF2Log("/K ", didit, "")))))))))));
        if (didit) MakeRangeTbl(2);
    } //~Refocus // also sets tRadix

    public void GotBytes(byte[] msg, int lxx) {
        if (msg == null) return;
        if ((((int) msg[0]) & 0xF0) != ArduinoIO.ANALOG_MESSAGE) return;
        if (msg.length >= 3) SetServo(((int) msg[0]) & 0xF,
                (((int) msg[2]) << 7) | ((int) msg[1]) & 0x7F);
    } //~GotBytes

    private int Color4ix(int whom) { // only frm InitInd
        int why = whom & 15, info = GroundsColors;
        if (whom > 1) {        // unpack grounds colors from single int..
            info = info >> 16;
            whom = whom & 1;
        }
        if (whom > 0) {
            whom = info >> 12;
            whom = (whom & 7) * 3;
            // info = info&0xFFF;
            if ((info & 15) >= whom) info = info - whom;
            whom = whom << 4;
            if ((info & 255) >= whom) info = info - whom;
            whom = whom << 4;
            if ((info & 0xFFF) >= whom) info = info - whom;
        }
        info = ((info & 0xF00) << 8) | ((info & 0xF0) << 4) | info & 0xF;
        info = (info << 4) + info;
        if ((info & -8) == 0) info = info + 0x10101; // so it won't confuse w/walls
        if (Log_Log) if (Qlog < 0) System.out.println(HandyOps.Dec2Log(" (Colo4ix) ", why,
                HandyOps.Hex2Log(" ", GroundsColors, 8, HandyOps.Hex2Log(" ", info, 8,
                        HandyOps.Hex2Log(" ", whom, 4, "")))));
        return info;
    } //~Color4ix

    public void LdPebTrak(int kx, int co, int zx, int cx) {
        // nominal color co is the mean around which pebbles go up or down;
        // the distance from luminance(co) to nearest rail defines half-range,
        // ..except at cx=9 top rail is raised by lx/2-96 (affects only lx>192);
        // contrast cx<9 reduces range /2^((9-cx)/2)        // kx=GroundsColors,
        double aim, scale, ink;   // co=PavColo, zx=PebblSize, cx=PebContrast
        int lxx, mx, nx, rx, why = 0;                     // only frm InitInd
        String aWord
                = HandyOps.Dec2Log("LdPebTrk: ", cx, "");
        while (true) {
            why++; // why = 1
            if ((co & -8) == 0) co = co + 0x10101;
            for (nx = 0; nx <= 9; nx++)
                if ((GotPebz & (1 << nx)) == 0) // BuildMap '[]' overrides default
                    PebleTrak[nx] = co; // default no pebbling..
            if ((kx & 0x8000) == 0) break;
            why++; // why = 2
            if (cx <= 0) break;
            why++; // why = 3
            if (cx > 9) break;
            why++; // why = 4
            if ((zx & -8) != 0) break;
            why++; // why = 5
            if (GotPebz == 0x3FF) break; // incomplete track load is ignored
            GotPebz = 0;
            for (nx = 0; nx <= 9; nx++) PebleTrak[nx] = 0; // otherwise calc reasonable pebbles..
            nx = co >> 8;
            rx = nx >> 8;  // R
            nx = nx & 255; // G
            kx = co & 255; // B
            lxx = rx + nx + kx; // = 3*lum
            mx = 0;
            if (cx == 9) if (lxx > 576) mx = lxx / 2 - 288;
            scale = MyMath.iMin(lxx, mx + 768 - lxx) / 15.0;
            if ((cx & 1) == 0) scale = scale / 1.4;
            for (mx = 7; mx >= cx; mx += -2) scale = scale * 0.5;
            for (zx = 0; zx <= 16; zx += 8) {
                rx = 255 << zx;
                mx = (co >> zx) & 255;
                aim = mx - scale * 5.0;
                aWord = aWord + HandyOps.Dec2Log(HandyOps.IffyStr(zx == 0, " B ",
                        HandyOps.IffyStr(zx == 8, "\n   G ", "\n   R ")), mx,
                        HandyOps.Flt2Log(" = ", aim, HandyOps.Flt2Log(" +", scale, "")));
                for (nx = 0; nx <= 9; nx++) {
                    if (zx == 0) kx = 5;
                    else kx = 6;
                    if (nx == kx) aWord = aWord + "\n      ";
                    kx = nx;
                    ink = kx;
                    ink = scale * ink;
                    aWord = aWord + HandyOps.Dec2Log(" *", kx, HandyOps.Flt2Log("=", ink, ""));
                    kx = MyMath.iMax(MyMath.iMin((int) Math.round(aim + ink), 255), 0);
                    PebleTrak[nx] = PebleTrak[nx] + (kx << zx);
                }
            } //~for
            why = 0;
            break;
        } //~while
        aWord = aWord + HandyOps.Dec2Log(" (", why, ") +++>");
        for (nx = 0; nx <= 9; nx++) {
            kx = PebleTrak[nx];
            if ((kx & -8) == 0) {
                PebleTrak[nx] = kx + 0x10101;
                aWord = aWord + " 0+0";
            } //~if
            else aWord = aWord + HandyOps.Colo2Log(" ", kx, "");
        } //~for   // "LdPebTrk:"
        ValiPavColo();   // MapLogged = D_MapLogged && ((Qlog&1) !=0)..
        if (MapLogged) if ((Qlog & 256) != 0) {
            System.out.println(aWord);
        }
    } //~LdPebTrak

    private boolean InitIndex(int whoz, int[] theInx) { // =T if OK, always logs
        int tall, wide, anim, myHi = ImHi, zx = ArtBase, nx = 0, rx = 0, cx = 0,
                Bmap = 0, Pmap = 0, bitz = 0, info = 0, dimz = 0, aim = 0,
                lxx = 0, whom = 0, why = 0;                           // no early exit
        int[] myFax = null;
        String aWord = "", aLine;     // frm ReadTrakInd=1, LoadTrackInf=2 (BuildMa)
        boolean OK = true;
        InWalls = false;
        NumFax = 0; // soon: number of artifact spex
        RectMap = 0;
        nBax = 0;
        for (nx = 0; nx <= 15; nx++) AnimInfo[nx] = 0;
        if (WallColoz != null) {
            WallColoz[0] = BackWall;
            WallColoz[1] = CreamWall;
            WallColoz[2] = DarkWall;
            WallColoz[3] = BackWall;
            WallColoz[4] = PilasterCo;
            WallColoz[5] = WallCo5;
            WallColoz[6] = WallCo6;
            WallColoz[7] = WallCo7;
        }
        if (WalzTall != null)
            for (nx = 255; nx >= 0; nx += -1) WalzTall[nx] = 0; // heights in pk meters*16
        if (theInx != null) if (theInx.length > ArtBase) {
            ImageWide = theInx[0] & 0xFFFF;
            dimz = theInx[3]; // = park dims (=myMap[3+3] in BuildMa)
            GroundsColors = theInx[4];
            nx = theInx[2]; // start of map
            Pmap = nx + GridSz; // start of BG map
            RectMap = theInx[ArtBase - 1]; // (paint map) = myMap[ArtBase+2] as constr'd
            MapIxBase = nx;
            if (nx > ArtBase) if (GroundsColors != 0) { // set colors & start pos'n..
                info = theInx[6]; // = orientation + line width + pebble contrast
                rx = theInx[5];
                if (nx < theInx.length) nBax = theInx[nx - 1]; // =0 if no BG images
                for (nx = nx; nx >= 0; nx += -1) {
                    aim = theInx[zx];
                    NumFax++;
                    anim = (aim >> 28) & 15;
                    aim = aim & 0x0FFFFFFF;
                    bitz = bitz | (1 << anim);
                    if (NoisyMap) if (Qlog < 0) if (zx < ArtBase + 15)
                        System.out.println(HandyOps.Dec2Log("  (...) ", nx,
                                HandyOps.Dec2Log(" ", zx, HandyOps.Int2Log(" ", aim,
                                        HandyOps.Dec2Log("+", anim, HandyOps.Dec2Log(" ", NumFax, ""))))));
                    if (aim == 0) break;
                    if (anim > 4) AnimInfo[anim & 15] = aim;
                    else if (anim < 4) {
                        // if (aim==prio) NumFax--; // no need to count alt views, but OK
                        // prio = aim;
                        zx = zx + 3;
                    } //~if
                    else zx++;
                    zx++;
                } //~for
                if (NumFax > 0) NumFax++;
                cx = rx & 0xFFFF;
                rx = rx >> 16;
                SetStart(rx, cx, info & 0xFFFF);
                info = info >> 16;
                zx = info >> 8; // 0-spec: keep default..
                if (zx > 0) if (zx <= 9) PebContrast = zx;    // (in InitInd)
                info = info & 255;
                if (info > 99) info = -info;
                // else if (info==0) info = (int)Math.round(WhiteLnWi*100.0);
                if (info >= 0) {
                    WhitLnSz = MyMath.Fix2flt(info, 0) * 0.014; // white line width
                    aWord = aWord + HandyOps.Flt2Log("W= ", WhitLnSz, " ");
                } //~if
                PavColo = Color4ix(0);
                PavDk = Color4ix(1);
                GrasColo = Color4ix(2);
                GrasDk = Color4ix(3);
                if (!TrakNoPix) if (NumFax > 0) if (TrakImages != null) {
                    theFax = new int[NumFax * 2]; // used to sort arts by distance for disp
                    myFax = theFax;
                    if (myFax == null) NumFax = 0;
                    else for (info = NumFax * 2 - 1; info >= 0; info += -1)
                        if (info >= 0) if (info < myFax.length) myFax[info] = 0;
                }
            }
        } //~if
        if (GroundsColors < 0) for (nx = nx; nx <= HalfMap * HalfTall - 1; nx += HalfMap + 1) { // diagonal
            // this probably fails, cuz walls are diff't.. **
            if (nx > HalfMap * (HalfTall - 3 - MapIxBase)) { // init'ly: nx=MapIxBase =356
                InWalls = true; // no walls, but also no BG, call it inside..
                break;
            } //~if
            if (theInx != null)
                if (nx > 0) if (nx < theInx.length) info = theInx[nx];
            if (info <= 0) continue;
            if (info >= 0x70000000) break; // found BG, it's probly outdoors
            if (info >= 0x40000000) continue;
            if (info < 8) InWalls = true; // found a wall, it's inside
            break;
        } //~for // (nx)     // otherwise outside background (InWalls=false)
        tall = dimz >> 16; // dimz = theInx[3] = park dims
        wide = dimz & 0xFFF;
        MapHy = tall;
        MapWy = wide;
        ShowOdometer = (MapWy < 200) && ShowMap;
        fMapWi = MyMath.Fix2flt(wide, 0);
        ZooMapScale = 0.0; // ZMS*(2m grid) -> img pix in close-up
        ZooMapWhLn = 0.0; // ZMW*(img pix) -> park meters: ZMPS*ZMS = 2.0*1.4
        ZooMapTopL = 0; // (ditto) park coords for top-left corner of close-up
        ZooMapBase = 0; // where on the image it is shown
        ZooMapShf = 0; // shift: img pix : park meters
        ZooMapDim = 0; // in park meters as used to calc edges of close-up
        while (true) { // if ((GroundsColors<0)||(dimz != ParkDims)) // =200<<16 +256
            why++; // why = 1              // ..close-up is useful in all cases
            if (!DoCloseUp) break;
            why++; // why = 2
            if (!ShowMap) break;
            why++; // why = 3
            if ((1 << MapWiBit) != MapWide) break; // verify correct constant
            why++; // why = 4
            if (tall >= ImHaf) if (tall + 99 > myHi) break; // ..unless no space for it
            why++; // why = 5
            if ((rx | cx) == 0) {
                rx = MyMath.Trunc8(Vposn); // (for log)
                cx = MyMath.Trunc8(Hposn);
            } //~if
            lxx = MyMath.iMax(tall + 2, myHi - 16 - MapTall); // = top row of displayed c-u
            SetCloseTop(lxx);
            if (((ZooMapDim & 0xFF) << ZooMapShf) != MapWide) OK = false;
            // these two for log (example only) as if car is centered (not)...........
            rx = MyMath.iMax(rx - (info >> 1), 0);
            cx = MyMath.iMax(cx - ((ZooMapDim >> 1) & 0xFFF), 0);
            ZooMapTopL = (rx << 16) + cx; // in pk meters (using formula from BuildFrame)..
            aWord = aWord + HandyOps.Int2Log("\n// ZMD=", ZooMapDim,
                    HandyOps.Dec2Log(" ZSf=", ZooMapShf, HandyOps.Int2Log(" ZB=",
                            ZooMapBase, HandyOps.Flt2Log(" ZSc=", ZooMapScale,
                                    HandyOps.Flt2Log(" ZW=", ZooMapWhLn,
                                            HandyOps.Int2Log(" ZT=", ZooMapTopL,
                                                    HandyOps.IffyStr(OK, "", " OOPS")))))));
            // ZMD=32,32 ZSf=3 ZB=224,642 ZSc=16. ZW=0.2 ZT=8,16
            ZooMapTopL = 0;
            why = 0;
            break;
        } //~while // (true)
        nx = GroundsColors; // (shorter line)
        LdPebTrak(nx, PavColo, PebblSize, PebContrast);                 // (in InitInd)
        // if (ShoHedLit) if (SeenHedLite) HeadShoLines(true); // too soon?
        if (nBax > 0) if (theInx != null) {
            zx = MapIxBase - 1; // MapIxBase = theInx[2]; // start of map
            if (zx > 0) if (zx < theInx.length) Bmap = theInx[zx] - 4;
        }
        aWord = aWord + HandyOps.Dec2Log(" -- iW=", ImageWide,
                HandyOps.Dec2Log(" L=", RectMap, ""));
        if (theInx != null)
            aWord = aWord + "\n  =-=" + HandyOps.ArrayDumpLine(theInx, 123, 5);
        System.out.println(HandyOps.Dec2Log(" (IniIndx/", whoz,
                HandyOps.Dec2Log(") = ", why, HandyOps.Dec2Log(" ", tall,  // why =
                        HandyOps.Dec2Log("/", wide, HandyOps.Hex2Log(" ", nx, 8,
                                HandyOps.Colo2Log(" G=", GrasColo, HandyOps.Colo2Log("/", GrasDk,
                                        HandyOps.Colo2Log(" T=", PavColo, HandyOps.Colo2Log("/", PavDk,
                                                HandyOps.TF2Log(" I=", InWalls, HandyOps.Dec2Log(" ", myHi,
                                                        HandyOps.Dec2Log(" ", NumFax, HandyOps.Dec2Log(" BG=", nBax,
                                                                HandyOps.Hex2Log("\n    x", bitz, 4, HandyOps.TF2Log(" ", theInx != null,
                                                                        HandyOps.Dec2Log(" ", NumFax, aWord + "\n  ---> "
                                                                                + HandyOps.ArrayDumpLine(AnimInfo, 0, 8))))))))))))))))));
        if (ReloTabs) MakeRangeTbl(3);
        if (nBax > 0) if (WalzTall != null) {
            if (Bmap > 4) if (Bmap < zx) for (lxx = 1; lxx <= 255; lxx++) { // collect wall heights..
                Bmap = Bmap + 4;
                if (Bmap < 8) break;
                if (Bmap > theInx.length - 6) break;
                info = theInx[Bmap]; // V,H map coords for this image (bottom L/R)
                if (info == 0) break; // done
                aim = (theInx[Bmap + 1] >> 10) & 3; // pie width/aim:4/compass:10
                zx = theInx[Bmap + 2]; // ppm/file offset (=-1 if ImgWall=plain)
                // if (zx==0) continue;
                anim = theInx[Bmap + 3]; // pixel H,W of this image (=colo if plain)
                tall = (anim >> 16) & 0x7FFF;
                bitz = (zx >> 24) & 255; // = ppm
                if (zx + 1 == 0) { // plain wall..
                    WalzTall[lxx & 255] = (lxx << 4) + 0x3000; // format for plain wall: 12K
                    nBax = lxx;
                } //~if
                else if (bitz > 0) { // image wall..
                    tall = MyMath.iMin((tall << 4) / bitz, 4095); // pk meters*16
                    if (tall > CarTall) if (tall < (CarTall << 2)) { // CarTall = CameraHi*16
                        OK = true;
                        for (zx = whom - 1; zx >= 0; zx += -1) {
                            wide = DidCells[zx & 255];
                            if (((wide - tall) & 0xFFFF) != 0) continue;
                            DidCells[zx & 255] = wide + 0x10000;
                            OK = false;
                            break;
                        } //~for
                        if (OK) {
                            DidCells[whom & 255] = tall + 0x10000;
                            whom++;
                        }
                    } //~if
                    bitz = info >> 8;
                    if ((aim & 1) != 0) { // 1=E, 3=W (compare N/S face to edge)..
                        rx = (bitz >> 16) & 255; // ending V
                        cx = (info >> 16) & 255; // starting V
                        wide = dimz >> 16;
                    } //~if
                    else {            // 0=N, 2=S (compare E/W face to edge)..
                        wide = dimz & 0xFFFF;
                        rx = bitz & 255;      // ending H
                        cx = info & 255;
                    } //~else // start H
                    zx = 0x20000; // flag for close to park edge
                    nx = 0; // if ((((aim>>1)+aim)&1) !=0) // 1=E:N-edge, 2=S:E // naw, ..
                    if (aim > 1) { // 2=S:E-edge, 3=W:S-edge (both compare to high)..
                        wide = wide - 5;
                        if (MyMath.iMin(rx, cx) < wide) zx = 0;
                        nx++;
                    } //~if
                    else if (MyMath.iMax(rx, cx) > 5) zx = 0; // 0=N:W-edge, 1=E:N (both: lo)
                    tall = tall + zx;
                    WalzTall[lxx & 255] = tall; // img wall: pk meters*16 (<4K)
                    if (Mini_Log) if ((Qlog & 256) != 0)
                        System.out.println(HandyOps.Dec2Log("    (..) ", lxx,
                                HandyOps.Dec2Log(" @ ", Bmap, HandyOps.Int2Log(" -> [", info,
                                        HandyOps.Dec2Log(" (", aim, HandyOps.Int2Log(") .. ", anim,
                                                HandyOps.Int2Log("] => ", tall, HandyOps.Dec2Log(" ", rx,
                                                        HandyOps.Dec2Log("/", cx, HandyOps.IffyStr(nx == 0, " > 5",
                                                                HandyOps.Dec2Log(" < ", wide, "")))))))))));
                    nBax = lxx;
                }
            } //~for // lxx (collect wall heights)
            info = 0;
            if (whom > 0) for (zx = whom - 1; zx >= 0; zx += -1) { // find most common wall height..
                wide = DidCells[zx & 255];
                if (wide > info) info = wide;
            } //~for
            if (info > 0) if (WalzTall != null) WalzTall[0] = info;
            if (Pmap > 0) if (Mini_Log) if ((Qlog & 256) != 0) {
                System.out.println(HandyOps.Dec2Log(" (WalzTal) ", nBax,
                        HandyOps.Dec2Log(" ", CarTall, " =:= "
                                + HandyOps.ArrayDumpLine(WalzTall, nBax + 2, 22))));
                aLine = "^:^" + HandyOps.Dec2Log(" ", nBax, HandyOps.Dec2Log("/", Pmap, ""));
                zx = 1;
                for (rx = 0; rx <= MapHy - 1; rx++) {
                    aWord = "\n" + HandyOps.Dec2Log(HandyOps.IffyStr(rx > 99, "",
                            HandyOps.IffyStr(rx > 9, "+", "  ")), rx,
                            HandyOps.Dec2Log("/", Pmap, ":"));
                    if (rx == MapHy - 1) zx++; // always show 1st & last lines
                    for (cx = 0; cx <= HalfMap - 1; cx += 4) {
                        if (Pmap >= theInx.length) break;
                        info = theInx[Pmap];
                        Pmap++;
                        if (cx >= MapWy) continue;
                        if (cx > 0) if ((cx & 31) == 0) if (cx + 4 < MapWy)
                            aWord = aWord + "\n          ";
                        if ((cx & 15) == 0) aWord = aWord + " ";
                        if (info == 0) {
                            aWord = aWord + " 0000";
                            continue;
                        } //~if
                        zx++;
                        if ((info & 0x0F0F0F0F) == info) {
                            aWord = aWord + HandyOps.Hex2Log(" ", info, 1,
                                    HandyOps.Hex2Log("", info >> 8, 1, HandyOps.Hex2Log("", info >> 16, 1,
                                            HandyOps.Hex2Log("", info >> 24, 1, ""))));
                            continue;
                        } //~if
                        for (nx = 0; nx <= 3; nx++) {
                            aWord = aWord + HandyOps.Dec2Log(" ", info & 255, "");
                            info = info >> 8;
                        }
                    } //~for // (nx)(cx)
                    if (zx > 0) aLine = aLine + aWord;
                    zx = 0;
                } //~for // (rx)
                System.out.println(aLine);
            }
        } //~if // (Mini_Log)
        return why >= 0;
    } //~InitIndex // always true

    private void ReadTrakIndex() { // only frm LoadTrackIn; see also BuildMap
        int here, thar,      // this reads the (binary PatsAcres) track .indx file
                lxx = 0, tops = 0, xize = 0, info = 0, why = 0; // (only if no BldMap)
        boolean LilEndian = false;
        byte[] bytes8 = new byte[12];
        int[] theInx;
        byte[] xData;
        File myFile = new File(SceneFiName + ".indx");
        FileInputStream theFile = null;
        try {
            while (true) {
                why++; // why = 1
                if (myFile == null) break;
                why++; // why = 2
                try {
                    theFile = new FileInputStream(myFile);
                } catch (Exception ex) {
                    theFile = null;
                }
                if (theFile == null) break;
                why++; // why = 3
                info = theFile.read(bytes8);
                if (info < 8) break;
                why++; // why = 4
                info = HandyOps.Int4bytes(false, bytes8[0], bytes8[1], bytes8[2], bytes8[3]);
                if (info == 0x4C696C45) LilEndian = true; // "LilE"
                else if (info == 0x456C694C) LilEndian = true;
                else if (info != 0x42696745) if (info != 0x45676942) break; // "BigE"
                why++; // why = 5
                xize = HandyOps.Int4bytes(LilEndian, bytes8[4], bytes8[5],
                        bytes8[6], bytes8[7]);
                tops = HandyOps.Int4bytes(LilEndian, bytes8[8], bytes8[9],
                        bytes8[10], bytes8[11]);
                info = xize;
                if (xize < 9999) break;
                if (xize > 9999999) break;
                if (!TrakNoPix) if (tops > 0) {
                    info = tops;
                    if (tops < 512) break;
                    if (tops > 9999999) break;
                    if ((tops & 255) != 0) break;
                } //~if
                lxx = xize * 4; // index size in ints, now in bytes
                xData = new byte[lxx];
                why++; // why = 6
                if (xData == null) break;
                why++; // why = 7
                theInx = new int[xize];
                if (theInx == null) break;
                why++; // why = 8
                info = theFile.read(xData);
                if (info < lxx) break;
                here = lxx - 4;
                lxx = 0; // thar = xize-1;
                why = 9;
                for (thar = xize - 1; thar >= 0; thar += -1) {
                    if (xData == null) break;
                    why++;
                    if (here < 0) break;
                    if (here < xData.length - 4)
                        info = HandyOps.Int4bytes(LilEndian, xData[here], // why = 10
                                xData[here + 1], xData[here + 2], xData[here + 3]);
                    if (theInx == null) break;
                    why++;
                    if (thar < 0) break;
                    if (thar < theInx.length)
                        theInx[thar] = info; // why = 11
                    here = here - 4;
                    why = 9;
                } //~for
                if (why > 9) break;
                if (!InitIndex(1, theInx)) {                                  // ReadTrakIn
                    why = 19;
                    break;
                }
                why = 12;
                MapIndex = theInx;
                theInx = null;
                xData = null;
                if (!TrakNoPix) if (tops > 0) { // (PatsAcres file has no image component)
                    lxx = tops * 4; // image size in ints, now in bytes
                    xData = new byte[lxx + 4];
                    why++; // why = 13
                    if (xData == null) break;
                    why++; // why = 14
                    theInx = new int[tops];
                    if (theInx == null) break;
                    why++; // why = 15
                    info = theFile.read(xData);
                    if (info < lxx) break;
                    here = 0;
                    why = 16;
                    for (thar = 0; thar <= tops - 1; thar++) { // TIFF = RGBA, we want ABGR..
                        if (xData == null) break;
                        why++;
                        if (here < 0) break;
                        if (here < xData.length - 4)
                            info = HandyOps.Int4bytes(false, xData[here], xData[here + 1],
                                    xData[here + 2], xData[here + 3]);
                        if (theInx == null) break; // why = 17
                        why++;
                        if (thar < 0) break;
                        if (thar < theInx.length) theInx[thar] = info; // why = 18
                        here = here + 4;
                        why = 16;
                    } //~for
                    if (why > 16) break;
                    TrakImages = theInx;
                }
                why = 0;
                break;
            }
        } catch (Exception ex) {
            why = -why;
        }
        if (why != 0) if (why < 13) MapIndex = null;
        if (Qlog < 0) System.out.println(HandyOps.Dec2Log("ReadPatsIndx ", xize >> 10,
                HandyOps.Dec2Log("K / ", tops >> 10, HandyOps.Dec2Log("K (", info,
                        HandyOps.Dec2Log(") = ", why, "")))));
    } //~ReadTrakIndex

    private void LoadTrackInfo() throws IOException { // true if success
        int nx, zx, tall = 0, wide = 0, why = 0;  // this loads the text track file
        String theList, aLine, filename;
        int[] theInx = null; // (loading this was an exercise for the user ;-)
        int[] myMap = null;
        while (UseTexTrak) {
            why++; // why = 1
            MapIndex = null;
            TrakImages = null;
            LuminanceMap = null;
            theList = HandyOps.ReadWholeTextFile(SceneFiName + ".txt");
            if (theList.equals("")) {
                break; // why = 1
            }
            why++; // why = 2
            if (HandyOps.CharAt(0, theList) == '\"') { // skip to named descriptor..
                aLine = " " + HandyOps.NthItemOf(true, 1, theList);
                nx = HandyOps.NthOffset(0, aLine, theList);
                zx = HandyOps.NthOffset(0, "\n0x", theList);
                if (nx > 0) {
                    while (nx > 0)
                        if (HandyOps.CharAt(nx, theList) == '\n') {
                            zx = nx;
                            break;
                        } else {
                            nx--;
                        }
                }
                if (zx > 0) {
                    theList = HandyOps.RestOf(zx + 1, theList);
                } else {
                    break;
                }
            } //~if // why = 2
            why++; // why = 3
            if (!TrakNoPix && (GotImgOps(theList) != 0)) {
                why++; // why = 4
                filename = SceneFiName + ".tiff";
                zx = HandyOps.ReadTiff32Image(filename, null);
                if (zx != 0) {
                    tall = zx >> 16;
                    wide = zx & 0xFFFF;
                    nx = tall * wide;
                    theInx = new int[nx];
                    why++; // why = 5
                    nx = HandyOps.ReadTiff32Image(filename, theInx);
                    if (nx == zx) {
                        WhiteAlfa(tall, wide, theInx);
                        TrakImages = theInx;
                    } else {
                        theInx = null;
                    }
                    why++; // why = 6
                }
            }
            myMap = BuildMap(theList, (tall << 16) + wide, theInx);
            if (myMap == null) {
                break; // why = 2..6
            }
            for (nx = 0; nx <= myMap.length - 4; nx++) {
                if (myMap == null) {
                    break;
                }
                if (nx < 0) {
                    break;
                }
                if (nx < myMap.length - 3) {
                    myMap[nx] = myMap[nx + 3];
                }
            } //~for
            if (InitIndex(2, myMap)) {
                MapIndex = myMap; // InIx always logs, always true
            }
            why = -why; // why = -2..-6
            break;
        } //~while
        if ((Qlog < 0) || (why > 0)) {
            System.out.println(HandyOps.Dec2Log("(LdTrkInfo) = ", why, HandyOps.TF2Log(" ", UseTexTrak, "")));
        }
        if (MapIndex == null) {
            ReadTrakIndex();
            throw new IllegalStateException("MapIndex is null.");
        }
    } //~LoadTrackInfo

    private void Valid8consts() {
        boolean NG = false;
        String aWord = "";
        int info = HandyOps.Countem("aba", "ababaaabaaababb"), // =3
                whar = HandyOps.NthOffset(2, "aba", "ababaaabaaababb"); // =6
        double Vat = HandyOps.SafeParseFlt("12.3"),
                Hat = HandyOps.SafeParseFlt(" -0.01 ");
        if (NoisyMap) if (Qlog < 0) {
            aWord = HandyOps.Flt2Log("*** FltTest: ", Vat * 10.0,
                    HandyOps.Flt2Log(" ", Hat * 1000.0,
                            HandyOps.Flt2Log(" ", HandyOps.SafeParseFlt("0x22.2"),
                                    HandyOps.Dec2Log(" ", info, HandyOps.Dec2Log(" ", whar, " ")))));
            info = HandyOps.Int4bytes(false, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78);
            whar = HandyOps.Int4bytes(true, (byte) 0x78, (byte) 0x56, (byte) 0x34, (byte) 0x12);
            NG = (info != whar) || (info != 0x12345678);
            if (NG) aWord = "<!> " + aWord;
            System.out.println(HandyOps.Hex2Log(aWord, info, 8,
                    HandyOps.Hex2Log(" = ", whar, 8, HandyOps.TF2Log(" *** ", NG, ""))));
            aWord = "";
        } //~if
        if (ParkDims != 0xC80100) { // ParkDims checked only here
            System.out.println("<!> TrakSim is designed only for ParkDims=200x256");
            NG = true;
        } //~if
        if (BayerTile != 1) {
            System.out.println("<!> TrakSim is designed only for BayerTile=1");
            NG = true;
        } //~if
        while (true) {
            if (ImHi == 480) if (ImWi == 640) break;
            if (ImHi == 240) if (ImWi == 320) break;
            System.out.println(HandyOps.Dec2Log("<!> TrakSim has not been tested"
                    + " with image sizes other than V=480 x H=640 and V=240 x H=320"
                    + " (", ImHi, HandyOps.Dec2Log("x", ImWi, ")")));
            break;
        } //~while
        if (DrawDash < 0) {
            System.out.println("<!> We don't do negative DrawDash");
            NG = true;
        } //~if
        else if (DrawDash > 32) {
            System.out.println("<!> A large DrawDash leaves no image space for track");
            if (DrawDash * 4 > ImHi) NG = true;
        } //~if
        while (true) {
            if (MapTall == 200) if (MapWide == 256) break;
            System.out.println("<!> TrakSim is designed for park size V=200 x H=256");
            NG = true;
            break;
        } //~while
        while (true) {
            if (Vramp > 0) if (Vramp < MapTall) if (Hramp > 0) if (Hramp < MapWide) break;
            System.out.println(HandyOps.Dec2Log("<!> Vramp,Hramp must be inside park: ",
                    MapTall, HandyOps.Dec2Log(",", MapWide, "")));
            NG = true;
            break;
        } //~while
        if ((RampA < 0) || (RampA >= 360))
            System.out.println("<!> RampA should be in the range 0-359");
        if ((Zoom35 < 20) || (Zoom35 > 255))
            System.out.println("<!> TrakSim probably won't work well with Zoom35 as extreme "
                    + HandyOps.IffyStr(Zoom35 < 99, "close-up", "telephoto") + " lens");
        if (FrameTime < 20) {
            System.out.println("<!> TrakSim is designed only for FrameTime >= 20ms");
            NG = true;
        } //~if
        if (SteerServo == GasServo) {
            System.out.println("<!> TrakSim requires distinct Steer & Gas Servos");
            NG = true;
        } //~if
        while (true) {
            if (SteerServo > 0) if (SteerServo < 16)
                if (GasServo > 0) if (GasServo < 16) break;
            System.out.println("<!> TrakSim is designed only for servo pins 1-15");
            NG = true;
            break;
        } //~while
        if (MinESCact >= MaxESCact) {
            System.out.println("<!> MinESCact >= MaxESCact can't work");
            NG = true;
        } //~if
        else while (true) {
            if (MinESCact >= 0) if (MinESCact < 90)
                if (MaxESCact > 0) if (MaxESCact <= 90) break;
            System.out.println("<!> TrakSim is designed for MinESCact/MaxESCact"
                    + " only in the range 0-90");
            NG = true;
            break;
        } //~while
        // while (true) {
        //   if (LeftSteer >= 0) if (LeftSteer <= 90)
        //     if (RiteSteer >= 0) if (RiteSteer <= 90) break;
        //   System.out.println("<!> TrakSim is designed for LeftSteer/RiteSteer"
        //       + " only in the range 0-90");
        //   NG = true;
        //   break;} //~while
        info = Crummy + 1;
        if ((info & -info) != info) { // if (DoScenery) if (!TrakNoPix)
            System.out.println("<!> Masks like Crummy only work as 2^n-1");
            NG = true;
        } //~if
        if ((PebblSize & -8) != 0)
            System.out.println("<!> PebblSize out of range");
        if (PebContrast > 9)
            System.out.println("<!> PebContrast cannot exceed 9");
        info = CheckerBd + 1;
        if (!TrakNoPix) if ((info & -info) != info) // if (DoScenery)
            System.out.println("<!> Masks like CheckerBd only work as 2^n-1");
        if ((MarinBlue & -0x01000000) != 0) aWord = "MarinBlue";
        else if ((SteerColo & -0x01000000) != 0) aWord = "SteerColo";
        else if ((CarColo & -0x01000000) != 0) aWord = "CarColo";
        else if ((DarkWall & -0x01000000) != 0) aWord = "DarkWall";
        else if ((BackWall & -0x01000000) != 0) aWord = "BackWall";
        else if ((CreamWall & -0x01000000) != 0) aWord = "CreamWall";
        if (aWord != "")
            System.out.println("<!> TrakSim is designed for 00 alpha channel"
                    + " in colors like " + aWord);
        if (TurnRadius < 0.5) {
            System.out.println("<!> I don't think TrakSim can simulate a car"
                    + " with a TurnRadius less than 2 meters");
            NG = true;
        } //~if
        else if (TurnRadius < 2.0)
            System.out.println("<!> Wow! That's a really short TurnRadius");
        else if (TurnRadius > 64.0)
            System.out.println("<!> Wow! That's a really long TurnRadius,"
                    + " Did you intend your car to have no steering wheel?");
        if (fMinSpeed < 0.0) {
            System.out.println("<!> TrakSim does not simulate a car going backwards");
            NG = true;
        } //~if
        else if (fMinSpeed > 32.0) System.out.println("<!> MINimum speed 65mph"
                + " (fMinSpeed>32 m/s) is ridiculous");
        if (WhiteLnWi < 0.0) {
            System.out.println("<!> You can't have negative width WhiteLnWi");
            NG = true;
        } //~if
        else if (WhiteLnWi > 2.0)
            System.out.println("<!> You want *really* wide WhiteLnWi?");
        else if (WhiteLnWi == 0.0) if (Qlog < 0)
            System.out.println("<!> No white lines, eh?");
        info = 1 << MxLayShf; // AssertRange(MaxLayers+3,1<<MxLayShf,256);
        if ((MaxLayers < 2) || (MxLayShf < 2) || (info - 3 < MaxLayers) || (info > 256)) {
            System.out.println("<!> Invalid MaxLayers/MxLayShf");
            NG = true;
        } //~if
        if (Acceleration < 0.0) {
            System.out.println("<!> You can't have negative Acceleration");
            NG = true;
        } //~if
        else if (Acceleration > 30.0)
            System.out.println("<!> Your car has no get-up-and-go (Acceleration)?");
        else if (Acceleration == 0.0)
            System.out.println("<!> That's a pretty zippy car. OK");
        if (NG) System.exit(8);
    }

    public void StartPatty(String whom) {
        System.out.println(HandyOps.Dec2Log("StartingSim #", nClients, " " + whom));
        if (nClients == 0) {
            if (ShowMap) BreadCrumbs = new int[Crummy + 1]; // Crummy = 255
            if (GoodLog) if (ShowMap) if (DoCloseUp) ShoHeadLite = new int[4];
            SeenWall = new int[MyMath.iMax(LayerSz + 1, 1024)];
            // PixelSteps = new int[ImWi];
            PrioRaster = new int[ImWi];
            PebleTrak = new int[16];
            ShoLumiLox = new int[32];
            DidCells = new int[256];
            WalzTall = new int[256];
            AnimInfo = new int[16];
            WallColoz = new int[8];
            FltWi = (double) ImWi;
            fImHaf = (double) ImHaf;
            Valid8consts(); // quits if bogus defined constants
            Refocus(Zoom35); // does these things:
            // Dzoom = ((double)zx50)/((double)Zoom35*2); // "denom-zoom" = 1/(2*fZoom)
            // WiZoom = (Dzoom*32.0)/FltWi; // = 16/(ImWi*fZoom)
            // fZoom = ((double)Zoom35)/((double)zx50); // = 2x for 100mm-equivalent lens
            // MakeRangeTbl(2); // builds RangeRo,RowRang, sets tRadix
            if (TweakRx != 0) while (tRadix > ImHi - 8 - DrawDash) { // TweakRx=0
                if (TweakRx > 0) {
                    effTurnRad = effTurnRad + MyMath.Fix2flt(TweakRx, 0); // +TurnRadius/4.0;
                    MakeRangeTbl(4);
                } //~if
                else if (TweakRx < 0) Refocus(ZoomFocus + TweakRx);
                else break;
            } //~while
            SetStart(Vramp, Hramp, RampA);
            // Vposn = Vramp; Hposn = Hramp; Facing = RampA;
            try {
                LoadTrackInfo(); // calls BuildMap
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(3);
            }
            if (false) if (Mini_Log) if (Qlog < 0) if (MapIndex != null)
                System.out.println("  =-=" + HandyOps.ArrayDumpLine(MapIndex, 123, 5));
            if (!NewGridTbl(Grid_Locns)) {
            }
            Velocity = 0.0;
            fSpeed = 0.0;
            fSteer = 0.0;
            SteerWhee = 0;
            GasBrake = 0;
            NuData++;
            SerialCalls = new SimHookX();
            ArduinoIO.HookExtend(SerialCalls);
        }
        nClients++;
    } //~StartPatty

    public String toString() {
        return "TrakSim " + RevDate;
    }

    public class SimHookX extends SimHookBase {
        public void SendBytes(byte[] msg, int lxx) {
            GotBytes(msg, lxx);
        }
    } //~SimHookX
} //~TrakSim (trakSimFiles) (TS)
