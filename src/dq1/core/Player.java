package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player {
    
    private static Animation animation;
    private static int walking = -1;
    private static int walkDx;
    private static int walkDy;
    private static final Point CURRENT_WALK_TILE = new Point();
    private static final Point TARGET_WALK_TILE = new Point();
    private static boolean showingStatus;
    private static int accumulatedHealInMilli;
    
    public static void start() {
        BufferedImage tileset = Resource.getImage("chars");
        int tilesetCols = tileset.getWidth() / 20;
        int tilesetRows = tileset.getHeight() / 20;
        animation = new Animation(tileset, tilesetCols, tilesetRows, 100);
        animation.createAnimation("normal_down", new int[] { 0, 1 });
        animation.createAnimation("normal_left", new int[] { 2, 3 });
        animation.createAnimation("normal_up", new int[] { 4, 5 });
        animation.createAnimation("normal_right", new int[] { 6, 7 });

        animation.createAnimation("princess_down", new int[] { 64, 65 });
        animation.createAnimation("princess_left", new int[] { 66, 67 });
        animation.createAnimation("princess_up", new int[] { 68, 69 });
        animation.createAnimation("princess_right", new int[] { 70, 71 });
        
        Script.setGlobalValue("$$player_name", "HIRO");
        Script.setGlobalValue("$$player_state", "normal");
        
        Script.setGlobalValue("##player_visible", 1);
        Script.setGlobalValue("##player_x", 0);
        Script.setGlobalValue("##player_y", 0);

        Script.setGlobalValue("$$player_animation_id", "");
        Script.setGlobalValue("$$player_direction", "");

        Script.setGlobalValue("$$player_outside_map", "");
        Script.setGlobalValue("$$player_outside_music_id", "");
        Script.setGlobalValue("##player_outside_row", 0);
        Script.setGlobalValue("##player_outside_col", 0);
        
        //level  1 ,   4 ,   4 ,  15 ,   0 ,     0
        int lv = 1;
        int str = 4;
        int agi = 4;
        int hp = 15;
        int mp = 0;
        int gp = 0;
        int xp = 0;
        
        Script.setGlobalValue("##player_str", str);
        Script.setGlobalValue("##player_agi", agi);
        Script.setGlobalValue("##player_max_hp", hp);
        Script.setGlobalValue("##player_max_mp", mp);
        
        Script.setGlobalValue("##player_last_lv", PlayerLevel.lastLevel);
        Script.setGlobalValue("##player_lv", lv);
        Script.setGlobalValue("##player_hp", hp);
        Script.setGlobalValue("##player_mp", mp);
        Script.setGlobalValue("##player_g", gp);
        Script.setGlobalValue("##player_e", xp);
        
        Script.setGlobalValue("##player_weapon_id", 0);
        Script.setGlobalValue("##player_armor_id", 0);
        Script.setGlobalValue("##player_shield_id", 0);

        Script.setGlobalValue("##player_status_asleep", 0);
        Script.setGlobalValue("##player_status_stopspell", 0);
        
        Script.setGlobalValue("##player_extra_atk", 0);
        Script.setGlobalValue("##player_extra_def", 0);
        
        // = "{60}{61}"
        Script.setGlobalValue("$$player_curses", "");
        
        Script.setGlobalValue("##player_repel_counter", 0);
        Script.setGlobalValue("$$player_repel_source", "");

        Script.setGlobalValue("##player_disabled_event_row", 0);
        Script.setGlobalValue("##player_disabled_event_col", 0);

        Script.setGlobalValue("##player_movement_locked", 0);
        
        Script.setGlobalValue("$$player_trigger_event_id_on_key_press", "");
        Script.setGlobalValue("$$player_trigger_event_label_on_key_press", "");

        Script.setGlobalValue("##player_start_time_ms"
                                                , System.currentTimeMillis());
        
        Script.setGlobalValue("##player_accumulated_time_ms", 0);
        
        Script.setGlobalValue("##player_light_remaining_steps", 0);

        Script.setGlobalValue("##player_xp_for_next_lv", 0);
        updateXPForNextLv();

        Script.setGlobalValue("##player_just_teleported", 0);
        Script.setGlobalValue("##player_just_died", 0);
        Script.setGlobalValue("##battle_enabled", 1);
        Script.setGlobalValue("##player_can_encounter_enemy", 0);
        Script.setGlobalValue("##player_put_fighters_ring", 0);
        
        // final boss defeated ?
        Script.setGlobalValue("##game_state_dragon_lord_defeated", 0);
        
        // 0=not rescued 
        // 1=rescued, player is carrying princess
        // 2=rescued, princess is already in tantegel castle
        Script.setGlobalValue("##game_state_princess_rescued", 0);
    }

    public static Animation getAnimation() {
        return animation;
    }

    public static int getWalkDx() {
        return walkDx;
    }

    public static int getWalkDy() {
        return walkDy;
    }
    
    @ScriptCommand(name = "change_player_animation")
    public static void changeAnimation(String animationId) {
        animation.change(getState() + "_" + animationId);
        Script.setGlobalValue("$$player_animation_id", animationId);
    }
    
    // direction = "down", "left", "up" or "right";
    @ScriptCommand(name = "change_player_direction")
    public static void changeDirection(String direction) {
        Script.setGlobalValue("$$player_direction", direction);
        changeAnimation(direction);
        switch (direction) {
            case "down": walkDx = 0; walkDy = 1; break;
            case "left": walkDx = -1; walkDy = 0; break;
            case "up": walkDx = 0; walkDy = -1; break;
            case "right": walkDx = 1; walkDy = 0; break;
        }
    }

    public static String getName() {
        String playerName = Script.getGlobalValue("$$player_name").toString()
                + "    ";
        playerName = playerName.substring(0, 4);
        return playerName;
    }

    public static void setName(String name) {
        Script.setGlobalValue("$$player_name", name.trim());
    }

    public static String getState() {
        return Script.getGlobalValue("$$player_state").toString();
    }

    @ScriptCommand(name = "set_player_state")
    public static void setState(String state) {
        Script.setGlobalValue("$$player_state", state);
    }

    public static boolean isVisible() {
        return 1 == (Integer) Script.getGlobalValue("##player_visible");
    }

    public static void setVisible(boolean visible) {
        Script.setGlobalValue("##player_visible", visible ? 1 : 0);
    }
    
    public static Integer getX() {
        return (Integer) Script.getGlobalValue("##player_x");
    }

    public static Integer getY() {
        return (Integer) Script.getGlobalValue("##player_y");
    }

    public static void incX(int ix) {
        Script.setGlobalValue("##player_x", getX() + ix);
    }

    public static void incY(int iy) {
        Script.setGlobalValue("##player_y", getY() + iy);
    }

    public static int getLightRemainingSteps() {
        int remainingSteps = -1; // -1=not dark place -2=torch >0=radiant
        try {
            remainingSteps = (Integer) Script.getGlobalValue(
                                            "##player_light_remaining_steps");
        }
        catch (Exception e) { }
        return remainingSteps;
    }

    public static void decLightRemainingSteps() {
        int steps = getLightRemainingSteps();
        // -1 = it's not a dark place
        // -2 = using torch (unlimited steps)
        if (steps < 0) {
            return;
        }
        // radiant spell
        steps--;
        if (steps < 0) {
            steps = 0;
        }
        Script.setGlobalValue("##player_light_remaining_steps", steps);
    }
    
    @ScriptCommand(name = "player_use_torch_item")
    public static void useTorchItem() {
        // -2 = torch, unlimited steps
        Script.setGlobalValue("##player_light_remaining_steps", -2);
        updateViewLightRadius(false);
    }
    
    @ScriptCommand(name = "player_cast_radiant_spell")
    public static void castRadiantSpell() {
        Script.setGlobalValue("##player_light_remaining_steps", 239);
        updateViewLightRadius(false);
    }

    public static void setDarkPlace() {
        // 0 = dark place
        Script.setGlobalValue("##player_light_remaining_steps", 0); 
    }

    public static void setNotDarkPlace() {
        // -1 = it's not a dark place
        Script.setGlobalValue("##player_light_remaining_steps", -1); 
    }
    
    //  steps 0~79 80~159 160~239
    // radius    1      2       3
    public static int getLightRadius() {
        int lightRadius = 0;
        // -1 = lights off
        if (getLightRemainingSteps() == -1 || getLightRemainingSteps() == 0) {
            lightRadius = 1;
        }
        // -2 = torch item (unlimited steps)
        else if (getLightRemainingSteps() == -2) {
            lightRadius = 2;
        }
        // >0 = radiant spell
        else if (getLightRemainingSteps() > 0) {
            lightRadius = getLightRemainingSteps() / 80 + 2;
        }
        return lightRadius;
    }

    public static void updateViewLightRadius(boolean instant) {
        int currentRadius = View.getLightRadiusLayer0();
        int targetRadius = Game.getCurrentMap().isDark() 
                                ? Player.getLightRadius() * 16 : -1;
        if (currentRadius == targetRadius) {
            return;
        }
        if (instant) {
            View.setLightRadiusLayer0(targetRadius);
            return;
        }
        int d = targetRadius - currentRadius > 0 ? 1 : -1;
        while (currentRadius != targetRadius) {
            currentRadius += d;
            View.setLightRadiusLayer0(currentRadius);
            if (currentRadius % 8 == 0) {
                Audio.playSound(Audio.SOUND_TORCH_USED);
                View.refresh();
                Game.sleep(150);
            }
        }
    }
        
    public static int getMapCol() {
        return getX() / 16;
    }

    public static int getMapRow() {
        return getY() / 16;
    }
    
    public static void setLocation(int row, int col) {
        Script.setGlobalValue("##player_x", col * 16);
        Script.setGlobalValue("##player_y", row * 16);
        CURRENT_WALK_TILE.setLocation(col, row);
        TARGET_WALK_TILE.setLocation(-1, -1);
        updateCanEncounterEnemy();
    }
    
    public static Integer getOutsideCol() {
        return (Integer) Script.getGlobalValue("##player_outside_col");
    }

    public static Integer getOutsideRow() {
        return (Integer) Script.getGlobalValue("##player_outside_row");
    }

    public static void setOutsideLocation(
                    String outsideMap, String musicId, int col, int row) {
        
        Script.setGlobalValue("$$player_outside_map", outsideMap);
        Script.setGlobalValue("$$player_outside_music_id", musicId);
        Script.setGlobalValue("##player_outside_col", col);
        Script.setGlobalValue("##player_outside_row", row);
    }
    
    public static Integer getStr() {
        return (Integer) Script.getGlobalValue("##player_str");
    }

    public static Integer getAgi() {
        return (Integer) Script.getGlobalValue("##player_agi");
    }

    public static Integer getMaxHP() {
        return (Integer) Script.getGlobalValue("##player_max_hp");
    }

    public static Integer getMaxMP() {
        return (Integer) Script.getGlobalValue("##player_max_mp");
    }
    
    public static Integer getExtraAtk() {
        return (Integer) Script.getGlobalValue("##player_extra_atk");
    }

    public static void addExtraAtk(int atkAdd) {
        Script.setGlobalValue("##player_extra_atk", getExtraAtk() + atkAdd);
    }

    public static Integer getExtraDef() {
        return (Integer) Script.getGlobalValue("##player_extra_def");
    }

    public static void addExtraDef(int defAdd) {
        Script.setGlobalValue("##player_extra_def", getExtraDef() + defAdd);
    }
    
    public static Integer getLV() {
        return (Integer) Script.getGlobalValue("##player_lv");
    }

    public static Integer getHP() {
        return (Integer) Script.getGlobalValue("##player_hp");
    }

    @ScriptCommand(name = "hit_player")
    public static void hit(int damage) {
        int hp = getHP() - damage;
        if (hp < 0) {
            hp = 0;
        }
        Script.setGlobalValue("##player_hp", hp);
    }

    @ScriptCommand(name = "heal_player")
    public static void heal(int hpHealMin, int hpHealMax) {
        int hpHeal = Util.random(hpHealMin, hpHealMax);
        int hp = getHP() + hpHeal;
        if (hp > getMaxHP()) {
            hp = getMaxHP();
        }
        Script.setGlobalValue("##player_hp", hp);
    }
    
    public static Integer getMP() {
        return (Integer) Script.getGlobalValue("##player_mp");
    }

    // return: true - consumed ok
    //         false - there is no enough mp
    public static boolean consumeMP(int consumedMP) {
        int mp = getMP() - consumedMP;
        if (mp < 0) {
            return false;
        }
        Script.setGlobalValue("##player_mp", mp);
        return true;
    }
    
    public static Integer getG() {
        return (Integer) Script.getGlobalValue("##player_g");
    }
    
    public static void setG(int g) {
        Script.setGlobalValue("##player_g", g);
    }
    
    public static int incG(int ig) {
        int currentG = getG();
        int newG = currentG + ig;
        if (newG > 65535) {
            newG = 65535;
        }
        Script.setGlobalValue("##player_g", newG);
        return newG - currentG;
    }
    
    public static Integer getE() {
        return (Integer) Script.getGlobalValue("##player_e");
    }

    public static int incE(int ie) {
        int currentIE = getE();
        int newIE = currentIE + ie;
        if (newIE > 65535) {
            newIE = 65535;
        }
        Script.setGlobalValue("##player_e", newIE);
        updateXPForNextLv();
        return newIE - currentIE;
    }
    
    public static Item getWeapon() {
        Item weapon = null;
        if (Script.getGlobalValue("##player_weapon_id") != null) {
            int weaponId 
                    = (Integer) Script.getGlobalValue("##player_weapon_id");

            if (weaponId >= 0) {
                weapon = Resource.getItemById(weaponId);
            }
        }
        if (weapon == null) {
            weapon = Item.EMPTY;
        }
        return weapon;
    }

    public static Item getArmor() {
        Item armor = null;
        if (Script.getGlobalValue("##player_armor_id") != null) {
            int armorId = (Integer) Script.getGlobalValue("##player_armor_id");
            if (armorId >= 0) {
                armor = Resource.getItemById(armorId);
            }
        }
        if (armor == null) {
            armor = Item.EMPTY;
        }
        return armor;
    }

    public static Item getShield() {
        Item shield = null;
        if (Script.getGlobalValue("##player_shield_id") != null) {
            int shieldId 
                    = (Integer) Script.getGlobalValue("##player_shield_id");
            
            if (shieldId >= 0) {
                shield = Resource.getItemById(shieldId);
            }
        }
        if (shield == null) {
            shield = Item.EMPTY;
        }
        return shield;
    }
    
    public static void resetAllStatus() {
        setStatusASleep(false);
        setStatusStopSpell(false);
    }
    
    public static boolean isStatusASleep() {
        return ((Integer) Script.getGlobalValue("##player_status_asleep")) != 0;
    }

    public static void setStatusASleep(boolean asleep) {
        Script.setGlobalValue("##player_status_asleep", asleep ? 1 : 0);
        Script.setGlobalValue("##player_status_asleep_turn", 0);
    }

    public static int getStatusASleepTurn() {
        return (Integer) Script.getGlobalValue("##player_status_asleep_turn");
    }

    public static void incStatusASleepTurn() {
        Integer turn = getStatusASleepTurn();
        Script.setGlobalValue("##player_status_asleep_turn", turn + 1);
    }

    public static boolean isStatusStopSpell() {
        return (Integer) 
                Script.getGlobalValue("##player_status_stopspell") != 0;
    }

    public static void setStatusStopSpell(boolean stopSpell) {
        Script.setGlobalValue("##player_status_stopspell", stopSpell ? 1 : 0);
    }

    public static boolean isCursed() {
        Object cursedObj = Script.getGlobalValue("$$player_curses");
        return cursedObj != null && !cursedObj.toString().trim().isEmpty();
    }

    // remove also all related items from inventory
    @ScriptCommand(name = "remove_player_all_curses")
    public static void removeAllCurses() {
        Object cursesObj = Script.getGlobalValue("$$player_curses");
        String curses = "";
        if (cursesObj != null) {
            curses = cursesObj.toString();
        }
        String[] itemIds = curses.split("}");
        for (String itemIdStr : itemIds) {
            int itemId = Integer.parseInt(itemIdStr.replace("{", ""));
            Inventory.removeItem(itemId);
        }
        Script.setGlobalValue("$$player_curses", "");
    }

    @ScriptCommand(name = "add_player_curse")
    public static void addCurse(int curseId) {
        Object cursesObj = Script.getGlobalValue("$$player_curses");
        String curses = "";
        if (cursesObj != null) {
            curses = cursesObj.toString();
        }
        curses = curses.replace("{" + curseId + "}", "");
        curses = curses + "{" + curseId + "}";
        Script.setGlobalValue("$$player_curses", curses);
    }
    
    @ScriptCommand(name = "is_player_cursed")
    public static void isCursed(String resultGlobalVar) {
        Script.setGlobalValue(resultGlobalVar, isCursed() ? 1 : 0);
    }

    public static boolean hasSpecificCurse(int curseId) {
        Object cursesObj = Script.getGlobalValue("$$player_curses");
        String curses = "";
        if (cursesObj != null) {
            curses = cursesObj.toString();
        }
        boolean cursed = curses.contains("{" + curseId + "}");
        return cursed;
    }

    @ScriptCommand(name = "has_player_specific_curse")
    public static void hasSpecificCurse(String resultGlobalVar, int curseId) {
        Script.setGlobalValue(
                resultGlobalVar, hasSpecificCurse(curseId) ? 1 : 0);
    }
    
    public static Integer getDisabledEventCol() {
        return (Integer) Script.getGlobalValue("##player_disabled_event_col");
    }

    public static Integer getDisabledEventRow() {
        return (Integer) Script.getGlobalValue("##player_disabled_event_row");
    }
    
    public static void setDisabledEventLocation(int col, int row) {
        Script.setGlobalValue("##player_disabled_event_col", col);
        Script.setGlobalValue("##player_disabled_event_row", row);
    }

    public static Integer getRepelCounter() {
        return (Integer) Script.getGlobalValue("##player_repel_counter");
    }

    public static String getRepelSource() {
        return (String) Script.getGlobalValue("$$player_repel_source");
    }

    @ScriptCommand(name = "set_player_repel_counter")
    public static void setRepelCounter(int repelCounter, String repelSource) {
        Script.setGlobalValue("##player_repel_counter", repelCounter);
        Script.setGlobalValue("$$player_repel_source", repelSource);
    }

    public static void decRepelCounter() {
        int newRepelCounter = getRepelCounter() - 1;
        if (newRepelCounter < 0) {
            newRepelCounter = 0;
        }
        Script.setGlobalValue("##player_repel_counter", newRepelCounter);
    }
    
    public static int getAtk() {
        int modifiers = getWeapon().getAtk() + getArmor().getAtk() 
                + getShield().getAtk() + getExtraAtk();
        
        return getStr() + modifiers;
    }
    
    // The hero's defense is equal to his agility / 2 rounded down, 
    // plus the modifiers for his equipment.
    public static int getDef() {
        int modifiers = getWeapon().getDef() + getArmor().getDef() 
                + getShield().getDef() + getExtraDef();
        
        return getAgi() / 2 + modifiers;
    }

    public static boolean isMovementLocked() {
        return (Integer) 
                Script.getGlobalValue("##player_movement_locked") != 0;
    }

    public static void setMovementLocked(boolean locked) {
        Script.setGlobalValue("##player_movement_locked", locked ? 1 : 0);
    }

    public static String getTriggerOnKeyPressEventId() {
        return Script.getGlobalValue(
                "$$player_trigger_event_id_on_key_press").toString();
    }

    public static String getTriggerOnKeyPressEventLabel() {
        return Script.getGlobalValue(
                "$$player_trigger_event_label_on_key_press").toString();
    }

    @ScriptCommand(name = "register_player_trigger_event_on_key_press")
    public static void registerTriggerEventOnKeyPress(
            String eventId, String eventLabel) {
        
        Script.setGlobalValue(
                "$$player_trigger_event_id_on_key_press", eventId);
        
        Script.setGlobalValue(
                "$$player_trigger_event_label_on_key_press", eventLabel);
    }
    
    public static boolean isBlocked(int row, int col) {
        return (CURRENT_WALK_TILE.x == col && CURRENT_WALK_TILE.y == row)
                || (TARGET_WALK_TILE.x == col && TARGET_WALK_TILE.y == row);
    }

    public static long getStartTimeMS() {
        long startTimeMs = 0;
        try {
            startTimeMs = (Long) Script.getGlobalValue(
                                            "##player_start_time_ms");
        }
        catch (Exception e) { }
        return startTimeMs;
    }

    public static long getAccumulatedTimeMS() {
        long accumulatedTimeMs = 0;
        try {
            accumulatedTimeMs = (Long) Script.getGlobalValue(
                                            "##player_accumulated_time_ms");
        }
        catch (Exception e) { }
        return accumulatedTimeMs;
    }

    public static boolean isBattleEnabled() {
        return (Integer) Script.getGlobalValue("##battle_enabled") != 0;
    }
    
    public static boolean isJustTeleported() {
        return (Integer) Script.getGlobalValue("##player_just_teleported") != 0;
    }

    public static void setJustTeleported(boolean justTeleported) {
        Script.setGlobalValue(
                "##player_just_teleported", justTeleported ? 1 : 0);
    }

    public static boolean isJustDied() {
        return (Integer) Script.getGlobalValue("##player_just_died") != 0;
    }

    public static void setJustDied(boolean justDied) {
        Script.setGlobalValue(
                "##player_just_died", justDied ? 1 : 0);
    }
    
    private static void updateCanEncounterEnemy() {
        Tile tile = 
                Game.getCurrentMap().getTile(getMapRow(), getMapCol());

        // set if is there any possibility of encountering monsters
        boolean canEncounterEnemy = 
                tile.getEnemyProbabilityDenominator() > 0 
                && tile.getEnemyProbabilityNumerator() > 0
                && isBattleEnabled() 
                && Game.getCurrentMap()
                        .canPlayerEncounterEnemyAtCurrentLocation();

        Script.setGlobalValue("##player_can_encounter_enemy"
                                    , canEncounterEnemy ? 1 : 0);
    }
    
    public static void update() throws Exception {
        if (animation != null) {
            animation.update();
        }
        if (walking == 0) {
            walking--;
            CURRENT_WALK_TILE.setLocation(TARGET_WALK_TILE);
            TARGET_WALK_TILE.setLocation(-1, -1);
            
            // allows (not blocked + fire not required) events 
            // to be triggered again
            if (getMapRow() != getDisabledEventRow() 
                    || getMapCol() != getDisabledEventCol()) {
                
                setDisabledEventLocation(-1, -1);
            }
        }
        else if (walking < 0) {
            
            // if trigger event on key press is registered,
            // wait until the user press any action key
            if (getTriggerOnKeyPressEventId() != null 
                    && !getTriggerOnKeyPressEventId().trim().isEmpty()) {
                
                if (Input.isKeyJustPressed(KEY_LEFT)
                    || Input.isKeyJustPressed(KEY_RIGHT)
                    || Input.isKeyJustPressed(KEY_UP)
                    || Input.isKeyJustPressed(KEY_DOWN)
                    || Input.isKeyJustPressed(KEY_CONFIRM)
                    || Input.isKeyJustPressed(KEY_CANCEL)) {
                        
                        Game.triggerEvent(getTriggerOnKeyPressEventId()
                                , getTriggerOnKeyPressEventLabel());

                        registerTriggerEventOnKeyPress("", "");
                }
                return;
            }

            if (walking == -1) {
                walking--;

                // set if is there any possibility of encountering monsters
                updateCanEncounterEnemy();
                
                // avoid player from suffer terrain damage 
                // or encounters enemy just when teleported
                if (isJustTeleported()) {
                    setJustTeleported(false);
                    return;
                }
                
                // check armor that heals player
                accumulatedHealInMilli += getArmor().getHealPerStepInMilli();
                while (accumulatedHealInMilli >= 1000) {
                    accumulatedHealInMilli -= 1000;
                    heal(1, 1);
                }
                
                // check if current tile can cause damage like swamp
                Tile tile = 
                        Game.getCurrentMap().getTile(getMapRow(), getMapCol());

                if (tile.getDamagePerStep() > 0 
                                    && !Player.isProtectsTerrain()) {
                    
                    Audio.playSound(Audio.SOUND_SWAMP);
                    hit(tile.getDamagePerStep());
                    View.flash(1, 1, Color.RED, 50);
                }
                
                // player dead
                if (getHP() <= 0) {
                    Player.hideSimplifiedStatus();
                    View.fill(1, "0xff000080");
                    View.flash(4, 6, Color.RED, 40);
                    kill();
                    return;
                }
                
                // light radius in dark maps like dungeons
                decLightRemainingSteps();
                updateViewLightRadius(false);

                if (isBattleEnabled()) {
                    checkEnemyEncounter(Game.getCurrentMap());
                }
            }
            else if (walking < -30 && !showingStatus) {
                showSimplifiedStatus();
            }
            else {
                walking--;
            }
            if (!isMovementLocked()) {
                handleMovement(Game.getCurrentMap());
            }
        }
        else {
            walking--;
            incX(walkDx);
            incY(walkDy);
        }  
    }
    
    private static void walk(TileMap currentMap, int dx, int dy) {
        walkDx = dx;
        walkDy = dy;
        int targetX = getX() + dx * 16;
        int targetY = getY() + dy * 16;
        if (!currentMap.isBlocked(targetX, targetY)) {
            TARGET_WALK_TILE.setLocation(targetX / 16, targetY / 16);
            walking = 16;
            hideSimplifiedStatus();
        }
        else if ((-walking % 15) == 0) {
            System.out.println(walking);
            Audio.playSound(Audio.SOUND_BLOCKED);
        }
    }
    
    public static void handleMovement(TileMap currentMap) throws Exception {
        if (Input.isKeyPressed(KEY_LEFT)) {
            changeDirection("left");
            walk(currentMap, -1, 0);
        }
        else if (Input.isKeyPressed(KEY_RIGHT)) {
            changeDirection("right");
            walk(currentMap, 1, 0);
        }
        else if (Input.isKeyPressed(KEY_UP)) {
            changeDirection("up");
            walk(currentMap, 0, -1);
        }
        else if (Input.isKeyPressed(KEY_DOWN)) {
            changeDirection("down");
            walk(currentMap, 0, 1);
        }
        else if (Input.isKeyJustPressed(KEY_CANCEL)) {
            TileMap.showMainMenu();
        }
        
        Debug.update();
    }
    
    public static void draw(Graphics2D g) {
        if (animation != null) {
            int ax = 8 * 16 - 8;
            int ay = 7 * 16;
            animation.draw(g, ax, ay);
        }
    }
    
    public static boolean checkEventTrigger(
            Event event, boolean fireJustPressed) {
        
        boolean fireOk = !event.isFireRequired()
                || (event.isFireRequired() 
                    && fireJustPressed);

        if (!event.isVisible() || getX() % 16 != 0 || getY() % 16 != 0 
            || event.getX() % 16 != 0 || event.getY() % 16 != 0) {
            return false;
        }
        
        int playerCol = getX() / 16;
        int playerRow = getY() / 16;
        int eventCol = event.getX() / 16;
        int eventRow = event.getY() / 16;

        // prevent an event from occurring repeatedly. 
        // for the event to occur again, the player needs to step outside 
        // the event tile and step again. 
        if (!event.isBlocked() && !event.isFireRequired()
                && getDisabledEventCol() == eventCol 
                && getDisabledEventRow() == eventRow) {
            
            return false;
        }
        
        if (!event.isBlocked() && fireOk
                && playerCol == eventCol && playerRow == eventRow) {
            
            if (!event.isFireRequired()) {
                setDisabledEventLocation(eventCol, eventRow);
            }
            return true;
        }
        else if (event.isBlocked()) {
            int dx = 0;
            int dy = 0;
            
            if (event.isFireRequired()) {
                if (walkDx < 0) dx = -1;
                else if (walkDx > 0) dx = 1;
                else if (walkDy < 0) dy = -1;
                else if (walkDy > 0) dy = 1;
            }
            else {
                if (walkDx < 0 
                        && Input.isKeyJustPressed(KEY_LEFT)) dx = -1;
                else if (walkDx > 0 
                        && Input.isKeyJustPressed(KEY_RIGHT)) dx = 1;
                else if (walkDy < 0 
                        && Input.isKeyJustPressed(KEY_UP)) dy = -1;
                else if (walkDy > 0 
                        && Input.isKeyJustPressed(KEY_DOWN)) dy = 1;
            }
            
            if (fireOk && playerCol + dx == eventCol 
                    && playerRow + dy == eventRow) {
                
                return true;
            }
        }
        return false;
    }

    private static void checkEnemyEncounter(
                    TileMap currentMap) throws Exception {
        
        // check if enemy can be evaded with repel spell or fairy water item
        boolean repeling = false;
        // System.out.println("repel counter: " + getRepelCounter());
        if (getRepelCounter() > 0) {
            decRepelCounter();
            if (getRepelCounter() == 0) {
                Dialog.print(0, 1, Game.getText("@@repel_lost_effect"));
                Dialog.close();
            }
            else if (!currentMap.isRepelHasNoEffect()) {
                repeling = true;
            }
        }
                    
        Tile tile = currentMap.getTile(getY() / 16, getX() / 16);
        if (currentMap.isEnemiesEncounterEnabled() 
                                    && tile.wasMonsterEncountered()) {
            
            Enemy enemy = currentMap.getZoneEnemy();
            if (enemy == null) {
                return;
            }
            // repel or fairy water worked
            else if (repeling && getDef() / 2 > enemy.getStr() / 2) {
                System.out.println(
                        "enemy " + enemy.getName() + " was evaded.");
                return;
            }
            Battle.start(enemy, tile, false, false, BATTLE_MUSIC_ID);
        }
    }
    
    // invoke one of enemies of current map region
    @ScriptCommand(name = "invoke_local_enemy")
    public static void invokeEnemy() throws Exception {
        TileMap currentMap = Game.getCurrentMap();
        Tile tile = currentMap.getTile(getY() / 16, getX() / 16);
        Enemy enemy = currentMap.getZoneEnemy();
        if (enemy == null) {
            return;
        }
        Battle.start(enemy, tile, false, false, BATTLE_MUSIC_ID);
    }

    @ScriptCommand(name = "hide_player_simplified_status")
    public static void hideSimplifiedStatus() {
        Dialog.fillBox(2, -1, 1, 3, 8, 14);
        showingStatus = false;
        if (walking < -2) {
            walking = -2;
        }
    }
    
    @ScriptCommand(name = "show_player_simplified_status")
    public static void showSimplifiedStatus() {
        Dialog.drawBoxBorder(2, 1, 3, 8, 14);
        Dialog.fillBox(2, ' ', 2, 4, 7, 13);
        Dialog.printText(2, 2, 5, "LV");
        Dialog.printText(2, 2, 7, "HP");
        Dialog.printText(2, 2, 9, "MP");
        Dialog.printText(2, 2, 11, "G");
        Dialog.printText(2, 2, 13, "E");

        String playerName = 
                Script.getGlobalValue("$$player_name").toString() + "    ";
        
        playerName = playerName.substring(0, 4);
        Dialog.printText(2, 3, 3, playerName);
        
        Dialog.print(2, 2, 3, 15);
        
        Dialog.printText(2, 5, 5, Util.formatRight(
                Script.getGlobalValue("##player_lv").toString(), 3));
        
        Dialog.printText(2, 5, 7, Util.formatRight(
                Script.getGlobalValue("##player_hp").toString(), 3));
        
        Dialog.printText(2, 5, 9, Util.formatRight(
                Script.getGlobalValue("##player_mp").toString(), 3));
        
        Dialog.printText(2, 3, 11, Util.formatRight(
                Script.getGlobalValue("##player_g").toString(), 5));
        
        Dialog.printText(2, 3, 13, Util.formatRight(
                Script.getGlobalValue("##player_e").toString(), 5));
        
        View.refresh();
        
        showingStatus = true;
    }

    public static void updateXPForNextLv() {
        int currentLevel = getLV();
        int currentExp = getE();
        int xpForNextLv = -1;
        if (currentLevel + 1 <= PlayerLevel.lastLevel) {
            PlayerLevel nextLevel = Resource.getPlayerLevel(currentLevel + 1);
            xpForNextLv = nextLevel.getXP() - currentExp;
        }
        Script.setGlobalValue("##player_xp_for_next_lv", xpForNextLv);
        
        // TODO when ask king for remaining exp for next level,
        //      if you are already in the last level, show different message
        //      saying you can't get stronger than that (?)
    }
    
    public static boolean isLevelUp() {
        int currentLevel = getLV();
        int currentExp = getE();
        PlayerLevel nextLevel = Resource.getPlayerLevel(currentLevel + 1);
        return (currentLevel < 30 && currentExp >= nextLevel.getXP());
    }
    
    public static void levelUp() {
        Audio.playSound(Audio.SOUND_LEVEL_UP);
        Game.sleep(2500);
        int nextLevel = getLV() + 1;
        PlayerLevel playerLevel = Resource.getPlayerLevel(nextLevel);
        Script.setGlobalValue("##player_lv", nextLevel);
        showSimplifiedStatus();
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, Game.getText("@@battle_player_level_up_1"));
        Dialog.print(0, 2, Game.getText("@@battle_player_level_up_2"));
        
        // TODO values are bit different depending on the name of player

        int incrStr = playerLevel.getStr() - getStr();
        int incrAgi = playerLevel.getAgi() - getAgi();
        int incrHP = playerLevel.getHP() - getMaxHP();
        int incrMP = playerLevel.getMP() - getMaxMP();
        boolean learnedNewSpell = learnedNewSpell();
        int totalMessages = (incrStr > 0 ? 1 : 0) + (incrAgi > 0 ? 1 : 0) 
                + (incrHP > 0 ? 1 : 0) + (incrMP > 0 ? 1 : 0)
                + (learnedNewSpell ? 1 : 0);
        
        Script.setGlobalValue("##battle_level_up_increased_str", incrStr);
        Script.setGlobalValue("##battle_level_up_increased_agi", incrAgi);
        Script.setGlobalValue("##battle_level_up_increased_hp", incrHP);
        Script.setGlobalValue("##battle_level_up_increased_mp", incrMP);

        if (incrStr > 0) {
            Script.setGlobalValue("##player_str", playerLevel.getStr());
            Dialog.print(0, 0, "");
            Dialog.print(0, totalMessages-- > 1 ? 2 : 1
                    , Game.getText("@@battle_player_level_up_str"));
        }
        if (incrAgi > 0) {
            Script.setGlobalValue("##player_agi", playerLevel.getAgi());
            Dialog.print(0, 0, "");
            Dialog.print(0, totalMessages-- > 1 ? 2 : 1
                    , Game.getText("@@battle_player_level_up_agi"));
        }
        if (incrHP > 0) {
            Script.setGlobalValue("##player_max_hp", playerLevel.getHP());
            Dialog.print(0, 0, "");
            Dialog.print(0, totalMessages-- > 1 ? 2 : 1
                    , Game.getText("@@battle_player_level_up_hp"));
        }
        if (incrMP > 0) {
            Script.setGlobalValue("##player_max_mp", playerLevel.getMP());
            Dialog.print(0, 0, "");
            Dialog.print(0, totalMessages-- > 1 ? 2 : 1
                    , Game.getText("@@battle_player_level_up_mp"));
        }
        // new spell available
        if (learnedNewSpell) {
            Dialog.print(0, 0, "");
            Dialog.print(0, 1
                    , Game.getText("@@battle_player_learned_new_spell"));
        }
        updateXPForNextLv();
    }

    private static boolean learnedNewSpell() {
        Spell newSpell = Magic.getSpellByLevel(getLV());
        return newSpell != null;
    }
    
    public static void kill() throws Exception {
        Audio.stopMusic();
        Game.sleep(500);
        Audio.playSound(Audio.SOUND_PLAYER_DEAD);
        Game.sleep(5000);
        Dialog.print(0, 0, "");
        int originalSpeed = Dialog.getSpeed();
        Dialog.setSpeed(250);
        Dialog.print(0, 1, Game.getText("@@battle_player_dead"));
        Dialog.setSpeed(originalSpeed);
        Game.waitForAnyKey();
        Dialog.close();
        // When you die, you'll come back at Tangetel Castle, 
        // in front of the King. You will still have all of your experience, 
        // but you will lose half of your gold on hand. 
        // This will only really bother you in the beginning of the game.
        // Later, gold will be so easy to come by that the only bad side to 
        // dying in combat will be having to walk back to where you were.
        Player.setJustDied(true);
        Player.setG(Player.getG() / 2);
        Script.setGlobalValue("##player_hp"
                , Script.getGlobalValue("##player_max_hp"));
        
        Script.setGlobalValue("##player_mp"
                , Script.getGlobalValue("##player_max_mp"));        
        
        Game.teleport("tantegel_castle", 65, 16, "up", 1
                , "tantegel", 0, 1, 1, 1);
    }
    
    public static void showCompleteStatus() {
        Dialog.drawBoxBorder(3, 9, 3, 28, 26);
        Dialog.fillBox(3, ' ', 10, 4, 27, 25);
        
        String playerName = Player.getName();
        Dialog.printText(3, 11, 4, Util.formatRight(
                Game.getText("@@player_status_complete_name") 
                        + ":", 9) + playerName);
        
        String str = Util.formatRight(getStr().toString(), 3);
        Dialog.printText(3, 11, 8, Util.formatRight(
                Game.getText("@@player_status_complete_strength") 
                        + ":", 14) + str);
        
        String agi = Util.formatRight(getAgi().toString(), 3);
        Dialog.printText(3, 11, 10, Util.formatRight(
                Game.getText("@@player_status_complete_agility") 
                        + ":", 14) + agi);
        
        String hpMax = Util.formatRight(getMaxHP().toString(), 3);
        Dialog.printText(3, 11, 12, Util.formatRight(
                Game.getText("@@player_status_complete_max_hp") 
                        + ":", 14) + hpMax);
        
        String mpMax = Util.formatRight(getMaxMP().toString(), 3);
        Dialog.printText(3, 11, 14, Util.formatRight(
                Game.getText("@@player_status_complete_max_mp") 
                        + ":", 14) + mpMax);
        
        String atk = Util.formatRight(getAtk() + "", 3);
        Dialog.printText(3, 11, 16, Util.formatRight(
                Game.getText("@@player_status_complete_atk") + ":", 14) + atk);
        
        String def = Util.formatRight(getDef() + "", 3);
        Dialog.printText(3, 11, 18, Util.formatRight(
                Game.getText("@@player_status_complete_def") + ":", 14) + def);
        
        Dialog.printText(3, 11, 20, Util.formatRight(
                Game.getText("@@player_status_complete_weapon") + ":", 8));
        
        Dialog.printText(3, 11, 22, Util.formatRight(
                Game.getText("@@player_status_complete_armor") + ":", 8));
        
        Dialog.printText(3, 11, 24, Util.formatRight(
                Game.getText("@@player_status_complete_shield") + ":", 8));
        
        showItemName(Player.getWeapon(), 19, 20, 9);
        showItemName(Player.getArmor(), 19, 22, 9);
        showItemName(Player.getShield(), 19, 24, 9);
        
        while (!Input.isKeyJustPressed(KEY_CONFIRM)
                && !Input.isKeyJustPressed(KEY_CANCEL)) {
            
            String playingTime = Util.convertMsToHHMMSS(
                    Player.getAccumulatedTimeMS() + System.currentTimeMillis() 
                            - Player.getStartTimeMS());
            
            Dialog.printText(3, 11, 6, Util.formatRight(
                    Game.getText("@@player_status_complete_time") + ":", 9)
                        + playingTime);
            
            View.refresh();
            Game.sleep(1000 / 60);
        }
        Dialog.fillBox(3, -1, 9, 3, 28, 26);
    }

    private static void showItemName(
            Item item, int col, int row, int maxCols) {
        
        if (item == null) {
            return;
        }
        String[] names = item.getName().trim().split("\\s+");
        String[] nameLine = new String[] { "", "" };
        int nameLineIndex = 0;
        int nameLineLength = 0;
        for (int i = 0; i < names.length; i++) {
            if (nameLineLength + names[i].length() > maxCols) {
                nameLineIndex++;
                nameLineLength = 0;
            }
            nameLineLength += names[i].length();
            nameLine[nameLineIndex] += names[i] 
                    + (nameLineLength < maxCols ? " " : "");
            nameLineLength += nameLineLength < maxCols ? 1 : 0;
        }
        Dialog.printText(3, col, row, nameLine[0]);
        Dialog.printText(3, col + 1, row + 1, nameLine[1]);        
    }
    
    public static void updateStartPlayingTime() {
        long currentTimeMs = System.currentTimeMillis();
        Script.setGlobalValue("##player_start_time_ms", currentTimeMs);
    }
    
    public static void updatePlayingTime() {
        long accumulatedTimeMs = 0;
        try {
            accumulatedTimeMs = (Long) Script.getGlobalValue(
                                            "##player_accumulated_time_ms");
        }
        catch (Exception e) { }
        long startTimeMs = System.currentTimeMillis();
        try {
            startTimeMs 
                    = (Long) Script.getGlobalValue("##player_start_time_ms");
        }
        catch (Exception e) { }
        long currentTimeMs = System.currentTimeMillis();
        long deltaTimeMs = currentTimeMs - startTimeMs;
        accumulatedTimeMs += deltaTimeMs;
        Script.setGlobalValue("##player_start_time_ms", currentTimeMs);
        Script.setGlobalValue(
                "##player_accumulated_time_ms", accumulatedTimeMs);
    }
    
    @ScriptCommand(name = "equip_player")
    public static void equipPlayer(int itemId) {
        Item equip = Resource.getItemById(itemId);
        switch (equip.getType()) {
            case WEAPON:
                Script.setGlobalValue("##player_weapon_id", itemId);
                break;
            case ARMOR: 
                Script.setGlobalValue("##player_armor_id", itemId);
                break;
            case SHIELD: 
                Script.setGlobalValue("##player_shield_id", itemId);
                break;
        }        
    }

    public static boolean isImmuneToStopSpell() {
        return getWeapon().isImmuneToStopSpell() 
                || getArmor().isImmuneToStopSpell()
                || getShield().isImmuneToStopSpell();
    }

    public static boolean isRelievesHurt() {
        return getWeapon().isRelievesHurt()
                || getArmor().isRelievesHurt()
                || getShield().isRelievesHurt();
    }

    public static boolean isRelievesBreath() {
        return getWeapon().isRelievesBreath()
                || getArmor().isRelievesBreath()
                || getShield().isRelievesBreath();
    }

    // protects against terrain tiles that can damage player each walk step
    public static boolean isProtectsTerrain() {
        return getWeapon().isProtectsTerrain()
                || getArmor().isProtectsTerrain()
                || getShield().isProtectsTerrain();
    }

}
