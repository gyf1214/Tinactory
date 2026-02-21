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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(0, Blocks.STONE, Items.COBBLESTONE, Tiers.IRON, Voltage.ULV,
        3f, 6f, Material.STONE, MaterialColor.STONE, SoundType.STONE, "stone"),
    DEEPSLATE(1, Blocks.DEEPSLATE, Items.COBBLED_DEEPSLATE, Tiers.IRON, Voltage.LV,
        4.5f, 6f, Material.STONE, MaterialColor.DEEPSLATE, SoundType.DEEPSLATE, "stone"),
    NETHERRACK(2, Blocks.NETHERRACK, Items.NETHERRACK, Tiers.DIAMOND, Voltage.HV,
        6f, 7.5f, Material.STONE, MaterialColor.NETHER, SoundType.NETHERRACK, "netherrack"),
    END_STONE(3, Blocks.END_STONE, Items.END_STONE, Tiers.NETHERITE, Voltage.EV,
        7.5f, 9f, Material.STONE, MaterialColor.SAND, SoundType.STONE, "end_stone");

    public final int rank;
    public final Block baseBlock;
    public final Item baseItem;
    public final Tier mineTier;
    public final Voltage voltage;
    public final float destroyTime;
    public final float explodeResistance;
    public final Material blockMaterial;
    public final MaterialColor materialColor;
    public final SoundType soundType;
    public final String material;

    OreVariant(int rank, Block baseBlock, Item baseItem, Tier mineTier, Voltage voltage,
        float destroyTime, float explodeResistance, Material blockMaterial,
        MaterialColor materialColor, SoundType soundType, String material) {
        this.rank = rank;
        this.baseBlock = baseBlock;
        this.baseItem = baseItem;
        this.mineTier = mineTier;
        this.voltage = voltage;
        this.destroyTime = destroyTime;
        this.explodeResistance = explodeResistance;
        this.blockMaterial = blockMaterial;
        this.materialColor = materialColor;
        this.soundType = soundType;
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
