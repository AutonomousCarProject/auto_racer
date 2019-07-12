package org.avphs.coreinterface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class holds the data Objects each module requires to run.
 * @author kevin
 */
public class CarData {
    // HashMap that holds each individual object.
    private Map<String, Object> moduleDataMap = new ConcurrentHashMap<>();

    /**
     * getModuleData returns the data object at any specific string index in the
     * moduleDataMap.
     * @param moduleName The key for any object.
     * @return Data from any Module
     */
    public Object getModuleData(String moduleName) {
        return moduleDataMap.get(moduleName);
    }

    /**
     * @param moduleName The key for any module's Data.
     * @param moduleData The data to be added/updated.
     */
    public void initizlizeModule(String moduleName, Object moduleData) {
        moduleDataMap.put(moduleName, moduleData);
    }

}
