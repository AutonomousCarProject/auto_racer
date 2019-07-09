module org.avphs.position {
    requires org.avphs.core;

    exports org.avphs.racingline;

    provides org.avphs.core.CarModule with org.avphs.racingline.PositionModule;
}