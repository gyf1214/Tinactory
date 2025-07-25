package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.WIDTH;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record Texture(ResourceLocation loc, int width, int height) {
    public static final Texture VOID = new Texture(modLoc("void"), 16, 16);
    public static final Texture BACKGROUND = new Texture(
        gregtech("gui/base/background"), WIDTH, 166);

    public static final Texture SLOT_BACKGROUND = new Texture(
        gregtech("gui/base/slot"), SLOT_SIZE, SLOT_SIZE);
    public static final Texture SWITCH_BUTTON = new Texture(
        gregtech("gui/widget/toggle_button_background"), 18, 36);
    public static final Texture GREGTECH_LOGO = new Texture(
        gregtech("gui/icon/gregtech_logo"), 17, 17);
    public static final Texture VANILLA_WIDGETS = new Texture(
        mcLoc("gui/widgets"), 256, 256);

    public static final Texture PROGRESS_ARROW = progressBar("arrow");
    public static final Texture PROGRESS_MACERATE = progressBar("macerate");
    public static final Texture PROGRESS_BATH = progressBar("bath");
    public static final Texture PROGRESS_SIFT = progressBar("sift");
    public static final Texture PROGRESS_EXTRACT = progressBar("extract");
    public static final Texture PROGRESS_CIRCUIT = progressBar("circuit");
    public static final Texture PROGRESS_CIRCUIT_ASSEMBLER = progressBar("circuit_assembler");
    public static final Texture PROGRESS_GAS = progressBar("gas_collector");
    public static final Texture PROGRESS_MULTIPLE = progressBar("arrow_multiple");
    public static final Texture PROGRESS_BENDING = progressBar("bending");
    public static final Texture PROGRESS_WIREMILL = progressBar("wiremill");
    public static final Texture PROGRESS_LATHE = progressBar("lathe");
    public static final Texture PROGRESS_SLICE = progressBar("slice");
    public static final Texture PROGRESS_MAGNETIC = progressBar("magnet");
    public static final Texture PROGRESS_COMPRESS = progressBar("compress");
    public static final Texture PROGRESS_MIXER = progressBar("mixer");
    public static final Texture PROGRESS_EXTRUDER = progressBar("extruder");
    public static final Texture PROGRESS_RECYCLER = progressBar("recycler");
    public static final Texture PROGRESS_BURN = new Texture(
        gregtech("gui/progress_bar/progress_bar_boiler_fuel_steel"), 18, 36);
    public static final Texture PROGRESS_LATH_BASE = new Texture(
        gregtech("gui/progress_bar/progress_bar_lathe_base"), 5, 18);
    public static final Texture PROGRESS_CLEANROOM = new Texture(
        gregtech("gui/progress_bar/progress_bar_turbine_rotor_speed"), 62, 14);

    public static final Texture HEAT_EMPTY = new Texture(
        gregtech("gui/progress_bar/progress_bar_boiler_empty_steel"), 10, 54);
    public static final Texture HEAT_FULL = new Texture(
        gregtech("gui/progress_bar/progress_bar_boiler_heat"), 10, 54);
    public static final Texture RECIPE_BOOK_BG = new Texture(
        mcLoc("gui/recipe_book"), 256, 256);
    public static final Texture RECIPE_BOOK_BUTTON = new Texture(
        mcLoc("gui/recipe_button"), 256, 256);
    public static final Texture DISABLE_BUTTON = new Texture(
        modLoc("gui/disable_recipe"), 16, 16);
    public static final Texture RECIPE_BUTTON = new Texture(
        modLoc("gui/recipe_book_button"), 42, 21);
    public static final Texture CRAFTING_ARROW = new Texture(
        modLoc("gui/arrow_crafting"), 22, 15);
    public static final Texture LOCK_BUTTON = new Texture(
        gregtech("gui/widget/button_public_private"), 18, 36);
    public static final Texture ALLOW_ARROW_BUTTON = new Texture(
        gregtech("gui/widget/button_allow_import_export"), 20, 40);
    public static final Texture GLOBAL_PORT_BUTTON = new Texture(
        gregtech("gui/widget/button_distinct_buses"), 18, 36);

    public Texture(ResourceLocation loc, int width, int height) {
        this.loc = new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png");
        this.width = width;
        this.height = height;
    }

    private static Texture progressBar(String name) {
        return new Texture(gregtech("gui/progress_bar/progress_bar_" + name), 20, 40);
    }
}
