package org.shsts.tinactory.content.machine;

import com.mojang.datafixers.util.Either;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.logistics.IFluidCollection;
import org.shsts.tinactory.content.logistics.IItemCollection;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IProcessingMachine {
    /**
     * Must be called from Server.
     */
    void onWorkTick(double partial);

    boolean hasPort(int port);

    Either<IItemCollection, IFluidCollection> getPort(int port, boolean internal);

    double getProgress();

    static double getProgress(BlockEntity be) {
        return be.getCapability(AllCapabilities.PROCESSING_MACHINE.get())
                .map(IProcessingMachine::getProgress)
                .orElse(0.0);
    }
}
