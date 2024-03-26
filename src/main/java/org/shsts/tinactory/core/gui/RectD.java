package org.shsts.tinactory.core.gui;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record RectD(double x, double y, double width, double height) {
    public static RectD corners(double x, double y, double endX, double endY) {
        return new RectD(x, y, endX - x, endY - y);
    }

    public static final RectD ZERO = corners(0d, 0d, 0d, 0d);
    public static final RectD FULL = corners(0d, 0d, 1d, 1d);

    public double endX() {
        return x + width;
    }

    public double endY() {
        return y + height;
    }
}
