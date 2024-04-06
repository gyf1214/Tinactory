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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
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

    private MaterialSet(Builder<?> builder) {
        this.color = builder.color;
        this.items = builder.items;
        this.callbacks = builder.callbacks;
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
        return builder.unlockedBy("has_material", has(unlock));
    }

    @SuppressWarnings("unchecked")
    private void toolProcess(String result, int count, String[] patterns, int materialCount, Object[] args) {
        var builder = AllRecipes.TOOL.modRecipe(loc(result))
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

    private MaterialSet smelt(String output, String input, int time) {
        if (items.containsKey(output) || items.containsKey(input)) {
            return this;
        }
        REGISTRATE.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(tag(input)), item(output), 0, time)
                .unlockedBy("has_material", has(tag(input))));
        return this;
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.accept(this);
        }
        this.callbacks.clear();
    }

    public static class Builder<P> extends BuilderBase<MaterialSet, P, Builder<P>> {
        private final String name;
        private final Map<String, Entry> items = new HashMap<>();
        private final List<Consumer<MaterialSet>> callbacks = new ArrayList<>();
        private int color = 0xFFFFFFFF;
        @Nullable
        private IconSet icon = null;
        private int durability = 0;
        @Nullable
        private Tier tier = null;

        public Builder(P parent, String name) {
            super(parent);
            this.name = name;
        }

        @Override
        public MaterialSet createObject() {
            return new MaterialSet(this);
        }

        private static String prefix(String sub) {
            return sub.startsWith("tool/") ? sub : "materials/" + sub;
        }

        private static TagKey<Item> prefixTag(String sub) {
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

        public Builder<P> durability(int value) {
            durability = value;
            return this;
        }

        public Builder<P> tier(Tier value) {
            tier = value;
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

        public Builder<P> existing(String sub, ResourceLocation loc) {
            assert !items.containsKey(sub);
            put(sub, loc, () -> Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(loc)));
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
                    .dummies("ingot")
                    .alias("primary", "ingot")
                    .dummies("nugget", "plate", "stick");
        }

        public Builder<P> mechanicalSet() {
            return metalSet()
                    .dummies("bolt", "screw", "gear", "rotor", "spring");
        }

        public Builder<P> ore(Tier mineTier, float strength, OreVariant... variants) {
            var raw = put("raw", () -> REGISTRATE.item(newId("raw"), Item::new)
                    .model(ModelGen.basicItem(ModelGen.modLoc("items/material/raw")))
                    .tint(color)
                    .register());

            REGISTRATE.block(newId("ore"), OreBlock::new)
                    .properties(p -> p.strength(strength, strength * 2))
                    .transform(ModelGen.oreBlock(Arrays.asList(variants)))
                    .tint(color)
                    .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .tag(mineTier.getTag())
                    .drop(raw::getItem)
                    .register();

            callbacks.add($ -> AllRecipes.ORE_WASHER.modRecipe($.loc("crushed_purified"))
                    .inputItem(0, $.entry("crushed"), 1)
                    .outputItem(2, $.entry("crushed_purified"), 1)
                    .workTicks(200)
                    .build());

            return dust()
                    .dummies("crushed", "crushed_centrifuged", "crushed_purified")
                    .dummies("dust_impure", "dust_pure");
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
            process("stick", 1, "plate", TOOL_FILE);
            // bolt
            process("bolt", 2, "stick", TOOL_SAW);
            // screw
            process("screw", 1, "bolt", TOOL_FILE);
            // gear
            process("gear", 1, "A\nB\nA", "stick", "plate", TOOL_HAMMER, TOOL_WIRE_CUTTER);
            // rotor
            process("rotor", 1, "A A\nBC \nA A", "plate", "stick", "screw",
                    TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
            // spring
            process("spring", 1, "A\nA", "stick", TOOL_FILE, TOOL_SAW, TOOL_WIRE_CUTTER);
            return this;
        }

        public Builder<P> smelt(int time) {
            callbacks.add($ -> $.smelt("ingot", "dust", time).smelt("nugget", "dust_tiny", time));
            return this;
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

        private String tool(String category, Function<Item.Properties, ToolItem> factory) {
            var handle = Optional.ofNullable(TOOL_HANDLE_TEX.get(category))
                    .map(MaterialSet.Builder::toolTex)
                    .orElse(ModelGen.VOID_TEX);
            var head = ModelGen.gregtech("items/tools/" + category);
            var sub = "tool/" + category;
            put(sub, () -> REGISTRATE.item(newId(sub), factory)
                    .model(ModelGen.basicItem(handle, head))
                    .tint(0xFFFFFFFF, this.color)
                    .register());
            return sub;
        }

        private void tool(String category, String pattern, Object... args) {
            assert durability > 0;
            var sub = switch (category) {
                case "wrench" -> tool(category, p -> new UsableToolItem(p, durability,
                        Objects.requireNonNull(tier), MINEABLE_WITH_WRENCH));
                case "wire_cutter" -> tool(category, p -> new UsableToolItem(p, durability,
                        Objects.requireNonNull(tier), MINEABLE_WITH_CUTTER));
                default -> tool(category, p -> new ToolItem(p, durability));
            };
            process(sub, 1, pattern, args);
        }

        public Builder<P> hammer() {
            tool("hammer", "AA \nAAB\nAA ", "primary", TOOL_HANDLE);
            return this;
        }

        public Builder<P> mortar() {
            tool("mortar", " A \nBAB\nBBB", "primary", ItemTags.STONE_TOOL_MATERIALS);
            return this;
        }

        public Builder<P> toolSet() {
            tool("file", "A\nA\nB", "plate", TOOL_HANDLE);
            tool("saw", "AAB\n  B", "plate", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
            tool("screwdriver", "  A\n A \nB  ", "stick", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
            tool("wrench", "A A\nAAA\n A ", "plate", TOOL_HAMMER);
            tool("wire_cutter", "A A\n A \nBCB", "plate", TOOL_HANDLE, TOOL_SCREW,
                    TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
            return hammer().mortar();
        }
    }
}
