package main;

import dq1.core.Game;
import dq1.core.Resource;
import static dq1.core.Settings.*;
import dq1.core.View;
import java.awt.Color;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main class.
 * 
 * Game entry point.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                View.getCanvas().setBackground(Color.BLACK);
                View.getCanvas().setPreferredSize(
                            new Dimension(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
                
                JFrame frame = new JFrame();
                frame.setIconImage(Resource.getImage("dq1"));
                frame.setTitle("Dragon Quest 1 / Dragon Warrior 1");
                frame.getContentPane().add(View.getCanvas());
                frame.setResizable(false);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);

                View.start();
                View.getCanvas().requestFocus();
                Game.start();
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        });
        
    }   
 
}
