package dq1.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author admin
 */
public class Credits {

    static String msg = "\n\n" +
"Original Credits\n" +
"\n" +
"\n" +
"\n" +
"Dragon Warrior Staff:\n" +
"\n" +
"\n" +
"\n" +
"Scenario Written by:     \n" +
"\n" +           
"   Yuji Horii\n" +
"\n" +           
"\n" +           
"\n" +           
"Character Designed by:   \n" + 
"\n" +
"   Akira Toriyama\n" +
"\n" +           
"\n" +           
"\n" +           
"Music Composed by:       \n" + 
"\n" +
"   Koichi Sugiyama\n" +
"\n" +           
"\n" +           
"\n" +           
"Programmed by:           \n" + 
"\n" +
"   Koichi Nakamura\n" +
"   Koji Yoshida\n" +
"   Takenori Yamamori\n" +
"\n" +           
"\n" +           
"\n" +           
"CG Designed by:          \n" + 
"\n" +
"   Takashi Yasuno\n" +
"\n" +           
"\n" +           
"\n" +           
"Scenario Assisted by:    \n" + 
"\n" +
"   Hiroshi Miyaoka\n" +
"\n" +           
"\n" +           
"\n" +           
"Assisted by:             \n" + 
"\n" +
"   Rika Suzuki\n" +
"   Tadashi Fukuzawa\n" +
"\n" +           
"\n" +           
"\n" +           
"Special Thanks to:       \n" + 
"\n" +
" Kazuhiko Torishima\n" +
"\n" +
"\n" +
"\n" +
"Translation Staff:\n" +
"\n" +
"\n" +
"\n" +
"Translated by:           \n" + 
"\n" +
"   Toshiko Watson\n" +
"\n" +
"\n" +
"\n" +
"Revised Text by:         \n" + 
"\n" +
"   Scott Pelland\n" +
"\n" +
"\n" +
"\n" +
"Technical Support by:    \n" + 
"\n" +
"   Doug Baker\n" +
"\n" +
"\n" +
"\n" +
"Programmed by:           \n" + 
"\n" +
"   Kenichi Masuta\n" +
"   Manabu Yamana\n" +
"\n" +
"\n" +
"\n" +
"CG Designed by:          \n" + 
"\n" +
"   Satoshi Fudaba\n" +
"\n" +
"\n" +
"\n" +
"Special Thanks to:       \n" + 
"\n" +
"   Howard Phillips\n" +
"\n" +
"\n" +
"\n" +
"Directed by:             \n" + 
"\n" +
"   Koichi Nakamura\n" +
"\n" +
"\n" +
"\n" +
"Produced by:             \n" + 
"\n" +
"   Yukinobu Chida\n" +
"\n" +
"\n" +
"\n" +
"Java PC version by:       \n" + 
"\n" +
"   O.L.\n" +
"\n" +
"\n" +
"\n" +
"Based on DRAGON QUEST\n" +
"\n" +
"\n" +
"\n" +
"Copyright\n" +
"\n" +
"\n" +
"\n" +
"Armor Project    1986-1989\n" +
"Bird Studio      1986-1989\n" +
"Koichi Sugiyama  1986-1989\n" +
"Chun Soft        1986-1989\n" +
"Enix             1986-1989\n" +
"O.L.             2021-2021\n" +
"\n" +
"\n";
    
    public static void main(String[] args) throws Exception {
        BitmapFont font = new BitmapFont();
        BufferedImage result = new BufferedImage(256, 240 * 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) result.getGraphics();
        String[] lines = msg.split("\n");
        int lineNumber = 0;
        for (String line : lines) {
            font.drawText(g, line, 3, lineNumber);
            lineNumber++;
        }
        ImageIO.write(result, "png", new File("d:/dq1_credits.png"));
    }
    
}
