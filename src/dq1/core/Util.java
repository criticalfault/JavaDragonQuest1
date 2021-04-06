package dq1.core;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Util class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Util {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static int random(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }
    
    public static int random(int max) {
        return RANDOM.nextInt(max);
    }

    public static String formatLeft(String v, int length) {
        String spaces = "";
        for (int i = 0; i < length; i++) {
            spaces += " ";
        }
        v = v.trim() + spaces;
        v = v.substring(0, length);
        return v;
    }

    public static String formatRight(String v, int length) {
        String spaces = "";
        for (int i = 0; i < length; i++) {
            spaces += " ";
        }
        v = spaces + v.trim();
        return v.substring(v.length() - length, v.length());
    }
    
    public static String convertMsToHHMMSS(long ms) {
        long seconds = ms / 1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h,m,s);
    }
    
    private static final Map<String, Color> COLORS = new HashMap<>();
    
    // example: red color = 0xff0000
    public static Color getColor(String encodedColor) {
        Color color = COLORS.get(encodedColor);
        if (color == null) {
            if (encodedColor.startsWith("0x") && encodedColor.length() == 10) {
                int r = Integer.parseUnsignedInt(
                            encodedColor.substring(2, 4), 16);
                
                int g = Integer.parseUnsignedInt(
                            encodedColor.substring(4, 6), 16);
                
                int b = Integer.parseUnsignedInt(
                            encodedColor.substring(6, 8), 16);
                
                int a = Integer.parseUnsignedInt(
                            encodedColor.substring(8, 10), 16);
                
                color = new Color(r, g, b, a);
                COLORS.put(encodedColor, color);
            }
            else {
                color = Color.decode(encodedColor);
                COLORS.put(encodedColor, color);
            }
        }
        return color;
    }
    
}
