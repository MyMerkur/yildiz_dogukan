package raster;

import model.Line;

public abstract class LineRasterizer {

    protected final RasterCanvas canvas;
    protected int color = 0xFFFFFF;

    public LineRasterizer(RasterCanvas canvas) {
        this.canvas = canvas;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void draw(Line line) {
        draw(
                line.getStart().getX(),
                line.getStart().getY(),
                line.getEnd().getX(),
                line.getEnd().getY()
        );
    }

    public abstract void draw(int x1, int y1, int x2, int y2);
}
