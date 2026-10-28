package app;

import raster.RasterCanvas;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;

public class DrawPanel extends JPanel {

    private final RasterCanvas canvas;

    public DrawPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        canvas = new RasterCanvas(width, height);
        canvas.clear();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(canvas.getImage(), 0, 0, null);
    }

    public RasterCanvas getCanvas() {
        return canvas;
    }
}
