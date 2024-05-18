package org.shsts.tinactory.api.tech;

public interface IServerTeamProfile extends ITeamProfile {
    void advanceTechProgress(ITechnology tech, long progress);
}
