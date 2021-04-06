package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory class.
 * 
 * Note 1: there is a limit on the number of items you can take at a time.
 *         If the inventory is full and you take an item, it will ask you
 *         to discard an item. 
 *         Check: https://www.youtube.com/watch?v=rghmVPMY24I&t=376
 * 
 * Note 2: Important items can't be discarted.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Inventory {
    
    public static final int MAX_SLOTS_SIZE = 9;
    
    public static void start() {
        // initialize all inventory global variables
        for (int i = 0; i < 256; i++)  {
            Script.setGlobalValue("##player_item_" + i, 0);
        }
        
//        Script.setGlobalValue("##player_item_41", 3);
//        Script.setGlobalValue("##player_item_43", 4);
//        Script.setGlobalValue("##player_item_46", 1);
//        Script.setGlobalValue("##player_item_47", 1);
//        Script.setGlobalValue("##player_item_48", 1);
//        Script.setGlobalValue("##player_item_50", 1);
//        Script.setGlobalValue("##player_item_51", 1);
//        Script.setGlobalValue("##player_item_52", 1);
//        Script.setGlobalValue("##player_item_53", 1);
//        Script.setGlobalValue("##player_item_60", 1);
//        Script.setGlobalValue("##player_item_70", 1);
    }
    
    private static class Slot {
        Item item;
        int itemCount;

        public Slot(Item item, int itemCount) {
            this.item = item;
            this.itemCount = itemCount;
        }
    }

    private static final List<Slot> ITEMS_SLOTS = new ArrayList<>();
    
    // return null -> canceled
    public static Item showSelectItemDialog() {
        Item returnItem = null;
        Dialog.drawBoxBorder(3, 18, 5, 29, 6);
        int row = 6;
        ITEMS_SLOTS.clear();
        for (Item itemTmp : Resource.getITEMS().values()) {
            Integer itemCount = (Integer) Script.getGlobalValue(
                                        "##player_item_" + itemTmp.getId());
            
            if (itemTmp.getType() == Item.Type.ITEM 
                    && itemCount != null && itemCount > 0) {
                
                Slot slot;
                while (itemCount > 0) {
                    if (itemCount > itemTmp.getItemsPerSlot()) {
                        slot = new Slot(itemTmp, itemTmp.getItemsPerSlot());
                        itemCount -= itemTmp.getItemsPerSlot();
                    }
                    else {
                        slot = new Slot(itemTmp, itemCount);
                        itemCount = 0;
                    }
                    ITEMS_SLOTS.add(slot);
                    
                    Dialog.drawBoxBorder(3, 18, row + 1, 29, row + 2);

                    Dialog.printText(3, 18, row
                            , (char) Dialog.getBOX_BORDER().left + "          " 
                            + (char) Dialog.getBOX_BORDER().right);

                    Dialog.printText(3, 18, row + 1
                            , (char) Dialog.getBOX_BORDER().left + "          " 
                            + (char) Dialog.getBOX_BORDER().right);

                    int maxCols = itemTmp.getItemsPerSlot() > 1 ? 8 : 9;
                    String[] names = itemTmp.getName().trim().split("\\s+");
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
                                + (nameLineLength < maxCols - i ? " " : "");

                        nameLineLength += nameLineLength < maxCols - i ? 1 : 0;
                    }
                    Dialog.printText(3, 20, row, nameLine[0]);
                    Dialog.printText(3, 21, row + 1, nameLine[1]);

                    if (itemTmp.getItemsPerSlot() > 1) {
                        String itemCountStr = "" + slot.itemCount;
                        Dialog.printText(3, 29 - itemCountStr.length()
                                                        , row, itemCountStr);
                    }
                    row += 2;
                }
            }
        }
        
        if (ITEMS_SLOTS.isEmpty()) {
            View.getOffscreenGraphics2D(3).clearRect(
                18 * 8, 5 * 8, 12 * 8, (2 * ITEMS_SLOTS.size() + 2) * 8);
            
            Battle.hideMainMenu();
            Dialog.print(0, 0, Game.getText("@@player_items_empty"));
            Game.sleep(500);
        }
        else {
                
            int selectedItem = 0;

            // for appropriate cursor start blink time
            long blinkTime = System.nanoTime();

            while (true) {
                for (int r = 0; r < ITEMS_SLOTS.size(); r++) {
                    Dialog.print(3, 19, 6 + 2 * r, ' ');
                }

                // blink cursor
                if ((int) ((System.nanoTime() - blinkTime) 
                                            * 0.0000000035) % 2 == 0) {

                    Dialog.print(3, 19, 6 + 2 * selectedItem, 2);
                }

                if (Input.isKeyJustPressed(KEY_UP) 
                                                && selectedItem > 0) {
                    
                    selectedItem--;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_DOWN) 
                                    && selectedItem < ITEMS_SLOTS.size() - 1) {

                    selectedItem++;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                    Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                    returnItem = ITEMS_SLOTS.get(selectedItem).item;
                    break;
                }
                else if (Input.isKeyJustPressed(KEY_CANCEL)) {
                    returnItem = null;
                    break;
                }
                View.refresh();
                Game.sleep(1000 / 60);
            }
        }
        View.getOffscreenGraphics2D(3).clearRect(
                18 * 8, 5 * 8, 12 * 8, (2 * ITEMS_SLOTS.size() + 2) * 8);
        
        return returnItem;
    }
    // return  1 = true
    //        -1 = false -> there is no more empty slot available
    //        -2 = false -> you already have max allowed number of items
    public static int checkCanAddItem(int itemId) {
        Item item = Resource.getItemById(itemId);
        Integer itemCount = (Integer) Script.getGlobalValue(
                                                "##player_item_" + itemId);
        int emptySlotSize = getEmptySlotsSize();
        if (itemCount == null || itemCount == 0) {
            return emptySlotSize > 0 ? 1 : -1;
        }
        else {
            int lastSlotSpace = itemCount % item.getItemsPerSlot();
            if (itemCount + 1 > item.getMaxCount()) {
                return -2;
            }
            else if (lastSlotSpace == 0 && emptySlotSize == 0) {
                return -1;
            }
            return 1;
        }
    }
    
    public static int getEmptySlotsSize() {
        int usedSlotsSize = 0;
        for (Item itemTmp : Resource.getITEMS().values()) {
            
            // magic key item has 1 slot reserved
            if (itemTmp.getId() == 46) {
                continue;
            }
            
            Integer itemCount = (Integer) Script.getGlobalValue(
                                        "##player_item_" + itemTmp.getId());
            
            if (itemTmp.getType() == Item.Type.ITEM 
                    && itemCount != null && itemCount > 0) {
                
                while (itemCount > 0) {
                    if (itemCount > itemTmp.getItemsPerSlot()) {
                        itemCount -= itemTmp.getItemsPerSlot();
                    }
                    else {
                        itemCount = 0;
                    }
                    usedSlotsSize++;
                }
            }
        }
        int emptySlotsSize = MAX_SLOTS_SIZE - usedSlotsSize;
        emptySlotsSize = emptySlotsSize < 0 ? 0 : emptySlotsSize;
        return emptySlotsSize;
    }    
    
    @ScriptCommand(name = "add_inventory_item")
    public static void addItem(int itemId) {
        Object itemCountObj = Script.getGlobalValue("##player_item_" + itemId);
        if (itemCountObj != null) {
            int itemCount = (Integer) itemCountObj;
            itemCount++;
            Script.setGlobalValue("##player_item_" + itemId, itemCount);
        }
        else {
            Script.setGlobalValue("##player_item_" + itemId, 1);
        }
    }
    
    @ScriptCommand(name = "remove_inventory_item")
    public static void removeItem(int itemId) {
        Object itemCountObj = Script.getGlobalValue("##player_item_" + itemId);
        if (itemCountObj != null) {
            int itemCount = (Integer) itemCountObj;
            if (itemCount > 0) {
                itemCount--;
            }
            Script.setGlobalValue("##player_item_" + itemId, itemCount);
        }
    }
        
    @ScriptCommand(name = "consume_item")
    public static boolean consumeItem(int itemId) {
        int currentCount 
                = (Integer) Script.getGlobalValue("##player_item_" + itemId);
        if (currentCount > 0) {
            Script.setGlobalValue("##player_item_" + itemId, currentCount - 1);
            return false;
        }
        return false;
    }
    
    @ScriptCommand(name = "drop_item")
    public static boolean dropItem(int itemId) {
        Item item = Resource.getItemById(itemId);
        int currentCount 
                = (Integer) Script.getGlobalValue("##player_item_" + itemId);
        currentCount -= item.getItemsPerSlot();
        if (currentCount < 0) {
            currentCount = 0;
        }
        Script.setGlobalValue("##player_item_" + itemId, currentCount);
        return false;
    }
    
    @ScriptCommand(name = "remove_first_disposable_item")
    public static void removeFirstDisposableItem(
            String itemIdGlobalVar, String itemNameGlobalVar) {
        
        for (Item itemTmp : Resource.getITEMS().values()) {
            Integer itemCount = (Integer) Script.getGlobalValue(
                                        "##player_item_" + itemTmp.getId());
            
            if (itemTmp.getType() == Item.Type.ITEM 
                    && itemCount != null && itemCount > 0
                    && itemTmp.isDisposable()) {
                
                dropItem(itemTmp.getId());
                Script.setGlobalValue(itemIdGlobalVar, itemTmp.getId());
                Script.setGlobalValue(itemNameGlobalVar, itemTmp.getName());
                return;
            }
        }
    }

    // has_player_parent_items ##item_id ##result -> 1=true 0=false
    @ScriptCommand(name = "has_player_parent_items")
    public static void hasPlayerParentItems(int itemId, String resGlobalVar) {
        Item itemTmp = Resource.getItemById(itemId);
        if (itemTmp == null) {
            Script.setGlobalValue(resGlobalVar, 0);
        }
        else {
            Script.setGlobalValue(
                    resGlobalVar, itemTmp.hasPlayerParentItem() ? 1 : 0);
        }
    }
    
}
