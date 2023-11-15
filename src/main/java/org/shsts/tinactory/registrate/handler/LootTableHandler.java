package org.shsts.tinactory.registrate.handler;

import com.mojang.datafixers.util.Pair;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
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
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LootTableHandler extends DataHandler<LootTableHandler.Provider> {
    public LootTableHandler(Registrate registrate) {
        super(registrate);
    }

    public static class Loot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
        private final Map<ResourceLocation, LootTable.Builder> tables = new HashMap<>();

        public void addTable(ResourceLocation loc, LootTable.Builder table) {
            this.tables.put(loc, table);
        }

        public void dropSingle(ResourceLocation loc, ItemLike item) {
            this.tables.put(loc, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .when(ExplosionCondition.survivesExplosion())
                            .add(LootItem.lootTableItem(item))));
        }

        @Override
        public void accept(BiConsumer<ResourceLocation, LootTable.Builder> writer) {
            for (var table : this.tables.entrySet()) {
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
                    Pair.of(this.blockLoot, LootContextParamSets.BLOCK)
            );
            this.tables = lootMaps.stream()
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

    public void blockLoot(Consumer<Loot> cb) {
        this.callbacks.add(prov -> cb.accept(prov.blockLoot));
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(new Provider(event));
    }
}
