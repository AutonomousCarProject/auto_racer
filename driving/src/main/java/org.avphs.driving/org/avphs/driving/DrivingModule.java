package org.avphs.driving;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.racingline.RacingLine;
import org.avphs.racingline.RacingLinePoint;

import java.util.ArrayList;

import static org.avphs.coreinterface.CarCommand.*;

public class DrivingModule implements CarModule {

    private RacingLine[] racingline;
    private ArrayList<RoadData> roadData;
    private RoadData currentSegment;
    private RoadData nextSegment;
    private int angle = 90;
    private int throttle = 90;
    private int MAX_SPEED;
    private int MAX_HARD_BRAKE;
    private VectorPoint currentPos;
    Speed speed = new Speed(currentPos, currentSegment, nextSegment);
    Steering steer = new Steering(currentPos, currentSegment);

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return new CarCommand[] {
                accelerate(true, 0),
                steer(false, 10),
                stop()
        };
    }

    @Override
    public void update(CarData carData) {
        System.out.println("Driving");
    }
  
    public void run() {
        System.out.println("Driving");
    }

    public DrivingModule(RacingLine[] racingLine){
        this.racingline = racingLine;
        roadData = new ArrayList<RoadData>();
        analyzeRacingLine();
    }

    public DrivingModule(){
    }
    public short findRadius(RacingLinePoint rcl1, RacingLinePoint rcl2, RacingLinePoint rcl3) {
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
        return r;
    }

    public void analyzeRacingLine(){
        //Racingline should eventaully have points only on the maxima and minima
        // .getDegree calculates the degree from the current point as a center and a previous and future point, and thus needs to start at second index
        for (int i = 1; i <  racingLinePoints.length ; i++ ) {
            if (racingLinePoints[i].getDegree() > 170 && racingLinePoints[i].getDegree() < 190) { // 10 is an arbitrary number that needs further numbers
                new Straight(racingLinePoints[i-1].getX(),racingLinePoints[i-1].getY(),racingLinePoints[i+1].getX(),racingLinePoints[i+1].getY());
            }
            else  {
                new Turn(racingLinePoints[i-1].getX(),racingLinePoints[i-1].getY(),racingLinePoints[i+1].getX(),racingLinePoints[i+1].getY(),
                        findRadius(racingLinePoints[i-1],racingLinePoints[i],racingLinePoints[i+1]));
            }
        }
        //ArrayList<RacingLinePoint> racingLinePoints = new ArrayList<RacingLinePoint>();
        //racingLine.getRacingLinePoints();
    }

    public void setCurrentPos(VectorPoint input){
        currentPos = input;
    }

    public VectorPoint getPosition(){ //returns the (x,y) of the car in the map
        return null;
    }

    public int getDirection(){ //returns the direction of the car from 0 to 180
        angle = steer.getAngle();
        if (angle == (float)-1){
            throttle = 90;
            return 90;
        }
        return angle;
    }

    public int getThrottle() { //returns the throttle of the car from 0 to 180
        //return throttle = speed.getThrottle();
        return 90;
    }
}
