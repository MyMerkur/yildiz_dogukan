package app;

import fill.SeedFill;
import model.Line;
import model.Point;
import model.Polygon;
import raster.BresenhamLineRasterizer;
import raster.LineRasterizer;
import raster.RasterCanvas;

import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private static final int BORDER_COLOR = 0xFFFFFF;
    private static final int FILL_COLOR = 0x4080FF;

    private final DrawPanel panel;
    private final RasterCanvas canvas;
    private final LineRasterizer lineRasterizer;
    private final SeedFill seedFill;
    private final List<Line> lines = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();
    private final List<Point> fillPoints = new ArrayList<>();

    private Point startPoint;
    private Point currentPoint;
    private Polygon currentPolygon;
    private Point polygonPreview;
    private boolean shiftPressed;

    public Controller(DrawPanel panel) {
        this.panel = panel;
        canvas = panel.getCanvas();
        lineRasterizer = new BresenhamLineRasterizer(canvas);
        seedFill = new SeedFill(canvas);

        panel.setFocusable(true);
        addMouseInput();
        addKeyboardInput();
        redraw();
    }

    private void addMouseInput() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                panel.requestFocusInWindow();

                if (!SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }

                startPoint = pointFromMouse(event);
                currentPoint = startPoint;
                redraw();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (startPoint == null) {
                    return;
                }

                currentPoint = getEndPoint(startPoint, pointFromMouse(event));
                redraw();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (startPoint == null || !SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }

                currentPoint = getEndPoint(startPoint, pointFromMouse(event));
                lines.add(new Line(startPoint, currentPoint));
                startPoint = null;
                currentPoint = null;
                redraw();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isMiddleMouseButton(event)) {
                    fillFromPoint(pointFromMouse(event));
                    return;
                }

                if (!SwingUtilities.isRightMouseButton(event)) {
                    return;
                }

                Point point = pointFromMouse(event);

                if (currentPolygon == null) {
                    currentPolygon = new Polygon();
                    polygons.add(currentPolygon);
                }

                currentPolygon.addPoint(point);
                polygonPreview = point;
                redraw();
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                if (currentPolygon == null) {
                    return;
                }

                polygonPreview = pointFromMouse(event);
                redraw();
            }
        };

        panel.addMouseListener(mouse);
        panel.addMouseMotionListener(mouse);
    }

    private void addKeyboardInput() {
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                shiftPressed = event.isShiftDown();

                if (event.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygons.clear();
                    fillPoints.clear();
                    startPoint = null;
                    currentPoint = null;
                    currentPolygon = null;
                    polygonPreview = null;
                    redraw();
                } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    closePolygon();
                } else if (startPoint != null) {
                    redraw();
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
                shiftPressed = event.isShiftDown();

                if (startPoint != null) {
                    redraw();
                }
            }
        });
    }

    private Point pointFromMouse(MouseEvent event) {
        int x = Math.max(0, Math.min(event.getX(), canvas.getWidth() - 1));
        int y = Math.max(0, Math.min(event.getY(), canvas.getHeight() - 1));
        return new Point(x, y);
    }

    private void closePolygon() {
        if (currentPolygon != null && currentPolygon.close()) {
            currentPolygon = null;
            polygonPreview = null;
            redraw();
        }
    }

    private void fillFromPoint(Point point) {
        if (seedFill.fill(point.getX(), point.getY(), BORDER_COLOR, FILL_COLOR)) {
            fillPoints.add(point);
            panel.repaint();
        }
    }

    private Point getEndPoint(Point start, Point mouse) {
        if (!shiftPressed) {
            return mouse;
        }

        int dx = mouse.getX() - start.getX();
        int dy = mouse.getY() - start.getY();
        int absX = Math.abs(dx);
        int absY = Math.abs(dy);

        if (absX > absY * 2) {
            return new Point(mouse.getX(), start.getY());
        }

        if (absY > absX * 2) {
            return new Point(start.getX(), mouse.getY());
        }

        int size = Math.max(absX, absY);
        int x = start.getX() + Integer.signum(dx) * size;
        int y = start.getY() + Integer.signum(dy) * size;

        x = Math.max(0, Math.min(x, canvas.getWidth() - 1));
        y = Math.max(0, Math.min(y, canvas.getHeight() - 1));
        return new Point(x, y);
    }

    private void redraw() {
        canvas.clear();
        lineRasterizer.setColor(BORDER_COLOR);

        for (Line line : lines) {
            lineRasterizer.draw(line);
        }

        for (Polygon polygon : polygons) {
            drawPolygon(polygon);
        }

        for (Point point : fillPoints) {
            seedFill.fill(point.getX(), point.getY(), BORDER_COLOR, FILL_COLOR);
        }

        if (startPoint != null && currentPoint != null) {
            lineRasterizer.setColor(0xFF4040);
            lineRasterizer.draw(new Line(startPoint, currentPoint));
        }

        if (currentPolygon != null && polygonPreview != null
                && !currentPolygon.getPoints().isEmpty()) {
            List<Point> points = currentPolygon.getPoints();
            Point lastPoint = points.get(points.size() - 1);
            lineRasterizer.setColor(0xFF4040);
            lineRasterizer.draw(new Line(lastPoint, polygonPreview));
        }

        panel.repaint();
    }

    private void drawPolygon(Polygon polygon) {
        List<Point> points = polygon.getPoints();
        lineRasterizer.setColor(BORDER_COLOR);

        for (int i = 1; i < points.size(); i++) {
            lineRasterizer.draw(new Line(points.get(i - 1), points.get(i)));
        }

        if (polygon.isClosed()) {
            lineRasterizer.draw(new Line(points.get(points.size() - 1), points.get(0)));
        }
    }
}
