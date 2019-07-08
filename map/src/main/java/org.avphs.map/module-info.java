module org.avphs.map {
    requires org.avphs.core;

    exports org.avphs.map;

    provides org.avphs.core.CarModule with org.avphs.map.MapModule;
}