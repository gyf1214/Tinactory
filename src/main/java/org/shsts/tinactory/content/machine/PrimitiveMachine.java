package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.SERVER_TICK;
import static org.shsts.tinactory.content.network.MachineBlock.WORKING;

/**
 * Machine that can run without a network.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitiveMachine extends CapabilityProvider implements IMachine, IEventSubscriber {
    private static final String ID = "machine/primitive";

    private final BlockEntity blockEntity;

    public PrimitiveMachine(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, PrimitiveMachine::new);
    }

    private void onServerTick(Level world) {
        var workSpeed = CONFIG.primitiveWorkSpeed.get();
        var processor = PROCESSOR.get(blockEntity);
        processor.onPreWork();
        processor.onWorkTick(workSpeed);
        var working = processor.getProgress() > 0d;
        var state = blockEntity.getBlockState();
        if (state.getValue(WORKING) != working) {
            world.setBlockAndUpdate(blockEntity.getBlockPos(), state.setValue(WORKING, working));
        }
    }

    private static class EmptyMachineConfig implements IMachineConfig {
        @Override
        public void apply(ISetMachineConfigPacket packet) {}

        @Override
        public boolean contains(String key, int tagType) {
            return false;
        }

        @Override
        public Optional<Boolean> getBoolean(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<Integer> getInt(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<String> getString(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<CompoundTag> getCompound(String key) {
            return Optional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {}
    }

    private final EmptyMachineConfig machineConfig = new EmptyMachineConfig();

    @Override
    public UUID uuid() {
        return new UUID(0L, 0L);
    }

    @Override
    public Optional<ITeamProfile> owner() {
        return Optional.empty();
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return true;
    }

    @Override
    public IMachineConfig config() {
        return machineConfig;
    }

    @Override
    public void setConfig(ISetMachineConfigPacket packet, boolean invokeUpdate) {}

    @Override
    public Component title() {
        return I18n.name(blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack icon() {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockEntity blockEntity() {
        return blockEntity;
    }

    @Override
    public Optional<BlockState> workBlock() {
        return Optional.of(blockEntity.getBlockState());
    }

    @Override
    public Optional<IProcessor> processor() {
        return PROCESSOR.tryGet(blockEntity);
    }

    @Override
    public Optional<IContainer> container() {
        return CONTAINER.tryGet(blockEntity);
    }

    @Override
    public Optional<IElectricMachine> electric() {
        return Optional.empty();
    }

    @Override
    public Optional<INetwork> network() {
        return Optional.empty();
    }

    @Override
    public void onConnectToNetwork(INetwork network) {}

    @Override
    public void onDisconnectFromNetwork() {}

    @Override
    public void buildSchedulings(INetworkComponent.SchedulingBuilder builder) {}

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_TICK.get(), this::onServerTick);
    }
}
