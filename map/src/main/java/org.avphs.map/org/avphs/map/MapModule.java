package org.avphs.map;


import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.position.PositionModule;

public class MapModule implements CarModule {

    private ImageModule imageModule;
    private PositionModule positionModule;

    private Map map = new Map();
    private FakeDataStreamForMap fakedata = new FakeDataStreamForMap();
    private MapFormatter mapformatter = new MapFormatter(map);

    //private MapUtils mapUtilities = new MapUtils();   --Commented out because we are not using any of these yet


    @Override
    public Class[] getDependencies() {
        return new Class[] {
            ImageModule.class, PositionModule.class
        };
    }
    @Override
    public void init(CarData carData) {
        imageModule = (ImageModule) carData.getModuleData("image");
        System.out.println("Image Module Found" + imageModule.getClass());
        positionModule = (PositionModule) carData.getModuleData("position");
        System.out.println("Position Module Found" + positionModule.getClass());
        //map.showMap();
        mapformatter.utils.setupDistanceLookup();
        //mapUtilities.setupSineAndCosine();   --Commented out because we are not using any of these yet
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        fakedata.updatePos();
        mapformatter.AddData(fakedata.returnPos(), (float)fakedata.runningRadianTotal, fakedata.bottomOuterWallHeights);
        if (fakedata.done) {
            if (!fakedata.mapshown) {
                map.showMap();
                fakedata.mapshown = true;
            }

        }
    }

    public Map getMap(){return map;}


}
