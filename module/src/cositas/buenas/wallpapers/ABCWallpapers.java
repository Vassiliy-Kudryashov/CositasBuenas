package cositas.buenas.wallpapers;

import cositas.buenas.util.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cositas.buenas.wallpapers.ABCConfig.*;


public class ABCWallpapers {
    //TODO: Refactor config as .properties + defaults-aware helper class
    //TODO: Parse font from https://fonts.google.com/ without installation,
    // GraphicsEnvironment.registerFont(
    //                Font.createFont(Font.TRUETYPE_FONT, {stream from URL}))
    // maybe use https://github.com/anupthegit/WOFFToTTFJava
    //TODO convert results into a web game where clicks in proper ABC order solves the puzzle

    static final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static Set<Font> ALL_FONTS = new TreeSet<>(Comparator.comparing(Font::getName));
    private static final List<String> STANDARD_FONT_NICKNAMES = Arrays.asList(Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED);

    static {
        System.out.print("Collecting system fonts and google fonts...");
        try {
            File baseDir = new File(new File(System.getProperty("user.home")), ".getlog").getCanonicalFile();
            HashSet<File> fontFiles = new HashSet<>();
            collectFontFiles(new File(baseDir,"fonts"), fontFiles);
            for (File fontFile : fontFiles) {
                try {
                    environment.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
                } catch (FontFormatException ffe) {
                    System.err.println(ffe.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ALL_FONTS.addAll(Arrays.asList(environment.getAllFonts()));
        ALL_FONTS.removeIf(font -> {
            for (int i = 0; i < ABC.length(); i++) {
                if (!font.canDisplay(ABC.charAt(i))) {
                    return true;
                }
            }
//            if (!font.getName().contains("Bold")) return true;
            return (STANDARD_FONT_NICKNAMES.contains(font.getFamily()) && font.getName().contains(font.getFamily()));
        });
        System.out.println("OK, there are " + ALL_FONTS.size() + " fonts to process");
    }

    static void collectFontFiles(File file, Set<File> collector) {
        if (file.isFile() && file.getName().endsWith(".ttf")) collector.add(file);
        Optional.ofNullable(file.listFiles()).ifPresent(
                files -> Arrays.stream(files).forEach(child -> collectFontFiles(child, collector)));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File pictures = HTML_VERSION
                ? FileUtil.mkDirs("Pictures/ABC_HTML")
//                : FileUtil.mkDirs("Pictures/ABCWallpapers");
                : FileUtil.mkDirs("Pictures/ABCHearts");
//                : FileUtil.mkDirs("Pictures/ABCTrees");
//                : FileUtil.mkDirs("Pictures/ABCircles");
        if (CLEAR_PREVIOUS_RESULTS) {
            File[] files = pictures.listFiles(file -> file.isFile() && file.getName().matches(".*\\.(png|jpg)"));
            if (files != null) {
                //noinspection ResultOfMethodCallIgnored
                Arrays.stream(files).forEach(File::delete);
            }
        }
        int minHue = 18;//100 / 6;//0
        int maxHue = 50;//100 / 2;//100
        GraphicsDevice device = environment.getDefaultScreenDevice();
        int width = /*2 **/ device.getDefaultConfiguration().getBounds().width;
        int height = /*2 * */device.getDefaultConfiguration().getBounds().height;

        final float sat = 1.0f;
        final float brt = 0.5f;
        final float hueStep = 2f * (maxHue - minHue) / ALL_FONTS.size();//1.0
        List<Integer> rgbs = new ArrayList<>();
        for (float hue = minHue; hue < maxHue; hue += hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));
        for (float hue = maxHue; hue >= minHue; hue -= hueStep) rgbs.add(Color.HSBtoRGB(hue / 100f, sat, brt));

//        int total = rgbs.size();
//        while (total % 3 == 0 && total % 2 == 0) total--;//366 -> 365

        ScheduledExecutorService service = Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 2));
        int i = 0;
        for (Font font : ALL_FONTS) {
            Color c1 = new Color(rgbs.get(i));
            Color c2 = new Color(rgbs.get((i + Math.max(1, rgbs.size() / 10)) % ALL_FONTS.size()));
            int finalI = i;
            service.submit(() -> {
                long start = System.currentTimeMillis();
                File output = new File(pictures, font.getName() + ".png");
                if (output.isFile() && SKIP_EXISTING_FILES) return;

                try {
                    BufferedImage image = new FontToImageProcessor(font, width, height, c1, c2).call();
//                    ImageUtil.writeJPEG(image, output, 1.0f);
                    ImageIO.write(image, "png", output);
//                    if (HTML_VERSION) {
//                        HtmlGame.generateHTML(pictures, width, height, output, multiRectMap);
//                    }
                    System.out.println(FORMAT.format(System.currentTimeMillis())+" "+ font.getName() + " " + (finalI + 1) + "/" + ALL_FONTS.size()
                            + " done in " + (System.currentTimeMillis() - start) + "ms ");
                } catch (Exception e) {
                    System.err.println(font.getName() + " failed with exception " + e.getMessage());
                }
            });
            i++;
        }
        service.shutdown();
        service.awaitTermination(1, TimeUnit.DAYS);

        Toolkit.getDefaultToolkit().beep();
    }
}
