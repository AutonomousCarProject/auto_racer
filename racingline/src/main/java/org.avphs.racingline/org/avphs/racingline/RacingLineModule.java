package org.avphs.racingline;

import org.avphs.core.CarCommand;
import org.avphs.core.CarCommandType;
import org.avphs.core.CarModule;
import java.util.ArrayList;


import java.util.ArrayList;

public class RacingLineModule implements CarModule {
    private RacingLine racingLine;

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

    /**
     * This method creates the racing line. This should be run before getRacingLine is called.
     *
     * @param map The map of the track represented by a 2d boolean array.
     */
    public void makeRacingLine(boolean[][] map) {
        System.out.println("RacingLine.makeRacingLine not implemented");
    }

    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     */
    public RacingLine getRacingLine() {
        System.out.println("RacingLine.getRacingLine not implemented");
        return null;
    }


    public class RacingLine {
        public ArrayList<RacingLinePoint> RacingLinePointsList = new ArrayList<RacingLinePoint>();
        public RacingLinePoint[] RacingLinePoints;

        public RacingLine() {
        }
    }

    //region Classes
    class WallPoint {
        int x, y;

        public WallPoint() {
            x = 0;
            y = 0;
        }

        public WallPoint(int _x, int _y) {
            x = _x;
            y = _y;
        }
    }
//endregion
}
