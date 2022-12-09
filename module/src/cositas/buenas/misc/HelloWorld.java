package cositas.buenas.misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class HelloWorld {
    public HelloWorld() {
        JFrame frame = new JFrame("Hello World");
        JLabel label = new JLabel("Click me");
        label.setFont(new Font("Serif", Font.PLAIN, 42));
        label.setHorizontalAlignment(JLabel.CENTER);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isAltDown()) {
                    System.err.println("Error!");
                } else {
                    System.out.println(e);
                }
            }
        });
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
