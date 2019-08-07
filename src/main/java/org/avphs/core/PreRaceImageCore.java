package org.avphs.core;

import org.avphs.car.Car;
import org.avphs.coreinterface.CarModule;
import org.avphs.image.ImageModule;
import org.avphs.image.tools.ImageWindowModule;

import java.awt.image.BufferedImage;
import java.util.concurrent.*;

/**
 * PreRaceImageCore is run before the race to create the map.
 * @author clayton
 * @see org.avphs.core.CarCore
 */
public class PreRaceImageCore extends CarCore {

    private CarModule imageModule, imageWindowModule;

    private Executor imageExecutor = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("image"));
    private ScheduledExecutorService moduleRunner = Executors.newSingleThreadScheduledExecutor(
            new NamedThreadFactory("module runner"));

    public PreRaceImageCore(Car car) {
        this(car, null);
    }

    public PreRaceImageCore(Car car, BufferedImage cameraImage) {
        super(car);

        imageModule = new ImageModule();
        imageWindowModule = new ImageWindowModule();
        if (cameraImage != null)
            ((ImageWindowModule) imageWindowModule).useGivenImage(cameraImage);


        // Add Run-time Modules
        updatingCarModules.add(imageModule);
        updatingCarModules.add(imageWindowModule);

        init();
        moduleRunner.scheduleAtFixedRate(this::update, 0, targetMillsPerFrame, TimeUnit.MILLISECONDS);

    }

    private void update() {
        CompletableFuture<Void> futureImage = CompletableFuture
                .runAsync(() -> {
                    car.getCameraImage(carData);
                    imageModule.update(carData);
                }, imageExecutor);

        CompletableFuture
                .runAsync(() -> imageWindowModule.update(carData));


        CompletableFuture<Void> futureMap = futureImage
                .thenAcceptAsync(v -> {});

        CompletableFuture.allOf(futureMap)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                })
                .join();

    }
}
