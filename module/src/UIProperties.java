import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UIProperties {


    public static void main(String[] args) {
        List<Object> lisd = new ArrayList<Object>(UIManager.getLookAndFeelDefaults().keySet());
        Collections.sort(lisd, new Comparator<Object>()  {
            public int compare(Object o1, Object o2) {
                return   String.valueOf(o1).compareTo(String.valueOf(o2));
            }
        });




        List<Icon> icons = new ArrayList<Icon>();
        for (Object key : lisd) {
            Object value = UIManager.get(key);
            if (value instanceof Icon) {
                icons.add((Icon) value);
            }
            System.out.println(key + "->" + value);new Color(13, 56, 99);//todo do-do
        }
        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        for (Icon icon : icons) {
            int x = (int)(Math.random() * (400 - icon.getIconWidth()));
            int y = (int)(Math.random() * (400 - icon.getIconHeight()));
            icon.paintIcon(null, graphics, x, y);
        }
        System.out.println(image);
    }
}