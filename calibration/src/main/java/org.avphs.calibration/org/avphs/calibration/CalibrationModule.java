package org.avphs.calibration;

import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;

public class CalibrationModule {

    //input current speed and desired speed. get distance
    private static final byte[][][] SPEED_CHANGE_DISTS = {};

    //input floor type, radius of turn, get max velocity
    private static final byte[][] MAX_SPEEDS = {};

    //input x and y
    private static final FishData[][] DEFISHER = {};

    public static final FishData getFishData(short x, short y){
        return DEFISHER[x][y];
    }
    public static final byte getMaxSpeed(byte floor, byte rad){
        return MAX_SPEEDS[floor][rad];
    }
    public static final byte getSpeedChangeDist(byte floor, byte initSpeed, byte finalSpeed){
        return SPEED_CHANGE_DISTS[floor][initSpeed][finalSpeed];
    }
/*
    static class pulseListener implements UpdateListener{ //adds listener for pulse
        int prior; //previous pulse read

        /**
         * Updates selected pin
         * @param pin chosen pin
         * @param value value to be entered
         */
       /* void pinUpdated(int pin, int value){ //updates pin
            boolean doit;
            if(pin==8){ //if pulse is sent to pin 8
                if(value+prior>0) {
                    SystemDebugLog("Pulse count = " + value);
                    prior = value;
                }//~if
            }//~pinif
        }//~pinUpdated
        public init_pulseListener() { //constructs a pulselistener
            prior = 0;//set prior to 0;
            SystemDebugLog("new pulseListener")
        }
    }//~pulseListener

    /**
     * Gets the distance travelled
     *
     * @return int distance (meters)
     */
    /*public float getDistance_Elapsed(pulseListener pulser){
        float dist = 0;
        if(pulser.prior != 0){ //if nonzero
            dist = pulser.prior*MetersPerTerm;
        }//~if
        return dist;
    }//~getDistance_Elapsed

    /**
     * Gets ping
     *
     * @return ping ping(ns)
     */
    /*public float getNetwork_Latency(){
        long beginning = System.nanoTime();
        long ping = System.nanoTime() - beginning;
        return ping;
    }//~getNetwork_Latency*/
}