package org.avphs.carclient;

import org.avphs.client.CarClient;
import org.avphs.core.CarModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarStart implements CarClient {

    public CarStart()
    {
        var modules = CarModule.getInstances();

        var executorService = Executors.newFixedThreadPool(modules.size());

        modules.forEach(executorService::submit);
    }

    public static void main(String[] args) {
        new CarStart();
    }
}
