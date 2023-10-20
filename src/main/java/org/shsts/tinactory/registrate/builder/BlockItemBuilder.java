package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockItemBuilder<U extends BlockItem, P extends BlockBuilder<?, ?, ?>, S extends BlockItemBuilder<U, P, S>>
        extends ItemBuilder<U, P, S> {

    @FunctionalInterface
    public interface Factory<U1 extends BlockItem> {
        U1 create(Block block, Item.Properties properties);
    }

    public BlockItemBuilder(Registrate registrate, P parent, Factory<U> factory) {
        super(registrate, parent.id, parent, properties -> {
            assert parent.entry != null;
            return factory.create(parent.entry.get(), properties);
        });
    }

    @Override
    public RegistryEntry<U> register() {
        if (this.modelCallback == null) {
            this.parent.buildItemModels(this);
        }
        if (this.tint == null) {
            this.tint = this.parent.getItemTint().orElse(null);
        }
        return super.register();
    }

    @Override
    public P build() {
        this.parent.onCreateEntry.add($ -> this.register());
        return this.parent;
    }
}
