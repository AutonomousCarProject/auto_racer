package org.avphs.core;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface CarModule {

    static List<CarModule> getInstances() {

        return ServiceLoader.load(CarModule.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
    }

    void update();

}