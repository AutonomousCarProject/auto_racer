package org.avphs.calibration;

import java.io.BufferedReader;
import java.io.FileReader;

import static java.lang.Short.parseShort;

public class CalibrationModule {

    /*private static <TOUT> TOUT readTable(String filepath){
        TOUT rowList = null;

        Object dimensionObj = rowList;
        int dimensionCount = 0;
        while(dimensionObj instanceof Object[]){
            var array = (Object[])dimensionObj;
            dimensionCount++;
            dimensionObj = array[0];
        }
        Function<Integer, Integer> x = (y)->y+5;
        x.
        return rowList;
    }
    */

    //Camera view angle width in degrees
    //TODO:Find real value
    public static final float CAMERA_VIEW_ANGLE = 100;

    //TODO: find real value
    //Axel dist in cm
    public static final double WHEEL_BASE = 32.5;

    public static final double  RIM_TO_RIM = 26.4;

    //Helper method to read speed change distance data
    private static byte[][][] readSpeedChangeDistData (){
        byte[][][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\DistanceCalculations.txt"))) {

            short numFloors = parseShort(br.readLine());
            short initSpeeds = parseShort(br.readLine());
            short finalSpeeds = parseShort(br.readLine());
            rowList = new byte[numFloors][initSpeeds][finalSpeeds];

            for(short i = 0; i<numFloors; i++){
                for(int j = 0; j < initSpeeds; j++) {

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
    private static byte[][] readMaxSpeedData (){
        byte[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\MaxSpeeds.txt"))) {

            short numFloor = parseShort(br.readLine());
            short numRadii = parseShort(br.readLine());
            rowList = new byte[numFloor][numRadii];

            for (int i = 0; i < numFloor; i++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");
                for (int j = 0; j < numRadii; j++) {
                    rowList[i][j] = Byte.parseByte(lineItems[j]);
                }
            }


        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //Helper method to read defishing data
    private static FishData[][] readFishData() {

        FishData[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\CameraData.txt"))) {


            short xCount = parseShort(br.readLine());
            short yCount = parseShort(br.readLine());
            rowList = new FishData[xCount][yCount];

            for (int i = 0; i < xCount; i++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");
                for (int j = 0; j < yCount; j += 2) {
                    float deg = Float.parseFloat(lineItems[j]);
                    float dist = Float.parseFloat(lineItems[j + 1]);
                    rowList[i][j] = new FishData(deg, dist);
                }
            }

        } catch (Exception e) {

        }
        return rowList;
    }

    //Helper method to read angle data
    private static short[] readAngleData (){
        short[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\AngleData.txt"))) {

            short radCount = parseShort(br.readLine());
            rowList = new short[radCount];
            String line = br.readLine();
            String[] lineItems = line.split(" ");
            for (int i = 0; i < radCount; i++) {

                rowList[i] = Short.parseShort(lineItems[i]);
            }

        }
        catch(Exception e){
            // Handle any I/O problems

        }
        return rowList;
    }

    //Helper method to read radii data
    private static short[] readRadiiData (){
        //TODO: FIX
        short[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\RadiiData.txt"))) {

            short angleCount = parseShort(br.readLine());
            rowList = new short[angleCount];
            String line = br.readLine();
            String[] lineItems = line.split(" ");
            for (int i = 0; i < angleCount; i++) {

                rowList[i] = Short.parseShort(lineItems[i]);
            }

        }
        catch(Exception e){
            // Handle any I/O problems

        }
        return rowList;
    }

    private static byte[][][] readThrottleData(){
        byte[][][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("calibration\\src\\main\\java\\org.avphs.calibration\\org\\avphs\\calibration\\ThrottleCalculations.txt"))) {

            short numFloors = parseShort(br.readLine());
            short radCount = parseShort(br.readLine());
            short desiredSpeedsCount = parseShort(br.readLine());
            rowList = new byte[numFloors][radCount][desiredSpeedsCount];

            for(short i = 0; i<numFloors; i++){
                for(int j = 0; j < radCount; j++) {

                    String line = br.readLine();
                    String[] lineItems = line.split(" ");
                    for (int k = 0; k < desiredSpeedsCount; k++) {
                        rowList[i][j][k] = Byte.parseByte(lineItems[k]);
                    }
                }
            }

        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //input current speed and desired speed. get distance
    private static final byte[][][] SPEED_CHANGE_DISTS = readSpeedChangeDistData();

    //input floor type, radius of turn, get max velocity
    private static final byte[][] MAX_SPEEDS = readMaxSpeedData();

    //input x and y
    private static final FishData[][] DEFISHER = readFishData();

    //input radius
    private static final short[] ANGLES = readAngleData();

    //input angle
    private static final short[] RADII = readRadiiData();

    //
    private static final byte[][][] THROTTLES = readThrottleData();



    public static final FishData getFishData(short x, short y) {
        return DEFISHER[x][y];
    }

    //0 rad is straight
    public static final byte getMaxSpeed(byte floor, short rad){
        return MAX_SPEEDS[floor][rad];

    }
    public static final byte getSpeedChangeDist(short floor, byte initSpeed, byte finalSpeed){
        return SPEED_CHANGE_DISTS[floor][initSpeed][finalSpeed];
    }

    //returns val btwn 0 and 180--90 is straight ahead 180 is sharp right, 0 is sharp left
    public static final short getAngle (short rad){
        return ANGLES[rad];
    }

    public static final short getRadius (short deg){
        return RADII[deg];
    }

    //returns the amount of throttle needed to maintain a given speed on a given floor surface and with a given turn radius
    //0 = go full throttle
    public static final byte getThrottle (byte floor, short radius, byte speed){
        return THROTTLES[floor][radius][speed];
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
