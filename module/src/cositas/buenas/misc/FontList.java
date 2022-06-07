package cositas.buenas.misc;

import java.awt.*;
import java.util.Arrays;

public class FontList {
    public static void main(String[] args) {
//        Arrays.stream(Toolkit.getDefaultToolkit().getFontList()).forEach(s -> System.out.println(s));
        Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .forEach(System.out::println);
    }
}
