package org.avphs.core;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

public class CarCore {
    private final int FPS = 30;

    private Queue<CarCommand> commandQueue = new PriorityQueue<>();
    public void init() {
        var modules = CarModule.getInstances();

        var executorService = Executors.newScheduledThreadPool(modules.size());

        modules.forEach(m -> {
            var deps = m.getDependencies();
            CarModule[] mods = {};
            if (deps != null && !deps.isEmpty()) {
                var temp = modules.stream().filter(x -> deps.contains(x.getClass())).toArray();
                mods = Arrays.copyOf(temp, temp.length, CarModule[].class);
            }
            m.init(mods);
        });
        modules.forEach(m -> executorService.scheduleAtFixedRate(m, 0, Math.round(1000 / FPS), TimeUnit.MILLISECONDS));

        run();
    }

    private void run() {
        while (true) {
            if (commandQueue.peek() != null) {
                commandQueue.poll().execute();
            }
        }
    }

}
