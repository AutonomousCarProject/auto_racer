module org.avphs.window {
    requires org.avphs.core;
    requires java.desktop;
    exports org.avphs.window;

    provides org.avphs.core.CarModule with org.avphs.window.WindowTMP;
}