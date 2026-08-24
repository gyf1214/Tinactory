package org.shsts.tinactory.api.tech;

import java.util.Optional;

public interface IClientTechManager extends ITechManager {
    Optional<ITeamProfile> localTeamProfile();

    boolean techInitialized();

    void onTechInit(Runnable callback);

    void removeTechInitListener(Runnable callback);
}
