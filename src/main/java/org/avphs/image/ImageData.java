package org.avphs.image;

public class ImageData {
    public int[] wallTop;
    public int[] wallTypes;
    public int[] wallBottom;

    ImageData(){
        wallTop = new int[640];
        wallTypes = new int[640];
        wallBottom = new int[640];
    }
}