package app;

import model.Line;
import model.Point;
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

    private final DrawPanel panel;
    private final RasterCanvas canvas;
    private final LineRasterizer lineRasterizer;
    private final List<Line> lines = new ArrayList<>();

    private Point startPoint;
    private Point currentPoint;
    private boolean shiftPressed;

    public Controller(DrawPanel panel) {
        this.panel = panel;
        canvas = panel.getCanvas();
        lineRasterizer = new BresenhamLineRasterizer(canvas);

        panel.setFocusable(true);
        addMouseInput();
        addKeyboardInput();
        redraw();
    }

    private void addMouseInput() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!SwingUtilities.isLeftMouseButton(event)) {
                    return;
                }

                panel.requestFocusInWindow();
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
                    startPoint = null;
                    currentPoint = null;
                    redraw();
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
        lineRasterizer.setColor(0xFFFFFF);

        for (Line line : lines) {
            lineRasterizer.draw(line);
        }

        if (startPoint != null && currentPoint != null) {
            lineRasterizer.setColor(0xFF4040);
            lineRasterizer.draw(new Line(startPoint, currentPoint));
        }

        panel.repaint();
    }
}
