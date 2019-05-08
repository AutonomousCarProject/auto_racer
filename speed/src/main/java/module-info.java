module org.avphs.speed {
    requires org.avphs.core;

    exports org.avphs.speed;

    provides org.avphs.core.CarModule with org.avphs.speed.SpeedModule;
}