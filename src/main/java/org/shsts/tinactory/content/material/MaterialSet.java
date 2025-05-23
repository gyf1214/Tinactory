package org.shsts.tinactory.content.material;

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
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRegistries.simpleFluid;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialSet {
    public final String name;
    public final int color;

    private record ItemEntry(ResourceLocation loc, TagKey<Item> tag,
        Supplier<? extends Item> item,
        @Nullable TagKey<Item> target, boolean isAlias) {
        public ItemEntry(ResourceLocation loc, TagKey<Item> tag,
            Supplier<? extends Item> item) {
            this(loc, tag, item, null, false);
        }

        public ItemEntry(ResourceLocation loc, TagKey<Item> tag,
            Supplier<? extends Item> item, TagKey<Item> target) {
            this(loc, tag, item, target, false);
        }

        public ItemEntry alias() {
            return new ItemEntry(loc, tag, item, target, true);
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

    public boolean hasTarget(String sub) {
        return items.containsKey(sub) && items.get(sub).target != null;
    }

    public TagKey<Item> target(String sub) {
        var ret = safeItem(sub).target;
        assert ret != null;
        return ret;
    }

    public boolean hasItem(String sub) {
        return items.containsKey(sub);
    }

    public Set<String> itemSubs() {
        return items.keySet();
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

    public boolean hasFluid() {
        return hasFluid("fluid");
    }

    public ResourceLocation fluidLoc(String sub) {
        assert fluids.containsKey(sub);
        return fluids.get(sub).loc;
    }

    public ResourceLocation fluidLoc() {
        return fluidLoc("fluid");
    }

    public Supplier<? extends Fluid> fluid(String sub) {
        assert fluids.containsKey(sub);
        return fluids.get(sub).fluid;
    }

    public Supplier<? extends Fluid> fluid() {
        return fluid("fluid");
    }

    public int fluidAmount(String sub, float amount) {
        assert fluids.containsKey(sub);
        return Math.round(amount * fluids.get(sub).baseAmount);
    }

    public int fluidAmount(float amount) {
        return fluidAmount("fluid", amount);
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

        private static String prefix(String sub) {
            return sub.startsWith("tool/") ? sub : "materials/" + sub;
        }

        public static TagKey<Item> prefixTag(String sub) {
            return AllTags.modItem(prefix(sub));
        }

        private TagKey<Item> newTag(String sub) {
            return AllTags.modItem(prefix(sub) + "/" + name);
        }

        public Builder<P> color(int value) {
            assert (value & 0xFF000000) != 0;
            color = value;
            return this;
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
            var loc = item.getRegistryName();
            assert loc != null;
            put(sub, loc, () -> item);
            return this;
        }

        public Builder<P> existing(String sub, TagKey<Item> targetTag, Item item) {
            assert !items.containsKey(sub);
            var tag = newTag(sub);
            var loc = item.getRegistryName();
            assert loc != null;
            items.put(sub, new ItemEntry(loc, tag, () -> item, targetTag));
            return this;
        }

        public Builder<P> existing(String sub, Fluid fluid, int baseAmount) {
            assert !fluids.containsKey(sub);
            var loc = fluid.getRegistryName();
            assert loc != null;
            fluids.put(sub, new FluidEntry(loc, () -> fluid, baseAmount));
            return this;
        }

        public Builder<P> alias(String sub, String sub2) {
            var entry = items.get(sub2);
            assert entry != null;
            items.put(sub, entry.alias());
            return this;
        }

        private String newId(String sub) {
            var prefix = sub.startsWith("tool/") ? "" : "material/";
            return prefix + sub + "/" + name;
        }

        public Builder<P> dummy(String sub, Function<Item.Properties, ? extends Item> factory) {
            put(sub, () -> REGISTRATE.item(newId(sub), factory)
                .tint(color)
                .register());
            return this;
        }

        private void dummy(String sub) {
            dummy(sub, Item::new);
        }

        public Builder<P> dummies(String... subs) {
            for (var sub : subs) {
                dummy(sub);
            }
            return this;
        }

        public Builder<P> dust() {
            return dummies("dust");
        }

        public Builder<P> dustPrimary() {
            return dust().alias("primary", "dust");
        }

        public Builder<P> metal() {
            return dust().dummies("ingot")
                .alias("primary", "ingot");
        }

        public Builder<P> plate() {
            return metal().dummies("plate");
        }

        public Builder<P> stick() {
            return metal().dummies("stick", "dust_tiny");
        }

        public Builder<P> ring() {
            return stick().dummies("ring");
        }

        public Builder<P> nugget() {
            return metal().dummies("nugget", "dust_tiny");
        }

        public Builder<P> wire() {
            return metal().dummies("wire", "dust_tiny");
        }

        public Builder<P> wireFine() {
            return wire().dummies("wire_fine");
        }

        public Builder<P> metalExt() {
            return metal().plate().stick();
        }

        public Builder<P> foil() {
            return plate().dummies("foil", "dust_tiny");
        }

        public Builder<P> wireAndPlate() {
            return plate().wire();
        }

        public Builder<P> pipe() {
            return plate().dummies("pipe");
        }

        public Builder<P> magnetic() {
            return stick().dummies("magnetic");
        }

        public Builder<P> gear() {
            return metalExt().dummies("gear");
        }

        public Builder<P> bolt() {
            return metalExt().dummies("bolt");
        }

        public Builder<P> mechanical() {
            return bolt().dummies("screw");
        }

        public Builder<P> rotor() {
            return mechanical().dummies("ring", "rotor");
        }

        public Builder<P> hot() {
            return metal().dummies("ingot_hot");
        }

        public Builder<P> polymer() {
            return dummies("sheet")
                .alias("primary", "sheet")
                .molten();
        }

        public Builder<P> polymerRing() {
            return polymer().dummies("ring");
        }

        public Builder<P> polymerFoil() {
            return polymer().dummies("foil");
        }

        public Builder<P> gem() {
            return dummies("gem", "gem_flawless", "gem_exquisite", "lens")
                .alias("primary", "gem");
        }

        public Builder<P> fluid(String sub, ResourceLocation tex,
            int texColor, int displayColor, int baseAmount) {
            var fluid = simpleFluid("material/" + sub + "/" + name, tex, texColor, displayColor);
            fluids.put(sub, new FluidEntry(fluid.loc(), fluid, baseAmount));
            return this;
        }

        public Builder<P> fluid(String sub, ResourceLocation tex, int baseAmount) {
            return fluid(sub, tex, color, color, baseAmount);
        }

        public Builder<P> fluid(String sub, String tex, int displayColor, int baseAmount) {
            return fluid(sub, gregtech("blocks/fluids/fluid." + tex), 0xFFFFFFFF, displayColor, baseAmount);
        }

        public Builder<P> fluid(String sub, String tex, int baseAmount) {
            return fluid(sub, tex, color, baseAmount);
        }

        public Builder<P> fluidPrimary(String sub) {
            assert fluids.containsKey(sub);
            fluids.put("fluid", fluids.get(sub));
            return this;
        }

        public Builder<P> molten() {
            return fluid("molten", gregtech("blocks/material_sets/dull/liquid"), 144)
                .fluidPrimary("molten");
        }

        public Builder<P> liquid(String sub, int color) {
            return fluid(sub, gregtech("blocks/material_sets/dull/liquid"), color, color, 1000);
        }

        public Builder<P> liquid() {
            return liquid("liquid", color).fluidPrimary("liquid");
        }

        public Builder<P> gas(String sub, int color) {
            return fluid(sub, gregtech("blocks/material_sets/dull/gas"), color, color, 1000);
        }

        public Builder<P> gas() {
            return gas("gas", color).fluidPrimary("gas");
        }

        public Builder<P> rawOre(OreVariant variant) {
            oreVariant = variant;
            if (!blocks.containsKey("ore")) {
                var ore = REGISTRATE.block(newId("ore"), OreBlock.factory(variant))
                    .material(variant.baseBlock.defaultBlockState().getMaterial())
                    .properties(p -> p.strength(variant.destroyTime, variant.explodeResistance))
                    .translucent()
                    .tint(color)
                    .noBlockItem()
                    .register();
                blocks.put("ore", new BlockEntry(ore.loc(), ore));
            }
            return dummies("raw");
        }

        public Builder<P> ore(OreVariant variant) {
            return rawOre(variant)
                .dummies("crushed", "crushed_centrifuged", "crushed_purified")
                .dummies("dust_impure", "dust_pure")
                .dust();
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

            private ToolBuilder toolItem(String category) {
                return item(category, p -> new ToolItem(p, durability));
            }

            private ToolBuilder usableItem(String category, TagKey<Block> blockTag) {
                assert tier != null;
                return item(category, p -> new UsableToolItem(p, durability, tier, blockTag));
            }

            public ToolBuilder hammer() {
                return toolItem("hammer");
            }

            public ToolBuilder mortar() {
                return toolItem("mortar");
            }

            public ToolBuilder basic() {
                return hammer().mortar().toolItem("file")
                    .toolItem("saw").toolItem("screwdriver")
                    .usableItem("wrench", MINEABLE_WITH_WRENCH)
                    .usableItem("wire_cutter", MINEABLE_WITH_CUTTER);
            }

            @Override
            protected Unit createObject() {
                return Unit.INSTANCE;
            }
        }

        public Builder<P> toolSet(int durability, Tier tier) {
            return (new ToolBuilder(durability, tier)).basic().build();
        }

        public ToolBuilder tool(int durability) {
            return new ToolBuilder(durability, null);
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
