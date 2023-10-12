package org.shsts.tinactory.registrate.builder;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.registrate.IBlockParent;
import org.shsts.tinactory.registrate.IItemParent;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class EntityBlockBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>,
        P extends IBlockParent & IItemParent, S extends EntityBlockBuilder<T, U, P, S>>
        extends BlockBuilder<U, P, S> {

    @FunctionalInterface
    public interface Factory<T1 extends SmartBlockEntity, U1 extends SmartEntityBlock<T1>> {
        U1 create(BlockBehaviour.Properties properties, Supplier<SmartBlockEntityType<T1>> entityType);
    }

    @Nullable
    protected Supplier<Supplier<SmartBlockEntityType<T>>> entityType = null;

    public S type(Supplier<Supplier<SmartBlockEntityType<T>>> entityType) {
        this.entityType = entityType;
        return self();
    }

    public EntityBlockBuilder(Registrate registrate, String id, P parent, Factory<T, U> factory) {
        super(registrate, id, parent);
        this.factory = properties -> {
            assert this.entityType != null;
            return factory.create(properties, this.entityType.get());
        };
    }
}
