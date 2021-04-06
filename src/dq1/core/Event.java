package dq1.core;

import static dq1.core.Event.MovementType.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Event class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Event {
    
    public static enum MovementType { STATIC, RANDOM }
    private static final String[] ANIMATION_IDS 
                                    = {"down", "left", "up", "right"};
    
    private String id;
    private String type;
    private Animation animation;
    private MovementType movementType = MovementType.STATIC;
    private int x;
    private int y;
    private boolean visible = true;
    private boolean blocked = true;
    private boolean fireRequired = false;
    private Script script;
    
    private int walking = -1;
    private int walkDx;
    private int walkDy;
    private final Point currentWalkTile = new Point();
    private final Point targetWalkTile = new Point();
    private Rectangle walkArea;
    
    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(
            BufferedImage image, int cols, int rows, int animationSpeed) {
        
        animation = new Animation(image, cols, rows, animationSpeed);
    }
    
    public void createAnimation(String animationId, int[] frameIndices) {
        animation.createAnimation(animationId, frameIndices);
    }

    public void changeAnimation(String animationId) {
        animation.change(animationId);
    }
    
    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
        currentWalkTile.setLocation(x / 16, y / 16);
        targetWalkTile.setLocation(-1, -1);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isBlocked() {
        return blocked;
    }

    public boolean isBlocked(int row, int col) {
        return visible && blocked 
                && ((currentWalkTile.x == col && currentWalkTile.y == row)
                || (targetWalkTile.x == col && targetWalkTile.y == row));
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Point getTargetWalkTile() {
        return targetWalkTile;
    }

    public boolean isFireRequired() {
        return fireRequired;
    }

    public void setFireRequired(boolean fireRequired) {
        this.fireRequired = fireRequired;
    }

    public Script getScript() {
        return script;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public Rectangle getWalkArea() {
        return walkArea;
    }

    public void setWalkArea(int x, int y, int width, int height) {
        if (walkArea == null) {
            walkArea = new Rectangle();
        }
        walkArea.setBounds(x, y, width, height);
    }
    
    public void update() {
        if (animation != null) {
            animation.update();
        }
        if (walking == 0) {
            walking--;
            currentWalkTile.setLocation(targetWalkTile);
            targetWalkTile.setLocation(-1, -1);
        }
        else if (walking < 0 && movementType == RANDOM) {
            handleMovement();
        }
        else {
            walking--;
            x += walkDx;
            y += walkDy;
        }        
    }

    private void walk(int dx, int dy) {
        TileMap currentMap = Game.getCurrentMap();
        
        int targetX = x + dx * 16;
        int targetY = y + dy * 16;
        
        // outside of walking area
        if (walkArea != null && !walkArea.contains(targetX, targetY)) {
            return;
        }
        
        if (!currentMap.isBlocked(targetX, targetY) || (dx == 0 && dy == 0)) {
            targetWalkTile.setLocation(targetX / 16, targetY / 16);
            walking = 16;
            walkDx = dx;
            walkDy = dy;
        }
    }
    
    public void handleMovement() {
        int[][] directions = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}
                            , {0, 0}, {0, 0}, {0, 0}, {0, 0}
                            , {0, 0}, {0, 0}, {0, 0}, {0, 0}};
        int r = (int) (12 * Math.random());
        int[] direction = directions[r];
        walk(direction[0], direction[1]);
        if (r < 4) {
            animation.change(ANIMATION_IDS[r]);
        }
    }
    
    private static final boolean DEBUG_MODE = false;
    private static final String DEBUG_EVENT_ID = "soldier_tantegel_castle_1";
    private static final Color DEBUG_EVENT_COLOR = new Color(0, 0, 255, 128);
    private static final Color DEBUG_AREA_COLOR = new Color(0, 255, 0, 128);
    
    public void draw(Graphics2D g) {
        if (animation != null) {
            int ex = x - Player.getX() + 8 * 16 - 8;
            int ey = y - Player.getY() + 7 * 16;
            animation.draw(g, ex, ey);
            
            // debug walking area
            if (DEBUG_MODE && walkArea != null && id.equals(DEBUG_EVENT_ID)) {
                g.setColor(DEBUG_AREA_COLOR);
                g.fillRect(walkArea.x - Player.getX() + 8 * 16 - 8
                        , walkArea.y - Player.getY() + 7 * 16
                        , walkArea.width, walkArea.height);
                
                g.setColor(DEBUG_EVENT_COLOR);
                g.fillRect(ex, ey, 16, 16);
            }
        }
    }

    public void turnToPlayer() {
        turnTo(Player.getY() / 16, Player.getX() / 16);
    }
    
    public void turnTo(int rowTarget, int colTarget) {
        int dx = colTarget - x / 16;
        int dy = rowTarget - y / 16;
        if (dy > 0) {
            changeAnimation("down");
        }
        else if (dx < 0) {
            changeAnimation("left");
        }
        else if (dy < 0) {
            changeAnimation("up");
        }
        else if (dx > 0) {
            changeAnimation("right");
        }
    }
    
    public void clearLocalVars() {
        if (script != null) {
            script.clearLocalVars();
        }
    }
    
    // return: exit map
    public boolean execute(String label) throws Exception {
        boolean exitMap = false;
        if (script != null) {
            exitMap = script.execute(label);
        }
        return exitMap;
    }
    
}
