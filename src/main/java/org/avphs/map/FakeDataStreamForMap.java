package org.avphs.map;

public class FakeDataStreamForMap {

    // public int CircleSection = 1;
    //  public final double CarSpeed = 10; //100 mm/s (ten index per second) (each square on array "grid" is 10mm x 10mm... for now
    // public final double trackpathCircumference = 18849.555; total distance car will travel
    //car path equation: +/- sqrt( (300)^2 - (x-500)^2 ) + 500
    // public final double WallHeight = 20; //in cm
    //private final double pi = 3.14159265;
    public float xPosition;//Starting X Position of Car but will change when car starts moving
    public float yPosition;//Starting Y Position of Car but will change when car starts moving
    public double runningRadianTotal = 0; //RadianPosition on Circle
    private final float stepRadian = (float)Math.toRadians(0.3996);//radians each step
    private double timeSinceLastUpdate; // = System.currentTimeMillis();
    private final double frameRate = 33.33; //MILLISECONDS EVERY FRAME (1/30)
    public int[] bottomOuterWallHeights = new int[] {0, 330, 329, 328, 288, 288, 288, 288, 288, 288, 288, 288, 287, 286, 286, 286, 286, 285, 283, 281, 280, 277, 276, 274, 272, 270, 268, 266, 254, 254, 254, 254, 254, 254, 254, 254, 254, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 253, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 464, 464, 464, 464, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 254, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 258, 259, 259, 259, 259, 259, 259, 259, 259, 259, 259, 259, 259, 260, 260, 260, 260, 260, 260, 260, 260, 260, 260, 261, 261, 261, 261, 261, 261, 261, 261, 261, 261, 261, 262, 262, 262, 262, 262, 262, 262, 262, 262, 262, 262, 263, 263, 263, 263, 263, 263, 263, 263, 263, 263, 263, 264, 264, 264, 264, 264, 264, 264, 264, 264, 264, 264, 265, 265, 265, 265, 265, 265, 265, 265, 265, 266, 266, 266, 266, 266, 266, 266, 266, 266, 266, 267, 267, 267, 267, 267, 268, 268, 268, 268, 268, 268, 268, 268, 269, 269, 269, 269, 269, 269, 269, 269, 270, 270, 270, 270, 270, 271, 271, 271, 271, 271, 271, 271, 271, 272, 272, 272, 272, 272, 272, 273, 273, 273, 273, 273, 273, 273, 274, 274, 274, 274, 274, 274, 274, 275, 275, 275, 275, 275, 275, 275, 276, 276, 276, 276, 276, 277, 277, 277, 277, 277, 277, 277, 277, 278, 278, 278, 278, 278, 278, 279, 279, 279, 279, 279, 279, 280, 280, 280, 280, 280, 280, 280, 281, 281, 281, 281, 281, 281, 281, 282, 282, 282, 282, 282, 282, 282, 283, 283, 283, 283, 283, 283, 283, 284, 284, 284, 284, 284, 284, 284, 284, 285, 285, 285, 285, 285, 285, 285, 285, 286, 286, 286, 286, 286, 287, 287, 287, 287, 288, 288, 288, 288, 288, 288, 288, 288, 288, 288, 289, 289, 289, 289, 290, 290, 290, 290, 290, 290, 290, 290, 290, 291, 291, 291, 291, 291, 292, 292, 292, 292, 293, 293, 293, 293, 293, 294, 294, 294, 294, 294, 295, 295, 295, 295, 295, 296, 296, 296, 296, 296, 296, 296, 296, 296, 296, 297, 297, 297, 297, 297, 297, 298, 298, 298, 298, 298, 299, 299, 299, 299, 299, 299, 301, 301, 301, 301, 301, 301, 302, 302, 302, 302, 302, 302, 302, 303, 303, 303, 303, 303, 303, 304, 305, 305, 305, 305, 305, 305, 306, 306, 306, 306, 306, 306, 306, 307};
    //Array above is bottom wall heights we will get from image team. Image res is 640x480 for now.
    public float angle;
    public boolean done = false;
    public boolean mapshown = false;

    public FakeDataStreamForMap()//Default Starting Position
    {
        xPosition = 800;
        yPosition = 500;
    }
    /*
    LIST OF POSSIBLE STARTING POSITIONS
    800
     */
    public void updatePos ()
    {
            if (runningRadianTotal < (2 * Math.PI))
            {
                timeSinceLastUpdate = System.currentTimeMillis();
                if (System.currentTimeMillis() < (timeSinceLastUpdate + frameRate))
                {
                    System.out.println(runningRadianTotal);
                    runningRadianTotal += stepRadian;
                    updateXPos(); updateYPos();
                    //Export data from image.
                    timeSinceLastUpdate = System.currentTimeMillis();
                }
            }
            else
            {
                done = true;
            }
            System.out.println(xPosition + "," + yPosition);
            System.out.println("Percent Done: " + (runningRadianTotal / (2*Math.PI)) + "%");
    }


    public FakeDataStreamForMap(double xpos, double ypos)//If we want to change starting position.
    {
        xPosition = (float)xpos;
        yPosition = (float)ypos;
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
    //(500 + 300cos(θ + Ø) , 500 + 300sin(θ + Ø) ) =[
    //(500 + 300cos(runningRadianTotal + stepRadian) , 500 + 300sin(runningRadianTotal + stepRadian) )
    //Update Position

    public double updateXPos()
    {
        xPosition = (float)(500 + (300 * (Math.sin(runningRadianTotal))));
        return xPosition;
    }
    public double updateYPos()
    {

        yPosition = (float)(500 + (300 * (Math.cos(runningRadianTotal))));
        return yPosition;

    }
    public float[] returnPos()
    {
        float[] pos = new float[2];
        pos[0] = xPosition; pos[1] = (yPosition);
        return pos;
    }




}
