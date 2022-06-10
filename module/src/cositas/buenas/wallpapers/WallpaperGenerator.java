package cositas.buenas.wallpapers;

import cositas.buenas.ui.DitheredGradientPaint;
import cositas.buenas.ui.SuperEllipse;
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
    public static final String ABC = createABC(new char[][]{{'0', '9'}, {'A', 'Z'}, {'a', 'z'}});
    static List<Font> ALL_FONTS = new ArrayList<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()));

    static {
        ALL_FONTS.removeIf(font -> {
            for (int i = 0; i < ABC.length(); i++) {
                if (!font.canDisplay(ABC.charAt(i))) {
                    return true;
                }
            }
            return false;
        });
    }
    private static final Color ABC_COLOR = new Color(0, 0, 0, 16);//6
    public static final int GAP_DELIMITER = 1000;//100;
    public static final int OUTER_MARGIN_DELIMITER = 9;//100;

    private static double SPREAD = Math.PI / 12;

    //Do we fill the area as much as possible with random chars use each character just once
    private static boolean ONE_OF_EACH = false;

    private static boolean ONE_OF_EACH_IN_LEVEL = true;
    //Do we consider letter shape as rectangle or a more complex shape with possible inner holes etc..
    private static boolean MULTIRECT_LETTER_SHAPE = true;

    private static final int MULTIRECT_SQUARE_SIZE = 50;
    private static boolean JUST_ONE_PICTURE = false;

    private static boolean JUST_ONE_LETTER = false;

    private static int MAX_DELIMITER = 125;
    static final int GRID_POSITION_STEP = 1;//100;


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

        final float sat = 1.0f;
        final float brt = 0.5f;
        final float hueStep = 0.1f;//1.0
        List<Integer> rgbs = new ArrayList<>();
        for (float hue = minHue; hue < maxHue; hue += hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));
        for (float hue = maxHue; hue >= minHue; hue -= hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));

        final AtomicInteger counter = new AtomicInteger();
        final int total = rgbs.size();

        for (int i = 0; i < total; i++) {
            generateImage(pictures, width, height, rgbs, counter, total, i);
            if (JUST_ONE_PICTURE)
                break;
        }
        Toolkit.getDefaultToolkit().beep();
    }

    private static void generateImage(File pictures, int width, int height, List<Integer> rgbs, AtomicInteger counter, int total, int i) throws IOException {
        Color c1 = new Color(rgbs.get(i));
        Color c2 = new Color(rgbs.get((i + 5) % total));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new DitheredGradientPaint(0, height, c1, width, 0, c2));
//        graphics.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
//        graphics.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
//        graphics.setPaint(new GradientPaint(0, height, c1, width, 0, c2));
        graphics.fillRect(0, 0, width + 1, height + 1);
        addNoise(graphics, width, height, i);
        ImageIO.write(image, "png", new File(pictures, String.format("%03d", (i + 1)) + ".png"));
        System.out.println(counter.addAndGet(1) + "/" + total + " (#" + (i + 1) + ")");
    }

    private static void addNoise(Graphics2D graphics, int width, int height, int index) {
        int gap = Math.max(1, Math.min(width, height) / GAP_DELIMITER);
        //debug
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 120));
        graphics.drawString(getFont(20, index).getName(), 80, height * 7 / 8);


//        graphics.setColor(ABC_COLOR);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
        ArrayList<Shape> busy = new ArrayList<>();
        ArrayList<List<Rectangle2D>> multiBusy = new ArrayList<>();
        int delimiter = 2;//3;
        final Shape outerShape = createOuterShape(width, height);
        Set<String> used = new HashSet<>();//outer ABC set. Alternative is inner set, new one for each size
        outer:
        do {
            Set<String> usedInLevel = new HashSet<>();
            graphics.setColor(ABC_COLOR);
//            graphics.setColor(new Color(0, 0, 0, (int)Math.max(4, 32.0 / delimiter)));
//            graphics.setColor(new Color(0, 0, 0, Math.max(4, (int)(64.0 / Math.log(delimiter+3)))));
            int size = height / delimiter;
//            graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
            graphics.setFont(getFont(size, index));
//            graphics.setFont(new Font(Font.DIALOG, Font.PLAIN, size));
//            graphics.setFont(new Font(getRandom(Font.SANS_SERIF/*, Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF*/), Font.BOLD, size));
//            graphics.setFont(new Font("Blackadder ITC", Font.PLAIN, size));
//            Set<String> used = new HashSet<>();
            inner:
            for (int i = 0; i < 10000; i++) {
//                Font randomFont = getRandom(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
//                graphics.setFont(randomFont.deriveFont(/*Font.BOLD, */size));

                if (ONE_OF_EACH && used.size() == ABC.length()) break outer;
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.size() == ABC.length()) break inner;
                String s;
                s = getRandomSymbol(ABC);
//                if (!randomFont.canDisplay(s.charAt(0))) break inner;
                if (ONE_OF_EACH && used.contains(s)) continue;
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.contains(s)) continue;
                Shape stringShape = getPreciseStringShape(graphics, s);
                if (stringShape == null) continue;

                Rectangle bounds = stringShape.getBounds();

                int x = R.nextInt(width);
                int y = R.nextInt(height);
                if (GRID_POSITION_STEP > 1) {
                    x = (x / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                    y = (y / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                }
                List<Rectangle2D> cachedMultiRect = MULTIRECT_LETTER_SHAPE ? getPreciseStringMultiRect(graphics, s) : new ArrayList<>();
                List<Rectangle2D> translatedMultiRect = new ArrayList<>();
                for (Rectangle2D rectangle2D : cachedMultiRect) {
                    translatedMultiRect.add(new Rectangle2D.Double(x + rectangle2D.getX() - gap / 2d, y + rectangle2D.getY() - gap / 2d, rectangle2D.getWidth() + gap, rectangle2D.getHeight() + gap));
                }
                Rectangle2D candidateArea = new Rectangle2D.Double(x + bounds.getX() - gap / 2d, y + bounds.getY() - gap / 2d, bounds.getWidth() + gap, bounds.getHeight() + gap);
//                Rectangle2D realSize = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphOutline(0).getBounds2D();
//                Shape shape = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphLogicalBounds(0);


                if (!MULTIRECT_LETTER_SHAPE) {
                    if (!outerShape.contains(candidateArea)) continue inner;
                    for (Shape r : busy) {
                        if (r.intersects(candidateArea)) continue inner;
                    }
                    busy.add(candidateArea);
                } else {
                    for (Rectangle2D r2d : translatedMultiRect) {
                        if (!outerShape.contains(r2d)) continue inner;
                    }
                    for (List<Rectangle2D> multiUsed : multiBusy) {
                        for (Rectangle2D mu : multiUsed) {
                            for (Rectangle2D r2d : translatedMultiRect) {
                                if (mu.intersects(r2d)) continue inner;
                            }
                        }
                    }
                    multiBusy.add(translatedMultiRect);
                }

                used.add(s);
                usedInLevel.add(s);
                graphics.drawString(s, x, y);//debug
                //graphics.draw(new RoundRectangle2D.Double(candidateArea.getX(), candidateArea.getY(), candidateArea.getWidth(), candidateArea.getHeight(), gap, gap));
//                graphics.fill(candidateArea);
                graphics.setColor(ABC_COLOR);

//                for (Rectangle2D r : cachedMultiRect) {
//                    graphics.fill(r);
//                }
                if (JUST_ONE_LETTER) break outer;
            }
            delimiter *= 2;
//            delimiter++;
        } while (delimiter < MAX_DELIMITER);//17
        //debug drawing
//        graphics.setColor(ABC_COLOR);
//        graphics.fill(outerShape);
    }
    private static Font getFont(int size, int index) {
        return new Font(ALL_FONTS.get(index % ALL_FONTS.size()).getName(), Font.PLAIN, size);
    }

    private static Shape createOuterShape(int width, int height) {
        int margin = Math.max(1, Math.min(width, height) /  OUTER_MARGIN_DELIMITER);
        //return new Rectangle(0, 0, width, height);
        //return new Ellipse2D.Double(margin, margin, width - 2 * margin, height - 2 * margin);
        return new SuperEllipse(margin, margin, width - 2 * margin, height - 2 * margin, .7);
    }

    private static final Map<Integer, Shape> cache = new HashMap<>();

    // TODO: get it as multi-rectangular list (not just Area or Shape to be able to check precise intersection)
    // TODO Details: Let's split resulting rectangle into 50x50 table and check every piece if it's actually 'busy' or not.
    private static Shape getPreciseStringShape(Graphics2D g, String s) {
        Rectangle bounds = g.getFontMetrics().getStringBounds(s, g).getBounds();
        int code = (s + g.getFont().getSize()).hashCode();
//        if (bounds.width <=0 || bounds.height <=0) return null;
        Shape cached = cache.get(code);
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

    private static final Map<Integer, List<Rectangle2D>> multiCache = new HashMap<>();


    private static List<Rectangle2D> getPreciseStringMultiRect(Graphics2D g, String s) {
        Rectangle bounds = g.getFontMetrics().getStringBounds(s, g).getBounds();
        int code = s.hashCode() ^ g.getFont().hashCode();//s + g.getFont().getSize()).hashCode();
        List<Rectangle2D> cached = multiCache.get(code);
        if (cached != null) return cached;
        int margin = Math.min(bounds.width, bounds.height);
        BufferedImage image = new BufferedImage(bounds.width  + 2 * margin , bounds.height + 2 * margin, Transparency.TRANSLUCENT);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.setFont(g.getFont());
        graphics.drawString(s, margin-bounds.x, margin-bounds.y);
        boolean[][] grid = new boolean[1 + image.getWidth() / MULTIRECT_SQUARE_SIZE][1 + image.getHeight() / MULTIRECT_SQUARE_SIZE];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) != 0) {
                    grid[x / MULTIRECT_SQUARE_SIZE][y / MULTIRECT_SQUARE_SIZE] = true;
                }
            }
        }

        List<Rectangle2D> result = new ArrayList<>();
        for (int x = 0; x < grid.length; x++) {
            boolean[] column = grid[x];
            for (int y = 0; y < column.length; y++) {
                boolean b = column[y];
                if (b)
                    result.add(new Rectangle2D.Double(bounds.x -margin + x * MULTIRECT_SQUARE_SIZE, bounds.y -margin + y * MULTIRECT_SQUARE_SIZE, MULTIRECT_SQUARE_SIZE, MULTIRECT_SQUARE_SIZE));
            }
        }
        multiCache.put(code, result);
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
