package org.avphs.image;

public class ImageData {
    public boolean processingImage;
    public int[] wallTop;
    public int[] wallTypes;
    public int[] wallBottom;

    ImageData(){
        processingImage = false;
        wallTop = new int[640];
        wallTypes = new int[640];
        wallBottom = new int[640];
    }
}