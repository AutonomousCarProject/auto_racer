package org.avphs.core;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CarCore {
    private final int FPS = 30;

    public ClientInterface client;

    private Queue<CarCommand> commandQueue = new PriorityQueue<>();
    public void init() {
        var modules = CarModule.getInstances();

        var executorService = Executors.newScheduledThreadPool(modules.size());

        modules.forEach(m -> {
            var tempDeps = m.getDependencies();
            CarModule[] mods = {};
            if (tempDeps != null) {
                var deps = Arrays.asList(tempDeps);
                if (!deps.isEmpty()) {
                    var temp = modules.stream().filter(x -> deps.contains(x.getClass())).toArray();
                    mods = Arrays.copyOf(temp, temp.length, CarModule[].class);
                }
            }
            m.init(mods);
        });
        modules.forEach(m -> executorService.scheduleAtFixedRate(m, 0, Math.round(1000 / FPS), TimeUnit.MILLISECONDS));

        while (true) {
            modules.forEach(m -> commandQueue.addAll(Arrays.asList(m.commands())));
            if (commandQueue.peek() != null) {
                commandQueue.poll().execute();
            }
        }
    }
}
