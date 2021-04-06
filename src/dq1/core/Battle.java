package dq1.core;

import dq1.core.Script.ScriptCommand;
import static dq1.core.Settings.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Battle class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Battle {

    private static final Animation BACKGROUNDS;
    private static final Animation ENEMIES_IMAGES;
    private static Enemy enemy;
    private static int speed = 11;
    
    static {
        BACKGROUNDS = new Animation(
                Resource.getImage(RES_BATTLE_BACKGROUNDS_IMAGE), 13, 1, 0);
        
        ENEMIES_IMAGES = new Animation(
                Resource.getImage(RES_BATTLE_ENEMIES_IMAGE), 40, 1, 0);
    }

    public static Animation getBackgrounds() {
        return BACKGROUNDS;
    }

    public static Animation getEnemies() {
        return ENEMIES_IMAGES;
    }

    public static int getSpeed() {
        return speed;
    }

    public static void setSpeed(int speed) {
        Battle.speed = speed;
    }
    
    public static void showMainMenu() {
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, Game.getTexts().getProperty("@@battle_ask_command"));
        Game.sleep(50);

        Dialog.drawBoxBorder(2, 12, 1, 27, 6);
        Dialog.fillBox(2, ' ', 13, 2, 26, 5);
        Dialog.printText(2, 16, 1, 
                Game.getTexts().getProperty("@@battle_menu_command"));
        
        Dialog.print(2, 15, 1, 15);
        
        Dialog.printText(2, 14, 3, 
                Game.getTexts().getProperty("@@battle_menu_fight"));
        
        Dialog.printText(2, 14, 5, 
                Game.getTexts().getProperty("@@battle_menu_run"));
        
        Dialog.printText(2, 22, 3, 
                Game.getTexts().getProperty("@@battle_menu_spell"));
        
        Dialog.printText(2, 22, 5, 
                Game.getTexts().getProperty("@@battle_menu_item"));
    }        

    public static void hideMainMenu() {
        Dialog.fillBox(2, -1, 12, 1, 27, 6);
    }   

    @ScriptCommand(name = "start_battle")
    public static void start(String resultGlobalVar, int enemyId, int tileId
            , int playerCantRun, int enemyWillNotRun
                                        , String musicId) throws Exception {
        
        Enemy enemyTmp = Resource.getEnemyById(enemyId);
        Tile tile = Resource.getTileSet(RES_TILESET_INF).get(tileId);
        int battleResult = start(
            enemyTmp, tile, playerCantRun != 0, enemyWillNotRun != 0, musicId);
        
        Script.setGlobalValue(resultGlobalVar, battleResult);
    }
    
    // return result = 0 -> player is dead
    //                 1 -> player battle win
    //                 2 -> player ran away
    //                 3 -> enemy ran away
    public static int start(Enemy enemy, Tile tile
            , boolean playerCantRun, boolean enemyWillNotRun
                                        , String musicId) throws Exception {
        int battleResult = 0;
        int dialogOriginalSpeed = Dialog.getSpeed();
        Dialog.setSpeed(speed);
        Script.setGlobalValue("##player_is_battling", 1);
        Audio.saveCurrentMusic();
        Audio.playMusic(musicId);
        Player.resetAllStatus();
        enemy.reset();
        Battle.enemy = enemy;
        enemy.reset();
        Script.setGlobalValue("##enemy_id", enemy.getId());
        Script.setGlobalValue("$$enemy_name", enemy.getName());
        Script.setGlobalValue("$$enemy_xp", enemy.getXP());
        Script.setGlobalValue("$$enemy_gp", enemy.getGP());
        Audio.playSound(Audio.SOUND_CRITICAL_ATTACK);
        View.flash(1, 5, new Color(255, 255, 255, 200), 40);
        Game.sleep(250);
        BufferedImage background = tile.getBattleBackground();
        if (Game.getCurrentMap().isDark()) {
            background = BACKGROUNDS.getFrameImage(12);
        }
        View.showBattleBackgroundImageAnimation(background);
        BufferedImage enemyImage = ENEMIES_IMAGES.getFrameImage(enemy.getId());
        View.getOffscreenGraphics2D(4).drawImage(enemyImage, 72, 64 - 8, null);
        View.flashLayer4(3, Color.BLACK, 50);
        Dialog.open();
        Dialog.print(0, 0, Game.getText("@@battle_enemy_appears"));
        Game.sleep(250);
        Player.showSimplifiedStatus();
        Game.sleep(250);
        boolean battling = true;
        boolean enemyHasInitiative = checkEnemyInitiative();
        if (hasEnemyFled(enemyWillNotRun)) {
            Audio.playSound(Audio.SOUND_RUN_AWAY);
            showEnemyDisappearing();
            Audio.restoreAndPlaySavedMusic();
            Dialog.print(0, 0, "");
            Dialog.print(0, 0, Game.getText("@@battle_enemy_escaped"));
            Game.sleep(1000);
            enemy = null;
            battling = false;
            battleResult = 3;
        }
        while (battling) {
            if (enemyHasInitiative) {
                Dialog.print(0, 0, "");
                Dialog.print(0, 0, Game.getText("@@battle_enemy_initiative"));
                enemyHasInitiative = false;
            }
            else {
                if (Player.isStatusASleep() 
                                && Player.getStatusASleepTurn() > 0) {
                    
                    // Waking up from sleep is a 50/50 chance, but because 
                    // of the limitations of the original game programming 
                    // it's not possible for it to last for more than 6 turns.
                    boolean playerWokeUp = Util.random(2) == 0;
                    if (playerWokeUp || Player.getStatusASleepTurn() >= 6) {
                        Player.setStatusASleep(false);
                        Dialog.print(0, 0, "");
                        Dialog.print(0, 0
                                    , Game.getText("@@battle_player_wokeup"));
                    }
                }
                Player.incStatusASleepTurn();
                
                if (Player.isStatusASleep()) {
                    Dialog.print(0, 0, "");
                    Dialog.print(0, 0
                            , Game.getText("@@battle_player_still_asleep"));

                    Game.sleep(500);
                }
                else {
                    boolean optionExecuted = false;
                    while (!optionExecuted) {
                        int option = Battle.askPlayerCommand();
                        switch (option) {
                            case 0: optionExecuted = playerFight(); break;
                            case 1: optionExecuted = playerCastSpell(); break;
                            case 2: optionExecuted 
                                            = playerRun(playerCantRun); break;

                            case 3: optionExecuted = playerUseItem(); break;
                        }
                    }
                }
            }
            // player run away
            if (Battle.enemy == null) {
                battleResult = 2;
                break;
            }
            // check enemy defeated
            else if (enemy.getHP() <= 0) {
                enemyDefeated();
                // Ref.: https://www.youtube.com/watch?v=7L9M8ApJ3T0&t=770s
                if (Player.isLevelUp()) {
                    Player.levelUp();
                }
                Audio.restoreAndPlaySavedMusic();
                battleResult = 1;
                break;
            }
            processEnemysTurn(enemyWillNotRun);
            // enemy escaped
            if (Battle.enemy == null) {
                battleResult = 3;
                break;
            }
            // check player was killed
            else if (Player.getHP() <= 0) {
                Player.kill();
                battleResult = 0;
                
                // TODO player is dead. Revive and teleport to Tantegel King
                
                break;
            }
        }
        View.getOffscreenGraphics2D(4).clearRect(72, 64 - 8, 112, 112);        
        View.getOffscreenGraphics2D(2).clearRect(72, 64 - 8, 112, 112 - 8);
        Player.hideSimplifiedStatus();
        Dialog.close();
        Script.setGlobalValue("##player_is_battling", 0);
        Dialog.setSpeed(dialogOriginalSpeed);
        return battleResult;
    }

    //-=Initiative=-
    //This is the same equation as running from enemies in Group 1,
    //except instead of being blocked, the enemy will go first.        
    //If HeroAgility * Random # < EnemyAgility * Random # * GroupFactor,
    //then the enemy will block you.
    //Random # is a random number between 0 and 255.
    //GroupFactor depends on the group:
    //Group 1: 0.25        
    private static boolean checkEnemyInitiative() {
        int randPlayer = Util.random(256);
        int randEnemy = Util.random(256);
        return Player.getAgi() * randPlayer < enemy.getAgi() * randEnemy * 0.25;
    }

    private static final Point[][] CURSOR_LOCATIONS = { 
            { new Point(13, 3), new Point(21, 3) },
            { new Point(13, 5), new Point(21, 5) } };
        
    public static int askPlayerCommand() {
        showMainMenu();
        
        Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
        
        int curRow = 0;
        int curCol = 0;
        
        // wait for appropriate cursor start blink time
        long blinkTime = System.nanoTime();
        
        while (true) {
            Dialog.print(2, 13, 3, ' ');
            Dialog.print(2, 13, 5, ' ');
            Dialog.print(2, 21, 3, ' ');
            Dialog.print(2, 21, 5, ' ');
            Point cursorLocation = CURSOR_LOCATIONS[curRow][curCol];
            
            // blink cursor
            if ((int) ((System.nanoTime() - blinkTime) 
                                        * 0.0000000035) % 2 == 0) {
                
                Dialog.print(2, cursorLocation.x, cursorLocation.y, 2);
            }
            
            if (Input.isKeyJustPressed(KEY_LEFT) && curCol > 0) {
                curCol--;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_RIGHT) && curCol < 1) {
                curCol++;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_UP) && curRow > 0) {
                curRow--;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_DOWN) && curRow < 1) {
                curRow++;
                blinkTime = System.nanoTime();
            }
            else if (Input.isKeyJustPressed(KEY_CONFIRM)) {
                Audio.playSound(Audio.SOUND_MENU_CONFIRMED);
                int option = curCol + curRow * 2;
                return option;
            }
            View.refresh();
            Game.sleep(1000 / 60);
        }
    }

    //~Player's Attack~
    //All enemies except the Golem and the Dragonlord have the capability 
    //of dodging any attack, including excellent moves.
    //Just like the standard enemy attack, the range for the hero's standard 
    //attack damage is from: (HeroAttack - EnemyAgility / 2) / 4,
    //to: (HeroAttack - EnemyAgility / 2) / 2
    //If the damage done is less than 1, then there is 50/50 chance that you 
    //will do either no damage, or 1 damage.
    //Excellent moves have a 1/32 chance of happening per attack, and the
    //formulas completely ignore the defense of the enemy.  
    //They range from: Hero attack / 2
    //to: Hero attack
    //Unfortunately, excellent moves are not possible against either form
    //of theDragonlord. They can also be dodged.
    public static boolean playerFight() {
        hideMainMenu();
        Dialog.print(0, 0, Game.getText("@@battle_player_attacks"));
        int enemyAgility = enemy.getAgi();

        //Excellent moves have a 1/32 chance of happening per attack,
        //and the formulas completely ignore the defense of the enemy.
        //Unfortunately, excellent moves are not possible against either
        //form of the Dragonlord.  They can also be dodged.
        //Ref.: https://www.youtube.com/watch?v=8txzk0jkiiA&t=213s
        boolean excellentMove = !enemy.isFinalBoss() 
                && Util.random(32) == 0;
        
        if (excellentMove) {
            Audio.playSound(Audio.SOUND_CRITICAL_ATTACK);
        }
        else {
            Audio.playSound(Audio.SOUND_PLAYER_WILL_ATTACK);
        }
        Game.sleep(500);

        if (excellentMove) {
            enemyAgility = 0;
            Dialog.print(0, 0, Game.getText("@@battle_player_critical_attack"));
        }
        
        int minDamage = (Player.getAtk() - enemyAgility / 2) / 4;
        int maxDamage = (Player.getAtk() - enemyAgility / 2) / 2;
        int damage = 0; 

        if (minDamage < maxDamage) {
            damage = Util.random(minDamage, maxDamage);
        }

        //If the damage done is less than 1, then there is 50/50 
        //chance that you will do either no damage, or 1 damage.
        if (damage < 1) {
            damage = Util.random(2);
        }

        if (damage == 0)  {
            Audio.playSound(Audio.SOUND_ENEMY_DODGING);
            Dialog.print(0, 0, Game.getText("@@battle_player_attacks_miss"));
        }
        // note: critical attacks can also be dodged.
        else if (damage > 0 && enemy.checkResistedAttack()) {
            Audio.playSound(Audio.SOUND_ENEMY_DODGING);
            Dialog.print(0, 0, Game.getText("@@battle_enemy_dodged"));
        }
        else if (damage > 0) {
            hitEnemy(damage);
        }
        return true;
    }

    @ScriptCommand(name = "hit_enemy")
    public static void hitEnemy(int damageMin, int damageMax) {
        hitEnemy(Util.random(damageMin, damageMax));
    }
    
    private static void hitEnemy(int damage) {
        Audio.playSound(Audio.SOUND_ENEMY_HIT);
        View.flashLayer4(8, Color.RED, 25);
        Game.sleep(500);
        enemy.hit(damage);
        Script.setGlobalValue("$$battle_damage", damage);
        Dialog.print(0, 0, Game.getText("@@battle_enemy_damage"));
        Game.sleep(500);   
    }
    
    //~Player Run Away~
    //Hero: Certain groups of monsters have higher chances of blocking your
    //running. The more difficult an enemy is, it may belong to a group
    //that is better at blocking.
    //Group 1: #00 (Slime) - #19 (Druinlord)
    //Group 2: #20 (Drollmagi) - #29 (Werewolf)
    //Group 3: #30 (Green Dragon) - #34 (Blue Dragon)
    //Group 4: #35 (Stoneman) - #39 (Dragonlord second form)
    //So for instance, group 4 will block you much easier than group 1 will.
    //On top of that is a test of agility.  The test goes like this:
    //If HeroAgility * Random # < EnemyAgility * Random # * GroupFactor,
    //then the enemy will block you.
    //Random # is a random number between 0 and 255.
    //GroupFactor depends on the group:
    //Group 1: 0.25
    //Group 2: 0.375
    //Group 3: 0.5
    //Group 4: 1.0
    //So you can see that enemies in group 1 are going to have a much
    //lower product than enemies in group 4, meaning that it will be more
    //likely that your product will be higher and that you will get away.
    //And yes, it is possible to run from the Dragonlord, although you
    //will repeat the same dialog and have to fight the first form again.
    //You can also run from the Axe Knight in Hauksness, the Golem
    //outside Cantlin, and the Green Dragon in the Marsh Cave,
    //but it will put you back a spot so that you can't just skip
    //them and get their prize.
    //Also worth noting is that if the monster is asleep, you can
    //run away every time.
    private static boolean playerRun(boolean cantRun) {
        hideMainMenu();
        int randPlayer = Util.random(256);
        int randEnemy = Util.random(256);
        double[] groupFactor = { 0.0, 0.25, 0.375, 0.5, 1.0 };
        boolean runBlocked = cantRun || (Player.getAgi() * randPlayer 
                < enemy.getAgi() * randEnemy * groupFactor[enemy.getGroupId()]);
        
        if (enemy.isStatusASleep()) {
            runBlocked = false;
        }
        if (!runBlocked){
            Audio.playSound(Audio.SOUND_RUN_AWAY);
            Game.sleep(500);
            Audio.restoreAndPlaySavedMusic();
        }
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, Game.getText("@@battle_player_runaway"));
        Game.sleep(500);
        if (runBlocked) {
            Dialog.print(0, 0, Game.getText("@@battle_player_runaway_fail"));
        }
        else {
            enemy = null;
        }
        Game.sleep(1000);
        return true;
    }

    private static boolean playerUseItem() throws Exception {
        Item item = Inventory.showSelectItemDialog();
        hideMainMenu();
        boolean success = false;
        if (item != null) {
            if (!item.isUseInBattle()) {
                Dialog.print(0, 0, Game.getText("@@item_cant_use_in_battle"));
                Game.sleep(500);
                return false;
            }
            else {
                success = item.use("on_use_when_battle");
            }
            Player.showSimplifiedStatus();
        }
        View.refresh();
        Game.sleep(500);
        return success;
    }
    
    private static boolean playerCastSpell() throws Exception {
        Spell spell = Magic.showSelectSpellDialog();
        hideMainMenu();
        boolean sucess = false;
        if (spell != null) {
            if (!spell.isUseInBattle()) {
                Dialog.print(0, 0, Game.getText("@@spell_cant_use_in_battle"));
                Game.sleep(500);
                return false;
            } 
            else if (Player.getMP() < spell.getMp()) {
                Dialog.print(0, 0, Game.getText("@@player_mp_too_low"));
                Game.sleep(500);
                return false;
            }
            Script.setGlobalValue("$$casting_spell_name", spell.getName());
            Dialog.print(0, 0, Game.getText("@@player_cast_spell"));
            Audio.playSound(Audio.SOUND_SPELL);
            View.flash(4, 5, Color.WHITE, 30);
            Player.consumeMP(spell.getMp());
            Player.showSimplifiedStatus();
            Game.sleep(750);
            if (Player.isStatusStopSpell()) {
                Dialog.print(0, 0, Game.getText("@@spell_blocked"));
                sucess = true;
            }
            else {
                sucess = spell.cast("on_use_when_battle");
            }
            Player.showSimplifiedStatus();
        }
        View.refresh();
        Game.sleep(500);
        return sucess;
    }
    
    private static void processEnemysTurn(boolean enemyWillNotRun) {
        if (enemy.isStatusASleep()) {
            // You're always guaranteed one turn of attack, and after 
            // that turn it is a 1/3 chance that the enemy will wake up.
            if (enemy.getStatusASleepTurn() > 0) {
                boolean wokeUp = Util.random(3) == 0;
                if (wokeUp) {
                    enemy.setStatusASleep(false);
                    Dialog.print(0, 0, Game.getText("@@battle_enemy_wokeup"));
                }
            }
            if (enemy.isStatusASleep()) {
                Dialog.print(0, 0, Game.getText("@@battle_enemy_asleep"));
                enemy.incStatusASleepTurn();
                Game.sleep(500);
                return;
            }
        }
        if (hasEnemyFled(enemyWillNotRun)) {
            showEnemyDisappearing();
            Audio.restoreAndPlaySavedMusic();
            Audio.playSound(Audio.SOUND_RUN_AWAY);
            Dialog.print(0, 0, "");
            Dialog.print(0, 0, Game.getText("@@battle_enemy_escaped"));
            Game.sleep(1000);
            enemy = null;
            return;
        }
        boolean isSpecialMove1Enabled 
                = enemy.isSpecialMove1() && !enemy.isStatusStopSpell();
        
        switch (enemy.getSpecialMove1()) {
            case 0: isSpecialMove1Enabled &= !Player.isStatusASleep(); 
                    break; // SLEEP
            case 1: isSpecialMove1Enabled &= !Player.isStatusStopSpell(); 
                    break; // STOP_SPELL
            case 2: isSpecialMove1Enabled &= 
                        enemy.getHP() <= enemy.getHPMax() / 4; 
            
                    break; // HEAL
            case 3: isSpecialMove1Enabled &= 
                        enemy.getHP() <= enemy.getHPMax() / 4; 
            
                    break; // HEAL_MORE
        }
        if (isSpecialMove1Enabled) {
            enemyCastSpell(enemy.getSpecialMove1(), -1);
        }
        else if (enemy.isSpecialMove2() && !enemy.isStatusStopSpell()) {
            enemyCastSpell(-1, enemy.getSpecialMove2());
        }
        else {
            // regular attack
            Dialog.print(0, 0, "");
            Dialog.print(0, 0, Game.getText("@@battle_enemy_attacks"));
            Audio.playSound(Audio.SOUND_ENEMY_WILL_ATTACK);
            Game.sleep(500);
            
            int damage = calculatePlayersDamage();
            if (damage == 0) {
                Audio.playSound(Audio.SOUND_ENEMY_MISSED_ATTACK);
                Dialog.print(0, 0, "");
                Dialog.print(0, 0, Game.getText("@@battle_enemy_attacks_miss"));
                Game.sleep(500);
            }
            else {
                enemyHitPlayer(damage);
            }
        }        
    }

    private static void enemyCastSpell(int specialMove1, int specialMove2) {
        String specialName = "";
        int soundId = -1;
        boolean flashScreen = false;
        boolean sleepWorked = false;
        boolean stopSpellWorked = false;
        int playerHitHp = 0;
        int enemyHealHp = 0;
        switch (specialMove1) {
            case 0: // invoke SLEEP
                //SLEEP always puts you to sleep, and there is no resisting it.
                //Waking up from sleep is a 50/50 chance, but because of the 
                //limitations of the game programming it's not possible for it 
                //to last for more than 6 turns.
                soundId = Audio.SOUND_SPELL;
                Player.setStatusASleep(sleepWorked = true);
                specialName = "Sleep"; 
                flashScreen = true;
                break; 
            case 1: // invoke STOP_SPELL
                //STOPSPELL has a 50/50 chance of working against you.  
                //If you have Erdrick's Armor it has no chance of working.
                soundId = Audio.SOUND_SPELL;
                stopSpellWorked = !Player.isImmuneToStopSpell() 
                                                    && Util.random(2) == 0;
                
                Player.setStatusStopSpell(stopSpellWorked);
                specialName = "Stop Spell"; 
                flashScreen = true;
                break;
            case 2: // invoke HEAL
                //HEAL recovers 20 - 27 HP
                soundId = Audio.SOUND_SPELL;
                enemyHealHp = Util.random(20, 27);
                specialName = "Heal"; 
                flashScreen = true;
                break;
            case 3: // invoke HEAL_MORE
                //HEALMORE recovers 85 - 100 HP    
                soundId = Audio.SOUND_SPELL;
                enemyHealHp = Util.random(85, 100);
                specialName = "Heal More"; 
                flashScreen = true;
                break;
        }
        switch (specialMove2) {
            case 0: // invoke HURT
                //HURT does  3 - 10 damage
                //Both Magic Armor and Erdrick's Armor will 
                //reduce HURT spells by 1/3.  So:
                //HURT does  2 -  6 damage vs. Erdrick's and Magic Armor
                soundId = Audio.SOUND_SPELL;
                boolean reduced = Player.isRelievesHurt();
                playerHitHp = reduced ? Util.random(2, 6) : Util.random(3, 10);
                specialName = "Hurt"; 
                flashScreen = true;
                break;
            case 1: // invoke HURTMORE
                //HURTMORE does 30 - 45 damage        
                //Both Magic Armor and Erdrick's Armor will
                //reduce HURT spells by 1/3.  So:
                //HURTMORE does 20 - 30 damage vs. Erdrick's and Magic Armor
                soundId = Audio.SOUND_SPELL;
                playerHitHp = Player.isRelievesHurt() 
                        ? Util.random(20, 30) : Util.random(30, 45);
                
                specialName = "Hurt More"; 
                flashScreen = true;
                break;
            case 2: // invoke BREATH ATTACK 1
                //~Fire breath~
                //There are two types of fire breath.  
                //Only the Dragonlord's second form has the stronger breath.
                //The rest of the enemies that have breath attacks only have
                //the weaker type of breath.
                //Weak breath does 16 - 23 damage
                //Strong breath does 65 - 72 damage
                //The only thing that protects against fire breath is 
                //Erdrick's Armor, which reduces the damage by 1/3. So:
                //  Weak breath vs. Erdrick's Armor does 10 - 14 damage
                //Strong breath vs. Erdrick's Armor does 42 - 48 damage        
                soundId = Audio.SOUND_DRAGON_LORD_BREATHING_FIRE;
                playerHitHp = Player.isRelievesBreath()
                        ? Util.random(16, 23) : Util.random(10, 14);
                
                specialName = "Breath Fire"; 
                flashScreen = false;
                break;
            case 3: // invoke BREATH ATTACK 2 
                soundId = Audio.SOUND_DRAGON_LORD_BREATHING_FIRE;
                playerHitHp = Player.isRelievesBreath()
                        ? Util.random(65, 72) : Util.random(42, 48);
                
                specialName = "Breath Fire"; 
                flashScreen = false;
                break;
        }
        Dialog.print(0, 0, "");
        Script.setGlobalValue("$$battle_spell_name", specialName);
        Dialog.print(0, 0, Game.getText("@@battle_enemy_cast_spell"));
        Game.sleep(500);
        if (soundId > 0) {
            Audio.playSound(soundId);
        }
        if (flashScreen) {
            View.flashLayer4(8, Color.WHITE, 40);
        }
        else {
            Game.sleep(1000);
        }
        Game.sleep(250);
        if (enemyHealHp > 0) {
            enemy.heal(enemyHealHp);
        }
        else if (playerHitHp > 0) {
            enemyHitPlayer(playerHitHp);
        }
        else if (sleepWorked) {
            Dialog.print(0, 0
                    , Game.getText("@@battle_enemy_cast_sleep_success"));
        }
        else if (stopSpellWorked) {
            Dialog.print(0, 0
                    , Game.getText("@@battle_enemy_cast_stop_spell_success"));
        }
        else {
            // enemy's spell failed
            Dialog.print(0, 0, Game.getText("@@spell_will_not_work"));
        }
    }
        
    private static void enemyHitPlayer(int damage) {
        Audio.playSound(Audio.SOUND_PLAYER_HIT);
        int hpBefore = Player.getHP();
        Player.hit(damage);
        if (Player.getHP() == 0) {
            View.fill(1, "0xff000080");
            View.flash(4, 6, Color.RED, 40);
        }
        else {
            View.shake(2, 3, 5);
        }
        Game.sleep(500);
        Player.showSimplifiedStatus();
        Script.setGlobalValue("$$battle_damage", Math.min(hpBefore, damage));
        Dialog.print(0, 0, Game.getText("@@battle_player_damage"));
        Game.sleep(1000);  
    }
    
    //~Enemy Running~
    //Enemy: If your strength is two times the enemy's strength or more, there
    //is a 25% chance the enemy will run when it's given a turn.  The enemy
    //also makes this check right upon encounter, so he may get a chance to
    //run right away regardless of initiative.
    //Ref.: https://www.youtube.com/watch?v=8txzk0jkiiA&t=165s
    private static boolean hasEnemyFled(boolean enemyWillNotRun) {
        if (Player.getAtk() >= 2 * enemy.getStr()) {
            return !enemyWillNotRun && Util.random(4) == 0;
        }
        return false;
    }
    
    //~Enemy Attacks~
    //There are two formulas for attack damage for enemies.
    //The standard range is from: (EnemyStrength - HeroDefense / 2) / 4
    //to: (EnemyStrength - HeroDefense / 2) / 2
    //The hero's defense is equal to his agility / 2 rounded down, 
    //plus the modifiers for his equipment.
    //The other type of attack happens if your defense power is greater than 
    //or equal to the enemy's strength. 
    //In that case, the range is from: 0 to (enemyStrength + 4) / 6    
    private static int calculatePlayersDamage() {
        int minDamage = (enemy.getStr() - Player.getDef() / 2) / 4;
        int maxDamage = (enemy.getStr() - Player.getDef() / 2) / 2;
        if (Player.getDef() >= enemy.getStr()) {
            minDamage = 0;
            maxDamage = (enemy.getStr() + 4) / 6;
        }
        int damage = 0;
        if (minDamage < maxDamage) {
            damage = Util.random(minDamage, maxDamage);
        }                
        return damage;
    }

    private static void enemyDefeated() {
        Audio.stopMusic();
        showEnemyDisappearing();
        Audio.playSound(Audio.SOUND_BATTLE_WIN);
        Dialog.print(0, 0, "");
        Dialog.print(0, 0, Game.getText("@@battle_enemy_defeated"));
        int increasedE = Player.incE(enemy.getXP());
        Player.showSimplifiedStatus();
        if (increasedE > 0) {
            Game.sleep(500);
            Dialog.print(0, 0, "");
            Dialog.print(0, 0, Game.getText("@@battle_won_xp"));
        }
        int increasedG = Player.incG(enemy.getGP());
        Player.showSimplifiedStatus();
        if (Player.isLevelUp()) {
            if (increasedG > 0) {
                Dialog.print(0, 0, Game.getText("@@battle_won_gp"));
            }
            Game.sleep(500);
        }
        else {
            if (increasedG > 0) {
                Dialog.print(0, 1, Game.getText("@@battle_won_gp"));
            }
            else {
                Game.waitForAnyKey();
            }
        }
    }

    private static void showEnemyDisappearing() {
        View.flashLayer4(6, new Color(0, 0, 0, 0), 50);
        View.getOffscreenGraphics2D(4).clearRect(72, 64 - 8, 112, 112);        
        View.refresh();
    }

    @ScriptCommand(name = "check_battle_enemy_resisted_sleep")
    public static void checkEnemyResistedSleep(String resultGlobalVar) {
        boolean result = !enemy.isStatusASleep() && enemy.checkResistedSleep();
        Script.setGlobalValue(resultGlobalVar, result ? 1 : 0);
    }

    @ScriptCommand(name = "check_battle_enemy_resisted_stop_spell")
    public static void checkEnemyResistedStopSpell(String resultGlobalVar) {
        boolean result = !enemy.isStatusStopSpell() 
                                && enemy.checkResistedStopSpell();
        
        Script.setGlobalValue(resultGlobalVar, result ? 1 : 0);
    }

    @ScriptCommand(name = "check_battle_enemy_resisted_hurt")
    public static void checkEnemyResistedHurt(String resultGlobalVar) {
        boolean result = enemy.checkResistedHurt();
        Script.setGlobalValue(resultGlobalVar, result ? 1 : 0);
    }

    @ScriptCommand(name = "sleep_enemy")
    public static void sleepEnemy() {
        enemy.setStatusASleep(true);
    }

    @ScriptCommand(name = "stop_spell_enemy")
    public static void stopSpellEnemy() {
        enemy.setStatusStopSpell(true);
    }

    public static void applyConfigValues() {
        Integer configSpeedMs = null;
        try {
            configSpeedMs = (Integer) 
                    Script.getGlobalValue("##game_config_battle_speed_ms");
        }
        catch (Exception e) { }
        if (configSpeedMs == null) {
            configSpeedMs = 11;
        }
        speed = configSpeedMs;
    }    
}
