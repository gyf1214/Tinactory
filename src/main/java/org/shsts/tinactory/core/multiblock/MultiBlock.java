package org.shsts.tinactory.core.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlock extends MultiBlockBase {
    public final Layout layout;

    /**
     * must set this during checkMultiBlock, or fail
     */
    @Nullable
    protected MultiBlockInterface multiBlockInterface = null;

    public MultiBlock(SmartBlockEntity blockEntity, Layout layout) {
        super(blockEntity);
        this.layout = layout;
    }

    public abstract BlockState getAppearanceBlock();

    protected boolean checkInterface(Level world, BlockPos pos) {
        if (multiBlockInterface != null) {
            return false;
        }
        var be = world.getBlockEntity(pos);
        if (be == null) {
            return false;
        }
        var machine = AllCapabilities.MACHINE.tryGet(be);
        if (machine.isEmpty() || !(machine.get() instanceof MultiBlockInterface inter)) {
            return false;
        }
        multiBlockInterface = inter;
        return true;
    }

    @Override
    protected Optional<Collection<BlockPos>> checkMultiBlock(BlockPos start, int tx, int ty, int tz) {
        var oldInterface = multiBlockInterface;
        multiBlockInterface = null;
        var ret = super.checkMultiBlock(start, tx, ty, tz);
        var ok = ret.isPresent() && multiBlockInterface != null &&
                (oldInterface == null || oldInterface == multiBlockInterface);
        if (!ok) {
            // for invalidation
            multiBlockInterface = oldInterface;
        }
        return ok ? ret : Optional.empty();
    }

    @Override
    protected void onRegister() {
        assert multiBlockInterface != null;
        multiBlockInterface.setMultiBlock(this);
        sendUpdate(blockEntity);
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    @Override
    protected void onInvalidate() {
        if (multiBlockInterface != null) {
            multiBlockInterface.resetMultiBlock();
        }
        multiBlockInterface = null;
        sendUpdate(blockEntity);
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    public Optional<MultiBlockInterface> getInterface() {
        return Optional.ofNullable(multiBlockInterface);
    }

    public Optional<IContainer> getContainer() {
        return getInterface().flatMap(MultiBlockInterface::getContainer);
    }

    public IProcessor getProcessor() {
        return AllCapabilities.PROCESSOR.get(blockEntity);
    }

    public IElectricMachine getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.get(blockEntity);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.MULTI_BLOCK.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = new CompoundTag();
        if (multiBlockInterface != null) {
            var pos = multiBlockInterface.blockEntity.getBlockPos();
            tag.put("interfacePos", CodecHelper.serializeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        var world = blockEntity.getLevel();
        assert world != null;

        if (tag.contains("interfacePos", Tag.TAG_COMPOUND)) {
            var pos = CodecHelper.deserializeBlockPos(tag.getCompound("interfacePos"));

            var be1 = world.getBlockEntity(pos);
            if (be1 == null) {
                return;
            }
            AllCapabilities.MACHINE.tryGet(be1).ifPresent(machine ->
                    multiBlockInterface = (MultiBlockInterface) machine);
        } else {
            multiBlockInterface = null;
        }

        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG);
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> blastFurnace(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "multi_block", BlastFurnace::new);
    }
}
