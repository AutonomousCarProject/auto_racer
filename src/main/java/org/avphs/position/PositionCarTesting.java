package org.avphs.position;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PositionCarTesting {

    private BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("position_car_testing_data.txt"));
    private int strLength = 16;

    public PositionCarTesting() throws IOException {
        String[] vars = new String[]{"Position", "Direction", "Odometer", "WheelAngle"};
        for (int i = 0; i < vars.length; i++) {
            while (vars[i].length() < strLength) {
                vars[i] += " ";
            }
        }
        for (String var : vars) {
            bufferedWriter.write(var);
        }
    }

    public void writeToFile(float posX, float posY, float direction, int odomCount, int steerAngle) throws IOException {
        bufferedWriter.newLine();
        StringBuilder posStr = new StringBuilder("(" + Math.round(posX) + ", " + Math.round(posY) + ")");
        while (posStr.length() < strLength) {
            posStr.append(" ");
        }
        bufferedWriter.write(posStr.toString());

        StringBuilder dirStr = new StringBuilder(Math.round(direction));
        while (dirStr.length() < strLength) {
            dirStr.append(" ");
        }
        bufferedWriter.write(dirStr.toString());

        StringBuilder odomStr = new StringBuilder(odomCount);
        while (odomStr.length() < strLength) {
            odomStr.append(" ");
        }
        bufferedWriter.write(odomStr.toString());

        bufferedWriter.write(steerAngle);
    }

    public void close() throws IOException {
        bufferedWriter.close();
    }
}
