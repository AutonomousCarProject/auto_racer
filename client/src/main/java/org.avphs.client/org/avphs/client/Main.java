package org.avphs.client;

import org.avphs.core.CarModule;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static List<CarModule> modules = CarModule.getInstances();
    static int FPS = 30;

    // FIXME: Badly written code, but it works. Rewrite just about everything in this class.
    public static void main(String[] args) {

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(Main::updateModules,0, 1000 / FPS, TimeUnit.MILLISECONDS);

    }

    private static void updateModules() {
        for (CarModule module : modules) {
        }
    }
}
