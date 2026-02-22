package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.integration.LogisticsPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogisticsAdapterContractTest {
    @Test
    void repositoryShouldReturnPatternsSortedByPatternId() {
        var key = CraftKey.item("minecraft:iron_ingot", "");
        var second = pattern("tinactory:z_second", key);
        var first = pattern("tinactory:a_first", key);
        var repo = new LogisticsPatternRepository(List.of(second, first));

        var actual = repo.findPatternsProducing(key);

        assertEquals(List.of("tinactory:a_first", "tinactory:z_second"),
            actual.stream().map(CraftPattern::patternId).toList());
    }

    private static CraftPattern pattern(String id, CraftKey key) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(key, 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }
}
