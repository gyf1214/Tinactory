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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
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

    private record Entry(TagKey<Item> tag, @Nullable Supplier<Item> item) {
        public Entry(TagKey<Item> tag) {
            this(tag, null);
        }

        public Item getItem() {
            assert this.item != null;
            return this.item.get();
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

    private Entry safePut(String sub, Supplier<? extends Item> item) {
        if (this.isFrozen) {
            throw new IllegalStateException("Material set %s is frozen".formatted(this.name));
        }
        return this.items.computeIfAbsent(sub, $ -> {
            var tag = this.tag(sub);
            var prefixTag = prefixTag(sub);
            REGISTRATE.itemTag(item, tag);
            REGISTRATE.itemTag(tag, prefixTag);
            return new Entry(tag, item::get);
        });
    }

    public TagKey<Item> getTag(String sub) {
        var entry = this.items.get(sub);
        assert entry != null;
        return entry.tag;
    }

    public Optional<TagKey<Item>> get(String sub) {
        return Optional.ofNullable(this.items.get(sub)).map(x -> x.tag);
    }

    public Optional<Item> getItem(String sub) {
        return Optional.ofNullable(this.items.get(sub))
                .flatMap(x -> Optional.ofNullable(x.item))
                .map(Supplier::get);
    }

    public MaterialSet existing(String sub, ItemLike item) {
        this.safePut(sub, item::asItem);
        return this;
    }

    public MaterialSet existing(String sub, TagKey<Item> tag) {
        this.items.put(sub, new Entry(tag));
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

    private String extend(String sub) {
        return sub + "/" + this.name;
    }

    private ResourceLocation tex(String subFolder, String sub) {
        return new ResourceLocation(SET_LOC.getNamespace(), SET_LOC.getPath() + "/" + subFolder + "/" + sub);
    }

    public ResourceLocation loc(String sub) {
        return new ResourceLocation(REGISTRATE.modid, this.extend(sub));
    }

    private Entry dummy(String subFolder, String sub) {
        return safePut(sub, REGISTRATE.item(this.extend(sub), Item::new)
                .model(ModelGen.basicItem(tex(subFolder, sub)))
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
        return safePut(sub, REGISTRATE.item(this.extend(sub), properties -> new ToolItem(properties, 1, durability))
                .model(ModelGen.basicItem(handle, head))
                .tag(AllTags.TOOL)
                .tint(0xFFFFFF, this.color)
                .register());
    }

    private MaterialSet putTool(String sub, int durability, Consumer<Entry> cons) {
        var entry = this.putTool(sub, durability);
        this.callbacks.add(() -> cons.accept(entry));
        return this;
    }

    public MaterialSet tool(String sub, int durability) {
        this.putTool(sub, durability);
        return this;
    }

    private void hammerRecipe(Entry entry) {
        this.get("tool_material").ifPresent(material -> REGISTRATE.vanillaRecipe(
                () -> ShapedRecipeBuilder
                        .shaped(entry.getItem())
                        .pattern("MM ")
                        .pattern("MMS")
                        .pattern("MM ")
                        .define('M', material)
                        .define('S', Items.STICK)
                        .unlockedBy("has_material", AllRecipes.has(material))));
    }

    public MaterialSet hammer(int durability) {
        return this.putTool("hammer", durability, this::hammerRecipe);
    }

    private void mortarRecipe(Entry entry) {
        this.get("tool_material").ifPresent(material -> REGISTRATE.vanillaRecipe(
                () -> ShapedRecipeBuilder
                        .shaped(entry.getItem())
                        .pattern(" M ")
                        .pattern("SMS")
                        .pattern("SSS")
                        .define('M', material)
                        .define('S', ItemTags.STONE_TOOL_MATERIALS)
                        .unlockedBy("has_material", AllRecipes.has(material))));
    }

    public MaterialSet mortar(int durability) {
        return this.putTool("mortar", durability, this::mortarRecipe);
    }

    private void wrenchRecipe(Entry entry) {
        this.get("plate").ifPresent(plate -> AllRecipes.TOOL.recipe(entry.getItem())
                .pattern("P P")
                .pattern("PPP")
                .pattern(" P ")
                .define('P', plate)
                .damage(80)
                .toolTag(AllTags.TOOL_HAMMER)
                .build());
    }

    public MaterialSet wrench(int durability) {
        return this.putTool("wrench", durability, this::wrenchRecipe);
    }

    public void freeze() {
        for (var cb : this.callbacks) {
            cb.run();
        }
        this.isFrozen = true;
    }
}
