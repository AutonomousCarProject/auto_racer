package org.avphs.image;

public class ImageData {
    int[] wallHeights;
    int[] wallTypes;
    int[] wallPosition;

    ImageData(){
        wallHeights = new int[640];
        wallTypes = new int[640];
        wallPosition = new int[640];
    }
}
