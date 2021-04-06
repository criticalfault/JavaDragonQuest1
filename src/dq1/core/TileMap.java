package dq1.core;

import static dq1.core.Event.MovementType.*;
import static dq1.core.Settings.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TileMap class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class TileMap {
    
    private final String id;
    private final String name;
    private final int cols;
    private final int rows;
    private final String musicId;
    private final Tile outOfBoundsTileId;
    private boolean isDark;
    private boolean repelHasNoEffect;
    private boolean resetRepelOnEnter;
    private boolean resetLightOnEnter;
    private boolean enemiesEncounterEnabled = true;
    
    private List<Area> areas = new ArrayList<>();
    private Area currentArea;
    
    private final Tile[][] tiles;
    private final List<Event> events = new ArrayList<Event>();
    
    private int zoneRows;
    private int zoneCols;
    private int zoneTilesPerRow;
    private int zoneTilesPerCol;
    private int zoneOffsetRows;
    private int zoneOffsetCols;
    private int[][] zoneTable;
    
    private Map<Integer, List<Enemy>> zoneEnemies;
    
    private Map<Integer, Tile> tileSet;

    //#map_area 1 1 12 12
    //map_out_area_teleport overworld 15 13 right
    public static class Area extends Rectangle {
        public String teleportToMapId;
        
        // if player is out of this area teleport to ...
        public int teleportLocationRow;
        public int teleportLocationCol;
        public String teleportPlayerDirection;
        public boolean useFadeEffect;
        public String musicId;
        public boolean isDark;
        public boolean repelHasNoEffect;
        public boolean resetRepel;
        public boolean resetLight;
        
        public Area(int col1, int row1, int col2, int row2 
                , String teleportToMapId, int teleportLocationCol
                , int teleportLocationRow, String teleportPlayerDirection
                , boolean useFadeEffect, String musicId, boolean isDark
                , boolean repelHasNoEffect
                , boolean resetRepel, boolean resetLight) {
            
            super(col1 * 16, row1 * 16
                    , (col2 - col1 + 1) * 16, (row2 - row1 + 1) * 16);
            
            this.teleportToMapId = teleportToMapId;
            this.teleportLocationCol = teleportLocationCol;
            this.teleportLocationRow = teleportLocationRow;
            this.teleportPlayerDirection = teleportPlayerDirection;
            this.useFadeEffect = useFadeEffect;
            this.musicId = musicId;
            this.isDark = isDark;
            this.repelHasNoEffect = repelHasNoEffect;
            this.resetRepel = resetRepel;
            this.resetLight = resetLight;
        }
    }
    
    public TileMap(String id, String name, int cols, int rows
            , String musicId, Tile outOfBoundsTileId, Tile[][] tiles) {
        
        this.id = id;
        this.name = name;
        this.cols = cols;
        this.rows = rows;
        this.outOfBoundsTileId = outOfBoundsTileId;
        this.musicId = musicId;
        this.tiles = tiles;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public String getMusicId() {
        return musicId;
    }
    
    public Tile getTile(int row, int col) {
        if (row < 0 || col < 0 || row > rows - 1 || col > cols - 1) {
            return outOfBoundsTileId;
        }
        return tiles[row][col];
    }

    public void setTile(int row, int col, int tileId) {
        if (row < 0 || col < 0 || row > rows - 1 || col > cols - 1) {
            return;
        }
        Tile tile = tileSet.get(tileId);
        if (tile != null) {
            tiles[row][col] = tile;
        }
    }
    
    public boolean isDark() {
        return isDark;
    }

    public void setDark(boolean isDark) {
        this.isDark = isDark;
    }

    public boolean isRepelHasNoEffect() {
        return repelHasNoEffect;
    }

    public void setRepelHasNoEffect(boolean repelHasNoEffect) {
        this.repelHasNoEffect = repelHasNoEffect;
    }

    public boolean isResetRepelOnEnter() {
        return resetRepelOnEnter;
    }

    public void setResetRepelOnEnter(boolean resetRepelOnEnter) {
        this.resetRepelOnEnter = resetRepelOnEnter;
    }

    public boolean isResetLightOnEnter() {
        return resetLightOnEnter;
    }

    public void setResetLightOnEnter(boolean resetLightOnEnter) {
        this.resetLightOnEnter = resetLightOnEnter;
    }

    public boolean isEnemiesEncounterEnabled() {
        return enemiesEncounterEnabled;
    }

    public void setEnemiesEncounterEnabled(boolean enemiesEncounterEnabled) {
        this.enemiesEncounterEnabled = enemiesEncounterEnabled;
    }

    public void addArea(Area area) {
        areas.add(area);
    }
    
    public void selectAreaAccordingToPlayerLocation() {
        currentArea = null;
        for (Area area : areas) {
            if (area.contains(Player.getX(), Player.getY())) {
                currentArea = area;
                return;
            }
        }
    }

    public Area getCurrentArea() {
        return currentArea;
    }
    
    public boolean isBlocked(int x, int y) {
        int row = y / 16;
        int col = x / 16;
        Tile tile = getTile(row, col);
        if (tile.isBlocked()) {
            return true;
        }
        for (Event event : events) {
            if (event.isBlocked(row, col)) {
                return true;
            }
        }
        return Player.isBlocked(row, col);
    }
    
    public void addEvent(Event event) {
        events.add(event);
    }

    public void addEvents(List<Event> mapEvents) {
        events.addAll(mapEvents);
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setZonesData(int zoneRows, int zoneCols
            , int zoneTilesPerRow, int zoneTilesPerCol
            , int zoneOffsetRows, int zoneOffsetCols, int[][] zoneTable) {
        
        this.zoneRows = zoneRows;
        this.zoneCols = zoneCols;
        this.zoneTilesPerRow = zoneTilesPerRow;
        this.zoneTilesPerCol = zoneTilesPerCol;
        this.zoneOffsetRows = zoneOffsetRows;
        this.zoneOffsetCols = zoneOffsetCols;
        this.zoneTable = zoneTable;
    }

    public void setZoneEnemiesData(Map<Integer, List<Enemy>> zoneEnemies) {
        this.zoneEnemies = zoneEnemies;
    }
    
    public void update() {
        for (Event event : events) {
            if (event.isVisible()) {
                event.update();
            }
        }
    }

    public void clearAllLocalVars() throws Exception {
        for (Event event : events) {
            event.clearLocalVars();
        }
    }

    public void executeAllEvents(String label) throws Exception {
        for (Event event : events) {
            event.execute(label);
        }
    }
    
    public boolean checkEventTriggered() throws Exception {
        boolean fireJustPressed = Input.isKeyJustPressed(KEY_CONFIRM);
        for (Event event : events) {
            if (Player.checkEventTrigger(event, fireJustPressed)) {
                if (event.getMovementType() == RANDOM) {
                    event.turnTo(Player.getY() / 16, Player.getX() / 16);
                    Game.redraw();
                }
                boolean exitMap = event.execute("on_event_trigger");
                return exitMap;
            };
        }
        return false;
    }
    
    public boolean canPlayerEncounterEnemyAtCurrentLocation() {
        if (zoneTable == null || zoneEnemies == null) {
            return false;
        }
        int zoneRow = (Player.getY() / 16 - zoneOffsetRows) / zoneTilesPerRow;
        int zoneCol = (Player.getX() / 16 - zoneOffsetCols) / zoneTilesPerCol;
        int zoneId = zoneTable[zoneRow][zoneCol];
        List<Enemy> enemies = zoneEnemies.get(zoneId);
        return enemies != null && !enemies.isEmpty();
    }
    
    public Enemy getZoneEnemy() {
        if (zoneTable == null || zoneEnemies == null) {
            return null;
        }
        int zoneRow = (Player.getY() / 16 - zoneOffsetRows) / zoneTilesPerRow;
        int zoneCol = (Player.getX() / 16 - zoneOffsetCols) / zoneTilesPerCol;
        int zoneId = zoneTable[zoneRow][zoneCol];
        List<Enemy> enemies = zoneEnemies.get(zoneId);
        Enemy enemy = null;
        if (enemies != null) {
            enemy = enemies.get(Util.random(enemies.size()));
        }
        return enemy;
    }

    public void setTileSet(Map<Integer, Tile> tileSet) {
        this.tileSet = tileSet;
    }

    private static final boolean DEBUG_MODE = false;
    private static final Color DEBUG_AREA_COLOR = new Color(0, 255, 0, 128);
    
    public void draw(Graphics2D g) {
        int startCol = Player.getX() / 16 - 7;
        int startRow = Player.getY() / 16 - 7;
        int dx = Player.getX() % 16 - 8;
        int dy = Player.getY() % 16;
        for (int row = -1; row < 16; row++) {
            for (int col = -1; col < 17; col++) {
                Tile tile = getTile(startRow + row, col + startCol);
                int ix = col * 16 - dx;
                int iy = row * 16 - dy;
                g.drawImage(tile.getImage(), ix, iy, null);
            }
        }
        events.forEach(event -> {
            if (event.isVisible()) {
                event.draw(g);
            }
        });
        
        // debug walking area
        if (DEBUG_MODE && currentArea != null) {
            g.setColor(DEBUG_AREA_COLOR);
            g.fillRect(currentArea.x - Player.getX() + 8 * 16 - 8
                , currentArea.y - Player.getY() + 7 * 16
                , currentArea.width, currentArea.height);
        }        
    }

    private static final String[] MAIN_MENU_OPTIONS = new String[6];
    private static final String[] MAIN_MENU_EXIT_OPTIONS = new String[2];
    
    public static void showMainMenu() throws Exception {
        Player.showSimplifiedStatus();
        
        MAIN_MENU_OPTIONS[0] = Game.getText("@@map_menu_item");
        MAIN_MENU_OPTIONS[1] = Game.getText("@@map_menu_spell");
        MAIN_MENU_OPTIONS[2] = Game.getText("@@map_menu_status");
        MAIN_MENU_OPTIONS[3] = Game.getText("@@map_menu_config");
        MAIN_MENU_OPTIONS[4] = Game.getText("@@map_menu_load_game");
        MAIN_MENU_OPTIONS[5] = Game.getText("@@map_menu_quit");
        boolean exit = false;
        int option = 0;
        while (!exit) {
            option = Dialog.showOptionsMenu(
                    3, false, 9, 3, 0, 0, -1, option, MAIN_MENU_OPTIONS);

            if (option >= 0) {
                switch (option) {
                    case 0: exit = handleMenuItemOption(); break;
                    case 1: exit = handleMenuSpellOption(); break;
                    case 2: Player.showCompleteStatus(); break;
                    case 3: Game.showOptionsMenu(); break;
                    case 4: exit = Game.loadGame(); break;
                    case 5: handleMenuQuitOption(); break;
                }
                // if you still don't have any items, a message will be shown
                // if you still don't have any spells, a message will be shown
                Dialog.close();
            }
            else {
                exit = true;
            }
        }
        hideMainMenu();
        Player.hideSimplifiedStatus();
    }        

    private static void hideMainMenu() {
        Dialog.hideOptionsMenu(3, 9, 3, 0, 0, MAIN_MENU_OPTIONS);
    }

    private static boolean handleMenuItemOption() throws Exception {
        Item item = Inventory.showSelectItemDialog();
        boolean sucess = false;
        if (item != null) {
            hideMainMenu();
            if (!item.isUseInMap()) {
                Dialog.print(0, 0, Game.getText("@@item_cant_use_in_field"));
                return false;
            }
            else {
                sucess = item.use("on_use_when_map");
                Player.showSimplifiedStatus();
            }
        }
        return sucess;
    }
    
    private static boolean handleMenuSpellOption() throws Exception {
        Spell spell = Magic.showSelectSpellDialog();
        hideMainMenu();
        boolean sucess = false;
        if (spell != null) {
            if (!spell.isUseInMap()) {
                Dialog.print(0, 1, Game.getText("@@spell_cant_use_in_field"));
                return true;
            } 
            else if (Player.getMP() < spell.getMp()) {
                Dialog.print(0, 1, Game.getText("@@player_mp_too_low"));
                return true;
            }
            Script.setGlobalValue("$$casting_spell_name", spell.getName());
            Dialog.print(0, 0, Game.getText("@@player_cast_spell"));
            Audio.playSound(Audio.SOUND_SPELL);
            View.flash(4, 5, Color.WHITE, 30);
            Player.consumeMP(spell.getMp());
            Player.showSimplifiedStatus();
            sucess = spell.cast("on_use_when_map");
            Player.showSimplifiedStatus();
        }
        return sucess;
    }
    
    private static boolean handleMenuQuitOption() {
        MAIN_MENU_EXIT_OPTIONS[0] = Game.getText("@@option_yes");
        MAIN_MENU_EXIT_OPTIONS[1] = Game.getText("@@option_no");
        Dialog.print(0, 0, Game.getText("@@map_menu_quit_confirmation"));
        int exit = Dialog.showOptionsMenu(
                4, true, 15, 9, 0, 0, 1, 1, MAIN_MENU_EXIT_OPTIONS);
        if (exit == 0) {
            View.fadeOut();
            Game.sleep(500);
            Game.exitGame();
            return true;
        }
        Dialog.close();
        return false;
    }
    
}
