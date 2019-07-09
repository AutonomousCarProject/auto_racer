package org.avphs.racingline;

import org.java.ArrayList;

import org.avphs.core.CarCommand;
import org.avphs.core.CarModule;
import RacingLinePoint;

public class RacingLineModule implements CarModule {

    @Override
    public Class[] getDependencies() {
        return null;
    }

    @Override
    public void init(CarModule... dependencies) {

    }

    @Override
    public CarCommand[] commands() {
        return null;
    }

    @Override
    public void run() {
        System.out.println("Racing Line");
    }

    private void makeRacingLine(boolean[][] map)
    {
        System.out.println("RacingLine.makeRacingLine not implemented");
    }
    public RacingLine getRacingLine()
    {
        System.out.println("RacingLine.getRacingLine not implemented");
    }
}

public class RacingLine
{
    public ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList();
    public RacingLinePoint[] RacingLinePoints;

    public RacingLine()
    {

    }
}

public class RacingLinePoint
{
    float x, y, degree;


}