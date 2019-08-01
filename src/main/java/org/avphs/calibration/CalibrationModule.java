package org.avphs.calibration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;


import static java.lang.Short.parseShort;

public class CalibrationModule {
    private static boolean testMode = true;

    public static void main(String[] args){
        if (testMode){
            System.out.println("test start");
            System.out.println("getFishData: " + getFishData((short)0,(short)0));
            System.out.println("getMaxSpeed: " + getMaxSpeed((byte)0,(short)0));
            System.out.println("getSpeedChangeDist: " + getSpeedChangeDist((byte)0,(byte)0,(byte)0));
            System.out.println("getAngles: " + getAngles((short)0));
            System.out.println("getRadii: " + getRadii((short)0));
            System.out.println("getThrottle: " + getThrottle((short)0,(byte)0));
            System.out.println("getDist: " + getDist((short)0));
        }
    }


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
    public static final int SERVO_ANGLE_MIN = -33;
    public static final int SERVO_ANGLE_MAX = 44;
    //Camera view angle width in degrees
    //TODO:Find real value
    public static final float CAMERA_VIEW_ANGLE = 100;

    //Axel dist in cm
    public static final double WHEEL_BASE = 32.5;

    public static final double  RIM_TO_RIM = 26.4;

    public static final double  SUSPENSION_HEIGHT = 1;

    public static final double  CM_PER_ROTATION = 15; //approximation (from DriverCons)

    private static final int MIN_DELTA_SPEED = 5;

    //turn the car to this angle to make it go in a straight line
    public static final int STRAIGHT_ANGLE = 7;

    //Helper method to read speed change distance data
    private static byte[][][] readSpeedChangeDistData (){
        byte[][][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\DistanceCalculations.txt"))) {

            short numFloors = parseShort(br.readLine());
            short initSpeeds = parseShort(br.readLine());
            short finalSpeeds = parseShort(br.readLine());
            rowList = new byte[numFloors][initSpeeds][finalSpeeds];

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
    private static byte[][] readMaxSpeedData (){
        byte[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\MaxSpeeds.txt"))) {

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
    private static FishData[][] readFishData() {

        FishData[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\CameraData.txt"))) {


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
    private static HashMap<Integer, Integer> readAngleData (){
        HashMap<Integer, Integer> hashMap = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\AngleData.txt"))) {

            short radCount = parseShort(br.readLine());
            hashMap = new HashMap<>();
            for (int i = 0; i < radCount; i++) {
                String[] row = br.readLine().split(" ");
                int key = Integer.parseInt(row[0]);
                int val = Integer.parseInt(row[1]);
                hashMap.put(key, val);
            }

        }
        catch(Exception e){
            // Handle any I/O problems

        }
        return hashMap;
    }

    //Helper method to read radii data
    private static short[] readRadiiData (){
        //TODO: FIX
        short[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\RadiiData.txt"))) {

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

    //angle, desired velocity
    private static byte[][] readThrottleData(){
        byte[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\ThrottleCalculations.txt"))) {

            short radCount = parseShort(br.readLine());
            short desiredSpeedsCount = parseShort(br.readLine());
            rowList = new byte[radCount][desiredSpeedsCount];

                for(int j = 0; j < radCount; j++) {

                    String line = br.readLine();
                    String[] lineItems = line.split(" ");
                    for (int k = 0; k < desiredSpeedsCount; k++) {
                        rowList[j][k] = Byte.parseByte(lineItems[k]);
                    }
                }


        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //Helper method to read radii data
    private static float[] readPixelData (){
        float[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src\\main\\java\\org\\avphs\\calibration\\PixelData.txt"))) {

            short angleCount = parseShort(br.readLine());
            rowList = new float[angleCount];
            String line = br.readLine();
            String[] lineItems = line.split(" ");
            for (int i = 0; i < angleCount; i++) {

                rowList[i] = Float.parseFloat(lineItems[i]);
            }

        }
        catch(Exception e){
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
    private static final HashMap<Integer, Integer> ANGLES = readAngleData();

    //input angle
    private static final short[] RADII = readRadiiData();

    //
    private static final byte[][] THROTTLES = readThrottleData();

    private static final float[] PIXEL_DISTS = readPixelData();

    public static final FishData getFishData(short x, short y) {
        return DEFISHER[x][y];
    }

    public static final byte getMaxSpeed(byte floor, short rad) {
        return 1;
        //return MAX_SPEEDS[floor][rad];

    }

    public static final byte getSpeedChangeDist(byte floor, byte initSpeed, byte finalSpeed) {
        byte i = (byte) (initSpeed/MIN_DELTA_SPEED);
        byte f = (byte) (finalSpeed/MIN_DELTA_SPEED);
        return SPEED_CHANGE_DISTS[floor][i][f];
    }

    public static final int getAngles(int rad) {
        return ANGLES.get(rad);
    }

    public static final short getRadii(short angle) {
        return RADII[angle - SERVO_ANGLE_MIN];
    }

    //returns the amount of throttle needed to maintain a given speed on a given floor surface and with a given turn radius
    //0 = go full throttle
    public static final byte getThrottle (short radius, byte speed){
        return THROTTLES[radius][speed];
    }

    public static final float getDist(short pixelHeight){
        return PIXEL_DISTS[pixelHeight];
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