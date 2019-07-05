package org.avphs.client;

import org.avphs.core.CarModule;

public class Main {

    public static void main(String[] args) {
        var modules = CarModule.getInstances();

        System.out.println(modules.size());

        modules.forEach(CarModule::update);
        System.out.println("FINISHED");
    }
}
