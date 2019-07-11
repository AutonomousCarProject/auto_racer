module org.avphs.image {
    requires org.avphs.core;
    requires org.avphs.camera;
    exports org.avphs.image;
    provides org.avphs.core.CarModule with org.avphs.image.ImageModule;
}