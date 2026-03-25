package org.shsts.tinactory.core.autocraft.service;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.exec.ExecutorSnapshot;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record AutocraftJobSnapshot(UUID jobId, List<CraftAmount> targets, ExecutorSnapshot execution) {
    public AutocraftJobSnapshot {
        targets = List.copyOf(targets);
    }
}
