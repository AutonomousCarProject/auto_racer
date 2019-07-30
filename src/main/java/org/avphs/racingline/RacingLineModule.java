package org.avphs.racingline;

import org.avphs.coreinterface.CarCommand;
import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.detection.ObjectData;
import org.avphs.detection.Obstacle;

import java.util.*;
import java.io.*;

public class RacingLineModule implements CarModule {
    private boolean[][] map;
    private boolean[][] walls;
    private HashSet<Point> allWalls = new HashSet<Point>();
    private static ArrayList<Point> outerWall = new ArrayList<Point>();
    private static ArrayList<Point> innerWall = new ArrayList<Point>();
    private boolean addToOuter;

    private int rows, columns;
    private int[] dx = {-1, 0, 1, 0,1,-1,-1,1};
    private int[] dy = {0, 1, 0, -1,1,1,-1,-1};

    private boolean[][] visited;
    private boolean[][] added;

    private RacingLine center = new RacingLine();

    private ObjectData obstacles = new ObjectData();

    //region Overrides
    @Override
    public void init(CarData carData){
        try {
            //Convert a text file of 1's and 0's to a 2D boolean array used for RacingLine calculations
            BufferedReader bufread = new BufferedReader(new FileReader("testmap.txt"));
            StringTokenizer st = new StringTokenizer(bufread.readLine());

            rows = Integer.parseInt(st.nextToken());
            columns = Integer.parseInt(st.nextToken());

            boolean[][] testMap = new boolean[rows][columns];
            walls = new boolean[rows][columns];
            for (int i = 0; i < rows; i++) {
                String currentRow = bufread.readLine();
                for (int j = 0; j < columns; j++) {
                    testMap[i][j] = currentRow.charAt(j) == '1';
                }
            }
            bufread.close();

            System.out.println("Racing line init");
            System.out.println("Rows: " + rows + ", columns: " + columns);

            //Add a dummy RacingLine to cardata with the test map
            makeRacingLine(testMap);
            carData.addData("racingLine", center.getRacingLinePoints());
        } catch(IOException e) {
            e.printStackTrace();
        }

        carData.addData("RacingLine", center);
    }

    public void update(CarData carData) {
        //System.out.println("Passing update");

        //Update the detected obstacles for passing
        obstacles = (ObjectData) carData.getModuleData("detection");

        //Passing calculations
        //obstacle.update();
        //CheckPassingLine();

        //Update the cardata RacingLine
        carData.addData("RacingLine", center);
    }
    //endregion

    //region Map
    /**
     * Makes the map
     * @param _map
     */
    private void MakeMap(boolean[][] _map) {
        this.map = _map;
        rows = map.length;
        columns = map[0].length;
        walls = new boolean[rows][columns];
    }

    /**
     * Returns an ArrayList of Point that makes up the Inner Wall
     * @return
     */
    public static ArrayList<Point> getInnerWall() {
        return innerWall;
    }

    /**
     * Returns an ArrayList of Point that makes up the Outer Wall
     * @return
     */
    public static ArrayList<Point> getOuterWall() {
        return outerWall;
    }

    /**
     *
     * @param walls
     * @return
     */
    public boolean[][] getMapFromWalls(boolean[][] walls) {
        int wr = walls.length;
        int wc = walls[0].length;

        boolean[][] outermap = new boolean[wr][wc];
        boolean[][] track = new boolean[wr][wc];
        boolean[][] map = new boolean[wr][wc];

        outermap[0][0] = true;

        Queue<Point> q = new LinkedList<Point>();
        q.add(new Point(0,0));
        while(!q.isEmpty()) {
            Point c = q.remove();

            for(int k = 0; k < 4; k++) {
                int tx = c.x + dx[k];
                int ty = c.y + dy[k];

                if(tx >= 0 && tx < wr && ty >= 0 && ty < wc) {
                    if(!walls[tx][ty] && !outermap[tx][ty]) {
                        outermap[tx][ty] = true;
                        q.add(new Point(tx, ty));
                    }
                }
            }
        }
        q.clear();

        int minx = wr+1;
        int miny = wc+1;

        o:
        for(int i = 0; i < wr; i++) {
            for(int j = 0; j < wc; j++) {
                if(!outermap[i][j] && !walls[i][j]) {
                    minx = i;
                    miny = j;
                    break o;
                }
            }
        }

        q.add(new Point(minx,miny));
        track[minx][miny] = true;
        while(!q.isEmpty()) {
            Point c = q.remove();
            for(int k = 0; k < 4; k++) {
                int tx = c.x + dx[k];
                int ty = c.y + dy[k];

                if(tx >= 0 && tx < wr && ty >= 0 && ty < wc) {
                    if(!walls[tx][ty] && !track[tx][ty]) {
                        track[tx][ty] = true;
                        q.add(new Point(tx,ty));
                    }
                }
            }
        }

        for(int i=0;i<wr;i++) {
            for(int j=0;j<wc;j++) {
                map[i][j] = track[i][j];
            }
        }

        return map;
    }

    /**
     * Determines which walls are outer walls and which ones are inner walls.
     */
    private void getWalls() {
        visited = new boolean[rows][columns];
        added = new boolean[rows][columns];

        addToOuter = true;

        int mini = Integer.MAX_VALUE;
        int somej = -1;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                addonce:
                for(int k = 0; k < 4; k++) {
                    int tx = i + dx[k];
                    int ty = j + dy[k];

                    if (tx >= 0 && tx < rows && ty >= 0 && ty < columns) {
                        if(!map[i][j] && map[tx][ty]) {
                            walls[i][j] = true;
                            allWalls.add(new Point(i,j));

                            if(i < mini) {
                                mini = i;
                                somej = j;
                            }
                            break addonce;
                        }
                    }
                }
            }
        }

        BFS(mini,somej);

        addToOuter = false;
        int imini = innerWall.get(0).x;
        int isomej = innerWall.get(0).y;
        innerWall.clear();
        BFS(imini, isomej);

        //System.out.println("OUTER WALL: "+outerWall.size());
        //System.out.println("INNER WALL: "+innerWall.size());
    }

    /**
     * Search through all connected walls.
     * @param startx
     * @param starty
     */
    private void BFS(int startx, int starty) {
        Queue<Point> states = new LinkedList<Point>();
        states.add(new Point(startx, starty));
        visited[startx][starty] = true;
        boolean first = true;

        while (!states.isEmpty()) {
            Point currentPoint = states.remove();
            int x = currentPoint.x;
            int y = currentPoint.y;
            int times = 0;

            for (int i = 0; i < 8; i++) {
                int tx = x + dx[i];
                int ty = y + dy[i];

                if (tx >= 0 && tx < rows && ty >= 0 && ty < columns) {
                    if (walls[tx][ty] == true && added[x][y] == false) {
                        added[x][y] = true;
                        Point newPoint = new Point(x, y);

                        if(addToOuter) {
                            outerWall.add(newPoint);
                        } else {
                            innerWall.add(newPoint);
                        }

                        allWalls.remove(newPoint);
                    }
                    if (walls[tx][ty] == true && visited[tx][ty] == false) {
                        if(first && times >= 1) {
                            continue;
                        }

                        times++;
                        states.add(new Point(tx, ty));
                        visited[tx][ty] = true;
                    }
                }
            }
            first = false;
        }
        if(addToOuter) {
            innerWall.addAll(allWalls);
        }
    }

    /**
     * Closes the track so the car doesn't get too close to the walls
     * @param dist
     */
    public void closeTrack(int dist) {
        boolean[][] newmap = new boolean[rows][columns];
        for(int dist1 = 0; dist1 < dist; dist1++) {
            for(int i = 0; i < rows; i++) {
                for(int j = 0; j < columns; j++) {
                    if(!map[i][j]) {
                        continue;
                    }

                    newmap[i][j] = map[i][j];
                    for(int k = 0; k < 4; k++) {
                        int tx = i + dx[k];
                        int ty = j + dy[k];

                        if(tx >= 0 && tx < rows && ty >= 0 && ty < columns) {
                            if(!map[tx][ty]) {
                                newmap[i][j] = false;
                                break;
                            }
                        }
                    }
                }
            }

            for(int i = 0; i < rows; i++) {
                for(int j = 0; j < columns; j++) {
                    map[i][j] = newmap[i][j];
                }
            }
        }
    }
    //endregion

    //region RacingLine
    /**
     * Returns a RacingLine object that represents the racing line. Returns null if the racing line has not yet been created through makeRacingLine.
     *
     * @return A RacingLine object that contains an array of RacingLinePoint objects that represent the racing line.
     * //@see void makeRacingLine(boolean[][])
     * @see RacingLine
     */
    public RacingLine getRacingLine() {
        if (center == null) {
            System.out.println("Warning: Racing line has not yet been created. To create a racing line, run getMiddleLine");
        }
        return center;
    }

    /**
     * This method creates the racing line. This should be run before getRacingLine is called.
     *
     * @param _map The map of the track represented by a 2d boolean array.
     * @see RacingLine getRacingLine()
     */
    public void makeRacingLine(boolean[][] _map) {
        MakeMap(_map);
        getMiddleLine();
    }

    /**
     * Gets the middle line - line between outer wall and inner wall
     */
    private void getMiddleLine() {
        //getMapFromWalls
        closeTrack(10);
        getWalls();

        //Generate a RacingLine
        calcMiddleLine();

        //Reduce the number of data points
        center.sortPoints();
        trimSortedPoints(20);

        //Calculate the angles between every point on the line
        makeOriginal();
        getAngles();

        //Smooth the line
        //connectTheDots();
    }

    /**
     * Calculates the middle line between outer wall and inner wall
     */
    private void calcMiddleLine() {
        //Determine which set of walls is longer vs shorter
        ArrayList<Point> longer = outerWall.size() > innerWall.size() ? outerWall : innerWall;
        ArrayList<Point> shorter = outerWall.size() <= innerWall.size() ? outerWall : innerWall;

        int start = 0;
        int lsize = longer.size();
        int ssize = shorter.size();
        int range = ssize;
        //int times = 0;

        for(int i = 0; i < lsize; i++) {
            int closePoint = -1;
            float dist = Float.MAX_VALUE;

            //Find the closest point in the smaller wall
            for(int k = start; k < start + range; k++) {
                int j = k % ssize;
                float testDist = distanceBetweenPoints(longer.get(i),shorter.get(j));

                //Check if the current wallpoint is closer than the previously picked point
                if(testDist < dist && intersect(longer.get(i), shorter.get(j)) <= 5) {
                    closePoint = j;
                    dist = testDist;
                }
            }

            //Check if a point was found
            if(closePoint >= 0) {
                //Make a RacingLinePoint based on the starting point on the wall and the closest point on the opposite wall
                RacingLinePoint newpoint = midPoint(longer.get(i), shorter.get(closePoint));
                newpoint.setOuter(outerWall.get(i));
                newpoint.setInner(innerWall.get(closePoint));
                newpoint.setOriginal(true);
                center.addPoint(newpoint);

                //Update the starting point and range
                start = (closePoint + ssize - ssize / 10) % ssize;
                range = ssize / 5;
                //times++;
            } else {
                start = 0;
                range = ssize;
            }
        }
        //System.out.println("CALC MIDDLE LINE TIMES: " + times);
    }

    /**
     * Determines the midpoint of two points (on the wall)
     * @param outer
     * @param inner
     * @return
     */
    private RacingLinePoint midPoint(Point outer, Point inner) {
        //Average out the positions of the points and create a new RacingLinePoint
        float aveX = (float) ((outer.x + inner.x) / 2.0);
        float aveY = (float) ((outer.y + inner.y) / 2.0);
        return new RacingLinePoint(aveX, aveY);
    }

    /**
     * Trims sorted points. Leaves points if they are farther than distance or have to pass through walls.
     * @param trim
     */
    public void trimSortedPoints(float trim) {
        RacingLinePoint[] line = center.getRacingLinePoints();
        ArrayList<RacingLinePoint> compressedLine = new ArrayList<RacingLinePoint>();

        //Start the trim at the first point in the array
        RacingLinePoint p = line[0];
        compressedLine.add(p);

        int previous = 0;
        for(int i = 1; i < line.length; i++) {
            RacingLinePoint p2 = line[i];
            Point q1 = new Point(Math.round(p.getX()), Math.round(p.getY()));
            Point q2 = new Point(Math.round(p2.getX()), Math.round(p2.getY()));

            //Adds the next point in the line if it goes through a wall
            if (intersect(q1, q2) > 0) {
                p = line[i - 1];
                compressedLine.add(line[i - 1]);
                if(i-1 != previous) i--;
                previous = i;
            }
            //Adds the next point in the line if it is further than @param trim allows
            if(distanceBetweenPoints(p,p2) >= trim) {
                p = line[i];
                compressedLine.add(p);
            }
        }

        center.setRacingLinePointsList(compressedLine);
    }

    /**
     * Smoothly connects points in the racing line.
     * These new points should NOT be original.
     */
    private void connectTheDots() {
        RacingLinePoint[] array = center.getRacingLinePoints();
        int size = array.length;
        CurvePoint[] curves = new CurvePoint[size];
        ArrayList<RacingLinePoint> connected = new ArrayList<>();

        //Convert center to curvepoint array
        for(int i = 0; i < size; i++) {
            curves[i] = array[i].toCurvePoint();
        }

        //Calculate next/last for the new curvepoint and initialize values
        for(int i = 0; i < size; i++) {
            int next = (i + 1) % size;
            int last = (i + size - 1) % size;

            //Initialize the values in curves
            CurvePoint c = curves[i];
            c.setNext(curves[next]);
            c.setPrevious(curves[last]);
            c.start();
        }

        float cux, cuy;
        float iter;

        for(int i = 0; i < size; i++) {
            CurvePoint c = curves[i];
            CurvePoint next = curves[(i + 1) % size];
            cux = c.getX();
            cuy = c.getY();

            connected.add(array[i]);

            //translate and rotate
            float px = (float)Math.sqrt((next.getX() - c.getX()) * (next.getX() - c.getX()) +
                    (next.getY() - c.getY()) * (next.getY() - c.getY()));
            float cosrotateangle = (next.getX() - c.getX()) / px;
            float sinrotateangle = (c.getY() - next.getY()) / px;
            if(px < 2.5f) {
                continue;
            }

            iter = (float)1/px;

            float odx = c.getTargetX()*cosrotateangle - c.getTargetY()*sinrotateangle;
            float ody = c.getTargetX()*sinrotateangle + c.getTargetY()*cosrotateangle;
            float pdx = next.getTargetX()*cosrotateangle - next.getTargetY()*sinrotateangle;
            float pdy = next.getTargetX()*sinrotateangle + next.getTargetY()*cosrotateangle;
            float oslope = 1;
            float pslope = 1;
            if(Math.abs(odx) > 0.01) oslope = ody/odx;
            if(Math.abs(pdx) > 0.01) pslope = pdy/pdx;

            if(Math.abs(oslope) < 0.0001) oslope = 0;
            if(Math.abs(pslope) < 0.0001) pslope = 0;

            float stopx = 0;
            float stopy = 0;
            if((oslope >= 0 && pslope >= 0)||(oslope <= 0 && pslope <= 0)) {
                stopx = 0.5f*Math.abs(pslope)/(Math.abs(pslope)+Math.abs(oslope))+0.25f;
                stopy = (-1*oslope*stopx+pslope*stopx-pslope);
            } else {
                stopx = Math.abs(pslope)/(Math.abs(pslope)+Math.abs(oslope));
                stopy = 0;
            }
            if(oslope==0 && pslope==0) {
                stopx = 0;
                stopy = 0;
            }
            if(stopx<0||stopx>1) System.out.println("UH OH THERE IS AN ERROR");
            for(float t=0;t<1;t+=iter) {
                float tt = (t>=0.5f)?t+iter:t;
                float dx = 1;
                float dy = 0;
                if(tt>=stopx) {
                    dy = (pslope - stopy) * (tt - 1) / (1 - stopx) + pslope;
                } else {
                    dy = (stopy - oslope)*(tt/stopx) + oslope;
                }
                float rdx = dx*cosrotateangle + dy*sinrotateangle;
                float rdy = -1*dx*sinrotateangle + dy*cosrotateangle;
                cux += rdx;
                cuy += rdy;
                connected.add(new RacingLinePoint((int)cux,(int)cuy));
            }
        }
        for(int i=0;i<connected.size();i++) {
            int j = (i+1)%connected.size();
            RacingLinePoint c = connected.get(i);
            RacingLinePoint d = connected.get(j);
            if(c.getX() == d.getX() && c.getY() == d.getY()) {
                if(d.getOriginal()) {
                    connected.remove(i);
                    continue;
                }
                connected.remove(j);
                i--;
            }
        }
        center.setRacingLinePointsList(connected);
    }

    /**Merges multiple RacingLines into one
     *
     * @param curves
     * @return
     */
    private RacingLine combineRacingLines (RacingLine[] curves) {
        RacingLine combinedCurves = new RacingLine();

        for(int i = 0; i < curves.length; i++) {
            for(int j = 0; j < curves[i].getRacingLinePoints().length; j++) {
                combinedCurves.addPoint(curves[i].getRacingLinePoints()[j]);
            }
        }

        return combinedCurves;
    }
    //endregion

    //region Passing
    /**
     * Call when passing
     * @return
     */
    public RacingLine CheckPassingLine() {
        //Don't run any calculations if there are no objects
        if (obstacles.getObstacles().size() == 0) {
            return center;
        }

        //Unsmooth the line
        //removeUnoriginal();
        pass(obstacles.getObstacles().get(0));
        getAngles();

        //Resmooth the line
        //connectTheDots();

        return center;
    }

    /**
     * Passing code; shifts points towards wall when near object
     * @param obstacle
     */
    private void pass(Obstacle obstacle) {
        removeUnoriginal();
        RacingLinePoint[] line = center.getRacingLinePoints();
        int o1x = (int)obstacle.getCorners()[0].x;
        int o1y = (int)obstacle.getCorners()[0].y;
        int o2x = (int)obstacle.getCorners()[obstacle.getCorners().length-1].x;
        int o2y = (int)obstacle.getCorners()[obstacle.getCorners().length-1].y;
        float threshold = 90;
        int bob = 0;
        float mindist = 1000000;
        for(RacingLinePoint c: line) {
            float dist1 = (float)Math.sqrt((c.getX()-o1x)*(c.getX()-o1x)+(c.getY()-o1y)*(c.getY()-o1y));
            float dist2 = (float)Math.sqrt((c.getX()-o2x)*(c.getX()-o2x)+(c.getY()-o2y)*(c.getY()-o2y));
            float boxcx = (o1x+o2x)/2;
            float boxcy = (o1y+o2y)/2;
            float dist3 = (float)Math.sqrt((c.getInner().x-boxcx)*(c.getInner().x-boxcx)+(c.getInner().y-boxcy)*(c.getInner().y-boxcy));
            float dist4 = (float)Math.sqrt((c.getOuter().x-boxcx)*(c.getOuter().x-boxcx)+(c.getOuter().y-boxcy)*(c.getOuter().y-boxcy));
            if(dist1<mindist||dist2<mindist) {
                mindist = Math.min(dist1, dist2);
                if(dist3 < dist4) bob = 1;
                else bob = -1;
            }
        }
        for(RacingLinePoint c: line) {
            float dist1 = (float)Math.sqrt((c.getX()-o1x)*(c.getX()-o1x)+(c.getY()-o1y)*(c.getY()-o1y));
            float dist2 = (float)Math.sqrt((c.getX()-o2x)*(c.getX()-o2x)+(c.getY()-o2y)*(c.getY()-o2y));
            if(dist1 < threshold || dist2 < threshold) {
                c.setPass(true);
                float t = 0.5f;
                //change this
                t+=bob*(0.4-0.2*(dist1/threshold)-0.2*(dist2/threshold));
                c.setPassX(t*c.getOuter().x+(1-t)*c.getInner().x);
                c.setPassY(t*c.getOuter().y+(1-t)*c.getInner().y);
            } else {
                c.setPass(false);
            }
        }
    }

    /**
     * Sets every point in the Racing Line to original
     */
    private void makeOriginal() {
        RacingLinePoint[] array = center.getRacingLinePoints();
        for(RacingLinePoint c: array) {
            c.setOriginal(true);
        }
    }

    /**
     * Removes all points from the Racing Line that are not original
     */
    private void removeUnoriginal() {
        RacingLinePoint[] array = center.getRacingLinePoints();
        ArrayList<RacingLinePoint> original = new ArrayList<RacingLinePoint>();

        for(RacingLinePoint c: array) {
            if(c.getOriginal()) {
                original.add(c);
            }
        }
        center.setRacingLinePointsList(original);
    }
    //endregion

    //region Basic Calculations
    public static int floor(int num, int den) {
        return (int) -Math.ceil((double) -num / den);
    }
    public static int ceiling(int num, int den) {
        return (int) Math.ceil((double) num / den);
    }

    /**
     * Finds the number of intersections of the line segment p1 p2 and walls.
     * @param p1
     * @param p2
     * @return
     */
    public int intersect(Point p1, Point p2) {
        int num = 0;
        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;
        boolean horizontal;
        horizontal = (Math.abs(y2 - y1) <= Math.abs(x2 - x1));
        if ((horizontal && x1 > x2) || (!horizontal && y1 > y2)) {
            int swap = x2;
            x2 = x1;
            x1 = swap;
            swap = y2;
            y2 = y1;
            y1 = swap;
        }
        if (horizontal) {
            for (int j = x1; j <= x2; j++) {
                int xstep = j - x1;
                int ystep = (y2 - y1) * xstep;
                int ya = floor(ystep, x2 - x1) + y1;
                int yb = ceiling(ystep, x2 - x1) + y1;
                if (j >= 0 && ya >= 0 && j < rows && ya < columns) {
                    if (!map[j][ya]) {
                        num++;
                    }
                }
                if (j >= 0 && yb >= 0 && j < rows && yb < columns) {
                    if (!map[j][yb]) {
                        num++;
                    }
                }
                if (num > 5)
                    return 6;
            }
        } else {
            for (int j = y1; j <= y2; j++) {
                int ystep = j - y1;
                int xstep = (x2 - x1) * ystep;
                int xa = floor(xstep, y2 - y1) + x1;
                int xb = ceiling(xstep, y2 - y2) + x1;
                if (j >= 0 && xa >= 0 && j < rows && xa < columns) {
                    if (!map[xa][j])
                        num++;
                }
                if (j >= 0 && xb >= 0 && j < rows && xb < columns) {
                    if (!map[xa][j])
                        num++;
                }
                if (num > 5)
                    return 6;
            }
        }
        return num;
    }
    //Converts RacingLinePoints to Points and calls intersect()
    public int intersect(RacingLinePoint p1, RacingLinePoint p2) {
        return intersect(new Point(p1.getIntX(), p1.getIntY()), new Point(p1.getIntX(), p2.getIntY()));
    }

    /**
     * Calculates the distance between multiple points through the Pythagorean Theorem
     * @param start
     * @param end
     * @return
     */
    public static float distanceBetweenPoints(Point start, Point end) {
        int x = Math.abs(end.x - start.x);
        int y = Math.abs(end.y - start.y);
        float h = (float) Math.sqrt(x * x + y * y);
        return h;
    }
    public static float distanceBetweenPoints(RacingLinePoint start, RacingLinePoint end) {
        return distanceBetweenPoints(new Point(start.getIntX(), start.getIntY()), new Point(end.getIntX(), end.getIntY()));
    }

    public static float distance(float a, float b, float c, float d) {
        return (float) Math.sqrt((c - a) * (c - a) + (d - b) * (d - b));
    }
    //endregion

    //region List/Array things
    /**
     * Converts an ArrayList of Point to an array of Point
     * @param l
     * @return
     */
    public static Point[] PointListToArray(ArrayList<Point> l) {
        Point[] points = new Point[l.size()];

        for(int i = 0; i < l.size(); i++) {
            points[i] = l.get(i);
        }
        return points;
    }

    /**
     * Converts an ArrayList of RacingLinePoint to an array of RacingLinePoint
     * @param l
     * @return
     */
    public static RacingLinePoint[] RacingLinePointListToArray(ArrayList<RacingLinePoint> l) {
        RacingLinePoint[] points = new RacingLinePoint[l.size()];

        for(int i = 0; i < l.size(); i++) {
            points[i] = l.get(i);
        }
        return points;
    }

    /**
     * Check if a RacingLinePoint list has a specific point
     * @param list
     * @param point
     * @return
     */
    public static boolean ContainsPoint(ArrayList<RacingLinePoint> list, RacingLinePoint point) {
        for (RacingLinePoint p: list) {
            if (p == point) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the closest point in a list to a specified point
     * @param pointList
     * @param point
     * @return
     */
    public static Point ClosestPoint(Point[] pointList, Point point) {
        int closestPoint = 0;
        float closestDist = Float.MAX_VALUE;

        for (int p = 0; p < pointList.length; p++) {
            if (distanceBetweenPoints(pointList[p], point) < closestDist) {
                closestDist = distanceBetweenPoints(pointList[p], point);
                closestPoint = p;
            }
        }

        return pointList[closestPoint];
    }
    public static RacingLinePoint ClosestPoint(RacingLinePoint[] pointList, RacingLinePoint point) {
        int closestPoint = 0;
        float closestDist = Float.MAX_VALUE;

        for (int p = 0; p < pointList.length; p++) {
            if (distanceBetweenPoints(pointList[p], point) < closestDist) {
                closestDist = distanceBetweenPoints(pointList[p], point);
                closestPoint = p;
            }
        }
        return pointList[closestPoint];
    }
    //endregion

    private void getAngles() {
        RacingLinePoint[] array = center.getRacingLinePoints();
        for(int i=0;i<array.length;i++) {
            int j = (i+1)%array.length;
            int h = (i+array.length-1)%array.length;
            RacingLinePoint c = array[i];
            RacingLinePoint d = array[j];
            RacingLinePoint b = array[h];
            float ax = d.getX() - c.getX();
            float ay = d.getY() - c.getY();
            float bx = b.getX() - c.getX();
            float by = b.getY() - c.getY();
            float dot = ax*bx+ay*by;
            float cosangle = dot/((float)Math.sqrt(ax*ax+ay*ay))/((float)Math.sqrt(bx*bx+by*by));
            float angle = (float)Math.acos(cosangle);
            angle *= (180f/Math.PI);
            c.setDegree(angle);
        }
    }
}