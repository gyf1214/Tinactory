package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

/**
 * Machine that can run without a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveMachine extends CapabilityProvider implements IEventSubscriber {
    private final BlockEntity blockEntity;

    public PrimitiveMachine(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private void onServerTick(Level world) {
        var workSpeed = TinactoryConfig.INSTANCE.primitiveWorkSpeed.get();
        var processor = AllCapabilities.PROCESSOR.get(blockEntity);
        processor.onPreWork();
        processor.onWorkTick(workSpeed);
        var working = processor.getProgress() > 0d;
        var state = blockEntity.getBlockState();
        if (state.getValue(WORKING) != working) {
            world.setBlock(blockEntity.getBlockPos(), state.setValue(WORKING, working), 3);
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_TICK, this::onServerTick);
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "machine/primitive",
            PrimitiveMachine::new);
    }
}
