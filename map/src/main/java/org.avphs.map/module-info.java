module org.avphs.map {
    requires org.avphs.core;
    requires org.avphs.image;

    exports org.avphs.map;

    provides org.avphs.core.CarModule with org.avphs.map.MapModule;
}