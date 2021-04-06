package dq1.core;

import dq1.core.Audio.Music;
import dq1.core.Event.MovementType;
import static dq1.core.EventFactory.*;
import static dq1.core.Settings.*;
import dq1.core.TileMap.Area;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import static javax.sound.midi.ShortMessage.*;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;

/**
 * Resource class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Resource {
    
    private static final Map<String, BufferedImage> IMAGES;
    private static final Map<String, Map<Integer, Tile>> TILE_SETS;
    private static final Map<String, TileMap> TILE_MAPS;
    private static final Map<String, List<Event>> MAP_EVENTS;
    private static final Map<Integer, Enemy> ENEMIES;
    private static final Map<Integer, Item> ITEMS;
    private static final Map<Integer, Spell> SPELLS;
    private static final Map<Integer, PlayerLevel> PLAYER_LEVELS;
    private static final Map<String, Music> MUSICS = new HashMap<>();
    
    static {
        IMAGES = new HashMap<>();
        TILE_SETS = new HashMap<>();
        TILE_MAPS = new HashMap<>();
        MAP_EVENTS = new HashMap<>();
        ENEMIES = new HashMap<>();
        ITEMS = new HashMap<>();
        SPELLS = new HashMap<>();
        PLAYER_LEVELS = new HashMap<>();
    }

    public static Map<String, BufferedImage> getIMAGES() {
        return IMAGES;
    }

    public static Map<String, Map<Integer, Tile>> getTILE_SETS() {
        return TILE_SETS;
    }

    public static Map<String, TileMap> getTILE_MAPS() {
        return TILE_MAPS;
    }

    public static Map<String, List<Event>> getMAP_EVENTS() {
        return MAP_EVENTS;
    }

    public static Map<Integer, Enemy> getENEMIES() {
        return ENEMIES;
    }

    public static Map<Integer, Item> getITEMS() {
        return ITEMS;
    }

    public static Map<Integer, Spell> getSPELLS() {
        return SPELLS;
    }

    public static Map<Integer, PlayerLevel> getPLAYER_LEVELS() {
        return PLAYER_LEVELS;
    }
    
    public static BufferedImage getImage(String res) {
        BufferedImage image = IMAGES.get(res);
        if (image == null) {
            res = RES_IMAGE_PATH + res + RES_IMAGE_FILE_EXT;
            try (InputStream is = Resource.class.getResourceAsStream(res)) {
                image = ImageIO.read(is);
                IMAGES.put(res, image);
            } catch (IOException ex) {
                Logger.getLogger(
                        Resource.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        }
        return image;
    }

    public static Map<Integer, Tile> getTileSet(String res) {
        Map<Integer, Tile> tileSet = TILE_SETS.get(res);
        if (tileSet == null) {
            tileSet = new HashMap<>();
            String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            Resource.class.getResourceAsStream(resTmp)))) {
                
                int tileId = -1;
                BufferedImage tileSetImage = null;
                BufferedImage tileImage = null;
                int tileWidth = -1;
                int tileHeight = -1;
                boolean tileBlocked = false;
                int tileDamagePerStep = 0;
                int enemyProbabilityNumerator = 0;
                int enemyProbabilityDenominator = 1;
                BufferedImage tileBattleBackground = null;
                String imageFilename = null;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    //System.out.println(line);

                    if (imageFilename == null) {
                        imageFilename = line;
                        tileSetImage = getImage(imageFilename);
                        continue;
                    }
                    else if (tileWidth < 0) {
                        tileWidth = Integer.parseInt(line);
                        continue;
                    }
                    else if (tileHeight < 0) {
                        tileHeight = Integer.parseInt(line);
                        continue;
                    }

                    String[] fields = line.split(",");
                    tileId = Integer.parseInt(fields[0]);
                    int tsiCols = tileSetImage.getWidth() / 16;
                    int tsiX = (tileId % tsiCols) * 16;
                    int tsiY = (tileId / tsiCols) * 16;
                    tileImage = tileSetImage.getSubimage(tsiX, tsiY, 16, 16);
                    for (int i = 1; i < fields.length; i++) {
                        String field = fields[i];
                        String propertyName = field.split("=")[0].trim();
                        String propertyValue = field.split("=")[1].trim();
                        if (propertyName.equals("blocked")) {
                            tileBlocked = Boolean.valueOf(propertyValue);
                        }
                        else if (propertyName.equals("damagePerStep")) {
                            tileDamagePerStep = Integer.valueOf(propertyValue);
                        }
                        else if (propertyName.equals("enemy_probability")) {
                            String[] d = propertyValue.split("/");
                            enemyProbabilityNumerator = Integer.valueOf(d[0]);
                            enemyProbabilityDenominator = Integer.valueOf(d[1]);
                        }
                        else if (propertyName.equals("battleBackgroundId")) {
                            tileBattleBackground = Battle.getBackgrounds()
                                .getFrameImage(Integer.valueOf(propertyValue));
                        }
                        else {
                            Logger.getLogger(Resource.class.getName())
                                .log(Level.WARNING, "tileset '" + res 
                                + "' invalid property " + propertyName + " !");
                        }
                    }
                    Tile tile = new Tile(tileId, tileImage, tileBlocked
                        , tileDamagePerStep, tileBattleBackground
                        , enemyProbabilityNumerator
                        , enemyProbabilityDenominator);

                    tileSet.put(tileId, tile);
                    TILE_SETS.put(res, tileSet);
                }
            }   
            catch (IOException ex) {            
                Logger.getLogger(
                        Resource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tileSet;
    }

    public static TileMap getTileMap(String res) throws Exception {
        TileMap tileMap = TILE_MAPS.get(res);
        if (tileMap == null) {
            String resTmp = RES_MAP_PATH + res + RES_MAP_FILE_EXT;
            try (InputStream is = Resource.class.getResourceAsStream(resTmp);
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                String mapName = null;
                Map<Integer, Tile> mapTileSet = null;
                String mapMusicId = null;
                int mapOutOfBoundsTileId = 0;
                boolean mapIsDark = false;
                boolean mapRepelHasNoEffect = false;
                boolean mapResetRepelOnEnter = false;
                boolean mapResetLightOnEnter = false;
                List<Area> areas = new ArrayList<>();
                //String areaOutTeleportMapId = null;
                //int areaOutTeleportPlayerCol = -1;
                //int areaOutTeleportPlayerRow = -1;
                //String areaOutTeleportPlayerDirection = null;
                int mapRows = 0;
                int mapCols = 0;
                Tile[][] mapData;
                int zoneRows = 0;
                int zoneCols = 0;
                int zoneTilesPerRow = 0;
                int zoneTilesPerCol = 0;
                int zoneOffsetRows = 0;
                int zoneOffsetCols = 0;
                int[][] zoneData = null;
                
                int zoneEnemiesRows = 0;
                int zoneEnemiesCols = 0;
                Map<Integer, List<Enemy>> zoneEnemiesData = null;
        
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] args = line.split("\\ ");
                    // map data
                    if (args[0].equals("map_name")) {
                        mapName = args[1];
                    }
                    else if (args[0].equals("map_tileset")) {
                        mapTileSet = getTileSet(args[1]);
                    }
                    else if (args[0].equals("map_music")) {
                        mapMusicId = args[1];
                    }
                    else if (args[0].equals("map_out_of_bounds_tile_id")) {
                        mapOutOfBoundsTileId = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("map_is_dark")) {
                        mapIsDark = Boolean.parseBoolean(args[1]);
                    }
                    else if (args[0].equals("map_repel_has_no_effect")) {
                        mapRepelHasNoEffect = Boolean.parseBoolean(args[1]);
                    }
                    else if (args[0].equals("map_reset_repel_on_enter")) {
                        mapResetRepelOnEnter = Boolean.parseBoolean(args[1]);
                    }
                    else if (args[0].equals("map_reset_light_on_enter")) {
                        mapResetLightOnEnter = Boolean.parseBoolean(args[1]);
                    }
                    else if (args[0].equals("map_area")) {
                        int areaCol1 = Integer.parseInt(args[1]);
                        int areaRow1 = Integer.parseInt(args[2]);
                        int areaCol2 = Integer.parseInt(args[3]);
                        int areaRow2 = Integer.parseInt(args[4]);
                        String areaOutTeleportMapId = args[5];
                        int areaOutTeleportPlayerCol 
                                                = Integer.parseInt(args[6]);
                        
                        int areaOutTeleportPlayerRow 
                                                = Integer.parseInt(args[7]);
                        
                        String areaOutTeleportPlayerDirection = args[8];
                        boolean areaUseFadeEffect
                                                = Boolean.parseBoolean(args[9]);
                        String areaMusicId = args[10];
                        boolean areaIsDark = Boolean.parseBoolean(args[11]);
                        boolean areaRepelHasNoEffect 
                                            = Boolean.parseBoolean(args[12]);
                        
                        boolean areaResetRepel = Boolean.parseBoolean(args[13]);
                        boolean areaResetLight = Boolean.parseBoolean(args[14]);
                        
                        Area area = new Area(areaCol1, areaRow1
                            , areaCol2, areaRow2, areaOutTeleportMapId
                            , areaOutTeleportPlayerCol, areaOutTeleportPlayerRow
                            , areaOutTeleportPlayerDirection, areaUseFadeEffect 
                            , areaMusicId, areaIsDark, areaRepelHasNoEffect
                            , areaResetRepel, areaResetLight);
                        areas.add(area);
                    }
                    else if (args[0].equals("map_out_area_teleport")) {
                    }
                    else if (args[0].equals("map_rows")) {
                        mapRows = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("map_cols")) {
                        mapCols = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("map_data")) {
                        mapData = new Tile[mapRows][mapCols];
                        for (int row = 0; row < mapRows; row++) {
                            line = br.readLine().trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            String[] colTileIds = line.split("\\,");
                            for (int col = 0; col < mapCols; col++) {
                                int tileId 
                                    = Integer.parseInt(colTileIds[col].trim());
                                
                                Tile tile = mapTileSet.get(tileId);
                                mapData[row][col] = tile;
                            }
                        }
                        tileMap = new TileMap(res, mapName
                            , mapCols, mapRows, mapMusicId
                            , mapTileSet.get(mapOutOfBoundsTileId), mapData);
                        tileMap.setDark(mapIsDark);
                        tileMap.setRepelHasNoEffect(mapRepelHasNoEffect);
                        tileMap.setResetRepelOnEnter(mapResetRepelOnEnter);
                        tileMap.setResetLightOnEnter(mapResetLightOnEnter);
                        tileMap.setTileSet(mapTileSet);
                        for (Area area : areas) {
                            tileMap.addArea(area);
                        }
//                        tileMap.setOutAreaInfo(areaOutTeleportMapId
//                                , areaOutTeleportPlayerCol
//                                , areaOutTeleportPlayerRow
//                                , areaOutTeleportPlayerDirection);
                    }
                    // zones data
                    else if (args[0].equals("zone_rows")) {
                        zoneRows = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_cols")) {
                        zoneCols = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_offset_rows")) {
                        zoneOffsetRows = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_offset_cols")) {
                        zoneOffsetCols = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_tiles_per_row")) {
                        zoneTilesPerRow = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_tiles_per_col")) {
                        zoneTilesPerCol = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_data")) {
                        zoneData = new int[zoneRows][zoneCols];
                        for (int row = 0; row < zoneRows; row++) {
                            line = br.readLine().trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            String[] colZonesIds = line.split("\\,");
                            for (int col = 0; col < zoneCols; col++) {
                                int zoneId 
                                    = Integer.parseInt(colZonesIds[col].trim());
                                
                                zoneData[row][col] = zoneId;
                            }
                        }
                        tileMap.setZonesData(zoneRows, zoneCols
                            , zoneTilesPerRow, zoneTilesPerCol
                            , zoneOffsetRows, zoneOffsetCols, zoneData);
                    }        
                    else if (args[0].equals("zone_enemies_rows")) {
                        zoneEnemiesRows = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_enemies_cols")) {
                        zoneEnemiesCols = Integer.parseInt(args[1]);
                    }
                    else if (args[0].equals("zone_enemies_data")) {
                        zoneEnemiesData = new HashMap<>();
                        for (int row = 0; row < zoneEnemiesRows; row++) {
                            line = br.readLine().trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            String[] colEnemiesIds = line.split("\\,");
                            int zoneId = Integer.parseInt(
                                    colEnemiesIds[0].trim(), 16);
                            List<Enemy> enemies = new ArrayList<>();
                            zoneEnemiesData.put(zoneId, enemies);
                            int colMax = zoneEnemiesCols + 1;
                            for (int col = 1; col < colMax; col++) {
                                int monsterId  = Integer.parseInt(
                                        colEnemiesIds[col].trim(), 16);
                                enemies.add(Resource.getEnemyById(monsterId));
                            }
                        }
                        tileMap.setZoneEnemiesData(zoneEnemiesData);
                    }        

                    else if (args[0].startsWith("exit")) {
                        break;
                    };
                }
                
                tileMap.addEvents(getMapEvents(tileMap, res));
                TILE_MAPS.put(res, tileMap);
            } catch (IOException ex) {
                Logger.getLogger(View.class.getName())
                        .log(Level.SEVERE, null, ex);
                
                System.exit(-1);
            }
        }
        return tileMap;
    }
    
    public static List<Event> getMapEvents(
            TileMap map, String res) throws Exception {
        
        List<Event> mapEvents = MAP_EVENTS.get(res);
        if (mapEvents == null) {
            mapEvents = new ArrayList<>();
            MAP_EVENTS.put(res, mapEvents);
            String resTmp = RES_EVENT_PATH + res + RES_EVENT_FILE_EXT;
            try (InputStream is = Resource.class.getResourceAsStream(resTmp);
                 InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader br = new BufferedReader(isr)) {

                Event event = null;
                String line = "";
                String lineTmp;
                boolean newLine = true;
                while ((lineTmp = br.readLine()) != null) {
                    lineTmp = lineTmp.trim();
                    if (lineTmp.isEmpty() || lineTmp.startsWith(";")) {
                        continue;
                    }
                    if (newLine) {
                        line = "";
                        newLine = false;
                    }
                    if (lineTmp.endsWith("\\")) {
                        line += lineTmp.substring(0, lineTmp.length() - 1);
                        continue;
                    }
                    else {
                        line += lineTmp;
                    }
                    newLine = true;
                    
                    // System.out.println("line: " + line);
                    
                    String[] args = line.split("\\s+");
                    
                    if (args[0].equals("npc")) {
                        mapEvents.add(createNpcEvent(args));
                    }
                    else if (args[0].equals("door")) {
                        mapEvents.add(createDoorEvent(map, args));
                    }
                    else if (args[0].equals("teleport")) {
                        mapEvents.add(createTeleportEvent(args));
                    }
                    else if (args[0].equals("chest")) {
                        mapEvents.add(createChestEvent(map, args));
                    }
                    else if (args[0].equals("item_on_ground")) {
                        mapEvents.add(createItemOnGroundEvent(map, args));
                    }
                    else if (args[0].equals("save_point")) {
                        mapEvents.add(createSavePointEvent(args));
                    }
                    else if (args[0].equals("inn")) {
                        mapEvents.add(createInnEvent(args));
                    }
                    else if (args[0].equals("shop_item")) {
                        mapEvents.add(createShopItemEvent(args));
                    }
                    else if (args[0].equals("shop_equip")) {
                        mapEvents.add(createShopEquipEvent(args));
                    }
                    else if (args[0].equals("shop_keys")) {
                        mapEvents.add(createShopKeysEvent(args));
                    }
                    else if (args[0].equals("shop_fairy_water")) {
                        mapEvents.add(createShopFairyWaterEvent(args));
                    }
                    else if (line.startsWith("event ")) {
                        event = new Event();
                        event.setId(args[1]);
                    }
                    else if (line.startsWith("animation ")) {
                        int cols = Integer.parseInt(args[2]);
                        int rows = Integer.parseInt(args[3]);
                        int animationSpeed = Integer.parseInt(args[4]);
                        event.setAnimation(Resource.getImage(args[1])
                                            , cols, rows, animationSpeed);
                    }
                    else if (line.startsWith("create_animation ")) {
                        String animationId = args[1];
                        int[] animationFrames = new int[args.length - 2];
                        for (int i = 0; i < animationFrames.length; i++) {
                            animationFrames[i] = Integer.parseInt(args[i + 2]);
                        }
                        event.createAnimation(animationId, animationFrames);
                    }
                    else if (line.startsWith("location ")) {
                        int x = 16 * Integer.parseInt(args[1]);
                        int y = 16 * Integer.parseInt(args[2]);
                        event.setLocation(x, y);
                    }
                    else if (line.startsWith("walk_area ")) {
                        int x = 16 * Integer.parseInt(args[1]);
                        int y = 16 * Integer.parseInt(args[2]);
                        int width = 16 * Integer.parseInt(args[3]);
                        int height = 16 * Integer.parseInt(args[4]);
                        event.setWalkArea(x, y, width, height);
                    }
                    else if (line.startsWith("movement ")) {
                        event.setMovementType(MovementType.valueOf(args[1]));
                    }
                    else if (line.startsWith("visible ")) {
                        event.setVisible(Boolean.valueOf(args[1]));
                    }
                    else if (line.startsWith("blocked ")) {
                        event.setBlocked(Boolean.valueOf(args[1]));
                    }
                    else if (line.startsWith("fire_required ")) {
                        event.setFireRequired(Boolean.valueOf(args[1]));
                    }
                    else if (line.equals("script")) {
                        Script script = new Script(br);
                        event.setScript(script);
                    }
                    else if (line.equals("event_end")) {
                        mapEvents.add(event);
                        event = null;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(View.class.getName())
                        .log(Level.SEVERE, null, ex);
                
                System.exit(-1);
            }
        }
        return mapEvents;
    }
    
    public static Properties getTexts(String res) {
        Properties texts = new Properties();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try {
            texts.load(Resource.class.getResourceAsStream(resTmp));
        } catch (IOException ex) {
            Logger.getLogger(Resource.class.getName())
                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
        return texts;
    }
    
    public static void loadEnemies(String res) {
        ENEMIES.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                Enemy enemy = new Enemy(line);
                ENEMIES.put(enemy.getId(), enemy);
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }

    public static Enemy getEnemyById(int enemyId) {
        return ENEMIES.get(enemyId);
    }
    
    public static void loadItems(String res) {
        ITEMS.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {
            
            Item item = null;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                if (line.startsWith("item ") || line.startsWith("weapon ")
                        || line.startsWith("armor ") 
                        || line.startsWith("shield ")) {
                    
                    item = new Item(line);
                    ITEMS.put(item.getId(), item);
                }
                else if (line.startsWith("parent_items ")) {
                    String[] data = line.split("\\ ");
                    for (int i = 1; i < data.length; i++) {
                        item.addParentItem(Integer.parseInt(data[i]));
                    }
                }
                else if (line.equals("script")) {
                    Script script = new Script(br);
                    item.setScript(script);
                }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }

    public static Item getItemById(int itemId) {
        return ITEMS.get(itemId);
    }
    
    public static void loadSpells(String res) {
        SPELLS.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            Spell spell = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                if (line.startsWith("spell ")) {
                    spell = new Spell(line);
                    SPELLS.put(spell.getId(), spell);
                }
                else if (line.equals("script")) {
                    Script script = new Script(br);
                    spell.setScript(script);
                }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }

    public static Spell getSpellById(int spellId) {
        return SPELLS.get(spellId);
    }

    public static void loadPlayerLevels(String res) {
        PLAYER_LEVELS.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                if (line.startsWith("level_last ")) {
                    String[] args = line.split("\\s+");
                    PlayerLevel.lastLevel = Integer.parseInt(args[1]);
                }
                else if (line.startsWith("level ")) {
                    PlayerLevel playerLevel = new PlayerLevel(line);
                    PLAYER_LEVELS.put(playerLevel.getLv(), playerLevel);
                }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }

    public static PlayerLevel getPlayerLevel(int level) {
        return PLAYER_LEVELS.get(level);
    }
    
    public static Soundbank loadSoundBank(String res) {
        String resTmp = RES_SOUND_PATH + res + RES_SOUND_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp)) {
            BufferedInputStream bis = new BufferedInputStream(is);
            return MidiSystem.getSoundbank(bis);
        } 
        catch (IOException | InvalidMidiDataException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
        return null;
    }

    public static Sequence loadMusicSequence(String midiFile) {
        String res = RES_MUSIC_PATH + midiFile + RES_MUSIC_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(res)) {
            Sequence sequence = MidiSystem.getSequence(is);
            
            // remove the volume control messages to allow 
            // to change volume from the program
            List<MidiEvent> controlEventsToRemove = new ArrayList<>();
            for (Track track : sequence.getTracks()) {
                controlEventsToRemove.clear();
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent me = track.get(i);
                    MidiMessage mm = me.getMessage();
                    boolean controlMsg = 
                            (mm.getStatus() & CONTROL_CHANGE) == CONTROL_CHANGE;
                    
                    boolean timeMsg = 
                            (mm.getStatus() & MIDI_TIME_CODE) == MIDI_TIME_CODE;
                    
                    if (controlMsg && !timeMsg) {
                        controlEventsToRemove.add(me);
                    }
                }
                controlEventsToRemove.forEach(me -> track.remove(me));
            }
            
            return sequence;
        } 
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
        return null;
    }
    
    //id midi_file start_tick_position loop_start_tick loop_end_tick        

    public static void loadMusics(String res) {
        MUSICS.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            Music music = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                if (line.startsWith("music ")) {
                    music = new Music(line);
                    MUSICS.put(music.id, music);
                }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(View.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }
    
    public static Music getMusic(String musicId) {
        return MUSICS.get(musicId);
    }
    
}
