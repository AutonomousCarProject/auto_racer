//package org.avphs.traksim;
//
//import org.avphs.camera.FlyCamera;
//import org.avphs.sbcio.fakefirm.ArduinoIO;
//import org.avphs.sbcio.fakefirm.ArduinoIOIO;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferInt;
//
///* TrakSim Car Simulator for use with NWAPW Year 3 Autonomous Car Project
// * Java Window App to Test/Demo TrakSim..
// *
// * This simulator pretends to be a camera using the FlyCamera API, and
// * watches the commands being sent to the ArduinoIO through FakeFirmata,
// * and controls the simulated car based on those commands, then shows
// * what a forward-facing camera on the simulated car would see.
// *
// * TrakSim copyright 2018 Itty Bitty Computers and released at this time
// * to the public as open source. There are no warranties of any kind.
// *
// * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
// * but if you are developing your self-driving code on some computer other
// * than LattePanda, you can substitute package noJSSC, which has the same
// * APIs (as used by FakeFirmata) but does nothing.
// */
//
//public class DrDemo extends JFrame implements MouseListener {
//    /**
//     * This is the interface for specifying listeners to the ArduinoIO output.
//     */
//    public interface UpdateListener { public abstract
//
//    /**
//     * Override this method to see the data being sent from the ArduinoIO.
//     *
//     * @param  pin     The "pin" or message code that generated this message.
//     *
//     * @param  value   The 14-bit value being sent from HardAta
//     */
//    void pinUpdated(int pin, int value) ;}
//
//
//    private static final long serialVersionUID = 1L; // unneed but Java insists {
//
//    private static final int MinESCact = DriverCons.D_MinESCact,
//            MaxESCact = DriverCons.D_MaxESCact, StartGas = MinESCact*7/4,
//            ScrPix = TrakSim.nPixels, nServoTests = DriverCons.D_nServoTests,
//            ScrHi = DriverCons.D_ImHi, ImgWi = DriverCons.D_ImWi, HaffScrn = ImgWi/2,
//            ScrTop = ScrHi-1-DriverCons.D_DrawDash, ScrTall = ScrHi, HaffHi = ScrHi/2,
//            SimTile = DriverCons.D_BayTile, FastSet = (MaxESCact*3)/4,
//            ScrWi = TrakSim.WinWi,
//            FastBlobs = DriverCons.D_FastBlobs, // Pittman's Blobs
//            PosterColors = 0x07354210,      // post'd color +s: W,Y,Gy,B,G,R,Bk
//            blew = (PosterColors>>12)&15, // posterized colors..
//            grin = (PosterColors>>8)&15, raid = (PosterColors>>4)&15,
//            yelo = (PosterColors>>20)&15, whyt = PosterColors>>24,
//            blok = PosterColors&15, grey = (PosterColors>>16)&15,
//            BlakThresh = 56*16, WhitThresh = 208*16, EquThresh = 48, // colo thresh
//            MinBlobDim = 3*0x10001, MaxBlobDim = ~(63*0x10001),
//            MarinBlue = DriverCons.D_MarinBlue, PilasterCo = DriverCons.D_PilColo,
//    // Elts: (color) (top row, left cln) (bot row, right cln)
//    ScrFrTime = DriverCons.D_FrameTime, Qlog = DriverCons.D_Qlog,
//            AskOdomTime = DriverCons.D_AskOdomTime, // now depend on EatOften
//            Scr2L = ScrWi*2, PulseCnt_Pin = 8, DeadMan_Pin = 11,  // FrameRate_15=3..
//            CamFPS = FlyCamera.FrameRate_15-1, // CamTime = 528>>CamFPS, // cam frame in ms
//
//    CarColo = DriverCons.D_CarColo, AddColo = DriverCons.D_MarinBlue&0xFFFFFF,
//            SteerPin = DriverCons.D_SteerServo, GasPin = DriverCons.D_GasServo;
//
//    private static final double MperTurn = DriverCons.D_MetersPerTurn, // leave steering unscaled
//            LefScaleSt = 1.0, // LefScaleSt = ((double)DriverCons.D_LeftSteer)/90.0,
//            RitScaleSt = 1.0; // RitScaleSt = ((double)DriverCons.D_RiteSteer)/90.0;
//
//    private static final boolean StartInCalibrate = DriverCons.D_StartInCalibrate,
//            Mini_Log = DriverCons.D_Mini_Log && (Qlog !=0), ShowDateTime = Mini_Log,
//            ContinuousMode = false, DrawStuff = true, LiveCam = DriverCons.D_LiveCam,
//            ShoClikGrid = DriverCons.D_ShoClikGrid, ShowMap = DriverCons.D_ShowMap,
//            TestTimes = DriverCons.D_TestTimes, Postrize = DriverCons.D_Postrize,
//            StartLive = DriverCons.D_StartLive, GoodLog = (Qlog<0), NoisyBlob = false;
//
//    private static int StartYourEngines = 0, NoisyFrame = 999, // (35*4096+34)*4096+33,
//            ServoTstPos = 0, ServoTestCount = 0, // remaining number of steps
//            SawBlobFr = 0, NoneStep = 0, // >0: pause simulation after each recalc
//            ViDied = 0, CamTile = 0, CamTall = 0, CamWide = 0, CamFrame = 0,
//            ShoTimFrm = 0, SawSpeed = 0, SpareNum = 0;
//
//    private static boolean StepMe = false, mySpedFixt = DriverCons.D_FixedSpeed,
//            SeenHardAta = false, CamActive = false;
//
//    private static ArduinoIOIO theServos = null;
//    private static String ShoTimStr = "" ;
//
//    private FlyCamera theVideo = null;
//    private FlyCamera simVideo = null;
//    private TrakSim theSim = null;
//    private byte[] CamPix = null;
//
//    private static JFrame theWindow = null; // (used by static method)
//
//    public static class RunSoon implements Runnable { @Override
//    public void run() {starting();}} //~RunSoon
//
//
//    /**
//     * Example listener to capture periodic odometer readings from HardAta.
//     *   Also tracks DeadMan status.
//     *
//     */
//    private static class DrListener implements UpdateListener {
//        boolean died; // true if DeadMan activated
//        double sofar, // accumulated distance since startup/set, in park meters
//                speed;    // calc'd speed, in park meters per second; /4 = real mph
//        int readtime; // the time of last reading // @Override
//
//        public void pinUpdated(int pin, int value) { // called when it arrives
//            int now = ArduinoIO.getMills(), nuly = now-readtime; // o'flo after 3 wks
//            double dx, tim;
//            if (pin==ArduinoIO.DEADMAN_MESSAGE) died = (value >= 0x2000); // =251
//            else if (pin==PulseCnt_Pin) { // PulseCnt_Pin = 8
//                dx = MyMath.Fix2flt(value,0)*MperTurn;
//                tim = MyMath.Fix2flt(nuly,0)*0.001; // tim in secs
//                if (tim>0) speed = dx/tim;
//                // tim = MyMath.Fix2flt(now,0)*0.001;
//                sofar = sofar+dx;
//                readtime = now;} //~if // 0-based to program start
//            if ((Qlog&4) !=0) System.out.println(HandyOps.Dec2Log("DrListen=",pin,
//                    HandyOps.Int2Log(": ",value,HandyOps.TF2Log(" ",died,
//                            HandyOps.IffyStr(pin != PulseCnt_Pin,"",
//                                    HandyOps.Flt2Log(", = ",sofar,HandyOps.Flt2Log("m / ",speed,
//                                            HandyOps.Flt2Log("m/s ",speed,HandyOps.Dec2Log(" ",nuly,
//                                                    HandyOps.PosTime(" @ "))))) )))));
//            SawSpeed++;} //~pinUpdated
//
//        public void SetDistance(double dx) {sofar = dx;} //~SetDistance
//        public double GetDistance() {return sofar;} //~GetDistance
//        public double GetSpeed() {return speed;} //~GetSpeed // in pk m/sec
//        public boolean GetDied() {return died;} //~GetDied
//
//        public DrListener() { // constructor, init..
//            died = false;
//            sofar = 0.0;
//            speed = 0.0;
//            readtime = 0;
//            System.out.println(HandyOps.Int2Log("new DrListn Q=",Qlog,
//                    HandyOps.PosTime(" @ ") + " " + HandyOps.TimeStamp(false)));}}
//
//    private static DrListener ArdPinUpd = new DrListener();
//
//    private Timer TickTock = null;
//    private BufferedImage theImag = null;
//    private BufferedImage theBuff = null;
//    private byte[] SimBytes = null;
//    public int[] thePixels = null;
//    public int[] theBlobs = null;
//    public int[] PostPix = null;
//    public int[] RoSums = null;
//
//    private int Calibrating = 0;
//    private int SteerDegs = 0;
//    private int GasPedal = 0;
//    public int DidFrame = 0;
//    public int DarkState = 0;
//    private boolean ShoPoGray = false;
//    private boolean OhDark = false;
//    private boolean BusyPaint = false;
//    private boolean CanDraw = false;
//    public boolean CameraView = false;
//    public boolean unPaused = false;
//
//    private String DrTmpSt;
//
//    private final int[] Grid_Moves = {0,-32,-8,-1,0,1,8,32, 0,1,8,32, 0,1,8,32};
//
//    private final int[] AvgExamTbl = {
//            156,140,164,148, 132,148,132,140, 140,164,156,164, 164,148,140,148,
//            144,152,136,152, 144,160,136,136, 144,160,152,136, 136,160,136,136,
//            148,156,124,140, 156,156,132,124, 148,132,156,156, 132,156,148,124,
//            152,120,128,152, 136,120,120,136, 128,152,136,136, 136,152,136,144,
//
//            116,132,140,116, 148,140,116,124, 148,148,116,124, 140,148,124,140,
//            128,120,112,144, 120,136,144,144, 112,112,120,128, 120,128,120,144,
//            108,116,140,140, 132,132,108,132, 116,108,124,132, 140,108,140,124,
//            120,128,128,104, 112,112,136,112, 128,120,120,136, 112,136,104,128,
//
//            140,156,148,140, 148,116,132,100, 116,108,116,108, 124,100,108,108,
//            112,128,104,96,  128,96,112,128,  96,112,112,120,  96,96,96,128};
//
//    private class PaintAction implements ActionListener { @Override
//    public void actionPerformed(ActionEvent evt) {
//        if (theWindow != null) theWindow.repaint();}} //~PaintAction
//
//    private PaintAction doOften = null; // used for periodic events
//
//    /**
//     * Example orange cone color detector.
//     *
//     * @param  colo  RGB color to test
//     *
//     * @return       true if orange, false otherwise
//     *
//     */
//    public boolean IsOrange(int colo) {
//        int blu = colo&255, grn = colo>>8, rid = grn>>8;
//        grn = grn&255;
//        if (blu<44) if (grn>33) if (grn>blu+blu) if ((rid&255)>grn) return true;
//        return false;} //~IsOrange
//
//
//    /**
//     * Example time-test code, method overhead. With proper parameters, this
//     *                never recurs, but that fact is hidden from the compiler
//     *                so it sees the recursion and never inlines.
//     *
//     * @param  param  Should =0, but the compiler cannot know that
//     *
//     * @param  abit   Should >0, but the compiler must not know that
//     *
//     * @return        2nd parameter abit, for no particular reason
//     *
//     */
//    public int TimedMethod(int param, int abit) { // from TimeTest
//        // (the compiler doesn't know this never recurs, so not inlined)
//        if (param>abit) {
//            if (param>8) return 0; // prevent runaway resursion (error)
//            if (param<0) return 0;
//            return TimedMethod(param-1,abit+1);} //~if
//        return abit;} //~TimedMethod
//
//    /**
//     * Example time-test code. The convoluted code in this method is designed
//     *                to trick the compiler into not optimizing, so it actually
//     *                measures what it appears to measure.
//     *
//     *                Each timed test loop runs 100 million times, then the
//     *                time in milliseconds is divided by 100 for an iteration
//     *                time in picoseconds (at 1GHz, this would be the system
//     *                clock time; for higher clock rates, multiply by the ratio).
//     *                Some operations known to take longer are run fewer times.
//     *
//     *                You are encouraged to replace individual loops with your
//     *                own time tests. Experiment!
//     *
//     * @param  seed   Should =0, but the compiler should not know that
//     *
//     * @param  rpts   Should =10^7, best if the compiler does not know it
//     *
//     */
//    public void TimeTest(int seed, int rpts) { // Example
//        // call with seed=0, rpts=10000000 (10^7), so compiler cannot optimize
//        // calc value based on prior iteration within each loop,
//        // ..so compiler won't try to optimize it out of the loop
//        int[] PiDigs = theSim.GetPiDigs();
//        int lxx, mx, nx, xx, yy, zz, tstn, RandyBits = PiDigs[seed&127],
//                arySz = 0x7FFF, t0 = 0, t7 = 0, tx = 0;
//        int[] datary = new int[arySz+1];
//        double calc;
//        String whom;
//        boolean oddly;
//        for (tstn=0; tstn <= 11; tstn++) {
//            if (tstn<6) lxx = 9; // all tests before multiply
//            else lxx = (0x004999>>(tstn<<2))&15; // some have reduced runs
//            for (lxx=lxx; lxx >= 0; lxx += -1) { // outer loop to scale runtime
//                oddly = RandyBits<0;
//                RandyBits = RandyBits<<1;
//                if (!oddly) RandyBits++; // reverses polarity, to equalize time
//                xx = RandyBits&127;
//                xx = PiDigs[xx]&arySz;
//                yy = xx&127;
//                yy = PiDigs[yy]&arySz;
//                zz = yy&127;
//                zz = PiDigs[zz]&arySz;
//                mx = zz&127;
//                mx = PiDigs[mx]&arySz;
//                if (tstn+seed==5) zz++; // so divide/0 not possible
//                else if ((tstn|seed)==4) yy = (tstn>>2)+seed; // =1
//                tx = HandyOps.TimeSecs(true);
//                if (tstn==0) for (nx=rpts-1; nx >= 0; nx += -1) { // .....................base case..
//                    zz = xx+(yy&zz);} //~for // something nontrivial, compiler must gen code
//                else if (tstn==1) for (nx=rpts-1; nx >= 0; nx += -1) { // ................logical..
//                    zz = zz+(xx&yy); // base case, then..
//                    yy = xx&yy;      // two logical operations (we measure these),
//                    xx = zz|xx;} //~for // varying the terms to confuse the optimizer
//                else if (tstn==2) for (nx=rpts-1; nx >= 0; nx += -1) { // ................addition..
//                    zz = yy+(xx&zz); // base case, then..
//                    yy = zz+yy;      // two adds
//                    xx = yy+xx;} //~for // this should take same time as logical
//                else if (tstn==3) for (nx=rpts-1; nx >= 0; nx += -1) { // ................add alone..
//                    zz = yy+(xx&zz); // base case, then..
//                    yy = mx-zz+yy+xx;      // four extra adds (we measure these),
//                    xx = mx+yy+zz+xx;} //~for // varying the terms to confuse the optimizer
//                else if (tstn==4) for (nx=rpts-1; nx >= 0; nx += -1) { // ................shift..
//                    zz = yy+(xx&zz); // base case, then..
//                    yy = (mx+xx)<<14; // two extra shifts (we measure these),
//                    xx = (mx+yy)>>15;} //~for
//                else if (tstn==5) for (nx=rpts-1; nx >= 0; nx += -1) { // ..............if-then-else..
//                    zz = (yy+zz)|zz; // base case, then..
//                    if (zz>seed) {   // compare true (measured)
//                        xx = yy*zz;
//                        yy = xx*zz;}} //~for
//                else if (tstn==6) for (nx=rpts-1; nx >= 0; nx += -1) { // ................multiply..
//                    zz = (yy+xx)&zz; // base case, then..
//                    xx = yy*zz;      // two multiplies
//                    yy = xx*zz;} //~for
//                else if (tstn==7) for (nx=rpts-1; nx >= 0; nx += -1) { // ................divide..
//                    zz = (yy+xx)|zz; // base case, then..
//                    yy = yy/zz;} //~for // one divide (divisor known >0)
//                else if (tstn==8) for (nx=rpts-1; nx >= 0; nx += -1) { // .............array access..
//                    yy = (xx+zz)&yy; // base case, then..
//                    if (datary != null) if (zz >= 0) if (zz < datary.length)
//                        yy = datary[zz];} //~for // known not out of bounds
//                else if (tstn==9) for (nx=rpts-1; nx >= 0; nx += -1) { // .............method call..
//                    zz = (yy&xx)|zz; // base case, then..
//                    yy = TimedMethod(seed,zz);} //~for // one method call
//                else /* if (tstn>9) */ for (nx=rpts-1; nx >= 0; nx += -1) { // ....for-loop overhead..
//                        yy = yy&(xx+zz); // base case, then..
//                        for (mx=((tstn-1)&8)+lxx; mx >= 0; mx += -1) yy = zz+yy;}} //~for // 1x then 9x 2nd time
//            tx = HandyOps.TimeSecs(true)-tx-t0;
//            calc = 100.0;
//            switch (tstn) {
//                case 0:
//                    t0 = tx;
//                    continue;
//                case 1:
//                    whom = "$ AND+OR took ";
//                    break;
//                case 2:
//                    whom = "$ 2 adds took ";
//                    t7 = tx;
//                    break;
//                case 3:
//                    whom = "$ 4 adds alone took ";
//                    tx = tx-t7;
//                    break;
//                case 4:
//                    whom = "$ 2 shifts alone took ";
//                    tx = tx-t7;
//                    break;
//                case 5:
//                    whom = "$ one compare true took ";
//                    tx = tx-t7;
//                    break;
//                case 6:
//                    whom = "$ one array access took ";
//                    break;
//                case 7:
//                    whom = "$ 2 multiplies took ";
//                    break;
//                case 8:
//                    whom = "$ one divide took ";
//                    break;
//                case 9:
//                    whom = "$ one method call took ";
//                    calc = calc*0.5;
//                    tx = (t0>>1)+tx; // this ran half as many times
//                    t0 = t0/10; // the last two run 10% as many times
//                    break;
//                case 10:
//                    t7 = tx;
//                    continue;
//                case 11:
//                    whom = "$ for-loop overhead took ";
//                    tx = tx-t7;
//                    calc = calc*0.8;
//                    break;
//                default:
//                    continue;} //~switch
//            if (calc>0.0) System.out.println(HandyOps.Flt2Log(whom,tx/calc,
//                    HandyOps.Int2Log(" ps, #",tstn,HandyOps.PosTime(" @ "))));} //~for
//        // put a breakpoint on this line to read results on console log
//    }
//
//    /**
//     * Tom Pittman's Speedy Blob Finder: Create new blob and return
//     *   its index inx>0, or else -1 if failed. If the new blob touches
//     *   or overlaps an existing blob, they are merged and the index
//     *   of the merged blob is returned. Enabled by FastBlobs>0
//     *
//     *   Each blob is encoded as three ints in blobz array,
//     *     blobz[inx+0] = (inx,poco) = inx*65536+poco (posterized color)
//     *     blobz[inx+1] = (top,left) = top*65536+left
//     *     blobz[inx+2] = (bottom,right) = (row,coln) = row*65536+coln
//     *
//     * @param  whom  What is known about this new blob. The low 3 bits is
//     *               the posterized color (poco) of this blob. The high 16
//     *               bits is either a blob index, and NewBlob is being asked
//     *               to validate & merge with an existing blob if possible;
//     *               or else its (negative) current height, width>0 below.
//     *
//     *               If whom=0 NewBlob is being asked to merge all possible
//     *               blobs and to remove any invalid blobs.
//     *
//     * @param  row   The current row, which is the bottom of the new blob.
//     *               If whom=0 and row>0, deletes only blobs above row.
//     *
//     * @param  coln  The current column, which is the right edge of the new blob.
//     *
//     * @param  blobz The blob array.
//     *
//     * @return       -1 if no blob is created or merged (array full or error),
//     *               0 if merge made no changes & found no errors, otherwise
//     *               (inx,poco) where inx is the index of created/merged blob
//     *                 pointing to the start of 3 numbers in the blobz array:
//     *
//     *               The value at blobz[inx] = (inx,poco) = inx*65536+poco
//     *               The value at blobz[inx+1] = (top,left)
//     *               The value at blobz[inx+2] = (bottom,right) = (row,coln)
//     */
//    public int NewBlob(int whom, int row, int coln, int[] blobz) {
//        int rez = -1, here = whom>>16, thar, whar, info = here, more,
//                kx, zx, tall, wide, topple, butter, tops = 0, colo = 0,
//                why = 0;
//        while (true) { // once thru..
//            why++; // why = 1
//            if (blobz==null) break;
//            tops = blobz[0];
//            if (here>0) { // if (whom>0) // try to merge this blob..............
//                why = 10;
//                if (tops<1) break;
//                why++; // why = 11
//                if (NoisyBlob) { // test for =1 mod 3 (valid index)..
//                    // info = here;
//                    while (info>3) info = (info>>2)+(info&3);
//                    if (info !=1) break;} //~if
//                why++; // why = 12
//                if (here>blobz.length-3) break;
//                why++; // why = 13
//                colo = blobz[here];
//                if (colo<16) { // invalid (undeleted) blob there..
//                    rez--; // say so (rez = -2)
//                    break;} //~if
//                if ((colo&0xFFFF)>7) break; // invalid blob color there
//                why++; // why = 14
//                if ((colo>>16) != here) break; // invalid blob index there
//                topple = blobz[here+1];
//                butter = blobz[here+2];
//                tall = butter-topple;
//                why++; // why = 15
//                if (((tall-MinBlobDim)&MaxBlobDim) !=0) break; // invalid size
//                why++; // why = 16
//                if (butter>ScrHi*0x10000) break; // blob bottom off-screen
//                why++; // why = 17
//                if ((butter&0xFFFF)>ScrWi) break; // blob right off-screen
//                why++; // why = 18
//                zx = row; // non-zero            // safe to test squareness..
//                if ((row<<16)>butter) if ((butter&~0x70007) !=0) {
//                    wide = tall&0xFFFF; // must be approx'ly square..
//                    tall = tall>>16;
//                    zx = (tall>>4)+2; // allowed margin of difference
//                    if (tall+zx<wide) break; // why = 18
//                    if (wide+zx<tall) break;} //~if
//                why++; // why = 19
//                rez = 0; // not an error
//                for (whar=here-3; whar >= 1; whar += -3) { // check for touch/overlap..
//                    if (blobz==null) break;
//                    if (whar>blobz.length-3) break;
//                    zx = blobz[whar];
//                    if (zx==0) continue;
//                    if (((zx-colo)&15) !=0) continue;
//                    info = blobz[whar+1];
//                    more = blobz[whar+2];
//                    if (((butter+0x10001-info)&0x80008000) !=0) continue;
//                    if (((more+0x10001-topple)&0x80008000) !=0) continue;
//                    colo = zx; // we can merge these..
//                    topple = MyMath.iMin(topple&-0x10000,info&-0x10000)
//                            + MyMath.iMin(topple&0xFFFF,info&0xFFFF);
//                    butter = MyMath.iMax(butter&-0x10000,more&-0x10000)
//                            + MyMath.iMax(butter&0xFFFF,more&0xFFFF);
//                    if (NoisyBlob) if (Qlog<0)
//                        System.out.println(HandyOps.Dec2Log("  (=+=) ",here,
//                                HandyOps.Dec2Log(" => ",whar,HandyOps.Dec2Log(" => ",topple>>16,
//                                        HandyOps.Dec2Log("/",topple&0xFFFF,
//                                                HandyOps.Dec2Log(" .. ",butter>>16,
//                                                        HandyOps.Dec2Log("/",butter&0xFFFF,""))))) ));
//                    blobz[whar+1] = topple;
//                    blobz[whar+2] = butter;
//                    rez = colo; // = blobz[whar];
//                    break;} //~for // (whar)
//                // if (rez==0) break; // why = 19
//                why = -why; // why = -19
//                break;} //~if // (try to merge this blob)
//            if (whom<0) { // make a new blob here.....................................
//                why = 4;
//                if (row <= 0) break;
//                if (coln <= 0) break;
//                if (row>ScrHi-2) break;
//                if (coln>ImgWi-2) break;
//                colo = whom&15;
//                why++; // why = 5
//                if (colo>7) break;
//                butter = (row<<16)+coln;
//                topple = (whom&-0x10000)-((whom>>4)&0xFFF)+butter;
//                why++; // why = 6
//                if (topple<0x10000) break;
//                if ((topple&0xFFFF)==0) break;
//                if (tops>0) here = tops+3;
//                else here = 1;
//                why++; // why = 7
//                if (here>FastBlobs*3) break;
//                if (here<0) break;
//                if (here>blobz.length-4) break;
//                why++; // why = 8
//                info = (here<<16)+colo;
//                blobz[here] = info;
//                blobz[here+1] = topple;
//                blobz[here+2] = butter;
//                if (tops<1) rez = 0;
//                else rez = NewBlob(info,row,coln,blobz); // validate & merge
//                if (rez<0) {
//                    blobz[here] = 0;
//                    break;} //~if // why = 8
//                if (rez==0) { // did not merge, so added blob is good..
//                    tops = here;
//                    blobz[0] = tops;
//                    rez = info;} //~if // = (tops<<16)+colo;
//                why = -why; // why = -8
//                break;} //~if
//            // if (whom==0) { // compact blob space, remove invalid blobs.............
//            why++; // why = 2
//            if (whom !=0) break; // huh?
//            why++; // why = 3
//            if (NoisyBlob) { // test for =1 mod 3 (valid index)..
//                info = tops;
//                while (info>3) info = (info>>2)+(info&3); // same as "casting out 9s"
//                if (info !=1) break;} //~if
//            rez = 0; // assume no errors at first
//            for (whar=tops; whar >= 1; whar += -3) { // exits after no changes
//                thar = 1;
//                for (here=1; here <= whar; here += 3) {
//                    info = here<<16;
//                    if (here<blobz.length) {
//                        zx = blobz[here];
//                        if (zx==0) { // deleted, skip over this one..
//                            if (NoisyBlob) if (Qlog<0)
//                                System.out.println(HandyOps.Dec2Log("  (=^=) ",here,""));
//                            continue;} //~if
//                        info = zx&15|info;} //~if
//                    info = NewBlob(info,row,coln,blobz); // validate & merge
//                    if (info !=0) { // invalid or merged, now ignored..
//                        if (info>0) colo = info&15;
//                        if (rez==0) rez = info; // capture 1st merge
//                        else if (info<0) rez = info; // some error occurred
//                        if (info+2 !=0) continue;} //~if
//                    if (here==thar) if (info+2 !=0) { // list so far unchanged..
//                        thar = thar+3;
//                        continue;} //~if
//                    if (row>0) if (here>0) if (here<blobz.length-3)
//                        if ((blobz[here+2]>>16) >= row) { // open blob, so..
//                            blobz[here] = 0; // ..mark it invalid, but don't move it
//                            if (NoisyBlob) if (Qlog<0)
//                                System.out.println(HandyOps.Dec2Log("  (=0=) ",here,""));
//                            continue;} //~if
//                    if (NoisyBlob) if (Qlog<0)
//                        System.out.println(HandyOps.Dec2Log("  (=_=) ",here,
//                                HandyOps.Dec2Log(" => ",thar,"")));
//                    for (zx=here; zx <= here+2; zx++) // copy this blob down..
//                        if (thar>0)
//                            if (zx<blobz.length) if (thar<blobz.length) {
//                                blobz[thar] = blobz[zx];
//                                thar++;}} //~for // (zx) (here)
//                if (thar==tops) break; // all the way through, no changes: done
//                while (tops>thar) {
//                    blobz[tops] = 0;
//                    tops = tops-3;} //~while
//                tops = thar;
//                blobz[0] = tops;} //~for // (whar)
//            why = 0;
//            // break;} //~if // (compact blob space)
//            break;} //~while // (once thru)
//        // if (whom>0) colo = whom&15;
//        kx = whom;
//        if (kx !=0) {
//            colo = colo&15;
//            kx = colo-raid;
//            if (kx !=0) kx = colo-grin;} //~if
//        if (NoisyBlob) if (Qlog<0) if (kx==0 ) {
//            if (whom==0) DrTmpSt = HandyOps.Int2Log("=== (NuBlob) ====> ",rez," 0 ");
//            else if (whom<0) DrTmpSt = HandyOps.Int2Log("(NuBlob) ==> ",rez,
//                    HandyOps.Dec2Log(" ",whom>>16,HandyOps.Dec2Log(",",(whom>>4)&0xFFF,
//                            HandyOps.Dec2Log(",",whom&15," "))));
//            else /* if (whom>0) */ DrTmpSt = HandyOps.Int2Log("    (NuBlob) => ",rez,
//                        HandyOps.Dec2Log(" ",whom>>16,HandyOps.Dec2Log(",",whom&15," ")));
//            DrTmpSt = HandyOps.Dec2Log(DrTmpSt,row,HandyOps.Dec2Log("/",coln,
//                    HandyOps.Dec2Log(" ^",tops,HandyOps.Int2Log(" ",info,
//                            HandyOps.Dec2Log(" = ",why, "")))));
//            System.out.println(DrTmpSt); }
//        return rez;} //~NewBlob
//
//    /**
//     * Gets the next image from whichever camera is selected.
//     *
//     * @return       True if successful, false otherwise.
//     *               The image is returned in class variable thePixels
//     */
//    public boolean GetCameraImg() { // -> thePixels, true if OK // (in DrDemo)
//        final int Post2BGB = (4<<(raid*4))+(2<<(grin*4))
//                +(1<<(blew*4))+(6<<(yelo*4))+(7<<(whyt*4))+(8<<(grey*4));
//        int rx, cx, wide = ScrWi, my2L = Scr2L, // Scr2L = WinWi*2
//                kx, mx, rid = 0, grn = 0, blu = 0, lxx = 0, avg = 0, raw = 0,
//                tile = 0, cz = 0, zx = 0, whom = 0, pixx = 0, here = 0, thar = 0;
//        boolean alive = LiveCam&&CameraView&&CamActive&&(CamPix != null),
//                gotit = alive;
//        byte[] myBy = SimBytes; // local vars visible in debugger ;-)
//        int[] myPix = thePixels; // both are dim'd [ScrPix] = ImHi*WinWi
//        int[] PoPix = PostPix; // ditto
//        int[] myRo = RoSums; // dim'd [ScrWi]
//        int[] blobz = theBlobs; // dim'd [FastBlobs*3]
//        FlyCamera myVid = simVideo;
//        OhDark = true;
//        if (FastBlobs>0) if (blobz != null) blobz[0] = 0; // always fresh
//        if (alive) {
//            myVid = theVideo;
//            tile = CamTile;
//            wide = CamWide;
//            my2L = wide+wide;
//            myBy = CamPix;} //~if
//        else tile = SimTile;
//        if (myBy != null) if (myPix != null) if (myVid != null) {
//            if (!alive) if (!ContinuousMode) if (StepMe) if (NoneStep >= 0)
//                if (Calibrating==0) {
//                    theSim.SimStep(1);
//                    NoneStep = -NoneStep;} //~if
//            StepMe = false;
//            gotit = myVid.NextFrame(myBy);
//            if (alive) if (!gotit) {
//                if (myVid.Live()) whom++;
//                whom++;}} //~if
//        if (tile != 1) if (tile != 3) gotit = false;
//        if (!gotit) {
//            ViDied++;
//            System.out.println(HandyOps.TF2Log("** GetCamIm ",(myVid != null),
//                    HandyOps.TF2Log("/",myBy != null,HandyOps.TF2Log("/",myPix != null,
//                            HandyOps.TF2Log(" = ",gotit,HandyOps.Dec2Log(" ** ",wide, // gotit =
//                                    HandyOps.Dec2Log("/",ScrWi,HandyOps.TF2Log(" ",alive,
//                                            HandyOps.Dec2Log("/",whom,HandyOps.Dec2Log(" ",tile,
//                                                    HandyOps.Dec2Log(" ",ViDied,""))))) ))))) );
//            if (whom !=0) if (myVid != null) System.out.println(myVid.toString());
//            return false;} //~if
//        if (!alive) CamFrame = theSim.GetFrameNo(); // updated by GetSimFrame
//        else CamFrame ++;
//        if (FastBlobs>0) for (cx=wide-1; cx >= 0; cx += -1) if (myRo == null) break;
//        else if (cx<myRo.length) myRo[cx] = -1;
//        for (rx=0; rx <= ScrHi-1; rx++) {
//            cz = wide-1;
//            avg = 0; // used for Gaussian average
//            lxx = -1; // horizontal run length
//            for (cx=0; cx <= cz; cx++) {
//                if (myBy == null) break; // can't, Java throws an exception instead
//                zx = here+my2L; // {4}
//                if (zx<0) break;
//                if (zx>myBy.length -2) break; // {6?}
//                if (here<0) break;
//                if (here>myBy.length -2) break; // {total index prep ~ =20}
//                if (tile==1) {                  // RG/GB.. {3}
//                    rid = ((int)myBy[here])&255; // {3+x}
//                    grn = ((int)myBy[here+1])&255; // {3+x}
//                    blu = ((int)myBy[zx+1])&255;} //~if // {3+x}
//                else if (tile==3) {         // GB/RG.. {3}
//                    blu = ((int)myBy[here+1])&255;
//                    grn = ((int)myBy[here])&255;
//                    rid = ((int)myBy[zx])&255;} //~if
//                pixx = (((rid<<8)|grn)<<8)|blu; // {8} {net =22+3x}
//                here = here+2;
//                if (myPix == null) break;
//                if (thar<0) break;
//                if (thar >= myPix.length) break;
//                myPix[thar] = pixx; // <- unsaturated result
//                if ((pixx&0x00F0F0F0) !=0) OhDark = false;
//
//                // Tom Pittman's Speedy Blobs (posterize)..
//                if (Postrize) if (cx<ImgWi) if (PoPix != null) if (thar >= 0)
//                    if (thar<PoPix.length) {
//                        whom = pixx; // keep a copy to mangle
//                        raw = rid+grn+grn+blu;
//                        whom = raw+avg+avg+avg; // luminance.. {11}
//                        avg = (whom+2)>>2; // running luminance average {4}
//                        whom = whom&-16; // = (despeckled) luminance*16 {4}
//                        zx = whom>>4; // luminance normalized to 8 bits {3}
//                        mx = MyMath.iMax(MyMath.iMax(rid,grn),blu); // {8}
//                        if (whom>WhitThresh) // WhitThresh=208 {4}
//                            whom = whom+whyt; // white {3}
//                        else if (whom<BlakThresh) // BlakThresh=56 {4}
//                            whom = whom+blok; // black {0}       // EquThresh = 48..
//                        else if (MyMath.iMin(MyMath.iMin(rid,grn),blu)+EquThresh>mx) { // {11}
//                            if (whom>BlakThresh+BlakThresh) whom = whom+grey; // gray {4+3}
//                            else whom = whom+blok;} //~if // black {0}
//                        else if (rid==mx) { // red is dominant.. {4}
//                            if (grn+grn >= rid+zx) whom = whom+yelo; // yellow {8+3}
//                            else if (zx+zx>rid) whom = whom+grey; // gray {6+3}
//                            else whom = whom+raid;} //~if // red {3}
//                        else if (blu==mx) { // blue is dominant.. {4}
//                            if (grn+grn >= blu+zx) whom = whom+blew; // teal => blue {8+3}
//                            else if (zx+zx>blu) whom = whom+grey; // gray {6+3}
//                            else whom = whom+blew;} //~if // blue {3}
//                        else if (rid+rid>grn+zx) // green is dominant.. {6}
//                            whom = whom+yelo; // yellow {3}
//                        else if (zx+zx+zx>(grn<<2)) whom = whom+grey; // gray {8+3}
//                        else whom = whom+grin; // green {3} {<=45}
//                        PoPix[thar] = (raw<<16)+whom; // lo 4: post index, next 8: lum {x}
//                        if (ShoPoGray||NoisyBlob) { // display posterized pix on window..
//                            mx = (whom&7)<<2; // {5} (this part is not particularly speedy)
//                            mx = (Post2BGB>>mx)&15; // Post2BGB=x70816240 {6}
//                            mx = (mx&4)*(255<<14)+(mx&2)*(255<<7)+(mx&1)*255 // {16}
//                                    + (mx&8)*0x111111;
//                            kx = (whom>>4)*0x10101; // {5}
//                            zx = thar+ScrPix+ScrWi-ImgWi; // {4}
//                            if (zx>0) if (zx<myPix.length) myPix[zx] = kx; // {6+x}
//                            zx = thar+ScrPix; // {4}
//                            if (zx>0) if (zx<myPix.length)
//                                myPix[zx] = mx;} //~if // (display posterized pix) {6+x} {=139+6x=}
//
//                        // Tom Pittman's Speedy Blob Finder..
//                        if (FastBlobs>0) if (rx<HaffHi) if (rx>0) if (myRo != null) // {6}
//                            if (cx<myRo.length) if (blobz != null) {
//                                whom = whom&15; // = posterized color index {4}
//                                zx = myRo[cx]; // = vertical run length {x}
//                                grn = zx&15; // existing V run color from above {4}
//                                blu = lxx&15; // existing H run color from left {4}
//                                // if (whom==whyt) if ((grn==raid)||(blu==raid))
//                                //   whom = raid; // see white as red if red up/left {0/15} {=18}
//                                if ((whom==raid)||(whom==grin)) { // ||(whom==yelo)) { // these only.. {12}
//                                    if (whom==grn) { // got match vertically.. {4}
//                                        rid = zx>>16; // the blob index + (if any) {3}
//                                        if (rid <= 0) { // {2}
//                                            zx = zx-0x10000;} //~if // add to height // {4}
//                                        else if (rid <= blobz[0]) if (rid<blobz.length-4)
//                                            if (blobz[rid]==zx) { // matched existing blob.. {2x+11}
//                                                grn = blobz[rid+2]; // set new bottom.. // {x+2}
//                                                grn = MyMath.iMax(rx<<16,grn&-0x10000)|grn&0xFFFF; // {12}
//                                                if (lxx <= 0) if (whom==blu) { // merge H-run.. {6}
//                                                    mx = blobz[rid+1]; // {x+1}
//                                                    mx = MyMath.iMin((lxx>>16)-1+cx,mx&0xFFFF)|mx&-0x10000; // {15}
//                                                    blobz[rid+1] = mx; // {x}
//                                                    lxx = zx;} //~if // {2}
//                                                blobz[rid+2] = grn;}} //~if // {x+1} {=76+6x}
//                                    else zx = whom-0x10000; // start new V-run {4} ..or {=20}
//                                    if (whom==blu) { // got match horizont'ly.. {4}
//                                        rid = lxx>>16; // the blob index + (if any) {3}
//                                        if (rid <= 0) {
//                                            lxx = lxx-0x10000;} //~if // add to width // {7}
//                                        else if (rid <= blobz[0]) if (rid<blobz.length-4)
//                                            if (blobz[rid]==lxx) { // matched existing blob.. {2x+7}
//                                                blu = blobz[rid+2]; // set new right.. // {x+1}
//                                                blu = MyMath.iMax(cx,blu&0xFFFF)|blu&-0x10000; // {12}
//                                                if (zx<0) { // merge V-run.. {2}
//                                                    mx = blobz[rid+1]; // {x+1}
//                                                    mx = MyMath.iMin((rx<<16)+zx,mx&-0x10000)|mx&0xFFFF;
//                                                    blobz[rid+1] = mx; // {x+15}
//                                                    zx = lxx;} //~if // {2}
//                                                blobz[rid+2] = blu;}} //~if // {x+1} {=54+5x}
//                                    else lxx = whom-0x10000; // start new H-run {4} ..or {=8}
//                                    if ((MinBlobDim&-0x10000)+zx<=0) if ((MinBlobDim&-0x10000)+lxx<=0) // {11}
//                                        if ((zx>>16)+rx>1) if ((lxx>>16)+cx>1) { // big 'nuff.. {12}
//                                            mx = zx&~0xFFF0|(-(lxx&-0x10000)>>12); // ..to be blob.. {10}
//                                            mx = NewBlob(mx,rx,cx,blobz); // {f}
//                                            if (mx>0) { // {2}
//                                                lxx = mx; // {2}
//                                                zx = mx;}}} //~if // {2} {=39+f}
//                                else { // not an interesting color.. {4}
//                                    lxx = -1;
//                                    zx = -1;} //~else // most often minimal: {=40+2x}
//                                myRo[cx] = zx;}} //~if // {x+1} {=187+12x+f=} // (Postrize)
//                thar++;} //~for // (cx)
//            here = here+my2L;
//            thar = thar+ScrWi-wide;} //~for // (rx)
//        if (FastBlobs>0) {
//            here = NewBlob(0,0,0,blobz); // validate & merge all blobs
//            if (NoisyBlob) if (Qlog<0) if (blobz != null) { // log the blobs..
//                here = blobz[0];
//                System.out.println("(Blobs) => "
//                        + HandyOps.ArrayDumpLine(blobz,here+4,11));}} //~if
//        return true;} //~GetCameraImg
//
//    /**
//     * Sends a steering servo message to the hardware (and to TrakSim).
//     *
//     * @param  fixt  True: whar is a signed absolute angle (usually 0);
//     *               False: whar is a signed inc/decrement to current setting
//     *
//     * @param  whar  The angle (increment) for the steering servo
//     */
//    public void SteerMe(boolean fixt, int whar) { // -> SetServo // SteerServo=9
//        if (!fixt) whar = SteerDegs+whar; // SteerDeg is centered on 0
//        whar = MyMath.iMax(MyMath.iMin(whar,90),-90);
//        if (whar !=0) if (whar==SteerDegs) return;
//        SteerDegs = whar;
//        if (Calibrating==0) {
//            if (whar<0) {
//                if (LefScaleSt<1.0) // LefScaleSt = LeftSteer/90.0
//                    whar = (int)Math.round(LefScaleSt*MyMath.Fix2flt(whar,0));} //~if
//            else if (whar>0) if (RitScaleSt>1.0)
//                whar = (int)Math.round(RitScaleSt*MyMath.Fix2flt(whar,0));} //~if
//        if (theServos == null) return;
//        StepMe = true;
//        theServos.servoWrite(SteerPin,whar+90);} //~SteerMe
//
//    /**
//     * Sends a drive ESC message to the hardware (and to TrakSim).
//     *
//     * @param  fixt  True: whar is a signed absolute velocity;
//     *               False: whar is a signed inc/decrement to current setting
//     *
//     * @param  whar  The velocity (increment) for the ESC
//     */
//    public void AxLR8(boolean fixt, int whar) { // -> SetServo // GasServo=10
//        if (!fixt) whar = GasPedal+whar; // GasPed is centered on 0
//        if (whar !=0) {
//            whar = MyMath.iMax(MyMath.iMin(whar,90),-90);
//            if (whar==GasPedal) return;} //~if
//        if (Calibrating==0) if (whar==0) if (!fixt)
//            if (StartYourEngines==0) if (GasPedal==0) return;
//        GasPedal = whar;
//        if (theServos == null) return;
//        StepMe = true;
//        theServos.servoWrite(GasPin,whar+90);} //~AxLR8
//
//    /**
//     * Get current odometer reading from ArduinoIO/HardAta or TrakSim.
//     *
//     * @param  sim  True: get it from TrakSim; False: from HardAta
//     *
//     * @return      The current odometer in park meters (=8x actual meters)
//     */
//    private double GetOdometer(boolean sim) { // ** (stub) **
//        double whom = 0.0;
//        // int now = HandyOps.TimeSecs(true); // TS version
//        return whom;} //~GetOdometer
//
//    /**
//     * Safe shutdown, because some hardware drivers need their close.
//     *
//     * @param  why   A number to log, the reason or cause
//     */
//    public void Stopit(int why) { // gotta turn the camera & JSSC off..
//        String aWord = "-------- Clean Stop -------- ";
//        FlyCamera myVid = theVideo;
//        try {
//            AxLR8(true,0);
//            SteerMe(true,0);
//            if (myVid != null) myVid.Finish();
//            if (theServos != null) theServos.Close();
//        } catch (Exception ex) {}
//        System.out.println(HandyOps.Dec2Log(aWord,why," @ ") + HandyOps.TimeStamp(false));
//        System.exit(why);}
//
//
//    // Java wants these if 'implements MouseListen' ..
//    @Override public void mouseExited(MouseEvent evt) {}
//    @Override public void mousePressed(MouseEvent evt) {}
//    @Override public void mouseReleased(MouseEvent evt) {}
//
//    /**
//     * Recognize a mouse rollover into the top left corner of the screen from
//     *   outside the window, so to start up self-driving software (or whatever).
//     */
//    @Override public void mouseEntered(MouseEvent evt) { // (in DrDemo)
//        Insets edges = getInsets();
//        int nx = 0, Vx = 0, Hx = 0, why = 0;
//        while (true) {
//            why++; // why = 1
//            if (!StartLive) return; // don't even log
//            why++; // why = 2
//            if (Calibrating !=0) break;
//            why++; // why = 3
//            if (!CameraView) break;
//            if (evt != null) if (edges != null) {
//                Hx = evt.getX()-edges.left;
//                Vx = evt.getY()-edges.top;} //~if
//            why++; // why = 4
//            if (Hx>ImgWi) break;
//            nx = theSim.GridBlock(Vx,Hx); // find which screen chunk it came in..
//            why++; // why = 5
//            if (nx != 0x10001) break; // top left corner only (from outside win)..
//            NoneStep = 0; // continuous
//            theSim.SimStep(0);
//            unPaused = true; // start it running..
//            why++; // why = 6
//            if (StartYourEngines>0) break;
//            if (ContinuousMode) theSim.SimStep(2);
//            StartYourEngines++;
//            DidFrame = 0;
//            if (mySpedFixt) AxLR8(true,StartGas);
//            else if (DarkState<2) DarkState = 2;
//            // You can use (DarkState >= 2) to start your self-driving software,
//            // ..or else insert code here to do that..
//            why = 0;
//            break;} //~while
//        if ((Qlog&2) !=0)
//            System.out.println(HandyOps.Dec2Log("(DrDemo) Got MousEnt = ",why,
//                    HandyOps.Dec2Log(" @ ",Vx,HandyOps.Dec2Log(",",Hx,
//                            HandyOps.Int2Log(": ",nx,
//                                    HandyOps.Dec2Log(" ",StartYourEngines,
//                                            HandyOps.TF2Log(" g=",unPaused,HandyOps.TF2Log(" cv=",CameraView,
//                                                    HandyOps.Dec2Log(" ns=",NoneStep,HandyOps.Dec2Log(" ",Calibrating,
//                                                            HandyOps.PosTime( ((
//                                                                    " @ "))))) ))))) )));} //~mouseEntered
//
//    /**
//     * Accepts clicks on screen image to control operation
//     */
//    @Override public void mouseClicked(MouseEvent evt) { // (in DrDemo)
//        Insets edges = getInsets();
//        int ix, kx = 0, nx = 0, zx = 0, Vx = 0, Hx = 0, why = 0;
//        boolean didit = false, rite = false;
//        if (evt != null) if (edges != null) { // we only implement/o'ride this one
//            if (evt.getButton()==MouseEvent.BUTTON3)
//                rite = true;
//            Hx = evt.getX()-edges.left;
//            Vx = evt.getY()-edges.top;} //~if
//        if (Hx<ImgWi) {
//            why = theSim.GridBlock(Vx,Hx); // find which screen chunk it's in..
//            zx = why&0xFF;
//            nx = why>>16;
//            if (((nx-1)|(zx-5))==0) { // top right corner captures screen w/FrameNo
//                HandyOps.ScreenCapt(theSim.GetFrameNo()+1000,ScrTall,ScrWi,thePixels);
//                if (false)
//                    HandyOps.WriteWholeTextFile("TestWrite.txt",theSim.toString());} //~if
//            else if (nx<3) { // top half, switch to camera view..
//                didit = ((nx|zx)==1); // top left corner simulates covering lens..
//                if (didit) theSim.DarkFlash(); // unseen if switches to live cam
//                CameraView = (theVideo != null)
//                        && (CamPix != null);
//                if (CameraView) {
//                    theSim.Activate(false); // turn TrakSim off if not being used
//                    theSim.SimStep(0);
//                    if (Calibrating>0) SteerMe(true,0);
//                    else if (Calibrating<0) AxLR8(true,0); // stop
//                    else if (didit) { // if click top-left, stop so ESC can recover..
//                        AxLR8(true,0);
//                        unPaused = false;
//                        StartYourEngines = 0;} //~if
//                    Calibrating = 0;} //~if
//                DidFrame = 0;
//                unPaused = CameraView && (nx==1);} //~if // top edge runs // (nx<3)
//            else if (nx==3) { // middle region, manual steer/calibrate..
//                if (Calibrating<0) {
//                    if (zx==4) AxLR8(true,0);
//                    else AxLR8(false,Grid_Moves[zx&7]);} //~if
//                else if (zx==4) SteerMe(true,0);  // Grid_Moves={0,-32,-8,-1,0,1,8,32,..
//                else SteerMe(false,Grid_Moves[zx&7]);
//                theSim.FreshImage();} //~if
//            else if (Calibrating>0) {
//                SteerMe(true,0); // straight ahead
//                Calibrating = -1;} //~if
//            else if (Calibrating<0) {
//                AxLR8(true,0); // stop
//                Calibrating = 0;
//                theSim.SimStep(1);
//                StartYourEngines = 0;} //~if
//            else if (nx==5) { // bottom, switch to sim view..
//                CameraView = false;
//                theSim.Activate(true); // turn TrakSim back on
//                DidFrame = 0;
//                if (theSim.IsCrashed()) theSim.SimStep(0); // clear crashed mode
//                if (zx==2) NoneStep = 1; // left half: 1-step
//                else NoneStep = 0; // right half: continuous
//                if (ContinuousMode) theSim.SimStep(2);
//                else theSim.SimStep(1);
//                unPaused = ((zx>1)&&(zx<4));} //~if // corners: DrDemo not control speed
//            else if (nx==4) { // low half..
//                if (zx<2) Stopit(0); // low half, left edge, kill it politely
//                else if (!CameraView) // otherwise toggle pause..
//                    unPaused = !unPaused;}} //~if
//        else if (ShowMap) {
//            ix = MyMath.Trunc8(theSim.GetFacing());
//            zx = theSim.GetMapSize(); // MapHy,MapWy = size of full map
//            nx = Hx-2-ImgWi;
//            if ((Vx<(zx>>16)) && (nx<(zx&0xFFF))) theSim.SetStart(Vx,nx,ix);
//            else zx = theSim.ZoomMap2true(!rite,Vx,Hx); // sets facing to -> click
//            if (rite) {
//                Vx = zx>>22; // ZoomMap2tru rtns (r,c)<<6..
//                Hx = (zx>>6)&0xFF;
//                if (Vx>0) if (Hx>0) theSim.SetStart(Vx,Hx,ix);} //~if
//            unPaused = false; // pause if click on map
//            theSim.FreshImage();} //~if
//        if (Calibrating==0) {
//            why = 256;
//            if (!unPaused) { // pause it..
//                why--; // why = 255
//                if (StartYourEngines>0) theSim.SimStep(0);
//                StartYourEngines = 0;
//                AxLR8(true,0);} //~if
//            else if (StartYourEngines==0) { // start..
//                why++; // why = 257
//                if (ContinuousMode) theSim.SimStep(2);
//                // else theSim.SimStep(1);
//                StartYourEngines++;
//                DidFrame = 0;
//                if (mySpedFixt && (ServoTestCount==0)) AxLR8(true,StartGas);
//                else if (DarkState<2) DarkState = 2;}} //~if
//        if ((Qlog&2) !=0)
//            System.out.println(HandyOps.Dec2Log("(DrDemo) Got click @ ",Vx,
//                    HandyOps.Dec2Log(",",Hx,HandyOps.Dec2Log(": ",nx,
//                            HandyOps.Dec2Log("/",zx,HandyOps.Dec2Log(" +",kx,
//                                    HandyOps.Dec2Log(HandyOps.IffyStr(rite,"=r "," "),StartYourEngines,
//                                            HandyOps.TF2Log(" s=",
//                                                    false,
//                                                    HandyOps.TF2Log(" g=",unPaused,HandyOps.TF2Log(" cv=",CameraView,
//                                                            HandyOps.Dec2Log(" ns=",NoneStep,HandyOps.Dec2Log(" ",Calibrating,
//                                                                    HandyOps.Dec2Log(" ",why,
//                                                                            HandyOps.PosTime( (
//                                                                                    " @ "))))) ))))) ))))) ;} //~mouseClicked
//
//    /**
//     * Exercise steering & ESC servos, but only if live camera.
//     */
//    public void TestServos() { // exercise steering & ESC servos
//        final int ServoMsgPos = DriverCons.D_ServoMsgPos,
//                LeftSteer = DriverCons.D_LeftSteer,
//                RiteSteer = DriverCons.D_RiteSteer,
//                ServoMsgTL = DriverCons.D_ServoMsgTL,
//                ServoMsgSiz = DriverCons.D_ServoMsgSiz;
//        boolean nowate = (nServoTests>99)&&((nServoTests&1) !=0),
//                doit = (StartYourEngines !=0) || nowate, flash = doit;
//        int faze = CamFrame, info = ScrFrTime, why = 0;
//        if (nServoTests <= 0) return; // normal exit (test turned off)
//        if (info <= 0) return; // shouldn't happen
//        while (info<400) { // reduce this to half-second steps..
//            if ((faze&1) !=0) return; // ignore intermediate frames
//            faze = faze>>1;
//            info = info<<1;} //~while
//        while (true) {
//            why++; // why = 1
//            if (ServoTestCount <= 0) break;
//            why++; // why = 2
//            if (!CamActive) break; // only works if camera is turned on
//            why++; // why = 3
//            if (StartLive) break; // only if manual start
//            why++; // why = 4
//            if (!CanDraw) break;
//            why++; // why = 5
//            if (ServoMsgPos <= 0) break; // position of warning in image file,
//            why++; // why = 6
//            if (ServoMsgTL <= 0) break;
//            why++; // why = 7
//            if (ServoMsgSiz <= 0) break;
//            why++; // why = 8
//            if (ServoTstPos==0) {
//                ServoTstPos = theSim.GetImgWide();
//                if (ServoTstPos <= 0) break;
//                ServoTstPos = (ServoMsgPos>>16)*ServoTstPos+(ServoMsgPos&0xFFFF);} //~if
//            why++; // why = 9
//            if (flash) if ((ServoTestCount&1)==0) flash = false;
//            if (!flash) theSim.SeeOnScrnPaint(ServoMsgTL>>16,ServoMsgTL&0xFFFF,
//                    ServoMsgSiz>>16,ServoMsgSiz&0xFFFF,ServoTstPos,-1);
//            if (!CameraView) break;
//            why++; // why = 9
//            if (!doit) break;
//            why = 0;
//            ServoTestCount--;
//            faze = ServoTestCount&7;
//            switch (faze) {
//                case 0:
//                case 2:
//                case 4:
//                    AxLR8(true,0);
//                    SteerMe(true,0);
//                    break;
//                case 1:
//                    if (LeftSteer>90) why = 11;
//                    else if (LeftSteer<0) why = 11;
//                    else SteerMe(true,-LeftSteer);
//                    break;
//                case 3:
//                    if (RiteSteer>90) why = 13;
//                    else if (RiteSteer<0) why = 13;
//                    else SteerMe(true,RiteSteer);
//                    break;
//                case 5:
//                    if (MaxESCact>90) why = 15; // finally fast
//                    else if (MaxESCact<0) why = 15;
//                    else AxLR8(true,MaxESCact);
//                    break;
//                case 6:
//                    if (FastSet>90) why = 16; // then medium
//                    else if (FastSet<0) why = 16;
//                    else AxLR8(true,FastSet);
//                    break;
//                case 7:
//                    if (MinESCact>90) why = 17; // first slow
//                    else if (MinESCact<0) why = 17;
//                    else AxLR8(true,MinESCact);
//                    break;} //~switch
//            break;} //~while
//        if (Qlog<0) System.out.println(HandyOps.Dec2Log("(TestServo) ",faze,
//                HandyOps.Dec2Log("/",CamFrame,
//                        HandyOps.Dec2Log(" = ",why,""))));} //~TestServos
//
//    /**
//     * Examples of drawing on TrakSim image. Delete what you don't need...
//     */
//    public void DrawDemo() { // examples of drawing on TrakSim image..
//        int haff = ScrHi/2, tx = theSim.TurnRadRow(), t2x = tx>>16,
//                HafScrn = ImgWi/2, nx, zx = 0, whar = 0;
//        double fmid = MyMath.Fix2flt(HaffScrn,0),
//                tmp = DriverCons.D_MetersPerTurn;
//        int[] blobz = theBlobs;
//        String aLine;
//        boolean ShowOdometer = false;
//        if (!CanDraw) return;
//        if (theSim.IsItProxim()) {
//            if (Qlog<0) System.out.println("!! Proxx !!");} //~if
//        tx = tx&0xFFF;
//        if (ShowOdometer) {
//            nx = theSim.DriveShaftCount(false);
//            if (nx>0) {
//                aLine = HandyOps.Flt2Log("",MyMath.Fix2flt(nx,0)*tmp/256,"");
//                theSim.LabelScene(aLine,12,ScrWi-8,0xFFFFFF);}} //~if
//        if (mySpedFixt) zx++; // mySpedFixt is blue
//        if (zx>0) { // notify SimInTrak on-screen..
//            nx = zx>>1; // zx=2 is red
//            nx = (nx<<7)-nx;
//            zx = zx&1;
//            zx = (zx<<7)-zx; // mySpedFixt (zx=1) is blue
//            zx = (nx<<16)+zx;
//            theSim.RectFill(zx,2,2,5,5);} //~if // both is purple
//        if (ShowDateTime) if (ShowMap)
//            if (((theSim.GetMapSize()&0xFFFF)<169) || ((Qlog&256) !=0)) {
//                nx = 0;
//                if (ShoTimFrm != CamFrame) {
//                    aLine = HandyOps.TimeStamp(false);
//                    nx = HandyOps.NthOffset(0,",",aLine);
//                    if (nx>2) ShoTimStr = HandyOps.Substring(nx-2,2,aLine)
//                            + HandyOps.ReplacAll(".",":",HandyOps.RestOf(nx+1,aLine));
//                    ShoTimFrm = CamFrame;
//                    if (Mini_Log) System.out.println(HandyOps.Dec2Log("(ShowDateTim) ",ShoTimFrm,
//                            HandyOps.Dec2Log(" ",nx,"'" + ShoTimStr + "'")));} //~if
//                if (ShoTimStr != "")
//                    theSim.LabelScene(ShoTimStr,12,ScrWi-44,AddColo);} //~if
//        for (nx=ScrHi-8; nx >= 8; nx += -8) { // draw tics on right edge of image..
//            if ((nx&31)==0) zx = ImgWi-5;
//            else zx = ImgWi-2;
//            theSim.DrawLine(AddColo,nx,zx,nx,ImgWi-1);} //~for
//        if (Calibrating !=0) {
//            theSim.SetPixSize(4);
//            if (Calibrating>0) zx = SteerDegs;
//            else zx = GasPedal;
//            if (zx>9) nx = 16;
//            else if (zx < -9) nx = 24;
//            else if (zx<0) nx = 16;
//            else nx = 8;
//            theSim.LabelScene(HandyOps.Dec2Log("",zx,""),haff-80,HafScrn+nx,0);
//            if (Calibrating>0) aLine = "S";
//            else aLine = "E";
//            theSim.LabelScene(aLine,haff-12,HafScrn+8,AddColo);
//            if (Calibrating>0) aLine = "-E-";
//            else aLine = "-0-";
//            theSim.LabelScene(aLine,haff+40,HafScrn+23,CarColo);
//            theSim.SetPixSize(0);} //~if
//        else if (tx>haff) { // draw pink lines at turn radius tx & 2*tx = t2x..
//            if (tx<ScrTop) theSim.DrawLine(CarColo,tx,HaffScrn-32,tx,HaffScrn+32);
//            else tx = ScrTop-1;
//            if (t2x>haff) if (t2x<tx) { // also center line between them..
//                theSim.DrawLine(CarColo,t2x,HaffScrn,tx,HaffScrn);
//                theSim.DrawLine(CarColo,t2x,HaffScrn-32,t2x,HaffScrn+32);
//                whar = theSim.ZoomMapCoord(false,MyMath.Fix2flt(tx,0),fmid);
//                if (whar>0) { // draw center line on map close-up, if possible..
//                    zx = 0;
//                    while (t2x+4<tx) {
//                        zx = theSim.ZoomMapCoord(false,MyMath.Fix2flt(t2x,0),fmid);
//                        if (zx>0) break;
//                        t2x = t2x+4;} //~while
//                    if (zx>0) theSim.DrawLine(CarColo,zx>>16,zx&0xFFF,
//                            whar>>16,whar&0xFFF);}}} //~if
//        if (FastBlobs>0) if (NoisyBlob||GoodLog) if (blobz != null) {
//            whar = blobz[0];
//            if (SawBlobFr != CamFrame) {
//                zx = whar/3;
//                if (whar>0) zx++;
//                if (Qlog<0) System.out.println(HandyOps.Dec2Log("** Blobs: ",zx,
//                        HandyOps.Dec2Log(" (",whar,HandyOps.Dec2Log(") ",CamFrame," **"))));
//                SawBlobFr = CamFrame;} //~if
//            if (whar <= 0) return;
//            if (NoisyBlob) if (Qlog<0)
//                System.out.println("  -- " + HandyOps.ArrayDumpLine(blobz,whar+4,-5));
//            for (whar=whar; whar >= 1; whar += -3) {
//                if (whar<0) break;
//                if (whar>blobz.length-3) break;
//                if (blobz[whar]==0) continue;
//                tx = blobz[whar+1];
//                zx = blobz[whar+2];
//                nx = MyMath.iMax((tx&0xFFFF)-2,0); // left
//                tx = MyMath.iMax((tx>>16)-2,0); // top
//                t2x = (zx>>16)+2; // bottom
//                zx = (zx&0xFFFF)+2; // right
//                theSim.DrawLine(MarinBlue,tx,nx,t2x,nx); // down left side
//                theSim.DrawLine(MarinBlue,t2x,nx,t2x,zx); // across bottom
//                theSim.DrawLine(MarinBlue,tx,zx,t2x,zx); // up right side  // across top
//                theSim.DrawLine(MarinBlue,tx,nx,tx,zx);}}} //~DrawDemo
//
//    /**
//     * Converts a RGB pixel array to BufferedImage for painting.
//     * Adapted from example code found on StackExchange.
//     *
//     * @param  pixels  The pixel array
//     *
//     * @param  width   Its width
//     *
//     * @param  height  Its height
//     *
//     * @return         The BufferedImage result
//     */
//    public BufferedImage Int2BufImg(int[] pixels, int width, int height) // (in DrDemo)
//            throws IllegalArgumentException {
//        int lxx = 0;
//        int[] theData = null; // Raster raz = null;  DataBufferInt DBI = nell;
//        BufferedImage bufIm = null;
//        if (pixels != null) lxx = pixels.length;
//        if (lxx==0) return null;
//        if (width==ScrWi) if (height == ScrTall) bufIm = theBuff;
//        if (bufIm==null) // (should be never)
//            bufIm = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
//        if (bufIm==null) return null;
//        theData = ((DataBufferInt) bufIm.
//                getRaster().getDataBuffer()).getData();
//        System.arraycopy(pixels,0,theData,0,lxx);
//        return bufIm;} //~Int2BufImg
//
//    /**
//     * All the heavy lifting happens here. Called on timer activation.
//     */
//    @Override public void paint(Graphics graf) { // (in DrDemo)
//        int fno = CamFrame, prio = NoneStep, zx, nx = 0, DimSave = 0;
//        double dist = 0.0, sped = 0.0;
//        int[] SaveScrn = null;
//        Insets edges;
//        if (DriverCons.D_Log_Draw)
//            if (Qlog<0) System.out.println(HandyOps.TF2Log("DrDemo.paint ",BusyPaint,
//                    HandyOps.TF2Log(" ",theImag != null,
//                            HandyOps.TF2Log(" ",thePixels != null,"" ))));
//        if (BusyPaint) return; // Java won't accept local static method vars
//        if (ViDied>3) Stopit(ViDied); // video gone
//        BusyPaint = true;
//        edges = getInsets();
//        super.paint(graf);
//        // if (theImag == null)
//        if (thePixels != null) try {
//            if (theImag != null) if (graf != null) if (edges != null)
//                graf.drawImage(theImag,edges.left,edges.top,null);
//            if (StartYourEngines>0) StepMe = true;
//            if (GetCameraImg()) { // sets: NoneStep = -NoneStep
//                if (NoisyFrame !=0) if ((NoisyFrame&0xFFF)<CamFrame)
//                    NoisyFrame = (NoisyFrame>>12)&0xFFFFF; // get 3 if 3rd + < 256
//                // Use: if ((NoisyFrame&0xFFF)==CamFrame) ...
//                fno = CamFrame;
//                if (((DarkState&1)==0)==OhDark) { // if (CamActive)
//                    DarkState++; // briefly cover camera lens for state change..
//                    if (Qlog<0) System.out.println(HandyOps.Dec2Log("*** DarkFrame ",fno,
//                            HandyOps.Dec2Log(" ",DarkState,HandyOps.PosTime(" *** @ "))));} //~if
//                CanDraw = false;
//                if (DrawStuff) {
//                    DimSave = theSim.GetMyScreenDims();
//                    SaveScrn = theSim.GetMyScreenAry();
//                    if (thePixels != null) {
//                        theSim.SetMyScreen(thePixels,ScrTall,ScrWi,1);
//                        if (DimSave==0) DimSave--;
//                        CanDraw = true;
//                        if ((SawSpeed>0) || !CameraView) { // do odometer reading..
//                            if (!CameraView) {
//                                dist = theSim.GetDistance(false);
//                                sped = theSim.GetSpeed();} //~if
//                            else if (ArdPinUpd != null) {
//                                dist = ArdPinUpd.GetDistance();
//                                sped = ArdPinUpd.GetSpeed();}
//                            if (Mini_Log) if (dist+sped > 0.0) {
//                                theSim.RectFill(PilasterCo,32,4,52,28); // PilasterCo=666
//                                theSim.LabelScene(HandyOps.Flt2Log("",dist,""),40,24,0xFFFFFF);
//                                theSim.LabelScene(HandyOps.Flt2Log("",sped,""),48,24,0xFFFFFF);} //~if
//                            SawSpeed = 0;} //~if
//                        if (!ShoClikGrid) { // otherwise TrakSim did it..
//                            nx = 0;
//                            if (Calibrating !=0) nx++;
//                            if (nx>0) theSim.DrawGrid();}} //~if
//                    else DimSave = 0;} //~if
//                if ((StartYourEngines==0)||(prio<0)) fno = DidFrame;
//
//                ///** If you have self-driving code, you could put it here **///
//                TestServos(); // (replace this with your own code)
//                if (CanDraw) {
//                    DrawDemo();
//                    if (false) if (CameraView)
//                        theSim.DrawSteerWheel(SteerDegs,true,true);} //~if
//                DidFrame = fno;
//                if (DrawStuff) if (CanDraw) if (DimSave>0) if (SaveScrn != null)
//                    theSim.SetMyScreen(SaveScrn,DimSave>>16,DimSave&0xFFFF,1);
//                theImag = Int2BufImg(thePixels,ScrWi,ScrTall);}} //~if
//        catch (Exception ex) {theImag = null;}
//        BusyPaint = false;} //~paint
//
//    private static void starting() {theWindow = new DrDemo();}
//
//    public static void main(String[] args) { // (in DrDemo)
//        Runnable runFrameLater = new RunSoon();
//        System.out.println(HandyOps.Dec2Log("(main) image size ",ScrWi,
//                HandyOps.Dec2Log("x",ScrHi,HandyOps.Dec2Log(" = ",ScrPix,
//                        " @ " + HandyOps.TimeStamp(false)))));
//        SwingUtilities.invokeLater(runFrameLater);}
//
//    /**
//     * This is the constructor, which sets everything up.
//     */
//    public DrDemo() { // outer class constructor..
//        // int CamFPS = FlyCamera.FrameRate_15-1; // FrameRate_15=3
//        int nx = ScrPix, tall = ScrHi;
//        String sayso = "= " + ScrWi + "x" + ScrHi; // "(Cal8="
//        boolean dunit = NoisyBlob ;
//        Timer titok;
//        FlyCamera myVid = null;
//        int[] myPix;
//        // unPaused = StartLive; // F=paused, so it requires user action to start
//        if ((FastBlobs>0)||Postrize) { // set up Tom Pittman's Speedy Blobs..
//            if (dunit)
//                ShoPoGray = Postrize;
//            if (Postrize) PostPix = new int[ScrPix];
//            if (PostPix==null) ShoPoGray = false;
//            else if (ShoPoGray || dunit) { // double win to sho poster..
//                sayso = sayso + " +PoCo";
//                tall = ScrHi*2;
//                nx = ScrPix*2;} //~if
//            if (FastBlobs>0) theBlobs = new int[FastBlobs*3+2];
//            if (theBlobs != null) {
//                theBlobs[0] = 0;
//                RoSums = new int[ImgWi];
//                if (RoSums != null) sayso = sayso + " +Blobs";}} //~if
//        if (StartInCalibrate) Calibrating = 1;
//        else if (LiveCam) if (nServoTests>0) if (ScrFrTime>40)
//            if (ScrFrTime<555) ServoTestCount = nServoTests*8-4;
//        System.out.println(HandyOps.Dec2Log("(Cal8=",Calibrating,
//                HandyOps.Dec2Log(") pix ",ScrPix*4,sayso)));
//        simVideo = new SimCamera();
//        theServos = new ArduinoIO();
//        theSim = new TrakSim();
//        if (LiveCam) theVideo = new FlyCamera();
//        mySpedFixt = theSim.IsFixtSped();
//        if (TestTimes) // normally you want to omit this..
//            TimeTest(0,TimedMethod(0,10*1000*1000)); // a safe way to call it
//        ViDied = 0;
//        dunit = theServos.IsOpen();
//        myPix = new int[nx]; // nx = ScrPix = ImHi*WinWi
//        SimBytes = new byte[ScrPix*4];
//        thePixels = myPix;
//        while (nx>0) {
//            if (myPix==null) break;
//            nx--;
//            if (nx<0) break;
//            if (nx<myPix.length) myPix[nx] = 0x6699CC;} //~while // prefill with blue-gray
//        doOften = new PaintAction();
//        TickTock = new Timer(
//                ScrFrTime,doOften);                           // (5/fps)
//        // reduce memory-manager burden by pre-allocating this..
//        theBuff = new BufferedImage(ScrWi,tall, BufferedImage.TYPE_INT_RGB);
//        titok = TickTock;
//        myVid = simVideo;
//        if (!dunit) System.out.println("FakeFirmata failed to open "
//                + ArduinoIO.CommPortNo);
//        else if (myVid != null) try {
//            nx = 0;
//            dunit = myVid.Connect(CamFPS);
//            if (theServos.GetFirmwareRev()>0x120000) {
//                SeenHardAta = true;
//                System.out.println("HardAta seen & active...");
//                nx = ArduinoIO.DEADMAN_MESSAGE;
//                theServos.addInputListener(nx,ArdPinUpd);
//                nx = ArduinoIO.REPORT_MISCELLANY;
//                theServos.addInputListener(nx,ArdPinUpd);
//                theServos.LogHardAta();
//                nx = ArduinoIO.REPORT_PULSECOUNT;
//                theServos.addInputListener(nx,ArdPinUpd); // DeadMan_Pin = 11..
//                theServos.pinMode(DeadMan_Pin, ArduinoIO.DEADMAN);
//                theServos.DoPulseCnt(PulseCnt_Pin,AskOdomTime); // PulseCnt_Pin = 8
//                nx = ArduinoIO.DM_SERVO;} //~if
//            else {
//                System.out.println("HardAta not seen");
//                nx = ArduinoIO.SERVO;} //~else
//            theServos.pinMode(SteerPin, ArduinoIO.SERVO);
//            if (nx>0) theServos.pinMode(GasPin,(byte)nx);} // GasPin=10, SteerPin = 9
//        catch (Exception ex) {dunit = false;}
//        else {
//            System.out.println("SimCamera failed to open");
//            dunit = false;} //~else
//        if (LiveCam) if (dunit) while (true) { try {
//            myVid = theVideo;
//            dunit = false;
//            if (myVid==null) break;
//            dunit = myVid.Connect(CamFPS); // OK to fail true, cuz CamActive=false
//            if (!dunit) break;
//            CamTile = myVid.PixTile();
//            nx = myVid.Dimz();
//            CamTall = nx>>16;
//            CamWide = nx&0xFFF;
//            nx = CamTall*CamWide;
//            CamPix = new byte[nx*4];
//            if (CamTall==ScrHi) if (nx>1023) if (CamWide <= ScrWi)
//                if ((CamTile==1) || (CamTile==3)) CamActive = (CamPix != null);
//        } catch (Exception ex) {CamActive = false; dunit = false;}
//            break;} //~while // (LiveCam)
//        if (!dunit) {
//            System.out.println("Connect failed"); // only early retn
//            Stopit(-1);
//            return;} //~if
//        else if (titok != null) titok.start();
//        theSim.Activate(CamActive); // turn TrakSim off if not being used
//        if (theSim.GetFacing() == 0.0) if (theSim.GetPosn(false) == theSim.GetPosn(true))
//        {}
//        AxLR8(true,0); // initialize stopped
//        SteerMe(true,0); // ..and straight ahead
//        if (Calibrating !=0) theSim.SimStep(0);
//        else if (StartLive) if (CamPix != null) if (theVideo != null)
//            if (theVideo.Live()) {
//                CameraView = true;
//                theSim.SimStep(0);} //~if
//        theSim.Activate(!CameraView); // turn TrakSim on/off as live camera is off/on
//        System.out.println("DrDemo " + sayso);
//        setTitle("DriveDemo Example"); // was: this.setTitle etc..
//        setSize(ScrWi+18,tall+40); // make it larger for insets to come
//        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
//        addMouseListener(this); // if 'implements MouseListen..'
//        setVisible(true);}} //~DrDemo (drivedemo) (DM)
