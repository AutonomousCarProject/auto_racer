module org.avphs.position {
    requires org.avphs.core;
    requires org.avphs.map;
    requires org.avphs.image;

    exports org.avphs.position;

    provides org.avphs.core.CarModule with org.avphs.position.PositionModule;
}