package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    public static final MaterialSet TEST;
    public static final MaterialSet STONE;
    public static final MaterialSet FLINT;
    public static final MaterialSet IRON;
    public static final MaterialSet WROUGHT_IRON;
    public static final MaterialSet MAGNETITE;

    static {
        MATERIALS = new HashSet<>();

        TEST = set("test")
                .durability(12800000).tier(Tiers.NETHERITE)
                .toolSet()
                .buildObject();

        STONE = set("stone")
                .color(0xFFCDCDCD).icon(IconSet.ROUGH).durability(16)
                .existing("block", ItemTags.STONE_CRAFTING_MATERIALS, Items.COBBLESTONE)
                .existing("tool/pickaxe", Items.STONE_PICKAXE)
                .existing("tool/shovel", Items.STONE_SHOVEL)
                .existing("tool/hoe", Items.STONE_HOE)
                .existing("tool/axe", Items.STONE_AXE)
                .existing("tool/sword", Items.STONE_SWORD)
                .alias("primary", "block")
                .dust()
                .hammer().toolProcess()
                .buildObject();

        FLINT = set("flint")
                .color(0xFF002040).icon(IconSet.DULL).durability(16)
                .existing("primary", Items.FLINT)
                .dust()
                .mortar().toolProcess()
                .buildObject();

        IRON = set("iron")
                .color(0xFFC8C8C8).icon(IconSet.METALLIC)
                .existing("ingot", Tags.Items.INGOTS_IRON, Items.IRON_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_IRON, Items.IRON_NUGGET)
                .existing("wire", ModelGen.modLoc("network/cable/ulv"))
                .mechanicalSet()
                .toolProcess().smelt(200)
                .buildObject();

        WROUGHT_IRON = set("wrought_iron")
                .color(0xFFC8B4B4).icon(IconSet.METALLIC).durability(200).tier(Tiers.IRON)
                .metalSet().toolSet()
                .toolProcess().smelt(200)
                .buildObject();

        MAGNETITE = set("magnetite")
                .color(0xFF1E1E1E).icon(IconSet.METALLIC)
                .ore(Tiers.IRON, 3.0f)
                .buildObject();

        // tool component tags
        REGISTRATE.tag(Items.STICK, AllTags.TOOL_HANDLE);
        REGISTRATE.tag(WROUGHT_IRON.tag("stick"), AllTags.TOOL_HANDLE);
        REGISTRATE.tag(IRON.tag("screw"), AllTags.TOOL_SCREW);
    }

    private static final Set<MaterialSet> MATERIALS;

    private static MaterialSet.Builder<?> set(String id) {
        return (new MaterialSet.Builder<>(Unit.INSTANCE, id))
                .onCreateObject(MATERIALS::add);
    }

    public static void init() {}

    public static void initRecipes() {
        for (var material : MATERIALS) {
            material.freeze();
        }
    }
}
