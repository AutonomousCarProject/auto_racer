package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarModule;
import org.avphs.driving.prerace.PreRaceDrivingModule;
import org.avphs.image.ImageModule;
import org.avphs.map.MapModule;
import org.avphs.map.MapRacingModule;
import org.avphs.window.WindowModule;

import java.util.concurrent.*;

/**
 * PreRaceCore is run before the race to create the map.
 * @author kevin
 * @see org.avphs.core.CarCore
 */
public class PreRaceCore extends CarCore {

    private CarModule imageModule, mapModule, preRaceDrivingModule, windowModule;

    private Executor imageExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("image"));
    private ScheduledExecutorService moduleRunner = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("module runner"));

    public PreRaceCore(Car car, boolean showWindow) {
        super(car);

        imageModule = new ImageModule();
        mapModule = new MapModule();
        preRaceDrivingModule = new PreRaceDrivingModule(car);


        // Add Run-time Modules
        updatingCarModules.add(imageModule);
        updatingCarModules.add(mapModule);
        updatingCarModules.add(preRaceDrivingModule);

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

        CompletableFuture<Void> futureMap = futureImage
                .thenAcceptAsync(v -> mapModule.update(carData));

        CompletableFuture.allOf(futureMap)
                .thenAccept(v -> preRaceDrivingModule.update(carData))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                })
                .join();
    }
}
