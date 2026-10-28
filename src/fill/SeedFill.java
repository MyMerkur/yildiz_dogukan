package fill;

import raster.RasterCanvas;

public class SeedFill {

    private final RasterCanvas canvas;

    public SeedFill(RasterCanvas canvas) {
        this.canvas = canvas;
    }

    public boolean fill(int x, int y, int borderColor, int fillColor) {
        int backgroundColor = canvas.getClearColor();

        if (fillColor == backgroundColor
                || !canFill(x, y, backgroundColor, borderColor, fillColor)) {
            return false;
        }

        fillSpan(x, y, backgroundColor, borderColor, fillColor);
        return true;
    }

    private void fillSpan(int x, int y, int backgroundColor,
                          int borderColor, int fillColor) {
        if (!canFill(x, y, backgroundColor, borderColor, fillColor)) {
            return;
        }

        int left = x;
        int right = x;

        while (canFill(left - 1, y, backgroundColor, borderColor, fillColor)) {
            left--;
        }

        while (canFill(right + 1, y, backgroundColor, borderColor, fillColor)) {
            right++;
        }

        for (int currentX = left; currentX <= right; currentX++) {
            canvas.setPixel(currentX, y, fillColor);
        }

        fillNextRow(left, right, y - 1, backgroundColor, borderColor, fillColor);
        fillNextRow(left, right, y + 1, backgroundColor, borderColor, fillColor);
    }

    private void fillNextRow(int left, int right, int y, int backgroundColor,
                             int borderColor, int fillColor) {
        int x = left;

        while (x <= right) {
            while (x <= right
                    && !canFill(x, y, backgroundColor, borderColor, fillColor)) {
                x++;
            }

            if (x <= right) {
                fillSpan(x, y, backgroundColor, borderColor, fillColor);
            }

            while (x <= right
                    && !canFill(x, y, backgroundColor, borderColor, fillColor)) {
                x++;
            }
        }
    }

    private boolean canFill(int x, int y, int backgroundColor,
                            int borderColor, int fillColor) {
        if (!canvas.isInside(x, y)) {
            return false;
        }

        int color = canvas.getPixel(x, y);
        return color == backgroundColor && color != borderColor && color != fillColor;
    }
}
