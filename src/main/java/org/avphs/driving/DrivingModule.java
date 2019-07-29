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
    private int throttle = 12;
    private boolean stop = false;
    private final Car car;

    private VectorPoint currentPos;

    /*From speed:*/
    private final byte MAX_HARD_BRAKE;  //max throttle for braking w/o skidding
    private final byte FLOOR;           //floor index
    private int brakeDist;
    private float[] currentPosOnLine;   //where we "should" be on the racing line. Updates with every getThrottle call.
    private short throttleForSeg;

    /*From steering*/
    private final float MAX_DIST_FROM_RL;

    public DrivingModule(Car car) {
        this.car = car;
        FLOOR = 0;
        MAX_HARD_BRAKE = 80; //dummy value
        MAX_DIST_FROM_RL = 10;
    }

    @Override
    public void init(CarData carData) {
        carData.addData("driving", angle);
        racingLinePoints = (RacingLinePoint[])carData.getModuleData("racingLine");
        analyzeRacingLine();
        PositionData posData = (PositionData)carData.getModuleData("position");
        currentPos = new VectorPoint(posData.getPosition(), posData.getDirection(), posData.getSpeed());
        getSegment();
    }

    @Override
    public void update(CarData carData) {
        PositionData posData = (PositionData)carData.getModuleData("position");
        currentPos = new VectorPoint(posData.getPosition(), posData.getDirection(), posData.getSpeed());
        getSegment();
        getDirection();
        getThrottle();
        carData.addData("driving", angle);

        //FIXME THROTTLE IS SET TO SOMETHING INCORRECT
        car.accelerate(true, 12);
        car.steer(true, angle);
    }

    private void initialize() {
        if (currentSegment instanceof Straight) {
            brakeDist = CalibrationModule.getSpeedChangeDist(FLOOR, CalibrationModule.getMaxSpeed(FLOOR,
                    currentSegment.radius), CalibrationModule.getMaxSpeed(FLOOR, nextSegment.radius));
            throttleForSeg = (short) 90;
        } else {
            throttleForSeg = CalibrationModule.getThrottle(FLOOR, currentSegment.radius,
                    CalibrationModule.getMaxSpeed(FLOOR, currentSegment.radius));
        }
    }

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
        initialize();
    }

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
                roadData.add(new Turn(racingLinePoints[(i+n-1)%n].getX(),racingLinePoints[(i+n-1)%n].getY(),racingLinePoints[(i+1)%n].getX(),racingLinePoints[(i+1)%n].getY(),
                        findRadiusAndCenter(racingLinePoints[(i+n-1)%n],racingLinePoints[i],racingLinePoints[(i+1)%n])));
            }
        }
        System.out.println("NNNNNNN: "+n);
    }

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

    /*  Steer Function  */
    private void getDirection(){ //returns the direction of the car from 0 to 180
        if (onRacingLine()) {
            if (currentSegment instanceof Straight) {
                angle = 90;
            } else {
                angle = CalibrationModule.getAngles(currentSegment.radius);
            }
        } else {
            angle = -1;
            //stop = true;
        }
    }

    /*  Speed Function  */
    private void getThrottle() { //returns the throttle of the car from 0 to 180
        if (currentSegment instanceof Straight){
            currentPosOnLine = Calculator.findClosestPoint(currentPos.getX(), currentPos.getY(), //Update
                    ((Straight)currentSegment).getSlope(), ((Straight)currentSegment).getB());     //currentPosOnLine
            if ((int)Math.sqrt(Math.pow(currentSegment.endX - currentPosOnLine[0], 2.0) //If we're not to the brake
                    + Math.pow(currentSegment.endY - currentPosOnLine[1], 2.0)) > brakeDist){ //point yet,
                throttle = 90;     //full throttle
            } else {
                throttle = MAX_HARD_BRAKE;
            }
        } else {
            throttle = throttleForSeg;
        }
    }
}
