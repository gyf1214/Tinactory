package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftPreviewSessionStore {
    private final Map<UUID, PreviewSnapshot> snapshots = new LinkedHashMap<>();

    public void put(
        UUID planId, UUID cpuId, List<CraftAmount> targets, CraftPlan planSnapshot, List<CraftAmount> summaryOutputs) {
        snapshots.put(
            planId, new PreviewSnapshot(cpuId, List.copyOf(targets), planSnapshot, List.copyOf(summaryOutputs)));
    }

    public Optional<PreviewSnapshot> find(UUID planId) {
        return Optional.ofNullable(snapshots.get(planId));
    }

    public Optional<PreviewSnapshot> remove(UUID planId) {
        return Optional.ofNullable(snapshots.remove(planId));
    }

    public void clear() {
        snapshots.clear();
    }

    public record PreviewSnapshot(UUID cpuId, List<CraftAmount> targets, CraftPlan planSnapshot,
                                  List<CraftAmount> summaryOutputs) {}
}
