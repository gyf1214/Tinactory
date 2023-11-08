package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    private boolean isFrozen = false;
    private final String name;
    private final IconSet icon;
    private final int color;
    private final List<Runnable> callbacks = new ArrayList<>();

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
            REGISTRATE.itemTag(item, tag);
            REGISTRATE.itemTag(tag, prefixTag);
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
        REGISTRATE.itemTag(targetTag, tag);
        REGISTRATE.itemTag(tag, prefixTag(sub));
        var loc = item.getRegistryName();
        assert loc != null;
        this.items.put(sub, new Entry(loc, tag, () -> item));
        return this;
    }

    public MaterialSet alias(String sub, String sub2) {
        var entry = this.items.get(sub2);
        assert entry != null;
        this.items.put(sub, entry);
        REGISTRATE.itemTag(entry.tag, prefixTag(sub));
        return this;
    }

    private String id(String sub) {
        return sub + "/" + this.name;
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

    private MaterialSet defer(Consumer<Entry[]> cons, String... names) {
        this.callbacks.add(() -> this.optional(cons, names));
        return this;
    }

    private Entry dummy(String sub) {
        return safePut(sub, () -> REGISTRATE.item(this.id(sub), Item::new)
                .model(this.icon.itemModel(sub))
                .tint(this.color)
                .register());
    }

    public MaterialSet dust() {
        this.dummy("dust");
        return this;
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
        this.dustSet();
        this.dummy("ingot");
        this.alias("primary", "ingot");
        this.dummy("nugget");
        this.dummy("plate");
        this.dummy("stick");
        return this;
    }

    public MaterialSet mechanicalSet() {
        this.metalSet();
        this.dummy("bolt");
        this.dummy("screw");
        this.dummy("gear");
        this.dummy("rotor");
        this.dummy("spring");
        return this;
    }

    private void simpleToolProcess(String resultSub, String materialSub, int count, TagKey<Item> tool) {
        this.defer(entries -> AllRecipes.TOOL.modRecipe(entries[0].loc)
                .result(entries[0].getEntry(), count)
                .pattern("#")
                .define('#', entries[1].tag)
                .toolTag(tool)
                .build(), resultSub, materialSub);
    }

    public MaterialSet toolProcess() {
        // grind dust
        this.simpleToolProcess("dust", "primary", 1, AllTags.TOOL_MORTAR);
        this.simpleToolProcess("dust_tiny", "nugget", 1, AllTags.TOOL_MORTAR);
        // ingot to plate
        this.defer(entries -> AllRecipes.TOOL.modRecipe(entries[0].loc)
                .result(entries[0].getEntry(), 1)
                .pattern("#").pattern("#")
                .define('#', entries[1].tag)
                .toolTag(AllTags.TOOL_HAMMER)
                .build(), "plate", "ingot");
        // plate to stick
        this.simpleToolProcess("stick", "plate", 1, AllTags.TOOL_FILE);
        // stick to bolt
        this.simpleToolProcess("bolt", "stick", 2, AllTags.TOOL_SAW);
        // bolt to screw
        this.simpleToolProcess("screw", "bolt", 1, AllTags.TOOL_FILE);
        return this;
    }

    private static ResourceLocation toolTex(String sub) {
        return ModelGen.gregtech("items/tools/" + sub);
    }

    private Entry tool(String category, int durability) {
        var handle = switch (category) {
            case "saw" -> toolTex("handle_saw");
            case "hammer" -> toolTex("handle_hammer");
            case "mortar" -> toolTex("mortar_base");
            case "file" -> toolTex("handle_file");
            default -> ModelGen.VOID_TEX;
        };
        var head = ModelGen.gregtech("items/tools/" + category);
        var sub = "tool/" + category;
        return safePut(sub, () -> REGISTRATE.item(this.id(sub),
                        properties -> new ToolItem(properties, durability))
                .model(ModelGen.basicItem(handle, head))
                .tag(AllTags.TOOL)
                .tint(0xFFFFFF, this.color)
                .register());
    }

    public MaterialSet hammer(int durability) {
        var tool = this.tool("hammer", durability);
        return this.defer(materials -> REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(tool.getItem())
                .pattern("## ")
                .pattern("##S")
                .pattern("## ")
                .define('#', materials[0].tag)
                .define('S', AllTags.TOOL_HANDLE)
                .unlockedBy("has_material", AllRecipes.has(materials[0].tag))
        ), "primary");
    }

    public MaterialSet mortar(int durability) {
        var tool = this.tool("mortar", durability);
        return this.defer(materials -> REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(tool.getItem())
                .pattern(" # ")
                .pattern("S#S")
                .pattern("SSS")
                .define('#', materials[0].tag)
                .define('S', ItemTags.STONE_TOOL_MATERIALS)
                .unlockedBy("has_material", AllRecipes.has(materials[0].tag))
        ), "primary");
    }

    public MaterialSet toolSet(int durability) {
        this.hammer(durability).mortar(durability);

        var wrench = this.tool("wrench", durability);
        this.defer(materials -> AllRecipes.TOOL.modRecipe(wrench.loc)
                .result(wrench::getItem, 1)
                .pattern("# #")
                .pattern("###")
                .pattern(" # ")
                .define('#', materials[0].tag)
                .toolTag(AllTags.TOOL_HAMMER)
                .build(), "plate");

        var file = this.tool("file", durability);
        this.defer(materials -> REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(file.getItem())
                .pattern("#").pattern("#").pattern("S")
                .define('#', materials[0].tag)
                .define('S', AllTags.TOOL_HANDLE)
        ), "plate");

        var saw = this.tool("saw", durability);
        this.defer(materials -> AllRecipes.TOOL.modRecipe(saw.loc)
                .result(saw::getItem, 1)
                .pattern("##S").pattern("  S")
                .define('#', materials[0].tag)
                .define('S', AllTags.TOOL_HANDLE)
                .build(), "plate");

        return this;
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.run();
        }
        this.callbacks.clear();
        this.isFrozen = true;
    }
}
