package cositas.buenas.wallpapers;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

class MultiRect {
    private final List<Rectangle2D> myList;
    final Rectangle2D.Double myOuterRect;

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

    public Rectangle getBounds() {
        return new Rectangle((int) myOuterRect.x, (int) myOuterRect.y, (int) myOuterRect.width, (int) myOuterRect.height);
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
