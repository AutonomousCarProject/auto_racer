/**
 * FakeFirmata -- a simple way to control servos from LattePanda in Java.
 * <p>
 * This is essentially a translation of (small parts of) LattePanda's Arduino.cs
 * into Java for using the attached Arduino to control servos.
 * <p>
 * Under US Copyright law this miniscule copy counts as "Fair Use" and in the
 * public domain, but if you are worried about it, or if you extend it to
 * include more of their code, then you will be bound by the onerous rules of
 * the GNU General Public License or whatever they are currently using.
 * <p>
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but probably will work with any compatible Java serial port API.
 * <p>
 * This version has been edited to work specifically with the associated Arduino program.
 * The changes are to the protocol and the used functions,
 * mainly removal of unused functions and variables.
 * Current protocol doesn't have a catch for missed bytes.
 *
 * @author Firmata
 * @author Tom
 * @author Colton Jelsema
 */
package org.avphs.util;
 // use this instead for working with TrackSim
//                           // ..on a computer with no serial port.
//import com.apw.Interfacing.SerialPort;

import org.avphs.util.nojssc.FakeSerialPort;
import org.avphs.util.jssc.SerialPortException;

public class ArduinoIO implements PWMController { // Adapted to Java from arduino.cs ... (FakeFirmata)
    // (subclass this to add input capability)

    public static final boolean UseServos = false;

    public static final String CommPortNo = "COM3";
    public static final int MAX_DATA_BYTES = 16, // =64 in LattePanda's Arduino.cs
            MDB_msk = MAX_DATA_BYTES - 1;
    public static final int StartUpTimeMS = (int) System.currentTimeMillis();
    //Message types
    public static final int ANALOG_MESSAGE = 0xE0;
    public static final int FORCE_START = 0xFF;
    public static final double RotDistConversion = 1; //rotation to distance ratio
    protected static final boolean SpeakEasy = true;
    protected static boolean PortOpened = false;
    protected static SimHookBase HookFunctions = null; // for extensions
    protected int[] digitalOutputData;
    protected PortObject surrealPort;
    private int readSpeed;
    private int readAngle;
    private long rotations;


    public ArduinoIO(Boolean useServos) { // outer class constructor..
        surrealPort = useServos ? new FakeSerialPort(CommPortNo) : new SerialPortDump(CommPortNo);
        System.out.println("new Arduino " + CommPortNo + " " + (surrealPort != null));
        digitalOutputData = new int[MAX_DATA_BYTES];
        Open();
    }
    // Implicit override

    /**
     * Use this to link simulator actions to Firmata.
     *
     * @param whom An instance of a subclass of SimHookBase
     */
    public static void HookExtend(SimHookBase whom) {
        HookFunctions = whom;
    }

    public static int GetMills() {
        return (int) System.currentTimeMillis() - StartUpTimeMS;
    } //~GetMills

    /**
     * Convenient (millisecond) time formatter.
     *
     * @param  prefix  String added to front
     * @param  now     The relative time to format, in (int) milliseconds
     *
     * @return Formatted String prefix+hh:mm:ss.mmm
     */
    public static String FormatMillis(String prefix, int now) {
        int whom = 0;
        boolean more = false;
        if (now == 0x80000000) now = GetMills();
        if (now < 0) return FormatMillis(prefix + "-", -now);
        if (now >= 3600000) { // convert to hours..
            whom = now / 3600000;
            now = now - whom * 3600000;
            prefix = prefix + whom + ":";
            more = true;
        }
        if (now >= 60000) { // convert to minutes..
            if (more) if (now < 600000) prefix = prefix + "0";
            whom = now / 60000;
            now = now - whom * 60000;
            prefix = prefix + whom + ":";
            more = true;
        } else if (more) prefix = prefix + whom + "00:";
        if (more) if (now < 10000) prefix = prefix + "0";
        whom = now / 1000;
        now = now - whom * 1000;
        prefix = prefix + whom + ".";
        if (now < 10) prefix = prefix + "00";
        else if (now < 100) prefix = prefix + "0";
        return prefix + now;
    } //~FormatMillis

    public int getSpeed() {
        return readSpeed;
    }

    public int getAngle() {
        return readAngle;
    }

    /**
     * Current status of the FakeFirmata library, =true if successfully open.
     *
     * @return true if successfully open, false if failed or closed
     */
    public boolean IsOpen() {
        return PortOpened;
    } // true if opened successfully

    /**
     * Sets the mode of the specified pin (INPUT or OUTPUT).
     * Unused in modified version, pin numbers and modes are set arduino side.
     *
     * @param pin  The arduino pin.
     * @param mode Mode Arduino.OUTPUT or Arduino.SERVO
     *             (Arduino.INPUT, Arduino.ANALOG or Arduino.PWM not supported)
     */
    @Deprecated
    public void pinMode(int pin, byte mode) {
        byte[] msg = new byte[3];
        //if (SpeakEasy) System.out.println("F%%F/pinMode +" + pin + " = " + mode);
        msg[0] = (byte) 0xf;
        msg[1] = (byte) pin;
        msg[2] = mode;
        try {
            surrealPort.writeBytes(msg);
            if (HookFunctions != null) HookFunctions.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~pinMode

    /**
     * Reads any information from the buffer in the connection
     * with the arduino.
     * Currently prints into the console.
     *
     * @return void, may be subject to change.
     */
    public void digitalRead() {
        try {
            while (this.surrealPort.getInputBufferBytesCount() >= 3) {
                byte[] msg = surrealPort.readBytes(3);

                if (msg[0] == 100) {
                    System.out.println("----ARDUINO DEBUG");
                    System.out.print((int) msg[1]);
                    System.out.println((int) msg[2]);
                } else if (msg[0] >= 110) { //A maximum of 126 to be safe
                    System.out.println("----ARDUINO INFO -type: " + msg[0]);
                    System.out.println(msg[1]);
                    System.out.println(msg[2]);

                    System.out.println((msg[2] << 8) + (msg[1]));

                    if (msg[0] == 112) {
                        this.rotations += ((msg[2] << 8) + (msg[1]));
                        this.readSpeed = ((msg[2] << 8) + (msg[1]));
                        System.out.println("total rotations: " + this.rotations);
                    }
                } else {
                    System.out.println("----Arduino gave unexpected info:" + msg[0]);
                }

            }
        } catch (SerialPortException e) {
            e.printStackTrace();
            System.out.println("Arduino reading is creating a problem! is useServos set correctly?");
        }
    }

    /**
     * [For] controlling [a] servo.
     * Limit input to numbers castable to 1 byte.
     *
     * @param pin Servo output pin. Port 9 for steering
     * @param angle Set angle. Ranges from 0 to 180 where 90 is center.
     */
    public void setServoAngle(int pin, int angle) {
        byte[] msg = new byte[3];
        msg[0] = (byte) (ANALOG_MESSAGE); //Type of message. Likely unneeded
        msg[1] = (byte) (pin); //pin
        msg[2] = (byte) (angle); //angle
        try {
            surrealPort.writeBytes(msg);
            if (HookFunctions != null) HookFunctions.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~servoWrite

    /**
     * Writes to the buffer with all three bytes as inputs.
     *
     * @param messageType Purpose of message, currently unused.
     * @param pin second byte to be sent, commonly used to indicate pin
     * @param angle third byte to be send, commonly used for angle
     */
    public void Write(int messageType, int pin, int angle) {
        byte[] msg = new byte[3];
        msg[0] = (byte) (messageType); //Type of message. Likely unneeded
        msg[1] = (byte) (pin); //pin
        msg[2] = (byte) (angle); //angle
        try {
            surrealPort.writeBytes(msg);
            if (HookFunctions != null) HookFunctions.SendBytes(msg, 3);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    } //~servoWrite

    /**
     * Allows direct setting of the pulse width modulation,
     * a redundancy with the arduino protocol.
     */
    @Override
    @Deprecated
    public void setOutputPulseWidth(int pin, double ms) {
        setServoAngle(pin, (int) ((ms - 1) * 180));
    }

    /**
     * Opens the serial port connection, should it be required.
     * By default the port is opened when the object is first created.
     * <p>
     * Note that JSSC does not recover gracefully from a failure to close,
     * so a subsequent Open() will fail until the system is rebooted.
     */
    public void Open() {
        if (SpeakEasy) System.out.println("F%%F/Open..");
        if (PortOpened) {
            if (SpeakEasy) System.out.println("... " + CommPortNo + " is already open");
            return;
        } else try {
            PortOpened = surrealPort.openPort();
            surrealPort.setParams(57600, 8, 1, 0);
            this.Write(0xFF, 0, 0); //Send startup message
        } catch (Exception ex) {
            System.out.println(ex);
        }
        if (SpeakEasy) {
            if (PortOpened) System.out.println("... " + CommPortNo + " is now open");
            else System.out.println("... " + CommPortNo + " failed to open");
        }
    } //~Open

    /**
     * Closes the serial port.
     * <p>
     * Note that JSSC does not recover gracefully from a failure to close,
     * so a subsequent Open() will fail until the system is rebooted.
     */
    public void close() {
        if (SpeakEasy) System.out.println("F%%F/Close..");
        if (PortOpened) try {
            surrealPort.closePort();
            HookFunctions = null;
        } catch (Exception ex) {
            System.out.println(ex);
        }
        PortOpened = false;
    } //~Close
} //~Arduino (fakefirm) (OO)
