package org.shsts.tinactory.content.material;

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
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.CableModel;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.ORE_WASHER;
import static org.shsts.tinactory.content.AllRecipes.THERMAL_CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.TOOL;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.TOOL_FILE;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_HANDLE;
import static org.shsts.tinactory.content.AllTags.TOOL_MORTAR;
import static org.shsts.tinactory.content.AllTags.TOOL_SAW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
    public final String name;
    public final int color;
    private final List<Consumer<MaterialSet>> callbacks;

    private record Entry(ResourceLocation loc, TagKey<Item> tag, Supplier<Item> item) {
        public Item getItem() {
            return getEntry().get();
        }

        public Supplier<Item> getEntry() {
            assert item != null;
            return item;
        }
    }

    private final Map<String, Entry> items;
    private final Map<String, Supplier<Block>> blocks;
    @Nullable
    private final OreVariant oreVariant;

    private MaterialSet(Builder<?> builder) {
        this.name = builder.name;
        this.color = builder.color;
        this.items = builder.items;
        this.callbacks = builder.callbacks;
        this.blocks = builder.blocks;
        this.oreVariant = builder.oreVariant;
    }

    public TagKey<Item> tag(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).tag;
    }

    public ResourceLocation loc(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).loc;
    }

    public Item item(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).getItem();
    }

    public Supplier<Item> entry(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).item();
    }

    public OreVariant oreVariant() {
        assert oreVariant != null;
        return oreVariant;
    }

    public Block block(String sub) {
        assert blocks.containsKey(sub);
        return blocks.get(sub).get();
    }

    @SuppressWarnings("unchecked")
    private RecipeBuilder shapedProcess(String result, int count, String[] patterns, Object[] args) {
        var builder = ShapedRecipeBuilder.shaped(item(result), count);
        TagKey<Item> unlock = null;
        for (var pat : patterns) {
            builder.pattern(pat);
        }
        for (var i = 0; i < args.length; i++) {
            var material = args[i];
            var key = (char) ('A' + i);
            if (material instanceof String sub) {
                var tag = tag(sub);
                if (unlock == null) {
                    unlock = tag;
                }
                builder.define(key, tag);
            } else if (material instanceof TagKey<?> tag) {
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
        return builder.unlockedBy("has_material", AllRecipes.has(unlock));
    }

    @SuppressWarnings("unchecked")
    private void toolProcess(String result, int count, String[] patterns, int materialCount, Object[] args) {
        var builder = TOOL.recipe(loc(result))
                .result(entry(result), count);
        for (var pat : patterns) {
            builder.pattern(pat);
        }
        for (var i = 0; i < materialCount; i++) {
            var material = args[i];
            var key = (char) ('A' + i);
            if (material instanceof String sub) {
                builder.define(key, tag(sub));
            } else if (material instanceof TagKey<?> tag) {
                builder.define(key, (TagKey<Item>) tag);
            } else {
                throw new IllegalArgumentException();
            }
        }
        for (var i = materialCount; i < args.length; i++) {
            var material = args[i];
            if (material instanceof TagKey<?> tag) {
                builder.toolTag((TagKey<Item>) tag);
            } else {
                throw new IllegalArgumentException();
            }
        }
        builder.build();
    }

    private void process(String result, int count, String pattern, Object[] args) {
        if (!items.containsKey(result)) {
            return;
        }
        if (Arrays.stream(args).anyMatch(o -> o instanceof String s && !items.containsKey(s))) {
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
            REGISTRATE.vanillaRecipe(() -> shapedProcess(result, count, patterns, args));
        }
    }

    private MaterialSet smelt(String output, String input) {
        if (!items.containsKey(output) || !items.containsKey(input)) {
            return this;
        }
        REGISTRATE.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(tag(input)), item(output), 0, 200)
                .unlockedBy("has_material", has(tag(input))));
        return this;
    }

    private void smelt(Supplier<Item> item) {
        REGISTRATE.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(tag("dust")), item.get(), 0, 200)
                .unlockedBy("has_material", has(tag("dust"))), "_from_" + name);
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.accept(this);
        }
        this.callbacks.clear();
    }

    public static class Builder<P> extends SimpleBuilder<MaterialSet, P, Builder<P>> {
        private final String name;
        private final Map<String, Entry> items = new HashMap<>();
        private final Map<String, Supplier<Block>> blocks = new HashMap<>();
        private final List<Consumer<MaterialSet>> callbacks = new ArrayList<>();
        private int color = 0xFFFFFFFF;
        @Nullable
        private IconSet icon = null;
        @Nullable
        private OreVariant oreVariant = null;

        public Builder(P parent, String name) {
            super(parent);
            this.name = name;
        }

        @Override
        protected MaterialSet createObject() {
            return new MaterialSet(this);
        }

        private static String prefix(String sub) {
            return sub.startsWith("tool/") ? sub : "materials/" + sub;
        }

        public static TagKey<Item> prefixTag(String sub) {
            return AllTags.modItem(prefix(sub));
        }

        private TagKey<Item> newTag(String sub) {
            return AllTags.modItem(prefix(sub) + "/" + name);
        }

        public Builder<P> icon(IconSet value) {
            icon = value;
            return this;
        }

        public Builder<P> color(int value) {
            color = value;
            return this;
        }

        private Entry put(String sub, ResourceLocation loc, Supplier<Item> item) {
            if (items.containsKey(sub)) {
                return items.get(sub);
            }
            var tag = newTag(sub);
            REGISTRATE.tag(item, tag);
            REGISTRATE.tag(tag, prefixTag(sub));
            var entry = new Entry(loc, tag, item);
            items.put(sub, entry);
            return entry;
        }

        private Entry put(String sub, Supplier<RegistryEntry<? extends Item>> item) {
            if (items.containsKey(sub)) {
                return items.get(sub);
            }
            var entry = item.get();
            return put(sub, entry.loc, entry::get);
        }

        public Builder<P> existing(String sub, Item item) {
            assert !items.containsKey(sub);
            var loc = item.getRegistryName();
            assert loc != null;
            put(sub, loc, () -> item);
            return this;
        }

        public Builder<P> existing(String sub, TagKey<Item> targetTag, Item item) {
            assert !items.containsKey(sub);
            var tag = newTag(sub);
            REGISTRATE.tag(targetTag, tag);
            REGISTRATE.tag(tag, prefixTag(sub));
            var loc = item.getRegistryName();
            assert loc != null;
            items.put(sub, new Entry(loc, tag, () -> item));
            return this;
        }

        public Builder<P> existing(String sub, Block block) {
            blocks.put(sub, () -> block);
            return this;
        }

        public Builder<P> alias(String sub, String sub2) {
            var entry = items.get(sub2);
            assert entry != null;
            items.put(sub, entry);
            REGISTRATE.tag(entry.tag, prefixTag(sub));
            return this;
        }

        private String newId(String sub) {
            var prefix = sub.startsWith("tool/") ? "" : "material/";
            return prefix + sub + "/" + name;
        }

        private void dummy(String sub) {
            assert icon != null;
            var model = icon.itemModel(sub);
            put(sub, () -> REGISTRATE.item(newId(sub), Item::new)
                    .model(model)
                    .tint(color)
                    .register());
        }

        private Builder<P> dummies(String... subs) {
            for (var sub : subs) {
                dummy(sub);
            }
            return this;
        }

        public Builder<P> dust() {
            return dummies("dust");
        }

        public Builder<P> dustSet() {
            dummies("dust", "dust_tiny");
            callbacks.add($ -> {
                REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                        .shapeless($.item("dust_tiny"), 9)
                        .requires($.tag("dust"))
                        .unlockedBy("has_dust", has($.tag("dust"))));
                REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                        .shapeless($.item("dust"))
                        .requires(Ingredient.of($.tag("dust_tiny")), 9)
                        .unlockedBy("has_dust_small", has($.tag("dust_tiny"))));
            });
            return this;
        }

        public Builder<P> metalSet() {
            return dustSet()
                    .dummies("ingot", "nugget")
                    .alias("primary", "ingot");
        }

        public Builder<P> metalSetExt() {
            return metalSet().dummies("plate", "stick");
        }

        public Builder<P> mechanicalSet() {
            return metalSetExt()
                    .dummies("bolt", "screw", "gear", "rotor");
        }

        public Builder<P> pipe() {
            put("pipe", () -> REGISTRATE.item(newId("pipe"), Item::new)
                    .model(CableModel::pipeModel)
                    .tint(color)
                    .register());
            return this;
        }

        public Builder<P> wire() {
            put("wire", () -> REGISTRATE.item(newId("wire"), Item::new)
                    .model(CableModel::wireModel)
                    .tint(color)
                    .register());
            return this;
        }

        public Builder<P> wireAndPlate() {
            return wire().dummies("plate");
        }

        public Builder<P> magnetic() {
            return dummies("magnetic");
        }

        public class OreBuilder extends SimpleBuilder<Unit, Builder<P>, OreBuilder> {
            private final int amount;
            private boolean primitive = false;
            private final OreVariant variant;
            private final Map<String, Supplier<Item>> byproducts = new HashMap<>();

            private OreBuilder(OreVariant variant, int amount) {
                super(Builder.this);
                this.variant = variant;
                this.amount = amount;
            }

            public OreBuilder primitive() {
                primitive = true;
                return this;
            }

            public OreBuilder byproduct(String key, Supplier<Item> b1) {
                byproducts.put(key, b1);
                return this;
            }

            public OreBuilder byproduct(Supplier<Item> b1, Supplier<Item> b2, Supplier<Item> b3) {
                byproducts.put("wash", b1);
                byproducts.put("centrifuge", b2);
                byproducts.put("thermal_centrifuge", b3);
                return this;
            }

            public OreBuilder byproduct(Supplier<Item> b1) {
                return byproduct(b1, b1, b1);
            }

            private void crush(String output, String input) {
                callbacks.add($ -> MACERATOR.recipe($.loc(output))
                        .inputItem(0, $.tag(input), 1)
                        .outputItem(1, $.entry(output), input.equals("raw") ? 2 * amount : 1)
                        .voltage(Voltage.LV)
                        .workTicks((long) (variant.destroyTime * 40f))
                        .build());
            }

            private void wash(String output, String input, Voltage voltage) {
                callbacks.add($ -> {
                    var loc = $.loc(output);
                    if (input.equals("dust_pure")) {
                        loc = new ResourceLocation(loc.getNamespace(), loc.getPath() + "_from_pure");
                    }
                    var builder = ORE_WASHER.recipe(loc)
                            .inputItem(0, $.tag(input), 1)
                            .outputItem(2, $.entry(output), 1);
                    if (input.equals("crushed")) {
                        var byproduct = byproducts.getOrDefault("wash", $.entry("dust"));
                        builder.outputItem(4, byproduct, 1, 0.1)
                                .workTicks(200);
                    } else {
                        builder.workTicks(32);
                    }
                    builder.voltage(voltage)
                            .build();
                });
            }

            @Override
            protected Unit createObject() {
                var raw = put("raw", () -> REGISTRATE.item(newId("raw"), Item::new)
                        .model(ModelGen.basicItem(ModelGen.modLoc("items/material/raw")))
                        .tint(color)
                        .register());

                dust().dummies("crushed", "crushed_centrifuged", "crushed_purified")
                        .dummies("dust_impure", "dust_pure");

                oreVariant = variant;
                if (!blocks.containsKey("ore")) {
                    var ore = REGISTRATE.block(newId("ore"), OreBlock.factory(variant))
                            .material(variant.baseBlock.defaultBlockState().getMaterial())
                            .properties(p -> p.strength(variant.destroyTime, variant.explodeResistance))
                            .transform(ModelGen.oreBlock(variant))
                            .tint(color)
                            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                            .tag(variant.mineTier.getTag())
                            .drop(raw::getItem)
                            .register();
                    blocks.put("ore", ore::get);
                }

                if (variant.voltage.rank <= Voltage.ULV.rank) {
                    Builder.this.process("crushed", amount, "raw", TOOL_HAMMER);
                    Builder.this.process("dust_pure", 1, "crushed_purified", TOOL_HAMMER);
                    Builder.this.process("dust_impure", 1, "crushed", TOOL_HAMMER);
                }

                crush("crushed", "raw");
                crush("dust_impure", "crushed");
                crush("dust_pure", "crushed_purified");
                crush("dust", "crushed_centrifuged");
                wash("crushed_purified", "crushed", Voltage.ULV);
                wash("dust", "dust_impure", primitive ? Voltage.PRIMITIVE : Voltage.ULV);
                wash("dust", "dust_pure", Voltage.ULV);
                callbacks.add($ -> {
                    var byproduct = byproducts.getOrDefault("centrifuge", $.entry("dust"));
                    CENTRIFUGE.recipe($.loc("dust"))
                            .inputItem(0, $.tag("dust_pure"), 1)
                            .outputItem(2, $.entry("dust"), 1)
                            .outputItem(2, byproduct, 1, 0.1)
                            .voltage(Voltage.LV)
                            .workTicks(80)
                            .build();
                });
                callbacks.add($ -> {
                    var byproduct = byproducts.getOrDefault("thermal_centrifuge", $.entry("dust"));
                    THERMAL_CENTRIFUGE.recipe($.loc("crushed_centrifuged"))
                            .inputItem(0, $.tag("crushed_purified"), 1)
                            .outputItem(1, $.entry("crushed_centrifuged"), 1)
                            .outputItem(1, byproduct, 1, 0.1)
                            .build();
                });

                return Unit.INSTANCE;
            }
        }

        public OreBuilder ore(OreVariant variant, int amount) {
            return new OreBuilder(variant, amount);
        }

        public OreBuilder ore(OreVariant variant) {
            return ore(variant, 1);
        }

        public Builder<P> alloy(Object... components) {
            callbacks.add($ -> {
                Voltage voltage = Voltage.LV;
                var i = 0;
                if (components[0] instanceof Voltage v) {
                    voltage = v;
                    i = 1;
                }
                var totalCount = 0;

                var builder = ALLOY_SMELTER.recipe($.loc("ingot"))
                        .workTicks(200)
                        .voltage(voltage);

                for (; i < components.length; i += 2) {
                    var component = (MaterialSet) components[i];
                    var count = (int) components[i + 1];
                    builder.inputItem(0, component.tag("dust"), count);
                    totalCount += count;
                }
                builder.outputItem(1, $.entry("ingot"), totalCount)
                        .build();
            });
            return this;
        }

        private void process(String result, int count, String pattern, Object... args) {
            callbacks.add($ -> $.process(result, count, pattern, args));
        }

        private void process(String result, int count, String input, TagKey<Item> tool) {
            process(result, count, "A", input, tool);
        }

        public Builder<P> toolProcess() {
            // grind dust
            process("dust", 1, "primary", TOOL_MORTAR);
            process("dust_tiny", 1, "nugget", TOOL_MORTAR);
            // plate
            process("plate", 1, "A\nA", "ingot", TOOL_HAMMER);
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

        public Builder<P> smelt() {
            callbacks.add($ -> $.smelt("ingot", "dust").smelt("nugget", "dust_tiny"));
            return this;
        }

        public Builder<P> smelt(Supplier<Item> to) {
            callbacks.add($ -> $.smelt(to));
            return this;
        }

        public class ToolBuilder extends SimpleBuilder<Unit, Builder<P>, ToolBuilder> {
            private final int durability;
            @Nullable
            private final Tier tier;

            private ToolBuilder(int durability, @Nullable Tier tier) {
                super(Builder.this);
                this.tier = tier;
                this.durability = durability;
            }

            private static ResourceLocation toolTex(String sub) {
                return ModelGen.gregtech("items/tools/" + sub);
            }

            private static final Map<String, String> TOOL_HANDLE_TEX = ImmutableMap.<String, String>builder()
                    .put("hammer", "handle_hammer")
                    .put("mortar", "mortar_base")
                    .put("file", "handle_file")
                    .put("saw", "handle_saw")
                    .put("screwdriver", "handle_screwdriver")
                    .put("wire_cutter", "wire_cutter_base")
                    .build();

            private String item(String category, Function<Item.Properties, ToolItem> factory) {
                var handle = Optional.ofNullable(TOOL_HANDLE_TEX.get(category))
                        .map(MaterialSet.Builder.ToolBuilder::toolTex)
                        .orElse(ModelGen.VOID_TEX);
                var head = ModelGen.gregtech("items/tools/" + category);
                var sub = "tool/" + category;
                put(sub, () -> REGISTRATE.item(newId(sub), factory)
                        .model(ModelGen.basicItem(handle, head))
                        .tint(0xFFFFFFFF, color)
                        .register());
                return sub;
            }

            private String toolItem(String category) {
                return item(category, p -> new ToolItem(p, durability));
            }

            private String usableItem(String category, TagKey<Block> blockTag) {
                assert tier != null;
                return item(category, p -> new UsableToolItem(p, durability, tier, blockTag));
            }

            private void tool(String category, String pattern, Object... args) {
                process(toolItem(category), 1, pattern, args);
            }

            private void usable(String category, TagKey<Block> blockTag, String pattern, Object... args) {
                process(usableItem(category, blockTag), 1, pattern, args);
            }

            public ToolBuilder hammer() {
                tool("hammer", "AA \nAAB\nAA ", "primary", TOOL_HANDLE);
                return this;
            }

            public ToolBuilder mortar() {
                tool("mortar", " A \nBAB\nBBB", "primary", ItemTags.STONE_TOOL_MATERIALS);
                return this;
            }

            public ToolBuilder basic() {
                tool("file", "A\nA\nB", "plate", TOOL_HANDLE);
                tool("saw", "AAB\n  B", "plate", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
                tool("screwdriver", "  A\n A \nB  ", "stick", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
                usable("wrench", MINEABLE_WITH_WRENCH, "A A\nAAA\n A ", "plate", TOOL_HAMMER);
                usable("wire_cutter", MINEABLE_WITH_CUTTER, "A A\n A \nBCB", "plate", TOOL_HANDLE, TOOL_SCREW,
                        TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
                return hammer().mortar();
            }

            @Override
            protected Unit createObject() {
                return Unit.INSTANCE;
            }
        }

        public ToolBuilder tool(int durability, Tier tier) {
            return new ToolBuilder(durability, tier);
        }

        public ToolBuilder tool(int durability) {
            return new ToolBuilder(durability, null);
        }
    }
}
