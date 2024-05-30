package org.shsts.tinactory.api.tech;

import net.minecraft.resources.ResourceLocation;

public interface IServerTeamProfile extends ITeamProfile {
    void advanceTechProgress(ITechnology tech, long progress);

    void advanceTechProgress(ResourceLocation tech, long progress);

    void setTargetTech(ITechnology tech);

    void resetTargetTech();
}
