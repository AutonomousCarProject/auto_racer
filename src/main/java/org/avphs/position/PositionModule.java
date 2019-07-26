package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.sbcio.ArduinoData;
import org.avphs.traksim.TrakSim;

import java.sql.SQLOutput;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0); //WILL BE USED LATER
    private PositionData positionData;
    private float disBetweenAxle = 0;
    private float distanceTraveled;
    private float wheelAngle;
    private float deltaPositionAngle;
    private TrakSim ts;
    private int angle = 0;
    private float cumulatedDistance = 0;


    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{40.0f,56.0f}, 0, 0); //TEMPORARY
        ts = new TrakSim();
    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] {
                CarCommand.accelerate(true, 15),
                CarCommand.steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        if(cumulatedDistance > 33 && cumulatedDistance < 104){
            angle = 15;
        }
        else if (cumulatedDistance > 104 && cumulatedDistance < 137){
            angle = 0;
        }
        else if(cumulatedDistance > 137){
            angle = -15;
        }

        ArduinoData odom = (ArduinoData) carData.getModuleData("arduino");
        int steer = (int) carData.getModuleData("driving");
        cumulatedDistance += (float)ts.GetDistance(false);
        //System.out.println(ts.GetDistance(false));
        computePosition((float)ts.GetDistance(true), angle);
        carData.addData("position", positionData);

    }

    private void computePosition(float odometerCount, float drivingData){

        System.out.println("Position = ("+(positionData.getPosition()[0])+","+(positionData.getPosition()[1])+")");

    }
}