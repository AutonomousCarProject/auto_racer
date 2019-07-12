module org.avphs.map {
    requires org.avphs.coreinterface;
    requires org.avphs.image;
    requires org.avphs.position;
    requires java.desktop;
    exports org.avphs.map;
    provides org.avphs.coreinterface.CarModule with org.avphs.map.MapModule;

}