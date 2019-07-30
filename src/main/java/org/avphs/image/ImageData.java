package org.avphs.image;

/**Holds image data for carData
 *
 * @author Joshua Bromley
 * @author Kenneth Browder
 * @author Kevin "Poo" Tran
 * @see ImageModule
 * @see org.avphs.coreinterface.CarData
 */
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