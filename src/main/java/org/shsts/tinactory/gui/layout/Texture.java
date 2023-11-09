package org.shsts.tinactory.gui.layout;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.gui.ContainerMenu.SLOT_SIZE;
import static org.shsts.tinactory.gui.ContainerMenu.WIDTH;

@ParametersAreNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public static final Texture BACKGROUND = new Texture(
            ModelGen.gregtech("gui/base/background"), WIDTH, 166);
    public static final Texture SLOT_BACKGROUND = new Texture(
            ModelGen.gregtech("gui/base/slot"), SLOT_SIZE, SLOT_SIZE);
    public static final Texture PROGRESS_ARROW = new Texture(
            ModelGen.gregtech("gui/progress_bar/progress_bar_arrow"), 20, 40);

    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }
}
