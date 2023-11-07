package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    public static final MaterialSet TEST = set("test", 0xFFFFFF);
    public static final MaterialSet STONE = set("stone", Material.STONE);
    public static final MaterialSet FLINT = set("flint", 0x2E2D2D);

    static {
        TEST.wrench(1280)
                .freeze();

        STONE.existing("block", ItemTags.STONE_CRAFTING_MATERIALS)
                .existing("tool_material", ItemTags.STONE_TOOL_MATERIALS)
                .hammer(320)
                .dust("rough")
                .freeze();

        FLINT.existing("tool_material", Items.FLINT)
                .mortar(320)
                .freeze();
    }

    private static MaterialSet set(String id, int color) {
        return new MaterialSet(id, color);
    }

    private static MaterialSet set(String id, Material fromMaterial) {
        return new MaterialSet(id, fromMaterial.getColor().calculateRGBColor(MaterialColor.Brightness.NORMAL));
    }

    public static void init() {}
}
