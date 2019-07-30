module org.avphs.position {
    requires org.avphs.coreinterface;
    requires org.avphs.calibration;
    exports org.avphs.position;
    provides org.avphs.coreinterface.CarModule with org.avphs.position.PositionModule;
}