module org.avphs.detection {
    requires org.avphs.core;

    exports org.avphs.detection;

    provides org.avphs.core.CarModule with org.avphs.detection.DetectionModule;
}