package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Tier;
import org.shsts.tinactory.integration.material.MaterialSet;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredToolItem extends PoweredItem {
    private final long normalUseCost;
    private final long specialAbilityCost;
    private final float miningSpeed;
    private final Tier harvestTier;
    private final MaterialSet material;

    public PoweredToolItem(Properties properties, PoweredToolConfig config) {
        super(properties, config.voltage(), config.capacity());
        this.normalUseCost = config.normalUseCost();
        this.specialAbilityCost = config.specialAbilityCost();
        this.miningSpeed = config.miningSpeed();
        this.harvestTier = config.harvestTier();
        this.material = config.material();
    }

    public long normalUseCost() {
        return normalUseCost;
    }

    public long specialAbilityCost() {
        return specialAbilityCost;
    }

    public float miningSpeed() {
        return miningSpeed;
    }

    public Tier harvestTier() {
        return harvestTier;
    }

    public MaterialSet material() {
        return material;
    }
}
