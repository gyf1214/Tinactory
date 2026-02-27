package org.shsts.tinactory.unit.autocraft;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.integration.NetworkPatternCell;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternNetworkApiTest {
    @Test
    void writeShouldAutoPlaceByPriorityThenMachineThenSlot() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var cellB = new NetworkPatternCell(
            uuid("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), new BlockPos(1, 0, 0), 8, 1, new FakePatternPort(1024));
        var cellA = new NetworkPatternCell(
            uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), new BlockPos(2, 0, 0), 8, 2, new FakePatternPort(1024));
        var cellC = new NetworkPatternCell(
            uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), new BlockPos(3, 0, 0), 8, 0, new FakePatternPort(1024));
        component.registerPatternCell(cellB);
        component.registerPatternCell(cellA);
        component.registerPatternCell(cellC);

        var ok = component.writePattern(pattern("tinactory:target"));

        assertTrue(ok);
        assertEquals(1, cellC.patterns().size());
        assertEquals(0, cellA.patterns().size());
        assertEquals(0, cellB.patterns().size());
    }

    @Test
    void writeShouldRejectWhenNoVisibleCapacity() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var cell = new NetworkPatternCell(
            uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), new BlockPos(0, 0, 0), 1, 0, new FakePatternPort(256));
        component.registerPatternCell(cell);
        assertTrue(component.writePattern(pattern("tinactory:first")));

        assertFalse(component.writePattern(pattern("tinactory:second")));
    }

    @Test
    void readShouldReturnAllNetworkPatterns() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var cell = new NetworkPatternCell(
            uuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), new BlockPos(1, 0, 0), 1, 0, new FakePatternPort(1024));
        var other = new NetworkPatternCell(
            uuid("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), new BlockPos(9, 0, 0), 1, 0, new FakePatternPort(1024));
        cell.insert(pattern("tinactory:one"));
        other.insert(pattern("tinactory:two"));
        component.registerPatternCell(cell);
        component.registerPatternCell(other);

        var ids = component.listVisiblePatterns().stream().map(CraftPattern::patternId).toList();

        assertEquals(List.of("tinactory:one", "tinactory:two"), ids);
    }

    private static CraftPattern pattern(String id) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_plate", ""), 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static UUID uuid(String value) {
        return UUID.fromString(value);
    }

    private static final class FakePatternPort implements IPatternCellPort {
        private final int capacity;
        private final List<CraftPattern> patterns = new java.util.ArrayList<>();

        private FakePatternPort(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int bytesCapacity() {
            return capacity;
        }

        @Override
        public int bytesUsed() {
            return patterns.size() * 256;
        }

        @Override
        public List<CraftPattern> patterns() {
            return List.copyOf(patterns);
        }

        @Override
        public boolean insert(CraftPattern pattern) {
            if (patterns.stream().anyMatch($ -> $.patternId().equals(pattern.patternId()))) {
                return true;
            }
            if ((patterns.size() + 1) * 256 > capacity) {
                return false;
            }
            patterns.add(pattern);
            return true;
        }

        @Override
        public boolean remove(String patternId) {
            return patterns.removeIf($ -> $.patternId().equals(patternId));
        }
    }

    private static final class FakeNetwork implements INetwork {
        @Override
        public ITeamProfile owner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends INetworkComponent> T getComponent(IComponentType<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockPos getSubnet(BlockPos pos) {
            return BlockPos.ZERO;
        }

        @Override
        public Multimap<BlockPos, IMachine> allMachines() {
            return ArrayListMultimap.create();
        }

        @Override
        public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
            return List.of();
        }
    }
}
