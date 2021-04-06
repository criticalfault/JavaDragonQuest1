package dq1.core;

/**
 * Enemy class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Enemy {
    
    private final int id;
    private final String name;
    private final int str;
    private final int agi;
    private int hp;
    private final int hpMax;
    private final int pat;
    private final int sr;
    private final int dr;
    private final int xp;
    private final int gpMax;
    private int gp;

    // 0~3 used for initiative and blocking player's running logic
    private final int groupId;
    
    private final boolean finalBoss;
    
    private boolean statusASleep;
    private int statusASleepTurn;
    private boolean statusStopSpell;
    
    public Enemy(String serializedData) {
        String[] args = serializedData.trim().split(",");
        
        id = Integer.parseInt(args[0].trim(), 16);
        name = args[1].trim();
        str = Integer.parseInt(args[2].trim(), 16);
        agi = Integer.parseInt(args[3].trim(), 16);
        hpMax = Integer.parseInt(args[4].trim(), 16);
        pat = Integer.parseInt(args[5].trim(), 16);
        sr = Integer.parseInt(args[6].trim(), 16);
        dr = Integer.parseInt(args[7].trim(), 16);
        xp = Integer.parseInt(args[8].trim(), 16);
        gpMax = Integer.parseInt(args[9].trim(), 16);
        groupId = Integer.parseInt(args[10].trim(), 16);
        finalBoss = Boolean.parseBoolean(args[11].trim());
        statusASleep = false;
        statusASleepTurn = 0;
        statusStopSpell = false;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public int getHPMax() {
        return hpMax;
    }

    public int getPat() {
        return pat;
    }

    public int getSR() {
        return sr;
    }

    public int getDR() {
        return dr;
    }

    public int getXP() {
        return xp;
    }

    public int getGP() {
        return gp;
    }

    public int getGPMax() {
        return gpMax;
    }

    public int getGroupId() {
        return groupId;
    }

    public boolean isFinalBoss() {
        return finalBoss;
    }

    public boolean isStatusASleep() {
        return statusASleep;
    }

    public void setStatusASleep(boolean statusASleep) {
        this.statusASleep = statusASleep;
        statusASleepTurn = 0;
    }

    public int getStatusASleepTurn() {
        return statusASleepTurn;
    }

    public void incStatusASleepTurn() {
        this.statusASleepTurn++;
    }

    public boolean isStatusStopSpell() {
        return statusStopSpell;
    }

    public void setStatusStopSpell(boolean statusStopSpell) {
        this.statusStopSpell = statusStopSpell;
    }

    public void hit(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
    }

    public void heal(int hpHeal) {
        hp += hpHeal;
        if (hp > hpMax) hp = hpMax;
    }
    
    public boolean isDefeated() {
        return hp == 0;
    }

    public void reset() {
        hp = hpMax - (Util.random(256) * hpMax) / 1024;
        gp = (gpMax * (Util.random(64) + 192)) / 256;
        statusASleep = false;
        statusASleepTurn = 0;
        statusStopSpell = false;
    }
    
    public boolean isSpecialMove1() {
        int chance = pat & 0b00110000;
        chance = chance >> 4;
        return Util.random(3) + 1 <= chance;
    }

    public int getSpecialMove1() {
        int move = pat & 0b11000000;
        return (move >> 6);
    }

    public boolean isSpecialMove2() {
        int chance = pat & 0b00000011;
        return Util.random(3) + 1 <= chance;
    }

    public int getSpecialMove2() {
        int move = pat & 0b00001100;
        return (move >> 2);
    }
    
    public boolean checkResistedSleep() {
        int sleepResistance = sr >> 4;
        return Util.random(16) < sleepResistance;
    }

    public boolean checkResistedStopSpell() {
        int stopSpellResistance = (sr & 0xf0) >> 4;
        return Util.random(16) < stopSpellResistance;
    }

    public boolean checkResistedHurt() {
        int hurtResistance = (dr & 0xf0) >> 4;
        return Util.random(16) < hurtResistance;
    }

    public boolean checkResistedAttack() {
        int dodge = dr & 0x0f;
        return Util.random(64) < dodge;
    }

    @Override
    public String toString() {
        return "Enemy{" + "id=" + id + ", name=" + name + ", str=" + str 
                + ", agi=" + agi + ", hp=" + hp + ", hpMax=" + hpMax 
                + ", pat=" + pat + ", sr=" + sr + ", dr=" + dr + ", xp=" + xp 
                + ", gpMax=" + gpMax + ", gp=" + gp + ", groupId=" + groupId 
                + ", finalBoss=" + finalBoss + '}';
    }
    
}
