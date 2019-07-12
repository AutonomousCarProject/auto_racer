module org.avphs.image {
    requires org.avphs.coreinterface;
    exports org.avphs.image;

    provides org.avphs.coreinterface.CarModule with org.avphs.image.ImageModule;
}