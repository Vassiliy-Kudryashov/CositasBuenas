package cositas.buenas.util;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ImageUtil {
    public static void writeJPEG(BufferedImage image, File file, float quality) throws IOException {
        quality = Math.max(0, Math.min(quality, 1f));
        ImageWriter writer = null;
        try (OutputStream stream = Files.newOutputStream(file.toPath());
             ImageOutputStream ios = ImageIO.createImageOutputStream(stream)) {
            writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            if (writer != null) {
                writer.dispose();
            }
        }
    }

    public static void main(String[] args) throws IOException {
//        if (args.length == 1) {
//            convert(Paths.get(args[0]).toFile());
//        }
        showDuplicatesAsHTML(new File(new File(System.getProperty("user.home"), "Pictures"),"Cats"));
    }

    private static void convert(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    try {
                        convert(child);
                    } catch (IOException e) {
                        //not an image
                    }
                }
            }
            return;
        }
        int i = file.getName().lastIndexOf(".");
        if (i == -1) return;
        String nameWithoutExtension = file.getName().substring(0, i);
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                if (reader.getFormatName().equals("jpg")) return;
            }
        }
        BufferedImage image = ImageIO.read(file);
        writeJPEG(image, new File(file.getParentFile(), nameWithoutExtension+".jpg"), .95f);
        file.delete();
    }

    private static void showDuplicatesAsHTML(File scanFolder) throws IOException {
        if (!scanFolder.isDirectory()) return;

        File[] files = scanFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && (pathname.getName().endsWith(".jpg") || pathname.getName().endsWith(".png"));
            }
        });
        if (files == null) return;
        File report = new File(scanFolder, "report.html");
        BufferedWriter bw = new BufferedWriter(new FileWriter(report));
        bw.write("<html><body>");
        bw.newLine();
        Desktop.getDesktop().browse(report.toURI());
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            BufferedImage sq1 = toSquare(file1);
            if (sq1 == null) continue;
            int lowestDiff = Integer.MAX_VALUE;
            File best = null;
            for (int j = i + 1; j < files.length; j++) {
                File file2 = files[j];
                BufferedImage sq2 = toSquare(file2);
                if (sq2 == null) continue;
                double diff = getDiff(sq1, sq2);
                if (diff < lowestDiff) {
//                    System.out.println(file1 + " -> " + diff);
                    lowestDiff = (int) diff;
                    best = file2;
                }

            }

            if (lowestDiff > 120) continue;
            if (lowestDiff == 0) {
                if (file1.length() > best.length()) {
                    cache.remove(best);
                    best.delete();
                } else {
                    cache.remove(file1);
                    file1.delete();
                }
                continue;
            }

            System.out.println();
            String uri1 = file1.toURL().toExternalForm().replace("file:/", "file:///");
            System.out.println(uri1);
            System.out.println("and");
            String uri2 = best.toURL().toExternalForm().replace("file:/", "file:///");
            System.out.println(uri2);
            System.out.println("with diff " + (int) lowestDiff);
            System.out.println();
            bw.write("<table><tr><td><img src=\"" + uri1 + "\"><br>" + file1.getName() + "&nbsp;" + file1.length() / 1024 + "K</td></td><td><img src=\"" + uri2 + "\"><br>" + best.getName() + "&nbsp;" + best.length() / 1024 + "K</td></tr><td colspan=\"2\">" + lowestDiff + "</td></tr></table><hr>");
            bw.newLine();
            bw.flush();
        }
        bw.write("</body></html>");
        bw.newLine();
        bw.close();
    }

    static Map<File, BufferedImage> cache = new HashMap<>();

    private static BufferedImage toSquare(File file) throws IOException {
        if (!file.isFile()) return null;
        BufferedImage image = cache.get(file);
        if (image != null) {
            return image;
        }
        BufferedImage square = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        square.getGraphics().drawImage(ImageIO.read(file), 0, 0, 100, 100, null);
        cache.put(file, square);
        return square;
    }

    private static double getDiff(BufferedImage square1, BufferedImage square2) {
        double acc = 0;
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                acc += getDiff(square1.getRGB(x, y), square2.getRGB(x, y));
            }
        }
        return acc / 10000;
    }

    public static double getDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;
        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;
        double rmean = (r1 + r2) / 2d;
        int r = r1 - r2;
        int g = g1 - g2;
        int b = b1 - b2;
        double weightR = 2 + rmean / 256;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256;
        return Math.sqrt(weightR * r * r + weightG * g * g + weightB * b * b);
    }

}
