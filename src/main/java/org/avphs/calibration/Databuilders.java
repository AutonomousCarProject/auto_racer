package org.avphs.calibration;

import java.io.*;

public class Databuilders {
    public static double getradius(int angle) {
        double y = 0; //INSERT THE REGRESSION EQUATION HERE
        y = (73.9897 - 0.236503 * (angle - 7)) / (0.000818775 * (angle - 7) * (angle - 7) + 0.106047) + 81.8369;
        return y;
    }

    public static double getangle(double radius) {
        double y = 0;//INSERT THE REGRESSION EQUATION HERE
        radius *= -1;
        if (radius > 7) {//7 = angle required for straightaway
            y = (0.011452 * radius - 1.1767736 - Math.pow(-0.000347256 * radius * radius + 0.29926 * radius - 22.109, 0.5)) / (2 * (0.000819 * radius - 0.0670244211));
        } else {
            y = (-0.011452 * radius - 1.1767736 + Math.pow(-0.000347256 * radius * radius - 0.29926 * radius - 22.109, 0.5)) / (2 * (-0.000819 * radius - 0.0670244211));

        }

        return y;
    }

        //
        //Delete the /* on 29 and */ on 148 and run this file to write data to AngleData, DistanceCalculations, MaxSpeeds, and RadiiData
        //


   /* public static void main(String[] args) {
        Writer writer = null;
        int datagrab_int = 0;//temp value holder
        float g = 981; //cm/s^2; everything is measured in cm and s
        int[] v = new int[808];
        int[] vnaught = new int[808];
        double[] mu = {0.275, 0.3125, 0.729166667, 0.5885416667};//PUT COEFFICIENTS OF FRICTION HERE
        double[] radiusgrab = new double[78];

        for (int i = 0; i <= 77; i++) {//fill radius array
            radiusgrab[i] = (int) Math.round(getradius(i - 33));
            if(i < 40){
                radiusgrab[i]*=-1;
            }
        }
        File final_filetasy_I = new File("src\\main\\java\\org\\avphs\\calibration\\AngleData.txt");
        File final_filetasy_II = new File("src\\main\\java\\org\\avphs\\calibration\\DistanceCalculations.txt");
        File final_filetasy_III = new File("src\\main\\java\\org\\avphs\\calibration\\MaxSpeeds.txt");
        File final_filetasy_IV = new File("src\\main\\java\\org\\avphs\\calibration\\RadiiData.txt");
        //<-------------files correspond alphabetically in filepath
        try {
            writer = new BufferedWriter(new FileWriter(final_filetasy_I));//open writer
            writer.write("78\n");//put length of array at top of file
            for (int i = 0; i <= 77; i++) {//-33 is min steer angle, 44 is max steer angle
                datagrab_int = (int) Math.round(getangle(radiusgrab[i]));//plug in
                writer.write(i + " " + datagrab_int + "\n");//write
            }
        } catch (IOException e) { //if there's an error, print it
            e.printStackTrace();
        } finally {
            try {
                writer.close(); //seek to close the file, if unable to, throw error
            } catch (Exception e) {

            }
        }
        //End Angledata
        //Begin DistanceCalculations
        try {
            writer = new BufferedWriter(new FileWriter(final_filetasy_II));//write, etc.
            for (int i = 0; i <= 807; i++) {//min speed 0, top speed 4035, delta 5 (cm/s)
                //initialize values for v and vnaught
                vnaught[i] = i * 5;//multiplication by five is necessary such that the array is filled in increments of 5 from 0 to 4035
                v[i] = i * 5;
            }
            writer.write(mu.length + "\n" + vnaught.length + "\n" + v.length + "\n");
            for (int k = 0; k < mu.length; k++) { //cycles through mus
                for (int j = 0; j < vnaught.length; j++) { //cycles through vs
                    for (int i = 0; i < v.length; i++) { //cycles through vnaughts
                        datagrab_int = (int) Math.round((vnaught[j] + v[i]) * (vnaught[j] - v[i]) / (2 * mu[k] * g));//Use formula such that distance is positive
                        if (vnaught[j] > v[i]) {
                            writer.write(datagrab_int + " "); //if the final v is less than the initial v (i.e. car's slowing down,) write it
                        } else {
                            writer.write(0 + " ");//if the car is actually speeding up, write a 0
                        }
                    }
                    writer.write("\n");//after going through each combination of initial velocity i and every ending velocity, go to the next initial velocity
                }
            }
        } catch (IOException e) {//exceptions etc. like before
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {

            }
        }//End DistanceCalculations
        //Begin MaxSpeeds
        try {
            writer = new BufferedWriter(new FileWriter(final_filetasy_III));
            writer.write(mu.length + "\n" + radiusgrab.length + "\n");
            radiusgrab[40] = 0;
            for (int j = 0; j < mu.length; j++) {
                for (int i = 0; i < radiusgrab.length; i++) {
                    if(radiusgrab[i] < 0){
                        radiusgrab[i]*=-1; //negative radius breaks square root
                    }
                    int vrad = (int) Math.round(Math.pow(mu[j] * g * radiusgrab[i], 0.5)); //do formula
                    if(radiusgrab[i]==0){
                        vrad = 4035; //if the radius is 0, that's a straight line. Go as fast as possible if that's so
                    }
                    writer.write(vrad + " ");//write value
                }
                writer.write("\n");//if gone through every v, go to the next mu
            }
        } catch (IOException e) {//exceptions etc.
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {

            }
        }
        //End MaxSpeeds
        //Begin RadiiData
        try {
            writer = new BufferedWriter(new FileWriter(final_filetasy_IV));
            writer.write(radiusgrab.length + "\n");
            for (int i = 0; i <= 77; i++) {
                if(i<40){
                    radiusgrab[i]*=-1;//reverse the effects from MaxSpeeds
                }
                datagrab_int = (int)radiusgrab[i];//round
                    writer.write(datagrab_int+" ");//write value

            }
        } catch (IOException e) {//exceptions etc.
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {

            }
        }
    }

    */
}

