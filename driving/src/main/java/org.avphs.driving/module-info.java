module org.avphs.driving {
    requires org.avphs.core;

    exports org.avphs.driving;

    provides org.avphs.core.CarModule with org.avphs.driving.DrivingModule;
}
