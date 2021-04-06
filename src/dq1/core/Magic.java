package dq1.core;

import static dq1.core.Settings.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Magic class.
 * 
 * Set of spells available for player.
 * This is the equivalent "Inventory" for spells.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Magic {
    
    private static final List<Integer> SPELLS_ID = new ArrayList<>();
    
    // return null -> canceled
    public static Spell showSelectSpellDialog() {
        Spell returnSpell = null;
        Dialog.drawBoxBorder(3, 18, 3, 29, 4);
        Dialog.printText(3, 22, 3, Game.getText("@@map_menu_spell"));
        
        Dialog.print(3, 21, 3, 15);

        Dialog.printText(3, 18, 4
                , (char) Dialog.getBOX_BORDER().left + "          " 
                + (char) Dialog.getBOX_BORDER().right);
        
        int row = 5;
        SPELLS_ID.clear();
        for (Integer spellId : Resource.getSPELLS().keySet()) {
            Spell spellTmp = Resource.getSpellById(spellId);
            
            if (Player.getLV() < spellTmp.getLevel()) {
                continue;
            }
            
            SPELLS_ID.add(spellId);

            Dialog.drawBoxBorder(3, 18, row + 1, 29, row + 2);

            Dialog.printText(3, 18, row
                    , (char) Dialog.getBOX_BORDER().left + "          " 
                    + (char) Dialog.getBOX_BORDER().right);

            Dialog.printText(3, 18, row + 1
                    , (char) Dialog.getBOX_BORDER().left + "          " 
                    + (char) Dialog.getBOX_BORDER().right);

            String[] names = spellTmp.getName().trim().split("\\s+");
            String[] nameLine = new String[] { "", "" };
            int nameLineIndex = 0;
            int nameLineLength = 0;
            for (int i = 0; i < names.length; i++) {
                if (nameLineLength + names[i].length() > 9) {
                    nameLineIndex++;
                    nameLineLength = 0;
                }
                nameLineLength += names[i].length();
                nameLine[nameLineIndex] 
                        += names[i] + (nameLineLength < 9 ? " " : "");
                
                nameLineLength += nameLineLength < 9 ? 1 : 0;
            }
            Dialog.printText(3, 20, row, nameLine[0]);
            Dialog.printText(3, 21, row + 1, nameLine[1]);
            row += 2;
        }
        
        if (SPELLS_ID.isEmpty()) {
            View.getOffscreenGraphics2D(3).clearRect(
                    18 * 8, 3 * 8, 12 * 8, (2 * SPELLS_ID.size() + 3) * 8);
            
            Battle.hideMainMenu();
            Dialog.print(0, 0, Game.getText("@@player_cannot_use_spell_yet"));
            Game.sleep(500);
        }
        else {
            
            int selectedSpell = 0;

            // wait for appropriate cursor start blink time
            long blinkTime = System.nanoTime();

            while (true) {
                for (int r = 0; r < SPELLS_ID.size(); r++) {
                    Dialog.print(3, 19, 5 + 2 * r, ' ');
                }

                // blink cursor
                if ((int) ((System.nanoTime() - blinkTime) 
                                            * 0.0000000035) % 2 == 0) {

                    Dialog.print(3, 19, 5 + 2 * selectedSpell, 2);
                }

                if (Input.isKeyJustPressed(KEY_UP) 
                        && selectedSpell > 0) {
                    
                    selectedSpell--;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_DOWN) 
                                    && selectedSpell < SPELLS_ID.size() - 1) {

                    selectedSpell++;
                    blinkTime = System.nanoTime();
                }
                else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                    Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                    returnSpell 
                        = Resource.getSpellById(SPELLS_ID.get(selectedSpell));
                    break;
                }
                else if (Input.isKeyJustPressed(KEY_CANCEL)) {
                    returnSpell = null;
                    break;
                }
                View.refresh();
                Game.sleep(1000 / 60);
            }
        }

        View.getOffscreenGraphics2D(3).clearRect(
                18 * 8, 3 * 8, 12 * 8, (2 * SPELLS_ID.size() + 3) * 8);
        
        return returnSpell;
    }
    
    public static Spell getSpellByLevel(int level) {
        for (Spell spell : Resource.getSPELLS().values()) {
            if (spell.getLevel() == level) {
                return spell;
            }
        }
        return null;
    }
    
}
