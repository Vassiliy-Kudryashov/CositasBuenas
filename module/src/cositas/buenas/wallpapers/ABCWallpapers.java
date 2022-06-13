package cositas.buenas.wallpapers;

import cositas.buenas.ui.DitheredGradientPaint;
import cositas.buenas.ui.SuperEllipse;
import cositas.buenas.util.FileUtil;
import cositas.buenas.util.ImageUtil;

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


public class ABCWallpapers {
    private static final Random R = new Random();
    public static final String ABC = createABC(new char[][]{{'0', '9'}, {'A', 'Z'}, {'a', 'z'}});
    static List<Font> ALL_FONTS = new ArrayList<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()));
    private static final List<String> STANDARD_FONT_NICKNAMES = Arrays.asList(Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED);
    private static int maxFontNameLength = 0;
    private static String[] WRAPPERS;
    static {
        ALL_FONTS.removeIf(font -> {
            for (int i = 0; i < ABC.length(); i++) {
                if (!font.canDisplay(ABC.charAt(i))) {
                    return true;
                }
            }
            return (STANDARD_FONT_NICKNAMES.contains(font.getName()));
        });
        for (Font font : ALL_FONTS) {
            maxFontNameLength = Math.max(maxFontNameLength, font.getFamily().length());
        }
        WRAPPERS = new String[maxFontNameLength];
        WRAPPERS[WRAPPERS.length - 1] = "";
        for (int i = WRAPPERS.length - 2; i >= 0; i--) {
            WRAPPERS[i] = " " + WRAPPERS[i+1];
        }
    }
    static String wrap(String s) {
        if (s.length() < WRAPPERS.length) return s + WRAPPERS[s.length()];
        return s;
    }

    private static final Color ABC_COLOR = new Color(0, 0, 0, 16);//6
    public static final int GAP_DELIMITER = 100;//100;
    public static final int OUTER_MARGIN_DELIMITER = 9;//100;

    private static boolean CLEAR_PREVIOUS_RESULTS = true;
    //Do we fill the area as much as possible with random chars use each character just once
    private static boolean ONE_OF_EACH = true;

    private static boolean ONE_OF_EACH_IN_LEVEL = true;

    private static final int MULTIRECT_SQUARE_SIZE = 5;//50;

    private static boolean JUST_ONE_PICTURE = false;

    private static boolean JUST_ONE_LETTER = false;

    private static boolean EMBED_FONT_NAME = true;

    private static int MAX_DELIMITER = 64;

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
        File pictures = FileUtil.mkDirs("Pictures/ABCWallpapers");
        if (CLEAR_PREVIOUS_RESULTS) {
            File[] files = pictures.listFiles(file -> file.isFile() && file.getName().matches("\\d+\\.(png|jpg)"));
            if (files != null) {
                //noinspection ResultOfMethodCallIgnored
                Arrays.stream(files).forEach(File::delete);
            }
        }
        int minHue = 18;//100 / 6;//0
        int maxHue = 50;//100 / 2;//100
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = 2 * device.getDefaultConfiguration().getBounds().width;
        int height = 2 * device.getDefaultConfiguration().getBounds().height;

        final float sat = 1.0f;
        final float brt = 0.5f;
        final float hueStep = 2f * (maxHue - minHue) / ALL_FONTS.size();//1.0
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
//        cache.clear();
        multiCache.clear();
        long start = System.currentTimeMillis();
        Color c1 = new Color(rgbs.get(i));
        Color c2 = new Color(rgbs.get((i + Math.max(1, rgbs.size() / 10)) % total));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new DitheredGradientPaint(0, height, c1, width, 0, c2));
//        graphics.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_QUALITY);
//        graphics.setRenderingHint(KEY_DITHERING, VALUE_DITHER_ENABLE);
//        graphics.setPaint(new GradientPaint(0, height, c1, width, 0, c2));
        graphics.fillRect(0, 0, width + 1, height + 1);
        addLetters(graphics, width, height, i);
//        graphics.setPaint(new GradientPaint(
//                0, 0, new Color(255, 255, 255, 15),
//                0, height, new Color(0, 0, 0, 15)));
//        graphics.fillRect(0, 0, width+1, height +1);
//        ImageUtil.writeJPEG(image, new File(pictures, String.format("%03d", (i + 1)) + ".jpg"), .95f);
        ImageIO.write(image, "png", new File(pictures, String.format("%03d", (i + 1)) + ".png"));
        System.out.println(wrap(getFont(10, i).getFamily()) +": " + counter.addAndGet(1) + "/" + total + " (#" + (i + 1) + ") in " + (System.currentTimeMillis() - start) + "ms");
    }

    private static void addLetters(Graphics2D graphics, int width, int height, int index) {
        int gap = Math.max(1, Math.min(width, height) / GAP_DELIMITER);
        //debug
//        graphics.setColor(Color.BLACK);
//        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 120));
//        graphics.drawString(getFont(20, index).getName(), 80, height * 7 / 8);


//        graphics.setColor(ABC_COLOR);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
        ArrayList<MultiRect> multiBusy = new ArrayList<>();
        ArrayList<String> dictionary = new ArrayList<>();
        for (int i = 0; i < ABC.length(); i++) dictionary.add(""+ABC.charAt(i));
        double delimiter = 2;//3;
        final Shape outerShape = createOuterShape(width, height);
//        Set<String> used = new HashSet<>();
        boolean messageProcessed = false;
        //outer ABC set. Alternative is inner set, new one for each size
        outer:
        do {
            Set<String> usedInLevel = new HashSet<>();
            graphics.setColor(ABC_COLOR);
//            graphics.setColor(new Color(0, 0, 0, (int)Math.max(4, 32.0 / delimiter)));
//            graphics.setColor(new Color(0, 0, 0, Math.max(4, (int)(64.0 / Math.log(delimiter+3)))));
            double size = height / delimiter;
            if (dictionary.size() == ABC.length() && EMBED_FONT_NAME && !messageProcessed) {
                size /= 8/*(MAX_DELIMITER/4)*/;
            }
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

                if (ONE_OF_EACH && dictionary.isEmpty()) break outer;
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.size() == ABC.length()) break inner;
                String s;
                s = EMBED_FONT_NAME && !messageProcessed ? graphics.getFont().getFamily() : getRandom(dictionary);
//                if (!randomFont.canDisplay(s.charAt(0))) break inner;
//                if (ONE_OF_EACH && used.contains(s)) continue;
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.contains(s)) continue;
//                Shape stringShape = getPreciseStringBounds(graphics, s);
//                if (stringShape == null) continue;

//                Rectangle bounds = stringShape.getBounds();

                int x = R.nextInt(width);
                int y = R.nextInt(height);
                if (GRID_POSITION_STEP > 1) {
                    x = (x / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                    y = (y / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                }
                MultiRect cachedMultiRect = getPreciseStringMultiRect(graphics, s);
                MultiRect translatedMultiRect = cachedMultiRect.translateAndAddGap(x, y, gap);// or gap/2 ??
//                Rectangle2D candidateArea = new Rectangle2D.Double(x + bounds.getX() - gap / 2d, y + bounds.getY() - gap / 2d, bounds.getWidth() + gap, bounds.getHeight() + gap);
//                Rectangle2D realSize = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphOutline(0).getBounds2D();
//                Shape shape = graphics.getFont().createGlyphVector(graphics.getFontRenderContext(), s).getGlyphLogicalBounds(0);


                if (!outerShape.contains(translatedMultiRect.myOuterRect)) continue inner;
                for (MultiRect multiUsed : multiBusy) {
                    if (translatedMultiRect.intersects(multiUsed)) continue inner;
                }
                multiBusy.add(translatedMultiRect);
                graphics.drawString(s, x, y);//debug
                //graphics.draw(new RoundRectangle2D.Double(candidateArea.getX(), candidateArea.getY(), candidateArea.getWidth(), candidateArea.getHeight(), gap, gap));
//                graphics.fill(candidateArea);
                graphics.setColor(ABC_COLOR);

//                for (Rectangle2D r : cachedMultiRect) {
//                    graphics.fill(r);
//                }
                if (s.length() == 1) {
//                    used.add(s);
                    if (ONE_OF_EACH) dictionary.remove(s);
                    usedInLevel.add(s);
                } else {
                    messageProcessed = true;
                    break inner;
                }
                if (JUST_ONE_LETTER) break outer;
            }
            if (!EMBED_FONT_NAME || dictionary.size() < ABC.length()) {
//                delimiter = delimiter * 1.6180339;
                delimiter *= 2;
            }
//            delimiter++;
        } while (delimiter < MAX_DELIMITER);//17
        //debug drawing
//        graphics.setColor(ABC_COLOR);
//        graphics.fill(outerShape);
    }

    private static Font getFont(double size, int index) {
//        if (index == 0) return new Font("BodoniSvtyTwoOSITCTT-Book", Font.PLAIN, size);
        return new Font(ALL_FONTS.get(index % ALL_FONTS.size()).getName(), Font.PLAIN, (int)size);
    }

    private static Shape createOuterShape(int width, int height) {
        int margin = Math.max(1, Math.min(width, height) / OUTER_MARGIN_DELIMITER);
        //return new Rectangle(0, 0, width, height);
        //return new Ellipse2D.Double(margin, margin, width - 2 * margin, height - 2 * margin);
        return new SuperEllipse(margin, margin, width - 2 * margin, height - 2 * margin, .7);
    }

//    private static final Map<Integer, Rectangle> cache = new HashMap<>();

//    private static Shape getPreciseStringBounds(Graphics2D g, String s) {
//        Rectangle bounds = g.getFontMetrics().getStringBounds(s, g).getBounds();
//        int code = (s + g.getFont().getSize()).hashCode();
////        if (bounds.width <=0 || bounds.height <=0) return null;
//        return cache.computeIfAbsent(code, __ -> {
//            BufferedImage image = new BufferedImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
//            Graphics2D graphics = image.createGraphics();
//            graphics.setColor(Color.RED);
//            graphics.setFont(g.getFont());
//            graphics.drawString(s, -bounds.x, -bounds.y);
//            int minX = Integer.MAX_VALUE;
//            int minY = Integer.MAX_VALUE;
//            int maxX = Integer.MIN_VALUE;
//            int maxY = Integer.MIN_VALUE;
//            for (int x = 0; x < image.getWidth(); x++) {
//                for (int y = 0; y < image.getHeight(); y++) {
//                    if (image.getRGB(x, y) != 0) {
//                        minX = Math.min(minX, x);
//                        minY = Math.min(minY, y);
//                        maxX = Math.max(maxX, x);
//                        maxY = Math.max(maxY, y);
//                    }
//                }
//            }
//            return new Rectangle(bounds.x + minX, bounds.y + minY, maxX - minX, maxY - minY);
//        });
//    }

    private static final Map<Integer, MultiRect> multiCache = new HashMap<>();


    private static MultiRect getPreciseStringMultiRect(Graphics2D g, String s) {
        Rectangle bounds = g.getFontMetrics().getStringBounds(s, g).getBounds();
        int code = s.hashCode() ^ g.getFont().hashCode();//s + g.getFont().getSize()).hashCode();

        MultiRect cached = multiCache.get(code);
        if (cached != null) return cached;

//        int margin = 3 * Math.max(bounds.width, bounds.height);//todo make it wider
        int margin = Math.min(bounds.width, bounds.height);//todo make it wider
        BufferedImage image = new BufferedImage(bounds.width + 2 * margin, bounds.height + 2 * margin, Transparency.TRANSLUCENT);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.RED);
        graphics.setFont(g.getFont());
        graphics.drawString(s, margin - bounds.x, margin - bounds.y);
        boolean[][] grid = new boolean[1 + image.getWidth() / MULTIRECT_SQUARE_SIZE][1 + image.getHeight() / MULTIRECT_SQUARE_SIZE];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (grid[x / MULTIRECT_SQUARE_SIZE][y / MULTIRECT_SQUARE_SIZE]) continue;
                if (image.getRGB(x, y) != 0) {
                    grid[x / MULTIRECT_SQUARE_SIZE][y / MULTIRECT_SQUARE_SIZE] = true;
                }
            }
        }

        List<Rectangle2D> list = new ArrayList<>();
        for (int x = 0; x < grid.length; x++) {
            boolean[] column = grid[x];
            int startY = -1;
            for (int y = 0; y < column.length; y++) {
                if (column[y] && startY == -1) startY = y;
                if (!column[y] && startY != -1) {
                    list.add(new Rectangle2D.Double(bounds.x - margin + x * MULTIRECT_SQUARE_SIZE,
                            bounds.y - margin + startY * MULTIRECT_SQUARE_SIZE,
                            MULTIRECT_SQUARE_SIZE,
                            (y - startY) * MULTIRECT_SQUARE_SIZE));
                    startY = -1;
                }
//                if (b)
//                    result.add(new Rectangle2D.Double(bounds.x -margin + x * MULTIRECT_SQUARE_SIZE, bounds.y -margin + y * MULTIRECT_SQUARE_SIZE, MULTIRECT_SQUARE_SIZE, MULTIRECT_SQUARE_SIZE));
            }
        }
        MultiRect result = new MultiRect(list);
        multiCache.put(code, result);
        return result;
    }

    private static String getRandomSymbol(String s) {
        return "" + s.charAt(R.nextInt(s.length()));
    }

    private static<T> T getRandom(List<T> list) {
        return list.get(R.nextInt(list.size()));
    }

    @SafeVarargs
    private static <T> T getRandom(T... elements) {
        return elements[R.nextInt(elements.length)];
    }

    private static class MultiRect {
        private final List<Rectangle2D> myList;
        private final Rectangle2D.Double myOuterRect;

        public MultiRect(List<Rectangle2D> list) {
            myList = list;
            double minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
            for (Rectangle2D r : list) {
                minX = Math.min(minX, r.getX());
                maxX = Math.max(maxX, r.getX() + r.getWidth());
                minY = Math.min(minY, r.getY());
                maxY = Math.max(maxY, r.getY() + r.getHeight());
            }
            myOuterRect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        }

        boolean intersects(MultiRect other) {
            if (myOuterRect.intersects(other.myOuterRect)) {
                for (Rectangle2D r1 : myList) {
                    for (Rectangle2D r2 : other.myList) {
                        if (r1.intersects(r2)) return true;
                    }
                }
            }
            return false;
        }

        MultiRect translateAndAddGap(double x, double y, double gap) {
            List<Rectangle2D> copy = new ArrayList<>();
            for (Rectangle2D r : myList) {
                copy.add(new Rectangle2D.Double(x + r.getX() - gap, y + r.getY() - gap,
                        r.getWidth() + 2 * gap, r.getHeight() + 2 * gap));
            }
            return new MultiRect(copy);
        }
    }
}
