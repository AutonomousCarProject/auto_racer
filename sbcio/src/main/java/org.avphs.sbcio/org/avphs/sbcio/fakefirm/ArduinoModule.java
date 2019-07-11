/**
 * A module to make the ArduinoIO interface work with MrModule's file system
 * 
 * @author Colton Jelsema
 */

package org.avphs.sbcio.fakefirm;

import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;
import org.avphs.sbcio.IOSettings;
import org.avphs.sbcio.PWMController;

public class ArduinoModule implements CarModule {
    private PWMController driveSys;

    /**
     * Checks buffer input with arduino and sends null info to prevent timeout
     * 
     * @param control unused, may be put to use with digitalRead
     */
	
	/**
	 * Closes the port properly to assure future open calls work
	 */
	public void Close(){
		driveSys.close();
	}


	@Override
	public Class[] getDependencies() {
		return new Class[0];
	}

	@Override
	public void init(CarModule[] dependencies) {
		driveSys.write(ArduinoIO.FORCE_START, 0, 0);
	}

	@Override
	public CarCommandType[] commands() {
		return new CarCommandType[0];
	}

	@Override
	public void run() {
		if (IOSettings.readMessages)
			driveSys.digitalRead();

		driveSys.write(0, 0, 0);
	}
}
