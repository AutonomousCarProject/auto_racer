module org.avphs.racingline {
    requires org.avphs.coreinterface;
    exports org.avphs.racingline;

    provides org.avphs.coreinterface.CarModule with org.avphs.racingline.RacingLineModule;
}