package dq1.core;

/**
 * Spell class.
 * 
 * @author Leonardo Ono (ono.leo@gmail.com)
 */
public class Spell {

    public static final Spell EMPTY 
            = new Spell(0, "", 0, 0, false, false);
    
    private final int id;
    private final String name;
    private final int level;
    private final int mp;
    private final boolean useInBattle;
    private final boolean useInMap;

    private Script script;

    public Spell(int id, String name, int level, int mp
                                    , boolean useInBattle, boolean useInMap) {
        
        this.id = id;
        this.name = name;
        this.level = level;
        this.mp = mp;
        this.useInBattle = useInBattle;
        this.useInMap = useInMap;
    }
    
    public Spell(String serializedData) {
        String[] args = serializedData.trim().split(",");
        String[] h = args[0].trim().split("\\s+");
        id = Integer.parseInt(h[1]);
        name = args[1].trim();
        level = Integer.parseInt(args[2].trim());
        mp = Integer.parseInt(args[3].trim());
        useInBattle = Boolean.parseBoolean(args[4].trim());
        useInMap = Boolean.parseBoolean(args[5].trim());
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public int getMp() {
        return mp;
    }

    public boolean isUseInBattle() {
        return useInBattle;
    }

    public boolean isUseInMap() {
        return useInMap;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }
    
    // return boolean -> cast successfully
    public boolean cast(String when) throws Exception {
        if (script != null) {
            return script.execute(when);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Spell{" + "id=" + id + ", name=" + name + ", level=" + level 
                + ", mp=" + mp + ", useInBattle=" + useInBattle 
                + ", useInMap=" + useInMap + '}';
    }
    
}
