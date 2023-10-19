package org.shsts.tinactory.gui;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }
}
