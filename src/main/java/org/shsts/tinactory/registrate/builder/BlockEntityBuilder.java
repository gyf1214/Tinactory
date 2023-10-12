package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityBuilder<U extends SmartBlockEntity, P, S extends BlockEntityBuilder<U, P, S>>
        extends RegistryEntryBuilder<BlockEntityType<?>, SmartBlockEntityType<U>, P, S> {

    @FunctionalInterface
    public interface Factory<U1 extends SmartBlockEntity> {
        U1 create(BlockEntityType<U1> type, BlockPos pos, BlockState state);
    }

    protected final Factory<U> factory;
    protected final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();
    protected boolean ticking = false;
    @Nullable
    protected Class<U> entityClass = null;

    public BlockEntityBuilder(Registrate registrate, String id, P parent, Factory<U> factory) {
        super(registrate, registrate.blockEntityHandler, id, parent);
        this.factory = factory;
    }

    public S entityClass(Class<U> clazz) {
        this.entityClass = clazz;
        return self();
    }

    @SafeVarargs
    public final S validBlock(Supplier<? extends Block>... blocks) {
        this.validBlocks.addAll(Arrays.asList(blocks));
        return self();
    }

    public S ticking(boolean ticking) {
        this.ticking = ticking;
        return self();
    }

    public S ticking() {
        return this.ticking(true);
    }

    @Override
    public SmartBlockEntityType<U> buildObject() {
        var entry = this.entry;
        var entityClass = this.entityClass;
        var ticking = this.ticking;
        var factory = this.factory;
        assert entry != null;
        assert entityClass != null;
        return new SmartBlockEntityType<>((pos, state) -> factory.create(entry.get(), pos, state),
                validBlocks.stream().map(Supplier::get).collect(Collectors.toSet()), entityClass, ticking);
    }
}
