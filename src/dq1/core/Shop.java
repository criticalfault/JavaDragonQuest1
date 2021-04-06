package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shop class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Shop {
    
    private static final List<Item> ITEMS = new ArrayList<>();
    
    @ScriptCommand(name = "clear_shop")
    public static void clearShop() {
        ITEMS.clear();
    }
    
    @ScriptCommand(name = "add_shop_item")
    public static void addShopItem(int itemId, int buyPrice, int sellPrice) {
        Item item = Resource.getItemById(itemId);
        item.setBuy(buyPrice >= 0 ? buyPrice : item.getBuyOriginal());
        item.setSell(sellPrice >= 0 ? sellPrice : item.getSellOriginal());
        ITEMS.add(item);
    }

    public static Item showShopBuyItem() {
        Dialog.drawBoxBorder(3, 9, 3, 27, 4);
        
        Item returnItem = null;
        
        int row = 4;
        for (Item itemTmp : ITEMS) {
            Dialog.drawBoxBorder(3, 9, row + 1, 27, row + 2);

            Dialog.printText(3, 9, row
                    , (char) Dialog.getBOX_BORDER().left + "                 " 
                    + (char) Dialog.getBOX_BORDER().right);

            Dialog.printText(3, 9, row + 1
                    , (char) Dialog.getBOX_BORDER().left + "                 " 
                    + (char) Dialog.getBOX_BORDER().right);

            int maxCols = itemTmp.getMaxCount() > 1 ? 8 : 9;
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
                        + (nameLineLength < maxCols ? " " : "");

                nameLineLength += nameLineLength < maxCols ? 1 : 0;
            }
            Dialog.printText(3, 11, row, nameLine[0]);
            Dialog.printText(3, 12, row + 1, nameLine[1]);

            String itemCountStr = Util.formatRight("" + itemTmp.getBuy(), 5);
            Dialog.printText(3, 27 - itemCountStr.length()
                                            , row, itemCountStr);
            row += 2;
        }
        
        if (ITEMS.isEmpty()) {
            View.getOffscreenGraphics2D(3).clearRect(
                18 * 8, 5 * 8, 12 * 8, (2 * ITEMS.size() + 2) * 8);
            
            Dialog.print(0, 0, Game.getText("$$player_items_empty"));
            Game.sleep(500);
        }
        else {
                
            int selectedItem = 0;

            // for appropriate cursor start blink time
            long blinkTime = System.nanoTime();

            while (true) {
                for (int r = 0; r < ITEMS.size(); r++) {
                    Dialog.print(3, 10, 4 + 2 * r, ' ');
                }

                // blink cursor
                if ((int) ((System.nanoTime() - blinkTime) 
                                            * 0.0000000035) % 2 == 0) {

                    Dialog.print(3, 10, 4 + 2 * selectedItem, 2);
                }

                if (Input.isKeyJustPressed(KEY_UP) 
                                                && selectedItem > 0) {
                    
                    selectedItem--;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_DOWN) 
                                    && selectedItem < ITEMS.size() - 1) {

                    selectedItem++;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                    Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                    returnItem = ITEMS.get(selectedItem);
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
                9 * 8, 3 * 8, 27 * 8, (2 * ITEMS.size() + 4) * 8);
        
        View.refresh();
        return returnItem;
    }
    
}
