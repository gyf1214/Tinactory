package org.shsts.tinactory.unit.autocraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.integration.PatternCellStorage;
import org.shsts.tinactory.core.autocraft.integration.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternCellIntegrationTest {
    @Test
    void patternCellShouldUseFixedByteAccounting() {
        var tag = new CompoundTag();
        var codec = new PatternNbtCodec(new MachineConstraintRegistry());
        var first = pattern("tinactory:first");
        var second = pattern("tinactory:second");

        assertTrue(PatternCellStorage.insertPattern(tag, 2048, first, codec));
        assertTrue(PatternCellStorage.insertPattern(tag, 2048, second, codec));

        assertEquals(2, PatternCellStorage.listPatterns(tag, codec).size());
        assertEquals(2 * PatternCellStorage.BYTES_PER_PATTERN, PatternCellStorage.bytesUsed(tag, codec));
    }

    @Test
    void patternCellShouldSkipInvalidEntries() {
        var tag = new CompoundTag();
        var codec = new PatternNbtCodec(new MachineConstraintRegistry());
        var list = new ListTag();
        list.add(new CompoundTag());
        tag.put(PatternCellStorage.PATTERNS_KEY, list);

        assertEquals(0, PatternCellStorage.listPatterns(tag, codec).size());
        assertFalse(PatternCellStorage.listPatterns(tag, codec).iterator().hasNext());
    }

    private static CraftPattern pattern(String id) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }
}
