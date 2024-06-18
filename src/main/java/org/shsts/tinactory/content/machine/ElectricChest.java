package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricChest extends CapabilityProvider
        implements IEventSubscriber, INBTSerializable<CompoundTag> {
    private final WrapperItemHandler itemHandler;
    private final LazyOptional<IItemHandler> view;
    private final IItemCollection port;

    public ElectricChest(Layout layout) {
        this.itemHandler = new WrapperItemHandler(layout.slots.size());
        this.view = LazyOptional.of(() -> itemHandler);
        this.port = new ItemHandlerCollection(itemHandler);
    }

    private void onConnect(Network network) {
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        logistics.addStorage(port);
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.CONNECT, this::onConnect);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return view.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return ItemHelper.serializeItemHandler(itemHandler);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(itemHandler, tag);
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>>
    builder(Layout layout) {
        return CapabilityProviderBuilder.fromFactory("machine/chest", $ -> new ElectricChest(layout));
    }
}
