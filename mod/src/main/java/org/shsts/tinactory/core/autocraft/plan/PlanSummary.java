package org.shsts.tinactory.core.autocraft.plan;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;

import java.util.LinkedHashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PlanSummary(Map<IStackKey, Entry> entries) {
    public PlanSummary {
        entries = Map.copyOf(entries);
    }

    public static PlanSummary empty() {
        return new PlanSummary(Map.of());
    }

    static PlanSummary copyOf(Map<IStackKey, Entry> entries) {
        return new PlanSummary(new LinkedHashMap<>(entries));
    }

    public record Entry(long existingAmount, long consumedFromInventory, long craftedAmount) {}
}
