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

    static void posterizeImage(int[] rgbArray, int[] outArray, int diffThreshold) {
        int dt = diffThreshold;
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            int current = rgbArray[i];
            int red = getRed(current);
            int green = getGreen(current);
            int blue = getBlue(current);
            int rg = red - green;
            int rb = red - blue;
            int bg = blue - green;
            if(rg > dt && rb > dt) {
                outArray[i] = combineRGB(255,0,0);
            }
            else if (rg < -dt && bg < -dt) {
                outArray[i] = combineRGB(0,255,0);
            }
            else if (rb < -dt && bg > dt) {
                outArray[i] = combineRGB(0,0,255);
            }
            else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
                outArray[i] = combineRGB(255,255,0);
            }
            else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
                outArray[i] = combineRGB(255,0,255);
            }
            else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
                outArray[i] = combineRGB(0,255,255);
            }
            else {
                int avg = (int)((red + green + blue) * 0.333);
                if(avg < 25) {
                    outArray[i] = combineRGB(0,0,0);
                }
                else if(avg < 76) {
                    outArray[i] = combineRGB(51,51,51);
                }
                else if(avg < 127) {
                    outArray[i] = combineRGB(102,102,102);
                }
                else if(avg < 178) {
                    outArray[i] = combineRGB(153,153,153);
                }
                else if(avg < 229) {
                    outArray[i] = combineRGB(204,204,204);
                }
                else {
                    outArray[i] = combineRGB(255,255,255);
                }

            }
        }

    }
}