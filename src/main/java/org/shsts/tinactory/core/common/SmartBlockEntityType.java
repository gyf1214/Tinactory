package org.shsts.tinactory.core.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.gui.ContainerMenuType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
public class SmartBlockEntityType<T extends BlockEntity> extends BlockEntityType<T> {
    public final Class<T> entityClass;
    public final boolean ticking;
    @Nullable
    public final Supplier<ContainerMenuType<T, ?>> menu;

    private final boolean hasEvent;
    private final Map<ResourceLocation, Function<? super T, ? extends ICapabilityProvider>> capabilities;

    @SuppressWarnings("ConstantConditions")
    public SmartBlockEntityType(BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks,
                                Class<T> entityClass, boolean ticking, boolean hasEvent,
                                Map<ResourceLocation, Function<? super T, ? extends ICapabilityProvider>> capabilities,
                                @Nullable Supplier<ContainerMenuType<T, ?>> menu) {
        super(factory, validBlocks, null);
        this.entityClass = entityClass;
        this.ticking = ticking;
        this.hasEvent = hasEvent;
        this.capabilities = capabilities;
        this.menu = menu;
    }

    public T cast(BlockEntity be) {
        return this.entityClass.cast(be);
    }

    public void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> e) {
        var be = this.cast(e.getObject());
        EventManager eventManager = null;
        if (this.hasEvent) {
            eventManager = new EventManager();
            e.addCapability(new ResourceLocation(Tinactory.ID, "event_manager"), eventManager);
        }
        for (var capEntry : this.capabilities.entrySet()) {
            var cap = capEntry.getValue().apply(be);
            e.addCapability(capEntry.getKey(), cap);
            if (this.hasEvent && cap instanceof IEventSubscriber subscriber) {
                subscriber.subscribeEvents(eventManager);
            }
        }
    }
}
