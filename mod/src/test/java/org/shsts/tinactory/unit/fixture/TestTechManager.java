package org.shsts.tinactory.unit.fixture;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.core.tech.Technology;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class TestTechManager implements ITechManager {
    private final Map<ResourceLocation, Technology> technologies = new LinkedHashMap<>();

    public TestTechManager(Technology... technologies) {
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
