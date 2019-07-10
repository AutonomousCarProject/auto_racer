package org.avphs.position;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;

public class PositionModule implements CarModule {

    private ImageModule imageModule;
    private MapModule mapModule;

    @Override
    public Class[] getDependencies() {
        return new Class[] {
        	ImageModule.class, MapModule.class
        };
    }

    @Override
    public void init(CarModule... dependencies) {
        imageModule = (ImageModule) dependencies[0];
        mapModule = (MapModule) dependencies[1];
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Position");

    }
    
    
    
    //THE CODE BELOW IS TEMPORARY AND SUBJECT TO CHANGE PROBABLY VERY SOON
    public float[] getPosition(){ //returns the (x,y) of the car (ideally synced with the map)
        return new float[]{0,0};
    }
    
    public float getDirection(){ //returns the direction of the car in degrees, always 0<= x <360
        return 0.0;
    }
    
    //DEPRECATED BECAUSE THIS EXISTS IN TRAKSIM
    public float getSpeed(){
        return 0.0;
    }
}
