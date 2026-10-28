package clipping;

import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class SutherlandHodgmanClipper {

    private static final double EPSILON = 0.000001;

    public Polygon clip(Polygon subject, Polygon clipPolygon) {
        if (!isValid(subject) || !isConvex(clipPolygon)) {
            return null;
        }

        List<DoublePoint> output = toDoublePoints(subject.getPoints());
        List<Point> clipPoints = clipPolygon.getPoints();
        double orientation = signedArea(clipPoints) > 0 ? 1 : -1;

        // sutherland hodgman polygon clipping
        for (int i = 0; i < clipPoints.size(); i++) {
            Point edgeStart = clipPoints.get(i);
            Point edgeEnd = clipPoints.get((i + 1) % clipPoints.size());
            output = clipByEdge(output, edgeStart, edgeEnd, orientation);

            if (output.isEmpty()) {
                return null;
            }
        }

        Polygon result = new Polygon();
        Point previous = null;

        for (DoublePoint point : output) {
            Point rounded = new Point((int) Math.round(point.x), (int) Math.round(point.y));

            if (previous == null || previous.getX() != rounded.getX()
                    || previous.getY() != rounded.getY()) {
                result.addPoint(rounded);
                previous = rounded;
            }
        }

        List<Point> resultPoints = result.getPoints();

        if (resultPoints.size() > 1) {
            Point first = resultPoints.get(0);
            Point last = resultPoints.get(resultPoints.size() - 1);

            if (first.getX() == last.getX() && first.getY() == last.getY()) {
                result = copyWithoutLast(resultPoints);
            }
        }

        if (!result.close()) {
            return null;
        }

        return result;
    }

    public boolean isConvex(Polygon polygon) {
        if (!isValid(polygon)) {
            return false;
        }

        List<Point> points = polygon.getPoints();
        int direction = 0;

        for (int i = 0; i < points.size(); i++) {
            Point first = points.get(i);
            Point second = points.get((i + 1) % points.size());
            Point third = points.get((i + 2) % points.size());
            long cross = cross(first, second, third);

            if (cross == 0) {
                continue;
            }

            int currentDirection = cross > 0 ? 1 : -1;

            if (direction == 0) {
                direction = currentDirection;
            } else if (direction != currentDirection) {
                return false;
            }
        }

        return direction != 0 && Math.abs(signedArea(points)) > EPSILON;
    }

    private List<DoublePoint> clipByEdge(List<DoublePoint> input, Point edgeStart,
                                         Point edgeEnd, double orientation) {
        List<DoublePoint> output = new ArrayList<>();

        if (input.isEmpty()) {
            return output;
        }

        DoublePoint previous = input.get(input.size() - 1);

        for (DoublePoint current : input) {
            boolean currentInside = isInside(current, edgeStart, edgeEnd, orientation);
            boolean previousInside = isInside(previous, edgeStart, edgeEnd, orientation);

            if (currentInside) {
                if (!previousInside) {
                    output.add(intersection(previous, current, edgeStart, edgeEnd));
                }
                output.add(current);
            } else if (previousInside) {
                output.add(intersection(previous, current, edgeStart, edgeEnd));
            }

            previous = current;
        }

        return output;
    }

    private boolean isInside(DoublePoint point, Point edgeStart,
                             Point edgeEnd, double orientation) {
        double cross = (edgeEnd.getX() - edgeStart.getX())
                * (point.y - edgeStart.getY())
                - (edgeEnd.getY() - edgeStart.getY())
                * (point.x - edgeStart.getX());
        return cross * orientation >= -EPSILON;
    }

    private DoublePoint intersection(DoublePoint first, DoublePoint second,
                                     Point edgeStart, Point edgeEnd) {
        double subjectX = second.x - first.x;
        double subjectY = second.y - first.y;
        double edgeX = edgeEnd.getX() - edgeStart.getX();
        double edgeY = edgeEnd.getY() - edgeStart.getY();
        double denominator = subjectX * edgeY - subjectY * edgeX;

        if (Math.abs(denominator) < EPSILON) {
            return second;
        }

        double startX = edgeStart.getX() - first.x;
        double startY = edgeStart.getY() - first.y;
        double amount = (startX * edgeY - startY * edgeX) / denominator;
        return new DoublePoint(first.x + amount * subjectX, first.y + amount * subjectY);
    }

    private boolean isValid(Polygon polygon) {
        return polygon != null && polygon.isClosed() && polygon.getPoints().size() >= 3;
    }

    private long cross(Point first, Point second, Point third) {
        return (long) (second.getX() - first.getX()) * (third.getY() - second.getY())
                - (long) (second.getY() - first.getY()) * (third.getX() - second.getX());
    }

    private double signedArea(List<Point> points) {
        double area = 0;

        for (int i = 0; i < points.size(); i++) {
            Point first = points.get(i);
            Point second = points.get((i + 1) % points.size());
            area += first.getX() * second.getY() - second.getX() * first.getY();
        }

        return area / 2.0;
    }

    private List<DoublePoint> toDoublePoints(List<Point> points) {
        List<DoublePoint> result = new ArrayList<>();

        for (Point point : points) {
            result.add(new DoublePoint(point.getX(), point.getY()));
        }

        return result;
    }

    private Polygon copyWithoutLast(List<Point> points) {
        Polygon result = new Polygon();

        for (int i = 0; i < points.size() - 1; i++) {
            result.addPoint(points.get(i));
        }

        return result;
    }

    private static class DoublePoint {

        private final double x;
        private final double y;

        private DoublePoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
