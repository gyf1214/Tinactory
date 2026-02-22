package org.shsts.tinactory.unit.autocraft;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJob;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutocraftLogisticsIntegrationTest {
    @Test
    void logisticComponentShouldTickAutocraftService() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var service = new AutocraftJobService((targets, available) -> {
            throw new UnsupportedOperationException();
        }, () -> null, List::of) {
            @Override
            public void tick() {
                tickCount++;
            }
        };

        component.setAutocraftJobService(service);
        component.tickAutocraftJobs();

        assertEquals(1, tickCount);
    }

    @Test
    void logisticComponentShouldForwardSubmitRequest() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var target = new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1);
        var service = new AutocraftJobService((targets, available) -> {
            throw new UnsupportedOperationException();
        }, () -> null, List::of) {
            @Override
            public UUID submit(List<CraftAmount> targets) {
                submitted = targets;
                return jobId;
            }

            @Override
            public AutocraftJob job(UUID id) {
                return new AutocraftJob(id, List.of(), AutocraftJob.Status.QUEUED, null, null);
            }
        };

        component.setAutocraftJobService(service);
        var id = component.submitAutocraft(List.of(target));

        assertEquals(jobId, id);
        assertEquals(List.of(target), submitted);
    }

    private static int tickCount = 0;
    private static List<CraftAmount> submitted = List.of();
    private static final UUID jobId = UUID.fromString("11111111-1111-1111-1111-111111111111");

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
