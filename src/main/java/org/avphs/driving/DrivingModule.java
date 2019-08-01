package org.avphs.driving;

import org.avphs.calibration.CalibrationModule;
import org.avphs.car.Car;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.position.PositionData;
import org.avphs.racingline.RacingLine;
import org.avphs.racingline.RacingLinePoint;

import java.util.ArrayList;

public class DrivingModule implements CarModule {

    private RacingLine[] racingline;
    private ArrayList<RoadData> roadData = new ArrayList<RoadData>();
    private RacingLinePoint[] racingLinePoints;
    private RoadData currentSegment;
    private RoadData nextSegment;

    private int angle = 0;
    private int throttle = 0;
    private boolean stop = false;
    private final Car car;

    int index = 0;

    final int addOn = 0;

    private VectorPoint currentPos;

    /*From speed:*/
    private final byte MAX_HARD_BRAKE;  //max throttle for braking w/o skidding
    private final byte FLOOR;           //floor index
    private int brakeDist;
    private float[] currentPosOnLine;   //where we "should" be on the racing line. Updates with every getThrottle call.
    private short throttleForSeg;

    /*From steering*/
    private final float MAX_DIST_FROM_RL;

    //This constructor sets some values

    public DrivingModule(Car car) {
        this.car = car;
        FLOOR = 0;
        MAX_HARD_BRAKE = 80; //dummy value
        MAX_DIST_FROM_RL = 10;
    }

    @Override
    public void init(CarData carData) {
        //Adds the angle to carData
        //carData.addData("driving", angle);
        //Grabs the racing line from RacingLine and analyzes it
        racingLinePoints = (RacingLinePoint[])carData.getModuleData("racingLine");
        analyzeRacingLine();
        //Grabs the starting position from PositionTracking
        //PositionData posData = (PositionData)carData.getModuleData("position");
        //currentPos = new VectorPoint(posData.getPosition(), posData.getDirection(), posData.getSpeed());
        //Checks to see where the car is then sets the current and next segments
        currentPos = new VectorPoint(new float[]{racingLinePoints[index].getX(), racingLinePoints[index].getY()}, 0, 0);
        getSegment();
    }

    @Override
    public void update(CarData carData) {
        //Updates the current position and updates the current/next segments
        //PositionData posData = (PositionData)carData.getModuleData("position");

        currentPos = new VectorPoint(new float[]{racingLinePoints[index].getX(), racingLinePoints[index].getY()}, 0, 0);
        //getSegment();
        currentSegment = roadData.get(index);
        System.out.println(currentSegment);
        //Updates the direction and throttle to reflect position change
        getDirection();
        getThrottle();
        //Refreshes the data in carData
        //carData.addData("driving", angle);
        //Tells the car to move :)
        car.accelerate(true, throttle);
        car.steer(true, angle + addOn);
        index++;
        if (index >= racingLinePoints.length){
            index = 0;
        }
    }

    /*
        initialize() finds the distance needed to break on a given Straight and the max throttle for the segment
     */
    private void initialize() {
        if (currentSegment instanceof Straight) {
            brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, CalibrationModule.getMaxSpeed(FLOOR,
                    currentSegment.radius), CalibrationModule.getMaxSpeed(FLOOR, nextSegment.radius));
            throttleForSeg = (short) 60;
        } else {
            throttleForSeg = CalibrationModule.getThrottle(currentSegment.radius,
                    CalibrationModule.getMaxSpeed(FLOOR, currentSegment.radius));
        }
    }

    /*
        getSegment() finds, based on the car's current position, which segment of the racing line it is on,
        and if the car is on a different segment than previously though, it will set the currentSegment and nextSegment
        variables then call initialize()
     */

    private void getSegment(){
        float x = currentPos.getX(); float y = currentPos.getY();
        for (RoadData i : roadData){
            if (i instanceof Straight){
                Straight seg = (Straight)i;
                float m = seg.getSlope(); float b = (-m * x) + y;
                float eq = m * x + b;
                if (y > eq - 10 && y < eq + 10){
                    currentSegment = i;
                    int index = roadData.indexOf(i);
                    index++;
                    if (index == roadData.size()){
                        index = 0;
                    }
                    nextSegment = roadData.get(index);
                }
            } else {
                Turn seg = (Turn)i;
                float r = seg.getRadius(); float h = seg.getCenterX(); float k = seg.getCenterY();
                float eq = (float)Math.pow((double)(x - h),2) + (float)Math.pow((double)(y - k),2);
                float r2 = r * r;
                if (r2 > eq - 10 && r2 < eq + 10){
                    currentSegment = i;
                    int index = roadData.indexOf(i);
                    index++;
                    if (index == roadData.size()){
                        index = 0;
                    }
                    nextSegment = roadData.get(index);
                }
            }
        }
        //initialize();
    }

    /*
        This method does some math to determine, based on three points, the radius and center of the points, then returns a
        Curve object.
     */

    private Curve findRadiusAndCenter(RacingLinePoint rcl1, RacingLinePoint rcl2, RacingLinePoint rcl3) {
        //computers radius of three points (rcl = racing line point)
        float x12 = rcl1.getX() - rcl2.getX();
        float x13 = rcl1.getX() - rcl3.getX();

        float y12 = rcl1.getY() - rcl2.getY();
        float y13 = rcl1.getY() - rcl3.getY();

        float y31 = rcl3.getY() - rcl1.getY();
        float y21 = rcl2.getY() - rcl1.getY();

        float x31 = rcl3.getX() - rcl1.getX();
        float x21 = rcl2.getX() - rcl1.getX();
         // x1^2 - x3^2
        float sx13 =(float)( Math.pow(rcl1.getX(), 2) - Math.pow(rcl3.getX(), 2));

        // y1^2 - y3^2
        float sy13 = (float)(Math.pow(rcl1.getY(), 2) - Math.pow(rcl3.getY() , 2));

        float sx21 = (float)(Math.pow(rcl2.getX(), 2) - Math.pow(rcl1.getX(), 2));
        float sy21 = (float)(Math.pow(rcl2.getY(), 2) - Math.pow(rcl1.getY(), 2));

        float f = ((sx13) * (x12)
                + (sy13) * (x12)
                + (sx21) * (x13)
                + (sy21) * (x13))
                / (2 * ((y31) * (x12) - (y21) * (x13)));
        float g = ((sx13) * (y12)
                + (sy13) * (y12)
                + (sx21) * (y13)
                + (sy21) * (y13))
                / (2 * ((x31) * (y12) - (x21) * (y13)));

        float c = (float) ( -Math.pow(rcl1.getX(),2) - Math.pow(rcl1.getY(), 2) - 2 * g * rcl1.getX() - 2 * f * rcl1.getY());

        // eqn of circle be x^2 + y^2 + 2*g*x + 2*f*y + c = 0
        // where centre is (h = -g, k = -f) and radius r
        //float r^2 = h^2 + k^2 - c
        float h = -g;
        float k = -f;
        float sqr_of_r = h * h + k * k - c;

        // r is the radius
        short r = (short) Math.sqrt(sqr_of_r);
        return new Curve(h,k,r);
    }

    /*
        analyzeRacingLine() looks at any two points and determines whether they constitute a Straight or Curve, then inputs those
        coordinates and all other necessary data into the respective objects
     */

    private void analyzeRacingLine(){
        roadData.clear();
        //RacingLine should eventually have points only on the maxima and minima
        // .getDegree calculates the degree from the current point as a center and a previous and future point, and thus needs to start at second index
        int n = racingLinePoints.length;
        for (int i = 0; i < n; i++) {
            if (racingLinePoints[i].getDegree() > 170 && racingLinePoints[i].getDegree() < 190) { // 10 is an arbitrary number that needs further numbers
                roadData.add(new Straight(racingLinePoints[(i+n-1)%n].getX(),racingLinePoints[(i+n-1)%n].getY(),racingLinePoints[i].getX(),racingLinePoints[i].getY()));
            }
            else  {
                Turn toAdd = new Turn(racingLinePoints[(i+n-1)%n].getX(),racingLinePoints[(i+n-1)%n].getY(),racingLinePoints[(i+1)%n].getX(),racingLinePoints[(i+1)%n].getY(),
                        findRadiusAndCenter(racingLinePoints[(i+n-1)%n],racingLinePoints[i],racingLinePoints[(i+1)%n]));
                roadData.add(toAdd);
                RacingLinePoint prev = racingLinePoints[(i+n-1)%n];
                RacingLinePoint curr = racingLinePoints[i%n];
                RacingLinePoint next = racingLinePoints[(i+1)%n];
                int ax = curr.getIntX()-prev.getIntX();
                int ay = curr.getIntY()-prev.getIntY();
                int bx = next.getIntX()-curr.getIntX();
                int by = next.getIntY()-curr.getIntY();
                boolean dir = (ax*by-ay*bx)>0;
                toAdd.setLeft(dir);
            }
        }
    }

    /*
        onRacingLine() checks to see if the car is still on the racingLine
     */

    private boolean onRacingLine(){  //Returns whether we are close enough to the racing line
        float distance;
        if (currentSegment instanceof Straight){
            Straight segment = (Straight)currentSegment;
            distance = Calculator.findStraightDistance(currentPos.getX(), currentPos.getY(), segment.getB(),
                    segment.getSlope());
        } else {
            Turn segment = (Turn)currentSegment;
            distance = Calculator.findTurnDistance(currentPos.getX(),currentPos.getY(), new float[]{segment.getCenterX(),
                    segment.getCenterY()}, segment.getRadius());
        }
        return (distance < MAX_DIST_FROM_RL) || (distance == MAX_DIST_FROM_RL);
    }

    /*
        Steer Function that determines the angle the wheels need to be
     */
    private void getDirection(){ //returns the direction of the car from 0 to 180
        //if (onRacingLine()) {
            if (currentSegment instanceof Straight) {
                angle = 0;
            } else {
                Turn c = (Turn)currentSegment;
                int mult = (c.getLeft())?-6:6;
                angle = mult*CalibrationModule.getAngles(currentSegment.radius);
            }
        //} else {
            //angle = 1;
            //stop = true;
        //}
    }

    private void getThrottle(){
        if (currentSegment instanceof Straight){
            throttle = 18;
        } else {
            throttle = 18;
        }
    }

    /*
        Speed Function that determines the throttle the car needs to be
     */
    /*
    private void getThrottle() { //returns the throttle of the car from 0 to 180
        if (currentSegment instanceof Straight){
            currentPosOnLine = Calculator.findClosestPoint(currentPos.getX(), currentPos.getY(), //Update
                    ((Straight)currentSegment).getSlope(), ((Straight)currentSegment).getB());     //currentPosOnLine
            if ((int)Math.sqrt(Math.pow(currentSegment.endX - currentPosOnLine[0], 2.0) //If we're not to the brake
                    + Math.pow(currentSegment.endY - currentPosOnLine[1], 2.0)) > brakeDist){ //point yet,
                throttle = 60;     //full throttle
            } else {
                throttle = MAX_HARD_BRAKE;
            }
        } else {
            throttle = throttleForSeg;
        }
    }
     */
}
