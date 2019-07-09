module org.avphs.image {
    requires org.avphs.core;
    exports org.avphs.image;
    provides org.avphs.core.CarModule with org.avphs.image.ImageModule;
}