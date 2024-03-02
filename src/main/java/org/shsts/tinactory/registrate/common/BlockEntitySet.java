package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntitySet<T extends SmartBlockEntity, U extends SmartEntityBlock<T>> {
    public final RegistryEntry<SmartBlockEntityType<T>> blockEntityType;
    public final RegistryEntry<U> block;

    public BlockEntitySet(RegistryEntry<SmartBlockEntityType<T>> blockEntityType, RegistryEntry<U> block) {
        this.blockEntityType = blockEntityType;
        this.block = block;
    }

    public U getBlock() {
        return this.block.get();
    }
}
