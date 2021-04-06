package dq1.core;

import static dq1.core.Game.sleep;
import dq1.core.Script.ScriptCommand;
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import java.util.HashMap;
import java.util.Map;

/**
 * View class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class View {
    
    private static final Canvas CANVAS = new Canvas();
    private static BufferStrategy bs;
    private static BufferedImage backbuffer;
    private static BufferedImage offscreenTmp;
    private static Graphics2D bg;
    private static Graphics2D og2dTmp;
    
    private static final BufferedImage[] LAYERS = new BufferedImage[4];
    private static final Graphics2D[] OG2DS = new Graphics2D[4];
    
    // <0 = map is not dark
    private static int lightRadiusLayer0 = -1;
    
    public static Canvas getCanvas() {
        return CANVAS;
    }

    public static BufferedImage getOffscreen(int layer) {
        return LAYERS[layer - 1];
    }
    
    public static Graphics2D getOffscreenGraphics2D(int layer) {
        return OG2DS[layer - 1];
    }

    public static int getLightRadiusLayer0() {
        return lightRadiusLayer0;
    }

    public static void setLightRadiusLayer0(int lightRadiusLayer0) {
        View.lightRadiusLayer0 = lightRadiusLayer0;
    }

    public static void start() {
        CANVAS.createBufferStrategy(2);
        bs = CANVAS.getBufferStrategy();
        for (int i = 0; i < 4; i++) {
            LAYERS[i] = new BufferedImage(256, 240, TYPE_INT_ARGB);
            OG2DS[i] = (Graphics2D) LAYERS[i].getGraphics();
            OG2DS[i].setBackground(new Color(0, 0, 0, 0));
        }
        backbuffer = new BufferedImage(256, 240, TYPE_INT_ARGB);
        bg = (Graphics2D) backbuffer.getGraphics();
        offscreenTmp = new BufferedImage(256, 240, TYPE_INT_ARGB);
        og2dTmp = (Graphics2D) offscreenTmp.getGraphics();
        og2dTmp.setBackground(new Color(0, 0, 0, 0));
        CANVAS.addKeyListener(new Input());
    }
    
    private static int effectX = 0;
    private static int effectLayer = 0;
    private static Color effectColor;
    
    @ScriptCommand(name = "screen_shake")
    public static void shake(int layer, int count, int speed) {
        effectLayer = layer;
        for (int i = 0; i < count; i++) {
            int maxX = (int) (9 * ((count - i) / (double) count)) + 1;
            for (int s = 0; s < 16; s++) {
                double a = (2 * Math.PI) * (s / 10.0);
                effectX = (int) (maxX * Math.sin(a));
                refresh();
                Game.sleep(speed);
            }
        }
        effectLayer = 0;
        effectX = 0;
    }

    // encodedColor = hexa or octal string. example "0xffffff"
    @ScriptCommand(name = "screen_flash")
    public static void flash(
            int layer, int count, String encodedColor, int speed) {
        
        flash(layer, count, Util.getColor(encodedColor), speed);
    }

    public static void flash(int layer, int count, Color color, int speed) {
        effectLayer = layer;
        for (int i = 0; i < count * 2; i++) {
            effectColor = (i % 2) == 0 ? color : null;
            refresh();
            Game.sleep(speed);
        }
        effectLayer = 0;
        effectColor = null;
    }
    
    public static void flashLayer4(int count, Color color, int speed) {
        og2dTmp.drawImage(LAYERS[3], 0, 0, null);
        for (int i = 0; i < count * 2; i++) {
            OG2DS[3].clearRect(0, 0, 256, 240);
            OG2DS[3].drawImage(offscreenTmp, 0, 0, null);
            Color flashColor = (i % 2) == 0 ? color : null;
            if (flashColor != null) {
                Composite originalComp = OG2DS[3].getComposite();
                Composite comp 
                        = AlphaComposite.getInstance(AlphaComposite.SRC_IN);
                
                OG2DS[3].setComposite(comp);
                OG2DS[3].setColor(flashColor);
                OG2DS[3].fillRect(0, 0, 256, 240);
                OG2DS[3].setComposite(originalComp);
            }
            refresh();
            Game.sleep(speed);
        }
        OG2DS[3].drawImage(offscreenTmp, 0, 0, null);
        og2dTmp.clearRect(0, 0, 256, 240);
        refresh();
    }
    
    public final static Map<Integer, Shape> LIGHTS = new HashMap<>();
    
    @ScriptCommand(name = "screen_refresh")
    public static void refresh() {
        bg.clearRect(0, 0, 256, 240);
        Shape originalClip = null;
        for (int i = 0; i < LAYERS.length; i++) {
            if (i == 0 && lightRadiusLayer0 >= 0) {
                originalClip = bg.getClip();
                Shape light = LIGHTS.get(lightRadiusLayer0);
                if (light == null) {
                    light = new Ellipse2D.Double(
                        128 - lightRadiusLayer0, 120 - lightRadiusLayer0
                        , 2 * lightRadiusLayer0, 2 * lightRadiusLayer0);
                    LIGHTS.put(i, light);
                }
                bg.setClip(light);
            }
            bg.drawImage(LAYERS[i]
                    , i < effectLayer ? effectX : 0, 0, 256, 240, null);
            
            if (i == 0 && lightRadiusLayer0 >= 0) {
                bg.setClip(originalClip);
            }
            if (effectLayer > 0 
                    && i + 1 == effectLayer && effectColor != null) {
                
                bg.setColor(effectColor);
                bg.fillRect(0, 0, 256, 240);
            }
        }
        
        do {
            do {        
                Graphics g = bs.getDrawGraphics();
                g.drawImage(backbuffer, 0, 0, 
                        CANVAS.getWidth(), CANVAS.getHeight(), null);

                g.dispose();
            } while (bs.contentsRestored());
            bs.show();
        }
        while (bs.contentsLost()); 
    }
    
    private static final int[][] DIRECTIONS 
            = { { 1, 0 }, { 0, -1 }, { -1, 0 }, { 0, 1 } };
    
    // it will use the layer 4
    public static void showBattleBackgroundImageAnimation(
                                BufferedImage battleBackgroundImage) {
        
        int row = 3;
        int col = 3;
        for (int i = 0; i < 13; i++) {
            int s = i / 2 + 1;
            for (int j = 0; j < s; j++) {
                    int sx1 = col * 16;
                    int sy1 = row * 16;
                    int sx2 = sx1 + 16;
                    int sy2 = sy1 + 16;
                    int dx1 = col * 16 + 72;
                    int dy1 = row * 16 + 64 - 8;
                    int dx2 = dx1 + 16;
                    int dy2 = dy1 + 16;
                    OG2DS[1].drawImage(battleBackgroundImage
                            , dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
                    
                    refresh();
                    Game.sleep(10);
                    row += DIRECTIONS[i % 4][0];
                    col += DIRECTIONS[i % 4][1];
            }
        }
    }
    
    private static final Color[] FADE_COLORS = new Color[25];
    
    static {
        for (int i = 0; i < 25; i++) {
            FADE_COLORS[i] = new Color(0, 0, 0, (int) (255 * (i / 24.0)));
        }
    }
    
    @ScriptCommand(name = "screen_fade_in")
    public static void fadeIn() {
        Graphics2D g3 = getOffscreenGraphics2D(3);
        for (int i = 24; i >= 0; i--) {
            g3.setBackground(FADE_COLORS[i]);
            g3.clearRect(0, 0, 256, 240);
            View.refresh();
            sleep(1000 / 60);
        }
    }

    @ScriptCommand(name = "screen_fade_out")
    public static void fadeOut() {
        Graphics2D g3 = getOffscreenGraphics2D(3);
        g3.setColor(new Color(0, 0, 0, 16));
        for (int i = 0; i < 25; i++) {
            g3.setBackground(FADE_COLORS[i]);
            g3.clearRect(0, 0, 256, 240);
            View.refresh();
            sleep(1000 / 60);
        }
    }

    @ScriptCommand(name = "screen_clear")
    public static void clear(int layer, String color) {
        View.getOffscreenGraphics2D(layer).setBackground(Util.getColor(color));
        View.getOffscreenGraphics2D(layer).clearRect(0, 0, 256, 240);
    }
    
    @ScriptCommand(name = "screen_fill")
    public static void fill(int layer, String color) {
        View.getOffscreenGraphics2D(layer).setColor(Util.getColor(color));
        View.getOffscreenGraphics2D(layer).fillRect(0, 0, 256, 240);
        View.getOffscreenGraphics2D(layer).setColor(
                                    Util.getColor("0x00000000"));
    }

    @ScriptCommand(name = "screen_show_image")
    public static void showImage(int layer, String imageId, int x, int y) {
        BufferedImage image = Resource.getImage(imageId);
        View.getOffscreenGraphics2D(layer).drawImage(image, x, y, null);
        View.refresh();
    }

}
