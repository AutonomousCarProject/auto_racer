/* SerialPort -- a substitute class with the same API but does nothing.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but when using TrakSim, all you need are stubs with the same API.
 */
package org.avphs.util.nojssc;                                 // 2019 April 18

import org.avphs.util.PortObject;

/**
 * This class has the same name & API as JSSC but does nothing.
 */
public class FakeSerialPort implements PortObject {
  static final boolean logo = false; // enable logging..
  // static final boolean logo = true; // (to omit 'import apw3.DriverCons')

  public static final int MASK_RXCHAR = 1, MASK_RXFLAG = 2;

 /**
  * Port opening
  *
  * @return   true
  */
  public boolean openPort() {
    if (logo) System.out.println("noJSC/openPort");
    return true;}

 /**
  * Setting the parameters of port.
  *
  * @param  baudRate data transfer rate
  * @param  dataBits number of data bits
  * @param  stopBits number of stop bits
  * @param  parity parity
  *
  * @return   true
  */
  public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) {
    if (logo) System.out.println("noJSC/setParams " + baudRate + " " + dataBits
         + " " + stopBits + " " + parity);
    return true;} //~setParams

 /**
  * Write byte array to port
  *
  * @param  buffer the byte array to write
  *
  * @return  true
  */
  public boolean writeBytes(byte[] buffer) {
    if (buffer==null) return false;
    if (logo) System.out.println("noJSC/writeBytes " + buffer[0]);
    return true;} //~writeBytes

 /**
  * Read byte array from port, but there is no port so it returns null
  *
  * @param  byteCount  how many bytes to read
  *
  * @return  (the byte array as read) null
  */
  public byte[] readBytes(int byteCount) {return null;} //~readBytes

 /**
  * Return the number of input bytes waiting, always 0
  *
  * @return  0
  */
  public int getInputBufferBytesCount() {return 0;}

 /**
  * Close port. This method pretends to close the port
  *
  * @return  true
  */
  public boolean closePort() {
    if (logo) System.out.println("noJSC/closePort");
    return true;} //~closePort

  public FakeSerialPort() {if (logo) System.out.println("noJSC/new SerialPort");}
  public FakeSerialPort(String myPortName) { // constructor..
    if (logo) System.out.println("noJSC/new SerialPort '"
        + myPortName + "'");}} //~SerialPort (nojssc) (NS)
