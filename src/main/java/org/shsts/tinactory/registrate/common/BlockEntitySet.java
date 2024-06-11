package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntitySet<T extends SmartBlockEntity, U extends SmartEntityBlock<T>> {
    private final RegistryEntry<SmartBlockEntityType<T>> blockEntityType;
    private final RegistryEntry<U> block;

    public BlockEntitySet(RegistryEntry<SmartBlockEntityType<T>> blockEntityType, RegistryEntry<U> block) {
        this.blockEntityType = blockEntityType;
        this.block = block;
    }

    public SmartBlockEntityType<T> blockEntityType() {
        return blockEntityType.get();
    }

    public U block() {
        return block.get();
    }

    public RegistryEntry<U> entry() {
        return block;
    }
}
