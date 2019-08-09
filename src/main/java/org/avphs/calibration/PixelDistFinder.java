
package org.avphs.calibration;

import org.avphs.coreinterface.CarData;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageData;

public class PixelDistFinder {
    public final static int[] base_x = {100};//todo: put distance to measured wall in cm here (ideally these should all be the same)
    public final static int[] base_y = {100};//todo: put height of measured wall in cm here (ideally these should all be the same)
    public final static int[] base_upsilon = {4};//todo: put height of measured wall in px here

    public static int getdistance_cm(CarData carData) {//uses an equation to grab distance to wall in centimeter
        ImageData image = (ImageData) carData.getModuleData("image");//access updated magicloop in ImageModule from CarData

        //x = distance to measured wall in cm, y = height of measured wall in cm, xi = distance to measured wall in px upsilon = height of measured wall in px
        //y=m(x-a)+b, y=x, x=upsilon, m=cm/px or base_y/base_upsilon, a = base_upsilon, b = base_x
        //equation: x = base_y/base_upsilon*(upsilon-base_upsilon)+base_x

        //NOTE: Remove the dummy data and uncomment the image.wallclones if actually being used.

        int[] walltops = {0,1,2,3,4,5};//image.wallTop.clone(); //take wallTop
        int[] wallbottoms = {4,5,6,7,8,9};//image.wallBottom.clone(); //take wallBottom
        boolean Onetypeofwall = true;
        for (int i = 0; i < walltops.length - 1; i++) {
            if (wallbottoms[i] - walltops[i] != wallbottoms[i + 1] - walltops[i + 1]) {
                Onetypeofwall = false; //check if only one type of wall is being evaluated (same height)
            }
            if (Onetypeofwall == false) {
                System.out.println("Error: more than one type of wall found");
                break;
            }
        }
        int x = -1;
        if (Onetypeofwall == true) {
                int upsilon = wallbottoms[0] - walltops[0];
                int wallType = 1;
                //!!!!!wallType = ... waiting for walltypeidentification
            System.out.println("BASE UPSILON: "+ upsilon); //ONLY USE THIS LINE IF BASE_UPSILON NEEDS TO BE FOUND
            switch (wallType){
                case 1: wallType = 0;
                    break;
                case 2: wallType = 1;
                    break;
                case 3: wallType = 2;
                break;
                case 4: wallType = 3;
                break;
                case 5: wallType = 4;
                break;
                case 6: wallType = 5;
                break;
                case 7: wallType = 6;
                break;
                case 8: wallType = 7;
                break;
                case 9: wallType = 8;
                break;
                case 10: wallType = 9;
                break;
            }
            x = Math.round(base_y[wallType] / base_upsilon[wallType] * (upsilon - base_upsilon[wallType]) + base_x[wallType]);//use formula to grab x
        }
        return x;
    }
    /*public static void main(String[] args){
        int y = 0;
        CarData carData = new CarData();
        y = getdistance_cm(carData);
        System.out.println("x: "+y);
    }*/

}
