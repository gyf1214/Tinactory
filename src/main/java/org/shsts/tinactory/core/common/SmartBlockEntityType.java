package org.shsts.tinactory.core.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.core.gui.SmartMenuType;

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
    public final Supplier<SmartMenuType<T, ?>> menu;

    private final boolean eventManager;
    private final Map<ResourceLocation, Function<? super T, ? extends ICapabilityProvider>> capabilities;

    @SuppressWarnings("ConstantConditions")
    public SmartBlockEntityType(BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks,
                                Class<T> entityClass, boolean ticking, boolean eventManager,
                                Map<ResourceLocation, Function<? super T, ? extends ICapabilityProvider>> capabilities,
                                @Nullable Supplier<SmartMenuType<T, ?>> menu) {
        super(factory, validBlocks, null);
        this.entityClass = entityClass;
        this.ticking = ticking;
        this.eventManager = eventManager;
        this.capabilities = capabilities;
        this.menu = menu;
    }

    public T cast(BlockEntity be) {
        return entityClass.cast(be);
    }

    private static void attachCapability(AttachCapabilitiesEvent<BlockEntity> e, UpdateHelper helper,
                                         ResourceLocation loc, ICapabilityProvider provider) {
        e.addCapability(loc, provider);
        helper.attachCapability(loc, provider);
    }

    public void attachCapabilities(AttachCapabilitiesEvent<BlockEntity> e) {
        var be = cast(e.getObject());
        var updateHelper = new UpdateHelper();
        e.addCapability(UpdateHelper.LOC, updateHelper);
        EventManager eventManager = null;
        if (this.eventManager) {
            eventManager = new EventManager();
            attachCapability(e, updateHelper, EventManager.LOC, eventManager);
        }
        for (var capEntry : capabilities.entrySet()) {
            var cap = capEntry.getValue().apply(be);
            attachCapability(e, updateHelper, capEntry.getKey(), cap);
            if (this.eventManager && cap instanceof IEventSubscriber subscriber) {
                subscriber.subscribeEvents(eventManager);
            }
        }
    }
}
