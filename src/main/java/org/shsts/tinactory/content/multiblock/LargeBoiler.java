package org.shsts.tinactory.content.multiblock;

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
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.machine.FireBoiler;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.multiblock.FixedBlock.WORKING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeBoiler extends Multiblock implements INBTSerializable<CompoundTag> {
    private final FireBoiler boiler;
    private final LazyOptional<IProcessor> processorCap;
    private final List<BlockPos> fireboxes = new ArrayList<>();

    private int boilParallel = 1;

    public LargeBoiler(BlockEntity blockEntity, Builder<?> builder,
        FireBoiler.Properties properties) {
        super(blockEntity, builder);
        this.boiler = new FireBoiler(properties) {
            @Override
            protected Optional<IMachine> machine() {
                return Optional.ofNullable(multiblockInterface);
            }

            @Override
            protected double boilParallel() {
                return boilParallel;
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
            boilParallel = (int) ctx.getProperty("height") - 1;
            fireboxes.clear();
            for (var pos : ctx.blocks) {
                var block = ctx.getBlock(pos);
                if (block.isPresent() && block.get().getBlock() instanceof FixedBlock) {
                    fireboxes.add(pos);
                }
            }
        }
    }

    @Override
    protected void onRegister() {
        super.onRegister();
        assert multiblockInterface != null;
        boiler.setContainer(multiblockInterface.container().orElseThrow());
    }

    @Override
    protected void updateMultiblockInterface() {
        super.updateMultiblockInterface();
        if (multiblockInterface != null) {
            boiler.setContainer(multiblockInterface.container().orElseThrow());
        }
    }

    @Override
    public void setWorkBlock(Level world, BlockState state) {
        super.setWorkBlock(world, state);
        var working = (boolean) state.getValue(WORKING);
        for (var pos : fireboxes) {
            if (!world.isLoaded(pos)) {
                continue;
            }
            var state1 = world.getBlockState(pos);
            if (state1.hasProperty(WORKING) && state1.getValue(WORKING) != working) {
                world.setBlock(pos, state1.setValue(WORKING, true), 19);
            }
        }
    }

    @Override
    public IMenuType menu(IMachine machine) {
        return AllMenus.BOILER;
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
