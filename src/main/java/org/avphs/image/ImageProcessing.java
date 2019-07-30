package org.avphs.image;

@SuppressWarnings("Duplicates")

/**Holds all of the functions for ImageModule
 *
 * @author Joshua Bromley
 * @author Kenneth Browder
 * @author Kevin "Poo" Tran
 * @see ImageModule
 */
public class ImageProcessing{


    public enum PosterColor {
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

    public static final int[] ColorArr = {
            0xFF0000, //RED
            0x00FF00, //GREEN
            0x0000FF, //BLUE
            0x00FFFF, //CYAN
            0xFF00FF, //MAGENTA
            0xFFFF00, //YELLOW
            0,        //BLACK
            0x333333, //GREY1
            0x666666, //GREY2
            0x999999, //GREY3
            0xCCCCCC, //GREY4
            0xFFFFFF  //WHITE
    };


    /**Takes rgb value and extracts red value
     *
     * @param rgb Rgb value (no alpha)
     *
     * @return Red value (0-255)
     */
    static int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    /**Take rgb value and extracts green value
     *
     * @param rgb RGB value (no alpha)
     *
     * @return Green value (0-255)
     */
    static int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    /**Takes rgb value and extracts blue value
     *
     * @param rgb RGB Value (no alpha)
     *
     * @return Blue value (0-255)
     */
    static int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    /**Synthesizes red, green and blue values into one RGB value
     *
     * @param red Red value (0-255)
     * @param green Green value (0-255)
     * @param blue Blue value (0-255)
     *
     * @return RGB value
     */
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

    /**Debayers an image
     *
     * @param bayer 1D array of the bayer image
     * @param width width of image
     * @param height height of image
     * @param tile tiling pattern 0 -> RGGB, 1 -> GBRG
     *
     * @return Debayered image in RGB format
     */
    static int[] debayer(byte[] bayer, int width, int height, int tile) {
        int[] rgb = new int[width * height ];
        switch(tile) {
            case 0:
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int r = (int) bayer[2 * (2 * i * width + j)] & 0xFF;
                        int g = (int) bayer[2 * (2 * i * width + j) + 1] & 0xFF;
                        int b = (int) bayer[2 * ((2 * i + 1) * width + j) + 1] & 0xFF;
                        int pix = (r << 16) + (g << 8) + b;
                        rgb[i * width + j] = pix;
                    }
                }
                break;
            case 1:
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int r = (int) bayer[2 * ((2 * i +1) * width + j)] & 0xFF;
                        int g = (int) bayer[2 * (2 * i * width + j)] & 0xFF;
                        int b = (int) bayer[2 * ((2 * i) * width + j) + 1] & 0xFF;
                        int pix = (r << 16) + (g << 8) + b;
                        rgb[i * width + j] = pix;
                    }
                }
                break;
        }
        return rgb;
    }

    /**Posterizes a pixel. Restricts to one of the colors in PosterColor enum.
     *
     * @param rgb The RGB value of the pixel to posterize.
     * @param dt The difference threshold for which to determine color.
     *
     * @return The enum member corresponding to the posterized color.
     */
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

    /**Posterizes a pixel. Restricts to one of the colors in described in ColorArr.
     *
     * @param rgb The RGB value of the pixel to posterize.
     * @param dt The difference threshold for which to determine color.
     *
     * @return The color code corresponding to the posterized color.
     */
    static int posterizePixelInt(int rgb, int dt) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = (rgb) & 0xFF;
        int rg = red - green;
        int rb = red - blue;
        int bg = blue - green;
        if(rg > dt && rb > dt) {
            return 0;
        } else if (rg < -dt && bg < -dt) {
            return 1;
        } else if (rb < -dt && bg > dt) {
            return 2;
        } else if (rg < dt && rg > -dt && rb > dt && bg < -dt) {
            return 5;
        } else if (rb < dt && rb > -dt && rg > dt && bg > dt) {
            return 4;
        } else if (bg < dt && bg > -dt && rg < -dt && rb < -dt ) {
            return 3;
        } else {
            int avg = (red + green + blue + green) >> 2;
            if(avg < 25) {
                return 6;
            } else if(avg < 76) {
                return 7;
            } else if(avg < 127) {
                return 8;
            } else if(avg < 178) {
                return 9;
            } else if(avg < 229) {
                return 10;
            } else {
                return 11;
            }

        }
    }

    /**Posterizes a pixel based off of the HSL model. Restricts to one of the colors in PosterColor enum.
     *
     * @param rgb The RGB value of the pixel to posterize.
     * @param dt The difference threshold for which to determine color.
     *
     * @return The enum member corresponding to the posterized color.
     */
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

    /**Posterizes a pixel. Restricts to one of the colors in PosterColor enum.
     *
     * @param red The value of the red channel of the pixel (0-255).
     * @param blue The value of the blue channel of the pixel (0-255).
     * @param green The value of the green channel of the pixel (0-255).
     * @param dt The difference threshold for which to determine color.
     *
     * @return The enum member corresponding to the posterized color.
     */
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

    /**Posterizes a pixel based on the HSL color scheme. Restricts to one of the colors in PosterColor enum.
     *
     * @param red The value of the red channel of the pixel (0-255).
     * @param blue The value of the blue channel of the pixel (0-255).
     * @param green The value of the green channel of the pixel (0-255).
     * @param dt The difference threshold for which to determine color.
     *
     * @return The enum member corresponding to the posterized color.
     */
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

    /** Posterizes the entirety of an image, pixel by pixel.
     *
     * @param rgbArray Array of the RGB values of the pixels in the image.
     * @param outArray Array to place the posterized PosterColors.
     * @param diffThreshold The difference threshold with which to determine color.
     *
     */
    static void posterizeImage(int[] rgbArray, PosterColor[] outArray, int diffThreshold) {
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            outArray[i] = posterizePixel(rgbArray[i], diffThreshold);
        }

    }

    /**Converts an array of color codes to an array of RGB values.
     *
     * @param inArray Array of codes for colors that correspond to those in ColorArr.
     * @param outArray Array in which final RGB values should be placed.
     *
     */
    static void CodeToRGB(int[] inArray, int[] outArray) {
        for(int i = inArray.length - 1; i >= 0; i --) {
            outArray[i] = ColorArr[inArray[i]];
        }
    }

    /**Posterizes the entirety of an image, pixel by pixel.
     *
     * @param rgbArray Array of the RGB values of the pixels in the image.
     * @param outArray Array to place the posterized color codes.
     * @param diffThreshold The difference threshold with which to determine color.
     *
     */
    static void posterizeImageInt(int[] rgbArray, int[] outArray, int diffThreshold) {
        for(int i = rgbArray.length - 1; i >= 0; i --) {
            outArray[i] = posterizePixelInt(rgbArray[i], diffThreshold);
        }

    }

    //TrakSim RGGB
    //Camera GBRG

    /**
     * This function does magic (don't question it)
     *
     * @param bayer Bayer array to process (from camera).
     * @param width Width of image.
     * @param height Height of image.
     * @param dt The difference threshold with which to determine color.
     * @param tile Tile pattern of the bayering 0 -> RGGB, 1 -> GBRG
     * @return Array of posterized RGB values.
     */
    static int[] magicloop(byte[] bayer, int width, int height, int dt, int tile) {
        int[] rgb = new int[width * height ];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                int r = 0, g = 0, b = 0;
                switch(tile){
                    case 0:
                        r = (int)bayer[2*(2*i*width+j)] & 0xFF;
                        g = (int)bayer[2*(2*i*width+j)+1] & 0xFF;
                        b = (int)bayer[2*((2*i+1)*width+j)+1] & 0xFF;
                        break;
                    case 1:
                        r = (int)bayer[2*(2*(i+1)*width+j)] & 0xFF;
                        g = (int)bayer[2*(2*i*width+j)] & 0xFF;
                        b = (int)bayer[2*((2*i)*width+j)+1] & 0xFF;
                        break;
                }
                PosterColor posterPix = posterizeChannels(r, g, b, dt);
                rgb[i*width+j] = posterPix.code;
            }
        }
        return rgb;
    }

    /**Method that processes a bayer image into a posterized image
     *
     * @param bayerArray Bayer array to process (from camera).
     * @param width Width of image.
     * @param height Height of image.
     *
     * @return Array of posterized RGB values.
     */
    static int[] process(byte[] bayerArray, int width, int height) {
        return magicloop(bayerArray, width, height, 65, 0);
    }

    /**Blurs the image by doing a weighted average of the 5x5 square of pixels around the target pixel
     *
     * @param inArray input Array with RGB values
     * @param width width of image
     * @param height height of image
     * @return blurred image
     */
    static int[] fastBoxBlur(int[] inArray, int width, int height){
        int[] outArray = new int[height*width];
        for(int i = 2; i < height-2; i++){
            for(int j = 2; j < width-2; j++){
                int rsum = 0, gsum = 0, bsum = 0;
                for(int k = -2; k < 3; k++){
                    for(int l = -2; l < 3; l++){
                        if((k > -2 && k < 2) && (l > -2 && l  < 2) && (k != 0 && j != 0)){
                            rsum += (inArray[(i+k)*width+j+l] >> 16 & 0xFF) << 1;
                            gsum += (inArray[(i+k)*width+j+l] >> 8 & 0xFF) << 1;
                            bsum += (inArray[(i+k)*width+j+l] & 0xFF) << 1;
                        }else{
                            rsum += (inArray[(i+k)*width+j+l] >> 16 & 0xFF);
                            gsum += (inArray[(i+k)*width+j+l] >> 8 & 0xFF);
                            bsum += (inArray[(i+k)*width+j+l] & 0xFF);
                        }
                    }
                }
                rsum >>= 5;
                gsum >>= 5;
                bsum >>= 5;
                outArray[i*width+j] = rsum << 16 | gsum << 8 | bsum;


            }
        }
        return outArray;
    }

}