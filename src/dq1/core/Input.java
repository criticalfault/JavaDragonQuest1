package dq1.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Input class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Input implements KeyListener {
    
    private static Set<Integer> keyPressed = new HashSet<>();
    private static Set<Integer> keyPressedConsumed = new HashSet<>();
    private static KeyListener listener;

    public static void setListener(KeyListener listener) {
        Input.listener = listener;
    }

    public static synchronized boolean isKeyPressed(int keyCode) {
        return keyPressed.contains(keyCode);
    }

    public static synchronized boolean isKeyJustPressed(int keyCode) {
        if (!keyPressedConsumed.contains(keyCode) 
                && keyPressed.contains(keyCode)) {
            
            keyPressedConsumed.add(keyCode);
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized void keyTyped(KeyEvent e) {
        if (listener != null) {
            listener.keyTyped(e);
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        keyPressed.add(e.getKeyCode());
        if (listener != null) {
            listener.keyPressed(e);
        }
    }
    
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        keyPressed.remove(e.getKeyCode());
        keyPressedConsumed.remove(e.getKeyCode());
        if (listener != null) {
            listener.keyReleased(e);
        }
    }
    
}
