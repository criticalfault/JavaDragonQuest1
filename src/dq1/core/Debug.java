package dq1.core;

import static dq1.core.Player.getLV;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debug class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Debug {

    public static void update() {
        if (1 == 1) {
            return;
        }
        
        
        if (Input.isKeyJustPressed(KeyEvent.VK_B)) {
            try {
                Battle.start("##res", 33, 0, 1, 1, "boss");
            } catch (Exception ex) {
                Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        
        if (Input.isKeyJustPressed(KeyEvent.VK_1)) {
            Script.setGlobalValue("##game_state_dragon_lord_defeated", 1);
            System.out.println("dragon lord defeated");
        }
        if (Input.isKeyJustPressed(KeyEvent.VK_2)) {
            Script.setGlobalValue("##game_state_dragon_lord_defeated", 0);
            System.out.println("dragon lord NOT defeated");
        }
        if (Input.isKeyJustPressed(KeyEvent.VK_3)) {
            Script.setGlobalValue("##game_state_princess_rescued", 0);
            System.out.println("princess not rescued");
        }
        if (Input.isKeyJustPressed(KeyEvent.VK_4)) {
            Script.setGlobalValue("##game_state_princess_rescued", 1);
            System.out.println("princess rescued / player carrying princess");
            Player.setState("princess");
            Player.changeDirection("down");
        }
        if (Input.isKeyJustPressed(KeyEvent.VK_5)) {
            Script.setGlobalValue("##game_state_princess_rescued", 2);
            System.out.println("princess rescued already in tantegel");
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_L)) {
                //Player.setLocation(113, 111); // southern shrine
                //Player.setLocation(5, 84); // northern shrine
                //Player.setLocation(53, 70); // rainbow drop
                //Player.setLocation(35, 287); // ???
                //Player.setLocation(15, 108); // kol
                //Player.setLocation(117, 87); // erdricks token
            try {
                //Game.teleport("tantegel_castle", 97, 16, "right", 1, "tantegel", 0, 0, 0, 1);
                //Game.teleport("rimuldar", 40, 24, "left", 1, "town", 0, 0, 0, 1);
                Game.teleport("charlock_castle", 294, 35, "up", 1, "dungeon", 0, 0, 0, 1);
                //Game.teleport("garinham", 9, 24, "right", 1, "town", 0, 0, 0, 1);
                //Game.teleport("cantlin", 26, 11, "down", 1, "town", 0, 0, 0, 1);
                //Game.teleport("kol", 19, 23, "up", 1, "town", 0, 0, 0, 1);
                //Game.teleport("world", 32, 16, "down", 1, "world", 0, 0, 0, 1); // erdrick's cave
                //Game.teleport("world", 52, 52, "down", 1, "world", 0, 0, 0, 1); // charlock castle
                //Game.teleport("world", 77, 103, "down", 1, "world", 0, 0, 0, 1); // golem cantlin
                //Game.teleport("world", 108, 15, "up", 1, "world", 0, 0, 0, 1); // kol
                //Game.teleport("world", 108, 48, "up", 1, "world", 0, 0, 0, 1); // swamp cave
                //Game.teleport("world", 107, 76, "left", 1, "world", 0, 0, 0, 1); // rimuldar
                //Game.teleport("world", 52, 45, "left", 1, "world", 0, 0, 0, 1); // brecconary
                //Game.teleport("world", 6, 6, "left", 1, "world", 0, 0, 0, 1); // garinham
                //Game.teleport("world", 47 + 40, 47 + 70, "left", 1, "world", 0, 0, 0, 1); // erdrick's token
                //Game.teleport("world", 29, 93, "left", 1, "world", 0, 0, 0, 1); // hauksness
                //Game.teleport("swamp_cave", 16, 25, "down", 1, "dungeon", 1, 1, 0, 1); // swamp cave
                //Game.teleport("world", 85, 5, "left", 1, "world", 0, 0, 0, 1); // northern shrine
                //Game.teleport("world", 112, 113, "left", 1, "world", 0, 0, 0, 1); // southern shrine
            } catch (Exception ex) {
                Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_W)) {
            try {
                Game.teleport("world", 77, 105, "down", 1, "world", 0, 0, 0, 1); // golem cantlin
            } catch (Exception ex) {
                Logger.getLogger(Debug.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_U)) {
            System.out.println("level up!");
            Player.levelUp();

            int currentLevel = getLV();
            PlayerLevel playerLevel = Resource.getPlayerLevel(currentLevel);
            PlayerLevel playerLevelNext = Resource.getPlayerLevel(currentLevel + 1);
            int expDif = playerLevelNext.getXP() - playerLevel.getXP();
            Script.setGlobalValue("##player_e", playerLevel.getXP() + (int) (expDif * Math.random()));

            Dialog.close();
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_E)) {
            boolean enabled = Game.getCurrentMap().isEnemiesEncounterEnabled();
            Game.getCurrentMap().setEnemiesEncounterEnabled(!enabled);
            System.out.println("enemies enabled = " + (!enabled));
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_9)) {
            Player.equipPlayer(14);
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_A)) {
//            for (int i = 0; i < 100; i++) {
//                Script.setGlobalValue("##player_item_" + i, 0);
//            }

            Inventory.addItem(42);
            Inventory.addItem(43);
            Inventory.addItem(46);
            Inventory.addItem(46);
            Inventory.addItem(46);
            //Script.setGlobalValue("##player_weapon_id", 0);
            //Script.setGlobalValue("##player_armor_id", 0);
            //Script.setGlobalValue("##player_shield_id", 0);
            //Player.equipPlayer(14);
            //Player.equipPlayer(26);
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_S)) {
            int lv = 30;
            int str = 25;
            int agi = 25;
            int hp = 5;
            int mp = 50;
            int hpMax = 50;
            int mpMax = 50;
            int gp = 999;
            int xp = 0;

            Script.setGlobalValue("##player_str", str);
            Script.setGlobalValue("##player_agi", agi);
            Script.setGlobalValue("##player_max_hp", hpMax);
            Script.setGlobalValue("##player_max_mp", mpMax);

            Script.setGlobalValue("##player_lv", lv);
            Script.setGlobalValue("##player_hp", hp);
            Script.setGlobalValue("##player_mp", mp);
            Script.setGlobalValue("##player_g", gp);
            Script.setGlobalValue("##player_e", xp);
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_M)) {
            Script.setGlobalValue("##player_mp", 999);
        }
        
        if (Input.isKeyJustPressed(KeyEvent.VK_E)) {
            Player.equipPlayer(22);
        }

        if (Input.isKeyJustPressed(KeyEvent.VK_G)) {
            Player.incG(100);
        }
        
        if (Input.isKeyJustPressed(KeyEvent.VK_D)) {
            //setLocation(46, 75);
            //Script.setGlobalValue("##player_item_46", 6);
            //Script.setGlobalValue("##player_hp", 5);
            //Script.setGlobalValue("##player_mp", 6);
            
            Script.setGlobalValue("##player_g", 1000);
            Script.setGlobalValue("##game_state_dragon_lord_defeated", 0);
            Script.setGlobalValue("##game_state_princess_rescued", 0);
        
//            Set<String> removeKeys = new HashSet<>();
//            for (String key : Script.getVARS().keySet()) {
//                if (key.startsWith("##player_item_")) {
//                    removeKeys.add(key);
//                }
//            }
//            for (String key : removeKeys) {
//                Script.getVARS().remove(key);
//            }
            
            Script.setGlobalValue("##player_weapon_id", -1);
            Script.setGlobalValue("##player_armor_id", -1);
            Script.setGlobalValue("##player_shield_id", -1);
            
        }        
    }
    
}
