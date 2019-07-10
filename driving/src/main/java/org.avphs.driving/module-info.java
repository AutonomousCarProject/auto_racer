module org.avphs.driving {
    requires org.avphs.core;
    requires org.avphs.util;
    requires org.avphs.racingline;

    exports org.avphs.driving;

    provides org.avphs.core.CarModule with org.avphs.driving.DrivingModule;
}
