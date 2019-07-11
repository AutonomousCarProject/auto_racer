package org.avphs.image;

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

    static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    static int combineRGB(int red, int green, int blue) {
        return blue + (green << 8) + (red << 16);
    }

    static int posterizePixel(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;
        int rg = red - green;
        int rb = red - blue;
        int bg = blue - green;
        if(rg > dt && rb > dt) {
            return 0xFF0000;
        }
        else if (rg < -dt && bg < -dt) {
            return 0x00FF00;
        }
        else if (rb < -dt && bg > dt) {
            return 0x0000FF;
        }
        else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
            return 0xFFFF00;
        }
        else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
            return 0xFF00FF;
        }
        else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
            return 0x00FFFF;
        }
        else {
            int avg = (red + green + blue + green) >> 2;
            if(avg < 25) {
                return 0;
            }
            else if(avg < 76) {
                return 0x333333;
            }
            else if(avg < 127) {
                return 0x666666;
            }
            else if(avg < 178) {
                return 0x999999;
            }
            else if(avg < 229) {
                return 0xBBBBBB;
            }
            else {
                return 0xFFFFFF;
            }

        }
    }

    static void posterizeImage(int[] rgbArray, int[] outArray, int diffThreshold) {
        int dt = diffThreshold;
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            outArray[i] = posterizePixel(rgbArray[i]);
        }

    }
}