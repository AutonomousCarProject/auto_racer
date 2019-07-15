module org.avphs.driving {
    requires org.avphs.coreinterface;
    requires org.avphs.util;
    requires org.avphs.racingline;
    requires org.avphs.calibration;

    exports org.avphs.driving;

    provides org.avphs.coreinterface.CarModule with org.avphs.driving.DrivingModule;
}
