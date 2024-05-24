package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.material.Elements;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.Ores;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    // Element
    public static MaterialSet IRON;
    public static MaterialSet GOLD;
    public static MaterialSet COPPER;
    public static MaterialSet TIN;

    // Ore
    public static MaterialSet MAGNETITE;
    public static MaterialSet CHALCOPYRITE;
    public static MaterialSet PYRITE;
    public static MaterialSet CASSITERITE;

    public static MaterialSet TEST;
    public static MaterialSet STONE;
    public static MaterialSet FLINT;
    public static MaterialSet WROUGHT_IRON;

    static {
        MATERIALS = new HashMap<>();
        Elements.init();
        Ores.init();

        TEST = set("test")
                .toolDurability(12800000).toolTer(Tiers.NETHERITE).toolSet()
                .buildObject();

        STONE = set("stone")
                .color(0xFFCDCDCD).icon(IconSet.ROUGH).toolDurability(16)
                .existing("block", Items.COBBLESTONE)
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
                .color(0xFF002040).icon(IconSet.DULL)
                .existing("primary", Items.FLINT)
                .dust()
                .toolDurability(16).mortar()
                .toolProcess()
                .buildObject();

        WROUGHT_IRON = set("wrought_iron")
                .color(0xFFC8B4B4).icon(IconSet.METALLIC)
                .metalSet()
                .toolDurability(200).toolTer(Tiers.IRON).toolSet()
                .toolProcess().smelt()
                .buildObject();

        // tool component tags
        REGISTRATE.tag(Items.STICK, AllTags.TOOL_HANDLE);
        REGISTRATE.tag(WROUGHT_IRON.tag("stick"), AllTags.TOOL_HANDLE);
        REGISTRATE.tag(AllMaterials.IRON.tag("screw"), AllTags.TOOL_SCREW);
    }

    private static final Map<String, MaterialSet> MATERIALS;

    private static MaterialSet get(String name) {
        if (!MATERIALS.containsKey(name)) {
            throw new IllegalArgumentException("material %s does not exist".formatted(name));
        }
        return MATERIALS.get(name);
    }

    public static MaterialSet.Builder<?> set(String id) {
        return (new MaterialSet.Builder<>(Unit.INSTANCE, id))
                .onCreateObject(mat -> MATERIALS.put(mat.name, mat));
    }

    public static Supplier<Item> dust(String name) {
        return item(name, "dust");
    }

    public static Supplier<Item> ingot(String name) {
        return item(name, "ingot");
    }

    public static Supplier<Item> item(String name, String sub) {
        return () -> get(name).item(sub);
    }

    public static Supplier<TagKey<Item>> tag(String name, String sub) {
        return () -> get(name).tag(sub);
    }

    public static void init() {}

    public static void initRecipes() {
        for (var material : MATERIALS.values()) {
            material.freeze();
        }

        Ores.initRecipes();

        // smelt wrought iron nugget
        REGISTRATE.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(IRON.tag("nugget")), WROUGHT_IRON.item("nugget"), 0, 200)
                .unlockedBy("has_material", AllRecipes.has(IRON.tag("nugget"))), "_from_iron");

        // stone -> gravel
        AllRecipes.TOOL.recipe(Items.GRAVEL)
                .result(Items.GRAVEL, 1)
                .pattern("#").pattern("#")
                .define('#', STONE.tag("block"))
                .toolTag(AllTags.TOOL_HAMMER)
                .build();

        // gravel -> flint
        AllRecipes.TOOL.recipe(FLINT.loc("primary"))
                .result(FLINT.entry("primary"), 1)
                .pattern("###")
                .define('#', Items.GRAVEL)
                .toolTag(AllTags.TOOL_HAMMER)
                .build();

        // gravel -> sand
        AllRecipes.TOOL.recipe(Items.SAND)
                .result(Items.SAND, 1)
                .pattern("#")
                .define('#', Items.GRAVEL)
                .toolTag(AllTags.TOOL_MORTAR)
                .build();

    }
}
