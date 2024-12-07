package org.shsts.tinactory.datagen.content.builder;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.shsts.tinactory.datagen.builder.DataBuilder;
import org.shsts.tinactory.datagen.content.Models;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.datagen.api.IDataGen;
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.BENDER;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.CUTTER;
import static org.shsts.tinactory.content.AllRecipes.EXTRACTOR;
import static org.shsts.tinactory.content.AllRecipes.EXTRUDER;
import static org.shsts.tinactory.content.AllRecipes.FLUID_SOLIDIFIER;
import static org.shsts.tinactory.content.AllRecipes.LATHE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.MIXER;
import static org.shsts.tinactory.content.AllRecipes.ORE_WASHER;
import static org.shsts.tinactory.content.AllRecipes.POLARIZER;
import static org.shsts.tinactory.content.AllRecipes.SIFTER;
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
import static org.shsts.tinactory.datagen.DataGen._DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.VOID_TEX;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.oreBlock;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialBuilder<P> extends DataBuilder<P, MaterialBuilder<P>> {
    private static final Logger LOGGER = LogUtils.getLogger();

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
    private boolean hasProcess = false;
    private boolean hasOreProcess = false;

    public MaterialBuilder(IDataGen dataGen, P parent, MaterialSet material) {
        super(dataGen, parent, material.name);
        this.material = material;
    }

    public MaterialBuilder<P> icon(IconSet value) {
        icon = value;
        return this;
    }

    public MaterialBuilder<P> toolProcess(double factor) {
        // grind dust
        process("dust", 1, "primary", TOOL_MORTAR);
        process("dust_tiny", 1, "nugget", TOOL_MORTAR);
        // plate
        process("plate", 1, "A\nA", "ingot", TOOL_HAMMER);
        // foil
        process("foil", 2, "plate", TOOL_HAMMER);
        // ring
        process("ring", 1, "stick", TOOL_HAMMER);
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
        return machineProcess(Voltage.LV, factor);
    }

    public MaterialBuilder<P> toolProcess() {
        return toolProcess(1d);
    }

    private class MachineProcessBuilder {
        private final Voltage voltage;
        private final double factor;

        public MachineProcessBuilder(Voltage voltage, double factor) {
            this.voltage = voltage;
            this.factor = factor;
        }

        private long ticks(long ticks) {
            return Math.round(ticks * factor);
        }

        private void process(RecipeTypeEntry<?, ? extends ProcessingRecipe.BuilderBase<?, ?>> recipeType,
            String result, int count, String input, long workTicks) {
            if (!material.hasItem(result) || !material.hasItem(input)) {
                return;
            }
            recipeType.recipe(_DATA_GEN, material.loc(result))
                .outputItem(1, material.entry(result), count)
                .inputItem(0, material.tag(input), 1)
                .voltage(voltage)
                .workTicks(ticks(workTicks))
                .build();
        }

        private void assemble(String sub, long workTicks, boolean soldering, Object... inputs) {
            if (!material.hasItem(sub)) {
                return;
            }
            for (var input : inputs) {
                if (input instanceof String sub1 && !material.hasItem(sub1)) {
                    return;
                }
            }

            var i = 0;
            var k = 1;
            if (inputs.length > 0 && inputs[0] instanceof Integer k1) {
                k = k1;
                i = 1;
            }
            var builder = ASSEMBLER.recipe(_DATA_GEN, material.loc(sub))
                .outputItem(2, material.entry(sub), k)
                .voltage(voltage)
                .workTicks(ticks(workTicks));

            if (soldering) {
                builder.inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(0.5f));
            }

            for (; i < inputs.length; i++) {
                var sub1 = (String) inputs[i];
                k = 1;
                if (inputs.length > i + 1 && inputs[i + 1] instanceof Integer k1) {
                    k = k1;
                    i++;
                }
                builder.inputItem(0, material.tag(sub1), k);
            }

            builder.build();
        }

        private void macerate(String sub, String result, int amount) {
            if (!material.hasItem(result) || !material.hasItem(sub)) {
                return;
            }
            if (material.loc(sub).equals(material.loc(result))) {
                return;
            }
            MACERATOR.recipe(_DATA_GEN, suffix(material.loc(result), "_from_" + sub))
                .outputItem(1, material.entry(result), amount)
                .inputItem(0, material.tag(sub), 1)
                .voltage(voltage)
                .workTicks(ticks(128L))
                .build();
        }

        private void macerate(String sub, int amount) {
            macerate(sub, "dust", amount);
        }

        private void macerate(String sub) {
            macerate(sub, "dust", 1);
        }

        private void macerateTiny(String sub, int amount) {
            macerate(sub, "dust_tiny", amount);
        }

        private void macerate() {
            macerate("primary");
            macerateTiny("nugget", 1);
            macerate("magnetic");
            macerateTiny("wire", 4);
            macerateTiny("wire_fine", 1);
            macerateTiny("ring", 2);
            macerate("plate");
            macerateTiny("foil", 2);
            macerateTiny("stick", 4);
            macerateTiny("screw", 1);
            macerateTiny("bolt", 1);
            macerate("gear");
            macerate("rotor", 4);
            macerate("pipe", 3);
            macerate("gem_flawless", 8);
            macerate("gem_exquisite", 16);
        }

        private void molten(String sub, Voltage v, float amount, boolean solidifier) {
            if (!material.hasItem(sub) || !material.hasFluid()) {
                return;
            }
            var fluid = material.fluidEntry();

            EXTRACTOR.recipe(_DATA_GEN, suffix(material.fluidLoc(), "_from_" + sub))
                .outputFluid(2, fluid, material.fluidAmount(amount))
                .inputItem(0, material.tag(sub), 1)
                .voltage(v)
                .workTicks(ticks(160L))
                .build();

            if (solidifier) {
                FLUID_SOLIDIFIER.recipe(_DATA_GEN, material.loc(sub))
                    .outputItem(1, material.entry(sub), 1)
                    .inputFluid(0, fluid, material.fluidAmount(amount))
                    .voltage(v)
                    .workTicks(ticks(80L))
                    .build();
            }
        }

        private void molten(String sub, Voltage v, float amount) {
            molten(sub, v, amount, true);
        }

        private void molten() {
            var v = material.hasItem("sheet") ? voltage : Voltage.fromRank(voltage.rank + 1);

            molten("primary", v, 1f);
            molten("nugget", v, 1f / 9f);
            molten("magnetic", v, 0.5f, false);
            molten("wire", v, 0.5f, false);
            molten("wire_fine", v, 0.125f, false);
            molten("ring", v, 0.25f);
            molten("plate", v, 1f);
            molten("foil", v, 0.25f, false);
            molten("stick", v, 0.5f);
            molten("screw", v, 1f / 9f, false);
            molten("bolt", v, 0.125f);
            molten("gear", v, 2f);
            molten("rotor", v, 4.25f);
            molten("pipe", v, 3f);
        }

        private void extrude(String target, int outCount, int inCount) {
            if (material.hasItem(target) && material.hasItem("ingot")) {
                EXTRUDER.recipe(_DATA_GEN, material.loc(target))
                    .outputItem(1, material.entry(target), outCount)
                    .inputItem(0, material.tag("ingot"), inCount)
                    .voltage(Voltage.fromRank(voltage.rank + 1))
                    .workTicks(ticks(96L))
                    .build();
            }
        }

        private void extrude() {
            extrude("stick", 2, 1);
            extrude("plate", 1, 1);
            extrude("sheet", 1, 1);
            extrude("foil", 4, 1);
            extrude("ring", 4, 1);
            extrude("wire", 2, 1);
            extrude("bolt", 8, 1);
            extrude("gear", 1, 2);
            extrude("rotor", 1, 5);
            extrude("pipe", 1, 3);
        }

        public MaterialBuilder<P> build() {
            process(POLARIZER, "magnetic", 1, "stick", 40L);
            process(WIREMILL, "wire", 2, "ingot", 48L);
            process(WIREMILL, "wire_fine", 4, "wire", 64L);
            process(WIREMILL, "ring", 1, "stick", 64L);
            process(BENDER, "plate", 1, "ingot", 72L);
            process(BENDER, "foil", 4, "plate", 40L);
            process(LATHE, "stick", 1, "ingot", 64L);
            process(LATHE, "screw", 1, "bolt", 16L);
            process(LATHE, "lens", 1, "gem_exquisite", 600L);

            if (material.hasItem("bolt") && material.hasItem("stick")) {
                CUTTER.recipe(_DATA_GEN, material.loc("bolt"))
                    .outputItem(2, material.entry("bolt"), 4)
                    .inputItem(0, material.tag("stick"), 1)
                    .inputFluid(1, Fluids.WATER, 5)
                    .voltage(voltage)
                    .workTicks(128L)
                    .build();
            }

            if (material.hasItem("gem_flawless") && material.hasItem("gem")) {
                CUTTER.recipe(_DATA_GEN, material.loc("gem"))
                    .outputItem(2, material.entry("gem"), 8)
                    .inputItem(0, material.tag("gem_flawless"), 1)
                    .inputFluid(1, Fluids.WATER, 80)
                    .voltage(voltage)
                    .workTicks(480L)
                    .build();
            }

            assemble("gear", 128L, true, "plate", "stick", 2);
            assemble("rotor", 160L, true, "plate", 4, "ring", 1);
            assemble("pipe", 120L, true, "plate", 3);
            assemble("gem_exquisite", 400L, false, "gem_flawless", "gem", 4, "dust", 4);

            macerate();
            molten();
            extrude();

            hasProcess = true;
            return MaterialBuilder.this;
        }
    }

    private MachineProcessBuilder processBuilder(Voltage v, double factor) {
        return new MachineProcessBuilder(v, factor);
    }

    public MaterialBuilder<P> machineProcess(Voltage v, double factor) {
        return processBuilder(v, factor).build();
    }

    public MaterialBuilder<P> machineProcess(Voltage v) {
        return machineProcess(v, 1d);
    }

    public MaterialBuilder<P> smelt() {
        smelt("ingot", "dust");
        smelt("nugget", "dust_tiny");
        return this;
    }

    public MaterialBuilder<P> smelt(MaterialSet to) {
        xDataGen.vanillaRecipe(() -> SimpleCookingRecipeBuilder
            .smelting(Ingredient.of(material.tag("dust")), to.item("ingot"), 0, 200)
            .unlockedBy("has_material", has(material.tag("dust"))), "_from_" + material.name);
        return this;
    }

    public MaterialBuilder<P> blast(Voltage v, int temperature, long ticks, Object... extra) {
        var source = material;
        var suffix = "";
        if (extra.length > 0 && extra[0] instanceof MaterialSet mat) {
            source = mat;
            suffix = "_from_" + source.name;
        }
        BLAST_FURNACE.recipe(xDataGen, suffix(material.loc("ingot"), suffix))
            .outputItem(2, material.entry("ingot"), 1)
            .inputItem(0, source.tag("dust"), 1)
            .voltage(v)
            .temperature(temperature)
            .workTicks(ticks)
            .build();
        return this;
    }

    private <U extends ProcessingRecipe, B extends ProcessingRecipe.BuilderBase<U, B>> void compose(
        Voltage v, RecipeTypeEntry<U, B> recipeType, int outputPort,
        boolean decompose, long workTicks, String output, Object... components) {
        var loc = output.equals("fluid") ? material.fluidLoc() : material.loc(output);

        var alloyCount = 0;
        var totalCount = 0;
        var i = 0;

        if (components[0] instanceof Integer k) {
            alloyCount = k;
            i = 1;
        }

        var builder = recipeType.recipe(xDataGen, loc)
            .voltage(v);

        for (; i < components.length; i += 2) {
            var component = (MaterialSet) components[i];
            var sub = component.hasItem("dust") ? "dust" : "fluid";
            if (components[i] instanceof String sub1) {
                sub = sub1;
                i++;
            }
            var count = (int) components[i + 1];

            if (sub.equals("fluid")) {
                if (decompose) {
                    builder.outputFluid(outputPort + 1, component.fluidEntry(), component.fluidAmount(count));
                } else {
                    builder.inputFluid(1, component.fluidEntry(), component.fluidAmount(count));
                }
            } else {
                if (decompose) {
                    builder.outputItem(outputPort, component.item(sub), count);
                } else {
                    builder.inputItem(0, component.tag(sub), count);
                }
            }

            totalCount += count;
        }
        if (alloyCount == 0) {
            alloyCount = totalCount;
        }

        if (output.equals("fluid")) {
            if (decompose) {
                builder.inputFluid(1, material.fluidEntry(), material.fluidAmount(alloyCount));
            } else {
                builder.outputFluid(outputPort + 1, material.fluidEntry(), material.fluidAmount(alloyCount));
            }
        } else {
            if (decompose) {
                builder.inputItem(0, material.tag(output), alloyCount);
            } else {
                builder.outputItem(outputPort, material.entry(output), alloyCount);
            }
        }

        builder.workTicks(workTicks * totalCount).build();
    }

    public MaterialBuilder<P> decompose(Voltage voltage, Object... components) {
        compose(voltage, CENTRIFUGE, 2, true, 60L, "dust", components);
        return this;
    }

    public MaterialBuilder<P> mix(Voltage voltage, Object... components) {
        compose(voltage, MIXER, 2, false, 20L, "dust", components);
        return decompose(voltage, components);
    }

    public MaterialBuilder<P> alloyOnly(Voltage voltage, Object... components) {
        compose(voltage, ALLOY_SMELTER, 1, false, 40L, "ingot", components);
        return this;
    }

    public MaterialBuilder<P> alloy(Voltage voltage, Object... components) {
        return alloyOnly(voltage, components)
            .mix(voltage.rank < Voltage.LV.rank ? Voltage.LV : voltage, components);
    }

    public MaterialBuilder<P> fluidAlloy(Voltage voltage, Object... components) {
        compose(voltage, ALLOY_SMELTER, 1, false, 40L, "fluid", components);
        return this;
    }

    public OreRecipeBuilder oreBuilder(MaterialSet... byproduct) {
        return new OreRecipeBuilder(byproduct);
    }

    public MaterialBuilder<P> oreProcess(int amount, MaterialSet... byproduct) {
        return oreBuilder(byproduct)
            .amount(amount)
            .build();
    }

    public MaterialBuilder<P> oreProcess(MaterialSet... byproduct) {
        return oreBuilder(byproduct).build();
    }

    public MaterialBuilder<P> primitiveOreProcess(MaterialSet... byproduct) {
        return oreBuilder(byproduct).primitive().build();
    }

    private <U extends Item> Consumer<IEntryDataContext<Item, U, ItemModelProvider>> toolModel(String sub) {
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
        if (entry instanceof IEntry<? extends Item>) {
            // build item data for new item
            newItem(sub, tag, entry);
        } else {
            // simple add tag for existing item
            dataGen.tag(entry, tag);
        }
    }

    private void buildOre() {
        var variant = material.oreVariant();
        var tierTag = variant.mineTier.getTag();
        assert tierTag != null;
        dataGen.block(material.blockLoc("ore"), material.blockEntry("ore"))
            .blockState(oreBlock(variant))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(tierTag)
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
        var builder = TOOL_CRAFTING.recipe(xDataGen, material.loc(result))
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
            xDataGen.vanillaRecipe(() -> shapedProcess(result, count, patterns, args));
        }
    }

    private void process(String result, int count, String input, TagKey<Item> tool) {
        process(result, count, "A", input, tool);
    }

    private void smelt(String output, String input) {
        if (!material.hasItem(output) || !material.hasItem(input)) {
            return;
        }
        xDataGen.vanillaRecipe(() -> SimpleCookingRecipeBuilder
            .smelting(Ingredient.of(material.tag(input)), material.item(output), 0, 200)
            .unlockedBy("has_material", has(material.tag(input))));
    }

    private void toolRecipe(String sub, String pattern, Object... args) {
        process("tool/" + sub, 1, pattern, args);
    }

    public class OreRecipeBuilder {
        private int amount = 1;
        private boolean primitive = false;
        private boolean hammerPrimary = false;
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

        public OreRecipeBuilder siftAndHammer() {
            this.hammerPrimary = true;
            return this;
        }

        private void crush(String output, String input) {
            MACERATOR.recipe(xDataGen, suffix(material.loc(output), "_from_centrifuged"))
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
            var builder = ORE_WASHER.recipe(xDataGen, loc)
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
                if (material.hasItem("gem")) {
                    hammerPrimary = true;
                } else if (!hammerPrimary) {
                    process("crushed", amount, "raw", TOOL_HAMMER);
                }
                process("dust_pure", 1, "crushed_purified", TOOL_HAMMER);
                process("dust_impure", 1, "crushed", TOOL_HAMMER);
            }

            if (hammerPrimary) {
                process("primary", amount, "raw", TOOL_HAMMER);
            }

            crush("crushed", "raw");
            crush("dust_impure", "crushed");
            crush("dust_pure", "crushed_purified");
            crush("dust", "crushed_centrifuged");
            wash("crushed_purified", "crushed");
            wash("dust", "dust_impure");
            wash("dust", "dust_pure");

            CENTRIFUGE.recipe(xDataGen, material.loc("dust_pure"))
                .inputItem(0, material.tag("dust_pure"), 1)
                .outputItem(2, material.entry("dust"), 1)
                .outputItem(2, byproduct1, 1, 0.3d)
                .voltage(Voltage.LV)
                .workTicks(80)
                .build();

            THERMAL_CENTRIFUGE.recipe(xDataGen, material.loc("crushed_centrifuged"))
                .inputItem(0, material.tag("crushed_purified"), 1)
                .outputItem(1, material.entry("crushed_centrifuged"), 1)
                .outputItem(1, byproduct2, 1, 0.4d)
                .build();

            if (material.hasItem("gem")) {
                SIFTER.recipe(xDataGen, material.loc("crushed_purified"))
                    .inputItem(0, material.tag("crushed_purified"), 1)
                    .outputItem(1, material.entry("gem_flawless"), 1, 0.1d)
                    .outputItem(1, material.entry("gem"), 1, 0.35d)
                    .outputItem(1, material.entry("dust_pure"), 1, 0.65d)
                    .voltage(Voltage.LV)
                    .workTicks(600L)
                    .build();
            } else if (hammerPrimary) {
                SIFTER.recipe(xDataGen, material.loc("crushed_purified"))
                    .inputItem(0, material.tag("crushed_purified"), 1)
                    .outputItem(1, material.entry("primary"), 1, 0.8d)
                    .outputItem(1, material.entry("primary"), 1, 0.35d)
                    .outputItem(1, material.entry("dust_pure"), 1, 0.65d)
                    .voltage(Voltage.LV)
                    .workTicks(400L)
                    .build();
            }

            hasOreProcess = true;
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
            xDataGen.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(material.item("dust_tiny"), 9)
                .requires(material.tag("dust"))
                .unlockedBy("has_dust", has(material.tag("dust"))));
            xDataGen.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(material.item("dust"))
                .requires(Ingredient.of(material.tag("dust_tiny")), 9)
                .unlockedBy("has_dust_small", has(material.tag("dust_tiny"))));
        }
        toolRecipes();

        if (material.hasItem("primary") && !hasProcess) {
            LOGGER.warn("{} does not have process", material);
        }
        if (material.hasBlock("ore") && !hasOreProcess) {
            LOGGER.warn("{} does not have ore process", material);
        }
    }
}
