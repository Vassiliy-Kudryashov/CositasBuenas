package cositas.buenas.wallpapers;

import cositas.buenas.ui.DitheredGradientPaint;
import cositas.buenas.util.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.awt.RenderingHints.*;


public class WallpaperGenerator {
    private static final Random R = new Random();
    //    private static final String ABC = createABC(new char[][]{{'0', '9'}, {'A', 'Z'}, {'a', 'z'}});
    private static final String ABC = createABC(new char[][]{/*{'0', '9'},*/ {'A', 'Z'}/*, {'a', 'z'}*/});
    private static final Color ABC_COLOR = new Color(0, 0, 0, 16);//6

    private static double SPREAD = Math.PI / 12;

    private static String createABC(char[][] ranges) {
        StringBuilder sb = new StringBuilder();
        for (char[] range : ranges) {
            for (char ch = range[0]; ch <= range[1]; ch++) sb.append(ch);
        }
        System.out.println("ABC: " + sb);
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        File pictures = FileUtil.mkDirs("Pictures");
        File[] files = pictures.listFiles(file -> file.isFile() && file.getName().matches("\\d+\\.png"));
        if (files != null) {
            //noinspection ResultOfMethodCallIgnored
            Arrays.stream(files).forEach(File::delete);
        }
        int minHue = 18;//100 / 6;//0
        int maxHue = 50;//100 / 2;//100
        int width = 4 * GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().width;
        int height = 4 * GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().height;
        int gap = Math.min(width, height) / 100;
        final float sat = 1.0f;
        final float brt = 0.5f;
        final float hueStep = 1.0f;
        List<Integer> rgbs = new ArrayList<>();
        for (float hue = minHue; hue < maxHue; hue += hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));
        for (float hue = maxHue; hue >= minHue; hue -= hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));

        final AtomicInteger counter = new AtomicInteger();
        final int total = rgbs.size();

        for (int i = 0; i < total; i++) {
            generateImage(pictures, width, height, gap, rgbs, counter, total, i);
        }
        Toolkit.getDefaultToolkit().beep();
    }

    private static void generateImage(File pictures, int width, int height, int gap, List<Integer> rgbs, AtomicInteger counter, int total, int i) throws IOException {
        Color c1 = new Color(rgbs.get(i));
        Color c2 = new Color(rgbs.get((i + 5) % total));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new DitheredGradientPaint(0, height, c1, width, 0, c2));
//        graphics.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
//        graphics.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
//        graphics.setPaint(new GradientPaint(0, height, c1, width, 0, c2));
        graphics.fillRect(0, 0, width + 1, height + 1);
        addNoise(graphics, width, height, gap);
        ImageIO.write(image, "png", new File(pictures, String.format("%03d", (i + 1)) + ".png"));
        System.out.println(counter.addAndGet(1) + "/" + total + " (#" + (i + 1) + ")");
    }

    static final int gridStep = 100;

    private static void addNoise(Graphics2D graphics, int width, int height, int gap) {
//        graphics.setColor(ABC_COLOR);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
        ArrayList<Shape> busy = new ArrayList<>();
        int delimiter = 2;//3;
        Rectangle screen = new Rectangle(0, 0, width, height);
        Set<String> used = new HashSet<>();//outer ABC set. Alternative is inner set, new one for each size
        outer:
        do {
            graphics.setColor(ABC_COLOR);
//            graphics.setColor(new Color(0, 0, 0, (int)Math.max(4, 32.0 / delimiter)));
//            graphics.setColor(new Color(0, 0, 0, Math.max(4, (int)(64.0 / Math.log(delimiter+3)))));
            int size = height / delimiter;
            graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
//            graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
//            graphics.setFont(new Font(getRandom(Font.SANS_SERIF/*, Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF*/), Font.BOLD, size));
//            graphics.setFont(new Font("Blackadder ITC", Font.PLAIN, size));
//            Set<String> used = new HashSet<>();
            inner:
            for (int i = 0; i < 10000; i++) {
                if (used.size() == ABC.length()) break outer;
                String s;
                s = getRandomSymbol(ABC);
                if (used.contains(s)) continue;
                Rectangle bounds = getPreciseStringBounds(graphics, s);

                int x = R.nextInt(width);
                int y = R.nextInt(height);
                if (gridStep > 1) {
                    x = (x / gridStep) * gridStep;
                    y = (y / gridStep) * gridStep;
                }
                Rectangle2D candidateArea = new Rectangle2D.Double(x + bounds.getX() - gap / 2d, y + bounds.getY() - gap / 2d, bounds.getWidth() + gap, bounds.getHeight() + gap);
//                Rectangle2D realSize = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphOutline(0).getBounds2D();
//                Shape shape = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphLogicalBounds(0);

                if (!screen.contains(candidateArea)) continue inner;
                for (Shape r : busy) {
                    if (r.intersects(candidateArea)) continue inner;
                }
                busy.add(candidateArea);
                used.add(s);
                graphics.drawString(s, x, y);
//                graphics.draw(new RoundRectangle2D.Double(candidateArea.getX(), candidateArea.getY(), candidateArea.getWidth(), candidateArea.getHeight(), gap, gap));
//                graphics.fill(candidateArea);
            }
            delimiter *= 2;
//            delimiter++;
        } while (delimiter < 17);//17
    }

    private static Map<Integer, Rectangle> cache = new HashMap<>();

    // TODO: get it as multi-rectangular list (not just Area or Shape to be able to check precise intersection)
    // TODO Details: Let's split resulting rectangle into 50x50 table and check every piece if it's actually 'busy' or not.
    private static Rectangle getPreciseStringBounds(Graphics2D g, String s) {
        Rectangle bounds = g.getFontMetrics().getStringBounds(s, g).getBounds();
        int code = (s + g.getFont().getSize()).hashCode();
        Rectangle cached = cache.get(code);
        if (cached != null) return cached;
        BufferedImage image = new BufferedImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.setFont(g.getFont());
        graphics.drawString(s, -bounds.x, -bounds.y);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) != 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        Rectangle result = new Rectangle(bounds.x + minX, bounds.y + minY, maxX - minX, maxY - minY);
        cache.put(code, result);
        return result;
    }

    private static String getRandomSymbol(String s) {
        return "" + s.charAt(R.nextInt(s.length()));
    }

    @SafeVarargs
    private static <T> T getRandom(T... elements) {
        return elements[R.nextInt(elements.length)];
    }
}
