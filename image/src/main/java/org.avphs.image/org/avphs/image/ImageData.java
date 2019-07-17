package org.avphs.image;

public class ImageData {
    public int[] wallHeights;
    public int[] wallTypes;
    public int[] wallPosition;

    ImageData(){
        wallHeights = new int[640];
        wallTypes = new int[640];
        wallPosition = new int[640];
    }
}
