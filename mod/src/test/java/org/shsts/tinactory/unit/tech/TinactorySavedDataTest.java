package org.shsts.tinactory.unit.tech;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.tech.Technology;
import org.shsts.tinactory.core.tech.TinactorySavedData;
import org.shsts.tinactory.unit.fixture.TestTechManager;
import org.shsts.tinactory.unit.fixture.TestTechnologyHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestCodecHelper.TEST_REGISTRY;

class TinactorySavedDataTest {
    @Test
    void createsRemovesAndRecreatesProfilesByTeamName() {
        var manager = new TestTechManager();
        var data = new TinactorySavedData(manager);

        var first = data.getTeamProfile("alpha");
        var second = data.getTeamProfile("alpha");
        var third = data.getTeamProfile("beta");
        data.removeTeamProfile("alpha");
        var recreated = data.getTeamProfile("alpha");

        assertSame(first, second);
        assertEquals("beta", third.getName());
        assertEquals("alpha", recreated.getName());
        assertEquals(3, data.nextId());
    }

    @Test
    void saveLoadRoundTripPreservesKnownProgressAndIgnoresUnknownTechnologies() {
        var known = technology("tinactory:known", 10L, 1);
        var manager = new TestTechManager(known);
        var data = new TinactorySavedData(manager);
        var profile = data.getTeamProfile("alpha");
        profile.setTechProgress(known, 6L);
        profile.setTargetTech(known);
        var saved = data.save(new CompoundTag(), TEST_REGISTRY);

        var teams = saved.getList("teams", Tag.TAG_COMPOUND);
        var teamTag = teams.getCompound(0);
        var tech = teamTag.getList("tech", Tag.TAG_COMPOUND);
        var unknownTechTag = new CompoundTag();
        unknownTechTag.putString("id", "tinactory:missing");
        unknownTechTag.putLong("progress", 99L);
        tech.add(unknownTechTag);
        teamTag.putString("target", "tinactory:missing");

        var loaded = TinactorySavedData.fromTag(saved, TEST_REGISTRY, manager);
        var loadedProfile = loaded.getTeamProfile("alpha");

        assertEquals(6L, loadedProfile.getTechProgress(known));
        assertTrue(loadedProfile.getTargetTech().isEmpty());
        assertEquals(1, loaded.nextId());
    }

    private static Technology technology(String loc, long maxProgress, int rank) {
        return TestTechnologyHelper.technology(loc, maxProgress, rank);
    }
}
