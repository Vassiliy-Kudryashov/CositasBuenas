package cositas.buenas.ui;

import sun.awt.image.IntegerComponentRaster;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class DitheredGradientPaint extends GradientPaint {
    public DitheredGradientPaint(float x1, float y1, Color color1, float x2, float y2, Color color2) {
        super(x1, y1, color1, x2, y2, color2);
    }

    public DitheredGradientPaint(Point2D pt1, Color color1, Point2D pt2, Color color2) {
        super(pt1, color1, pt2, color2);
    }

    @Override
    public PaintContext createContext(ColorModel cm,
                                      Rectangle deviceBounds,
                                      Rectangle2D userBounds,
                                      AffineTransform xform,
                                      RenderingHints hints) {
        return new DitheredGradientPaintContext(cm, getPoint1(), getPoint2(), xform, getColor1(), getColor2());
    }

    /**
     * Most of the code copied from java.awt.GradientPaintContext
     */
    static class DitheredGradientPaintContext implements PaintContext {
        static ColorModel xrgbmodel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
        static ColorModel xbgrmodel = new DirectColorModel(24, 0x000000ff, 0x0000ff00, 0x00ff0000);

        static ColorModel cachedModel;
        static WeakReference<Raster> cached;

        static synchronized Raster getCachedRaster(ColorModel cm, int w, int h) {
            if (cm == cachedModel) {
                if (cached != null) {
                    Raster ras = cached.get();
                    if (ras != null &&
                            ras.getWidth() >= w &&
                            ras.getHeight() >= h) {
                        cached = null;
                        return ras;
                    }
                }
            }
            return cm.createCompatibleWritableRaster(w, h);
        }

        static synchronized void putCachedRaster(ColorModel cm, Raster ras) {
            if (cached != null) {
                Raster raster = cached.get();
                if (raster != null) {
                    int cw = raster.getWidth();
                    int ch = raster.getHeight();
                    int iw = ras.getWidth();
                    int ih = ras.getHeight();
                    if (cw >= iw && ch >= ih) {
                        return;
                    }
                    if (cw * ch >= iw * ih) {
                        return;
                    }
                }
            }
            cachedModel = cm;
            cached = new WeakReference<>(ras);
        }

        private static final ThreadLocal<int[][]> RGB_ARRAYS = new ThreadLocal<int[][]>() {
            @Override
            protected int[][] initialValue() {
                return new int[3][256];
            }
        };
        private static final ThreadLocal<int[][][][]> DITHER_ARRAYS = new ThreadLocal<int[][][][]>() {
            @Override
            protected int[][][][] initialValue() {
                return new int[3][256][][];
            }
        };
        //Dithering
        private static final double GR = (Math.sqrt(5) - 1) / 2;
        private static final int[] IDENTITY_FUNC = new int[256];

        private static final int[] SHIFT_FUNC = new int[256];

        private static final int[][][] DITHER_MATRIX = new int[256][256][];

        static {
            for (int i = 0; i < 256; i++) {
                IDENTITY_FUNC[i] = i;
                SHIFT_FUNC[i] = i + 1;
            }
            SHIFT_FUNC[255] = 255;
            int iter = 0;
            for (int i = 0; i < DITHER_MATRIX.length; i++) {
                int[][] row = DITHER_MATRIX[i];
                Arrays.fill(row, IDENTITY_FUNC);
                for (int j = 0; j < i; j++) {
                    int pos = (int) (1604419 * GR * iter) % (256 - j);
                    row[getIndex(row, pos)] = SHIFT_FUNC;
                    iter++;
                }
            }
        }

        private final double myX1;
        private final double myY1;
        private final double myDx;
        private final double myDy;
        private final int myRgb1;
        private final int myRgb2;

        private Raster saved;

        private final ColorModel model;

        DitheredGradientPaintContext(ColorModel cm,
                                     Point2D p1, Point2D p2, AffineTransform xform,
                                     Color c1, Color c2) {
            Point2D xvec = new Point2D.Double(1, 0);
            Point2D yvec = new Point2D.Double(0, 1);
            try {
                AffineTransform inverse = xform.createInverse();
                inverse.deltaTransform(xvec, xvec);
                inverse.deltaTransform(yvec, yvec);
            } catch (NoninvertibleTransformException e) {
                xvec.setLocation(0, 0);
                yvec.setLocation(0, 0);
            }

            double udx = p2.getX() - p1.getX();
            double udy = p2.getY() - p1.getY();
            double ulenSq = udx * udx + udy * udy;

            if (ulenSq <= Double.MIN_VALUE) {
                myDx = 0;
                myDy = 0;
            } else {
                double dxx = (xvec.getX() * udx + xvec.getY() * udy) / ulenSq;
                double dyy = (yvec.getX() * udx + yvec.getY() * udy) / ulenSq;

                if (dxx < 0) {
                    p1 = p2;
                    Color c = c1;
                    c1 = c2;
                    c2 = c;
                    myDx = -dxx;
                    myDy = -dyy;
                } else {
                    myDx = dxx;
                    myDy = dyy;
                }
            }

            Point2D dp1 = xform.transform(p1, null);
            this.myX1 = dp1.getX();
            this.myY1 = dp1.getY();

            myRgb1 = c1.getRGB();
            myRgb2 = c2.getRGB();
            int a1 = (myRgb1 >> 24) & 0xff;
            int r1 = (myRgb1 >> 16) & 0xff;
            int g1 = (myRgb1 >> 8) & 0xff;
            int b1 = (myRgb1) & 0xff;
            int da = ((myRgb2 >> 24) & 0xff) - a1;
            int dr = ((myRgb2 >> 16) & 0xff) - r1;
            int dg = ((myRgb2 >> 8) & 0xff) - g1;
            int db = ((myRgb2) & 0xff) - b1;
            ColorModel m;
            if (a1 == 0xff && da == 0) {
                m = xrgbmodel;
                if (cm instanceof DirectColorModel) {
                    DirectColorModel dcm = (DirectColorModel) cm;
                    int tmp = dcm.getAlphaMask();
                    if ((tmp == 0 || tmp == 0xff) &&
                            dcm.getRedMask() == 0xff &&
                            dcm.getGreenMask() == 0xff00 &&
                            dcm.getBlueMask() == 0xff0000) {
                        m = xbgrmodel;
                        tmp = r1;
                        r1 = b1;
                        b1 = tmp;
                        tmp = dr;
                        dr = db;
                        db = tmp;
                    }
                }
            } else {
                m = ColorModel.getRGBdefault();
            }
            model = m;

            for (int i = 0; i < 256; i++) {
                double rel = i / 256.0f;
                double rValue = r1 + dr * rel;
                double gValue = g1 + dg * rel;
                double bValue = b1 + db * rel;

                DITHER_ARRAYS.get()[0][i] = DITHER_MATRIX[(int) (rValue * 256) % 256];
                DITHER_ARRAYS.get()[1][i] = DITHER_MATRIX[(int) (gValue * 256) % 256];
                DITHER_ARRAYS.get()[2][i] = DITHER_MATRIX[(int) (bValue * 256) % 256];

                RGB_ARRAYS.get()[0][i] = (int) rValue;
                RGB_ARRAYS.get()[1][i] = (int) gValue;
                RGB_ARRAYS.get()[2][i] = (int) bValue;
            }
        }

        public void dispose() {
            if (saved != null) {
                putCachedRaster(model, saved);
                saved = null;
            }
        }

        public ColorModel getColorModel() {
            return model;
        }

        public Raster getRaster(int x, int y, int w, int h) {
            double rowrel = (x - myX1) * myDx + (y - myY1) * myDy;

            Raster rast = saved;
            if (rast == null || rast.getWidth() < w || rast.getHeight() < h) {
                rast = getCachedRaster(model, w, h);
                saved = rast;
            }
            IntegerComponentRaster irast = (IntegerComponentRaster) rast;
            int off = irast.getDataOffset(0);
            int adjust = irast.getScanlineStride() - w;
            int[] pixels = irast.getDataStorage();

            clipFillRaster(pixels, off, adjust, w, h, rowrel, myDx, myDy);

            return rast;
        }

        private static int getIndex(int[][] arr, int pos) {
            for (int i = 0; i < arr.length; i++) {
                int[] f = arr[i];
                if (f == IDENTITY_FUNC) {
                    pos--;
                }
                if (pos < 0) {
                    return i;
                }
            }
            throw new IllegalArgumentException();
        }

        void clipFillRaster(int[] pixels, int off, int adjust, int w, int h, double rowrel, double dx, double dy) {
            while (--h >= 0) {
                double colrel = rowrel;
                int j = w;
                if (colrel <= 0.0) {
                    int rgb = myRgb1;
                    do {
                        pixels[off++] = rgb;
                        colrel += dx;
                    }
                    while (--j > 0 && colrel <= 0.0);
                }
                while (colrel < 1.0 && --j >= 0) {
                    int offrel = off & 0xFF;
                    int idx = (int) (colrel * 256);

                    int rresult = DITHER_ARRAYS.get()[0][idx][offrel][RGB_ARRAYS.get()[0][idx]];
                    int gresult = DITHER_ARRAYS.get()[1][idx][offrel][RGB_ARRAYS.get()[1][idx]];
                    int bresult = DITHER_ARRAYS.get()[2][idx][offrel][RGB_ARRAYS.get()[2][idx]];

                    pixels[off++] = (rresult << 16) | (gresult << 8) | bresult;
                    colrel += dx;
                }
                if (j > 0) {
                    int rgb = myRgb2;
                    do {
                        pixels[off++] = rgb;
                    }
                    while (--j > 0);
                }

                off += adjust;
                rowrel += dy;
            }
        }
    }
}

