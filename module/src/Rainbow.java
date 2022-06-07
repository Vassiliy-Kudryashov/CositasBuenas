import cositas.buenas.util.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Cr eated by user on 18.08.14.
 */
public class Rainbow {

    public static void main(String[] args) throws IOException {
        File base = FileUtil.mkDirs("Pictures/colors/");
        for (int hue = 0; hue < 100; hue++) {
            final int side = (int)(100 * getRandom(.03, .4));

            boolean b = Math.random()>.5;
            int w = b? 1440: 900;
            int h = b? 900: 1440;
            double cx = Math.random() * w;
            double cy = Math.random() * h;
            Point2D c = new Point.Double(cx, cy);
            double size = c.distance(0, 0);
            size = Math.max(size, c.distance(0, w));
            size = Math.max(size, c.distance(h, w));
            size = Math.max(size, c.distance(h, 0));
//            int size = (int)(Math.sqrt(w*w + h*h)) / 2;
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.getGraphics();
            for ( int i = 0; i <= w/side; i++) {
                for (int j = 0; j <= h /side; j++) {
                    int x = i * side;
                    int y = j * side;
                     double distance = new Point(x, y).distance(cx, cy) / size;//0..1
                    int rgb = Color.HSBtoRGB(((float)hue)/100, getRandom(1 - distance / 4, 1), getRandom(1 - distance /3, 1));
                    graphics.setColor(new Color(rgb));
                    graphics.fillRect(x, y, side, side);
                    graphics.drawRect(x, y, side, side);
                }
            }




//            graphics.fillRect(0, 0, w+1, h+1);
            ImageIO.write(image, "png", new File(base, ""+hue + ".png"));
        }
    }
    private static float getRandom(double min, double max) {
        return Math.min(1, Math.max(0, (float) (min + Math.random() * (max - min))));
    }
}






















