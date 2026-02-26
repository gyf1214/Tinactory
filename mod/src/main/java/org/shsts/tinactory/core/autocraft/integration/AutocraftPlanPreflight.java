package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.plan.CraftPlan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AutocraftPlanPreflight {
    public Map<CraftKey, Long> findMissingInputs(CraftPlan plan, List<CraftAmount> available) {
        var required = new LinkedHashMap<CraftKey, Long>();
        for (var step : plan.steps()) {
            for (var input : step.pattern().inputs()) {
                var amount = input.amount() * step.runs();
                required.put(input.key(), required.getOrDefault(input.key(), 0L) + amount);
            }
        }

        var availableMap = new LinkedHashMap<CraftKey, Long>();
        for (var amount : available) {
            availableMap.put(amount.key(), availableMap.getOrDefault(amount.key(), 0L) + amount.amount());
        }

        var missing = new LinkedHashMap<CraftKey, Long>();
        for (var entry : required.entrySet()) {
            var left = entry.getValue() - availableMap.getOrDefault(entry.getKey(), 0L);
            if (left > 0L) {
                missing.put(entry.getKey(), left);
            }
        }
        return missing;
    }
}
