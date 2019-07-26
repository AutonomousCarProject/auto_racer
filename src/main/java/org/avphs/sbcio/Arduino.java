/* FakeFirmata -- a simple way to control servos from LattePanda in Java.
 *
 * This is essentially a translation of (small parts of) LattePanda's Arduino.cs
 * into Java for using the attached Arduino to control servos.
 *
 * Under US Copyright law this miniscule copy counts as "Fair Use" and in the
 * public domain, but if you are worried about it, or if you extend it to
 * include more of their code, then you will be bound by the onerous rules of
 * the GNU General Public License or whatever they are currently using.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but probably will work with any compatible Java serial port API.
 * When using TrakSim on a computer with no serial port, you will need to
 * import noJSSC (included with TrakSim) instead of JSSC.
 */
package org.avphs.sbcio; // (class Arduino)                 // 2019 April 18

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

// import noJSSC.SerialPort; // use these 2 lines instead to use TrakSim
// import noJSSC.SerialPortEvent; // ..on a computer with no serial port.

import jssc.SerialPort; // use these 2 lines in LattePanda..
import jssc.SerialPortEvent; // (comment them out when using noJSSC)
import org.avphs.sbcio.fakefirm.*;
// import jssc.SerialPortEventListener; // (didn't work for me, so unused)

import javax.swing.Timer;

public class Arduino { // Adapted to Java from arduino.cs ... (FakeFirmata)

    protected static boolean GoodOpen = false; // -- [Consistency Check!]

    public static final String CommPortNo = "COM5"; // or maybe "COM3"
    public static final boolean SpeakEasy = false, // enables logging
            LogAllPcnts = false; // always log pulse counts & DeadMan

    public static final int MAX_DATA_BYTES = 64, // =64 in LattePanda's Arduino.cs
            MDB_msk = MAX_DATA_BYTES - 1, SpeakHard = 15; // cf SpokeHard
    public static final int SET_PIN_MODE = 0xF4;
    // set a pin to INPUT/OUTPUT/PWM/etc
    public static final int DIGITAL_MESSAGE = 0x90;
    // send 8-bit data for a whole digital port
    public static final int ANALOG_MESSAGE = 0xE0;
    // send data for an analog pin (or PWM)

    // Use these selectors to set listeners..
    public static final int REPORT_ANALOG = 0xC0;
    // enable analog input by pin +
    public static final int REPORT_DIGITAL = 0xD0;
    // enable digital input by port
    public static final int REPORT_VERSION = 0xF9;
    // report firmware version

    public static final int REPORT_PULSECOUNT = 0xA0;
    // request pulse count feedback from Arduino (new)
    public static final int REPORT_INFO = 0xFA;
    // request diagnostic logging (new)
    public static final int DEADMAN_MESSAGE = 0xFB;
    // (logged) DeadMan transition (new)
    public static final int REPORT_MISCELLANY = 0xFD;
    // do something & report result (new)

    public static final byte LOW = 0;
    public static final byte HIGH = 1;
    public static final byte INPUT = 0;
    public static final byte OUTPUT = 1;
    public static final byte ANALOG = 2;
    public static final byte PWM = 3;
    public static final byte SERVO = 4;

    public static final byte DM_SERVO = 5; // (output) (new)
    public static final byte DEADMAN = 6; // (informative) (new)
    public static final byte PULSECOUNT = 7; // (input) (new)

    protected static SimHookBase DoMore = null; // for extensions
    private static final boolean AutoStartInputs = false, GetFirmData = false;

    private static int PCtime = 0, PrioSeriTime = 0, waitForData = 0,
            multiByteChannel = 0, exMultiComm = 0, MultiData = 0, SpokeHard = 0,
            ProPulsePin = 0, CountingPulses = 0, GotFirmVersion = 0,
            StartUpTimeMS = (int) System.currentTimeMillis();
    private static boolean ShowLogging = false;

    private static int[] digitalInputData;
    private static int[] analogInputData = new int[Arduino.MAX_DATA_BYTES];

    private static int[] ArduPiModes = new int[16];

    protected int[] digitalOutputData;
    protected
    SerialPortSwapper surrealPort;

    private static UpdateListener PulseCountUpdate = null;
    private static UpdateListener FeedbackUpdate = null;
    private static UpdateListener FirmwareUpdate = null;
    private static UpdateListener DeadManUpdate = null;
    private static UpdateListener DigiPinUpdate = null;
    private static UpdateListener AnaPinUpdate = null;

    private static Timer EatOften = null; // (gone if not static)
    private static SerialPortEvent OneSerEvt = null;

    /**
     * Convenient (millisecond) time fetch.
     *
     * @return The number of milliseconds since program started.
     */
    public static int GetMills() {
        return (int) System.currentTimeMillis() - StartUpTimeMS;
    } //~GetMills

    /**
     * Convenient (millisecond) time formatter.
     *
     * @param prefix String added to front
     * @param now    The relative time to format, in (int) milliseconds
     * @return Formatted String prefix+hh:mm:ss.mmm
     */
    public static String FormatMillis(String prefix, int now) {
        int whom = 0;
        boolean more = false;
        if (now == 0x80000000) {
            now = GetMills();
        }
        if (now < 0) {
            return FormatMillis(prefix + "-", -now);
        }
        if (now >= 3600000) { // convert to hours..
            whom = now / 3600000;
            now = now - whom * 3600000;
            prefix = prefix + whom + ":";
            more = true;
        }
        if (now >= 60000) { // convert to minutes..
            if (more && now < 600000) {
                prefix = prefix + "0";
            }
            whom = now / 60000;
            now = now - whom * 60000;
            prefix = prefix + whom + ":";
            more = true;
        } else if (more) {
            prefix = prefix + whom + "00:";
        }
        if (more && now < 10000) {
            prefix = prefix + "0";
        }
        whom = now / 1000;
        now = now - whom * 1000;
        prefix = prefix + whom + ".";
        if (now < 10) {
            prefix = prefix + "00";
        } else if (now < 100) {
            prefix = prefix + "0";
        }
        return prefix + now;
    } //~FormatMillis

    /**
     * Current status of the FakeFirmata library, =true if successfully open.
     *
     * @return true if successfully open, false if failed or closed
     */
    public boolean IsOpen() {
        return GoodOpen;
    } // true if opened successfully

    /**
     * Send a 3-byte packet to Arduino Firmata/HardAta..
     *
     * @param comm The command byte, must be 128..255
     * @param data The first data byte, must be 0..127, or else (-8192)..+16383
     * @param more Second data byte, 0..127, or else <0 if data is 14 bits
     */
    public void Send3bytes(int comm, int data, int more) {      // in {FF} Arduino
        int info = data & 127;
        boolean logy = ((SpokeHard & 0xFFFF) < SpeakHard)
                || LogAllPcnts && (comm == REPORT_PULSECOUNT); // = 0xA0;
        byte[] msg = new byte[3];
        SpokeHard++;
        if (more < 0){
            more = (data >> 7) & 127;
            if (logy) System.out.println("F%%F/Send3by " + comm + " " + data
                    + " .. => " + info + " " + more + FormatMillis(" @ ", 0x80000000));
            data = info;
        } else if (logy){
            System.out.println("F%%F/Send3by " + comm + " " + data + " " + more +
                    FormatMillis(" @ ", 0x80000000));
        }
        if (comm < 128){
            return;
        } // not a valid command byte..
        if (comm > 255){
            return;
        }
        more = more & 127;
        data = info;
        msg[0] = (byte) (comm);
        msg[1] = (byte) (data);
        msg[2] = (byte) (more);
        try {
            surrealPort.writeBytes(msg);
            if (DoMore != null) DoMore.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~Send3bytes

    public void setServoAngle(int pin, int angle) {
        byte[] msg = new byte[3];
        msg[0] = (byte) (ANALOG_MESSAGE); //Type of message. Likely unneeded
        msg[1] = (byte) (pin); //pin
        msg[2] = (byte) (angle); //angle
        try {
            surrealPort.writeBytes(msg);
            if (DoMore != null) DoMore.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    /**
     * Verifies that HardAta is installed and returns the "Firmware" version.
     *
     * @return A 32-bit number representing the major revision in the
     * upper 16 bits (HardAta returns the 2-diti year (18=2018),
     * and the minor revision in the low 16 bits (HardAta returns
     * the month (12=December).
     */
    public int GetFirmwareRev() { // (simple test of getting Arduino input)
        int g0 = 0, g1 = 0, g2 = 0, prio = GotFirmVersion, why = -1;
        String gottem = "";
        byte[] reply = null;
        if (SpeakEasy){
            System.out.println("F%%F/GetFirmRev.."); // SpeakEasy=true
        }
        GotFirmVersion = 0; // so we know we got most recent result
        Send3bytes(Arduino.REPORT_VERSION, 1, 2); // = 0xF9 (249)
        try {
            Thread.sleep(1000); // delay 1 second -- give it time to respond
            if (surrealPort.getInputBufferBytesCount() > 0){ // if (GetFirmData)
                reply = surrealPort.readBytes(3);
            }
            if (reply != null) { // we caught it before ProcessInp..
                if (reply.length > 0) {
                    g0 = reply[0];
                    gottem = " => " + g0;
                } //~if
                if (reply.length > 1) {
                    g1 = reply[1];
                    gottem = gottem + " " + g1;
                } //~if
                if (reply.length > 2) {
                    g2 = reply[2];
                    gottem = gottem + " " + g2;
                } //~if
                why = reply.length;
            } //~if
            if (GotFirmVersion > 0) { // ProcessInp got it..
                g1 = GotFirmVersion >> 16;
                g2 = GotFirmVersion & 0xFFF;
                g0 = Arduino.REPORT_VERSION;
                gottem = " => " + g0 + " " + g1 + " " + g2;
                why = 7;
            } //~if
            else if ((g0 & 255) == Arduino.REPORT_VERSION){
                GotFirmVersion = (g1 << 16) + (g2 & 0xFFFF);
            } // large Java bytes -> <0
            if (why >= 3) {
                if (SpeakEasy) {
                    if (GotFirmVersion == 0) {
                        System.out.println(" **F%%F/GotWhatever**" + gottem
                                + " = " + ((g2 << 7) + g1) + " (" + why + ")");
                        GotFirmVersion = prio;
                    } //~if
                    else System.out.println(" (F%%F/GetFirmRev)" + gottem);
                } //~if
                return GotFirmVersion;
            } //~if
        } catch (Exception ex) {
            System.out.println(ex);
        }
        if (SpeakEasy){
            System.out.println(" (F%%F/GetFirmRev)? = " + why + gottem);
        }
        return GotFirmVersion;
    } //~GetFirmwareRev

    /**
     * Turns on HardAta logging.
     */
    public void LogHardAta() { // (simple turn-on to log NotFirm diagnostics)
        int info;
        if (GotFirmVersion == 0){
            info = GetFirmwareRev();
        }
        if (GotFirmVersion > 18 * 0x10000) { // >: HardAta is installed..
            ShowLogging = true;
            Send3bytes(Arduino.REPORT_INFO, 8, 7); // = 0xFA (250)
            if (SpeakEasy){
                System.out.println(" (F%%F/LogHardAta)");
            }
        } else{
            System.out.println(" (F%%F/Can'tLog,NoHard)");
        }
    } //~LogHardAta

    /**
     * Set a (single) listener (once) for each type of input client wants.
     *
     * @param selector One of six input types:
     *                 Arduino.REPORT_VERSION -- to get the current version
     *                 Arduino.REPORT_PULSECOUNT -- to get current pulse count
     *                 Arduino.DEADMAN_MESSAGE -- to see DeadMan transitions
     *                 Arduino.REPORT_DIGITAL -- (unsupported) digital input
     *                 Arduino.REPORT_ANALOG -- (unsupported) analog input
     *                 Arduino.REPORT_MISCELLANY -- to get other HardAta output
     * @param whom     An UpdateListener class where the inputs will be sent
     */
    public void addInputListener(int selector, UpdateListener whom) {
        String whar = "";
        if (selector == Arduino.REPORT_VERSION) { // = 0xF9 (249)
            whar = " VERSION";
            FirmwareUpdate = whom;
        } else if (selector == Arduino.REPORT_MISCELLANY) {
            whar = " MISCELLANY";                        // = 0xFD (253)
            FeedbackUpdate = whom;
        } else if (selector == Arduino.DEADMAN_MESSAGE) {
            whar = " DEADMAN";                           // = 0xFB (251)
            DeadManUpdate = whom;
        } else if ((selector == Arduino.INPUT)
                || (selector == Arduino.REPORT_DIGITAL)) {
            whar = " DIGITAL";                         // = 0xD0 (208)
            DigiPinUpdate = whom;
        } else if ((selector == Arduino.ANALOG)
                || (selector == Arduino.REPORT_ANALOG)) {
            whar = " ANALOG";                         // = 0xC0 (192)
            AnaPinUpdate = whom;
        } else if ((selector == Arduino.PULSECOUNT)
                || (selector == Arduino.REPORT_PULSECOUNT)) {
            whar = " PULSECOUNT";                      // = 0xA0 (160)
            PulseCountUpdate = whom;
        }
        if (SpeakEasy) {
            if (whom != null) whar = whar + " -> " + whom.toString();
            System.out.println("F%%F/addInList #" + selector + whar);
        }
    } //~addInputListener

    // Start this pin as either analog or digital input..

    private void OpenPinInput(int pin, int mode) { // = 0xE0.. // = 0x90..
        int whom = (((mode & 0x10) << 1) + 0x20) ^ mode; // -> ANALOG_/DIGITAL_MESSAGE
        Timer trix = EatOften;
        if (SpeakEasy){
            System.out.println("F%%F/OpPinInp #" + pin + " = " + mode);
        }
        if(mode == Arduino.REPORT_VERSION){
            whom = mode; // = 0xF9
        } else if (mode == Arduino.PULSECOUNT){
            whom = mode; // = 7
        } else if (mode != Arduino.REPORT_DIGITAL){         // = 0xD0
            if (mode != Arduino.REPORT_ANALOG) {
                whom = 0;   // = 0xC0
            }
        }
        if (whom > 0){
            ArduPiModes[pin & 15] = whom;
        } else {
            trix = null;
        }
        if (trix == null){ // log any failure..
            System.out.println("  (F%%F/OpPinIn) failed: " + whom + "/" + mode);
            return;
        } else if (trix.getDelay() == 0) { // start up input thread..
            trix.setDelay(1);
            trix.start();
            if (SpeakEasy){
                System.out.println("  (F%%F/OpPinIn) Timer start OK");
            }
        }
        Send3bytes(mode | pin & 15, 1, 0);
    } //~OpenPinInput // we only turn it on, not off

    /**
     * Sets the mode of the specified pin (INPUT or OUTPUT).
     *
     * @param pin  The arduino pin.
     * @param mode Mode Arduino.OUTPUT or Arduino.SERVO
     *             (+ new Arduino.DM_SERVO, Arduino.DEADMAN, Arduino.PULSECOUNT)
     *             (Arduino.INPUT, Arduino.ANALOG or Arduino.PWM not supported)
     */
    public void pinMode(int pin, byte mode) {                   // in {FF} Arduino
        int whom = ((int) mode) & 255;
        if (SpeakEasy){
            System.out.println("F%%F/pinMode #" + pin + " = " + whom);
        }
        if ((whom & -8) != 0){
            return; // not a valid pin mode
        }
        if ((pin & -16) != 0){
            return; // not a valid pin
        }
        /*  Old version:  */
        if (ArduPiModes[pin & 15] != whom){
            while (true) {
                if (whom == Arduino.INPUT){
                    whom = Arduino.REPORT_DIGITAL; // = 0xD0 (208)
                } else if (whom == Arduino.ANALOG) {
                    whom = Arduino.REPORT_ANALOG; // = 0xC0 (192)
                } else if (whom != Arduino.REPORT_VERSION && whom != Arduino.PULSECOUNT){
                    break; // = 7
                }
                OpenPinInput(pin, whom);
                break;
            } //~while
        }
        /*  New Version:  */
      /*
        if (ArduPiModes[pin & 15] != whom && whom == Arduino.REPORT_VERSION && whom == Arduino.PULSECOUNT){
            if (whom == Arduino.INPUT){
                whom = Arduino.REPORT_DIGITAL;
            } else if (whom == Arduino.ANALOG){
                whom = Arduino.REPORT_ANALOG;
            }
            OpenPinInput(pin, whom);
        }
        */
        Send3bytes(SET_PIN_MODE, pin, mode);
    } //~pinMode // SET_PIN_MODE = 0xF4 (244)

    /**
     * Write to a digital pin that has been toggled to output mode
     * with pinMode() method.
     *
     * @param pin   The digital pin to write to.
     * @param value Value either Arduino.LOW or Arduino.HIGH.
     */
    public void digitalWrite(int pin, byte value) { // pack this bit into..
        int portNumber = (pin >> 3) & 0x0F;      // ..8-bit port & send to Ardu
        int digiData = digitalOutputData[portNumber & MDB_msk];
        if (SpeakEasy){
            System.out.println("F%%F/digiWrite #" + pin + " = " + value);
        }
        if ((int) value == 0){
            digiData = digiData & ~(1 << (pin & 7));
        } else {
            digiData = digiData | (1 << (pin & 7));
        }
        digitalOutputData[portNumber & MDB_msk] = digiData;
        Send3bytes(DIGITAL_MESSAGE | portNumber, digiData, -1);
    } //~digitalWrite

    /**
     * For controlling a servo.
     *
     * @param pin   Servo output pin.
     * @param angle Servo angle from 0 to 180.
     */
    public void servoWrite(int pin, int angle) {
        if (SpeakEasy){
            System.out.println("F%%F/servoWrite #" + pin + " = " + angle);
        }
        Send3bytes(ANALOG_MESSAGE | pin & 15, angle, -1);
    } //~servoWrite

    /**
     * Turn on pulse-counting.
     *
     * @param pin Input pin to count pulses.
     * @param ms  Count period, in milliseconds.
     */
    public void DoPulseCnt(int pin, int ms) { // (simple pulse count turn-on)
        int info;
        if (GotFirmVersion == 0){
            info = GetFirmwareRev();
        }
        if (GotFirmVersion < 18 * 0x10000){
            return; // >: HardAta is installed
        }
        CountingPulses = ms;
        ProPulsePin = pin;
        pinMode(pin, Arduino.PULSECOUNT);
    } //~DoPulseCnt

    /**
     * For reading the state of a digital input. This is not supported
     * in HardAta.ino, and has not been tested in the real Firmata.
     *
     * @param pin Arduino digital input pin.
     * @return Arduino.HIGH or Arduino.LOW
     */
    public int digitalRead(int pin) { // (unsupported in HardAta.ino)
        int info = digitalInputData[(pin >> 3) & MDB_msk], aBit = (info >> (pin & 7)) & 1;
        if (SpeakEasy){
            System.out.println("F%%F/digiRead #" + pin + " = " + aBit + "/" + info);
        }
        return aBit;
    } //~digitalRead

    /**
     * For reading the state of an analog input. This is not supported
     * in HardAta.ino, and has not been tested in the real Firmata.
     *
     * @param pin Arduino analog input pin.
     * @return A number representing the analog value between 0 (0V)
     * and 1023 (5V).
     */
    public int analogRead(int pin) { // (unsupported in HardAta.ino)
        int info = analogInputData[pin & MDB_msk];
        if (SpeakEasy){
            System.out.println("F%%F/analoRead #" + pin + " = " + info);
        }
        return info;
    } //~analogRead

    private void ProcessInput(SerialPortEvent evt) {
        int nx, aBit, prio, inData, spoken = SpokeHard >> 16, info = 0;
        byte[] inBytes = null;
        if (evt == null){
            return;
        }
        nx = evt.getEventType();
        if (nx == SerialPortEvent.RXCHAR) while (true) {
            inData = -1; // stay here getting data as long as it comes..
            try {
                if (surrealPort.getInputBufferBytesCount() == 0){
                    break; // (normal exit)
                }
                inBytes = surrealPort.readBytes(1);
            } catch (Exception ex) {
                System.out.println(ex);
            }
            if (inBytes != null){
                inData = ((int) inBytes[0]) & 255;
            }
            SpokeHard = SpokeHard + 0x10000;
            spoken++;
            if (spoken < SpeakHard) System.out.println("F%%F/ProcInp = "
                    + inData + " -- " + waitForData + " / " + exMultiComm);
            if (inData < 0) break; // -> return, wait for next byte (normal exit)
            if (waitForData > 0) if (inData < 128) {
                MultiData = (MultiData >> 7) + (inData << 7); // 1st byte is lo 7, 2nd is hi
                info = MultiData & 127;
                waitForData--;
                if (waitForData > 0) continue;
                if (exMultiComm == Arduino.REPORT_VERSION) {
                    if (SpeakEasy || ShowLogging)              // = 0xF9 (249)
                        System.out.println("  -> FirmVersion = " + info + "." + inData);
                    info = (info << 16) | inData;
                    GotFirmVersion = info; // so GetFirmRev can see it
                    if (FirmwareUpdate != null)
                        FirmwareUpdate.pinUpdated(exMultiComm, info);
                } else if (exMultiComm == Arduino.REPORT_PULSECOUNT) {
                    if ((spoken < SpeakHard) || LogAllPcnts || ShowLogging)      // = 0xA0 (160)
                        System.out.println("  -> PulseCount = " + MultiData
                                + FormatMillis(" @ ", 0x80000000));
                    if (PulseCountUpdate != null)
                        PulseCountUpdate.pinUpdated(ProPulsePin, MultiData);
                } else if (exMultiComm == Arduino.DEADMAN_MESSAGE) {
                    if ((spoken < SpeakHard) || LogAllPcnts || ShowLogging)      // = 0xFB (251)
                        System.out.println("  -> DeadMan = " + MultiData + " " + (inData > 63)
                                + FormatMillis(" @ ", 0x80000000));
                    if (DeadManUpdate != null) DeadManUpdate.pinUpdated(
                            Arduino.DEADMAN_MESSAGE, MultiData);
                } else if (exMultiComm > 127) { // any other received message..
                    nx = info & 127;
                    if (SpeakEasy || ShowLogging) System.out.println("  -> Feedback ("
                            + exMultiComm + ") = " + nx + "/" + inData + " = " + MultiData);
                    if (FeedbackUpdate != null)
                        FeedbackUpdate.pinUpdated(exMultiComm, (info << 16) | inData);
                }

                // ** review & rewrite these two (not in HardAta)..
                else if (exMultiComm == Arduino.DIGITAL_MESSAGE) {
                    prio = digitalInputData[multiByteChannel & MDB_msk];
                    aBit = 1;
                    for (nx = 0; nx <= 7; nx++) {
                        if (nx > 0) aBit = aBit << 1;
                        if (((prio ^ info) & aBit) == 0) continue;
                        digitalInputData[multiByteChannel & MDB_msk] = info;
                        if ((info & aBit) == 0) aBit = Arduino.LOW;
                        else aBit = Arduino.HIGH;
                        if (SpeakEasy) System.out.println("  -> DigiMsg = " + aBit);
                        if (DigiPinUpdate != null)
                            DigiPinUpdate.pinUpdated(multiByteChannel * 8 + nx, aBit);
                        break;
                    }
                } //~for (nx)
                else if (exMultiComm == Arduino.ANALOG_MESSAGE) {
                    analogInputData[multiByteChannel & MDB_msk] = info;
                    if (SpeakEasy) System.out.println("  -> AnaloMsg = " + info);
                    if (AnaPinUpdate != null)
                        AnaPinUpdate.pinUpdated(multiByteChannel, info);
                }
                if (waitForData > 0) continue; // go back for more input, if any
                break;
            } // -> return
            info = inData & 0xF0;                    // = 0xF9 (249)..
            if (inData == Arduino.DEADMAN_MESSAGE) info = inData;
            else if (inData == Arduino.REPORT_VERSION) info = inData;
            else if (false) if (info != Arduino.DIGITAL_MESSAGE)
                if (info != Arduino.ANALOG_MESSAGE) break;
            multiByteChannel = inData & 0x0F;  // always get two bytes of input..
            waitForData = 2; // then cycle back around to get any ready data
            exMultiComm = info;
            if (spoken >= SpeakHard) {
                if (!LogAllPcnts) continue;         // = 0xA0 (160), 0xFB (251)..
                if (info != Arduino.REPORT_PULSECOUNT)
                    if (inData != Arduino.DEADMAN_MESSAGE) continue;
            }
            System.out.println("  -> multiByChan = " + inData
                    + FormatMillis(" +2 @ ", 0x80000000));
        } //~while //~ProcessInpu
    }

    // SerialPortEventListener didn't work, so I wrote a timer loop to do it...

    private void TestSerialInput() {
        int now = GetMills(), nby = now - PrioSeriTime;
        SerialPortSwapper myPort = surrealPort;
        if (myPort == null) return; // otherwise test for input, fwd to listener..
        PrioSeriTime = now;
        if (SpeakEasy) if (nby > 222) // should be every ms, but seems not..
            System.out.println(FormatMillis(" (LATE TSI) +", nby)
                    + FormatMillis(" @ ", now));
        if (CountingPulses > 0) { // once each second, ask for current count..
            PCtime = PCtime + nby; // count actual elapsed time
            if (PCtime >= CountingPulses) { // always logs..
                Send3bytes(REPORT_PULSECOUNT, 0, 0); // pin + is ignored
                PCtime = PCtime - CountingPulses;
            }
        } //~if
        try {
            nby = surrealPort.getInputBufferBytesCount();
            if (nby <= 0) return;
            if (OneSerEvt == null) // ProcInp only looks at type, so keep this one..
                OneSerEvt = new SerialPortEvent(
                        CommPortNo, SerialPort.MASK_RXCHAR, nby);
            ProcessInput(OneSerEvt);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~TestSerialInput

    private class EatCereal implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            TestSerialInput();
        }
    } //~EatCereal

    private EatCereal FrootLoop = null;

    /**
     * Use this to link simulator actions to Firmata.
     *
     * @param whom An instance of a subclass of SimHookBase
     */
    public static void HookExtend(SimHookBase whom) {
        DoMore = whom;
    }

    /**
     * Opens the serial port connection, should it be required.
     * By default the port is opened when the object is first created.
     * <p>
     * Note that JSSC may not recover gracefully from a failure to close,
     * so a subsequent Open() may fail until the system is rebooted.
     */
    public void Open() {                                        // in {FF} Arduino
        boolean prio = GoodOpen;
        int nx, bitz = 0;
        if (SpeakEasy) System.out.println("F%%F/Open, (pre: GoodOpen = "
                + GoodOpen + ")");
        if (prio) {
            if (SpeakEasy) System.out.println("... " + CommPortNo + " is already open");
            return;
        } else try {
            GoodOpen = surrealPort.openPort();
            surrealPort.setParams(57600, 8, 1, 0);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        if (SpeakEasy) {
            if (GoodOpen) System.out.println("... " + CommPortNo + " is now open");
            else System.out.println("... " + CommPortNo + " failed to open");
        }
        if (GoodOpen) try {
            bitz = 8; // (for log)
            // surrealPort.addEventListener(theInLisner); // didn't work, so do my own
            FrootLoop = new EatCereal();     // (does not start it..)
            if (FrootLoop != null) {
                bitz = 12;
                EatOften = new Timer(0, FrootLoop);
                if (EatOften == null) bitz = 28;
            }
            if (digitalInputData == null) {
                digitalInputData = new int[MAX_DATA_BYTES];
                analogInputData = new int[MAX_DATA_BYTES];
            }
            if (ArduPiModes == null) {
                bitz = bitz + 2;
                ArduPiModes = new int[16];
            }
            for (nx = 0; nx <= 15; nx++) ArduPiModes[nx] = 0;
            if (AutoStartInputs) {
                bitz++;
                Thread.sleep(1000); // delay 8 seconds for Arduino reboot
                for (nx = 0; nx <= 2; nx++)
                    OpenPinInput(nx, Arduino.REPORT_ANALOG); // = 0xC0 (192)
                for (nx = 0; nx <= 1; nx++)
                    OpenPinInput(nx, Arduino.REPORT_DIGITAL);
            } // = 0xD0 (208)
        } catch (Exception ex) {
            System.out.println(ex);
            System.exit(-1);
        }
        if (SpeakEasy) System.out.println("   (Ardu/Op) => GoodOpen = " + GoodOpen
                + " (" + bitz + ")");
    } //~Open

    /**
     * Closes the serial port.
     * <p>
     * Note that JSSC may not recover gracefully from a failure to close,
     * so a subsequent Open() may fail until the system is rebooted.
     */
    public void Close() {                                       // in {FF} Arduino
        if (SpeakEasy) System.out.println("F%%F/Close.."); // SpeakEasy=true
        if (GoodOpen) try {
            surrealPort.closePort();
            DoMore = null;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        GoodOpen = false;
    } //~Close

    public Arduino() { // outer class constructor..
        DateFormat sdf = new SimpleDateFormat("yy MMM dd, HH:mm:ss");
        Date now = new Date();
        surrealPort = new SerialPortSwapper(CommPortNo);
        if (SpeakEasy) System.out.println("new Arduino " // time-stamp the log..
                + CommPortNo + " " + (surrealPort != null) + " -- " + sdf.format(now));
        digitalOutputData = new int[MAX_DATA_BYTES];
        digitalInputData = new int[MAX_DATA_BYTES];
        analogInputData = new int[MAX_DATA_BYTES];
        Open();
    }
} //~Arduino (fakefirm) (FF)
