package cositas.buenas.util;

import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

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
        if (args.length == 1) {
            convert(Paths.get(args[0]).toFile());
        }
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
}
