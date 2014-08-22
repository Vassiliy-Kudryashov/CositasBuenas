import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by user on 18.08.14.
 */
public class Rainbow {
    public static void main(String[] args) throws IOException {
        File base = new File("/Users/user/Pictures/colors/");
        base.mkdirs();
        for (int hue = 0; hue < 100; hue++) {
            final int side = (int)(100 * getRandom(.03, .4));

            boolean b = Math.random()>.5;
            int w = b? 1440: 900;
            int h = b? 900: 1440;
            int size = (int)(Math.sqrt(w*w + h*h)) / 2;
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = image.getGraphics();
            for (int i = 0; i < w/side; i++) {
                for (int j = 0; j < h/side; j++) {
                    int x = i * side;
                    int y = j * side;
                    double distance = new Point(x, y).distance(w / 2, h / 2) / size;//0..1
                    int rgb = Color.HSBtoRGB(((float)hue)/100, getRandom(.66, 1 - distance * .33), getRandom(.5, 1 - distance * .5));
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
