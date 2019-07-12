module org.avphs.position {
    requires org.avphs.coreinterface;
    exports org.avphs.position;
    provides org.avphs.coreinterface.CarModule with org.avphs.position.PositionModule;
}