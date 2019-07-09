interface ImageProcessingInterface {
    int[] getWallHeights();
    int[] getWallTypes();
    int[][] getBoxCoords();
    void loadImage();
}

public class ImageProcessing implements ImageProcessingInterface {

    @Override
    public int[] getWallHeights() {
        return new int[0];
    }

    @Override
    public int[] getWallTypes() {
        return new int[0];
    }

    @Override
    public int[][] getBoxCoords() {
        return new int[0][];
    }

    @Override
    public void loadImage() {

    }

    void blurImage() {

    }

    void posterizeImage() {

    }
}