package org.avphs.position;

import org.avphs.calibration.CalibrationModule;
import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
//import org.avphs.map.MapModule;

public class PositionModule implements CarModule {
    private ImageModule imageModule;
    //private MapModule mapModule;
    private CalibrationModule calibrationModule;

    private int prevSpins; //deprecated
    private PositionData prevPositionData = new PositionData(new float[]{0, 0},0, 0); //WILL BE USED LATER
    private PositionData positionData;


    @Override
    public Class[] getDependencies() {
        return new Class[] {
            ImageModule.class, /*MapModule.class,*/ CalibrationModule.class
        };
    }

    @Override
    public void init(CarData carData) {
        //imageModule = (ImageModule) dependencies[0];
        //mapModule = (MapModule) dependencies[1];
        //calibrationModule = (CalibrationModule) dependencies[2];
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{0, 0},0, 0); //TEMPORARY
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        //ORDER OF FUNCTION CALLING THAT HAPPENS EVERY TIME
        computeDirection();
        computeSpeed();
        computePosition();
        carData.addData("position",positionData);

        //THIS WILL BE USED LATER
        prevPositionData.updateAll(positionData.getPosition(), positionData.getDirection(), positionData.getSpeed());
    }

    private void computeDirection() {
        //FIXME: More complex calculation to find travel distance on arc required
        //CALL THE FUNCTION TO GET SERVO ANGLE
        //assuming straight wheels = 90 in servo angle return thing
        //prevdirection += WHEEL ANGLE - 90; //servo angle return value placeholder
        float direction = 0; //temporary
        if (direction >= 360) {
            direction -= 360;
        } else if (direction < 0) {
            direction += 360;
        }
        positionData.updateDirection(direction);
    }
    private void computeSpeed() {
        //GET SPIN COUNT - prevSpins;
        //INSERT calibrationn function to convert shaft spin to distance
        float speed = 0;//calibration.getDistance_Elapsed(); //0 is temporary
        //depending on calibration function, this may need conversion to m/s
        positionData.updateSpeed(speed);
    }

    private void computePosition() {
        float x = positionData.getPosition()[0] + (float) Math.cos(Math.toRadians(positionData.getDirection())) * positionData.getSpeed();
        float y = positionData.getPosition()[1] + (float) Math.sin(Math.toRadians(positionData.getDirection())) * positionData.getSpeed();
        positionData.updatePosition(new float[]{x, y});
    }


    //DEPRECATED, TRACKSIM HAS THIS
    private void updateSpinCount() {
        //prevSpins = GET SPIN COUNT
        prevSpins = 0; //temporary
    }
}

