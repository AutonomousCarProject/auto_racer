package org.avphs.map;

public class FakeDataStreamForMap {

   // public int CircleSection = 1;
  //  public final double CarSpeed = 10; //100 mm/s (ten index per second) (each square on array "grid" is 10mm x 10mm... for now
    // public final double trackpathCircumference = 18849.555; total distance car will travel
    //car path equation: +/- sqrt( (300)^2 - (x-500)^2 ) + 500
   // public final double WallHeight = 20; //in cm
    private final double pi = 3.14159265;
    public double xPosition;//Starting X Position of Car but will change when car starts moving
    public double yPosition;//Starting Y Position of Car but will change when car starts moving
    private double runningRadianTotal = 0; //RadianPosition on Circle
    private final double stepRadian = Math.toRadians(0.3996);
    private double timeSinceLastUpdate; // = System.currentTimeMillis();
    private final double frameRate = 33.33; //MILLISECONDS EVERY FRAME (1/30)

    public FakeDataStreamForMap()//Default Starting Position
    {
        xPosition = 800;
        yPosition = 500;
    }
    /*
    LIST OF POSSIBLE STARTING POSITIONS
    800
     */
    public FakeDataStreamForMap(double xpos, double ypos)//If we want to change starting position.
    {
        xPosition = xpos;
        yPosition = ypos;
    }
    public void run()
    {
        timeSinceLastUpdate = System.currentTimeMillis();
        while (runningRadianTotal < (2 * pi))
        {
            if (System.currentTimeMillis() < (timeSinceLastUpdate + frameRate))
            {
                updateXPos(); updateYPos(); //Export an Array of Pixel Heights
                timeSinceLastUpdate = System.currentTimeMillis();
            }
        }
        System.out.println("Done.");
    }

    //CENTER OF CIRCLE (a,b): 500,500
    //(a+r, b): (800, 500) (a, b+r): (500,800)
    /* Basic Visualization of Track and Car Path
    Traveling the middle of this circle track at a speed of 30cm/s
    Image team gives us 30 Frames per Second
    Track Inside Wall Radius: 200
    Track Outside Wall Radius: 400
    Car path radius: 300
    Wall Height: 20

                             π/2

                      ,,ggddY""""Ybbgg,,
                 ,agd""'              `""bg,
              ,gdP"                       "Ybg,
            ,dP"                             "Yb,
          ,dP"         _,,ddP"""Ybb,,_         "Yb,
         ,8"         ,dP"'         `"Yb,         "8,
        ,8'        ,d"                 "b,        `8,
       ,8'        d"                     "b        `8,
       d'        d'        ,gPPRg,        `b        `b
       8         8        dP'   `Yb        8         8
   π   8         8        8)     (8        8         8    0 or 2π
       8         8        Yb     dP        8         8
       8         Y,        "8ggg8"        ,P         8
       Y,         Ya                     aP         ,P
       `8,         "Ya                 aP"         ,8'
        `8,          "Yb,_         _,dP"          ,8'
         `8a           `""YbbgggddP""'           a8'
          `Yba                                 adP'
            "Yba                             adY"
              `"Yba,                     ,adP"'
                 `"Y8ba,             ,ad8P"'
                      ``""YYbaaadPP""''
                            3π/2

     */

    //FORMULA TO CALCULATE NEW POINT GIVEN
    //θ : Initial point θ from (a+r , b)
    //Ø : Increase of angle in radians (EVERY 1/30 OF A SECOND CAR MOVES 0.00133% OF THE CIRCUMFERENCE, OR .3996π/180 RADIANS UP ON THE CIRCLE
    //r : radius of circle
    //(a + rcos(θ + Ø) , b + rsin(θ + Ø) ) GIVES US NEW X and Y POSITION ON CAR PATH.
    //or
    //(500 + 300cos(θ + Ø) , 500 + 300sin(θ + Ø) )
    //(500 + 300cos(runningRadianTotal + stepRadian) , 500 + 300sin(runningRadianTotal + stepRadian) )
    //Update Position

    public double updateXPos()
    {
        xPosition = (500 + (300 * (Math.cos(runningRadianTotal + stepRadian))));
        runningRadianTotal += stepRadian;
        return xPosition;
    }
    public double updateYPos()
    {
        yPosition = (500 + (300 * (Math.sin(runningRadianTotal + stepRadian))));
        runningRadianTotal += stepRadian;
        return yPosition;
    }




}
