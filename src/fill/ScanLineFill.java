package fill;

import model.Point;
import model.Polygon;
import raster.RasterCanvas;

import java.util.ArrayList;
import java.util.List;

public class ScanLineFill {

    private final RasterCanvas canvas;

    public ScanLineFill(RasterCanvas canvas) {
        this.canvas = canvas;
    }

    // scanline fill for a closed polygon between its min and max y
    public boolean fill(Polygon polygon, int color) {
        List<Point> points = polygon.getPoints();

        if (!polygon.isClosed() || points.size() < 3) {
            return false;
        }

        int minY = points.get(0).getY();
        int maxY = minY;

        for (Point point : points) {
            minY = Math.min(minY, point.getY());
            maxY = Math.max(maxY, point.getY());
        }

        minY = Math.max(0, minY);
        maxY = Math.min(canvas.getHeight() - 1, maxY);

        for (int y = minY; y <= maxY; y++) {
            fillRow(points, y, color);
        }

        return true;
    }

    // find edge crossings on this row and fill pixels between the sorted pairs
    private void fillRow(List<Point> points, int y, int color) {
        List<Double> intersections = new ArrayList<>();
        double scanY = y + 0.5;

        for (int i = 0; i < points.size(); i++) {
            Point first = points.get(i);
            Point second = points.get((i + 1) % points.size());

            if ((first.getY() <= scanY && second.getY() > scanY)
                    || (second.getY() <= scanY && first.getY() > scanY)) {
                double x = first.getX()
                        + (scanY - first.getY())
                        * (second.getX() - first.getX())
                        / (second.getY() - first.getY());
                intersections.add(x);
            }
        }

        sortIntersections(intersections);

        for (int i = 0; i + 1 < intersections.size(); i += 2) {
            int startX = (int) Math.ceil(intersections.get(i));
            int endX = (int) Math.floor(intersections.get(i + 1));

            startX = Math.max(0, startX);
            endX = Math.min(canvas.getWidth() - 1, endX);

            for (int x = startX; x <= endX; x++) {
                canvas.setPixel(x, y, color);
            }
        }
    }

    private void sortIntersections(List<Double> values) {
        for (int i = 1; i < values.size(); i++) {
            double value = values.get(i);
            int position = i - 1;

            while (position >= 0 && values.get(position) > value) {
                values.set(position + 1, values.get(position));
                position--;
            }

            values.set(position + 1, value);
        }
    }
}
