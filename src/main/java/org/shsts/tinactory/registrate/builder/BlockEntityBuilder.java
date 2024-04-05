package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.ContainerMenuType;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityBuilder<U extends SmartBlockEntity, P> extends RegistryEntryBuilder<BlockEntityType<?>,
        SmartBlockEntityType<U>, P, BlockEntityBuilder<U, P>> {

    @FunctionalInterface
    public interface Factory<U1 extends SmartBlockEntity> {
        U1 create(BlockEntityType<U1> type, BlockPos pos, BlockState state);
    }

    private final Factory<U> factory;
    private final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();
    private boolean ticking = false;
    private boolean hasEvent = false;
    @Nullable
    private Class<U> entityClass = null;
    private final Map<ResourceLocation, Function<? super U, ? extends ICapabilityProvider>>
            capabilities = new HashMap<>();
    @Nullable
    private Supplier<ContainerMenuType<U, ?>> menu = null;

    public BlockEntityBuilder(Registrate registrate, String id, P parent, Factory<U> factory) {
        super(registrate, registrate.blockEntityHandler, id, parent);
        this.factory = factory;
    }

    public BlockEntityBuilder<U, P> entityClass(Class<U> clazz) {
        entityClass = clazz;
        return self();
    }

    @SafeVarargs
    public final BlockEntityBuilder<U, P> validBlock(Supplier<? extends Block>... blocks) {
        validBlocks.addAll(Arrays.asList(blocks));
        return self();
    }

    public BlockEntityBuilder<U, P> ticking(boolean value) {
        ticking = value;
        return self();
    }

    public BlockEntityBuilder<U, P> ticking() {
        return ticking(true);
    }

    public BlockEntityBuilder<U, P> hasEvent(boolean value) {
        hasEvent = value;
        return self();
    }

    public BlockEntityBuilder<U, P> hasEvent() {
        return hasEvent(true);
    }

    public void setMenu(Supplier<ContainerMenuType<U, ?>> value) {
        menu = value;
    }

    public <M extends ContainerMenu<U>> MenuBuilder<U, M, BlockEntityBuilder<U, P>>
    menu(ContainerMenu.Factory<U, M> factory) {
        return new MenuBuilder<>(registrate, id, this, factory);
    }

    public MenuBuilder<U, ContainerMenu<U>, BlockEntityBuilder<U, P>> menu() {
        return menu(ContainerMenu::new);
    }

    public <C extends CapabilityProviderBuilder<? super U, BlockEntityBuilder<U, P>>>
    C capability(Function<BlockEntityBuilder<U, P>, C> builderFactory) {
        var ret = builderFactory.apply(this);
        ret.onCreateObject(factory -> capabilities.put(ret.loc, factory));
        return ret;
    }

    public <C extends CapabilityProviderBuilder<? super U, BlockEntityBuilder<U, P>>>
    BlockEntityBuilder<U, P> simpleCapability(Function<BlockEntityBuilder<U, P>, C> builderFactory) {
        return capability(builderFactory).build();
    }

    @Override
    public SmartBlockEntityType<U> createObject() {
        var entry = this.entry;
        var factory = this.factory;
        assert entry != null;
        assert entityClass != null;
        return new SmartBlockEntityType<>((pos, state) -> factory.create(entry.get(), pos, state),
                validBlocks.stream().map(Supplier::get).collect(Collectors.toSet()),
                entityClass, ticking, hasEvent, this.capabilities, menu);
    }
}
