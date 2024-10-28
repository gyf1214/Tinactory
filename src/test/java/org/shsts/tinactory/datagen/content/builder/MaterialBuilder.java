package org.shsts.tinactory.datagen.content.builder;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.builder.DataBuilder;
import org.shsts.tinactory.datagen.content.Models;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinactory.datagen.context.RegistryDataContext;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.BENDER;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.CUTTER;
import static org.shsts.tinactory.content.AllRecipes.LATHE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.ORE_WASHER;
import static org.shsts.tinactory.content.AllRecipes.POLARIZER;
import static org.shsts.tinactory.content.AllRecipes.THERMAL_CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.WIREMILL;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.TOOL_FILE;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_HANDLE;
import static org.shsts.tinactory.content.AllTags.TOOL_MORTAR;
import static org.shsts.tinactory.content.AllTags.TOOL_SAW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.VOID_TEX;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.oreBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialBuilder<P> extends DataBuilder<P, MaterialBuilder<P>> {
    private static ResourceLocation toolTex(String sub) {
        return gregtech("items/tools/" + sub);
    }

    private static final Map<String, String> TOOL_HANDLE_TEX = ImmutableMap.<String, String>builder()
            .put("hammer", "handle_hammer")
            .put("mortar", "mortar_base")
            .put("file", "handle_file")
            .put("saw", "handle_saw")
            .put("screwdriver", "handle_screwdriver")
            .put("wire_cutter", "wire_cutter_base")
            .build();

    private final MaterialSet material;
    @Nullable
    private IconSet icon = null;

    public MaterialBuilder(DataGen dataGen, P parent, MaterialSet material) {
        super(dataGen, parent, material.name);
        this.material = material;
    }

    public MaterialBuilder<P> icon(IconSet value) {
        icon = value;
        return this;
    }

    public MaterialBuilder<P> toolProcess() {
        // grind dust
        process("dust", 1, "primary", TOOL_MORTAR);
        process("dust_tiny", 1, "nugget", TOOL_MORTAR);
        // plate
        process("plate", 1, "A\nA", "ingot", TOOL_HAMMER);
        // foil
        process("foil", 2, "plate", TOOL_HAMMER);
        // ring
        process("ring", 1, "plate", TOOL_WIRE_CUTTER);
        process("ring", 1, "sheet", TOOL_WIRE_CUTTER);
        // stick
        process("stick", 1, "ingot", TOOL_FILE);
        // bolt
        process("bolt", 2, "stick", TOOL_SAW);
        // screw
        process("screw", 1, "bolt", TOOL_FILE);
        // gear
        process("gear", 1, "A\nB\nA", "stick", "plate", TOOL_HAMMER, TOOL_WIRE_CUTTER);
        // rotor
        process("rotor", 1, "A A\nBC \nA A", "plate", "stick", "screw",
                TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
        // cut wire
        process("wire", 1, "plate", TOOL_WIRE_CUTTER);
        // pipe
        process("pipe", 1, "AAA", "plate", TOOL_HAMMER, TOOL_WRENCH);
        return this;
    }

    private void process(RecipeTypeEntry<?, ? extends ProcessingRecipe.BuilderBase<?, ?>> recipeType,
                         String result, int count, String input, int outputPort,
                         Voltage v, long workTicks) {
        if (!material.hasItem(result) || !material.hasItem(input)) {
            return;
        }
        recipeType.recipe(DATA_GEN, material.loc(result))
                .outputItem(outputPort, material.entry(result), count)
                .inputItem(0, material.tag(input), 1)
                .voltage(v)
                .workTicks(workTicks)
                .build();
    }

    private void process(RecipeTypeEntry<?, ? extends ProcessingRecipe.BuilderBase<?, ?>> recipeType,
                         String result, int count, String input, Voltage v, long workTicks) {
        process(recipeType, result, count, input, 1, v, workTicks);
    }

    public MaterialBuilder<P> simpleProcess(Voltage v) {
        process(WIREMILL, "wire", 2, "ingot", v, 48L);
        process(BENDER, "plate", 1, "ingot", v, 72L);
        process(BENDER, "foil", 2, "plate", v, 64L);
        process(POLARIZER, "magnetic", 1, "stick", v, 40L);
        process(LATHE, "stick", 1, "ingot", v, 64L);
        process(LATHE, "screw", 1, "bolt", v, 16L);
        process(CUTTER, "bolt", 4, "stick", 2, v, 128L);
        return this;
    }

    public MaterialBuilder<P> machineProcess(Voltage v) {
        return simpleProcess(v);
    }

    public MaterialBuilder<P> smelt() {
        smelt("ingot", "dust");
        smelt("nugget", "dust_tiny");
        return this;
    }

    public MaterialBuilder<P> smelt(MaterialSet to) {
        dataGen.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(material.tag("dust")), to.item("ingot"), 0, 200)
                .unlockedBy("has_material", has(material.tag("dust"))), "_from_" + material.name);
        return this;
    }

    public MaterialBuilder<P> alloy(Voltage voltage, Object... components) {
        var alloyCount = 0;
        var totalCount = 0;
        var i = 0;

        if (components[0] instanceof Integer k) {
            alloyCount = k;
            i = 1;
        }

        var builder = ALLOY_SMELTER.recipe(dataGen, material.loc("ingot"))
                .voltage(voltage);
        for (; i < components.length; i += 2) {
            var component = (MaterialSet) components[i];
            var count = (int) components[i + 1];
            builder.inputItem(0, component.tag("dust"), count);
            totalCount += count;
        }
        if (alloyCount == 0) {
            alloyCount = totalCount;
        }
        builder.outputItem(1, material.entry("ingot"), alloyCount)
                .workTicks(100L * totalCount)
                .build();
        return this;
    }

    private OreRecipeBuilder oreBuilder(MaterialSet... byproduct) {
        return new OreRecipeBuilder(byproduct);
    }

    public MaterialBuilder<P>
    oreProcess(int amount, MaterialSet... byproduct) {
        return oreBuilder(byproduct)
                .amount(amount)
                .build();
    }

    public MaterialBuilder<P>
    siftingOreProcess(int amount, MaterialSet... byproduct) {
        return oreBuilder(byproduct)
                .sifting()
                .amount(amount)
                .build();
    }

    public MaterialBuilder<P>
    oreProcess(MaterialSet... byproduct) {
        return oreBuilder(byproduct).build();
    }

    public MaterialBuilder<P>
    primitiveOreProcess(MaterialSet... byproduct) {
        return oreBuilder(byproduct).primitive().build();
    }

    private <U extends Item> Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    toolModel(String sub) {
        var category = sub.substring("tool/".length());
        var handle = Optional.ofNullable(TOOL_HANDLE_TEX.get(category))
                .map(MaterialBuilder::toolTex)
                .orElse(VOID_TEX);
        var head = gregtech("items/tools/" + category);
        return basicItem(handle, head);
    }

    private void newItem(String sub, TagKey<Item> tag, Supplier<? extends Item> entry) {
        assert icon != null;
        var builder = dataGen.item(material.loc(sub), entry).tag(tag);
        if (sub.startsWith("tool/")) {
            builder = builder.model(toolModel(sub));
        } else if (sub.equals("wire")) {
            builder = builder.model(Models::wireItem);
        } else if (sub.equals("pipe")) {
            builder = builder.model(Models::pipeItem);
        } else if (sub.equals("raw")) {
            builder = builder.model(basicItem(modLoc("items/material/raw")));
        } else {
            builder = builder.model(icon.itemModel(sub));
        }
        builder.build();
    }

    private void buildItem(String sub) {
        var prefixTag = AllMaterials.tag(sub);
        var tag = material.tag(sub);
        dataGen.tag(tag, prefixTag);

        if (material.isAlias(sub)) {
            // do nothing for alias
            return;
        }

        if (material.hasTarget(sub)) {
            // simply add tag for existing tag
            dataGen.tag(material.target(sub), tag);
            return;
        }

        var entry = material.entry(sub);
        if (entry instanceof RegistryEntry<?>) {
            // build item data for new item
            newItem(sub, tag, entry);
        } else {
            // simple add tag for existing item
            dataGen.tag(entry, tag);
        }
    }

    private void buildOre() {
        var variant = material.oreVariant();
        dataGen.block(material.blockLoc("ore"), material.blockEntry("ore"))
                .blockState(oreBlock(variant))
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .tag(variant.mineTier.getTag())
                .drop(material.entry("raw"))
                .build();
    }

    @SuppressWarnings("unchecked")
    private RecipeBuilder shapedProcess(String result, int count, String[] patterns, Object[] args) {
        var builder = ShapedRecipeBuilder.shaped(material.item(result), count);
        TagKey<Item> unlock = null;
        for (var pat : patterns) {
            builder.pattern(pat);
        }
        for (var i = 0; i < args.length; i++) {
            var input = args[i];
            var key = (char) ('A' + i);
            if (input instanceof String sub) {
                var tag = material.tag(sub);
                if (unlock == null) {
                    unlock = tag;
                }
                builder.define(key, tag);
            } else if (input instanceof TagKey<?> tag) {
                if (unlock == null) {
                    unlock = (TagKey<Item>) tag;
                }
                builder.define(key, (TagKey<Item>) tag);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (unlock == null) {
            throw new IllegalArgumentException();
        }
        return builder.unlockedBy("has_material", has(unlock));
    }

    @SuppressWarnings("unchecked")
    private void toolProcess(String result, int count, String[] patterns, int size, Object[] args) {
        var builder = TOOL_CRAFTING.recipe(dataGen, material.loc(result))
                .result(material.entry(result), count);
        for (var pat : patterns) {
            builder.pattern(pat);
        }
        for (var i = 0; i < size; i++) {
            var input = args[i];
            var key = (char) ('A' + i);
            if (input instanceof String sub) {
                builder.define(key, material.tag(sub));
            } else if (input instanceof TagKey<?> tag) {
                builder.define(key, (TagKey<Item>) tag);
            } else {
                throw new IllegalArgumentException();
            }
        }
        for (var i = size; i < args.length; i++) {
            var material = args[i];
            if (material instanceof TagKey<?> tag) {
                builder.toolTag((TagKey<Item>) tag);
            } else {
                throw new IllegalArgumentException();
            }
        }
        builder.build();
    }

    private void process(String result, int count, String pattern, Object... args) {
        if (!material.hasItem(result)) {
            return;
        }
        if (Arrays.stream(args).anyMatch(o -> o instanceof String s &&
                !material.hasItem(s))) {
            return;
        }
        var patterns = pattern.split("\n");
        var materialCount = 1 + pattern.chars()
                .filter(x -> x >= 'A' && x <= 'Z')
                .map(x -> x - 'A')
                .max().orElse(-1);
        if (args.length > materialCount) {
            toolProcess(result, count, patterns, materialCount, args);
        } else {
            dataGen.vanillaRecipe(() -> shapedProcess(result, count, patterns, args));
        }
    }

    private void process(String result, int count, String input, TagKey<Item> tool) {
        process(result, count, "A", input, tool);
    }

    private void smelt(String output, String input) {
        if (!material.hasItem(output) || !material.hasItem(input)) {
            return;
        }
        dataGen.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(material.tag(input)), material.item(output), 0, 200)
                .unlockedBy("has_material", has(material.tag(input))));
    }

    private void toolRecipe(String sub, String pattern, Object... args) {
        process("tool/" + sub, 1, pattern, args);
    }

    private class OreRecipeBuilder {
        private int amount = 1;
        private boolean primitive = false;
        private boolean sifting = material.hasItem("gem");
        private final OreVariant variant;
        private final Supplier<? extends Item> byproduct0, byproduct1, byproduct2;

        private Supplier<? extends Item> getByProduct(MaterialSet[] byproduct, int index) {
            if (byproduct.length == 0) {
                return material.entry("dust");
            } else if (index < byproduct.length) {
                return byproduct[index].entry("dust");
            } else {
                return byproduct[0].entry("dust");
            }
        }

        public OreRecipeBuilder(MaterialSet[] byproduct) {
            this.byproduct0 = getByProduct(byproduct, 0);
            this.byproduct1 = getByProduct(byproduct, 1);
            this.byproduct2 = getByProduct(byproduct, 2);
            this.variant = material.oreVariant();
        }

        public OreRecipeBuilder amount(int value) {
            this.amount = value;
            return this;
        }

        public OreRecipeBuilder primitive() {
            this.primitive = true;
            return this;
        }

        public OreRecipeBuilder sifting() {
            this.sifting = true;
            return this;
        }

        private void crush(String output, String input) {
            MACERATOR.recipe(dataGen, suffix(material.loc(output), "_from_centrifuged"))
                    .inputItem(0, material.tag(input), 1)
                    .outputItem(1, material.entry(output), input.equals("raw") ? 2 * amount : 1)
                    .voltage(Voltage.LV)
                    .workTicks((long) (variant.destroyTime * 40f))
                    .build();
        }

        private void wash(String output, String input) {
            var loc = material.loc(output);
            if (input.equals("dust_pure")) {
                loc = suffix(loc, "_from_pure");
            }
            var builder = ORE_WASHER.recipe(dataGen, loc)
                    .inputItem(0, material.tag(input), 1)
                    .outputItem(2, material.entry(output), 1);
            if (input.equals("crushed")) {
                builder.inputFluid(1, Fluids.WATER, 1000)
                        .outputItem(3, STONE.entry("dust"), 1)
                        .outputItem(4, byproduct0, 1, 0.3)
                        .workTicks(200);
            } else {
                builder.inputFluid(1, Fluids.WATER, 100).workTicks(32);
            }
            var voltage = primitive && input.equals("dust_impure") ?
                    Voltage.PRIMITIVE : Voltage.ULV;
            builder.voltage(voltage).build();
        }

        public MaterialBuilder<P> build() {
            if (primitive || variant.voltage.rank <= Voltage.ULV.rank) {
                if (sifting) {
                    process("primary", amount, "raw", TOOL_HAMMER);
                } else {
                    process("crushed", amount, "raw", TOOL_HAMMER);
                }
                process("dust_pure", 1, "crushed_purified", TOOL_HAMMER);
                process("dust_impure", 1, "crushed", TOOL_HAMMER);
            }

            crush("crushed", "raw");
            crush("dust_impure", "crushed");
            crush("dust_pure", "crushed_purified");
            crush("dust", "crushed_centrifuged");
            wash("crushed_purified", "crushed");
            wash("dust", "dust_impure");
            wash("dust", "dust_pure");

            CENTRIFUGE.recipe(dataGen, material.loc("dust"))
                    .inputItem(0, material.tag("dust_pure"), 1)
                    .outputItem(2, material.entry("dust"), 1)
                    .outputItem(2, byproduct1, 1, 0.3)
                    .voltage(Voltage.LV)
                    .workTicks(80)
                    .build();

            THERMAL_CENTRIFUGE.recipe(dataGen, material.loc("crushed_centrifuged"))
                    .inputItem(0, material.tag("crushed_purified"), 1)
                    .outputItem(1, material.entry("crushed_centrifuged"), 1)
                    .outputItem(1, byproduct2, 1, 0.4)
                    .build();

            // TODO: sifting

            return MaterialBuilder.this;
        }
    }

    private void toolRecipes() {
        toolRecipe("hammer", "AA \nAAB\nAA ", "primary", TOOL_HANDLE);
        toolRecipe("mortar", " A \nBAB\nBBB", "primary", ItemTags.STONE_TOOL_MATERIALS);
        toolRecipe("file", "A\nA\nB", "plate", TOOL_HANDLE);
        toolRecipe("saw", "AAB\n  B", "plate", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
        toolRecipe("screwdriver", "  A\n A \nB  ", "stick", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
        toolRecipe("wrench", "A A\n A \n A ", "plate", TOOL_HAMMER);
        toolRecipe("wire_cutter", "A A\n A \nBCB", "plate", TOOL_HANDLE, TOOL_SCREW,
                TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
    }

    @Override
    protected void register() {
        for (var sub : material.itemSubs()) {
            buildItem(sub);
        }
        if (material.hasBlock("ore")) {
            buildOre();
        }

        if (material.hasItem("dust") && material.hasItem("dust_tiny")) {
            dataGen.vanillaRecipe(() -> ShapelessRecipeBuilder
                    .shapeless(material.item("dust_tiny"), 9)
                    .requires(material.tag("dust"))
                    .unlockedBy("has_dust", has(material.tag("dust"))));
            dataGen.vanillaRecipe(() -> ShapelessRecipeBuilder
                    .shapeless(material.item("dust"))
                    .requires(Ingredient.of(material.tag("dust_tiny")), 9)
                    .unlockedBy("has_dust_small", has(material.tag("dust_tiny"))));
        }
        toolRecipes();
    }
}
