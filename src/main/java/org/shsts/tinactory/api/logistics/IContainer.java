package org.shsts.tinactory.api.logistics;

import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Optional;

public interface IContainer {
    Optional<ITeamProfile> getOwnerTeam();

    boolean hasPort(int port);

    IPort getPort(int port, boolean internal);
}
