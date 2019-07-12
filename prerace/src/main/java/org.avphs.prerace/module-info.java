module org.avphs.prerace {
    requires org.avphs.coreinterface;
    exports org.avphs.prerace;
    provides org.avphs.coreinterface.CarModule with org.avphs.prerace.PreRaceModule;
}