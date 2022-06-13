package cositas.buenas.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

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
}
