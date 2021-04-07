package dq1.core;

import static dq1.core.Game.State.*;
import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import dq1.core.TileMap.Area;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Game class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Game {
    
    public static enum State { OL_PRESENTS, TITLE, MAP, CHANGE_MAP }
    
    private static final Properties TEXTS;
    
    private static boolean running;
    private static TileMap currentMap;
    private static State state = State.OL_PRESENTS;
    private static TileMap newMap;
    private static boolean newMapUseFadeEffect;
    private static int playerNewRow;
    private static int playerNewCol;
    private static String playerNewDirection;
    private static boolean newMapRepelHasNoEffect;
    private static boolean newMapResetRepel;
    private static boolean newMapResetLight;
    private static boolean newMapIsDark;
    private static String newMapMusicId;
    private static boolean newClearLocalVars;
    private static Animation titleShineAnimation;
    
    static {
        TEXTS = Resource.getTexts(RES_TEXTS_INF);
    }

    public static Properties getTexts() {
        return TEXTS;
    }

    public static String getText(String varName) {
        return TEXTS.getProperty(varName);
    }

    public static State getState() {
        return state;
    }

    public static void setState(State state) {
        Game.state = state;
    }

    public static TileMap getCurrentMap() {
        return currentMap;
    }

    public static void start() throws Exception {
        Resource.loadMusics(RES_MUSICS_INF);
        Resource.loadEnemies(RES_ENEMIES_INF);
        Resource.loadItems(RES_ITEMS_INF);
        Resource.loadSpells(RES_SPELLS_INF);
        Resource.loadPlayerLevels(RES_PLAYER_LEVELS_INF);
        
        Audio.start();
        Player.start();
        Inventory.start();
        
        //testStartSpecificMap();
        //testStartLoadingSavedGame();
        //testChangePlayerStatus();
        
        Script.registerClassStaticCommands(Audio.class);
        Script.registerClassStaticCommands(Game.class);
        Script.registerClassStaticCommands(View.class);
        Script.registerClassStaticCommands(Dialog.class);
        Script.registerClassStaticCommands(Player.class);
        Script.registerClassStaticCommands(Inventory.class);
        Script.registerClassStaticCommands(Shop.class);
        Script.registerClassStaticCommands(Battle.class);
        
        titleShineAnimation = new Animation(
                Resource.getImage("title_shine"), 12, 1, 500);
        
        titleShineAnimation.createAnimation("shine"
                , new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                             11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
                             11, 11, 11, 11, 11, 11, 11, 11, 11, 11 });
        
        running = true;
        new Thread(new LogicThread()).start();
    }
    
    private static void testChangePlayerStatus() {
        Script.setGlobalValue("##player_lv", 35);
        Script.setGlobalValue("##player_hp", 999);
        Script.setGlobalValue("##player_mp", 999);
        Script.setGlobalValue("##player_g", 65535);
        Script.setGlobalValue("##player_e", 6);

        Script.setGlobalValue("##player_str", 500);
        Script.setGlobalValue("##player_agi", 500);
        Script.setGlobalValue("##player_max_hp", 999);
        Script.setGlobalValue("##player_max_mp", 999);
    }
    
    private static void testStartSpecificMap() throws Exception {
        //teleport("tantegel_castle", 65, 16, "up", 1, "tantegel", 0, 1, 1, 1);
        //teleport("rimuldar", 40, 24, "right", 1, "town", 0, 1, 1, 1);
        //teleport("shrine", 47, 16, "right", 1, "tantegel", 0, 1, 1, 1);
        //teleport("charlock_castle", 22, 31, "up", 1, "dungeon", 0, 1, 1, 1);
        //teleport("brecconary", 10, 25, "right", 1, "town", 0, 1, 1, 1);
        //teleport("garinham", 9, 24, "right", 1, "town", 0, 1, 1, 1);
        //teleport("swamp_cave", 12, 12, "right", 1, "town", 0, 1, 1, 1);
        //teleport("kol", 19, 23, "up", 1, "town", 0, 1, 1, 1);
        //teleport("cantlin", 36, 37, "down", 1, "town", 0, 1, 1, 1);
        //teleport("world", 87, 117, "right", 1, "world", 0, 1, 1, 1);
        //teleport("hauksness", 10, 20, "right", 1, "dungeon", 0, 1, 1, 1);
        teleport("world", 77, 103, "down", 1, "world", 0, 1, 1, 1); // cantlin cantlin golem
    }
    
    private static void testStartLoadingSavedGame() throws Exception {
        Map<String, Object> loadedGlobalVars = Script.loadVars(3);
        loadGameInternal(loadedGlobalVars);
    }
    
    private static class LogicThread implements Runnable {

        @Override
        public void run() {
            try {
                while (running) {
                    switch (state) {
                        case OL_PRESENTS: updateOLPresents(); break;
                        case TITLE: updateTitle(); break;
                        case MAP: updateMap(); break;
                        case CHANGE_MAP: updateChangeMap(); break;
                    }
                }
            }
            catch (Exception e) {
                Logger.getLogger(Game.class.getName())
                    .log(Level.SEVERE, null, e);
                System.exit(-1);
            }
        }
        
    }
    
    private static void updateOLPresents() throws Exception {
        OLPresents.reset();
        Graphics2D g = View.getOffscreenGraphics2D(1);
        OLPresents.update();
        OLPresents.draw(g);
        View.refresh();
        sleep(3000);
        startTime = System.currentTimeMillis();
        while (OLPresents.update()) {
            OLPresents.draw(g);
            View.refresh();
            sync30fps();
        }
        String presents = getText("@@presents");
        for (int i = 0; i < presents.length(); i++) {
            Dialog.print(1, 12 + i, 19, presents.charAt(i));
            sleep(100);
        }
        sleep(3000);
        View.fadeOut();
        state = TITLE;
    }
    
    private static void updateTitle() throws Exception {
        View.showImage(1, "title", 0, 0);
        sleep(2000);
        long introMusicStartTime = System.currentTimeMillis();
        Audio.playMusic("intro");
        View.fadeIn();
        Graphics2D g = View.getOffscreenGraphics2D(1);
        startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - introMusicStartTime < 10500) {
            titleShineAnimation.update();
            View.showImage(1, "title", 0, 0);
            titleShineAnimation.draw(g, 179, 60);
            View.refresh();
            sync30fps();
        }
        Audio.playSound(Audio.SOUND_SHOW_OPTIONS_MENU);
        View.showImage(1, "title", 0, -240);
        Dialog.print(0, 0, getText("@@title_keyboard_1"));
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, getText("@@title_keyboard_2"));
        Dialog.print(0, 0, "");
        Dialog.print(0, 2, getText("@@title_keyboard_3"));
        Dialog.print(0, 0, "");
        Dialog.print(0, 1, getText("@@title_keyboard_4"));
        
        Dialog.close();
        boolean exit = false;
        while (!exit) {
            View.showImage(1, "title", 0, -240);
            String startLabel = Game.getText("@@title_option_start");
            String continueLabel = Game.getText("@@title_option_continue");
            int option = -1;
            while (option == -1) {
                option = Dialog.showOptionsMenu(
                                10, 19, 10, 4, -1, startLabel, continueLabel);
            }
            switch (option) {
                case 0:
                    resetGameTypePlayersName();
                    boolean optionMenuOk = false;
                    while (!optionMenuOk) {
                        playerNameConfirmed = false;
                        playerNameCanceled = false;
                        exit = startGameTypePlayersName(); 
                        if (exit) {
                            
                            Dialog.fillBox(3, ' ', 19, 14, 29, 15);
                            Dialog.drawBoxBorder(3, 18, 13, 30, 16);
                            Dialog.printText(3, 19, 14
                                    , getText("@@title_keyboard_5"));
                            
                            Dialog.printText(3, 19, 15
                                    , getText("@@title_keyboard_6"));
                            
                            // allows player to change 
                            // message speed and audio volumes
                            optionMenuOk = showOptionsMenu();
                            Dialog.fillBox(3, -1, 18, 13, 30, 16);
                            if (optionMenuOk) {
                                Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                                // start game !
                                teleport("tantegel_castle", 65, 16, "up", 1
                                                    , "tantegel", 0, 1, 1, 1);
                            }
                        }
                        else {
                            optionMenuOk = true;
                        }
                    }
                    break; 
                case 1: exit = loadGame(); break;
            }
        }
    }
    
    private static final KeyHandler KEY_HANDLER = new KeyHandler();
    private static final char[] PLAYER_NAME = new char[5];
    private static int playerNameCursorIndex = 0;
    private static boolean playerNameConfirmed = false;
    private static boolean playerNameCanceled = false;

    private static void resetGameTypePlayersName() {
        playerNameCursorIndex = 0;
        playerNameConfirmed = false;
        playerNameCanceled = false;
        Arrays.fill(PLAYER_NAME, ' ');
    }
    
    private static boolean startGameTypePlayersName() throws Exception{
        View.clear(3, "0x00000000");
        
        Dialog.fillBox(3, ' ', 6, 22, 24, 23);
        Dialog.drawBoxBorder(3, 5, 21, 25, 24);
        Dialog.printText(3, 6, 22, "ENTER - Confirm");
        Dialog.printText(3, 6, 23, "  ESC - Cancel");
        
        Dialog.fillBox(3, ' ', 6, 17, 24, 19);
        Dialog.drawBoxBorder(3, 5, 16, 25, 20);
        Dialog.printText(3, 6, 17, "TYPE YOUR NAME:");
        
        Input.setListener(KEY_HANDLER);
        long blinkTime = System.nanoTime();
        while (!playerNameConfirmed && !playerNameCanceled) {
            synchronized (KEY_HANDLER) {
                for (int i = 0; i < 5; i++) {
                    Dialog.print(3, 13 + i, 19, PLAYER_NAME[i]);
                }
                if ((int) ((System.nanoTime() - blinkTime) 
                                            * 0.0000000035) % 2 == 0) {            
                    Dialog.print(3, 13 + playerNameCursorIndex, 19, 16);
                }
            }
            Game.sleep(1000 / 60);
        }
        if (playerNameCanceled) {
            View.clear(3, "0x00000000");
            return false;
        }
        else {
            Dialog.fillBox(3, -1, 5, 21, 25, 24);
            // set player's name
            Input.setListener(null);
            Player.setName(new String(PLAYER_NAME));
            Audio.playSound(Audio.SOUND_SHOW_OPTIONS_MENU);
            return true;
        }
    }
    
    private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private static class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            synchronized (KEY_HANDLER) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    playerNameCanceled = true;
                }
                else if (playerNameCursorIndex > 0 
                        && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    
                    playerNameConfirmed = true;
                }
                else if (playerNameCursorIndex > 0 
                        && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    
                    PLAYER_NAME[--playerNameCursorIndex] = ' ';
                }
                else if (VALID_CHARS.indexOf(
                        Character.toUpperCase(e.getKeyChar())) >= 0  
                            && playerNameCursorIndex < 4) {
                    
                    PLAYER_NAME[playerNameCursorIndex++] 
                            = Character.toUpperCase(e.getKeyChar());
                }
            }
        }

    }
    
    private static long startTime;
    
    private static void updateMap() throws Exception {
        startTime = System.currentTimeMillis();
        boolean exitMap = false;
        while (!exitMap) {
            // if the player is out of walkable area, 
            // he is automatically teleported to the configured location
            Area mapArea = currentMap.getCurrentArea();
            if (mapArea != null 
                    && Player.getX() % 16 == 0 && Player.getY() % 16 == 0
                    && !mapArea.contains(Player.getX(), Player.getY())){
                
                Audio.playSound(Audio.SOUND_ENTRANCE_OR_STAIRS);
                sleep(250);
                teleport(mapArea.teleportToMapId
                        , mapArea.teleportLocationCol
                        , mapArea.teleportLocationRow
                        , mapArea.teleportPlayerDirection
                        , mapArea.useFadeEffect ? 1 : 0, mapArea.musicId
                        , mapArea.isDark ? 1 : 0
                        , mapArea.repelHasNoEffect ? 1 : 0
                        , mapArea.resetRepel ? 1 : 0
                        , mapArea.resetLight ? 1 : 0);
                return;
            }
            
            if (Player.getX() % 16 == 0 && Player.getY() % 16 == 0) {
                exitMap = currentMap.checkEventTriggered();
            }
            if (exitMap || state != MAP) {
                break;
            }
            Player.update();
            // play can select "load game" option
            if (state != MAP) {
                break;
            }
            currentMap.update();
            redraw();
            sync30fps();
        }
    }
    
    // innacurate but i think it's ok xD ...
    private static void sync30fps() {
        long endTime = System.currentTimeMillis();
        int waitTime = (int) (33 - (endTime - startTime));
        if (waitTime < 1) {
            waitTime = 1;
        }
        startTime = endTime;
        sleep(waitTime);
    }
    
    @ScriptCommand(name = "force_redraw")
    public static void redraw() {
        Graphics2D g = View.getOffscreenGraphics2D(1);
        currentMap.draw(g);
        if (Player.isVisible()) {
            Player.draw(g);
        }
        View.refresh();
    }
    
    private static void updateChangeMap() throws Exception {
        if (newMapUseFadeEffect) {
            View.fadeOut();
        }
        boolean isSameMap = currentMap == newMap;
        if (currentMap != null) {
            if (isSameMap) {
                currentMap.executeAllEvents("on_map_internal_exit");
            }
            else {
                currentMap.executeAllEvents("on_map_exit");
            }
        }
        
        currentMap = newMap;
        newMap = null;
        Script.setGlobalValue("$$current_map_id", currentMap.getId());
        Script.setGlobalValue("$$current_map_name", currentMap.getName());
        Script.setGlobalValue("$$current_map_music_id", newMapMusicId);
        Script.setGlobalValue("##current_map_is_dark"
                                                , newMapIsDark ? 1 : 0);
        
        Script.setGlobalValue("##current_map_repel_has_no_effect"
                                        , newMapRepelHasNoEffect ? 1 : 0);
        
        if (isSameMap) {
            currentMap.executeAllEvents("on_map_internal_enter");
        }
        else {
            if (newClearLocalVars) {
                currentMap.clearAllLocalVars();
            }
            currentMap.executeAllEvents("on_map_enter");
        }

        // play background music
        if (newMapMusicId != null) {
            if (Audio.getCurrentMusic() == null 
                    || !Audio.getCurrentMusic().id.equals(newMapMusicId)) {
                
                Audio.playMusic(newMapMusicId);
                newMapMusicId = null;
            }
        }

        Player.setJustTeleported(true);
        Player.setLocation(playerNewRow, playerNewCol);
        Player.setDisabledEventLocation(playerNewCol, playerNewRow);
        if (!playerNewDirection.equals("preserve")) {
            Player.changeDirection(playerNewDirection);
        }
        
        // dark place with some kind of light effect
        currentMap.setDark(newMapIsDark);
        if (!newMapIsDark) {
            Player.setNotDarkPlace();
        }
        else if (newMapIsDark && newMapResetLight) {
            Player.setDarkPlace();
        }
        Player.updateViewLightRadius(true);
        
        // repel effect
        if (newMapResetRepel) {
            Player.setRepelCounter(0, "");
        }
        currentMap.setRepelHasNoEffect(newMapRepelHasNoEffect);
        currentMap.selectAreaAccordingToPlayerLocation();
        state = MAP;
        redraw();
        if (newMapUseFadeEffect) {
            View.fadeIn();
        }
    }
    
    @ScriptCommand(name = "wait_for_fire_key")
    public static void waitForFireKey() {
        while (true) {
            if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                break;
            }
            View.refresh();
            sleep(1);
        }
    }

    @ScriptCommand(name = "wait_for_fire_or_esc_key")
    public static void waitForFireOrEscKey() {
        while (true) {
            if (Input.isKeyJustPressed(KEY_CONFIRM)
                    || Input.isKeyJustPressed(KEY_CANCEL)) {
                
                break;
            }
            View.refresh();
            sleep(1);
        }
    }
    
    @ScriptCommand(name = "wait_for_any_key")
    public static void waitForAnyKey() {
        while (true) {
            if (Input.isKeyJustPressed(KEY_LEFT)) {
                break;
            }
            if (Input.isKeyJustPressed(KEY_RIGHT)) {
                break;
            }
            if (Input.isKeyJustPressed(KEY_UP)) {
                break;
            }
            if (Input.isKeyJustPressed(KEY_DOWN)) {
                break;
            }
            if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                break;
            }
            if (Input.isKeyJustPressed(KEY_CANCEL)) {
                break;
            }
            View.refresh();
            sleep(1);
        }
    }
    
    @ScriptCommand(name = "sleep")
    public static void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException ex) {
        }
        View.refresh();
    }
    
    @ScriptCommand(name = "teleport")
    public static void teleport(String mapId, int col, int row
            , String playerDirection, int useFadeEffect
            , String musicId, int isDark, int repelHasNoEffect
            , int resetRepel, int resetLight) throws Exception {
        
        if (currentMap != null && !currentMap.isDark() && isDark != 0 ) {
            Player.setOutsideLocation(currentMap.getId()
                    , Audio.getCurrentMusic().id
                    , Player.getMapCol(), Player.getMapRow());
        }
        
        newMap = Resource.getTileMap(mapId);
        playerNewRow = row;
        playerNewCol = col;
        playerNewDirection = playerDirection;
        newMapUseFadeEffect = useFadeEffect != 0;
        newMapMusicId = musicId;
        newMapIsDark = isDark != 0;
        newMapRepelHasNoEffect = repelHasNoEffect != 0;
        newMapResetRepel = resetRepel != 0;
        newMapResetLight = resetLight != 0;
        newClearLocalVars = true;
        state = CHANGE_MAP;
    }
    
    private static void showSavedFiles() {
        Dialog.drawBoxBorder(3, 9, 3, 28, 18);
        Dialog.fillBox(3, ' ', 10, 4, 27, 17);
        //Dialog.printText(3, 11, 3, "SAVE GAME");
        for (int i = 0; i < 3; i++) {
            String mapName = "---------";
            String playerName = "----";
            String playerLV = "--";
            String playerG = "-----";
            String playerAccumulatedTimeMs = "--:--:--";
            Map<String, Object> fileGlobalVars = Script.loadVars(i + 1);
            if (fileGlobalVars != null) {
                mapName = Util.formatLeft(
                    fileGlobalVars.get("$$current_map_name").toString(), 12);
                
                playerName = Util.formatLeft(
                        fileGlobalVars.get("$$player_name").toString(), 4);
                
                playerLV = Util.formatLeft(
                        fileGlobalVars.get("##player_lv").toString(), 2);
                
                playerG = Util.formatRight(
                        fileGlobalVars.get("##player_g").toString(), 5);
                
                playerAccumulatedTimeMs = Util.convertMsToHHMMSS(
                    (Long) fileGlobalVars.get("##player_accumulated_time_ms"));
            }
            Dialog.printText(
                    3, 11, 4 + i * 5, "File " + (i + 1) + " - " + playerName);
            
            Dialog.printText(3, 11, 5 + i * 5, "Loc.:" + mapName);
            Dialog.printText(
                    3, 11, 6 + i * 5, "Time:" + playerAccumulatedTimeMs);
            
            Dialog.printText(
                    3, 11, 7 + i * 5, "  LV:" + playerLV + "   G:" + playerG);
            
            if (i < 2) {
                Dialog.printText(3, 9, 8 + i * 5
                        , "\u000c\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\u000e");
            }
        }
    }
    
    private static void hideSavedFiles() {
        Dialog.fillBox(3, -1, 9, 3, 28, 18);
        View.refresh();
    }
    
    // return -1 = canceled;
    private static int selectGameFile(int defaultSelectedItem) {
        showSavedFiles();
        int selectedItem = defaultSelectedItem;
        // for appropriate cursor start blink time
        long blinkTime = System.nanoTime();
        boolean exit = false;
        while (!exit) {
            for (int r = 0; r < 3; r++) {
                Dialog.print(3, 10, 4 + r * 5, ' ');
            }

            // blink cursor
            if ((int) ((System.nanoTime() - blinkTime) 
                                        * 0.0000000035) % 2 == 0) {

                Dialog.print(3, 10, 4 + selectedItem * 5, 2);
            }

            if (Input.isKeyJustPressed(KEY_UP) 
                                            && selectedItem > 0) {

                selectedItem--;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_DOWN) 
                                && selectedItem < 3 - 1) {

                selectedItem++;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                break;
            }
            else if (Input.isKeyJustPressed(KEY_CANCEL)) {
                selectedItem = -1;
                break;
            }
            View.refresh();
            Game.sleep(1000 / 60);
        }        
        return selectedItem;
    }
    
    @ScriptCommand(name = "save_game")
    public static void saveGame() {
        int selectedItem = 0;
        boolean exit = false;
        while (!exit) {
            showSavedFiles();
            Dialog.clear();
            Dialog.print(0, 0, Game.getText("@@game_select_save_file"));
            selectedItem = selectGameFile(selectedItem);
            if (selectedItem >= 0) {
                hideSavedFiles();
                Dialog.clear();
                Script.setGlobalValue("##file_number", selectedItem + 1);
                Dialog.print(0, 0
                        , Game.getText("@@game_save_file_confirmation"));
                
                String yes = Game.getText("@@option_yes");
                String no = Game.getText("@@option_no");
                int saveConfirmation = Dialog.showOptionsMenu(
                                        3, true, 9, 3, 0, 0, 1, 1, yes, no);
                
                if (saveConfirmation == 0) {
                    Dialog.clear();
                    Player.updatePlayingTime();
                    if (Script.saveVars(selectedItem + 1)) {
                        Dialog.print(
                            0, 1, Game.getText("@@game_saved_successfully"));
                        
                        exit = true;
                    }
                    else {
                        Dialog.print(0, 1, Game.getText("@@game_save_error"));
                        exit = false;
                    }
                }
                else if (saveConfirmation == 1) {
                    showSavedFiles();
                    exit = false;
                }
            }
            else if (selectedItem < 0) {
                selectedItem = -1;
                break;
            }
        }
        hideSavedFiles();
    }

    @ScriptCommand(name = "load_game")
    public static boolean loadGame() throws Exception {
        int selectedItem = 0;
        boolean exit = false;
        while (!exit) {
            showSavedFiles();
            Dialog.clear();
            Dialog.print(0, 0, Game.getText("@@game_select_load_file"));
            selectedItem = selectGameFile(selectedItem);
            if (selectedItem >= 0) {
                hideSavedFiles();
                Dialog.clear();
                Script.setGlobalValue("##file_number", selectedItem + 1);
                Dialog.print(0, 0
                        , Game.getText("@@game_load_file_confirmation"));
                
                String yes = Game.getText("@@option_yes");
                String no = Game.getText("@@option_no");
                int loadConfirmation = Dialog.showOptionsMenu(
                                        3, true, 9, 3, 0, 0, 1, 1, yes, no);
                
                if (loadConfirmation == 0) {
                    Dialog.clear();
                    Map<String, Object> loadedFile 
                            = Script.loadVars(selectedItem + 1);
                    
                    if (loadedFile != null) {
                        Dialog.print(
                            0, 0, Game.getText("@@game_loaded_successfully"));
                        
                        Game.sleep(1000);
                        Dialog.close();
                        loadGameInternal(loadedFile);
                        exit = true;
                        return true;
                    }
                    else {
                        Dialog.print(0, 1, Game.getText("@@game_load_error"));
                        exit = false;
                    }
                }
                else if (loadConfirmation == 1) {
                    showSavedFiles();
                    exit = false;
                }
            }
            else if (selectedItem < 0) {
                exit = true;
                break;
            }
        }
        hideSavedFiles();
        Dialog.close();
        return false;        
    }
    
    private static void loadGameInternal(
            Map<String, Object> loadedGlobalVars) throws Exception {
        
        Script.getVARS().clear();
        Script.getVARS().putAll(loadedGlobalVars);
        Audio.applyConfigValues();
        Dialog.applyConfigValues();
        Battle.applyConfigValues();
        Player.updateStartPlayingTime();
        String mapId = Script.getGlobalValue("$$current_map_id").toString();
        // save the original outside spell information because it will be
        // affected by teleport function
        String outsideMapId = (String) Script.getGlobalValue("$$player_outside_map");
        String outsideMusicId = (String) Script.getGlobalValue("$$player_outside_music_id");
        Integer outsideRow = (Integer) Script.getGlobalValue("##player_outside_row");
        Integer outsideCol = (Integer) Script.getGlobalValue("##player_outside_col");
        teleport(mapId, Player.getMapCol(), Player.getMapRow(), "down", 1
            , "" + Script.getGlobalValue("$$current_map_music_id")
            , (Integer) Script.getGlobalValue("##current_map_is_dark")
            , (Integer) Script.getGlobalValue(
                    "##current_map_repel_has_no_effect"), 0, 0);
        // restore the original outside information
        Script.setGlobalValue("$$player_outside_map", outsideMapId);
        Script.setGlobalValue("$$player_outside_music_id", outsideMusicId);
        Script.setGlobalValue("##player_outside_row", outsideRow);
        Script.setGlobalValue("##player_outside_col", outsideCol);
        currentMap = null;
        newClearLocalVars = false;
        newMap.setResetLightOnEnter(false);
        newMap.setResetRepelOnEnter(false);
        Audio.stopMusic();
        Object newMapMusicIdTmp 
                = Script.getGlobalValue("$$current_map_music_id");
        
        newMapMusicId 
                = newMapMusicIdTmp == null ? null : newMapMusicIdTmp.toString();
    }

    @ScriptCommand(name = "change_event_location")
    public static void changeEventVisibility(String eventId, int col, int row) {
        for (Event event : currentMap.getEvents()) {
            if (event.getId().equals(eventId)) {
                event.setLocation(col * 16, row * 16);
                break;
            }
        }
    }
    
    @ScriptCommand(name = "change_event_visibility")
    public static void changeEventVisibility(String eventId, int visible) {
        for (Event event : currentMap.getEvents()) {
            if (event.getId().equals(eventId)) {
                event.setVisible(visible != 0);
                break;
            }
        }
    }

    @ScriptCommand(name = "change_event_animation")
    public static void changeEventAnimation(String eventId, String animatId) {
        for (Event event : currentMap.getEvents()) {
            if (event.getId().equals(eventId)) {
                event.changeAnimation(animatId);
                break;
            }
        }
    }

    @ScriptCommand(name = "change_event_turn_to_player")
    public static void changeEventTurnToPlayer(String eventId) {
        for (Event event : currentMap.getEvents()) {
            if (event.getId().equals(eventId)) {
                event.turnToPlayer();
                break;
            }
        }
    }

    // get_event col row "$$event_id" "$$event_type"
    @ScriptCommand(name = "get_event")
    public static void getEvent(int col, int row, String eventIdGlobalVar
                                , String eventTypeGlobalVar) throws Exception {
        
        Script.setGlobalValue(eventIdGlobalVar, "");
        Script.setGlobalValue(eventTypeGlobalVar, "");
        for (Event event : currentMap.getEvents()) {
            if (event.getX() == col * 16 && event.getY() == row * 16) {
                Script.setGlobalValue(eventIdGlobalVar, event.getId());
                Script.setGlobalValue(eventTypeGlobalVar, event.getType());
                break;
            }
        }
    }
    
    @ScriptCommand(name = "trigger_event")
    public static void triggerEvent(
            String eventId, String label) throws Exception {
        
        for (Event event : currentMap.getEvents()) {
            if (event.getId().equals(eventId)) {
                event.getScript().execute(label);
                break;
            }
        }
    }
    
    @ScriptCommand(name = "exit_game")
    public static void exitGame() {
        System.exit(0);
    }
    

    public static boolean showOptionsMenu() {
        Dialog.drawBoxBorder(3, 18, 3, 30, 12);
        Dialog.fillBox(3, ' ', 19, 4, 29, 11);

        String msgSpeed1 = Game.getText("@@game_config_option_msg_speed_1");
        String msgSpeed2 = Game.getText("@@game_config_option_msg_speed_2");
        String battleSpeed1 = 
                Game.getText("@@game_config_option_battle_speed_1");
        
        String battleSpeed2 = 
                Game.getText("@@game_config_option_battle_speed_2");
        
        String soundVol1 = Game.getText("@@game_config_option_sound_vol_1");
        String soundVol2 = Game.getText("@@game_config_option_sound_vol_2");
        String musicVol1 = Game.getText("@@game_config_option_music_vol_1");
        String musicVol2 = Game.getText("@@game_config_option_music_vol_2");
        
        msgSpeed1 = Util.formatLeft(msgSpeed1, 7);
        msgSpeed2 = Util.formatLeft(msgSpeed2, 7);
        battleSpeed1 = Util.formatLeft(battleSpeed1, 7);
        battleSpeed2 = Util.formatLeft(battleSpeed2, 7);
        soundVol1 = Util.formatLeft(soundVol1, 7);
        soundVol2 = Util.formatLeft(soundVol2, 7);
        musicVol1 = Util.formatLeft(musicVol1, 7);
        musicVol2 = Util.formatLeft(musicVol2, 7);
        
        Dialog.printText(3, 20, 4, msgSpeed1 + "\u000b \u0002");
        Dialog.printText(3, 21, 5, msgSpeed2);
        Dialog.printText(3, 20, 6, battleSpeed1 + "\u000b \u0002");
        Dialog.printText(3, 21, 7, battleSpeed2);
        Dialog.printText(3, 20, 8, soundVol1 + "\u000b \u0002");
        Dialog.printText(3, 21, 9, soundVol2);
        Dialog.printText(3, 20, 10, musicVol1 + "\u000b \u0002");
        Dialog.printText(3, 21, 11, musicVol2);
        
        // wait for appropriate cursor start blink time
        long blinkTime = System.nanoTime();
        int selectedOption = 0;
        boolean retValue = false;
        while (true) {
            Dialog.print(3, 19, 4, ' ');
            Dialog.print(3, 19, 6, ' ');
            Dialog.print(3, 19, 8, ' ');
            Dialog.print(3, 19, 10, ' ');

            Dialog.printText(3, 28, 4, "" + (9 - Dialog.getSpeed() / 10));
            Dialog.printText(3, 28, 6, "" + (9 - Battle.getSpeed() / 10));
            Dialog.printText(3, 28, 8, "" + Audio.getSoundVolume());
            Dialog.printText(3, 28, 10, "" + Audio.getMusicVolume());

            // blink cursor
            if ((int) ((System.nanoTime() - blinkTime) 
                                        * 0.0000000035) % 2 == 0) {

                Dialog.print(3, 19, 4 + 2 * selectedOption, 2);
            }

            if (Input.isKeyJustPressed(KEY_UP) 
                    && selectedOption > 0) {

                selectedOption--;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_DOWN) 
                                && selectedOption < 3) {

                selectedOption++;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_LEFT)) {
                changeOptionValue(selectedOption, -1);
            }
            else if (Input.isKeyJustPressed(KEY_RIGHT)) {
                changeOptionValue(selectedOption, 1);
            }
            else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                selectedOption = -1;
                retValue = true;
                break;
            }
            else if (Input.isKeyJustPressed(KEY_CANCEL)) {
                selectedOption = -1;
                retValue = false;
                break;
            }
            View.refresh();
            Game.sleep(1000 / 60);
        }

        Dialog.fillBox(3, -1, 18, 3, 30, 12);
        return retValue;
    }
    
    private static void changeOptionValue(int option, int dv) {
        switch (option) {
            // message speed
            case 0:
                int currentMessageSpeed = 9 - Dialog.getSpeed() / 10;
                int newMessageSpeed = currentMessageSpeed + dv < 1 
                        ? 1 : currentMessageSpeed + dv > 9 
                        ? 9 : currentMessageSpeed + dv;
                
                newMessageSpeed = (9 - newMessageSpeed) * 10 + 1;
                Dialog.setSpeed(newMessageSpeed);
                Script.setGlobalValue(
                        "##game_config_message_speed_ms", newMessageSpeed);
                
                break;
            // battle speed
            case 1:
                int currentBattleSpeed = 9 - Battle.getSpeed() / 10;
                int newBattleSpeed = currentBattleSpeed + dv < 1 
                        ? 1 : currentBattleSpeed + dv > 9 
                        ? 9 : currentBattleSpeed + dv;
                
                newBattleSpeed = (9 - newBattleSpeed) * 10 + 1;
                Battle.setSpeed(newBattleSpeed);
                Script.setGlobalValue(
                        "##game_config_battle_speed_ms", newBattleSpeed);
                
                break;
            // sound volume
            case 2:
                int currentSoundVolume = Audio.getSoundVolume();
                int newSoundVolume = currentSoundVolume + dv < 0 
                        ? 0 : currentSoundVolume + dv > 9 
                        ? 9 : currentSoundVolume + dv;

                Audio.setSoundVolume(newSoundVolume);
                Audio.playSound(Audio.SOUND_SHOW_OPTIONS_MENU);
                break;
            // music volume
            case 3:
                int currentMusicVolume = Audio.getMusicVolume();
                int newMusicVolume = currentMusicVolume + dv < 0 
                        ? 0 : currentMusicVolume + dv > 9 
                        ? 9 : currentMusicVolume + dv;
                
                Audio.setMusicVolume(newMusicVolume);
                break;
        }
    }
    
    // enabled != 0 = true
    @ScriptCommand(name = "set_current_map_enemies_encounter_enabled")
    public static void setCurrentMapEnemiesEncountersEnabled(int enabled) {
        if (currentMap != null) {
            currentMap.setEnemiesEncounterEnabled(enabled != 0);
        }
    }

    // direction -> "down", "left", "up", "right" or "stay"
    @ScriptCommand(name = "walk_event")
    public static void walkEvent(
            String eventId, String direction) throws Exception {
        
        int walkDx = 0;
        int walkDy = 0;
        switch (direction) {
            case "down": walkDx = 0; walkDy = 1; break;
            case "left": walkDx = -1; walkDy = 0; break;
            case "up": walkDx = 0; walkDy = -1; break;
            case "right": walkDx = 1; walkDy = 0; break;
            case "stay": walkDx = 0; walkDy = 0; break;
        }
        if (eventId.equals("player")) {
            if (!direction.equals("stay")) {
                Player.changeDirection(direction);
            }
            for (int i = 0; i < 16; i++) {
                Player.incX(walkDx);
                Player.incY(walkDy);
                Player.getAnimation().update();
                for (Event eventTmp : currentMap.getEvents()) {
                    eventTmp.getAnimation().update();
                }
                redraw();
                sleep(1000 / 60);
            }
            Player.setLocation(Player.getMapRow(), Player.getMapCol());
        }
        else {
            for (int i = 0; i < 16; i++) {
                for (Event eventTmp : currentMap.getEvents()) {
                    if (eventTmp.getId().equals(eventId)) {
                        if (!direction.equals("stay")) {
                            eventTmp.changeAnimation(direction);
                        }
                        eventTmp.setLocation(
                            eventTmp.getX() + walkDx, eventTmp.getY() + walkDy);
                    }
                    eventTmp.getAnimation().update();
                }
                Player.getAnimation().update();
                redraw();
                sleep(1000 / 60);
            }
        }
    }

    @ScriptCommand(name = "change_tile")
    public static void changeTile(int col, int row, int tileId) {
        if (currentMap != null) {
            currentMap.setTile(row, col, tileId);
        }
    }
    
}
