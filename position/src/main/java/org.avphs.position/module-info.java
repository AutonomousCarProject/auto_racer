module org.avphs.position {
    requires org.avphs.core;

    exports org.avphs.position;

    provides org.avphs.core.CarModule with org.avphs.position.PositionModule;
}