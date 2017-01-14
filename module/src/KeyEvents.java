import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by user on 29.07.14.
 */
public class KeyEvents extends JFrame {
    public static void main(String[] args) {
        System.out.println("KeyEvents #" + System.currentTimeMillis() % 222);
        KeyEvents  keyEvents = new KeyEvents();
        final JLabel label = new JLabel();
        keyEvents.setContentPane(label );//////////////////////////////////////////////////////////////////////////////////
        keyEvents.getContentPane().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                label.setText(""+e.getKeyCode() + " " + e.getKeyChar());
            }
        });
        keyEvents.setSize(500, 400);
        keyEvents.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        keyEvents.setVisible(true);

        Integer test_property = Integer.getInteger("test_property");
//        List<String> test = new ArrayList<String>(test_property);
//        ImageIcon icon = new ImageIcon(KeyEvents.class.getResource("../test.png"));
        new Exception().printStackTrace();
    }





















































}
/**
 * getProperty("filterNewlines")
 sw
 sw
 s
 w
 s
 w
 sw
 s
 w
 */

