package org.avphs.driving;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.racingline.RacingLine;
import org.avphs.racingline.RacingLinePoint;
import org.avphs.traksim.TrakSim;

import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;

import static org.avphs.coreinterface.CarCommand.accelerate;
import static org.avphs.coreinterface.CarCommand.steer;
import static org.avphs.coreinterface.CarCommand.stop;

public class DrivingModule implements CarModule {

    private RacingLine[] racingLine;
    private ArrayList<RoadData> roadData = new ArrayList<RoadData>();
    private RacingLinePoint[] racingLinePoints;
    private RoadData currentSegment;
    private RoadData nextSegment;

    private int angle = 0;
    private int throttle = 10;
    private boolean stop = false;

    private int MAX_SPEED;
    private int MAX_HARD_BRAKE;
    private VectorPoint currentPos;

    // FIXME: THIS WONT EVER NOT THROW A NULL POINTER EXCEPTION!!!!
    Speed speed = new Speed();
    Steering steer = new Steering();

    private int count = 0;

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarData carData) {

    }

    @Override
    public CarCommand[] commands() {
        if (stop){
            return new CarCommand[] {
                stop()
            };

        }
        return new CarCommand[] {
                accelerate(true, throttle), steer(true, angle)
        };
    }

    @Override
    public void update(CarData carData) {
        float x = 0; float y = 0;
        if (racingLinePoints == null) {
            try {
                racingLine = new RacingLine[]{(RacingLine)carData.getModuleData("RacingLine")};

                racingLinePoints = racingLine[0].getRacingLinePoints();

                analyzeRacingLine();

                currentPos = new VectorPoint(racingLinePoints[0].getX(), racingLinePoints[0].getY(), 90, 90);
                currentSegment = roadData.get(0);
                nextSegment = roadData.get(1);

                steer.changeCurrentSegment(currentSegment);
                steer.changeCurrentPos(currentPos);

                speed.initialize(currentSegment, nextSegment);
                speed.setCurrentPos(currentPos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            currentPos = new VectorPoint(racingLinePoints[count].getX(), racingLinePoints[count].getY(), 90, 90);
            currentSegment = roadData.get(0);
            currentSegment();
            getDirection();
            getThrottle();

            if (count+1 == racingLinePoints.length){
                count = 0;
            }
            count++;
        }
    }

    public void tempUpdate(CarData carData){
        racingLinePoints = (RacingLinePoint[])carData.getModuleData("racingLine");
        float[] temp = (float[])carData.getModuleData("position");
        currentPos = new VectorPoint(temp[0], temp[1], temp[2], temp[3]);
        analyzeRacingLine();
        currentSegment();
        getDirection();
        getThrottle();
    }

    public DrivingModule(){
    }
    public Curve findRadiusAndCenter(RacingLinePoint rcl1, RacingLinePoint rcl2, RacingLinePoint rcl3) {
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

    public void analyzeRacingLine(){
        //RacingLine should eventually have points only on the maxima and minima
        // .getDegree calculates the degree from the current point as a center and a previous and future point, and thus needs to start at second index
        for (int i = 1; i <  racingLinePoints.length-1 ; i++) {
            if (racingLinePoints[i].getDegree() > 170 && racingLinePoints[i].getDegree() < 190) { // 10 is an arbitrary number that needs further numbers
                roadData.add(new Straight(racingLinePoints[i-1].getX(),racingLinePoints[i-1].getY(),racingLinePoints[i+1].getX(),racingLinePoints[i+1].getY()));
            }
            else  {
                roadData.add(new Turn(racingLinePoints[i-1].getX(),racingLinePoints[i-1].getY(),racingLinePoints[i+1].getX(),racingLinePoints[i+1].getY(),
                        findRadiusAndCenter(racingLinePoints[i-1],racingLinePoints[i],racingLinePoints[i+1])));
            }
        }
    }

    public void currentSegment(){
        float x = currentPos.getX(); float y = currentPos.getY();
        if (currentSegment instanceof Straight){
            Straight seg = (Straight)currentSegment;
            float m = seg.getSlope(); float b = (-m * x) + y;
            float eq = m * x + b;
            if (y > eq - 10 && y < eq + 10){
                changeSegment();
            }
        } else {
            Turn seg = (Turn)currentSegment;
            float r = seg.getRadius(); float h = seg.getCenterX(); float k = seg.getCenterY();
            float eq = (float)Math.pow((double)(x - h),2) + (float)Math.pow((double)(y - k),2);
            float r2 = r * r;
            if (r2 > eq - 10 && r2 < eq + 10){
                changeSegment();
            }
        }
    }

    public void changeSegment(){
        int pos = roadData.indexOf(currentSegment);
        for (int i = 0; i < 2; i++) {
            pos++;
            if (pos == roadData.size()){
                pos = 0;
            }
            if (i == 0){
                currentSegment = roadData.get(pos);
            } else {
                nextSegment = roadData.get(pos);
            }
        }
    }

    public void getDirection(){ //returns the direction of the car from 0 to 180
        steer.changeCurrentPos(currentPos);
        steer.changeCurrentSegment(currentSegment);

        angle = steer.getAngle();
        if (angle == (float)-1){
            stop = true;
        }
    }

    public void getThrottle() { //returns the throttle of the car from 0 to 180
        speed.setCurrentPos(currentPos);
        speed.newSegment(nextSegment);

        throttle = speed.getThrottle();
    }
}
