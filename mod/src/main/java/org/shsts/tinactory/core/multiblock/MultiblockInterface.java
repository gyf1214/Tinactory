package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.AllCapabilities;
import org.shsts.tinactory.AllEvents;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.IFlexibleContainer;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.Optional;

import static org.shsts.tinactory.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.AllEvents.CLIENT_TICK;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockInterface extends Machine {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PARALLEL_KEY = "parallel";

    public final Voltage voltage;

    protected IFlexibleContainer container;
    /**
     * BlockEntity update may be before client load. If so, the update event needs to be delayed until the first tick.
     * This variable is to distinguish these two scenarios.
     */
    private boolean firstTick = false;
    @Nullable
    private BlockPos multiblockPos = null;
    @Nullable
    private Multiblock multiblock = null;
    @Nullable
    private IProcessor processor = null;
    @Nullable
    private IElectricMachine electricMachine = null;
    @Nullable
    private Layout layout = null;

    public MultiblockInterface(BlockEntity be) {
        super(be);
        this.voltage = getBlockVoltage(be);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, MultiblockInterface::new);
    }

    /**
     * Only called on server.
     */
    private void onMultiblockUpdate() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        if (network != null) {
            network.invalidate();
        }
        sendUpdate(blockEntity);
    }

    private void setJoined(Level world, boolean value) {
        getRealBlockState(world, blockEntity).ifPresent(state -> {
            var newState = state.setValue(MultiblockInterfaceBlock.JOINED, value);
            // prevent updateShape on neighbor
            world.setBlock(blockEntity.getBlockPos(), newState, 19);
        });
    }

    public void setContainerLayout(Layout val) {
        if (layout != val) {
            LOGGER.debug("{}: set container layout={}", this, val);
            container.setLayout(val);
            layout = val;
            if (multiblock != null) {
                multiblock.onContainerReady();
            }
        }
    }

    public void resetContainerLayout() {
        LOGGER.debug("{}: reset container layout", this);
        container.resetLayout();
        layout = null;
    }

    public boolean isContainerReady() {
        return layout != null;
    }

    public boolean isDigital() {
        return false;
    }

    public void setMultiblock(Multiblock target) {
        if (multiblock == target) {
            return;
        }
        LOGGER.debug("{} set multiblock = {}", this, target);
        multiblock = target;
        processor = target.processor().orElse(null);
        electricMachine = target.electric().orElse(null);
        target.getLayout().ifPresent(this::setContainerLayout);
        if (!world().isClientSide) {
            setJoined(world(), true);
        }
        onMultiblockUpdate();
    }

    public void resetMultiblock() {
        if (multiblock == null) {
            return;
        }
        LOGGER.debug("{} reset multiblock", this);
        var world = world();
        if (!world.isClientSide) {
            updateWorkBlock(world, false);
            setJoined(world, false);
        }
        multiblock = null;
        processor = null;
        electricMachine = null;
        resetContainerLayout();
        onMultiblockUpdate();
    }

    protected void onLoad() {
        container = (IFlexibleContainer) AllCapabilities.CONTAINER.get(blockEntity);
    }

    private void onContainerChange() {
        if (multiblock != null) {
            invoke(multiblock.blockEntity, CONTAINER_CHANGE);
        }
    }

    private void onConnect(INetwork network) {
        if (multiblock != null) {
            invoke(multiblock.blockEntity, CONNECT, network);
        }
    }

    @Override
    public void setConfig(ISetMachineConfigPacket packet, boolean invokeEvent) {
        super.setConfig(packet, invokeEvent);
        if (invokeEvent && multiblock != null) {
            invoke(multiblock.blockEntity, SET_MACHINE_CONFIG);
        }
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return super.canPlayerInteract(player) && multiblock != null;
    }

    /**
     * Called only on Client during BlockEntity update.
     * It is important to note that Multiblock and MultiblockInterface update are separate and their order is not
     * guaranteed.
     */
    private void updateMultiblock() {
        var world = world();
        assert world.isClientSide;

        LOGGER.debug("{}: update multiblock current={}, pos={}, firstTick={}",
            this, multiblock, multiblockPos, firstTick);

        if (multiblockPos != null) {
            var be1 = world.getBlockEntity(multiblockPos);
            if (be1 == null) {
                LOGGER.debug("cannot get blockEntity {}:{}", world.dimension().location(), multiblockPos);
                return;
            }
            Multiblock.tryGet(be1).ifPresentOrElse(this::setMultiblock, this::resetMultiblock);
        } else {
            resetMultiblock();
        }

        if (multiblock != null) {
            invoke(multiblock.blockEntity, SET_MACHINE_CONFIG);
        }
    }

    private void onClientTick() {
        if (!firstTick) {
            updateMultiblock();
            firstTick = true;
        }
    }

    @Override
    protected void onUse(AllEvents.OnUseArg arg, IReturnEvent.Result<InteractionResult> result) {
        super.onUse(arg, result);
        if (result.get() != InteractionResult.PASS || multiblock == null) {
            return;
        }

        var player = arg.player();
        var world = player.level;
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            multiblock.menu(this).open(serverPlayer, blockEntity.getBlockPos());
        }
        result.set(InteractionResult.sidedSuccess(world.isClientSide));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_TICK.get(), $ -> onClientTick());
        eventManager.subscribe(CONTAINER_CHANGE.get(), this::onContainerChange);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
    }

    @Override
    protected Optional<BlockState> workBlock(Level world) {
        if (multiblock == null) {
            return Optional.empty();
        }
        return getRealBlockState(world, multiblock.blockEntity);
    }

    @Override
    protected void setWorkBlock(Level world, BlockState state) {
        if (multiblock == null) {
            return;
        }
        LOGGER.trace("{}: set work block, state = {}", multiblock, state);
        multiblock.setWorkBlock(world, state);
    }

    @Override
    public Optional<IProcessor> processor() {
        return Optional.ofNullable(processor);
    }

    @Override
    public Optional<IContainer> container() {
        return Optional.of(container);
    }

    @Override
    public Optional<IElectricMachine> electric() {
        return Optional.ofNullable(electricMachine);
    }

    public int maxParallel() {
        return 1;
    }

    public int parallel() {
        var maxParallel = maxParallel();
        return Math.min(maxParallel, config.getInt(PARALLEL_KEY, maxParallel));
    }

    @Override
    public Component title() {
        if (config.contains("name", Tag.TAG_STRING) || multiblock == null) {
            return super.title();
        }
        return I18n.name(multiblock.blockEntity.getBlockState().getBlock());
    }

    public Optional<BlockState> getAppearanceBlock() {
        return multiblock == null ? Optional.empty() : Optional.of(multiblock.getAppearanceBlock());
    }

    public Optional<Multiblock> getMultiblock() {
        return Optional.ofNullable(multiblock);
    }

    @Override
    public ItemStack icon() {
        if (multiblock == null) {
            return super.icon();
        }
        var block = multiblock.blockEntity.getBlockState().getBlock();
        return new ItemStack(block);
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiblock != null) {
            var pos = multiblock.blockEntity.getBlockPos();
            tag.put("multiblockPos", CodecHelper.encodeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        super.deserializeOnUpdate(tag);

        multiblockPos = tag.contains("multiblockPos", Tag.TAG_COMPOUND) ?
            CodecHelper.parseBlockPos(tag.getCompound("multiblockPos")) : null;
        if (firstTick) {
            updateMultiblock();
        }
    }
}
