package org.avphs.image;

import java.util.EnumMap;

interface ImageProcessingInterface {
    int[] getWallHeights();
    int[] getWallTypes();
    int[][] getBoxCoords();
    void loadImage();
}

@SuppressWarnings("Duplicates")
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

    static int[] debayer(byte[] bayer, int width, int height) {
        int[] rgb = new int[width * height ];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int r = (int)bayer[2*(2*i*width+j)] & 0xFF;
                int g = (int)bayer[2*(2*i*width+j)+1] & 0xFF;
                int b = (int)bayer[2*((2*i+1)*width+j)+1] & 0xFF;
                int pix = (r << 16) + (g << 8) + b;
                rgb[i*width+j] = pix;
            }
        }
        return rgb;
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
        } else if (rg < -dt && bg < -dt) {
            return PosterColor.GREEN;
        } else if (rb < -dt && bg > dt) {
            return PosterColor.BLUE;
        } else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
            return PosterColor.YELLOW;
        } else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
            return PosterColor.MAGENTA;
        } else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
            return PosterColor.CYAN;
        } else {
            int avg = (red + green + blue + green) >> 2;
            if(avg < 25) {
                return PosterColor.BLACK;
            } else if(avg < 76) {
                return PosterColor.GREY1;
            } else if(avg < 127) {
                return PosterColor.GREY2;
            } else if(avg < 178) {
                return PosterColor.GREY3;
            } else if(avg < 229) {
                return PosterColor.GREY4;
            } else {
                return PosterColor.WHITE;
            }

        }
    }

    static PosterColor posterizePixelHSL(int rgb, int dt) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;
        int max = red > blue ? red > green ? red : green : blue > green ? blue : green;
        int min = red < blue ? red < green ? red : green : blue < green ? blue : green;
        int delta = max - min;
        int h = 0;
        if(delta == 0){
            h = 0;
        }else if(max == red){
            h = ((green-blue)/delta) % 6;
        }else if(max == green){
            h = (blue - red)/delta + 2;
        }else{
            h = (red - green)/delta + 4;
        }
        h *= 60;
        int l = (max + min) >> 1;
        if(delta > dt){
            if(h > 330 || h < 30){
                return PosterColor.RED;
            }else if(h > 30 && h < 90){
                return PosterColor.YELLOW;
            }else if( h > 90 && h < 150){
                return PosterColor.GREEN;
            }else if(h > 150 && h < 210){
                return PosterColor.CYAN;
            }else if(h > 210 && h < 270){
                return PosterColor.BLUE;
            }else if(h > 270 && h < 330){
                return PosterColor.MAGENTA;
            }
        }else{
            if(l < 43){
                return PosterColor.BLACK;
            }else if(l > 43 && l < 86){
                return  PosterColor.GREY1;
            }else if(l > 86 && l < 129){
                return PosterColor.GREY2;
            }else if(l > 129 && l < 152){
                return PosterColor.GREY3;
            }else if(l > 152 && l < 195){
                return PosterColor.GREY4;
            }else{
                return PosterColor.WHITE;
            }
        }
        return PosterColor.BLACK;
    }

    static PosterColor posterizeChannels(int red, int green, int blue, int dt) {
        int rg = red - green;
        int rb = red - blue;
        int bg = blue - green;
        if(rg > dt && rb > dt) {
            return PosterColor.RED;
        } else if (rg < -dt && bg < -dt) {
            return PosterColor.GREEN;
        } else if (rb < -dt && bg > dt) {
            return PosterColor.BLUE;
        } else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
            return PosterColor.YELLOW;
        } else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
            return PosterColor.MAGENTA;
        } else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
            return PosterColor.CYAN;
        } else {
            int avg = (red + green + blue + green) >> 2;
            if (avg < 25) {
                return PosterColor.BLACK;
            } else if (avg < 76) {
                return PosterColor.GREY1;
            } else if (avg < 127) {
                return PosterColor.GREY2;
            } else if (avg < 178) {
                return PosterColor.GREY3;
            } else if (avg < 229) {
                return PosterColor.GREY4;
            } else {
                return PosterColor.WHITE;
            }
        }
    }

    static PosterColor posterizeChannelsHSL(int red, int green, int blue, int dt) {
        int max = red > blue ? red > green ? red : green : blue > green ? blue : green;
        int min = red < blue ? red < green ? red : green : blue < green ? blue : green;
        int delta = max - min;
        int h = 0;
        if(delta == 0){
            h = 0;
        }else if(max == red){
            h = ((green-blue)/delta) % 6;
        }else if(max == green){
            h = (blue - red)/delta + 2;
        }else{
            h = (red - green)/delta + 4;
        }
        h *= 60;
        int l = (max + min) >> 1;
        if(delta > dt){
            if(h > 330 || h < 30){
                return PosterColor.RED;
            }else if(h > 30 && h < 90){
                return PosterColor.YELLOW;
            }else if( h > 90 && h < 150){
                return PosterColor.GREEN;
            }else if(h > 150 && h < 210){
                return PosterColor.CYAN;
            }else if(h > 210 && h < 270){
                return PosterColor.BLUE;
            }else if(h > 270 && h < 330){
                return PosterColor.MAGENTA;
            }
        }else{
            if(l < 43){
                return PosterColor.BLACK;
            }else if(l > 43 && l < 86){
                return  PosterColor.GREY1;
            }else if(l > 86 && l < 129){
                return PosterColor.GREY2;
            }else if(l > 129 && l < 152){
                return PosterColor.GREY3;
            }else if(l > 152 && l < 195){
                return PosterColor.GREY4;
            }else{
                return PosterColor.WHITE;
            }
        }
        return PosterColor.BLACK;
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

    static int[] magicloop(byte[] bayer, int width, int height, int dt) {
        int[] rgb = new int[width * height ];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int r = (int)bayer[2*(2*i*width+j)] & 0xFF;
                int g = (int)bayer[2*(2*i*width+j)+1] & 0xFF;
                int b = (int)bayer[2*((2*i+1)*width+j)+1] & 0xFF;
                PosterColor posterPix = posterizeChannels(r, g, b, dt);
                rgb[i*width+j] = posterPix.rgb;
            }
        }
        return rgb;
    }
    static int[] process(byte[] bayerArray, int width, int height) {
        return magicloop(bayerArray, width, height, 65);
    }
}