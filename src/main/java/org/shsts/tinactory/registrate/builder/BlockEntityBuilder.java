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

    public BlockEntityBuilder<U, P>
    capability(RegistryEntry<? extends CapabilityProviderType<? super U, ?>> cap) {
        capabilities.put(cap.loc, be -> cap.get().getBuilder().apply(be));
        return self();
    }

    @SuppressWarnings("unchecked")
    public <U1 extends BlockEntity, B extends Function<U1, ICapabilityProvider>>
    BlockEntityBuilder<U, P>
    capability(RegistryEntry<? extends CapabilityProviderType<U1, B>> cap, Transformer<B> transform) {
        capabilities.put(cap.loc, be -> transform.apply(cap.get().getBuilder()).apply((U1) be));
        return self();
    }

    public BlockEntityBuilder<U, P>
    capability(String id, Function<? super U, ? extends ICapabilityProvider> factory) {
        var loc = new ResourceLocation(registrate.modid, id);
        capabilities.put(loc, factory);
        return self();
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
