package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.machine.BoilerProcessor;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LargeBoiler extends Multiblock {
    private class Processor extends BoilerProcessor {
        public Processor(Properties properties) {
            super(blockEntity, properties);
        }

        @Override
        protected double boilParallel() {
            return boilParallel;
        }

        @Override
        protected int burnParallel() {
            return multiblockInterface == null ? 1 : multiblockInterface.maxParallel();
        }
    }

    private final Processor processor;
    private final LazyOptional<IProcessor> processorCap;

    private int boilParallel = 1;

    public LargeBoiler(BlockEntity blockEntity, Builder<?> builder,
        BoilerProcessor.Properties properties) {
        super(blockEntity, builder);
        this.processor = new Processor(properties);
        this.processorCap = LazyOptional.of(() -> processor);
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (!ctx.isFailed()) {
            boilParallel = (int) ctx.getProperty("height") - 1;
        }
    }

    @Override
    protected void onRegister() {
        super.onRegister();
        assert multiblockInterface != null;
        processor.setContainer(multiblockInterface.container().orElseThrow());
    }

    @Override
    protected void updateMultiblockInterface() {
        super.updateMultiblockInterface();
        if (multiblockInterface != null) {
            processor.setContainer(multiblockInterface.container().orElseThrow());
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
}
