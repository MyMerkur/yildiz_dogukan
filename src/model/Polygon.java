package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

    private final List<Point> points = new ArrayList<>();
    private boolean closed;

    public void addPoint(Point point) {
        if (!closed) {
            points.add(point);
        }
    }

    public boolean close() {
        if (points.size() < 3) {
            return false;
        }

        closed = true;
        return true;
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public boolean isClosed() {
        return closed;
    }
}
