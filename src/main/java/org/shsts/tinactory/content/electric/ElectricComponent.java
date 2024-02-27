package org.shsts.tinactory.content.electric;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.CompositeNetwork;
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricComponent extends Component {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected double lossFactor = 0d;
    protected double powerIn, powerOut, powerBuffer;
    protected double workFactor;
    protected double bufferFactor;

    public ElectricComponent(ComponentType<?> type, CompositeNetwork network) {
        super(type, network);
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof IElectricBlock electricBlock) {
            var voltage = (double) electricBlock.getVoltage(state);
            var loss = electricBlock.getResistance(state) / voltage / voltage;
            this.lossFactor += loss;
        }
    }

    @Override
    public void onConnect() {
        LOGGER.debug("{} on connect lossFactor = {}", this, this.lossFactor);
    }

    protected double solvePowerOut(double powerIn) {
        return 2 * powerIn / (1 + Math.sqrt(1 + 4 * this.lossFactor * powerIn));
    }

    protected double solveBufferFactor(double power) {
        if (this.powerBuffer < MathUtil.EPS) {
            return 0d;
        }
        return MathUtil.clamp(power / this.powerBuffer, 0d, 1d);
    }

    protected void solveNetwork() {
        this.powerIn = 0d;
        this.powerOut = 0d;
        this.powerBuffer = 0d;
        this.network.forEachMachine(machine -> machine
                .getCapability(AllCapabilities.ELECTRIC_MACHINE.get()).ifPresent(electric -> {
                    switch (electric.getMachineType()) {
                        case GENERATOR -> this.powerIn += electric.getPower();
                        case CONSUMER -> this.powerOut += electric.getPower();
                        case BUFFER -> this.powerBuffer += electric.getPower();
                    }
                }));
        var needPower = this.powerOut + this.powerOut * this.powerOut * this.lossFactor;
        if (needPower <= this.powerIn) {
            this.workFactor = 1d;
            // buffer is consumer
            this.bufferFactor = this.solveBufferFactor(this.solvePowerOut(this.powerIn) - this.powerOut);
        } else if (needPower <= this.powerIn + this.powerBuffer) {
            this.workFactor = 1d;
            // buffer is generator
            this.bufferFactor = -this.solveBufferFactor(needPower - this.powerIn);
        } else {
            // buffer is generator
            this.bufferFactor = -1d;
            var powerOut = this.solvePowerOut(this.powerIn + this.powerBuffer);
            if (this.powerOut < MathUtil.EPS) {
                this.workFactor = 0d;
            } else {
                this.workFactor = MathUtil.clamp(powerOut / this.powerOut, 0d, 1d);
            }
        }
    }

    public double getWorkFactor() {
        return this.workFactor;
    }

    public double getBufferFactor() {
        return this.bufferFactor;
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.ELECTRIC_SCHEDULING, (world, network) -> this.solveNetwork());
    }
}
