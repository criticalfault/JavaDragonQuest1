package dq1.core;

import java.awt.event.KeyEvent;

/**
 * (Project) Settings class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Settings {
    
    // --- display ---
    
    public static final int CANVAS_WIDTH = 256;
    public static final int CANVAS_HEIGHT = 240;

    public static final int VIEWPORT_WIDTH = (int) (CANVAS_WIDTH * 2.5);
    public static final int VIEWPORT_HEIGHT = (int) (CANVAS_HEIGHT * 2.0);

    // --- input ---

    public static int KEY_LEFT = KeyEvent.VK_LEFT;
    public static int KEY_RIGHT = KeyEvent.VK_RIGHT;
    public static int KEY_UP = KeyEvent.VK_UP;
    public static int KEY_DOWN = KeyEvent.VK_DOWN;
    public static int KEY_CONFIRM = KeyEvent.VK_X;
    public static int KEY_CANCEL = KeyEvent.VK_Z;
    
    // --- resources ---
    
    public static final String RES_IMAGE_FILE_EXT = ".png";
    public static final String RES_SOUND_FILE_EXT = ".sf2"; 
    public static final String RES_MUSIC_FILE_EXT = ".mid"; 
    public static final String RES_INF_FILE_EXT = ".inf"; 
    public static final String RES_MAP_FILE_EXT = ".map"; 
    public static final String RES_EVENT_FILE_EXT = ".evt"; 
    
    public static final String RES_IMAGE_PATH = "/res/image/";
    public static final String RES_SOUND_PATH = "/res/audio/";
    public static final String RES_MUSIC_PATH = "/res/audio/";
    public static final String RES_INF_PATH = "/res/inf/";
    public static final String RES_MAP_PATH = "/res/map/";
    public static final String RES_EVENT_PATH = "/res/event/";
    
    public static final String RES_SOUND_BANK = "tinypsg";
    public static final String RES_SOUND_EFFECTS = "sound_effects";
    
    public static final String RES_MUSICS_INF = "musics";
    public static final String RES_ENEMIES_INF = "enemies";
    public static final String RES_ITEMS_INF = "items";
    public static final String RES_SPELLS_INF = "spells";
    public static final String RES_PLAYER_LEVELS_INF = "player_levels";
    public static final String RES_TILESET_INF = "tileset";
    public static final String RES_TEXTS_INF = "texts";
    
    public static final String RES_BITMAP_FONT_IMAGE = "bitmap_font";
    public static final String RES_TILESET_IMAGE = "tileset";
    public static final String RES_CHARS_IMAGE = "chars";
    public static final String RES_SAVE_POINT_IMAGE = "blue_fire";
    public static final String RES_BATTLE_ENEMIES_IMAGE = "enemies";
    public static final String RES_BATTLE_BACKGROUNDS_IMAGE 
                                                        = "battle_backgrounds";
    
    public static final String BATTLE_MUSIC_ID = "battle";
    
}
