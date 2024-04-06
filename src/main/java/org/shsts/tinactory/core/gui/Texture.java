package org.shsts.tinactory.core.gui;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.WIDTH;

@ParametersAreNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public static final Texture BACKGROUND = new Texture(
            ModelGen.gregtech("gui/base/background"), WIDTH, 166);
    public static final Texture SLOT_BACKGROUND = new Texture(
            ModelGen.gregtech("gui/base/slot"), SLOT_SIZE, SLOT_SIZE);
    public static final Texture PROGRESS_ARROW = new Texture(
            ModelGen.gregtech("gui/progress_bar/progress_bar_arrow"), 20, 40);
    public static final Texture SWITCH_BUTTON = new Texture(
            ModelGen.gregtech("gui/widget/toggle_button_background"), 18, 36);
    public static final Texture ITEM_OUT_BUTTON = new Texture(
            ModelGen.gregtech("gui/widget/button_item_output_overlay"), 18, 18);
    public static final Texture FLUID_OUT_BUTTON = new Texture(
            ModelGen.gregtech("gui/widget/button_fluid_output_overlay"), 18, 18);

    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }
}
