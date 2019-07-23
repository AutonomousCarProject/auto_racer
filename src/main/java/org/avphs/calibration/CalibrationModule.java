package org.avphs.calibration;

import java.io.BufferedReader;
import java.io.FileReader;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;

public class CalibrationModule {

    //Helper method to read speed change distance data
    private static byte[][][] reedSpeedChangeDistData() {
        byte[][][] rowList = new byte[][][]{};
        try (BufferedReader br = new BufferedReader(new FileReader("pathtocsvfile.csv"))) {

            short numFloors = Short.parseShort(br.readLine());
            short initSpeeds = Short.parseShort(br.readLine());
            short finalSpeeds = Short.parseShort(br.readLine());
            for (short i = 0; i < numFloors; i++) {
                for (int j = 0; j < initSpeeds; j++) {

                    String line = br.readLine();
                    String[] lineItems = line.split(" ");
                    for (int k = 0; k < finalSpeeds; k++) {
                        rowList[i][j][k] = Byte.parseByte(lineItems[k]);
                    }
                }
            }

        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //Helper method to read max speed data
    private static byte[][] reedMaxSpeedData() {
        byte[][] rowList = new byte[][]{};
        try (BufferedReader br = new BufferedReader(new FileReader("pathtocsvfile.csv"))) {

            short numFloors = Short.parseShort(br.readLine());
            short initSpeeds = Short.parseShort(br.readLine());
            short finalSpeeds = Short.parseShort(br.readLine());

            for (int i = 0; i < initSpeeds; i++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");
                for (int j = 0; j < finalSpeeds; j++) {
                    rowList[i][j] = Byte.parseByte(lineItems[j]);
                }
            }


        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //Helper method to read defishing data
    private static FishData[][] reedFishData() {
        FishData[][] rowList = new FishData[][]{};
        try (BufferedReader br = new BufferedReader(new FileReader("pathtocsvfile.csv"))) {

            short numFloors = Short.parseShort(br.readLine());
            short initSpeeds = Short.parseShort(br.readLine());
            short finalSpeeds = Short.parseShort(br.readLine());

            for (int i = 0; i < initSpeeds; i++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");
                for (int j = 0; j < finalSpeeds; j += 2) {
                    float deg = Float.parseFloat(lineItems[j]);
                    float error = Float.parseFloat(lineItems[j + 1]);
                    rowList[i][j] = new FishData(deg, error);
                }
            }

        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //input current speed and desired speed. get distance
    private static final byte[][][] SPEED_CHANGE_DISTS = reedSpeedChangeDistData();

    //input floor type, radius of turn, get max velocity
    private static final byte[][] MAX_SPEEDS = reedMaxSpeedData();

    //input x and y
    private static final FishData[][] DEFISHER = reedFishData();

    //
    private static final short[] ANGLES = {};

    public static final FishData getFishData(short x, short y) {
        return DEFISHER[x][y];
    }

    public static final byte getMaxSpeed(byte floor, short rad) {
        return MAX_SPEEDS[floor][rad];

    }

    public static final byte getSpeedChangeDist(byte floor, byte initSpeed, byte finalSpeed) {
        return SPEED_CHANGE_DISTS[floor][initSpeed][finalSpeed];
    }

    public static final short getAngles(short rad) {
        return ANGLES[rad];
    }

/*
    static class pulseListener implements UpdateListener{ //adds listener for pulse
        int prior; //previous pulse read

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
