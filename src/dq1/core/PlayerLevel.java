package dq1.core;

/**
 * PlayerLevel class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class PlayerLevel {

    public static int lastLevel;
    
    private final int lv;
    private final int str;
    private final int agi;
    private final int hp;
    private final int mp;
    private final int xp;

    public PlayerLevel(int lv, int str, int agi, int hp, int mp, int xp) {
        this.lv = lv;
        this.str = str;
        this.agi = agi;
        this.hp = hp;
        this.mp = mp;
        this.xp = xp;
    }

    public PlayerLevel(String serializedData) {
        String[] args = serializedData.trim().split(",");
        String[] h = args[0].trim().split("\\s+");
        lv = Integer.parseInt(h[1]);
        str = Integer.parseInt(args[1].trim());
        agi = Integer.parseInt(args[2].trim());
        hp = Integer.parseInt(args[3].trim());
        mp = Integer.parseInt(args[4].trim());
        xp = Integer.parseInt(args[5].trim());
    }

    public int getLv() {
        return lv;
    }

    public int getStr() {
        return str;
    }

    public int getAgi() {
        return agi;
    }

    public int getHP() {
        return hp;
    }

    public int getMP() {
        return mp;
    }

    public int getXP() {
        return xp;
    }

    @Override
    public String toString() {
        return "PlayerLevel{" + "lv=" + lv + ", str=" + str + ", agi=" 
                    + agi + ", hp=" + hp + ", mp=" + mp + ", xp=" + xp + '}';
    }
    
}
