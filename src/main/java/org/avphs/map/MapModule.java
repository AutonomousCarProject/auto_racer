package org.avphs.map;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.image.ImageData;
import org.avphs.position.PositionData;
import org.avphs.traksim.TrakSim;
import org.avphs.position.PositionModule;

import java.io.FileWriter;
import java.io.IOException;

public class MapModule implements CarModule, CloseHook {

    private final int MAP_MODE = 3;
    //0: Mapping while driving close to the walls
    //1: Mapping by driving through the center of the track and expanding the track 5 carlengths out
    //Modes 2&3 are debugging modes
    //2: Debugging in TrakSim
    //3: Debugging using FakeDataStream
    //4: Debugging in TrakSim using only position tracking
    private final int MAP_X_DIMENSION = 1500;
    private final int MAP_Y_DIMENSION = 1500;
    private final float STARTING_ANGLE = 270.0f;


    //One unit in the array = 1cm. This means that 1500x1500 is equal to a 15m by 15m room.

    private final float MODIFIED_CAR_X_STARTING_POSITION = 0.0f;
    private final float MODIFIED_CAR_Y_STARTING_POSITION = 0.0f;
    //These numbers are added to the (0,0) origin to indicate the starting position of the car in the room.


    private ImageData imageData;
    private PositionData positionModule;
    private TrakSim trakSimData = new TrakSim();
    private Map map = new Map(MAP_X_DIMENSION, MAP_Y_DIMENSION);

    //public float waitToShowTrack = (float)System.currentTimeMillis();
    private FakeDataStreamForMap fakedata;
    private MapFormatter mapformatter = new MapFormatter(map);


    //private boolean mapshown = false;
    private int cycleCounter  = 0;
    //private MapUtils mapUtilities = new MapUtils();   --Commented out because we are not using any of these yet

    @Override
    public void init(CarData carData) {
        carData.addData("map", map);
        //modules are only used in update
        //DO NOT GET THE MODULES HERE ATM

        //System.out.println("set");
        //mapUtilities.setupSineAndCosine();   --Commented out because we are not using any of these yet

        switch (MAP_MODE)//Chooses which to setup based on situation.
        {
            case 3:
                fakedata = new FakeDataStreamForMap();
            case 2:
            case 4:
            case 0:
                mapformatter.utils.setupDistanceLookup();
                break;
            default:
        }
    }

    @Override
    public void update(CarData carData) {
        //positionModule = (PositionData)carData.getModuleData("position");
        // imageData = (ImageData) carData.getModuleData("image");
        //System.out.println("Anything");

        //For Testing in traksim
        //System.out.println(trakSimData.GetPosn(true))

        //System.out.println(trakSimData.GetFacing());

        switch (MAP_MODE)//Updating statements used for each map mode.
        {
            case 0:
                positionModule = (PositionData) carData.getModuleData("position");
                imageData = (ImageData) carData.getModuleData("image");
                mapformatter.AddData(positionModule.getPosition(), positionModule.getDirection(), imageData.wallBottom);
                break;
            case 1:
                positionModule = (PositionData) carData.getModuleData("position");
                mapformatter.expandTrackFiveCarLengthsToTheLeftAndRightOfCurrentPos(positionModule.getPosition(), positionModule.getDirection());
                break;
            case 2:
                //For Testing in traksim


                imageData = (ImageData) carData.getModuleData("image");
                //System.out.println(trakSimData.GetPosn(true));
                float[] pos = new float[2];

                pos[0] = (float) ((trakSimData.GetPosn(true) * 12.5) + 200);//Convert TrakSim x position to map position

                pos[1] = (float) ((trakSimData.GetPosn(false) * 12.5));//Convert Traksim y position to map position


                //System.out.println("Current Pos: " + pos[0] + ", "+ pos[1]);


                //This converts a traksim angle to one more like what position tracking would give us
                float currentAngle;
                float trakSimAngle = (float) trakSimData.GetFacing();

                //Converts TrakSimAngle to Angle Resembling what we would get from Pos Tracking
                if (trakSimAngle < STARTING_ANGLE) {
                    currentAngle = (360.0f - (STARTING_ANGLE - trakSimAngle));
                } else if (trakSimAngle > STARTING_ANGLE) {
                    currentAngle = (trakSimAngle - STARTING_ANGLE);
                } else {
                    currentAngle = 0.0f;
                }


                //System.out.println("Current Angle: " + currentAngle);

                mapformatter.AddData(pos, currentAngle, imageData.wallBottom);


                break;
            case 3:
                //For Testing Using FakeDataStream

                fakedata.updatePos();//Updates position on FakeData (Fake Track)
                mapformatter.AddData(fakedata.returnPos(), (float) fakedata.runningRadianTotal, fakedata.bottomOuterWallHeights);
                if (fakedata.done) {
                    if (!fakedata.mapshown) {
                        map.showMap();
                        fakedata.mapshown = true;
                    }

                }
                break;
            case 4:

                //System.out.println(trakSimData.GetPosn(true));
                float[] pos1 = new float[2];

                pos1[0] = (float)(( trakSimData.GetPosn(true) * 12.5) + MODIFIED_CAR_X_STARTING_POSITION);//Convert TrakSim x position to map position

                pos1[1] = (float)(( trakSimData.GetPosn(false) * 12.5) + MODIFIED_CAR_Y_STARTING_POSITION);//Convert Traksim y position to map position



                //System.out.println("Current Pos: " + pos[0] + ", "+ pos[1]);


                //This converts a traksim angle to one more like what position tracking would give us
                float currentAngle1; float trakSimAngle1 = (float)trakSimData.GetFacing();

                //Converts TrakSimAngle to Angle Resembling what we would get from Pos Tracking
                if (trakSimAngle1 < STARTING_ANGLE)
                {
                    currentAngle1 = (360.0f - (STARTING_ANGLE - trakSimAngle1));
                }
                else if (trakSimAngle1 > STARTING_ANGLE)
                {
                    currentAngle1 = (trakSimAngle1 - STARTING_ANGLE);
                }
                else
                {
                    currentAngle1 = 0.0f;
                }


                //System.out.println("Current Angle: " + currentAngle);

                mapformatter.expandTrackFiveCarLengthsToTheLeftAndRightOfCurrentPos(pos1, currentAngle1);
        }

        cycleCounter++;

        /**
         * THINGY
         * ============================
         */
        /*
        if (cycleCounter % 200 == 0)//Show map developing
        {
            map.showMap();
        }//*/
        carData.addData("map", map);

    }

    public Map getMap() {
        return map;
    }


    @Override
    public void onClose() {
        try {
            boolean[][] m = map.getMap();
            FileWriter f = new FileWriter("src/main/java/org/avphs/map/map.txt");
            f.write(m.length + "  " + m[0].length + "\n");
            for (int i = 0; i < map.getMap().length; i++) {
                for (int j = 0; j < m[0].length; j++) {
                    if (i == map.startY && j == map.startX) {
                        f.write('s');
                    } else {
                        if (m[i][j]) f.write('1');
                        else f.write('0');
                    }
                }
                f.write('\n');
            }

            f.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
