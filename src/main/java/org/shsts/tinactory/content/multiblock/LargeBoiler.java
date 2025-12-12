package org.shsts.tinactory.content.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.machine.FireBoiler;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.network.MachineBlock;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeBoiler extends Multiblock implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final FireBoiler boiler;
    private final List<BlockPos> fireboxes = new ArrayList<>();
    private final LazyOptional<IProcessor> processorCap;

    private int boilParallel = 1;

    public LargeBoiler(BlockEntity blockEntity, Builder<?> builder,
        FireBoiler.Properties properties, double baseBoilerParallel) {
        super(blockEntity, builder);
        this.boiler = new FireBoiler(properties) {
            @Override
            protected Optional<IMachine> machine() {
                return Optional.ofNullable(multiblockInterface);
            }

            @Override
            protected double boilParallel() {
                return boilParallel * baseBoilerParallel;
            }

            @Override
            protected int burnParallel() {
                return multiblockInterface == null ? 1 : multiblockInterface.maxParallel();
            }

            @Override
            protected void setChanged() {
                blockEntity.setChanged();
            }
        };
        this.processorCap = LazyOptional.of(() -> boiler);
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            boilParallel = (int) ctx.getProperty("height") - 2;
            fireboxes.clear();
            for (var pos : ctx.blocks) {
                var block = ctx.getBlock(pos);
                if (block.isPresent() && block.get().getBlock() instanceof FixedBlock) {
                    fireboxes.add(pos);
                }
            }
        }
    }

    private void setBoilerContainer(IMachine machine) {
        LOGGER.debug("{}: set boiler container", this);
        boiler.setContainer(machine.container().orElseThrow());
    }

    @Override
    protected void onInvalidate() {
        super.onInvalidate();
        boiler.resetContainer();
        var world = blockEntity.getLevel();
        if (world != null) {
            setFireboxBlock(world, false);
        }
    }

    @Override
    protected void updateMultiblockInterface() {
        super.updateMultiblockInterface();
        if (multiblockInterface != null) {
            if (multiblockInterface.isContainerReady()) {
                setBoilerContainer(multiblockInterface);
            }
        } else {
            boiler.resetContainer();
        }
    }

    /**
     * We don't need to call {@link #setBoilerContainer} in {@link #onRegister}. This is because
     * {@link MultiblockInterface#setMultiblock} is always called before {@link #onRegister} on server.
     */
    @Override
    public void onContainerReady() {
        if (multiblockInterface != null && !boiler.hasContainer()) {
            setBoilerContainer(multiblockInterface);
        }
    }

    private void setFireboxBlock(Level world, boolean working) {
        for (var pos : fireboxes) {
            if (!world.isLoaded(pos)) {
                continue;
            }
            var state1 = world.getBlockState(pos);
            if (state1.hasProperty(FixedBlock.WORKING) && state1.getValue(FixedBlock.WORKING) != working) {
                world.setBlock(pos, state1.setValue(FixedBlock.WORKING, working), 19);
            }
        }
    }

    @Override
    public void setWorkBlock(Level world, BlockState state) {
        super.setWorkBlock(world, state);
        setFireboxBlock(world, state.getValue(MachineBlock.WORKING));
    }

    @Override
    public IMenuType menu(MultiblockInterface machine) {
        return machine.isDigital() ? AllMenus.BOILER_DIGITAL_INTERFACE : AllMenus.BOILER;
    }

    private void onConnect(INetwork network) {
        if (multiblockInterface != null) {
            Machine.registerStopSignal(network, multiblockInterface, boiler::setStopped);
        }
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(CONTAINER_CHANGE.get(), boiler::onUpdateContainer);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == PROCESSOR.get()) {
            return processorCap.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        return boiler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boiler.deserializeNBT(tag);
    }
}
