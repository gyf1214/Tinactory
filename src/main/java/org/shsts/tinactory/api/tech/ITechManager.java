package org.shsts.tinactory.api.tech;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

public interface ITechManager {
    Optional<? extends ITechnology> techByKey(ResourceLocation loc);

    Collection<? extends ITechnology> allTechs();
}
