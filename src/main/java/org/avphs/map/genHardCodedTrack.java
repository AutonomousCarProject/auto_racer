package org.avphs.map;

public class genHardCodedTrack {

    private Map theMap = new Map();

    public genHardCodedTrack(Map mappy)
    {
        theMap = mappy;
    }

    /*TODO: Get accurate measurements (all numbers right now are inaccurate) and fill in constants by lunch on Tuesday.
    Need longer tape measure to finalize measurements.
    */
    public final int TRACK_WIDTH = 60;

    public final int X_DIM = 1463;
    public final int Y_DIM = 1037;

    public final int START_X = 322;
    public final int START_Y = 279;

    //Track Structure Cons

    //First Straightaway travels on parallel to the X-Axis
    public final int FIRST_STRAIGHTAWAY_LENGTH = 900;

    //First turn is left turn which looks like a fourth of a circle, starting at the end of the first straightaway
    //Starting location of the turn is (300 + FIRST_STRAIGHTAWAY_LENGTH, 279)
    public final int FIRST_TURN_RADIUS = 0;

    //Second Straightaway starts at the end of the first turn, travelling parallel to the y-axis.
    public final int SECOND_STRAIGHTAWAY_LENGTH = 549;

    //Second turn is a left turn which looks like a semicircle, starting at the end of the second straightaway.
    public final int SECOND_TURN_RADIUS = 0;

    //Third turn is a right turn which looks like a fourth of a circle, beginning at the end of the second turn.
    public final int THIRD_TURN_RADIUS = 0;

    //Third Straightaway starts at the end of the first turn, travelling parallel to the x-axis.
    public final int THIRD_STRAIGHTAWAY_LENGTH = 0;

    //Fourth turn is a left turn which begins at the end of the third straight away, taking you back to the starting position on the track.
    public final int FOURTH_TURN_RADIUS = 0;

    public void genMap()
    {
        //These VARS are used to make the track building a little easier to follow. They are the end pos of the latest draw.
        int endingXPosOfLastDraw; int endingYPosOfLastDraw;
        endingXPosOfLastDraw = (START_X + FIRST_STRAIGHTAWAY_LENGTH); endingYPosOfLastDraw = START_Y;

        //Generate walls along first straightaway
        for (int i = START_X; i < (START_X + FIRST_STRAIGHTAWAY_LENGTH + 1); i++)
        {
            theMap.setValueAtIndex(i, START_Y + TRACK_WIDTH, true);
            theMap.setValueAtIndex(i, START_Y - TRACK_WIDTH, true);

        }

        //Generate walls along first turn



        //Generate walls along second straightaway

        //Generate walls along second turn

        //Generate walls along third turn

        //Generate walls along third straightaway

        //Generate walls along fourth turn
    }








}
