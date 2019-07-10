module org.avphs.position {
    requires org.avphs.core;
    requires org.avphs.image;
    requires org.avphs.map;
    exports org.avphs.position;

    provides org.avphs.core.CarModule with org.avphs.position.PositionModule;
}