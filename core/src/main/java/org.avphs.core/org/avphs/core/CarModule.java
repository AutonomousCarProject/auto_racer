package org.avphs.core;

import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface CarModule extends Runnable {

    static List<CarModule> getInstances() {
        return ServiceLoader.load(CarModule.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
    }

    Collection<Class> getDependencies();

    void init(CarModule[] dependencies);
    void update();
}
