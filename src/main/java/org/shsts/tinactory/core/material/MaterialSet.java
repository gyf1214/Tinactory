package org.shsts.tinactory.core.material;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.tool.ToolItem;
import org.shsts.tinactory.core.tool.UsableToolItem;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRegistries.simpleFluid;
import static org.shsts.tinactory.content.AllTags.extend;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
    public final String name;
    public final int color;

    private record ItemEntry(ResourceLocation loc, TagKey<Item> tag,
        Supplier<? extends Item> item, boolean isAlias) {
        public ItemEntry(ResourceLocation loc, TagKey<Item> tag,
            Supplier<? extends Item> item) {
            this(loc, tag, item, false);
        }

        public ItemEntry alias() {
            return new ItemEntry(loc, tag, item, true);
        }

        public Item getItem() {
            return getEntry().get();
        }

        public Supplier<? extends Item> getEntry() {
            assert item != null;
            return item;
        }
    }

    private record BlockEntry(ResourceLocation loc, Supplier<? extends Block> block) {}

    private record FluidEntry(ResourceLocation loc, Supplier<? extends Fluid> fluid, int baseAmount) {}

    private final Map<String, ItemEntry> items;
    private final Map<String, BlockEntry> blocks;
    private final Map<String, FluidEntry> fluids;
    @Nullable
    private final OreVariant oreVariant;

    private MaterialSet(Builder<?> builder) {
        this.name = builder.name;
        this.color = builder.color;
        this.items = builder.items;
        this.blocks = builder.blocks;
        this.oreVariant = builder.oreVariant;
        this.fluids = builder.fluids;
    }

    private ItemEntry safeItem(String sub) {
        assert items.containsKey(sub) : "%s does not have item %s".formatted(this, sub);
        return items.get(sub);
    }

    public ResourceLocation loc(String sub) {
        return safeItem(sub).loc;
    }

    public TagKey<Item> tag(String sub) {
        return safeItem(sub).tag;
    }

    public Supplier<? extends Item> entry(String sub) {
        return safeItem(sub).item();
    }

    public Item item(String sub) {
        return safeItem(sub).getItem();
    }

    public boolean isAlias(String sub) {
        return items.containsKey(sub) && items.get(sub).isAlias;
    }

    public boolean hasItem(String sub) {
        return items.containsKey(sub);
    }

    public Set<String> itemSubs() {
        return items.keySet();
    }

    public Set<String> fluidSubs() {
        return fluids.keySet();
    }

    private BlockEntry safeBlock(String sub) {
        assert blocks.containsKey(sub) : "%s does not have block %s".formatted(this, sub);
        return blocks.get(sub);
    }

    public ResourceLocation blockLoc(String sub) {
        return safeBlock(sub).loc;
    }

    public Supplier<? extends Block> blockEntry(String sub) {
        return safeBlock(sub).block;
    }

    public Block block(String sub) {
        return safeBlock(sub).block.get();
    }

    public boolean hasBlock(String sub) {
        return blocks.containsKey(sub);
    }

    public boolean hasFluid(String sub) {
        return fluids.containsKey(sub);
    }

    public ResourceLocation fluidLoc(String sub) {
        assert fluids.containsKey(sub);
        return fluids.get(sub).loc;
    }

    public Supplier<? extends Fluid> fluid(String sub) {
        assert fluids.containsKey(sub);
        return fluids.get(sub).fluid;
    }

    public int fluidAmount(String sub, float amount) {
        assert fluids.containsKey(sub);
        return Math.round(amount * fluids.get(sub).baseAmount);
    }

    public OreVariant oreVariant() {
        assert oreVariant != null;
        return oreVariant;
    }

    public static class Builder<P> extends SimpleBuilder<MaterialSet, P, Builder<P>> {
        private final String name;
        private final Map<String, ItemEntry> items = new HashMap<>();
        private final Map<String, BlockEntry> blocks = new HashMap<>();
        private final Map<String, FluidEntry> fluids = new HashMap<>();
        private int color = 0xFFFFFFFF;
        @Nullable
        private OreVariant oreVariant = null;

        private Builder(P parent, String name) {
            super(parent);
            this.name = name;
        }

        @Override
        protected MaterialSet createObject() {
            return new MaterialSet(this);
        }

        private TagKey<Item> newTag(String sub) {
            return extend(AllTags.material(sub), name);
        }

        public Builder<P> color(int value) {
            assert (value & 0xFF000000) != 0;
            color = value;
            return this;
        }

        public int getColor() {
            return color;
        }

        private String newId(String sub) {
            var prefix = sub.startsWith("tool/") ? "" : "material/";
            return prefix + sub + "/" + name;
        }

        private void put(String sub, ResourceLocation loc, Supplier<? extends Item> item) {
            if (items.containsKey(sub)) {
                return;
            }
            var tag = newTag(sub);
            var entry = new ItemEntry(loc, tag, item);
            items.put(sub, entry);
        }

        private void put(String sub, Supplier<IEntry<? extends Item>> item) {
            if (items.containsKey(sub)) {
                return;
            }
            var entry = item.get();
            put(sub, entry.loc(), entry);
        }

        public Builder<P> existing(String sub, Item item) {
            assert !items.containsKey(sub);
            var loc = modLoc(newId(sub));
            put(sub, loc, () -> item);
            return this;
        }

        public Builder<P> existing(String sub, Fluid fluid, int baseAmount) {
            assert !fluids.containsKey(sub);
            var loc = modLoc(newId(sub));
            fluids.put(sub, new FluidEntry(loc, () -> fluid, baseAmount));
            return this;
        }

        public Builder<P> alias(String sub, String sub2) {
            if (items.containsKey(sub2)) {
                items.put(sub, items.get(sub2).alias());
            } else if (fluids.containsKey(sub2)) {
                fluids.put(sub, fluids.get(sub2));
            } else {
                throw new IllegalArgumentException("Alias " + sub2 + " does not exist in " + name);
            }
            return this;
        }

        public Builder<P> item(String sub, Function<Item.Properties, ? extends Item> factory) {
            put(sub, () -> REGISTRATE.item(newId(sub), factory)
                .tint(color)
                .register());
            return this;
        }

        public Builder<P> item(String sub) {
            return item(sub, Item::new);
        }

        public Builder<P> fluid(String sub, ResourceLocation tex,
            int texColor, int displayColor, int baseAmount) {
            var fluid = simpleFluid("material/" + sub + "/" + name, tex, texColor, displayColor);
            fluids.put(sub, new FluidEntry(fluid.loc(), fluid, baseAmount));
            return this;
        }

        public Builder<P> oreOnly(OreVariant variant) {
            oreVariant = variant;
            if (!blocks.containsKey("ore")) {
                var ore = REGISTRATE.block(newId("ore"), OreBlock.factory(variant))
                    .material(variant.blockMaterial, variant.materialColor)
                    .properties(p -> p.strength(variant.destroyTime, variant.explodeResistance)
                        .sound(variant.soundType))
                    .translucent()
                    .tint(color)
                    .noBlockItem()
                    .register();
                blocks.put("ore", new BlockEntry(ore.loc(), ore));
            }
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

            private ToolBuilder item(String category, Function<Item.Properties, ToolItem> factory) {
                var sub = "tool/" + category;
                put(sub, () -> REGISTRATE.item(newId(sub), factory)
                    .tint(0xFFFFFFFF, color)
                    .register());
                return this;
            }

            public ToolBuilder item(String category) {
                return item(category, p -> new ToolItem(p, durability));
            }

            public ToolBuilder usable(String category) {
                assert tier != null;
                var tag = AllTags.modBlock("mineable/" + category);
                return item(category, p -> new UsableToolItem(p, durability, tier, tag));
            }

            @Override
            protected Unit createObject() {
                return Unit.INSTANCE;
            }
        }

        public ToolBuilder tool(int durability, @Nullable Tier tier) {
            return new ToolBuilder(durability, tier);
        }
    }

    public static <P> Builder<P> builder(P parent, String name) {
        return new Builder<>(parent, name);
    }

    @Override
    public String toString() {
        return "MaterialSet{%s}".formatted(name);
    }
}
