package org.shsts.tinactory.api.tech;

import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface ITechManager {
    Optional<? extends ITechnology> techByKey(ResourceLocation loc);

    Collection<? extends ITechnology> allTechs();

    void onProgressChange(Consumer<ITeamProfile> callback);

    void removeProgressChangeListener(Consumer<ITeamProfile> callback);

    void broadcastUpdate(ITeamProfile team, IPacket packet);
}
