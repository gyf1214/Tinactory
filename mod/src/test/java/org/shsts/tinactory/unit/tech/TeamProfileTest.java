package org.shsts.tinactory.unit.tech;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.Technology;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamProfileTest {
    @Test
    void progressClampsAndCompletionModifierAppliesOnlyOnce() {
        var tech = technology("tinactory:progress", List.of(), 10L, Map.of("speed", 2), 1);
        var manager = new TestTechManager(tech);
        var profile = new TeamProfile(manager, "alpha");

        profile.setTechProgress(tech, 15L);
        profile.setTechProgress(tech, 30L);

        assertEquals(10L, profile.getTechProgress(tech));
        assertEquals(2, profile.getModifier("speed"));
    }

    @Test
    void availabilityAndCanResearchFollowDependencyAndProgressRules() {
        var dependency = technology("tinactory:dependency", List.of(), 5L, Map.of(), 1);
        var target = technology("tinactory:target", List.of(dependency.getLoc()), 10L, Map.of(), 2);
        var profile = new TeamProfile(new TestTechManager(dependency, target), "alpha");

        assertFalse(profile.isTechAvailable(target));

        profile.setTechProgress(target, 1L);
        assertTrue(profile.isTechAvailable(target));
        assertTrue(profile.canResearch(target, 9L));
        assertFalse(profile.canResearch(target, 10L));

        profile.setTechProgress(target, 10L);
        assertFalse(profile.canResearch(target));
    }

    @Test
    void targetTechCanBeSetAndReset() {
        var tech = technology("tinactory:target", List.of(), 10L, Map.of(), 1);
        var profile = new TeamProfile(new TestTechManager(tech), "alpha");

        profile.setTargetTech(tech);
        assertSame(tech, profile.getTargetTech().orElseThrow());

        profile.resetTargetTech();
        assertTrue(profile.getTargetTech().isEmpty());
    }

    private static Technology technology(String loc, List<ResourceLocation> depends,
        long maxProgress, Map<String, Integer> modifiers, int rank) {

        var technology = new Technology(depends, maxProgress, modifiers, Optional.empty(), Optional.empty(), rank);
        technology.setLoc(new ResourceLocation(loc));
        return technology;
    }

    private static final class TestTechManager implements ITechManager {
        private final Map<ResourceLocation, Technology> technologies;

        private TestTechManager(Technology... technologies) {
            this.technologies = new LinkedHashMap<>();
            for (var technology : technologies) {
                this.technologies.put(technology.getLoc(), technology);
            }
            this.technologies.values().forEach(technology -> technology.resolve(this));
        }

        @Override
        public Optional<Technology> techByKey(ResourceLocation loc) {
            return Optional.ofNullable(technologies.get(loc));
        }

        @Override
        public Collection<Technology> allTechs() {
            return technologies.values();
        }

        @Override
        public void onProgressChange(Consumer<ITeamProfile> callback) {}

        @Override
        public void removeProgressChangeListener(Consumer<ITeamProfile> callback) {}
    }
}
