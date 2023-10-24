package org.shsts.tinactory.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.gui.ContainerMenuType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class SmartBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {
    public final Class<T> entityClass;
    public final boolean ticking;
    public final List<Supplier<CapabilityProviderType<? super T, ?>>> capabilities;
    @Nullable
    public final Supplier<ContainerMenuType<T, ?>> menu;

    @SuppressWarnings("ConstantConditions")
    public SmartBlockEntityType(BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks,
                                Class<T> entityClass, boolean ticking,
                                List<Supplier<CapabilityProviderType<? super T, ?>>> capabilities,
                                @Nullable Supplier<ContainerMenuType<T, ?>> menu) {
        super(factory, validBlocks, null);
        this.entityClass = entityClass;
        this.ticking = ticking;
        this.capabilities = capabilities;
        this.menu = menu;
    }

    public T cast(BlockEntity be) {
        return this.entityClass.cast(be);
    }

    public void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        var be = this.cast(event.getObject());
        for (var capSupplier : this.capabilities) {
            var cap = capSupplier.get();
            var capProvider = cap.create(be);
            event.addCapability(cap.getRegistryName(), capProvider);
        }
    }
}
