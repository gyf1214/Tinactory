package org.shsts.tinactory.core.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechManager implements ITechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Map<ResourceLocation, Technology> technologies = new HashMap<>();
    private final Set<Consumer<ITeamProfile>> changeCallbacks = new HashSet<>();

    @Override
    public Optional<Technology> techByKey(ResourceLocation loc) {
        return Optional.ofNullable(technologies.get(loc));
    }

    @Override
    public Collection<Technology> allTechs() {
        return technologies.values();
    }

    public void unload() {
        LOGGER.debug("unload tech manager {}", this);
        technologies.clear();
    }

    @Override
    public void onProgressChange(Consumer<ITeamProfile> callback) {
        changeCallbacks.add(callback);
    }

    @Override
    public void removeProgressChangeListener(Consumer<ITeamProfile> callback) {
        changeCallbacks.remove(callback);
    }

    public void invokeChange(ITeamProfile teamProfile) {
        for (var cb : changeCallbacks) {
            cb.accept(teamProfile);
        }
    }
}
