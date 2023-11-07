package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    public static final MaterialSet TEST = set("test", 0xFFFFFF);
    public static final MaterialSet STONE = set("stone", 0xCDCDCD);
    public static final MaterialSet FLINT = set("flint", 0x002040);
    public static final MaterialSet IRON = set("iron", 0xC8C8C8);

    static {
        TEST.wrench(1280)
                .freeze();

        STONE.existing("block", ItemTags.STONE_CRAFTING_MATERIALS)
                .alias("primary", "block")
                .hammer(320)
                .dust("rough").grind(20)
                .freeze();

        FLINT.existing("primary", Items.FLINT)
                .mortar(320)
                .freeze();

        IRON.existing("ingot", Tags.Items.INGOTS_IRON)
                .alias("primary", "ingot")
                .dust("dull").grind(80)
                .freeze();
    }

    private static MaterialSet set(String id, int color) {
        return new MaterialSet(id, color);
    }

    public static void init() {}
}
