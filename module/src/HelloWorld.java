import javax.swing.*;
import java.awt.*;

public class HelloWorld {
    public HelloWorld() {
        JFrame frame = new JFrame("Hello world");
        JLabel label = new JLabel();
        label.setFont(new Font("Serif", Font.PLAIN, 32));
        label.setText("Hello World!");
        frame.getContentPane().add(label);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new HelloWorld();
    }
}
