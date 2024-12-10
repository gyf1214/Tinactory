package org.shsts.tinactory.registrate.builder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.DistLazy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityTypeBuilder<U extends SmartBlockEntity, P> extends
    RegistryEntryBuilder<BlockEntityType<?>, SmartBlockEntityType<U>, P, BlockEntityTypeBuilder<U, P>> {

    @FunctionalInterface
    public interface Factory<U1 extends SmartBlockEntity> {
        U1 create(BlockEntityType<U1> type, BlockPos pos, BlockState state);
    }

    private final Factory<U> factory;
    private final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();
    private boolean ticking = false;
    private boolean eventManager = false;
    @Nullable
    private Class<U> entityClass = null;
    private final Map<ResourceLocation, Function<? super U, ? extends ICapabilityProvider>>
        capabilities = new HashMap<>();
    @Nullable
    private Supplier<MenuType<?>> menu = null;

    public BlockEntityTypeBuilder(Registrate registrate, String id, P parent, Factory<U> factory) {
        super(registrate, registrate.blockEntityHandler, id, parent);
        this.factory = factory;
    }

    public BlockEntityTypeBuilder<U, P> entityClass(Class<U> clazz) {
        entityClass = clazz;
        return self();
    }

    @SafeVarargs
    public final BlockEntityTypeBuilder<U, P> validBlock(Supplier<? extends Block>... blocks) {
        validBlocks.addAll(List.of(blocks));
        return self();
    }

    public BlockEntityTypeBuilder<U, P> ticking(boolean value) {
        ticking = value;
        return self();
    }

    public BlockEntityTypeBuilder<U, P> ticking() {
        return ticking(true);
    }

    public BlockEntityTypeBuilder<U, P> eventManager(boolean value) {
        eventManager = value;
        return self();
    }

    public BlockEntityTypeBuilder<U, P> eventManager() {
        return eventManager(true);
    }

    public BlockEntityTypeBuilder<U, P> setMenu(Supplier<MenuType<?>> value) {
        menu = value;
        return this;
    }

    public <C extends CapabilityProviderBuilder<? super U, BlockEntityTypeBuilder<U, P>>> C capability(
        Function<BlockEntityTypeBuilder<U, P>, C> builderFactory) {
        var ret = builderFactory.apply(this);
        ret.onCreateObject(factory -> capabilities.put(ret.loc, factory));
        return ret;
    }

    public <C extends CapabilityProviderBuilder<? super U,
        BlockEntityTypeBuilder<U, P>>> BlockEntityTypeBuilder<U, P> simpleCapability(
        Function<BlockEntityTypeBuilder<U, P>, C> builderFactory) {
        return capability(builderFactory).build();
    }

    public BlockEntityTypeBuilder<U, P> renderer(DistLazy<BlockEntityRendererProvider<U>> renderer) {
        onCreateObject(type -> renderer.runOnDist(Dist.CLIENT, () -> provider ->
            registrate.rendererHandler.setBlockEntityRenderer(type, provider)));
        return self();
    }

    @Override
    protected SmartBlockEntityType<U> createObject() {
        var entry = this.entry;
        var factory = this.factory;
        assert entry != null;
        assert entityClass != null;
        return new SmartBlockEntityType<>((pos, state) -> factory.create(entry.get(), pos, state),
            validBlocks.stream().map(Supplier::get).collect(Collectors.toSet()),
            entityClass, ticking, eventManager, this.capabilities, menu);
    }
}
