module org.avphs.steering {
    requires org.avphs.core;

    exports org.avphs.steering;

    provides org.avphs.core.CarModule with org.avphs.steering.SteeringModule;
}