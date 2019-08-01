package org.avphs.map;

public class genHardCodedTrack {

    private Map theMap = new Map();

    private final int TRACK_NUMBER = 0;
    //0: Complex Track
    //1: Easy Loop


    public genHardCodedTrack(Map mapMan12345)
    {
        theMap = mapMan12345;
    }

    /*TODO: Calculate locations and equations for track walls and also account for flipped y pos.
    Need longer tape measure to finalize measurements.
    */


    public Map genMap()
    {

        switch (TRACK_NUMBER)
        {
            case 0:
                //200CM width everywhere except during step 5.

                //Step 1 y = 617 {100 <= x <= 794}
                for (int i = 100; i < 795; i++)
                {
                    theMap.setValueAtIndex(i, 617, true);
                }

                //Step 2 x = 981 {350 <= y <= 794}
                for (int i = 350; i < 795; i++)
                {
                    theMap.setValueAtIndex(981, i, true);
                }

                //Step 3 1.701x - 734.326 {794 <= x <= 898}
                for (int i = 794; i < 899; i++)
                {
                    theMap.setValueAtIndex(i, (float)(1.701 * i - 734.326), true);
                }

                //Step 4 2.1325x -1298.012048 {898 <= x <= 981}
                for (int i = 899; i < 982; i++)
                {
                    theMap.setValueAtIndex(i, (float)(2.1325 * i - 1298.012048), true);
                }

                //Step 5 y = 350 {300 <= x <= 981}
                for (int i = 300; i < 982; i++)
                {
                    theMap.setValueAtIndex(i, 350, true);
                }

                //Step 6 y = 450 {300 <= x <= 898}
                for (int i = 300; i < 899; i++)
                {
                    theMap.setValueAtIndex(i, 450, true);
                }

                //Step 7 y = 224 {100 <= x <= 1200}
                for (int i = 100; i < 1201; i++)
                {
                    theMap.setValueAtIndex(i, 224, true);
                }

                //Step 8 x = 898 {450 <= y <= 617}
                for (int i = 450; i < 618; i++)
                {
                    theMap.setValueAtIndex(898, i, true);
                }

                //Step 9 x = 898 {794 <= y <= 910}
                for (int i = 794; i < 911; i++)
                {
                    theMap.setValueAtIndex(898, i, true);
                }

                //Step 10 y = 910 {898 <= x <= 1200}
                for (int i = 898; i < 1201; i++)
                {
                    theMap.setValueAtIndex(i, 910, true);
                }

                //Step 11 x = 1200 {224 <= y <= 910}
                for (int i = 224; i < 911; i++)
                {
                    theMap.setValueAtIndex(1200, i, true);
                }

                //Step 12
                for (int i = 224; i <= 618; i++ )
                {
                    theMap.setValueAtIndex(100, i, true);
                }

                //Step 13
                for (int i = 350; i <= 451; i++)
                {
                    theMap.setValueAtIndex(300, i, true);
                }

                return theMap;
            case 1:
                return theMap;
                default:
                    return theMap;
        }


    }







}
