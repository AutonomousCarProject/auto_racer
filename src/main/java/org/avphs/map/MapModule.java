package org.avphs.map;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.coreinterface.CloseHook;
import org.avphs.image.ImageData;
import org.avphs.image.ImageModule;
import org.avphs.position.PositionData;
import org.avphs.position.PositionModule;

public class MapModule implements CarModule, CloseHook {

    private ImageData imageData;
    private PositionData positionModule;

    private Map map = new Map();
    private FakeDataStreamForMap fakedata = new FakeDataStreamForMap();
    private MapFormatter mapformatter = new MapFormatter(map);

    //private MapUtils mapUtilities = new MapUtils();   --Commented out because we are not using any of these yet

    @Override
    public void init(CarData carData) {
        carData.addData("map", map);
        //modules are only used in update
        //DO NOT GET THE MODULES HERE ATM
        // positionModule = (PositionData)carData.getModuleData("position");
        //imageData = (ImageData)carData.getModuleData("image");

        mapformatter.utils.setupDistanceLookup();
        //mapUtilities.setupSineAndCosine();   --Commented out because we are not using any of these yet
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        carData.addData("map", map);
        positionModule = (PositionData)carData.getModuleData("position");
        imageData = (ImageData) carData.getModuleData("image");


        /*
        fakedata.updatePos();
        mapformatter.AddData(fakedata.returnPos(), (float)fakedata.runningRadianTotal, fakedata.bottomOuterWallHeights);
        if (fakedata.done) {
            if (!fakedata.mapshown) {
                map.showMap();
                fakedata.mapshown = true;
            }

        }
        */
    }

    public Map getMap(){return map;}


    @Override
    public void onClose() {
        System.out.println("OGENISOI EJOSIGJSOGI  I AM CLOSIN RIGHT NOW!!!!");
    }
}
