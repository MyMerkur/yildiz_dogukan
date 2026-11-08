package fill;

import raster.RasterCanvas;

public class SeedFill {

    private final RasterCanvas canvas;

    public SeedFill(RasterCanvas canvas) {
        this.canvas = canvas;
    }

    public enum Mode {
        BACKGROUND_COLOR,
        BORDER_COLOR
    }

    // flood fill from a seed pixel, supports background and border modes
    public boolean fill(int x,int y,int borderColor,int fillColor,Mode mode) {
        //int backgroundColor = canvas.getClearColor();
        int targetColor = canvas.getPixel(x, y);
        
        if(targetColor == fillColor || targetColor == borderColor){
            return false;
        }
        if(!canFill(x,y,targetColor,borderColor,fillColor,mode)){
            return false;
        }

        //??????
        fillSpan(x,y,targetColor,borderColor,fillColor,mode);
        return true;
    }

    

    

    private void fillSpan(int x,int y,int targetColor,int borderColor,int fillColor,Mode mode) {
        if (!canFill(x,y,targetColor,borderColor,fillColor,mode)) {
            return;
        }

        int left = x;
        int right = x;

        while (canFill(left - 1,y,targetColor,borderColor,fillColor,mode)) {
            left--;
        }

        while (canFill(right + 1, y,targetColor,borderColor,fillColor,mode)) {
            right++;
        }

        for (int currentX = left; currentX <= right; currentX++) {
            canvas.setPixel(currentX, y, fillColor);
        }

        fillNextRow(left,right, y - 1,targetColor,borderColor,fillColor,mode);
        fillNextRow(left,right, y + 1,targetColor,borderColor,fillColor,mode);
    }

    private void fillNextRow(int left, int right, int y,int targetColor,int borderColor, int fillColor,Mode mode) {
        int x = left;

        while (x <= right) {
            while (x <= right
                    && !canFill(x,y,targetColor,borderColor,fillColor,mode)) {
                x++;
            }

            if (x <= right) {
                fillSpan(x,y,targetColor,borderColor,fillColor,mode);
            }

            while (x <= right
                    && !canFill(x,y,targetColor,borderColor,fillColor,mode)) {
                x++;
            }
        }
    }

    // tells if a pixel is still fillable under the current mode
    private boolean canFill(int x,int y,int targetColor,int borderColor,int fillColor,Mode mode) {
        if (!canvas.isInside(x, y)) {
            return false;
        }

        int color = canvas.getPixel(x, y);
        //return color == backgroundColor && color != borderColor && color != fillColor;

        if (color == fillColor){
            return false;
        }
        if (mode == Mode.BACKGROUND_COLOR){
            return color == targetColor;
        }
        return color != borderColor;
    }
}
