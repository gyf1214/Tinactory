package org.shsts.tinactory.integration.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.Tags;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum OreVariant implements StringRepresentable {
    STONE(0, Blocks.STONE, Items.COBBLESTONE, BlockTags.NEEDS_IRON_TOOL, Voltage.ULV,
        3f, 6f, MapColor.STONE, SoundType.STONE, "stone"),
    DEEPSLATE(1, Blocks.DEEPSLATE, Items.COBBLED_DEEPSLATE, BlockTags.NEEDS_IRON_TOOL, Voltage.LV,
        4.5f, 6f, MapColor.DEEPSLATE, SoundType.DEEPSLATE, "stone"),
    NETHERRACK(2, Blocks.NETHERRACK, Items.NETHERRACK, BlockTags.NEEDS_DIAMOND_TOOL, Voltage.HV,
        6f, 7.5f, MapColor.NETHER, SoundType.NETHERRACK, "netherrack"),
    END_STONE(3, Blocks.END_STONE, Items.END_STONE, Tags.Blocks.NEEDS_NETHERITE_TOOL, Voltage.EV,
        7.5f, 9f, MapColor.SAND, SoundType.STONE, "end_stone");

    public final int rank;
    public final Block baseBlock;
    public final Item baseItem;
    public final TagKey<Block> mineTag;
    public final Voltage voltage;
    public final float destroyTime;
    public final float explodeResistance;
    public final MapColor mapColor;
    public final SoundType soundType;
    public final String material;

    OreVariant(int rank, Block baseBlock, Item baseItem, TagKey<Block> mineTag, Voltage voltage,
        float destroyTime, float explodeResistance, MapColor mapColor, SoundType soundType,
        String material) {
        this.rank = rank;
        this.baseBlock = baseBlock;
        this.baseItem = baseItem;
        this.mineTag = mineTag;
        this.voltage = voltage;
        this.destroyTime = destroyTime;
        this.explodeResistance = explodeResistance;
        this.mapColor = mapColor;
        this.soundType = soundType;
        this.material = material;
    }

    public static OreVariant fromName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public ResourceLocation getLoc() {
        return BuiltInRegistries.ITEM.getKey(baseItem);
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
