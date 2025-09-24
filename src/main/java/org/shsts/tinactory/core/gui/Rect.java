package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Rect(int x, int y, int width, int height) {
    public static Rect corners(int x, int y, int endX, int endY) {
        return new Rect(x, y, endX - x, endY - y);
    }

    public static final Rect ZERO = corners(0, 0, 0, 0);

    public int endX() {
        return x + width;
    }

    public int endY() {
        return y + height;
    }

    public Rect offset(int dx, int dy) {
        return new Rect(x + dx, y + dy, width, height);
    }

    public Rect resize(int newW, int newH) {
        return new Rect(x, y, newW, newH);
    }

    public Rect enlarge(int dw, int dh) {
        return new Rect(x, y, width + dw, height + dh);
    }

    public int inX(double s) {
        return x + (int) (width * s);
    }

    public int inY(double s) {
        return y + (int) (height * s);
    }

    public boolean in(double pX, double pY) {
        return pX >= (double) (x) && pX < (double) (x + width) &&
            pY >= (double) (y) && pY < (double) (y + height);
    }

    public Rect2i toRect2i() {
        return new Rect2i(x, y, width, height);
    }
}
