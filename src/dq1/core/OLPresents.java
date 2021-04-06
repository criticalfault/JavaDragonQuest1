package dq1.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * OLPresents class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class OLPresents extends JPanel {
    
    private static final BufferedImage OFFSCREEN;
    private static final Graphics2D G1;
    private static final BufferedImage OFFSCREEN2;
    private static final Graphics2D G2;
    private static final BufferedImage BLOCK;
    private static final BufferedImage[] WALLS = new BufferedImage[2];
    private static final AffineTransform CAMERA_TRANSFORM 
                                                = new AffineTransform();

    private static final Color[] COLORS = new Color[32];
    private static double angle = 0;
    private static double value = 10;
    
    static {
        BufferedImage olPresents = Resource.getImage("ol_presents");
        BLOCK = olPresents.getSubimage(0, 0, 24, 24);
        WALLS[0] = olPresents.getSubimage(24, 0, 24, 24);
        WALLS[1] = olPresents.getSubimage(48, 0, 24, 24);
        OFFSCREEN = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        OFFSCREEN2 = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        G1 = (Graphics2D) OFFSCREEN.getGraphics();
        G2 = (Graphics2D) OFFSCREEN2.getGraphics();
        for (int i = 0; i < COLORS.length; i++) {
            int c = (int) (255 * (i / 31.0));
            COLORS[i] = new Color(0, 0, 0, c);
        }
    }

    public static void reset() {
        angle = 0;
        value = 10;
    }

    public static boolean update() {
        angle = -0.7725 + value;
        if (value > 2.5) {
            value -= 0.0795;
        }
        else {
            value *= 0.9825;
            value -= 0.008;
        }
        if (value < 0.01) {
            value = 0;
            return false;
        }
        return true;
    }
    
    public static void draw(Graphics g) {
        G1.setBackground(new Color(0, 0, 0, 255));
        G1.clearRect(0, 0, 256, 256);
        int dx = (int) (0 * Math.sin(angle));
        int dy = (int) (164 * (value / 10.0));
        CAMERA_TRANSFORM.setToIdentity();
        CAMERA_TRANSFORM.translate(128 + dx, 164 - dy);
        CAMERA_TRANSFORM.rotate(-angle);
        CAMERA_TRANSFORM.translate(-12, -12);
        G1.drawImage(BLOCK, CAMERA_TRANSFORM, null);
        G2.setColor(Color.BLACK);
        G2.fillRect(0, 0, 256, 256);
        for (int y = 0; y < 250; y += 1) {
            double scale = y / 256.0;
            int x1 = (int) (128 * scale);
            int x2 = (int) (256 - 128 * scale);
            G2.drawImage(OFFSCREEN, 0, y, 256, y + 1, x1, y, x2, y + 1, null);
        }
        G1.clearRect(0, 0, 256, 256);
        outer:
        for (int x = 0; x < 256; x++) {
            for (int y = 255; y >= 0; y--) {
                int c = OFFSCREEN2.getRGB(x, y);
                if (c == -16777216) {
                    OFFSCREEN2.setRGB(x, y, Color.BLUE.getRGB());
                }
                else {
                    double height = 12.0 / (1.0 - y / 256.0);
                    int textureIndex = (c >> 8) & 255; // green
                    if (textureIndex < 2) {
                        int dx1 = x;
                        int dy1 = (int) (128 - height);
                        int dx2 = dx1 + 1;
                        int dy2 = (int) (128 + height);
                        int sx1 = (c & 255);
                        int sy1 = 0;
                        int sx2 = sx1 + 1;
                        int sy2 = 24;
                        G1.drawImage(WALLS[textureIndex]
                            , dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    }
                    if (textureIndex < 2) {
                        continue outer;
                    }
                }
            }
        }
        g.drawImage(OFFSCREEN, 0, 0, 256, 240, null);
        int c = (int) (32 * (value / 10.0));
        g.setColor(COLORS[c]);
        g.fillRect(0, 0, 256, 240);
    }

}
