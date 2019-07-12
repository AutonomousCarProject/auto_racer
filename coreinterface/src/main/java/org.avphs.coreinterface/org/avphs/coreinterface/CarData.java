package org.avphs.coreinterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarData {
    private Map<String, Object> moduleDataMap = new ConcurrentHashMap<>();

    public Object getModuleData(String moduleName) {
        return moduleDataMap.get(moduleName);
    }

    public void initizlizeModule(String moduleName, Object moduleData) {
        moduleDataMap.put(moduleName, moduleData);
    }
}
