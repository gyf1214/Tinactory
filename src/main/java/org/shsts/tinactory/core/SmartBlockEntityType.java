package org.shsts.tinactory.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@ParametersAreNonnullByDefault
public class SmartBlockEntityType<T extends SmartBlockEntity> extends BlockEntityType<T> {
    public final Class<T> entityClass;
    public final boolean ticking;

    @SuppressWarnings("ConstantConditions")
    public SmartBlockEntityType(BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks,
                                Class<T> entityClass, boolean ticking) {
        super(factory, validBlocks, null);
        this.entityClass = entityClass;
        this.ticking = ticking;
    }
}
