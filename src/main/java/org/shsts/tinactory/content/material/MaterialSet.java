package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;

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

    private record BlockEntry(ResourceLocation loc, Supplier<? extends Block> block) {
        public Block getBlock() {
            return getEntry().get();
        }

        public Supplier<? extends Block> getEntry() {
            assert block != null;
            return block;
        }
    }

    private final Map<String, ItemEntry> items;
    private final Map<String, BlockEntry> blocks;
    @Nullable
    private final OreVariant oreVariant;

    private MaterialSet(Builder<?> builder) {
        this.name = builder.name;
        this.color = builder.color;
        this.items = builder.items;
        this.blocks = builder.blocks;
        this.oreVariant = builder.oreVariant;
    }

    public ResourceLocation loc(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).loc;
    }

    public TagKey<Item> tag(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).tag;
    }

    public Supplier<? extends Item> entry(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).item();
    }

    public Item item(String sub) {
        assert items.containsKey(sub);
        return items.get(sub).getItem();
    }

    public boolean isAlias(String sub) {
        return items.get(sub).isAlias;
    }

    public boolean hasTarget(String sub) {
        return items.get(sub).target != null;
    }

    public TagKey<Item> target(String sub) {
        var ret = items.get(sub).target;
        assert ret != null;
        return ret;
    }

    public boolean hasItem(String sub) {
        return items.containsKey(sub);
    }

    public Set<String> itemSubs() {
        return items.keySet();
    }

    public ResourceLocation blockLoc(String sub) {
        assert blocks.containsKey(sub);
        return blocks.get(sub).loc;
    }

    public Supplier<? extends Block> blockEntry(String sub) {
        assert blocks.containsKey(sub);
        return blocks.get(sub).getEntry();
    }

    public Block block(String sub) {
        assert blocks.containsKey(sub);
        return blocks.get(sub).getBlock();
    }

    public boolean hasBlock(String sub) {
        return blocks.containsKey(sub);
    }

    public OreVariant oreVariant() {
        assert oreVariant != null;
        return oreVariant;
    }

    public static class Builder<P> extends SimpleBuilder<MaterialSet, P, Builder<P>> {
        private final String name;
        private final Map<String, ItemEntry> items = new HashMap<>();
        private final Map<String, BlockEntry> blocks = new HashMap<>();
        private int color = 0xFFFFFFFF;
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

        public Builder<P> color(int value) {
            color = value;
            return this;
        }

        private void put(String sub, ResourceLocation loc, Supplier<? extends Item> item) {
            if (items.containsKey(sub)) {
                items.get(sub);
                return;
            }
            var tag = newTag(sub);
            var entry = new ItemEntry(loc, tag, item);
            items.put(sub, entry);
        }

        private void put(String sub, Supplier<RegistryEntry<? extends Item>> item) {
            if (items.containsKey(sub)) {
                items.get(sub);
                return;
            }
            var entry = item.get();
            put(sub, entry.loc, entry);
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

        private void dummy(String sub) {
            put(sub, () -> REGISTRATE.item(newId(sub), Item::new)
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
            return dummies("dust", "dust_tiny");
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
            return dummies("pipe");
        }

        public Builder<P> wire() {
            return dummies("wire");
        }

        public Builder<P> wireAndPlate() {
            return wire().dummies("plate");
        }

        public Builder<P> magnetic() {
            return dummies("magnetic");
        }

        // TODO
//        public class OreBuilder extends SimpleBuilder<Unit, Builder<P>, OreBuilder> {
//            private final int amount;
//            private boolean primitive = false;
//            private final OreVariant variant;
//            private final Map<String, Supplier<Item>> byproducts = new HashMap<>();
//
//            private OreBuilder(OreVariant variant, int amount) {
//                super(Builder.this);
//                this.variant = variant;
//                this.amount = amount;
//            }
//
//            public OreBuilder primitive() {
//                primitive = true;
//                return this;
//            }
//
//            public OreBuilder byproduct(String key, Supplier<Item> b1) {
//                byproducts.put(key, b1);
//                return this;
//            }
//
//            public OreBuilder byproduct(Supplier<Item> b1, Supplier<Item> b2, Supplier<Item> b3) {
//                byproducts.put("wash", b1);
//                byproducts.put("centrifuge", b2);
//                byproducts.put("thermal_centrifuge", b3);
//                return this;
//            }
//
//            public OreBuilder byproduct(Supplier<Item> b1) {
//                return byproduct(b1, b1, b1);
//            }
//
//            private void crush(String output, String input) {
//                callbacks.add($ -> MACERATOR.recipe($.loc(output))
//                        .inputItem(0, $.tag(input), 1)
//                        .outputItem(1, $.entry(output), input.equals("raw") ? 2 * amount : 1)
//                        .voltage(Voltage.LV)
//                        .workTicks((long) (variant.destroyTime * 40f))
//                        .build());
//            }
//
//            private void wash(String output, String input, Voltage voltage) {
//                callbacks.add($ -> {
//                    var loc = $.loc(output);
//                    if (input.equals("dust_pure")) {
//                        loc = new ResourceLocation(loc.getNamespace(), loc.getPath() + "_from_pure");
//                    }
//                    var builder = ORE_WASHER.recipe(loc)
//                            .inputItem(0, $.tag(input), 1)
//                            .outputItem(2, $.entry(output), 1);
//                    if (input.equals("crushed")) {
//                        var byproduct = byproducts.getOrDefault("wash", $.entry("dust"));
//                        builder.outputItem(4, byproduct, 1, 0.1)
//                                .workTicks(200);
//                    } else {
//                        builder.workTicks(32);
//                    }
//                    builder.voltage(voltage)
//                            .build();
//                });
//            }
//
//            @Override
//            protected Unit createObject() {
//                var raw = put("raw", () -> REGISTRATE.item(newId("raw"), Item::new)
//                        .model(ModelGen.basicItem(modLoc("items/material/raw")))
//                        .tint(color)
//                        .register());
//
//                dust().dummies("crushed", "crushed_centrifuged", "crushed_purified")
//                        .dummies("dust_impure", "dust_pure");
//
//                oreVariant = variant;
//                if (!blocks.containsKey("ore")) {
//                    var ore = REGISTRATE.block(newId("ore"), OreBlock.factory(variant))
//                            .material(variant.baseBlock.defaultBlockState().getMaterial())
//                            .properties(p -> p.strength(variant.destroyTime, variant.explodeResistance))
//                            .transform(ModelGen.oreBlock(variant))
//                            .tint(color)
//                            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
//                            .tag(variant.mineTier.getTag())
//                            .drop(raw::getItem)
//                            .register();
//                    blocks.put("ore", ore::get);
//                }
//
//                if (variant.voltage.rank <= Voltage.ULV.rank) {
//                    Builder.this.process("crushed", amount, "raw", TOOL_HAMMER);
//                    Builder.this.process("dust_pure", 1, "crushed_purified", TOOL_HAMMER);
//                    Builder.this.process("dust_impure", 1, "crushed", TOOL_HAMMER);
//                }
//
//                crush("crushed", "raw");
//                crush("dust_impure", "crushed");
//                crush("dust_pure", "crushed_purified");
//                crush("dust", "crushed_centrifuged");
//                wash("crushed_purified", "crushed", Voltage.ULV);
//                wash("dust", "dust_impure", primitive ? Voltage.PRIMITIVE : Voltage.ULV);
//                wash("dust", "dust_pure", Voltage.ULV);
//                callbacks.add($ -> {
//                    var byproduct = byproducts.getOrDefault("centrifuge", $.entry("dust"));
//                    CENTRIFUGE.recipe($.loc("dust"))
//                            .inputItem(0, $.tag("dust_pure"), 1)
//                            .outputItem(2, $.entry("dust"), 1)
//                            .outputItem(2, byproduct, 1, 0.1)
//                            .voltage(Voltage.LV)
//                            .workTicks(80)
//                            .build();
//                });
//                callbacks.add($ -> {
//                    var byproduct = byproducts.getOrDefault("thermal_centrifuge", $.entry("dust"));
//                    THERMAL_CENTRIFUGE.recipe($.loc("crushed_centrifuged"))
//                            .inputItem(0, $.tag("crushed_purified"), 1)
//                            .outputItem(1, $.entry("crushed_centrifuged"), 1)
//                            .outputItem(1, byproduct, 1, 0.1)
//                            .build();
//                });
//
//                return Unit.INSTANCE;
//            }
//        }

        public Builder<P> ore(OreVariant variant) {
            oreVariant = variant;
            if (!blocks.containsKey("ore")) {
                var ore = REGISTRATE.block(newId("ore"), OreBlock.factory(variant))
                        .material(variant.baseBlock.defaultBlockState().getMaterial())
                        .properties(p -> p.strength(variant.destroyTime, variant.explodeResistance))
                        .tint(color)
                        .register();
                blocks.put("ore", new BlockEntry(ore.loc, ore));
            }
            return dummies("raw", "crushed", "crushed_centrifuged", "crushed_purified")
                    .dummies("dust_impure", "dust_pure").dust();
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
            // TODO
//
//            private void tool(String category, String pattern, Object... args) {
//                process(toolItem(category), 1, pattern, args);
//            }
//
//            private void usable(String category, TagKey<Block> blockTag, String pattern, Object... args) {
//                process(usableItem(category, blockTag), 1, pattern, args);
//            }
//
//            public ToolBuilder hammer() {
//                tool("hammer", "AA \nAAB\nAA ", "primary", TOOL_HANDLE);
//                return this;
//            }
//
//            public ToolBuilder mortar() {
//                tool("mortar", " A \nBAB\nBBB", "primary", ItemTags.STONE_TOOL_MATERIALS);
//                return this;
//            }
//
//            public ToolBuilder basic() {
//                tool("file", "A\nA\nB", "plate", TOOL_HANDLE);
//                tool("saw", "AAB\n  B", "plate", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
//                tool("screwdriver", "  A\n A \nB  ", "stick", TOOL_HANDLE, TOOL_FILE, TOOL_HAMMER);
//                usable("wrench", MINEABLE_WITH_WRENCH, "A A\nAAA\n A ", "plate", TOOL_HAMMER);
//                usable("wire_cutter", MINEABLE_WITH_CUTTER, "A A\n A \nBCB", "plate", TOOL_HANDLE, TOOL_SCREW,
//                        TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER);
//                return hammer().mortar();
//            }

            public ToolBuilder hammer() {
                return toolItem("hammer");
            }

            public ToolBuilder mortar() {
                return toolItem("mortar");
            }

            public ToolBuilder basic() {
                return hammer().mortar().toolItem("file").toolItem("saw").toolItem("screwdriver")
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
}
