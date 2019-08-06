package org.avphs.calibration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

import static java.lang.Short.parseShort;

public class CalibrationModule {
   /* public static void main(String[] args){
            System.out.println("test start");
            System.out.println("getFishData: " + getFishData((short)0,(short)0));
            System.out.println("getMaxSpeed: " + getMaxSpeed((byte)0,(short)0));
            System.out.println("getSpeedChangeDist: " + getSpeedChangeDist((byte)0,(byte)0,(byte)0));
            System.out.println("getAngles: " + getAngles((short)0));
            System.out.println("getRadii: " + getRadii((short)0));
            System.out.println("getThrottle: " + getThrottle((short)0,(byte)0));
            System.out.println("getDist: " + getDist((short)0));
    }

    */


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
    public static final int SERVO_ANGLE_MIN = -33; //minimum servo angle for steering (left angle turn)
    public static final int SERVO_ANGLE_MAX = 44; //maximum servo angle for steering (right angle turn)
    //Camera view angle width in degrees
    //TODO:Find real value
    public static final float CAMERA_VIEW_ANGLE = 100; //viewing angle of the camera

    //Axel dist in cm
    public static final double WHEEL_BASE = 32.5; //distance between front axle and back axle

    public static final double RIM_TO_RIM = 26.4;

    public static final double SUSPENSION_HEIGHT = 1;

    public static final double CM_PER_ROTATION = 15; //approximation (from DriverCons)

    private static final int MIN_DELTA_SPEED = 5;

    //turn the car to this angle to make it go in a straight line
    public static final int STRAIGHT_ANGLE = 2; //2 for orange 7 for green

    private static final float[] turnRadiiForAngles = new float[]{170.1f, 170.2f, 170.292f, 171.99f, 175f, 175.532f, 177.38f, 179.283f, 181.242f, 185f, 185.342f, 187.489f, 189.704f, 191.99f, 194.352f, 196.6f, 199.396f, 201.927f, 204.629f, 206.6f, 210.33f, 213.34f, 216.463f, 219.707f, 224f, 226.585f, 230.236f, 234.039f, 238.005f, 241.4f, 246.466f, 250.987f, 255.718f, 260.676f, 265.877f, 271.338f, 277.081f, 283.128f, 289.503f, 295.3f, 303.351f, 310.889f, 318.887f, 327.386f, 336.437f, 346.095f, 356.423f, 367.493f, 374.99f, 375.5f, 406.053f, 421.064f, 437.389f, 455.211f, 474.743f, 496.245f, 520.031f, 546.484f, 576.081f, 620f, 647.248f, 690.55f, 740.601f, 799.113f, 868.429f, 951.846f, 1054.154f, 1182.59f, 1348.628f, 1571f, 1886.905f, 2366.784f, 3185.64f, 4898.768f, 10733.292f, 11000f, 12000f, 13000f, 14000f, 9000f, 8000f, 7000f, 6000f, 5808.404f, 3940.952f, 2505.715f, 1960.406f, 1614.147f, 1375f, 1199.411f, 1065.412f, 959.686f, 874.138f, 803.49f, 744.174f, 693.655f, 650.114f, 612.199f, 576f, 549.38f, 523.07f, 499.467f, 478.17f, 458.847f, 441.247f, 425.147f, 410.36f, 396.737f, 387f, 372.463f, 361.604f, 351.4826f, 342.0249f, 333.17f, 324.86f, 317.04f, 309.68f, 302.73f, 295f, 289.95f, 284.06f, 278.466f, 273.149f, 268.088f, 263.265f, 258.66f, 254.27f, 253.5f, 253f, 242.12f, 238.51f, 234.96f, 231.56f, 230f, 225.147f, 222.12f, 219.2f, 216.39f, 213.4f, 211.06f, 208.53f, 206.09f, 203.7f, 197.8f, 197.7f, 197.087f, 195f, 193f, 189f, 189.15f, 187.31f, 185.527f, 183.79f, 181.3f, 180.46f, 178.869f, 176.4f}; //-77 <= x <= 80; x != 2


    //Helper method to read speed change distance data
    private static short[][][] readSpeedChangeDistData() {
        short[][][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/DistanceCalculations.txt"))) {

            short numFloors = parseShort(br.readLine()); //read the array lengths at the top of the file marked there ^
            short initSpeeds = parseShort(br.readLine()); //these numbers give the array length
            short finalSpeeds = parseShort(br.readLine());
            rowList = new short[numFloors][initSpeeds][finalSpeeds];


            for (short i = 0; i < numFloors; i++) {
                for (int j = 0; j < initSpeeds; j++) {

                    String line = br.readLine();
                    String[] lineItems = line.split(" ");
                    for (int k = 0; k < finalSpeeds; k++) {
                        rowList[i][j][k] = Short.parseShort(lineItems[k]); //read through the rows, THIS IS THE SAME FOR EVERY READ FILE

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowList;
    }

    //Helper method to read max speed data
    private static short[][] readMaxSpeedData() {
        short[][] rowList = null;//same as the other read files
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/MaxSpeeds.txt"))) {

            short numFloors = Short.parseShort(br.readLine());
            short numRads = Short.parseShort(br.readLine());
            rowList = new short[numFloors][];
            for (int i = 0; i < numFloors; i++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");

                rowList[i] = new short[numRads];

                for (int j = 0; j < numRads; j++) {
                    rowList[i][j] = Short.parseShort(lineItems[j]);
                }
            }


        } catch (Exception e) {
            // Handle any I/O problems
        }
        return rowList;
    }

    //Helper method to read defishing data
    private static FishData[][] readFishData() { //same as the other read files

        FishData[][] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/CameraData.txt"))) {

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
            //Handle I/O problems
        }
        return rowList;
    }

    //Helper method to read angle data
    private static HashMap<Integer, Integer> readAngleData (){ //Same documentation as any other read file
        HashMap<Integer, Integer> hashMap = null; //set up a hashmap, hashmap's like a dictionary: one item to one item
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/AngleData.txt"))) {

            short radCount = parseShort(br.readLine());
            hashMap = new HashMap<>();
            for (int i = 0; i < radCount; i++) {
                String[] row = br.readLine().split(" ");
                int key = Integer.parseInt(row[1]);
                int val = Integer.parseInt(row[0]);
                hashMap.put(key, val);
            }

        }
        catch(Exception e){
            e.printStackTrace();

        }
        return hashMap;
    }

    //Helper method to read radii data
    private static short[] readRadiiData() { //same as any other read file
        //TODO: FIX
        short[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/RadiiData.txt"))) {

            short angleCount = parseShort(br.readLine());
            rowList = new short[angleCount];
            String line = br.readLine();
            String[] lineItems = line.split(" ");
            for (int i = 0; i < angleCount; i++) {

                rowList[i] = Short.parseShort(lineItems[i]);
            }

        }
        catch(Exception e){
            e.printStackTrace();

        }
        return rowList;
    }

    //angle, desired velocity

    private static short[][] readThrottleData() {
        short[][] rowList = null;//same as any other read file

        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/ThrottleData.txt"))) {

            short radCount = parseShort(br.readLine());
            short desiredSpeedsCount = parseShort(br.readLine());
            rowList = new short[radCount][desiredSpeedsCount];

            for (int j = 0; j < radCount; j++) {

                String line = br.readLine();
                String[] lineItems = line.split(" ");
                for (int k = 0; k < desiredSpeedsCount; k++) {
                    rowList[j][k] = Short.parseShort(lineItems[k]);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowList;
    }


    private static float[] readPixelData() { //same as any other read file
        float[] rowList = null;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/org/avphs/calibration/PixelData.txt"))) {

            short angleCount = parseShort(br.readLine());
            rowList = new float[angleCount];
            String line = br.readLine();
            String[] lineItems = line.split(" ");
            for (int i = 0; i < angleCount; i++) {

                rowList[i] = Float.parseFloat(lineItems[i]);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return rowList;
    }

    //input current speed and desired speed. get distance
    private static final short[][][] SPEED_CHANGE_DISTS = readSpeedChangeDistData();//Create an array based on the data read from the file
    //same as all others in this locale


    //input floor type, radius of turn, get max velocity that can be taken around that curve
    private static final short[][] MAX_SPEEDS = readMaxSpeedData();

    //input x and y, get defishing
    private static final FishData[][] DEFISHER = readFishData();

    //input radius, get angle required to turn that radius
    private static final HashMap<Integer, Integer> ANGLES = readAngleData();

    //input angle, get radius that the car will turn at that angle
    private static final short[] RADII = readRadiiData();

    //input floor surface and a given turn radius and get the throttle required to get that speed
    private static final short[][] THROTTLES = readThrottleData();

    private static final float[] PIXEL_DISTS = readPixelData();





    public static final FishData getFishData(short x, short y) {
        return DEFISHER[x][y];
    }



    public static final short getMaxSpeed(short floor, short rad) {
        return MAX_SPEEDS[floor][rad];

    }

    public static final short getSpeedChangeDist(byte floor, short initSpeed, short finalSpeed) {
        byte i = (byte) (initSpeed / MIN_DELTA_SPEED);//allows a negative index to be accessed...
        byte f = (byte) (finalSpeed / MIN_DELTA_SPEED);
        return SPEED_CHANGE_DISTS[floor][i][f];//here
    }

    public static final int getAngles(int rad) {
        return ANGLES.get(rad);
    }

    public static final short getRadii(short angle) {
        return RADII[angle - SERVO_ANGLE_MIN];//Adds min such that negative angles can be accessed in the array
    }

    //returns the amount of throttle needed to maintain a given speed on a given floor surface and with a given turn radius
    //0 = go full throttle
    public static final short getThrottle (short radius, short speed){
        return THROTTLES[radius][speed];

    }


    public static float getTurnRadiusOfAngle(int angle) {
        if (angle < -77) {
            angle = -77;
            System.out.println("You requested radius for an angle less than -77. Don't.");
        } else if (angle > 80) {
            angle = 80;
            System.out.println("You requested radius for an angle greater than 80. Don't.");
        }
        if (angle > 2) {
            angle -= 1;
        }
        angle += 77;
        return (turnRadiiForAngles[angle]);
    }

    public static int getAngleForTurnRadius(float radius, boolean turningLeft) { //can be optimized
        if (turningLeft) {
            if (radius > turnRadiiForAngles[78]) { //if radius is greater than 1 deg radius
                return 2; //go straight
            } else {
                for (int i = 1; i >= -77; i--) {
                    if (radius >= turnRadiiForAngles[i + 77]) {
                        if (Math.abs(turnRadiiForAngles[i + 77] - radius) <= Math.abs(turnRadiiForAngles[i + 78] - radius)) {
                            return i;
                        } else {
                            return i + 1;
                        }
                    }

                }
            }
        } else {
            if (radius > turnRadiiForAngles[79]) { //if radius is greater than 3 deg radius
                return 2; //go straight
            } else {
                for (int i = 3; i <= 80; i++) {
                    if (radius >= turnRadiiForAngles[i + 76]) {
                        if (Math.abs(turnRadiiForAngles[i + 76] - radius) <= Math.abs(turnRadiiForAngles[i + 75] - radius)) {
                            return i;
                        } else {
                            return i - 1;
                        }
                    }

                }
            }
        }
        return 2;
    }

    public static final float getDist(short pixelHeight) {
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