package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockItemBuilder<U extends BlockItem, P extends BlockBuilder<?, ?, ?>>
        extends ItemBuilder<U, P, BlockItemBuilder<U, P>> {

    @FunctionalInterface
    public interface Factory<U1 extends BlockItem> {
        U1 create(Block block, Item.Properties properties);
    }

    public BlockItemBuilder(Registrate registrate, P parent, Factory<U> factory) {
        super(registrate, parent.id, parent, properties -> {
            assert parent.entry != null;
            return factory.create(parent.entry.get(), properties);
        });
        onBuild.add($ -> parent.onCreateEntry.add($p -> $.register()));
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        if (modelCallback == null) {
            modelCallback = parent.getItemModel();
        }
        if (tint == null) {
            tint = parent.getItemTint().orElse(null);
        }
        return super.createEntry();
    }
}
