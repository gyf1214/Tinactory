package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.model.ModelGen.gregtech;
import static org.shsts.tinactory.content.model.ModelGen.mcLoc;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.WIDTH;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public static final Texture BACKGROUND = new Texture(
            gregtech("gui/base/background"), WIDTH, 166);
    public static final Texture SLOT_BACKGROUND = new Texture(
            gregtech("gui/base/slot"), SLOT_SIZE, SLOT_SIZE);
    public static final Texture SWITCH_BUTTON = new Texture(
            gregtech("gui/widget/toggle_button_background"), 18, 36);
    public static final Texture AUTO_IN_BUTTON = new Texture(
            gregtech("gui/widget/button_output"), 18, 18);
    public static final Texture ITEM_OUT_BUTTON = new Texture(
            gregtech("gui/widget/button_item_output_overlay"), 18, 18);
    public static final Texture FLUID_OUT_BUTTON = new Texture(
            gregtech("gui/widget/button_fluid_output_overlay"), 18, 18);
    public static final Texture VANILLA_WIDGETS = new Texture(
            mcLoc("gui/widgets"), 256, 256);

    public static final Texture PROGRESS_ARROW = progress("arrow");
    public static final Texture PROGRESS_MACERATE = progress("macerate");
    public static final Texture PROGRESS_BATH = progress("bath");
    public static final Texture PROGRESS_SIFT = progress("sift");
    public static final Texture PROGRESS_EXTRACT = progress("extract");
    public static final Texture PROGRESS_CIRCUIT = progress("circuit");
    public static final Texture PROGRESS_GAS = progress("gas_collector");
    public static final Texture PROGRESS_MULTIPLE = progress("arrow_multiple");
    public static final Texture PROGRESS_BURN = new Texture(
            gregtech("gui/progress_bar/progress_bar_boiler_fuel_steel"), 18, 36);

    public static final Texture HEAT_EMPTY = new Texture(
            gregtech("gui/progress_bar/progress_bar_boiler_empty_steel"), 10, 54);
    public static final Texture HEAT_FULL = new Texture(
            gregtech("gui/progress_bar/progress_bar_boiler_heat"), 10, 54);

    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }

    public static Texture progress(String name) {
        return new Texture(gregtech("gui/progress_bar/progress_bar_" + name), 20, 40);
    }
}
