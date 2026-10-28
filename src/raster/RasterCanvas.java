package raster;

import java.awt.image.BufferedImage;

public class RasterCanvas {

    private final BufferedImage image;
    private int clearColor = 0x202030;

    public RasterCanvas(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void clear() {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, clearColor);
            }
        }
    }

    public void setPixel(int x, int y, int color) {
        if (isInside(x, y)) {
            image.setRGB(x, y, color);
        }
    }

    public int getPixel(int x, int y) {
        if (!isInside(x, y)) {
            return clearColor;
        }
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    public boolean isInside(int x, int y) {
        return x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight();
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getClearColor() {
        return clearColor;
    }

    public BufferedImage getImage() {
        return image;
    }
}
