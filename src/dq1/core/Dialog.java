package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dialog class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Dialog {
    
    private static final BoxBorder BOX_BORDER = new BoxBorder();
    private static int speed = 31;
    private static int col;
    private static int row;
    private static boolean open;
    private static boolean newLine;
    
    static {
        setBoxBorderTileIds(3, 4, 5, 6, 7, 8, 9, 10);        
    }

    public static boolean isOpen() {
        return open;
    }

    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        Dialog.speed = speed;
    }
    
    public static void print(int col, int row, int chr) {
        print(2, col, row, chr);
    }
    
    public static void print(int layer, int col, int row, int chr) {
        Graphics2D g = View.getOffscreenGraphics2D(layer);
        if (chr < 0 || chr >= 65535) {
            g.clearRect(8 * col, 8 * row, 8, 8);
        }
        else {
            BitmapFont.drawText(g, String.valueOf((char) chr), col, row);
        }
    }

    public static void printText(int layer, int col, int row, String text) {
        for (int c = 0; c < text.length(); c++) {
            print(layer, col + c, row, text.charAt(c));
        }
    }
    
    public static void drawBox(
            int layer, int chr, int col1, int row1, int col2, int row2) {
        
        for (int colTmp = col1; colTmp <= col2; colTmp++) {
            print(layer, colTmp, row1, chr);
            print(layer, colTmp, row2, chr);
        }
        for (int rowTmp = row1; rowTmp <= row2; rowTmp++) {
            print(layer, col1, rowTmp, chr);
            print(layer, col2, rowTmp, chr);
        }
    }

    public static void drawBox(
            int chr, int col1, int row1, int col2, int row2) {
        
        drawBox(2, chr, col1, row1, col2, row2);
    }

    public static void fillBox(
            int chr, int col1, int row1, int col2, int row2) {
        
        fillBox(2, chr, col1, row1, col2, row2);
    }

    public static void fillBox(
            int layer, int chr, int col1, int row1, int col2, int row2) {
        
        for (int rowTmp = row1; rowTmp <= row2; rowTmp++) {
            for (int colTmp = col1; colTmp <= col2; colTmp++) {
                print(layer, colTmp, rowTmp, chr);
            }
        }
    }

    public static class BoxBorder {
        int topLeft;
        int top;
        int topRight;
        int left;
        int right;
        int bottomLeft;
        int bottom;
        int bottomRight;
    }

    public static BoxBorder getBOX_BORDER() {
        return BOX_BORDER;
    }
    
    public static void setBoxBorderTileIds(int topLeft, int top, int topRight, 
                             int left, int right, 
                             int bottomLeft, int bottom, int bottomRight) {

        BOX_BORDER.topLeft = topLeft;
        BOX_BORDER.top = top;
        BOX_BORDER.topRight = topRight;
        BOX_BORDER.left = left;
        BOX_BORDER.right = right;
        BOX_BORDER.bottomLeft = bottomLeft;
        BOX_BORDER.bottom = bottom;
        BOX_BORDER.bottomRight = bottomRight;
    } 

    public static void drawBoxBorder(int col1, int row1, int col2, int row2) {
        drawBoxBorder(2, col1, row1, col2, row2);
    }

    public static void drawBoxBorder(
            int layer, int col1, int row1, int col2, int row2) {
        
        for (int rowTmp = row1; rowTmp <= row2; rowTmp++) {
            for (int colTmp = col1; colTmp <= col2; colTmp++) {
                if (rowTmp == row1 && colTmp == col1) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.topLeft);
                }
                else if (rowTmp == row1 && colTmp == col2) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.topRight);
                }
                else if (rowTmp == row1) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.top);
                }
                else if (rowTmp == row2 && colTmp == col1) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.bottomLeft);
                }
                else if (rowTmp == row2 && colTmp == col2) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.bottomRight);
                }
                else if (rowTmp == row2) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.bottom);
                }
                else if (colTmp == col1) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.left);
                }
                else if (colTmp == col2) {
                    print(layer, colTmp, rowTmp, BOX_BORDER.right);
                }
            }
        }
    }
    
    public static void copy(int dstCol, int dstRow, 
            int srcCol1, int srcRow1, int srcCol2, int srcRow2) {
    
            copy(2, dstCol, dstRow, srcCol1, srcRow1, srcCol2, srcRow2);
    }
    public static void copy(int layer , int dstCol, int dstRow, 
            int srcCol1, int srcRow1, int srcCol2, int srcRow2) {
        
        Graphics2D g = View.getOffscreenGraphics2D(layer);
        BufferedImage offscreen = View.getOffscreen(layer);
        int sx1 = srcCol1 * 8;
        int sy1 = srcRow1 * 8;
        int sx2 = srcCol2 * 8;
        int sy2 = srcRow2 * 8;
        int dx1 = dstCol * 8;
        int dy1 = dstRow * 8;
        int dx2 = dx1 + (sx2 - sx1);
        int dy2 = dy1 + (sy2 - sy1);
        g.drawImage(offscreen, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }
    
    @ScriptCommand(name = "open_dialog")
    public static void open() {
        open(2);
    }
    
    public static void open(int layer) {
        if (open) {
            return;
        }
        for (int x = 0; x < 12; x++) {
            fillBox(layer, ' ', 16 - x, 23, 17 + x, 24);
            drawBoxBorder(layer, 16 - x, 23, 17 + x, 24);
            View.refresh();
            Game.sleep(10);
        }
        for (int y = 0; y < 5; y++) {
            fillBox(layer, ' ', 3 + 1, 23 - y + 1, 28 - 1, 24 + y - 1);
            drawBoxBorder(layer, 3, 23 - y, 28, 24 + y);
            View.refresh();
            Game.sleep(20);
        }
        col = 4;
        row = 20;
        open = true;
        newLine = true;
    }
    
    // dialogType = 0 log -> without inner space
    //              1 dialog -> with 1 char inner space + "'" before and after 
    @ScriptCommand(name = "show_dialog")
    public static void print(
            int dialogType, int waitType, String message) {
        
        print(2, dialogType, waitType, message);
    }
    
    public static void print(
            int layer, int dialogType, int waitType, String message) {
        
        if (!open) {
            open(layer);
        }
        if (row > 20 && dialogType == 1) {
            nextRow(layer);
        }
        if (newLine && dialogType == 1) {
            print(layer, '"');
            newLine = false;
        }
        
        // recreate message replacing all the global variables
        String[] words = message.split("\\ ");
        message = "";
        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();
            boolean isGlobalVar = false;
            if (Script.isGlobalVar(word)) {
                try {
                    word = Script.getArgument(null, word).toString();
                    isGlobalVar = true;
                } catch (Exception ex) {
                    Logger.getLogger(Dialog.class.getName())
                            .log(Level.SEVERE, null, ex);
                    
                    System.exit(-1);
                }
            }
            message += word;
            if (i < words.length - 1 && !isGlobalVar) {
                message += " ";
            }
        }
        
        // print all the words scrolling up automatically when necessary
        words = message.split("\\ ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i].trim();
            if (word.equals("|")) {
                col = dialogType == 1 ? 5 : 4;
                nextRow(layer);
                continue;
            } 
            else if (word.length() + col > (dialogType == 1 ? 27 : 28)) {
                col = dialogType == 1 ? 5 : 4;
                nextRow(layer);
            }
            for (int c = 0; c < word.length(); c++) {
                if (dialogType == 1){
                    Audio.playSound(Audio.SOUND_NPC_DIALOG);
                }
                print(layer, word.charAt(c));
            }
            if (i == words.length - 1) {
                if (dialogType == 1) {
                    print(layer, '\'');
                }
                newLine = true;
                col = 4;
                nextRow(layer);
            }
            else if (i < words.length - 1 && col < 28) {
                print(layer, ' ');
            }
        }
        
        switch (waitType) {
            // will not wait
            case 0: break; 
            // wait for any key
            case 1: Game.waitForAnyKey(); break; 
            // wait for fire or cancel key and show a blink cursor
            case 2: blinkContinueCursor(); break; 
        }
    }

    private static void print(int layer, char c) {
        print(layer, col++, row, c);
        View.refresh();
        Game.sleep(speed);
    }
    
    private static void nextRow(int layer) {
        row++;
        if (row > 27) {
            row = 27;
            scrollUp(layer);
        }
    }

    @ScriptCommand(name = "dialog_scroll_up")
    public static void scrollUp() {
        scrollUp(2);
    }

    public static void scrollUp(int layer) {
        copy(layer, 3, 20, 3, 21, 28, 28);
        drawBox(layer, ' ', 4, 27, 27, 27);
        View.refresh();
    }
    
    @ScriptCommand(name = "clear_dialog")
    public static void clear() {
        clear(2);
    }

    public static void clear(int layer) {
        col = 4;
        row = 20;
        newLine = true;
        if (!open) {
            open(layer);
        }
        fillBox(layer, ' ', 4, 20, 27, 27);
        View.refresh();
    }
    
    @ScriptCommand(name = "show_dialog_blink_cursor")
    public static void blinkContinueCursor() {
        blinkContinueCursor(2);
    }

    public static void blinkContinueCursor(int layer) {
        long blkTime = System.nanoTime();
        while (true) {
            if ((int) ((System.nanoTime() - blkTime) * 0.0000000035) % 2 == 0) {
                print(layer, 15, row, 1);
            }
            else {
                print(layer, 15, row, ' ');
            }
            if (Input.isKeyJustPressed(KEY_CONFIRM) 
                            || Input.isKeyJustPressed(KEY_CANCEL)) {
                
                print(layer, 15, row, ' ');
                View.refresh();
                return;
            }
            View.refresh();
            Game.sleep(1000 / 60);
        }
    }

    @ScriptCommand(name = "close_dialog")
    public static void close() {
        close(2);
    }
    
    public static void close(int layer) {
        if (!open) {
            return;
        }
        for (int y = 4; y > 0; y--) {
            drawBoxBorder(layer, 3, 23 - y, 28, 24 + y);
            drawBox(layer, (char) -1, 2, 23 - y - 1, 29, 24 + y + 1);
            View.refresh();
            Game.sleep(20);
        }
        for (int x2 = 13; x2 >= -1; x2--) {
            drawBoxBorder(layer, 16 - x2, 23, 15 + x2, 24);
            drawBox(layer, (char) -1, 16 - x2 - 1, 23 - 1, 16 + x2, 24 + 1);
            View.refresh();
            Game.sleep(10);
        }
        open = false;
    }
    
    // default: use layer 3
    public static int showOptionsMenu(int col, int row, int minWidth
            , int minHeight, int cancelValue, String ... options) {
        
        return showOptionsMenu(3, true, col, row, minWidth, minHeight
                                                , cancelValue, 0, options);
    }

    public static int showOptionsMenu(int layer, boolean hideAfterChoose
            ,int col, int row, int minWidth, int minHeight
            , int cancelValue, int defaultOption, String ... options) {
        
        Audio.playSound(Audio.SOUND_SHOW_OPTIONS_MENU);
        
        int width = minWidth;
        int height = Math.max(minHeight, options.length + 2);
        for (String option : options) {
            width = Math.max(width, option.length() + 3);
        }
        drawBoxBorder(layer, col, row, col + width - 1, row + height - 1);
        fillBox(layer, ' ', col + 1, row + 1
                , col + width - 2, row + height - 2);
        
        for (int i = 0; i < options.length; i++) {
            printText(layer, col + 2, row + i + 1, options[i]);
        }
        
        int selectedItem = defaultOption;

        // for appropriate cursor start blink time
        long blinkTime = System.nanoTime();

        while (true) {
            for (int r = 0; r < options.length; r++) {
                Dialog.print(layer, col + 1, row + 1 + r, ' ');
            }

            // blink cursor
            if ((int) ((System.nanoTime() - blinkTime) 
                                        * 0.0000000035) % 2 == 0) {

                Dialog.print(layer, col + 1, row + 1 + selectedItem, 2);
            }

            if (Input.isKeyJustPressed(KEY_UP) 
                                            && selectedItem > 0) {

                selectedItem--;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_DOWN) 
                                && selectedItem < options.length - 1) {

                selectedItem++;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                break;
            }
            else if (Input.isKeyJustPressed(KEY_CANCEL)) {
                selectedItem = cancelValue;
                break;
            }
            View.refresh();
            Game.sleep(1000 / 60);
        }
        if (hideAfterChoose) {
            View.getOffscreenGraphics2D(layer).clearRect(
                    col * 8, row * 8, width * 8, height * 8);
        }
        else {
            Dialog.print(layer, col + 1, row + 1 + selectedItem, 2);
        }
        return selectedItem;
    }
    
    public static void hideOptionsMenu(int layer, int col, int row
        , int minWidth, int minHeight, String ... options) {
        
        int width = minWidth;
        int height = Math.max(minHeight, options.length + 2);
        for (String option : options) {
            width = Math.max(width, option.length() + 3);
        }
        View.getOffscreenGraphics2D(layer).clearRect(
                col * 8, row * 8, width * 8, height * 8);
    }
    
    public static void applyConfigValues() {
        Integer configSpeedMs = null;
        try {
            configSpeedMs = (Integer) 
                    Script.getGlobalValue("##game_config_message_speed_ms");
        }
        catch (Exception e) { }
        if (configSpeedMs == null) {
            configSpeedMs = 31;
        }
        speed = configSpeedMs;
    }
    
}
