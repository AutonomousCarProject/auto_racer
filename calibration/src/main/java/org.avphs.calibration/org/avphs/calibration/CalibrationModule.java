package org.avphs.calibration;

import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;

public class CalibrationModule {

    //Helper method to read speed change distance data
    private static byte[][][] reedSpeedChangeDistData (){
        byte[][][] data = new byte[][][]{};
        return data;
    }

    //Helper method to read max speed data
    private static byte[][] reedMaxSpeedData (){
        byte[][] data = new byte[][]{};
        return data;
    }

    //Helper method to read defishing data
    private static FishData[][] reedFishData (){
        FishData[][] data = new FishData[][]{};
        return data;
    }
    //input current speed and desired speed. get distance
    private static final byte[][][] SPEED_CHANGE_DISTS = reedSpeedChangeDistData();

    //input floor type, radius of turn, get max velocity
    private static final byte[][] MAX_SPEEDS = reedMaxSpeedData();

    //input x and y
    private static final FishData[][] DEFISHER = reedFishData();

    //input x and y of pixels and outputs defisher data
    public static final FishData getFishData(short x, short y){
        return DEFISHER[x][y];
    }

    //input the type of floor and the radius of the turn (cm) an output the max speed
    public static final byte getMaxSpeed(byte floor, byte rad){
        return MAX_SPEEDS[floor][rad];
    }

    //input the type of floor, initial speed (cm/s), and final speed (cm/s) wich is SMALLER than initial speed.
    // Output the distance (cm) it will take to get there
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