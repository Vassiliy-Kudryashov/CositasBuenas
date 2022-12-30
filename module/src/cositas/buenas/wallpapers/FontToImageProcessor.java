package cositas.buenas.wallpapers;

import cositas.buenas.ui.DitheredGradientPaint;
import cositas.buenas.ui.SuperEllipse;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;

import static java.awt.RenderingHints.*;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_GASP;
import static cositas.buenas.wallpapers.ABCConfig.*;

public class FontToImageProcessor implements Callable<BufferedImage> {
    private static final Random R = new Random();

    private final Font font;
    private final int width;
    private final int height;
    private final Color c1;
    private final Color c2;
    private final Map<Integer, MultiRect> multiCache = new HashMap<>();

    public FontToImageProcessor(Font font, int width, int height, Color c1, Color c2) {
        this.font = font;
        this.width = width;
        this.height = height;
        this.c1 = c1;
        this.c2 = c2;
    }

    @Override
    public BufferedImage call() throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(new DitheredGradientPaint(0, height, c1, width, 0, c2));
        graphics.fillRect(0, 0, width + 1, height + 1);
        Map<String, MultiRect> multiRectMap = addLetters(graphics, width, height);
        return image;
    }

    public Map<String, MultiRect> addLetters(Graphics2D graphics, int width, int height) {
        double screenGap = Math.max(1, Math.min(width, height) * GAP_SCREEN_RATIO);
        graphics.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_GASP);
        Map<String, MultiRect> multiBusy = new LinkedHashMap<>();
        ArrayList<String> dictionary = new ArrayList<>();
        //todo: Maybe sort letters in 'emptiness' order, Where J or D is more 'interesting' than 1 or I
        long lastTime = System.currentTimeMillis();
        for (int i = 0; i < ABC.length(); i++) dictionary.add("" + ABC.charAt(i));
        double fontRatio = INITIAL_FONT_RATIO;//3;
        final FormProvider formProvider = createFormProvider(width, height);
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
            graphics.setFont(font.deriveFont(font.getName().contains("Italic") ? Font.ITALIC : Font.PLAIN, (float)size));
            inner:
            for (int i = 0; i < 100000; i++) {//1000000
                if (System.currentTimeMillis() - lastTime > 10000) {
                    if (DEBUG) {
                        System.err.println("Emergency downscale for '" + graphics.getFont().getName() + "', size=" + size +
                                ", dictionary=" + dictionary.size() + " of " + ABC.length());
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
                double localGap = Math.min(cachedMultiRect.myOuterRect.width, cachedMultiRect.myOuterRect.height) * LOCAL_GAP_RATIO;
                MultiRect translatedMultiRect = cachedMultiRect.translateAndAddGap(x, y, USE_LOCAL_GAP_RATIO ? localGap : screenGap);// or gap/2 ??

                if (!formProvider.contains(translatedMultiRect.myOuterRect)) continue;
                for (MultiRect multiUsed : multiBusy.values()) {
                    if (translatedMultiRect.intersects(multiUsed)) continue inner;
                }
                multiBusy.put(s, translatedMultiRect);
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
//        graphics.fill(formProvider);
        return multiBusy;
    }

    private MultiRect getPreciseStringMultiRect(Graphics2D g, String s) {
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

    private static double getSmaller(double d) {
//        return d * .99;
        return d * (Math.sqrt(5) - 1) / 2;//0.6180339
    }

    private static FormProvider createFormProvider(int width, int height) {
        int margin = Math.max(1, Math.min(width, height) / OUTER_MARGIN_DELIMITER);
        //return new Rectangle(0, 0, width, height);
        //return new Ellipse2D.Double(margin, margin, width - 2 * margin, height - 2 * margin);
        return createImageFormProvider(width, height, "heart2.png");
//        return createImageFormProvider(width, height, "christmas-tree.png");
//        return createImageFormProvider(width, height, "cat2.png");
//        return new FormProvider.ShapeFormProvider(new SuperEllipse(margin, margin, width - 2 * margin, height - 2 * margin, .7));

//        int d = Math.min(width, height) * 3 / 4;
//        return new FormProvider.ShapeFormProvider(new Ellipse2D.Double(width/2 - d/2, height/2 - d/2, d, d));
    }

    private static FormProvider createImageFormProvider(int width, int height, String name) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(new File("resources/"+name));
        } catch (IOException e) {
            return null;
        }
        double scaleFactor = (double)bufferedImage.getWidth() / bufferedImage.getHeight() > (double)width / height
                ? (double)width / bufferedImage.getWidth()
        : (double) height / bufferedImage.getHeight();
        BufferedImage bigOne = new BufferedImage(width, height, Transparency.TRANSLUCENT);
        Image scaled = bufferedImage.getScaledInstance((int)(bufferedImage.getWidth() * scaleFactor), (int)(bufferedImage.getHeight() * scaleFactor), Image.SCALE_SMOOTH);
        bigOne.createGraphics().drawImage(scaled, (width - scaled.getWidth(null))/2, (height - scaled.getHeight(null))/2, null);
        return new FormProvider.TransparentImageFormProvider(bigOne);
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
}
