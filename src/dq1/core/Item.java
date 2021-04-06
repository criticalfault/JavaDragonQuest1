package dq1.core;

import static dq1.core.Item.Type.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Item class.
 * events: 
 *         -> on_use_when_map    -> ret 0->return to menu | 1->exit main menu
 *               consume_item item_id (needs to consume manually)
 
 *         -> on_use_when_battle -> ret 0->keep turn | 1->pass turn to enemy
 *               consume_item item_id (needs to consume manually)
 * 
 * Notes: * using a item will not consume it automatically.
 * 
 *        * blocking dialog messages or wait_for_fire_key must be called
 *          manually (if this is the desired behaviour).
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Item {
    
    public static final Item EMPTY = new Item(0, ITEM, "", 0, 0, 0, 0, 0
            , false, false, false, 0, false, false, false, false, 0);
    
    public static enum Type { ITEM, WEAPON, ARMOR, SHIELD };
    
    private final int id;
    private final Type type;
    private final String name;
    private int buy;
    private int sell;
    private final int buyOriginal;
    private final int sellOriginal;
    private final int atk;
    private final int def;
    private final int maxCount;
    private final boolean useInBattle;
    private final boolean useInMap;
    private final boolean disposable;
    private final int itemsPerSlot;
    private final boolean immuneToStopSpell;
    private final boolean relievesHurt;
    private final boolean relievesBreath;
    // protects against terrain tiles like swamp and barrier tiles
    private final boolean protectsTerrain; 
    private final int healPerStepInMilli;
    private List<Integer> parentItems = new ArrayList<>();
    private Script script;
            
    public Item(int id, Type type, String name
            , int buy, int sell, int atk, int def
            , int maxCount, boolean useInBattle, boolean useInMap
            , boolean disposable, int itemsPerSlot, boolean immuneToStopSpell
            , boolean relievesHurt, boolean relievesBreath
            , boolean protectsTerrain, int healPerStepInMilli) {
        
        this.id = id;
        this.type = type;
        this.name = name;
        this.buy = buy;
        this.sell = sell;
        this.buyOriginal = buy;
        this.sellOriginal = sell;
        this.atk = atk;
        this.def = def;
        this.maxCount = maxCount;
        this.useInBattle = useInBattle;
        this.useInMap = useInMap;
        this.disposable = disposable;
        this.itemsPerSlot = itemsPerSlot;
        this.immuneToStopSpell = immuneToStopSpell;
        this.relievesHurt = relievesHurt;
        this.relievesBreath = relievesBreath;
        this.protectsTerrain = protectsTerrain;
        this.healPerStepInMilli = healPerStepInMilli;
    }
    
    public Item(String serializedData) {
        String[] args = serializedData.trim().split(",");
        String[] h = args[0].trim().split("\\s+");
        id = Integer.parseInt(h[1]);
        type = Type.valueOf(h[0].toUpperCase());
        name = args[1].trim();
        buy = Integer.parseInt(args[2].trim());
        sell = Integer.parseInt(args[3].trim());
        buyOriginal = buy;
        sellOriginal = sell;
        atk = Integer.parseInt(args[4].trim());
        def = Integer.parseInt(args[5].trim());
        maxCount = Integer.parseInt(args[6].trim());
        useInBattle = Boolean.parseBoolean(args[7].trim());
        useInMap = Boolean.parseBoolean(args[8].trim());
        disposable = Boolean.parseBoolean(args[9].trim());
        itemsPerSlot = Integer.parseInt(args[10].trim());
        immuneToStopSpell = Boolean.parseBoolean(args[11].trim());
        relievesHurt = Boolean.parseBoolean(args[12].trim());
        relievesBreath = Boolean.parseBoolean(args[13].trim());
        protectsTerrain = Boolean.parseBoolean(args[14].trim());
        healPerStepInMilli = Integer.parseInt(args[15].trim());
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getBuy() {
        return buy;
    }

    public void setBuy(int buy) {
        this.buy = buy;
    }

    public int getSell() {
        return sell;
    }

    public void setSell(int sell) {
        this.sell = sell;
    }

    public int getBuyOriginal() {
        return buyOriginal;
    }

    public int getSellOriginal() {
        return sellOriginal;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public boolean isUseInBattle() {
        return useInBattle;
    }

    public boolean isDisposable() {
        return disposable;
    }

    public boolean isUseInMap() {
        return useInMap;
    }

    public int getItemsPerSlot() {
        return itemsPerSlot;
    }

    public boolean isImmuneToStopSpell() {
        return immuneToStopSpell;
    }

    public boolean isRelievesHurt() {
        return relievesHurt;
    }

    public boolean isRelievesBreath() {
        return relievesBreath;
    }

    public boolean isProtectsTerrain() {
        return protectsTerrain;
    }

    public int getHealPerStepInMilli() {
        return healPerStepInMilli;
    }
    
    public void addParentItem(int itemId) {
        if (!containsParentItem(itemId)) {
            parentItems.add(itemId);
        }
    }
    
    public boolean containsParentItem(int itemId) {
        return parentItems.contains(itemId);
    }
    
    public boolean hasPlayerParentItem() {
        for (Integer itemId : parentItems) {
            Item itemTmp = Resource.getItemById(itemId);
            switch (itemTmp.getType()) {
                case WEAPON:
                    if (Player.getWeapon().getId() == itemId) {
                        return true;
                    }
                    break;
                case ARMOR: 
                    if (Player.getArmor().getId() == itemId) {
                        return true;
                    }
                    break;
                case SHIELD: 
                    if (Player.getShield().getId() == itemId) {
                        return true;
                    }
                    break;
                case ITEM:
                    Integer itemCount = (Integer) 
                            Script.getGlobalValue("##player_item_" + itemId);
                    if (itemCount != null && itemCount > 0) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
    
    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
    
    public boolean isEquip() {
        return type != ITEM;
    }
    
    // return boolean -> used successfully
    public boolean use(String label) throws Exception {
        if (script != null) {
            if (script.execute(label)) {
                return true; 
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Item{" + "id=" + id + ", type=" + type + ", name=" + name 
            + ", buy=" + buy + ", sell=" + sell 
            + ", buyOriginal=" + buyOriginal 
            + ", sellOriginal=" + sellOriginal + ", atk=" + atk 
            + ", def=" + def + ", maxCount=" + maxCount 
            + ", useInBattle=" + useInBattle + ", useInMap=" + useInMap 
            + ", disposable=" + disposable + ", itemsPerSlot=" + itemsPerSlot 
            + ", immuneToStopSpell=" + immuneToStopSpell 
            + ", relievesHurt=" + relievesHurt 
            + ", relievesBreath=" + relievesBreath
            + ", protectsTerrain=" + protectsTerrain
            + ", healPerStepInMilli=" + healPerStepInMilli + '}';
    }
    
}
