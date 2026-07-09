package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.shsts.tinactory.core.autocraft.api.IAutocraftService;
import org.shsts.tinactory.core.autocraft.api.ICraftExecutor;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.minecraft.nbt.Tag.TAG_COMPOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftJobService implements IAutocraftService {
    private static final long DEFAULT_ITEM_BANDWIDTH = 64L;
    private static final long DEFAULT_FLUID_BANDWIDTH = 16000L;
    private static final int DEFAULT_EXECUTION_INTERVAL_TICKS = 1;

    private final ICraftExecutor executor;
    private final long itemBandwidth;
    private final long fluidBandwidth;
    private final int executionIntervalTicks;
    private final long memoryLimit;

    private List<CraftAmount> currentTargets = List.of();
    private long currentMemoryUsage;
    private int pendingTicks;

    public AutocraftJobService(
        ICraftExecutor executor,
        long itemBandwidth,
        long fluidBandwidth,
        int executionIntervalTicks,
        long memoryLimit) {

        this.executor = executor;
        this.itemBandwidth = itemBandwidth;
        this.fluidBandwidth = fluidBandwidth;
        this.executionIntervalTicks = Math.max(1, executionIntervalTicks);
        this.memoryLimit = Math.max(0L, memoryLimit);
    }

    public AutocraftJobService(ICraftExecutor executor, long itemBandwidth, long fluidBandwidth,
        int executionIntervalTicks) {
        this(executor, itemBandwidth, fluidBandwidth, executionIntervalTicks, Long.MAX_VALUE);
    }

    public AutocraftJobService(ICraftExecutor executor) {
        this(executor, DEFAULT_ITEM_BANDWIDTH, DEFAULT_FLUID_BANDWIDTH, DEFAULT_EXECUTION_INTERVAL_TICKS);
    }

    @Override
    public boolean isBusy() {
        return !currentTargets.isEmpty() && executor.isBusy();
    }

    @Override
    public long memoryLimit() {
        return memoryLimit;
    }

    public Optional<CompoundTag> serializeRunningSnapshot(HolderLookup.Provider provider, PatternCodec codec) {
        if (!isBusy()) {
            return Optional.empty();
        }
        var tag = new CompoundTag();
        tag.put("targets", serializeAmounts(provider, currentTargets, codec));
        tag.put("execution", executor.serialize(provider, codec));
        tag.putLong("memoryUsage", currentMemoryUsage);
        return Optional.of(tag);
    }

    public void restoreRunningSnapshot(HolderLookup.Provider provider, CompoundTag tag, PatternCodec codec) {
        if (!currentTargets.isEmpty() || executor.isBusy()) {
            return;
        }
        var targets = deserializeAmounts(provider, tag.getList("targets", TAG_COMPOUND), codec);
        if (targets.isEmpty()) {
            return;
        }
        executor.restore(provider, tag.getCompound("execution"), codec);
        currentTargets = executor.isBusy() ? List.copyOf(targets) : List.of();
        currentMemoryUsage = executor.isBusy() ? tag.getLong("memoryUsage") : 0L;
        pendingTicks = 0;
    }

    @Override
    public void submitPrepared(List<CraftAmount> targets, CraftPlan plan) {
        if (isBusy()) {
            throw new IllegalStateException("autocraft CPU is busy");
        }
        executor.start(plan);
        currentTargets = executor.isBusy() ? List.copyOf(targets) : List.of();
        currentMemoryUsage = executor.isBusy() ? plan.memoryUsage() : 0L;
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
            executor.error(),
            currentMemoryUsage));
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
        executor.runCycle(itemBandwidth, fluidBandwidth);
        if (!executor.isBusy()) {
            currentTargets = List.of();
            currentMemoryUsage = 0L;
        }
        return true;
    }

    private static ListTag serializeAmounts(HolderLookup.Provider provider, List<CraftAmount> amounts,
        PatternCodec codec) {
        var out = new ListTag();
        for (var amount : amounts) {
            out.add(codec.encodeAmount(provider, amount));
        }
        return out;
    }

    private static List<CraftAmount> deserializeAmounts(HolderLookup.Provider provider, ListTag amounts,
        PatternCodec codec) {
        var out = new ArrayList<CraftAmount>(amounts.size());
        for (var i = 0; i < amounts.size(); i++) {
            out.add(codec.decodeAmount(provider, amounts.getCompound(i)));
        }
        return out;
    }
}
