package org.avphs.image;

import java.util.EnumMap;

interface ImageProcessingInterface {
    int[] getWallHeights();
    int[] getWallTypes();
    int[][] getBoxCoords();
    void loadImage();
}

public class ImageProcessing implements ImageProcessingInterface {

    enum PosterColor {
        RED(0xFF0000, (short)0),
        GREEN(0x00FF00, (short)1),
        BLUE(0x0000FF, (short)2),
        CYAN(0x00FFFF, (short)3),
        MAGENTA(0xFF00FF, (short)4),
        YELLOW(0xFFFF00, (short)5),
        BLACK(0, (short)6),
        GREY1(0x333333, (short)7),
        GREY2(0x666666, (short)8),
        GREY3(0x999999, (short)9),
        GREY4(0xCCCCCC, (short)10),
        WHITE(0xFFFFFF, (short)11);
        final int rgb;
        final short code;
        private PosterColor(int rgb, short code) {
            this.rgb = rgb;
            this.code = code;
        }
    }

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

    static void greyscaleFromChannel(int[] rgbArray, int[] outArray, int channel) {
        switch(channel) {
            case 1:
                for(int i = 0; i < rgbArray.length; i ++) {
                    outArray[i] = combineRGB(getRed(rgbArray[i]), getRed(rgbArray[i]), getRed(rgbArray[i]));
                }
                break;
            case 2:
                for(int i = 0; i < rgbArray.length; i ++) {
                    outArray[i] = combineRGB(getGreen(rgbArray[i]), getGreen(rgbArray[i]), getGreen(rgbArray[i]));
                }
                break;
            case 3:
                for(int i = 0; i < rgbArray.length; i ++) {
                    outArray[i] = combineRGB(getBlue(rgbArray[i]), getBlue(rgbArray[i]), getBlue(rgbArray[i]));
                }
                break;
        }
    }

    static PosterColor posterizePixel(int rgb, int dt) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;
        int rg = red - green;
        int rb = red - blue;
        int bg = blue - green;
        if(rg > dt && rb > dt) {
            return PosterColor.RED;
        }
        else if (rg < -dt && bg < -dt) {
            return PosterColor.GREEN;
        }
        else if (rb < -dt && bg > dt) {
            return PosterColor.BLUE;
        }
        else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
            return PosterColor.YELLOW;
        }
        else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
            return PosterColor.MAGENTA;
        }
        else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
            return PosterColor.CYAN;
        }
        else {
            int avg = (red + green + blue + green) >> 2;
            if(avg < 25) {
                return PosterColor.BLACK;
            }
            else if(avg < 76) {
                return PosterColor.GREY1;
            }
            else if(avg < 127) {
                return PosterColor.GREY2;
            }
            else if(avg < 178) {
                return PosterColor.GREY3;
            }
            else if(avg < 229) {
                return PosterColor.GREY4;
            }
            else {
                return PosterColor.WHITE;
            }

        }
    }

    static void posterizeImage(int[] rgbArray, PosterColor[] outArray, int diffThreshold) {
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            outArray[i] = posterizePixel(rgbArray[i], diffThreshold);
        }

    }

    static void PosterToRGB(PosterColor[] inArray, int[] outArray) {
        for(int i = inArray.length - 1; i >= 0; i --) {
            outArray[i] = inArray[i].rgb;
        }
    }
}