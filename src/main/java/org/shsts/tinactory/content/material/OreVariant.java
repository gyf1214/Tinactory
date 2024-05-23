package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.machine.Voltage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(Blocks.STONE, Tiers.IRON, Voltage.ULV, 3f, 6f),
    DEEPSLATE(Blocks.DEEPSLATE, Tiers.IRON, Voltage.LV, 4.5f, 6f),
    NETHERRACK(Blocks.NETHERRACK, Tiers.DIAMOND, Voltage.MV, 3f, 3f),
    END_STONE(Blocks.END_STONE, Tiers.NETHERITE, Voltage.EV, 4.5f, 9f);

    public final Block baseBlock;
    public final Tier mineTier;
    public final Voltage voltage;
    public final float destroyTime;
    public final float explodeResistance;

    OreVariant(Block baseBlock, Tier mineTier, Voltage voltage,
               float destroyTime, float explodeResistance) {
        this.mineTier = mineTier;
        this.voltage = voltage;
        this.destroyTime = destroyTime;
        this.explodeResistance = explodeResistance;
        this.baseBlock = baseBlock;
    }

    public String getName() {
        return name().toLowerCase();
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
}
