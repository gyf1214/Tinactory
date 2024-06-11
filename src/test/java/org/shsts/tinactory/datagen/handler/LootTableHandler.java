package org.shsts.tinactory.datagen.handler;

import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
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

        public void dropSingle(ResourceLocation loc, ItemLike item) {
            tables.put(loc, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(item))));
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

    public void dropSingle(ResourceLocation loc, Supplier<? extends Item> item) {
        var loc1 = prepend(loc, "blocks");
        addCallback(prov -> ((Provider) prov).blockLoot.dropSingle(loc1, item.get()));
    }
}
