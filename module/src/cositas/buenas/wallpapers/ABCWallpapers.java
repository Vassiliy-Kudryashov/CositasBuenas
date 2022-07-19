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
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static java.awt.RenderingHints.*;


public class ABCWallpapers {
    //TODO: Refactor config as .properties + defaults-aware helper class
    //TODO: Parse font from https://fonts.google.com/ without installation,
    // GraphicsEnvironment.registerFont(
    //                Font.createFont(Font.TRUETYPE_FONT, {stream from URL}))
    // maybe use https://github.com/anupthegit/WOFFToTTFJava
    //TODO convert results into a web game where clicks in proper ABC order solves the puzzle
    private static final Random R = new Random();

    private static final Format FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final String ABC = createABC(new char[][]{{'0', '9'}, {'A', 'Z'}, {'a', 'z'}});
    static List<Font> ALL_FONTS = new ArrayList<>(Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()));
    private static final List<String> STANDARD_FONT_NICKNAMES = Arrays.asList(Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED);
    private static int maxFontNameLength = 0;
    private static final String[] WRAPPING_SPACES;

    static {
        ALL_FONTS.removeIf(font -> {
            for (int i = 0; i < ABC.length(); i++) {
                if (!font.canDisplay(ABC.charAt(i))) {
                    return true;
                }
            }
            return (STANDARD_FONT_NICKNAMES.contains(font.getFamily()) && font.getName().contains(font.getFamily()));
        });
        for (Font font : ALL_FONTS) {
            maxFontNameLength = Math.max(maxFontNameLength, font.getFamily().length());
        }
        WRAPPING_SPACES = new String[maxFontNameLength];
        WRAPPING_SPACES[WRAPPING_SPACES.length - 1] = "";
        for (int i = WRAPPING_SPACES.length - 2; i >= 0; i--) {
            WRAPPING_SPACES[i] = " " + WRAPPING_SPACES[i + 1];
        }
    }

    static String wrap(String s) {
        if (s.length() < WRAPPING_SPACES.length) return s + WRAPPING_SPACES[s.length()];
        return s;
    }

    private static final Color ABC_COLOR = new Color(0, 0, 0, 16);//6
    public static final double GAP_SCREEN_RATIO = .005;
    public static final double LOCAL_GAP_RATIO = .05;
    public static final boolean USE_LOCAL_GAP_RATIO = false;
    public static final int OUTER_MARGIN_DELIMITER = 9;//100;

    private static boolean CLEAR_PREVIOUS_RESULTS = false;

    private static boolean SKIP_EXISTING_FILES = false;

    private static boolean JUST_ONE_LETTER = false;

    //Do we fill the area as much as possible with random chars use each character just once
    private static boolean ONE_OF_EACH = true;

    private static boolean ONE_OF_EACH_IN_LEVEL = true;

    private static final int MULTIRECT_SQUARE_SIZE = 3;//50;

    private static boolean JUST_ONE_PICTURE = false;

    private static boolean EMBED_FONT_NAME = true;

    private static double INITIAL_FONT_RATIO = .5;
    private static double MIN_FONT_RATIO = 1d / 256;

    static final int GRID_POSITION_STEP = 1;//100;

    static final boolean DEBUG = true;

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
        int width = /*2 **/ device.getDefaultConfiguration().getBounds().width;
        int height = /*2 * */device.getDefaultConfiguration().getBounds().height;

        final float sat = 1.0f;
        final float brt = 0.5f;
        final float hueStep = 2f * (maxHue - minHue) / ALL_FONTS.size();//1.0
        List<Integer> rgbs = new ArrayList<>();
        for (float hue = minHue; hue < maxHue; hue += hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));
        for (float hue = maxHue; hue >= minHue; hue -= hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));

        int total = rgbs.size();
        while (total % 3 == 0 && total % 2 == 0) total--;//366 -> 365

        for (int i = 0; i < total; i++) {
            generateImage(pictures, width, height, rgbs, total, i);
            if (JUST_ONE_PICTURE)
                break;
        }
        Toolkit.getDefaultToolkit().beep();
    }

    private static void generateImage(File pictures, int width, int height, List<Integer> rgbs, int total, int i) throws IOException {
//        cache.clear();
        File output = new File(pictures, String.format("%03d", (i + 1)) + ".png");
        if (output.isFile() && SKIP_EXISTING_FILES) return;
        multiCache.clear();
        long start = System.currentTimeMillis();
        Color c1 = new Color(rgbs.get(i));
        Color c2 = new Color(rgbs.get((i + Math.max(1, rgbs.size() / 10)) % total));
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new DitheredGradientPaint(0, height, c1, width, 0, c2));
        graphics.fillRect(0, 0, width + 1, height + 1);
        addLetters(graphics, width, height, i);
//        graphics.setPaint(new GradientPaint(
//                0, 0, new Color(255, 255, 255, 15),
//                0, height, new Color(0, 0, 0, 15)));
//        graphics.fillRect(0, 0, width+1, height +1);
//        ImageUtil.writeJPEG(image, new File(pictures, String.format("%03d", (i + 1)) + ".jpg"), 1.0f);
        ImageIO.write(image, "png", output);
        System.out.println(wrap(getFont(10, i).getName()) + ": " + (i + 1) + "/" + total
                + " in " + (System.currentTimeMillis() - start) + "ms " + FORMAT.format(System.currentTimeMillis()));
    }

    private static void addLetters(Graphics2D graphics, int width, int height, int index) {
        double screenGap = Math.max(1, Math.min(width, height) * GAP_SCREEN_RATIO);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
        ArrayList<MultiRect> multiBusy = new ArrayList<>();
        ArrayList<String> dictionary = new ArrayList<>();
        //todo: Maybe sort letters in 'emptiness' order, Where J or D is more 'interesting' than 1 of I
        long lastTime = System.currentTimeMillis();
        for (int i = 0; i < ABC.length(); i++) dictionary.add("" + ABC.charAt(i));
        double fontRatio = INITIAL_FONT_RATIO;//3;
        final Shape outerShape = createOuterShape(width, height);
        boolean messageProcessed = false;
        //outer ABC set. Alternative is inner set, new one for each size
        outer:
        do {
            Set<String> usedInLevel = new HashSet<>();
            graphics.setColor(fontRatio > Math.sqrt(INITIAL_FONT_RATIO * MIN_FONT_RATIO) ? ABC_COLOR : new Color(0, 0, 0, 10));
            double size = height * fontRatio;
            if (dictionary.size() == ABC.length() && EMBED_FONT_NAME && !messageProcessed) {
                size /= 10;
            }
            graphics.setFont(getFont(size, index));
            inner:
            for (int i = 0; i < 1000000; i++) {
                if (System.currentTimeMillis() - lastTime > 10000) {
                    if (DEBUG) {
                    System.err.println("Emergency downscale for '" + graphics.getFont().getName() + "', size=" + size +
                            ", dictionary=" + dictionary.size()+" of " + ABC.length());
                    }
                    fontRatio = getSmaller(fontRatio);
                    lastTime = System.currentTimeMillis();
                    break;
                }
//                Font randomFont = getRandom(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts());
//                graphics.setFont(randomFont.deriveFont(/*Font.BOLD, */size));

                if (ONE_OF_EACH && dictionary.isEmpty()) break outer;
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.size() == ABC.length()) break;
                String s = EMBED_FONT_NAME && !messageProcessed ? graphics.getFont().getName() : getRandom(dictionary);
                if (ONE_OF_EACH_IN_LEVEL && usedInLevel.contains(s)) continue;

                int x = R.nextInt(width);
                int y = R.nextInt(height);
                if (GRID_POSITION_STEP > 1) {
                    x = (x / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                    y = (y / GRID_POSITION_STEP) * GRID_POSITION_STEP;
                }
                MultiRect cachedMultiRect = getPreciseStringMultiRect(graphics, s);
                double localGap = Math.min(cachedMultiRect.myOuterRect.width, cachedMultiRect.myOuterRect.height)  * LOCAL_GAP_RATIO;
                MultiRect translatedMultiRect = cachedMultiRect.translateAndAddGap(x, y, USE_LOCAL_GAP_RATIO ? localGap : screenGap);// or gap/2 ??

                if (!outerShape.contains(translatedMultiRect.myOuterRect)) continue;
                for (MultiRect multiUsed : multiBusy) {
                    if (translatedMultiRect.intersects(multiUsed)) continue inner;
                }
                multiBusy.add(translatedMultiRect);
                graphics.drawString(s, x, y);//debug
                if (DEBUG) {
                    System.err.print(s);
                }
                lastTime = System.currentTimeMillis();
                //graphics.draw(new RoundRectangle2D.Double(candidateArea.getX(), candidateArea.getY(), candidateArea.getWidth(), candidateArea.getHeight(), gap, gap));
//                graphics.fill(candidateArea);
//                graphics.setColor(fontRatio > Math.sqrt(INITIAL_FONT_RATIO * MIN_FONT_RATIO) ? ABC_COLOR : new Color(0,0, 0, 10));

//                for (Rectangle2D r : cachedMultiRect) {
//                    graphics.fill(r);
//                }
                if (s.length() == 1) {
//                    used.add(s);
                    dictionary.remove(s);
                    if (!ONE_OF_EACH && dictionary.isEmpty()) {
                        for (int j = 0; j < ABC.length(); j++) dictionary.add("" + ABC.charAt(j));
                    }
                    usedInLevel.add(s);
                } else {
                    messageProcessed = true;
                    break;
                }
                if (JUST_ONE_LETTER) break outer;
            }
            if (!EMBED_FONT_NAME || dictionary.size() < ABC.length()) {
//                delimiter = delimiter * 1.6180339;
                fontRatio = getSmaller(fontRatio);
//                fontRatio *=Math.sqrt(.5);
//                fontRatio *= 0.99;
            }
//            delimiter++;
        } while (fontRatio >= MIN_FONT_RATIO);//17
        //debug drawing
//        graphics.setColor(ABC_COLOR);
//        graphics.fill(outerShape);
    }
    private static double getSmaller(double d) {
//        return d * .99;
        return d * (Math.sqrt(5) - 1) / 2;//0.6180339
    }

    private static Font getFont(double size, int index) {
//        if (index == 0) return new Font("BodoniSvtyTwoOSITCTT-Book", Font.PLAIN, size);
        String name = ALL_FONTS.get(index % ALL_FONTS.size()).getName();
        int style = Font.PLAIN;
        if (name.contains("Italic")) style = Font.ITALIC;
        return new Font(name, style, (int) size);
    }

    private static Shape createOuterShape(int width, int height) {
        int margin = Math.max(1, Math.min(width, height) / OUTER_MARGIN_DELIMITER);
        //return new Rectangle(0, 0, width, height);
        //return new Ellipse2D.Double(margin, margin, width - 2 * margin, height - 2 * margin);
        return new SuperEllipse(margin, margin, width - 2 * margin, height - 2 * margin, .7);
    }

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

    private static <T> T getRandom(List<T> list) {
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
