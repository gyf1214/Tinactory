package org.shsts.tinactory.api.tech;

import java.util.Optional;

public interface IClientTechManager extends ITechManager {
    Optional<ITeamProfile> localTeamProfile();
}
