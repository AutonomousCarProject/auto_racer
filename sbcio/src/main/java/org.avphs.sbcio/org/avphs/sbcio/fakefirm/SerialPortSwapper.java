package org.avphs.sbcio.fakefirm;

import jssc.SerialPort;
import jssc.SerialPortException;
import org.avphs.sbcio.IOSettings;

public class SerialPortSwapper {

    private SerialPort serialPort;

    public SerialPortSwapper(String portName) {
        serialPort = new SerialPort(portName);
    }

    public boolean openPort() throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.openPort();
        }
        return true;
    }

    public boolean closePort() throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.closePort();
        }
        return true;
    }

    public boolean writeBytes(byte[] buffer) throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.writeBytes(buffer);
        }
        return true;
    }

    public byte[] readBytes(int byteCount) throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.readBytes(byteCount);
        }
        return null;
    }

    public int getInputBufferBytesCount() throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.getInputBufferBytesCount();
        }
        return 0;
    }

    public boolean setParams(int baudRate, int dataBits, int stopBits, int parity) throws SerialPortException {
        if (IOSettings.useServos) {
            return serialPort.setParams(baudRate, dataBits, stopBits, parity);
        } else {
            return true;
        }
    }

}
