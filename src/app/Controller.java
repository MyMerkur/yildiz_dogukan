package app;

import clipping.SutherlandHodgmanClipper;
import fill.ScanLineFill;
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
    private static final int SEED_FILL_COLOR = 0x4080FF;
    private static final int SCAN_FILL_COLOR = 0x40B060;
    private static final int CLIP_BORDER_COLOR = 0xFFD040;
    private static final int CLIPPED_COLOR = 0x40FFFF;

    private final DrawPanel panel;
    private final RasterCanvas canvas;
    private final LineRasterizer lineRasterizer;
    private final SeedFill seedFill;
    private SeedFill.Mode seedFillMode = SeedFill.Mode.BACKGROUND_COLOR;
    private final ScanLineFill scanLineFill;
    private final SutherlandHodgmanClipper polygonClipper;
    private final List<Line> lines = new ArrayList<>();
    private final List<Polygon> polygons = new ArrayList<>();
    private final List<FillRequest> fillRequests = new ArrayList<>();
    private final List<Polygon> scanFilledPolygons = new ArrayList<>();

    private Point startPoint;
    private Point currentPoint;
    private Polygon currentPolygon;
    private Point polygonPreview;
    private Polygon clippingPolygon;
    private Polygon clippedPolygon;
    private boolean shiftPressed;
    private boolean polygonMode;

    public Controller(DrawPanel panel) {
        this.panel = panel;
        canvas = panel.getCanvas();
        lineRasterizer = new BresenhamLineRasterizer(canvas);
        seedFill = new SeedFill(canvas);
        scanLineFill = new ScanLineFill(canvas);
        polygonClipper = new SutherlandHodgmanClipper();

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

                if (polygonMode) {
                    if (currentPolygon == null) {
                        currentPolygon = new Polygon();
                        polygons.add(currentPolygon);
                        clippingPolygon = null;
                        clippedPolygon = null;
                    }

                    polygonPreview = pointFromMouse(event);
                    redraw();
                    return;
                }

                startPoint = pointFromMouse(event);
                currentPoint = startPoint;
                redraw();
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                if (polygonMode) {
                    if (currentPolygon == null || polygonPreview == null) {
                        return;
                    }

                    polygonPreview = pointFromMouse(event);
                    redraw();
                    return;
                }

                if (startPoint == null) {
                    return;
                }

                currentPoint = getEndPoint(startPoint, pointFromMouse(event));
                redraw();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }

                if (polygonMode) {
                    if (currentPolygon == null) {
                        return;
                    }

                    polygonPreview = pointFromMouse(event);
                    currentPolygon.addPoint(polygonPreview);
                    polygonPreview = null;
                    redraw();
                    return;
                }

                if (startPoint == null) {
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
                }
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

                if (event.getKeyCode() == KeyEvent.VK_P) {
                    polygonMode = true;
                    startPoint = null;
                    currentPoint = null;

                } else if (event.getKeyCode() == KeyEvent.VK_L) {
                    polygonMode = false;
                    polygonPreview = null;

                } else if (event.getKeyCode() == KeyEvent.VK_B) {
                    if (seedFillMode == SeedFill.Mode.BACKGROUND_COLOR) {
                        seedFillMode = SeedFill.Mode.BORDER_COLOR;
                    } else {
                        seedFillMode = SeedFill.Mode.BACKGROUND_COLOR;
                    }

                } else if (event.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygons.clear();
                    fillRequests.clear();
                    scanFilledPolygons.clear();
                    startPoint = null;
                    currentPoint = null;
                    currentPolygon = null;
                    polygonPreview = null;
                    clippingPolygon = null;
                    clippedPolygon = null;
                    redraw();

                } else if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    closePolygon();

                } else if (event.getKeyCode() == KeyEvent.VK_F) {
                    fillLastPolygon();

                } else if (event.getKeyCode() == KeyEvent.VK_K) {
                    clipLastPolygons();

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
        if (seedFill.fill(point.getX(), point.getY(), BORDER_COLOR, SEED_FILL_COLOR, seedFillMode)) {
            fillRequests.add(new FillRequest(point, seedFillMode));
            panel.repaint();
        }
    }

    private void fillLastPolygon() {
        for (int i = polygons.size() - 1; i >= 0; i--) {
            Polygon polygon = polygons.get(i);

            if (polygon.isClosed()) {
                if (!scanFilledPolygons.contains(polygon)) {
                    scanFilledPolygons.add(polygon);
                }
                redraw();
                return;
            }
        }
    }

    private void clipLastPolygons() {
        Polygon subject = null;
        Polygon clip = null;

        for (int i = polygons.size() - 1; i >= 0; i--) {
            Polygon polygon = polygons.get(i);

            if (!polygon.isClosed()) {
                continue;
            }

            if (clip == null) {
                clip = polygon;
            } else {
                subject = polygon;
                break;
            }
        }

        if (subject == null || !polygonClipper.isConvex(clip)) {
            clippingPolygon = null;
            clippedPolygon = null;
            redraw();
            return;
        }

        clippingPolygon = clip;
        clippedPolygon = polygonClipper.clip(subject, clip);
        redraw();
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
        drawBorders();

        for (FillRequest request : fillRequests) {
            seedFill.fill(
                    request.point.getX(),
                    request.point.getY(),
                    BORDER_COLOR,
                    SEED_FILL_COLOR,
                    request.mode);
        }

        for (Polygon polygon : scanFilledPolygons) {
            scanLineFill.fill(polygon, SCAN_FILL_COLOR);
        }

        drawBorders();

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

        if (points.size() >= 2) {
            Point firstPoint = points.get(0);
            lineRasterizer.draw(new Line(firstPoint, polygonPreview));
        }
    }

        panel.repaint();
    }

    private void drawBorders() {
        lineRasterizer.setColor(BORDER_COLOR);

        for (Line line : lines) {
            lineRasterizer.draw(line);
        }

        for (Polygon polygon : polygons) {
            int color = polygon == clippingPolygon ? CLIP_BORDER_COLOR : BORDER_COLOR;
            drawPolygon(polygon, color);
        }

        if (clippedPolygon != null) {
            drawPolygon(clippedPolygon, CLIPPED_COLOR);
        }
    }

    private void drawPolygon(Polygon polygon, int color) {
        List<Point> points = polygon.getPoints();
        lineRasterizer.setColor(color);

        for (int i = 1; i < points.size(); i++) {
            lineRasterizer.draw(new Line(points.get(i - 1), points.get(i)));
        }

        if (polygon.isClosed()) {
            lineRasterizer.draw(new Line(points.get(points.size() - 1), points.get(0)));
        }
    }

    private static class FillRequest {

        private final Point point;
        private final SeedFill.Mode mode;

        private FillRequest(Point point, SeedFill.Mode mode) {
            this.point = point;
            this.mode = mode;
        }
    }
}
