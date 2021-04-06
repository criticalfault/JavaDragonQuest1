package dq1.core;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Animation {
    
    private final BufferedImage image;
    private final int cols;
    private final int rows;
    private int animationSpeed = 60;
    private final Map<String, BufferedImage[]> animationsFrames 
            = new HashMap<>();
    
    private BufferedImage[] currentAnimation;
    private String currentAnimationId;
    private int frameIndex; // divided by 1000
    
    public Animation(
            BufferedImage image, int cols, int rows, int animationSpeed) {
        
        this.image = image;
        this.cols = cols;
        this.rows = rows;
        this.animationSpeed = animationSpeed;
    }

    public int getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    
    public void createAnimation(String id, int[] frames) {
        BufferedImage[] animationFrames = new BufferedImage[frames.length];
        for (int i = 0; i < frames.length; i++) {
            int index = frames[i];
            animationFrames[i] = getFrameImage(index);
        }
        animationsFrames.put(id, animationFrames);
        change(id);
    }
    
    public BufferedImage getFrameImage(int frameIndex) {
        if (image != null) {
            int frameWidth = image.getWidth() / cols;
            int frameHeight = image.getHeight() / rows;
            int ix = (frameIndex % cols) * frameWidth;
            int iy = (frameIndex / cols) * frameHeight;
            return image.getSubimage(ix, iy, frameWidth, frameHeight);
        }
        return null;
    }
    
    public void change(String id) {
        if (currentAnimationId != null && currentAnimationId.equals(id)) {
            return;
        }
        frameIndex = 0;
        currentAnimation = animationsFrames.get(id);
        currentAnimationId = id;
    }
    
    public void update() {
        if (currentAnimation != null) {
            frameIndex += animationSpeed;
        }
    }
    
    public void draw(Graphics2D g, int x, int y) {
        if (currentAnimation != null) {
            int index = (frameIndex / 1000) % currentAnimation.length;
            BufferedImage imageFrame = currentAnimation[index];
            g.drawImage(imageFrame, x, y, null);
        }
    }
            
}
