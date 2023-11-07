package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.Nullable;
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
    private static final ResourceLocation SET_LOC = ModelGen.gregtech("items/material_sets");

    private boolean isFrozen = false;
    private final String name;
    private final int color;
    private final List<Runnable> callbacks = new ArrayList<>();

    private record Entry(ResourceLocation loc, TagKey<Item> tag, @Nullable Supplier<Item> item) {
        public Entry(ResourceLocation loc, TagKey<Item> tag) {
            this(loc, tag, null);
        }

        public Item getItem() {
            return this.getEntry().get();
        }

        public Supplier<Item> getEntry() {
            assert this.item != null;
            return this.item;
        }
    }

    private final Map<String, Entry> items = new HashMap<>();

    public MaterialSet(String name, int color) {
        this.name = name;
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

    private Entry safePut(String sub, ItemLike itemLike) {
        var item = itemLike.asItem();
        var loc = item.getRegistryName();
        assert loc != null;
        return this.safePut(sub, loc, () -> item);
    }

    private Entry safePut(String sub, RegistryEntry<? extends Item> item) {
        return this.safePut(sub, item.loc, item::get);
    }

    private Optional<Entry> get(String sub) {
        return Optional.ofNullable(this.items.get(sub));
    }

    public MaterialSet existing(String sub, ItemLike item) {
        this.safePut(sub, item);
        return this;
    }

    public MaterialSet existing(String sub, TagKey<Item> tag) {
        this.items.put(sub, new Entry(this.loc(sub), tag));
        REGISTRATE.itemTag(tag, prefixTag(sub));
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

    private static ResourceLocation icon(String subFolder, String sub) {
        return new ResourceLocation(SET_LOC.getNamespace(), SET_LOC.getPath() + "/" + subFolder + "/" + sub);
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

    private Entry dummy(String subFolder, String sub) {
        return safePut(sub, REGISTRATE.item(this.id(sub), Item::new)
                .model(ModelGen.basicItem(icon(subFolder, sub)))
                .tint(this.color)
                .register());
    }

    public MaterialSet dust(String subFolder) {
        var dust = dummy(subFolder, "dust");
        var dustSmall = dummy(subFolder, "dust_tiny");
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(dustSmall.getItem(), 9)
                .requires(dust.tag)
                .unlockedBy("has_dust", AllRecipes.has(dust.tag)));
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(dust.getItem())
                .requires(Ingredient.of(dustSmall.tag), 9)
                .unlockedBy("has_dust_small", AllRecipes.has(dustSmall.tag)));
        return this;
    }

    public MaterialSet grind(int damage) {
        return defer(entries -> AllRecipes.TOOL.modRecipe("mortar/" + entries[0].loc.getPath())
                .result(entries[0].getEntry(), 1)
                .pattern("#")
                .define('#', entries[1].tag)
                .toolTag(AllTags.TOOL_MORTAR)
                .damage(damage)
                .build(), "dust", "primary");
    }

    private static ResourceLocation toolTex(String sub) {
        return ModelGen.gregtech("items/tools/" + sub);
    }

    private Entry putTool(String category, int durability) {
        var handle = switch (category) {
            case "saw" -> toolTex("handle_saw");
            case "hammer" -> toolTex("handle_hammer");
            case "mortar" -> toolTex("mortar_base");
            default -> ModelGen.VOID_TEX;
        };
        var head = ModelGen.gregtech("items/tools/" + category);
        var sub = "tool/" + category;
        return safePut(sub, REGISTRATE.item(this.id(sub),
                        properties -> new ToolItem(properties, 1, durability))
                .model(ModelGen.basicItem(handle, head))
                .tag(AllTags.TOOL)
                .tint(0xFFFFFF, this.color)
                .register());
    }

    public MaterialSet tool(String sub, int durability) {
        this.putTool(sub, durability);
        return this;
    }

    public MaterialSet hammer(int durability) {
        var tool = this.putTool("hammer", durability);
        return this.defer(materials -> REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(tool.getItem())
                .pattern("MM ")
                .pattern("MMS")
                .pattern("MM ")
                .define('M', materials[0].tag)
                .define('S', Items.STICK)
                .unlockedBy("has_material", AllRecipes.has(materials[0].tag))
        ), "primary");
    }

    public MaterialSet mortar(int durability) {
        var tool = this.putTool("mortar", durability);
        return this.defer(materials -> REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(tool.getItem())
                .pattern(" M ")
                .pattern("SMS")
                .pattern("SSS")
                .define('M', materials[0].tag)
                .define('S', ItemTags.STONE_TOOL_MATERIALS)
                .unlockedBy("has_material", AllRecipes.has(materials[0].tag))
        ), "primary");
    }

    public MaterialSet wrench(int durability) {
        var tool = this.putTool("wrench", durability);
        return this.defer(materials -> AllRecipes.TOOL.recipe(tool.getItem())
                .pattern("P P")
                .pattern("PPP")
                .pattern(" P ")
                .define('P', materials[0].tag)
                .damage(80)
                .toolTag(AllTags.TOOL_HAMMER)
                .build(), "plate");
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.run();
        }
        this.isFrozen = true;
    }
}
