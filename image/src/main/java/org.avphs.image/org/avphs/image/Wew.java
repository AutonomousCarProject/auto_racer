package org.avphs.image;

import org.avphs.image.fsmgeneration.Table;

public class Wew {
    public static void main(String[] args) throws Exception {
        ImageProcessing.PosterColor[] colors = new ImageProcessing.PosterColor[13];

        System.arraycopy(
                ImageProcessing.PosterColor.values(), 
                0, 
                colors, 
                0, 
                ImageProcessing.PosterColor.values().length
        );
        colors[12] = ImageProcessing.PosterColor.GREEN;
        
        Table wew = TomFsmFileFormatParser.parseFile(
                "C:\\Users\\TEST\\IdeaProjects\\auto_racer\\image\\src\\main\\java\\org.avphs.image\\org\\avphs\\image\\BetterTubeWallFSM.txt",
                "1",
                colors
        );
        
        wew.generateImage();
    }
}
