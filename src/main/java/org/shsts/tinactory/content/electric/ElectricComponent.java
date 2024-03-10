package org.shsts.tinactory.content.electric;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.api.network.IScheduling;
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
    protected double powerGen, powerCons;
    protected double powerBufferGen, powerBufferCons;
    protected double workFactor;
    protected double bufferFactor;

    public ElectricComponent(ComponentType<?> type, CompositeNetwork network) {
        super(type, network);
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof IElectricBlock electricBlock) {
            var voltage = electricBlock.getVoltage(state);
            if (voltage > 0) {
                var loss = electricBlock.getResistance(state) / voltage / voltage;
                this.lossFactor += loss;
            }
        }
    }

    @Override
    public void onConnect() {
        LOGGER.debug("{} on connect lossFactor = {}", this, this.lossFactor);
    }

    @Override
    public void onDisconnect() {
        this.lossFactor = 0d;
    }

    protected double solvePowerCons(double powerGen) {
        return 2 * powerGen / (1 + Math.sqrt(1 + 4 * this.lossFactor * powerGen));
    }

    protected double solveBufferFactor(double power) {
        var comp = MathUtil.compare(power);
        if (comp == 0) {
            return 0d;
        } else if (comp > 0) {
            /* consumer */
            if (this.powerBufferCons < MathUtil.EPS) {
                return 0d;
            }
            return MathUtil.clamp(power / this.powerBufferCons, 0d, 1d);
        } else {
            /* generator */
            if (this.powerBufferGen < MathUtil.EPS) {
                return 0d;
            }
            return MathUtil.clamp(power / this.powerBufferGen, 0d, 1d);
        }
    }

    protected void solveNetwork() {
        this.powerGen = 0d;
        this.powerCons = 0d;
        this.powerBufferGen = 0d;
        this.powerBufferCons = 0d;
        for (var machine : this.network.getMachines()) {
            machine.getElectric().ifPresent(electric -> {
                switch (electric.getMachineType()) {
                    case GENERATOR -> this.powerGen += electric.getPowerGen();
                    case CONSUMER -> this.powerCons += electric.getPowerCons();
                    case BUFFER -> {
                        this.powerBufferGen += electric.getPowerGen();
                        this.powerBufferCons += electric.getPowerCons();
                    }
                }
            });
        }
        var needPower = this.powerCons + this.powerCons * this.powerCons * this.lossFactor;
        if (needPower <= this.powerGen) {
            this.workFactor = 1d;
            // buffer is consumer
            this.bufferFactor = this.solveBufferFactor(this.solvePowerCons(this.powerGen) - this.powerCons);
        } else if (needPower <= this.powerGen + this.powerBufferGen) {
            this.workFactor = 1d;
            // buffer is generator
            this.bufferFactor = this.solveBufferFactor(this.powerGen - needPower);
        } else {
            // buffer is generator
            this.bufferFactor = -1d;
            var powerOut = this.solvePowerCons(this.powerGen + this.powerBufferGen);
            if (this.powerCons < MathUtil.EPS) {
                this.workFactor = 0d;
            } else {
                this.workFactor = MathUtil.clamp(powerOut / this.powerCons, 0d, 1d);
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
