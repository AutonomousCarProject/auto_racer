/* SerialPortEvent -- a substitute class with the same API but does nothing.
 *
 * FakeFirmata is designed to work with JSSC (Java Simple Serial Connector),
 * but when using TrakSim, all you need are stubs with the same API.
 */
package org.avphs.util.nojssc;                                 // 2019 April 18


/**
 * This class has the same name & API as JSSC but does nothing.
 */
public class FakeSerialPortEvent { // used by FakeFirmata/ProcessInput
  static final boolean logo = false; // enable logging..
  // static final boolean logo = true; // (to omit 'import apw3.DriverCons')

  public static final int RXCHAR = 1, RXFLAG = 2;

 /**
  * Get Event Type. There are no events, so this method returns 0.
  *
  * @return  0
  */
  public int getEventType() {return 0;} //~getEventType

  public FakeSerialPortEvent() { // constructors..
    if (logo) System.out.println("noJSC/new SerPrtEvt");}
  public FakeSerialPortEvent(String myPortName, int mask, int nby) {
    if (logo) System.out.println("noJSC/new SerPrtEvt '" + myPortName + "' = "
        + mask + "," + mask);}} //~SerialPortEvent (noJSSC) (NV)
