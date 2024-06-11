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
import org.shsts.tinactory.content.material.FirstDegrees;
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
    public static MaterialSet NICKEL;
    public static MaterialSet ALUMINIUM;

    // First Degree
    public static MaterialSet WROUGHT_IRON;
    public static MaterialSet BRONZE;
    public static MaterialSet INVAR;
    public static MaterialSet CUPRONICKEL;
    public static MaterialSet STEEL;

    // Ore
    public static MaterialSet CHALCOPYRITE;
    public static MaterialSet PYRITE;
    public static MaterialSet LIMONITE;
    public static MaterialSet BANDED_IRON;
    public static MaterialSet COAL;
    public static MaterialSet CASSITERITE;
    public static MaterialSet REDSTONE;
    public static MaterialSet CINNABAR;
    public static MaterialSet RUBY;
    public static MaterialSet MAGNETITE;

    public static MaterialSet TEST;
    public static MaterialSet STONE;
    public static MaterialSet FLINT;

    static {
        SET = new HashMap<>();
        Elements.init();
        FirstDegrees.init();
        Ores.init();

        TEST = set("test")
                .toolSet(12800000, Tiers.NETHERITE)
                .buildObject();

        STONE = set("stone")
                .color(0xFFCDCDCD)
                .existing("block", Items.COBBLESTONE)
                .existing("tool/pickaxe", Items.STONE_PICKAXE)
                .existing("tool/shovel", Items.STONE_SHOVEL)
                .existing("tool/hoe", Items.STONE_HOE)
                .existing("tool/axe", Items.STONE_AXE)
                .existing("tool/sword", Items.STONE_SWORD)
                .alias("primary", "block")
                .dust()
                .toolProcess()
                .tool(16).hammer().build()
                .buildObject();

        FLINT = set("flint")
                .color(0xFF002040)
                .existing("primary", Items.FLINT)
                .dust()
                .toolProcess()
                .tool(16).mortar().build()
                .buildObject();

        // tool component tags
        REGISTRATE.tag(Items.STICK, AllTags.TOOL_HANDLE);
        REGISTRATE.tag(WROUGHT_IRON.tag("stick"), AllTags.TOOL_HANDLE);
        REGISTRATE.tag(AllMaterials.IRON.tag("screw"), AllTags.TOOL_SCREW);
    }

    public static final Map<String, MaterialSet> SET;

    public static MaterialSet get(String name) {
        if (!SET.containsKey(name)) {
            throw new IllegalArgumentException("material %s does not exist".formatted(name));
        }
        return SET.get(name);
    }

    public static MaterialSet.Builder<?> set(String id) {
        return (new MaterialSet.Builder<>(Unit.INSTANCE, id))
                .onCreateObject(mat -> SET.put(mat.name, mat));
    }

    public static Supplier<Item> primary(String name) {
        return item(name, "primary");
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

    public static TagKey<Item> tag(String sub) {
        return MaterialSet.Builder.prefixTag(sub);
    }

    public static void init() {}

    public static void initRecipes() {
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
