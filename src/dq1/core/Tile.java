package dq1.core;

import java.awt.image.BufferedImage;

/**
 * Tile class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Tile {
    
    private final int id;
    private final BufferedImage image;
    private final boolean blocked;
    private final int damagePerStep;
    private final BufferedImage battleBackground;
    private final int enemyProbabilityNumerator;
    private final int enemyProbabilityDenominator;

    public Tile(int id, BufferedImage image, boolean blocked
            , int damagePerStep, BufferedImage battleBackground
            , int enemyProbabilityNumerator, int enemyProbabilityDenominator) {
        
        this.id = id;
        this.image = image;
        this.blocked = blocked;
        this.damagePerStep = damagePerStep;
        this.battleBackground = battleBackground;
        this.enemyProbabilityNumerator = enemyProbabilityNumerator;
        this.enemyProbabilityDenominator = enemyProbabilityDenominator;
    }
    
    public int getId() {
        return id;
    }

    public BufferedImage getImage() {
        return image;
    }

    public boolean isBlocked() {
        return blocked;
    }
    
    public int getDamagePerStep() {
        return damagePerStep;
    }

    public BufferedImage getBattleBackground() {
        return battleBackground;
    }

    public int getEnemyProbabilityNumerator() {
        return enemyProbabilityNumerator;
    }

    public int getEnemyProbabilityDenominator() {
        return enemyProbabilityDenominator;
    }

    public boolean wasMonsterEncountered() {
        if (enemyProbabilityDenominator == 0) {
            return false;
        }
        int r = Util.random(enemyProbabilityDenominator);
        
        //System.out.println("tile: " + id + " " + enemyProbabilityNumerator 
        //        + "/" + enemyProbabilityDenominator + " r=" + r);
        
        return r < enemyProbabilityNumerator;
    }
    
    @Override
    public String toString() {
        return "Tile{" + "id=" + id + ", image=" + image 
                + ", blocked=" + blocked + ", damagePerStep=" + damagePerStep 
                + ", battleBackground=" + battleBackground 
                + ", enemyProbabilityNumerator=" + enemyProbabilityNumerator 
                + ", enemyProbabilityDenominator=" + enemyProbabilityDenominator 
                + '}';
    }

}
