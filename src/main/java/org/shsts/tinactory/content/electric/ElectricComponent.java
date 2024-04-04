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
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricComponent extends Component {
    private static final Logger LOGGER = LogUtils.getLogger();

    private double lossFactor = 0d;
    private double powerGen, powerCons;
    private double powerBufferGen, powerBufferCons;
    private double workFactor;
    private double bufferFactor;

    public ElectricComponent(ComponentType<?> type, Network network) {
        super(type, network);
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof IElectricBlock electricBlock) {
            var voltage = electricBlock.getVoltage(state);
            if (voltage > 0) {
                var loss = electricBlock.getResistance(state) / voltage / voltage;
                lossFactor += loss;
            }
        }
    }

    @Override
    public void onConnect() {
        LOGGER.debug("{} on connect lossFactor = {}", this, lossFactor);
    }

    @Override
    public void onDisconnect() {
        lossFactor = 0d;
    }

    private double solvePowerCons(double powerGen) {
        return 2 * powerGen / (1 + Math.sqrt(1 + 4 * lossFactor * powerGen));
    }

    private double solveBufferFactor(double power) {
        var comp = MathUtil.compare(power);
        if (comp == 0) {
            return 0d;
        } else if (comp > 0) {
            /* consumer */
            if (powerBufferCons < MathUtil.EPS) {
                return 0d;
            }
            return MathUtil.clamp(power / powerBufferCons, 0d, 1d);
        } else {
            /* generator */
            if (powerBufferGen < MathUtil.EPS) {
                return 0d;
            }
            return MathUtil.clamp(power / powerBufferGen, 0d, 1d);
        }
    }

    private void solveNetwork() {
        powerGen = 0d;
        powerCons = 0d;
        powerBufferGen = 0d;
        powerBufferCons = 0d;
        for (var machine : network.getMachines()) {
            machine.getElectric().ifPresent(electric -> {
                switch (electric.getMachineType()) {
                    case GENERATOR -> powerGen += electric.getPowerGen();
                    case CONSUMER -> powerCons += electric.getPowerCons();
                    case BUFFER -> {
                        powerBufferGen += electric.getPowerGen();
                        powerBufferCons += electric.getPowerCons();
                    }
                }
            });
        }
        var needPower = powerCons + powerCons * powerCons * lossFactor;
        if (needPower <= powerGen) {
            workFactor = 1d;
            // buffer is consumer
            bufferFactor = solveBufferFactor(solvePowerCons(powerGen) - powerCons);
        } else if (needPower <= powerGen + powerBufferGen) {
            workFactor = 1d;
            // buffer is generator
            bufferFactor = solveBufferFactor(powerGen - needPower);
        } else {
            // buffer is generator
            bufferFactor = -1d;
            var powerOut = solvePowerCons(powerGen + powerBufferGen);
            if (powerCons < MathUtil.EPS) {
                workFactor = 0d;
            } else {
                workFactor = MathUtil.clamp(powerOut / powerCons, 0d, 1d);
            }
        }
    }

    public double getWorkFactor() {
        return workFactor;
    }

    public double getBufferFactor() {
        return bufferFactor;
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.ELECTRIC_SCHEDULING, (world, network) -> solveNetwork());
    }
}
