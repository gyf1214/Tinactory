package org.shsts.tinactory.content.electric;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.core.metrics.MetricsManager;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.network.ComponentType;
import org.shsts.tinactory.integration.network.NetworkComponent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoublePredicate;

import static org.shsts.tinactory.AllNetworks.ELECTRIC_SCHEDULING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricComponent extends NetworkComponent {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static class Subnet {
        public final BlockPos center;
        public double lossFactor = 0d;
        public double gen, cons;
        public double bGen, bCons;
        public double pGen, pCons;

        public final List<Subnet> children = new ArrayList<>();

        public Subnet(BlockPos center) {
            this.center = center;
        }

        public void reset() {
            gen = cons = 0d;
            bGen = bCons = 0d;
        }

        @Override
        public String toString() {
            return "Subnet[" + center + "]";
        }

        public void solve(double wFactor, double bFactor, boolean isBGen) {
            var tGen = gen + children.stream().mapToDouble(sub -> sub.pCons).sum() +
                (isBGen ? bGen * bFactor : 0d);
            var tCons = cons * wFactor + children.stream().mapToDouble(sub -> sub.pGen).sum() +
                (isBGen ? 0d : bCons * bFactor);

            var diff = tGen - tCons - lossFactor * tCons * tCons;
            var comp = MathUtil.compare(diff);
            if (comp == 0) {
                pGen = pCons = 0d;
            } else if (comp < 0) {
                pGen = -diff;
                pCons = 0d;
            } else {
                var tCons1 = 2 * tGen / (1 + Math.sqrt(1 + 4 * lossFactor * tGen));
                pGen = 0d;
                pCons = tCons1 - tCons;
            }
            assert pGen >= 0d && pCons >= 0d;
        }
    }

    private final Map<BlockPos, Subnet> subnets = new HashMap<>();
    private final List<Subnet> solveOrder = new ArrayList<>();

    private double workFactor;
    private double bufferFactor;

    public ElectricComponent(ComponentType<?> type, INetwork network) {
        super(type, network);
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        var sub = subnets.computeIfAbsent(subnet, Subnet::new);
        if (state.getBlock() instanceof IElectricBlock electricBlock) {
            var voltage = electricBlock.getVoltage(state);
            if (voltage > 0) {
                var loss = electricBlock.getResistance(state) / voltage / voltage;
                sub.lossFactor += loss;
            }
        }
    }

    @Override
    public void onConnect() {
        Subnet root = null;
        for (var entry : network.allBlocks()) {
            var child = entry.getKey();
            var parent = entry.getValue();
            if (parent.equals(child)) {
                root = subnets.get(parent);
            } else if (subnets.containsKey(child)) {
                subnets.get(parent).children.add(subnets.get(child));
            }
        }
        assert root != null;

        var st = 0;
        solveOrder.add(root);
        while (st < solveOrder.size()) {
            var cur = solveOrder.get(st++);
            solveOrder.addAll(cur.children);
        }
        Collections.reverse(solveOrder);

        LOGGER.trace("solve order = {}", solveOrder);
    }

    @Override
    public void onDisconnect() {
        subnets.clear();
        solveOrder.clear();
    }

    private boolean solve(double wFactor, double bFactor, boolean isBGen) {
        for (var sub : solveOrder) {
            sub.solve(wFactor, bFactor, isBGen);
        }
        var root = solveOrder.get(solveOrder.size() - 1);
        return MathUtil.compare(root.pGen) == 0;
    }

    private double find(DoublePredicate tester, boolean reverse) {
        var st = 0d;
        var ed = 1d;
        while (ed - st > MathUtil.EPS) {
            var m = (st + ed) / 2d;
            if (tester.test(m) ^ reverse) {
                st = m;
            } else {
                ed = m;
            }
        }
        return reverse ? ed : st;
    }

    private void reportMetrics(String team, double gen, double workCons, double buffer) {
        var label = List.of(team);
        MetricsManager.report("electric_consumed", label, workCons);
        MetricsManager.report("electric_generated", label, gen);
        var sign = MathUtil.compare(buffer);
        MetricsManager.report("electric_buffer_charged", label, sign > 0 ? buffer : 0);
        MetricsManager.report("electric_buffer_discharged", label, sign < 0 ? -buffer : 0);
    }

    private void solveNetwork() {
        for (var sub : subnets.values()) {
            sub.reset();
        }
        for (var entry : network.allMachines().entries()) {
            entry.getValue().electric().ifPresent(electric -> {
                var sub = subnets.get(entry.getKey());

                switch (electric.getMachineType()) {
                    case GENERATOR -> sub.gen += electric.getPowerGen();
                    case CONSUMER -> sub.cons += electric.getPowerCons();
                    case BUFFER -> {
                        sub.bGen += electric.getPowerGen();
                        sub.bCons += electric.getPowerCons();
                    }
                }
            });
        }

        if (solve(1d, 1d, false)) {
            workFactor = 1d;
            bufferFactor = 1d;
        } else if (solve(1d, 0d, false)) {
            workFactor = 1d;
            bufferFactor = find(m -> solve(1d, m, false), false);
        } else if (solve(1d, 1d, true)) {
            workFactor = 1d;
            bufferFactor = -find(m -> solve(1d, m, true), true);
        } else {
            workFactor = find(m -> solve(m, 1d, true), false);
            bufferFactor = -1d;
        }

        var gen = subnets.values().stream().mapToDouble(sub -> sub.gen).sum();
        var maxCons = subnets.values().stream().mapToDouble(sub -> sub.cons).sum();
        var workCons = maxCons * workFactor;
        var bufferGen = subnets.values().stream().mapToDouble(sub -> sub.bGen).sum();
        var bufferCons = subnets.values().stream().mapToDouble(sub -> sub.bCons).sum();
        var comp = MathUtil.compare(bufferFactor);

        double buffer;
        if (comp == 0) {
            buffer = 0d;
        } else if (comp > 0) {
            buffer = bufferCons * bufferFactor;
        } else {
            buffer = bufferGen * bufferFactor;
        }

        var team = network.owner().getName();
        reportMetrics(team, gen, workCons, buffer);
    }

    public double getWorkFactor() {
        return workFactor;
    }

    public double getBufferFactor() {
        return bufferFactor;
    }

    @Override
    public void buildSchedulings(ISchedulingRegister builder) {
        builder.add(ELECTRIC_SCHEDULING.get(), ($1, $2) -> solveNetwork());
    }
}
