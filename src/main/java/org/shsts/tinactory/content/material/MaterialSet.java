package org.shsts.tinactory.content.material;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.registrate.common.RegistryEntry;

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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
    private boolean isFrozen = false;
    private final String name;
    private final IconSet icon;
    private final int color;
    private final List<Runnable> callbacks = new ArrayList<>();
    private int durability = 0;

    private record Entry(ResourceLocation loc, TagKey<Item> tag, Supplier<Item> item) {
        public Item getItem() {
            return this.getEntry().get();
        }

        public Supplier<Item> getEntry() {
            assert this.item != null;
            return this.item;
        }
    }

    private final Map<String, Entry> items = new HashMap<>();

    public MaterialSet(String name, IconSet icon, int color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    private static String prefix(String sub) {
        return sub.startsWith("tool/") ? sub : "materials/" + sub;
    }

    private static TagKey<Item> prefixTag(String sub) {
        return AllTags.modItem(prefix(sub));
    }

    private TagKey<Item> tag(String sub) {
        return AllTags.modItem(prefix(sub) + "/" + this.name);
    }

    private Entry safePut(String sub, ResourceLocation loc, Supplier<Item> item) {
        if (this.isFrozen) {
            throw new IllegalStateException("Material set %s is frozen".formatted(this.name));
        }
        return this.items.computeIfAbsent(sub, $ -> {
            var tag = this.tag(sub);
            var prefixTag = prefixTag(sub);
            REGISTRATE.tag(item, tag);
            REGISTRATE.tag(tag, prefixTag);
            return new Entry(loc, tag, item);
        });
    }

    private Entry safePut(String sub, Supplier<RegistryEntry<? extends Item>> item) {
        if (this.items.containsKey(sub)) {
            return this.items.get(sub);
        }
        var entry = item.get();
        return this.safePut(sub, entry.loc, entry::get);
    }

    private Optional<Entry> get(String sub) {
        return Optional.ofNullable(this.items.get(sub));
    }

    public TagKey<Item> getTag(String sub) {
        assert this.items.containsKey(sub);
        return this.items.get(sub).tag;
    }

    public Item getItem(String sub) {
        assert this.items.containsKey(sub);
        return this.items.get(sub).getItem();
    }

    public Supplier<Item> getItemEntry(String sub) {
        assert this.items.containsKey(sub);
        return this.items.get(sub).item();
    }

    public MaterialSet existing(String sub, Item item) {
        assert !this.items.containsKey(sub);
        var loc = item.getRegistryName();
        assert loc != null;
        this.safePut(sub, loc, () -> item);
        return this;
    }

    public MaterialSet existing(String sub, TagKey<Item> targetTag, Item item) {
        assert !this.items.containsKey(sub);
        var tag = this.tag(sub);
        REGISTRATE.tag(targetTag, tag);
        REGISTRATE.tag(tag, prefixTag(sub));
        var loc = item.getRegistryName();
        assert loc != null;
        this.items.put(sub, new Entry(loc, tag, () -> item));
        return this;
    }

    public MaterialSet alias(String sub, String sub2) {
        var entry = this.items.get(sub2);
        assert entry != null;
        this.items.put(sub, entry);
        REGISTRATE.tag(entry.tag, prefixTag(sub));
        return this;
    }

    private String id(String sub) {
        var prefix = sub.startsWith("tool/") ? "" : "material/";
        return prefix + sub + "/" + this.name;
    }

    public ResourceLocation loc(String sub) {
        return new ResourceLocation(REGISTRATE.modid, this.id(sub));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void optional(Consumer<Entry[]> cons, String... names) {
        var optionals = Arrays.stream(names).map(this::get).toList();
        if (optionals.stream().anyMatch(Optional::isEmpty)) {
            return;
        }
        cons.accept(optionals.stream().map(Optional::get).toArray(Entry[]::new));
    }

    private void defer(Consumer<Entry[]> cons, String... names) {
        this.callbacks.add(() -> this.optional(cons, names));
    }

    private Entry dummy(String sub) {
        return safePut(sub, () -> REGISTRATE.item(this.id(sub), Item::new)
                .model(this.icon.itemModel(sub))
                .tint(this.color)
                .register());
    }

    private Entry dummy(String sub, ResourceLocation... layers) {
        return safePut(sub, () -> REGISTRATE.item(this.id(sub), Item::new)
                .model(ModelGen.basicItem(layers))
                .tint(this.color)
                .register());
    }

    private MaterialSet dummies(String... subs) {
        for (var sub : subs) {
            this.dummy(sub);
        }
        return this;
    }

    public MaterialSet dust() {
        return this.dummies("dust");
    }

    public MaterialSet dustSet() {
        var dust = this.dummy("dust");
        var dustTiny = this.dummy("dust_tiny");
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(dustTiny.getItem(), 9)
                .requires(dust.tag)
                .unlockedBy("has_dust", AllRecipes.has(dust.tag)));
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(dust.getItem())
                .requires(Ingredient.of(dustTiny.tag), 9)
                .unlockedBy("has_dust_small", AllRecipes.has(dustTiny.tag)));
        return this;
    }

    public MaterialSet metalSet() {
        return this.dustSet()
                .dummies("ingot")
                .alias("primary", "ingot")
                .dummies("nugget", "plate", "stick");
    }

    public MaterialSet mechanicalSet() {
        return this.metalSet()
                .dummies("bolt", "screw", "gear", "rotor", "spring");
    }

    private void simpleToolProcess(String resultSub, int count, String materialSub, TagKey<Item> tool) {
        this.defer(entries -> AllRecipes.TOOL.modRecipe(entries[0].loc)
                .result(entries[0].getEntry(), count)
                .pattern("#")
                .define('#', entries[1].tag)
                .toolTag(tool)
                .build(), resultSub, materialSub);
    }

    @SuppressWarnings("unchecked")
    private void toolProcess(Entry entry, int count, String pattern, Object... args) {
        var patterns = pattern.split("\n");
        var materialCount = 1 + pattern.chars().filter(x -> x >= 'A' && x <= 'Z').map(x -> x - 'A').max().orElse(-1);
        this.callbacks.add(() -> {
            if (Arrays.stream(args).anyMatch(o -> o instanceof String s && !this.items.containsKey(s))) {
                return;
            }
            if (args.length > materialCount) {
                var builder = AllRecipes.TOOL.modRecipe(entry.loc).result(entry::getItem, count);
                for (var pat : patterns) {
                    builder.pattern(pat);
                }
                for (var i = 0; i < materialCount; i++) {
                    var material = args[i];
                    var key = (char) ('A' + i);
                    if (material instanceof String s) {
                        builder.define(key, this.items.get(s).tag);
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
            } else {
                REGISTRATE.vanillaRecipe(() -> {
                    var builder = ShapedRecipeBuilder.shaped(entry.getItem(), count);
                    TagKey<Item> unlock = null;
                    for (var pat : patterns) {
                        builder.pattern(pat);
                    }
                    for (var i = 0; i < args.length; i++) {
                        var material = args[i];
                        var key = (char) ('A' + i);
                        if (material instanceof String s) {
                            var tag = this.items.get(s).tag;
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
                });
            }
        });
    }

    private void toolProcess(String sub, int count, String pattern, Object... args) {
        this.get(sub).ifPresent(entry -> this.toolProcess(entry, count, pattern, args));
    }

    public MaterialSet toolProcess() {
        // grind dust
        this.simpleToolProcess("dust", 1, "primary", AllTags.TOOL_MORTAR);
        this.simpleToolProcess("dust_tiny", 1, "nugget", AllTags.TOOL_MORTAR);
        // plate
        this.toolProcess("plate", 1, "A\nA", "ingot", AllTags.TOOL_HAMMER);
        // stick
        this.simpleToolProcess("stick", 1, "plate", AllTags.TOOL_FILE);
        // bolt
        this.simpleToolProcess("bolt", 2, "stick", AllTags.TOOL_SAW);
        // screw
        this.simpleToolProcess("screw", 1, "bolt", AllTags.TOOL_FILE);
        // gear
        this.toolProcess("gear", 1, "A\nB\nA", "stick", "plate", AllTags.TOOL_HAMMER, AllTags.TOOL_WIRE_CUTTER);
        // rotor
        this.toolProcess("rotor", 1, "A A\nBC \nA A", "plate", "stick", "screw",
                AllTags.TOOL_HAMMER, AllTags.TOOL_FILE, AllTags.TOOL_SCREWDRIVER);
        // spring
        this.toolProcess("spring", 1, "A\nA", "stick", AllTags.TOOL_FILE, AllTags.TOOL_SAW, AllTags.TOOL_WIRE_CUTTER);

        return this;
    }

    private Consumer<Entry[]> smeltRecipe(int time) {
        return materials -> REGISTRATE.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(materials[1].tag), materials[0].getItem(), 0, time)
                .unlockedBy("has_material", AllRecipes.has(materials[1].tag)));
    }

    public MaterialSet smelt(int time) {
        this.defer(this.smeltRecipe(time), "ingot", "dust");
        this.defer(this.smeltRecipe(time), "nugget", "dust_tiny");
        return this;
    }

    public MaterialSet ore(List<ResourceLocation> baseModels, Tier mineTier, float strength) {
        var raw = this.dummy("raw", ModelGen.modLoc("items/material/raw"));
        REGISTRATE.block(this.id("ore"), properties -> new OreBlock(properties, baseModels.size()))
                .properties(p -> p.strength(strength, strength * 2))
                .drop(raw::getItem)
                .blockState(ctx -> {
                    var block = ctx.object;
                    var models = ctx.provider.models();
                    var multipart = ctx.provider.getMultipartBuilder(block);
                    var i = 0;
                    for (var baseModel : baseModels) {
                        multipart.part()
                                .modelFile(models.getExistingFile(baseModel)).addModel()
                                .condition(block.getProperty(), i++).end();
                    }
                    var overlay = models.getExistingFile(ModelGen.modLoc("block/material/ore_overlay"));
                    multipart.part()
                            .modelFile(overlay)
                            .addModel().end();
                })
                .translucent()
                .tint(this.color)
                .tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .tag(mineTier.getTag())
                .register();
        this.dust()
                .dummies("crushed", "crushed_centrifuged", "crushed_purified")
                .dummies("dust_impure", "dust_pure");

        AllRecipes.ORE_WASHER.modRecipe(this.loc("crushed_purified"))
                .inputItem(0, this.getItemEntry("crushed"), 1)
                .outputItem(2, this.getItemEntry("crushed_purified"), 1)
                .workTicks(200)
                .build();

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

    private Entry tool(String category, Function<Item.Properties, ToolItem> factory) {
        var handle = Optional.ofNullable(TOOL_HANDLE_TEX.get(category))
                .map(MaterialSet::toolTex)
                .orElse(ModelGen.VOID_TEX);
        var head = ModelGen.gregtech("items/tools/" + category);
        var sub = "tool/" + category;
        return safePut(sub, () -> REGISTRATE.item(this.id(sub), factory)
                .model(ModelGen.basicItem(handle, head))
                .tint(0xFFFFFFFF, this.color)
                .register());
    }

    private MaterialSet tool(String category, String pattern, Object... args) {
        var durability = this.durability;

        Function<Item.Properties, ToolItem> factory = switch (category) {
            case "wrench" -> p -> new UsableToolItem(p, durability, Tiers.IRON, AllTags.MINEABLE_WITH_WRENCH);
            case "wire_cutter" -> p -> new UsableToolItem(p, durability, Tiers.IRON, AllTags.MINEABLE_WITH_CUTTER);
            default -> p -> new ToolItem(p, durability);
        };

        var entry = this.tool(category, factory);
        this.toolProcess(entry, 1, pattern, args);
        return this;
    }

    private MaterialSet hammer() {
        return this.tool("hammer", "AA \nAAB\nAA ", "primary", AllTags.TOOL_HANDLE);
    }

    public MaterialSet hammer(int durability) {
        this.durability = durability;
        return this.hammer();
    }

    private MaterialSet mortar() {
        return this.tool("mortar", " A \nBAB\nBBB", "primary", ItemTags.STONE_TOOL_MATERIALS);
    }

    public MaterialSet mortar(int durability) {
        this.durability = durability;
        return this.mortar();
    }

    public MaterialSet toolSet(int durability) {
        this.durability = durability;

        return this.hammer().mortar()
                .tool("file", "A\nA\nB", "plate", AllTags.TOOL_HANDLE)
                .tool("saw", "AAB\n  B", "plate", AllTags.TOOL_HANDLE,
                        AllTags.TOOL_FILE, AllTags.TOOL_HAMMER)
                .tool("screwdriver", "  A\n A \nB  ", "stick", AllTags.TOOL_HANDLE,
                        AllTags.TOOL_FILE, AllTags.TOOL_HAMMER)
                .tool("wrench", "A A\nAAA\n A ", "plate", AllTags.TOOL_HAMMER)
                .tool("wire_cutter", "A A\n A \nBCB", "plate", AllTags.TOOL_HANDLE, AllTags.TOOL_SCREW,
                        AllTags.TOOL_HAMMER, AllTags.TOOL_FILE, AllTags.TOOL_SCREWDRIVER);
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.run();
        }
        this.callbacks.clear();
        this.isFrozen = true;
    }
}
