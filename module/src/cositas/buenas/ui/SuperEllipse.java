package cositas.buenas.ui;

import java.awt.*;
import java.awt.geom.*;

public class SuperEllipse implements Shape {
    GeneralPath myPath;
    public SuperEllipse(double x, double y, double width, double height) {
        this (x, y, width, height, .33);
    }
    public SuperEllipse(double x, double y, double width, double height, double arcRatio) {
        GeneralPath path = new GeneralPath();
        path.moveTo(1, 0);
        double step = Math.PI / 360;
        double n = 100d - 98d * Math.pow(arcRatio, .1);
        for (double theta = step; theta <= 2 * Math.PI; theta += step) {
            path.lineTo(
                    Math.pow(Math.abs(Math.cos(theta)), 2d/n) * Math.signum(Math.cos(theta)),
                    Math.pow(Math.abs(Math.sin(theta)), 2d/n) * Math.signum(Math.sin(theta)));
        }
        path.lineTo(1, 0);
        path.closePath();
        AffineTransform transform = AffineTransform.getScaleInstance(width /2 , height / 2);
        transform.preConcatenate(AffineTransform.getTranslateInstance(x + width /2, y + height /2));
        path.transform(transform);
        myPath = path;
    }

    @Override
    public Rectangle getBounds() {
        return myPath.getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        return myPath.getBounds2D();
    }

    @Override
    public boolean contains(double x, double y) {
        return myPath.contains(x, y);
    }

    @Override
    public boolean contains(Point2D p) {
        return myPath.contains(p);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return myPath.intersects(x, y, w, h);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        return contains(x, y, w, h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return myPath.contains(r);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return myPath.getPathIterator(at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at, flatness);
    }
}
