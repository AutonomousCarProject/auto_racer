import org.avphs.prerace.PreRaceModule;

module org.avphs.prerace {
    requires org.avphs.core;

    exports org.avphs.prerace;

    provides org.avphs.core.CarModule with PreRaceModule;
}