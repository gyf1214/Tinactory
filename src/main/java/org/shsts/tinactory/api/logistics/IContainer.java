package org.shsts.tinactory.api.logistics;

import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Optional;

public interface IContainer {
    Optional<? extends ITeamProfile> getOwnerTeam();

    int portSize();

    boolean hasPort(int port);

    PortDirection portDirection(int port);

    IPort getPort(int port, boolean internal);
}
