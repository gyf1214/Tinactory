package org.shsts.tinactory.core.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(0, Blocks.STONE, Tiers.IRON, Voltage.ULV, 3f, 6f, "stone"),
    DEEPSLATE(1, Blocks.DEEPSLATE, Tiers.IRON, Voltage.LV, 4.5f, 6f, "stone"),
    NETHERRACK(2, Blocks.NETHERRACK, Tiers.DIAMOND, Voltage.HV, 6f, 7.5f, "netherrack"),
    END_STONE(3, Blocks.END_STONE, Tiers.NETHERITE, Voltage.EV, 7.5f, 9f, "end_stone");

    public final int rank;
    public final Block baseBlock;
    public final Item baseItem;
    public final Tier mineTier;
    public final Voltage voltage;
    public final float destroyTime;
    public final float explodeResistance;
    public final String material;

    OreVariant(int rank, Block baseBlock, Tier mineTier, Voltage voltage,
        float destroyTime, float explodeResistance, String material) {
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
        this.material = material;
    }

    public String getName() {
        return name().toLowerCase();
    }

    public static OreVariant fromName(String name) {
        return valueOf(name.toUpperCase());
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
