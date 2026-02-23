package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.logistics.MEDrive;
import org.shsts.tinactory.core.autocraft.integration.IPatternCellPort;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.logistics.IBytesProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MEDrivePatternBytesTest {
    @Test
    void aggregateByteStatsShouldIncludeDigitalAndPatternPorts() {
        var stats = MEDrive.aggregateByteStats(
            List.of(provider(30, 100), provider(10, 50)),
            List.of(patternPort(20, 80)));

        assertEquals(60, stats.bytesUsed());
        assertEquals(230, stats.bytesCapacity());
    }

    private static IBytesProvider provider(int used, int capacity) {
        return new IBytesProvider() {
            @Override
            public int bytesCapacity() {
                return capacity;
            }

            @Override
            public int bytesUsed() {
                return used;
            }
        };
    }

    private static IPatternCellPort patternPort(int used, int capacity) {
        return new IPatternCellPort() {
            @Override
            public int bytesCapacity() {
                return capacity;
            }

            @Override
            public int bytesUsed() {
                return used;
            }

            @Override
            public List<CraftPattern> patterns() {
                return List.of();
            }

            @Override
            public boolean insert(CraftPattern pattern) {
                return false;
            }

            @Override
            public boolean remove(String patternId) {
                return false;
            }
        };
    }
}
