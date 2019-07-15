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
    private float[] position = {0,0};
    private float direction=0;
    private float speed=0;
    private float movement;
    private int prevSpins; //depricated
    private PositionData positionData;

    @Override
    public Class[] getDependencies() {
        return new Class[] {
            ImageModule.class, /*MapModule.class,*/ CalibrationModule.class
        };
    }

    @Override
    public void init(CarModule... dependencies) {
        //imageModule = (ImageModule) dependencies[0];
        //mapModule = (MapModule) dependencies[1];
        //calibrationModule = (CalibrationModule) dependencies[2];
    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void update(CarData carData) {
        //FIXME: Position never instantiated
        //positionData.update(position,direction,speed);
        //carData.addData("position",positionData);

        //System.out.println("Position");
        //ORDER OF FUNCTION CALLING THAT HAPPENS EVERY TIME
        //computeMovement
        //computeDirection
        //computePosition

    }
    private void computeDirection() {
        //CALL THE FUNCTION TO GET SERVO ANGLE
        //assuming straight wheels = 90 in servo angle return thing
        //direction += WHEEL ANGLE - 90; //servo angle return value placeholder
        direction = 0; //temporary
        if (direction >= 360) {
            direction -= 360;
        } else if (direction < 0) {
            direction += 360;
        }
    }
    private void computeMovement() {
        //GET SPIN COUNT - prevSpins;
        //INSERT calibrationn function to convert shaft spin to distance
        float distance = 0;//calibration.getDistance_Elapsed(); //0 is temporary
        movement = distance;
    }

    private void computePosition() {
        position[0] += Math.cos(Math.toRadians(direction)) * movement;
        position[1] += Math.sin(Math.toRadians(direction)) * movement;
    }

    //DEPRECATED, TRACKSIM HAS THIS
    private void updateSpinCount() {
        //prevSpins = GET SPIN COUNT
        prevSpins = 0; //temporary
    }
}

