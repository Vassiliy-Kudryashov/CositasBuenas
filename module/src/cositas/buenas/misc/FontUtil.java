package cositas.buenas.misc;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FontUtil {
    public static void main(String[] args) {
//        Arrays.stream(Toolkit.getDefaultToolkit().getFontList()).forEach(s -> System.err.println(s));
        AtomicInteger i = new AtomicInteger(0);
        for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            System.out.println(i.incrementAndGet() + ". " + font.getName() + "/" + font.getFamily() + "/" + font.getFontName());
        }
    }
//    public static String getUniqueFontName(Font font) {
//        String name = font.getName();
//        int pos = name.indexOf('-');
//        //Bold, Condensed, CondensedBold, CondensedLight, Light, Regular, BoldItalicMT
//        String suffix = "";
//        if (pos != -1) suffix = " " + name.substring(pos + 1);
//        if (suffix.equals(" Regular")) suffix = "";
//        if (suffix.equals(" OTF")) suffix = "";
//        suffix = suffix.replace('_', ' ');
//        if (suffix.length() > 0 && font.getName().endsWith(suffix)) suffix = "";
//        return font.getName() + suffix;
//    }
}
