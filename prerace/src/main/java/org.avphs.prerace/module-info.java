module org.avphs.prerace {
    requires org.avphs.core;

    exports org.avphs.prerace;

    provides org.avphs.core.CarModule with org.avphs.prerace.PreRaceModule;
}