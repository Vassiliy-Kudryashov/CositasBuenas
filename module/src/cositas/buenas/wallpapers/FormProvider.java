package cositas.buenas.wallpapers;

import javafx.scene.layout.BackgroundImage;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public interface FormProvider {
    boolean contains(Rectangle2D rectangle);

    public class ShapeFormProvider implements FormProvider {
        private final Shape shape;

        public ShapeFormProvider(Shape shape) {
            this.shape = shape;
        }

        @Override
        public boolean contains(Rectangle2D rectangle) {
            return shape.contains(rectangle);
        }
    }

    public class TransparentImageFormProvider implements FormProvider {
        private final BufferedImage image;

        public TransparentImageFormProvider(BufferedImage image) {
            this.image = image;
        }

        @Override
        public boolean contains(Rectangle2D rectangle) {
            int startX = Math.max(0, (int)rectangle.getX());
            int endX = Math.min(image.getWidth(), (int)(rectangle.getX() + rectangle.getWidth()));
            int startY = Math.max(0, (int)rectangle.getY());
            int endY = Math.min(image.getHeight(), (int)(rectangle.getY() + rectangle.getHeight()));
            for (int x = startX; x < endX; x++) {
                for(int y = startY; y < endY; y++) {
                    if ( ((image.getRGB(x, y)  >> 24) & 0xff) == 0) return false;
                }
            }
            return true;
        }
    }
}
