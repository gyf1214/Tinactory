package org.shsts.tinactory.content.electric;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.network.NetworkComponent;
import org.shsts.tinactory.core.util.MathUtil;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.DoublePredicate;
import java.util.function.Supplier;

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

        public List<Subnet> children = new ArrayList<>();

        public Subnet(BlockPos center) {
            this.center = center;
        }

        public void reset() {
            gen = cons = 0d;
            bGen = bCons = 0d;
        }

        @Override
        public String toString() {
            return "Subnet{" + center + '}';
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

    public static class Metrics {
        private double workFactor, gen, workCons, buffer;

        public Metrics() {}

        public double getWorkFactor() {
            return workFactor;
        }

        public double getGen() {
            return gen;
        }

        public double getWorkCons() {
            return workCons;
        }

        public double getBuffer() {
            return buffer;
        }

        public double getEfficiency() {
            var comp = MathUtil.compare(buffer);
            if (comp == 0) {
                return workCons / gen;
            } else if (comp > 0) {
                return (workCons + buffer) / gen;
            } else {
                return workCons / (gen - buffer);
            }
        }

        public void writeToBuf(FriendlyByteBuf buf) {
            buf.writeDouble(workFactor);
            buf.writeDouble(gen);
            buf.writeDouble(workCons);
            buf.writeDouble(buffer);
        }

        public void readFromBuf(FriendlyByteBuf buf) {
            workFactor = buf.readDouble();
            gen = buf.readDouble();
            workCons = buf.readDouble();
            buffer = buf.readDouble();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Metrics metrics = (Metrics) o;
            return Double.compare(metrics.workFactor, workFactor) == 0 &&
                    Double.compare(metrics.gen, gen) == 0 &&
                    Double.compare(metrics.workCons, workCons) == 0 &&
                    Double.compare(metrics.buffer, buffer) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(workFactor, gen, workCons, buffer);
        }
    }

    private final Map<BlockPos, Subnet> subnets = new HashMap<>();
    private final Map<BlockPos, BlockPos> edges = new HashMap<>();
    private final List<Subnet> solveOrder = new ArrayList<>();
    private final Metrics metrics = new Metrics();

    private double workFactor;
    private double bufferFactor;

    public ElectricComponent(ComponentType<?> type, Network network) {
        super(type, network);
    }

    @Override
    public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        var sub = subnets.computeIfAbsent(subnet, Subnet::new);
        edges.put(pos, subnet);

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
        for (var entry : edges.entrySet()) {
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

        LOGGER.debug("solve order = {}", solveOrder);
    }

    @Override
    public void onDisconnect() {
        subnets.clear();
        edges.clear();
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

    private void solveNetwork() {
        for (var sub : subnets.values()) {
            sub.reset();
        }
        for (var entry : network.getMachines().entries()) {
            entry.getValue().getElectric().ifPresent(electric -> {
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

        metrics.gen = subnets.values().stream().mapToDouble(sub -> sub.gen).sum();
        var maxCons = subnets.values().stream().mapToDouble(sub -> sub.cons).sum();
        metrics.workFactor = workFactor;
        metrics.workCons = maxCons * workFactor;
        var bufferGen = subnets.values().stream().mapToDouble(sub -> sub.bGen).sum();
        var bufferCons = subnets.values().stream().mapToDouble(sub -> sub.bCons).sum();
        var comp = MathUtil.compare(bufferFactor);
        if (comp == 0) {
            metrics.buffer = 0L;
        } else if (comp > 0) {
            metrics.buffer = bufferCons * bufferFactor;
        } else {
            metrics.buffer = bufferGen * bufferFactor;
        }
    }

    public double getWorkFactor() {
        return workFactor;
    }

    public double getBufferFactor() {
        return bufferFactor;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public void buildSchedulings(BiConsumer<Supplier<IScheduling>, Ticker> cons) {
        cons.accept(AllNetworks.ELECTRIC_SCHEDULING, ($1, $2) -> solveNetwork());
    }
}
