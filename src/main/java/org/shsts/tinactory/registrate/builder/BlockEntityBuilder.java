package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.shsts.tinactory.core.common.CapabilityProviderType;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.ContainerMenuType;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RegistryEntry;

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
public class BlockEntityBuilder<U extends SmartBlockEntity, P, S extends BlockEntityBuilder<U, P, S>>
        extends RegistryEntryBuilder<BlockEntityType<?>, SmartBlockEntityType<U>, P, S> {

    @FunctionalInterface
    public interface Factory<U1 extends SmartBlockEntity> {
        U1 create(BlockEntityType<U1> type, BlockPos pos, BlockState state);
    }

    protected final Factory<U> factory;
    protected final Set<Supplier<? extends Block>> validBlocks = new HashSet<>();
    protected boolean ticking = false;
    protected boolean hasEvent = false;
    @Nullable
    protected Class<U> entityClass = null;
    protected final Map<ResourceLocation, Function<? super U, ? extends ICapabilityProvider>> capabilities =
            new HashMap<>();
    @Nullable
    protected Supplier<ContainerMenuType<U, ?>> menu = null;

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

    public S hasEvent(boolean hasEvent) {
        this.hasEvent = hasEvent;
        return self();
    }

    public S hasEvent() {
        return this.hasEvent(true);
    }

    public void setMenu(Supplier<ContainerMenuType<U, ?>> menu) {
        this.menu = menu;
    }

    private class SimpleMenuBuilder<M extends ContainerMenu<U>> extends MenuBuilder<U, M, S, SimpleMenuBuilder<M>> {
        public SimpleMenuBuilder(String id, ContainerMenu.Factory<U, M> factory) {
            super(BlockEntityBuilder.this.registrate, id, BlockEntityBuilder.this.self(), factory);
        }
    }

    public <M extends ContainerMenu<U>> MenuBuilder<U, M, S, ?>
    menu(ContainerMenu.Factory<U, M> factory) {
        return new SimpleMenuBuilder<>(this.id, factory);
    }

    public MenuBuilder<U, ContainerMenu<U>, S, ?> menu() {
        return this.menu(ContainerMenu::new);
    }

    public S capability(RegistryEntry<? extends CapabilityProviderType<? super U, ?>> cap) {
        this.capabilities.put(cap.loc, be -> cap.get().getBuilder().apply(be));
        return self();
    }

    @SuppressWarnings("unchecked")
    public <U1 extends BlockEntity, B extends Function<U1, ICapabilityProvider>>
    S capability(RegistryEntry<? extends CapabilityProviderType<U1, B>> cap, Transformer<B> transform) {
        this.capabilities.put(cap.loc, be -> transform.apply(cap.get().getBuilder()).apply((U1) be));
        return self();
    }

    public S capability(String id, Function<? super U, ? extends ICapabilityProvider> factory) {
        var loc = new ResourceLocation(this.registrate.modid, id);
        this.capabilities.put(loc, factory);
        return self();
    }

    @Override
    public SmartBlockEntityType<U> createObject() {
        var entry = this.entry;
        var entityClass = this.entityClass;
        var ticking = this.ticking;
        var hasEvent = this.hasEvent;
        var factory = this.factory;
        var menu = this.menu;
        assert entry != null;
        assert entityClass != null;
        return new SmartBlockEntityType<>((pos, state) -> factory.create(entry.get(), pos, state),
                validBlocks.stream().map(Supplier::get).collect(Collectors.toSet()),
                entityClass, ticking, hasEvent, this.capabilities, menu);
    }
}
