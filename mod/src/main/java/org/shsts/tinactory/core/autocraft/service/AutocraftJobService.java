package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftJobService implements IAutocraftService {
    private static final long DEFAULT_TRANSMISSION_BANDWIDTH = 64L;
    private static final int DEFAULT_EXECUTION_INTERVAL_TICKS = 1;

    private final ICraftExecutor executor;
    private final long transmissionBandwidth;
    private final int executionIntervalTicks;

    private List<CraftAmount> currentTargets = List.of();
    private int pendingTicks;

    public AutocraftJobService(ICraftExecutor executor, long transmissionBandwidth, int executionIntervalTicks) {
        this.executor = executor;
        this.transmissionBandwidth = transmissionBandwidth;
        this.executionIntervalTicks = Math.max(1, executionIntervalTicks);
    }

    public AutocraftJobService(ICraftExecutor executor) {
        this(executor, DEFAULT_TRANSMISSION_BANDWIDTH, DEFAULT_EXECUTION_INTERVAL_TICKS);
    }

    @Override
    public boolean isBusy() {
        return !currentTargets.isEmpty() && executor.isBusy();
    }

    public Optional<CompoundTag> serializeRunningSnapshot(PatternNbtCodec codec) {
        if (!isBusy()) {
            return Optional.empty();
        }
        var tag = new CompoundTag();
        tag.put("targets", serializeAmounts(currentTargets, codec));
        tag.put("execution", executor.serialize(codec));
        return Optional.of(tag);
    }

    public void restoreRunningSnapshot(CompoundTag tag, PatternNbtCodec codec) {
        if (!currentTargets.isEmpty() || executor.isBusy()) {
            return;
        }
        var targets = deserializeAmounts(tag.getList("targets", TAG_COMPOUND), codec);
        if (targets.isEmpty()) {
            return;
        }
        executor.restore(tag.getCompound("execution"), codec);
        currentTargets = executor.isBusy() ? List.copyOf(targets) : List.of();
        pendingTicks = 0;
    }

    @Override
    public void submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
        if (isBusy()) {
            throw new IllegalStateException("autocraft CPU is busy");
        }
        executor.start(plan);
        currentTargets = executor.isBusy() ? List.copyOf(targets) : List.of();
        pendingTicks = 0;
    }

    @Override
    public Optional<AutocraftJobSnapshot> getJob() {
        if (!isBusy()) {
            return Optional.empty();
        }
        return Optional.of(new AutocraftJobSnapshot(
            currentTargets,
            executor.state(),
            executor.completedSteps(),
            executor.totalSteps(),
            executor.error()));
    }

    @Override
    public boolean cancel() {
        if (!isBusy()) {
            return false;
        }
        executor.cancel();
        return true;
    }

    public boolean tick() {
        if (!isBusy()) {
            return false;
        }
        pendingTicks++;
        if (pendingTicks < executionIntervalTicks) {
            return false;
        }
        pendingTicks = 0;
        executor.runCycle(transmissionBandwidth);
        if (!executor.isBusy()) {
            currentTargets = List.of();
        }
        return true;
    }

    private static ListTag serializeAmounts(List<CraftAmount> amounts, PatternNbtCodec codec) {
        var out = new ListTag();
        for (var amount : amounts) {
            out.add(codec.encodeAmount(amount));
        }
        return out;
    }

    private static List<CraftAmount> deserializeAmounts(ListTag amounts, PatternNbtCodec codec) {
        var out = new ArrayList<CraftAmount>(amounts.size());
        for (var i = 0; i < amounts.size(); i++) {
            out.add(codec.decodeAmount(amounts.getCompound(i)));
        }
        return out;
    }

}
