module org.avphs.window {
    requires org.avphs.coreinterface;
    requires java.desktop;
    requires org.avphs.camera;
    exports org.avphs.window;

    provides org.avphs.coreinterface.CarModule with org.avphs.window.WindowModule;
}
