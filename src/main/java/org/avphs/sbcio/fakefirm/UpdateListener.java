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
 */
package org.avphs.sbcio.fakefirm; // (interface UpdateListener)   // 2018 December 18

/**
 * This is the interface for specifying listeners to the Arduino output.
 */
public interface UpdateListener { public abstract

 /**
  * Override this method to see the data being sent from the Arduino.
  *
  * @param  pin     The "pin" or message code that generated this message.
  *
  * @param  value   The 14-bit value being sent from HardAta
  */
  void pinUpdated(int pin, int value) ;}
