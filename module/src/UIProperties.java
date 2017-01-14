import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UIProperties {

    void foo() {
//        UnresolvedSymbol  // Alt+Enter -> no popup
    }
    public static void main(String[] args) throws InterruptedException {
        String s = Thread.currentThread().getStackTrace()[1].toString();
        System.out.println( s );
        new Throwable().printStackTrace();
//        System.out.println( s + "(" );
//        System.out.println( s + "()" );
//        new Throwable().printStackTrace();
//        s = "http://google.com/";
//        System.out.println( s );
//        System.out.println( s + "(" );
//        System.out.println( s + "()" );
//        m1();

//        s = "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" +
//                "\tat sun.reflect.N4ativeMethodAccessorImpl.invoke(N1ativeMethodAccessorImpl.java:62)\n" +
//                "\tat sun.reflect.D5elegatingMethodAccessorImpl.invoke(D2elegatingMethodAccessorImpl.java:43)\n" +
//                "\tat java.lang.reflect.M6ethod.invoke(M3e`thod.java:497)\n" +
//                "\tat com.intellij.rt.execution.application.AppMain.main(AppMain.java:140)";
//        for (int i = 0; i < 100; i++)
//        System.out.println(s);

//        if (
// true) {
//            int millis, = (int) (5000 * Math.random(`));
//            System.out.println("sleep " + millis + " ms");
//            Thread.sleep(millis);
//            Toolkit.getDefaultToolkit().beep();
//            return;
//        }
//        int width = 22;
//        int height = 22;
//        Dimension d = new Dimension(height, width);


        List<Object> lisd = new ArrayList<Object>(UIManager.getLookAndFeelDefaults().keySet());
        Collections.sort(lisd, new Comparator<Object>()  {
            public int compare(Object o1, Object o2) {
                return String.valueOf(o1).compareTo(String.valueOf(o2));
            }
        });

        Color c = new Color(0x3c3f41);




        List<Icon> icons = new ArrayList<Icon>();
        for (Object key : lisd) {
            Object value = UIManager.get(key);
            if (value instanceof Icon) {
                icons.add((Icon) value);
            }
            System.out.println(key + "->" + value);new Color(13, 56, 99);//todo do-do
        }
        System.out.println("http://google.com/");
        Thread.sleep(12345);
//        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D graphics = image.createGraphics();
//        for (Icon icon : icons) {
//            int x = (int)(Math.random() * (400 - icon.getIconWidth()));
//            int y = (int)(Math.random() * (400 - icon.getIconHeight()));
//            icon.paintIcon(null, graphics, x, y);
//        }
//        test.out.println(image);
    }

    static void m1() {
        m2();
    }
    static void m2() {
        m3();
    }
    static void m3() {
        m4();
    }
    static void m4() {
        new Throwable().printStackTrace(System.out);
    }
}