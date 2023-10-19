package org.shsts.tinactory.gui;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public static final Texture BACKGROUND = new Texture(
            ModelGen.vendorLoc("gregtech", "gui/base/background"), ContainerMenu.WIDTH, 166);
    public static final Texture SLOT_BACKGROUND = new Texture(
            ModelGen.vendorLoc("gregtech", "gui/base/slot"), ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE);
    public static final Texture PROGRESS_ARROW = new Texture(
            ModelGen.vendorLoc("gregtech", "gui/progress_bar/progress_bar_arrow"), 20, 40);

    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }
}
