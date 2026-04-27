package org.shsts.tinactory.unit.tech;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.unit.fixture.TestTechnologyHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TechManagerTest {
    @Test
    void techLookupAndUnloadManageTechnologyMap() {
        var alpha = technology("tinactory:alpha", 1);
        var beta = technology("tinactory:beta", 2);
        var manager = new StubTechManager(alpha, beta);

        assertSame(alpha, manager.techByKey(alpha.loc()).orElseThrow());
        assertIterableEquals(Set.of(alpha, beta), Set.copyOf(manager.allTechs()));

        manager.unload();

        assertTrue(manager.techByKey(alpha.loc()).isEmpty());
        assertTrue(manager.allTechs().isEmpty());
    }

    @Test
    void progressChangeListenersCanBeAddedInvokedAndRemoved() {
        var manager = new StubTechManager();
        var profile = new TeamProfile(manager, "alpha");
        var invocations = new AtomicInteger();
        ITeamProfile[] changedProfile = new ITeamProfile[1];
        Consumer<ITeamProfile> callback = changed -> {
            invocations.incrementAndGet();
            changedProfile[0] = changed;
        };

        manager.onProgressChange(callback);
        manager.onProgressChange(callback);
        manager.invokeChange(profile);

        assertEquals(1, invocations.get());
        assertSame(profile, changedProfile[0]);

        manager.removeProgressChangeListener(callback);
        manager.invokeChange(profile);

        assertEquals(1, invocations.get());
    }

    private static Technology technology(String loc, int rank) {
        return TestTechnologyHelper.technology(loc, rank);
    }

    private static final class StubTechManager extends TechManager {
        private StubTechManager(Technology... technologies) {
            for (var technology : technologies) {
                this.technologies.put(technology.loc(), technology);
            }
        }

        @Override
        public void broadcastUpdate(ITeamProfile team, IPacket packet) {}
    }
}
