package org.shsts.tinactory.registrate.builder;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class EntityBlockBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>, P>
        extends BlockBuilder<U, P, EntityBlockBuilder<T, U, P>> {

    @FunctionalInterface
    public interface Factory<T1 extends SmartBlockEntity, U1 extends SmartEntityBlock<T1>> {
        U1 create(BlockBehaviour.Properties properties, Supplier<SmartBlockEntityType<T1>> entityType);
    }

    @Nullable
    private Supplier<Supplier<SmartBlockEntityType<T>>> entityType = null;

    public EntityBlockBuilder<T, U, P> type(Supplier<Supplier<SmartBlockEntityType<T>>> entityType) {
        this.entityType = entityType;
        return self();
    }

    public EntityBlockBuilder(Registrate registrate, String id, P parent, Factory<T, U> factory) {
        super(registrate, id, parent);
        this.factory = properties -> {
            assert entityType != null;
            return factory.create(properties, entityType.get());
        };
    }
}
