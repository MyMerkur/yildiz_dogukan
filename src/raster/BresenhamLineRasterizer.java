package raster;

// draws line pixels using only integer math, works for every slope
public class BresenhamLineRasterizer extends LineRasterizer {

    public BresenhamLineRasterizer(RasterCanvas canvas) {
        super(canvas);
    }

    @Override
    public void draw(int x1, int y1, int x2, int y2) {
        // bresenham line rasterization for all directions
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int stepX = x1 < x2 ? 1 : -1;
        int stepY = y1 < y2 ? 1 : -1;
        int error = dx - dy;

        while (true) {
            canvas.setPixel(x1, y1, color);

            if (x1 == x2 && y1 == y2) {
                break;
            }

            // pick the next pixel from the accumulated error on x and y
            int error2 = error * 2;

            if (error2 > -dy) {
                error = error - dy;
                x1 = x1 + stepX;
            }

            if (error2 < dx) {
                error = error + dx;
                y1 = y1 + stepY;
            }
        }
    }
}
