package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Tier;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.material.MaterialSet;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PoweredToolConfig(Voltage voltage, long capacity, long normalUseCost,
    long specialAbilityCost, float miningSpeed, Tier harvestTier, MaterialSet material) {}
