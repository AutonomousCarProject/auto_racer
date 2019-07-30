package org.avphs.calibration;

import fly2cam.FlyCamera;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm;
import org.avphs.camera.Camera;
import org.avphs.camera.SimCamera;
import org.avphs.car.Car;
import org.avphs.core.CalibrationCore;
import org.avphs.coreinterface.CarData;
import org.avphs.image.ImageData;
import org.avphs.sbcio.ArduinoData;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MoveForward {

    public MoveForward() {

    }

    public static void main(String[] args) {
        System.out.println("Start");
        //Camera cam = new FlyCamera();
        Camera cam = new SimCamera();
        Car car = new Car(cam);
        CalibrationCore core = new CalibrationCore(car, true);
        ImageData imageData = (ImageData) core.getCarData().getModuleData("image");

        ArrayList<Double> wallHeights = new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<>();
        ArduinoData data;
        double dist = 100;

        car.accelerate(true, 12);
        car.steer(true, 0);
        try (PrintWriter writer = new PrintWriter(/*"src\\main\\java\\org\\avphs\\calibration\\*/"PixelData.txt")) {
            System.out.println("good");
            while (true) {
                data = (ArduinoData) core.getCarData().getModuleData("arduino");
                dist -= data.getOdomCount() * CalibrationModule.CM_PER_ROTATION;
                wallHeights.add((double) imageData.wallBottom[320] - imageData.wallTop[320]);
                distances.add(dist);
                if (dist < 1) {
                    break;
                }
            }
            System.out.println("done driving");
            car.stop();

            writer.println(wallHeights.size());
            writer.print("\n");

            double[] wallHeightsArray = new double[wallHeights.size()];
            double[] distancesArray = new double[distances.size()];
            for (int i = 0; i < wallHeights.size(); i++) {
                wallHeightsArray[i] = wallHeights.get(i);
                distancesArray[i] = distances.get(i);
            }
            PolynomialFunctionLagrangeForm interp = new PolynomialFunctionLagrangeForm(wallHeightsArray, distancesArray);

            for (double i = 0; i < wallHeights.size(); i++) {
                writer.print(' ');
                writer.print(interp.value(i));
            }
        } catch (IOException e) {
            while (true) {
                data = (ArduinoData) core.getCarData().getModuleData("arduino");
                dist -= data.getOdomCount() * CalibrationModule.CM_PER_ROTATION;
                wallHeights.add((double) imageData.wallBottom[320] - imageData.wallTop[320]);
                distances.add(dist);
                if (dist < 1) {
                    break;
                }
            }
            car.stop();

            for (int i = 0; i < wallHeights.size(); i++) {
                System.out.print("Wall Heights:\n");
                System.out.print(wallHeights);
                System.out.print("\n\nDistances from wall:\n");
                System.out.print(distances);
            }
        }
    }
}