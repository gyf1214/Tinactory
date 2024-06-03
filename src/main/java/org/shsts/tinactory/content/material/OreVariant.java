package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.machine.Voltage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(0, Blocks.STONE, Tiers.IRON, Voltage.ULV, 3f, 6f),
    DEEPSLATE(1, Blocks.DEEPSLATE, Tiers.IRON, Voltage.LV, 4.5f, 6f),
    NETHERRACK(2, Blocks.NETHERRACK, Tiers.DIAMOND, Voltage.MV, 6f, 7.5f),
    END_STONE(3, Blocks.END_STONE, Tiers.NETHERITE, Voltage.EV, 7.5f, 9f);

    public final int rank;
    public final Block baseBlock;
    public final Item baseItem;
    public final Tier mineTier;
    public final Voltage voltage;
    public final float destroyTime;
    public final float explodeResistance;

    OreVariant(int rank, Block baseBlock, Tier mineTier, Voltage voltage,
               float destroyTime, float explodeResistance) {
        this.rank = rank;
        this.mineTier = mineTier;
        this.voltage = voltage;
        this.destroyTime = destroyTime;
        this.explodeResistance = explodeResistance;
        this.baseBlock = baseBlock;
        if (baseBlock == Blocks.STONE) {
            this.baseItem = Items.COBBLESTONE;
        } else if (baseBlock == Blocks.DEEPSLATE) {
            this.baseItem = Items.COBBLED_DEEPSLATE;
        } else {
            this.baseItem = baseBlock.asItem();
        }
    }

    public String getName() {
        return name().toLowerCase();
    }

    public ResourceLocation getLoc() {
        var loc = baseItem.getRegistryName();
        assert loc != null;
        return loc;
    }

    @Override
    public String getSerializedName() {
        return getName();
    }
}
