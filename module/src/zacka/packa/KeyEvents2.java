package zacka.packa;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by user on 06/07/15.
 */
public class KeyEvents2 extends JFrame {
    public static void main(String[] args) {
        KeyEvents2 keyEvents = new KeyEvents2();
        final JLabel label = new JLabel();
        keyEvents.setContentPane(label);//////////////////////////////////////////////////////////////////////////////////
        keyEvents.getContentPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                label.setText("" + e.getKeyCode() + " " + e.getKeyChar());
            }
        });
        keyEvents.setSize(500, 400);
        keyEvents.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        keyEvents.setVisible(true);
    }
}
