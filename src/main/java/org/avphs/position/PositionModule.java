package org.avphs.position;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.traksim.TrakSim;

public class PositionModule implements CarModule {

    private PositionData prevPositionData = new PositionData(new float[]{0, 0}, 0, 0);
    private PositionData positionData;
    private TrakSim ts;
    private int angle = 0;
    private float cumulatedDistance = 0;
    private float deltaPositionAngle;
    private float turnRadius = 0;


    public void init(CarData carData) {
        //THIS WILL BE WHERE WE READ FROM A FILE TO FIND THE INITIAL POSITION
        positionData = new PositionData(new float[]{40.0f,56.0f}, 90, 0); //initial position for this map is (40,56)
        ts = new TrakSim();
    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] { //still using CarCommand because this is an older version of master
                CarCommand.accelerate(true, 15),
                CarCommand.steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        if(cumulatedDistance > 33 && cumulatedDistance < 105){ //hard code to make the car run one lap around the track
            angle = 15;
        }
        else if (cumulatedDistance > 105 && cumulatedDistance < 136){
            angle = 0;
        }
        else if(cumulatedDistance > 136 && cumulatedDistance < 150){
            angle = -15;
        }
        else if(cumulatedDistance > 150 && cumulatedDistance < 153){
            angle = 0;
        }
        else if(cumulatedDistance > 153 && cumulatedDistance < 210){
            angle = -15;
        }
        else if(cumulatedDistance > 210){
            angle = 0;
        }

        cumulatedDistance += (float)ts.GetDistance(false); //used only to find when to turn for the hard code
        ComputePosition((float)ts.GetDistance(true), angle, positionData.getDirection());
        carData.addData("position", positionData);

    }

    private void ComputePosition(float distanceTraveled, float turnAngle, float currentDirection){
        float wheelBase = 10f; //not sure if actually 10

        if (turnAngle > 91) { //if turning right
            turnRadius = (float) (wheelBase / Math.cos(Math.toRadians(-turnAngle)));
        } else if (turnAngle < 89) { //if turning left
            turnRadius = (float) (wheelBase / Math.cos(Math.toRadians(turnAngle)));
        }

        if (turnRadius == 0) {//FIXME: talk to ryan from calibration about straight forward, infinite turn radius
            //just drive straight forward
            convertPosition(0, distanceTraveled);
        } else {
            //if turning
            //distance traveled / circumference give fraction traveled around circle. Multiply by 360 to get the number of degrees around circle we have traveled
            deltaPositionAngle = (float) (360 * distanceTraveled / (Math.PI * Math.pow(turnRadius, 2)));
            if (deltaPositionAngle < 90 || deltaPositionAngle > 270) {//take that angle travelend along the circle, and get an x,y coordinate from that
                convertPosition((float) (turnRadius - turnRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (turnRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]

            } else {//if(deltaPositionAngle > 90), turning left
                convertPosition((float) (turnRadius + turnRadius * Math.cos(Math.toRadians(deltaPositionAngle))), (float) (turnRadius * Math.sin(Math.toRadians(deltaPositionAngle))));//weird trig stuff because for the unit circle the trig is based on center of circle. Here, the car starts at either (1,0) [turning left] or (-1,0) [turning right]
            }

            //given angle traveled around the circle, update the direction we are facing
            if (turnAngle > 91) {//if turning right
                computeDirection(deltaPositionAngle);//update direction with delta direction  because clockwise = positive
            } else if (turnAngle < 91) {//if turning left
                computeDirection(-deltaPositionAngle);//update direction with negative turn going left
            }
        }

        //System.out.println("direction: " + positionData.getDirection());
        //System.out.println("turn radius" + turnRadius);

        System.out.println("our position = ("+(positionData.getPosition()[0])+","+(positionData.getPosition()[1])+")");
        System.out.println("traksim position = (" + ts.GetPosn(true) + "," + ts.GetPosn(false) + ")");
        System.out.println("");
    }

    private void computeDirection(float newDirection) {//adds a new direction to the old direction
        float direction = positionData.getDirection();//get the original direction the car was facing
        direction += newDirection;//add the changed direction
        if (direction >= 360 || direction < 0) {//get the direction between 0 and 360
            direction %= 360;
            if (direction < 360) {//because % returns remainder instead of modulus, we need to add 360 when the angle is < 0
                direction += 360;
            }
        }
        positionData.updateDirection(direction);//add new dir to posdata
    }

    private void convertPosition(float x, float y) {
        //FIXME x and y are currently in cm, not in the virtual world coordinates.
        if(!(x == 0 && y == 0)){
            float[] temp = pol(x, y);
            temp = cart(temp[0], temp[1] - positionData.getDirection());
            positionData.updatePosition(temp);
        }
    }

    private float[] pol(float x, float y){//to polar coordinates
        return new float[] {(float) Math.sqrt(x*x + y*y), (float) Math.toDegrees(Math.atan2(y,x))};
    }

    private float[] cart(float l, float d){//to cartesian
        return new float[] {(float) (l * Math.cos(Math.toRadians(d))), (float) (l * Math.sin(Math.toRadians(d)))};
    }
}