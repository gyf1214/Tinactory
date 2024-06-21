package org.shsts.tinactory.datagen.handler;

import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.DataGen;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.LocHelper.prepend;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LootTableHandler extends DataHandler<LootTableProvider> {
    public LootTableHandler(DataGen dataGen) {
        super(dataGen);
    }

    public static class Loot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
        private final Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

        private LootTable.Builder getBuilder(ResourceLocation loc) {
            return tables.computeIfAbsent(loc, $ -> LootTable.lootTable());
        }

        public void dropSingle(ResourceLocation loc, ItemLike item, float chance) {
            var pool = LootPool.lootPool()
                    .when(ExplosionCondition.survivesExplosion())
                    .add(LootItem.lootTableItem(item));
            if (chance < 1f) {
                pool.when(LootItemRandomChanceCondition.randomChance(chance));
            }
            getBuilder(loc).withPool(pool);
        }

        public void dropOnState(ResourceLocation loc, ItemLike item, Block block,
                                BooleanProperty prop, boolean value) {
            getBuilder(loc).withPool(LootPool.lootPool()
                    .when(ExplosionCondition.survivesExplosion())
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                    .hasProperty(prop, value)))
                    .add(LootItem.lootTableItem(item)));
        }

        public void dropOnTool(ResourceLocation loc, ItemLike item, TagKey<Item> tool) {
            var pool = LootPool.lootPool()
                    .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(tool))
                            .or(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                    .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH,
                                            MinMaxBounds.Ints.atLeast(1))))))
                    .add(LootItem.lootTableItem(item));
            getBuilder(loc).withPool(pool);
        }

        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> writer) {
            for (var table : tables.entrySet()) {
                writer.accept(table.getKey(), table.getValue());
            }
        }

        public Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> toFactory() {
            return () -> this;
        }
    }

    public class Provider extends LootTableProvider {
        private final Loot blockLoot = new Loot();
        private final List<Pair<Supplier<Consumer<BiConsumer<
                ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables;

        public Provider(GatherDataEvent event) {
            super(event.getGenerator());
            var lootMaps = List.of(
                    Pair.of(blockLoot, LootContextParamSets.BLOCK));
            tables = lootMaps.stream()
                    .map(p -> Pair.of(p.getFirst().toFactory(), p.getSecond()))
                    .toList();
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> tables, ValidationContext ctx) {
            tables.forEach((name, table) -> LootTables.validate(ctx, name, table));
        }

        @Override
        protected List<Pair<Supplier<Consumer<BiConsumer<
                ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
            LootTableHandler.this.register(this);
            return tables;
        }
    }

    @Override
    protected LootTableProvider createProvider(GatherDataEvent event) {
        return new Provider(event);
    }

    public void dropSingle(ResourceLocation loc, Supplier<? extends ItemLike> item, float chance) {
        var loc1 = prepend(loc, "blocks");
        addCallback(prov -> ((Provider) prov).blockLoot.dropSingle(loc1, item.get(), chance));
    }

    public void dropOnState(ResourceLocation loc, Supplier<? extends ItemLike> item,
                            Supplier<? extends Block> block, BooleanProperty prop, boolean value) {
        var loc1 = prepend(loc, "blocks");
        addCallback(prov -> ((Provider) prov).blockLoot.dropOnState(
                loc1, item.get(), block.get(), prop, value));
    }

    public void dropOnTool(ResourceLocation loc, Supplier<? extends ItemLike> item, TagKey<Item> tool) {
        var loc1 = prepend(loc, "blocks");
        addCallback(prov -> ((Provider) prov).blockLoot.dropOnTool(loc1, item.get(), tool));
    }
}
