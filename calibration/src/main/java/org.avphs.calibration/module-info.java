module org.avphs.calibration {
    requires org.avphs.coreinterface;
    exports org.avphs.calibration;

    provides org.avphs.coreinterface.CarModule with org.avphs.calibration.CalibrationModule;
}