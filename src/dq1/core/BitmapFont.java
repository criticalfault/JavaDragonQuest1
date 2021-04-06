package dq1.core;

import static dq1.core.Settings.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * BitmapFont class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class BitmapFont {
    
    public static BufferedImage bitmapFontImage;
    public static BufferedImage[] letters;
    
    public static int letterWidth;
    public static int letterHeight;
    public static int letterVerticalSpacing = 0;
    public static int letterHorizontalSpacing = 0;

    static {
        loadFont(RES_BITMAP_FONT_IMAGE, 16, 16);
    }
    
    public static void drawText(Graphics2D g, String text, int col, int row) {
        if (letters == null) {
            return;
        }
        int x = col * letterWidth;
        int y = row * letterHeight;
        int px = 0;
        int py = 0;
        for (int i=0; i<text.length(); i++) {
            int c = text.charAt(i);
            Image letter = letters[c];
            g.drawImage(letter, (int) (px + x), (int) (py + y), null);
            px += letterWidth + letterHorizontalSpacing;
        }
    }

    private static void loadFont(String res, Integer cols, Integer rows) {
        bitmapFontImage = Resource.getImage(res);
        loadFont(bitmapFontImage, cols, rows);
    }
    
    private static void loadFont(
            BufferedImage image, Integer cols, Integer rows) {
        
        int lettersCount = cols * rows; 
        bitmapFontImage = image;
        letters = new BufferedImage[lettersCount];
        letterWidth = bitmapFontImage.getWidth() / cols;
        letterHeight = bitmapFontImage.getHeight() / rows;

        for (int y=0; y<rows; y++) {
            for (int x=0; x<cols; x++) {
                letters[y * cols + x] = new BufferedImage(
                        letterWidth, letterHeight, BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g = (Graphics2D) letters[y * cols + x].getGraphics();
                g.drawImage(bitmapFontImage, 0, 0, letterWidth, letterHeight
                        , x * letterWidth, y * letterHeight
                        , x * letterWidth + letterWidth
                        , y * letterHeight + letterHeight, null);
            }
        }
    }
    
}
