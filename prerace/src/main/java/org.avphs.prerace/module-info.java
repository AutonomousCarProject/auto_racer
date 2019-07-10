module org.avphs.prerace {
    requires org.avphs.core;
    requires org.avphs.image;

    exports org.avphs.prerace;

    provides org.avphs.core.CarModule with org.avphs.prerace.PreRaceModule;
}