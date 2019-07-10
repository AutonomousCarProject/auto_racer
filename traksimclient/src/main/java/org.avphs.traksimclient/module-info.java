module org.avphs.traksimclient {
    exports org.avphs.traksimclient;
    requires org.avphs.core;
    requires org.avphs.sbcio;
    requires org.avphs.traksim;
    requires java.desktop;
    requires org.avphs.image;
    uses org.avphs.core.CarModule;
}