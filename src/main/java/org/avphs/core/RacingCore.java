package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarModule;
import org.avphs.detection.ObjectDetectionModule;
import org.avphs.driving.DrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;
import org.avphs.map.MapRacingModule;
import org.avphs.position.PositionModule;
import org.avphs.racingline.RacingLineModule;
import org.avphs.window.WindowModule;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * RacingCore is run during the race.
 *
 * @author kevin
 * @see org.avphs.core.CarCore
 */
public class RacingCore extends CarCore {

    private CarModule imageModule;
    //private CarModule positionModule;
    private CarModule objectDetectionModule;
    private CarModule racingLineModule;
    private CarModule drivingModule;
    private CarModule windowModule;
    private CarModule mapModule;

    private Executor imageExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("image"));
    private Executor positionExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("position"));
    private Executor objectDetectExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("object detection"));
    private ScheduledExecutorService moduleRunner = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("module runner"));

    public RacingCore(Car car, boolean showWindow) {
        super(car);
        car.accelerate(true, 0);
        car.steer(true, 0);
        imageModule = new ImageModule();
        //positionModule = new PositionModule();
        objectDetectionModule = new ObjectDetectionModule();
        racingLineModule = new RacingLineModule();
        drivingModule = new DrivingModule(car);
        mapModule = new MapRacingModule();


        // Image - No one.
        // Position- Image
        // ObjectDetection - Image, Pos, Static Map.
        // RacingLineModule - Obstacle, Mapping. Only when obj
        // DrivingModule - Pos, RL, Static Calibration.

        updatingCarModules.add(imageModule);
        //updatingCarModules.add(positionModule);
        updatingCarModules.add(objectDetectionModule);
        updatingCarModules.add(racingLineModule);
        updatingCarModules.add(drivingModule);
        updatingCarModules.add(mapModule);

        if (showWindow) {
            windowModule = new WindowModule();
            updatingCarModules.add(windowModule);
        }

        init();
        moduleRunner.scheduleAtFixedRate(this::update, 0, targetMillsPerFrame, TimeUnit.MILLISECONDS);
    }

    private void update() {

        CompletableFuture<Void> futureImage = CompletableFuture
                .runAsync(() -> {
                    car.getCameraImage(carData);
                    imageModule.update(carData);
                }, imageExecutor);

        if (windowModule != null) {
            CompletableFuture
                    .runAsync(() -> windowModule.update(carData));
        }

        //CompletableFuture<Void> futurePosition = futureImage
                //.thenAcceptAsync(v -> positionModule.update(carData), positionExecutor);

        CompletableFuture<Void> objDetection = futureImage
                .thenAcceptAsync(v -> {
                    objectDetectionModule.update(carData);
                    if (true) {
                        racingLineModule.update(carData);
                    }
                }, objectDetectExecutor);

        CompletableFuture.allOf(objDetection)
                .thenAccept(v -> {
                    drivingModule.update(carData);
                    car.update(carData);
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                })
                .join();
    }
}
