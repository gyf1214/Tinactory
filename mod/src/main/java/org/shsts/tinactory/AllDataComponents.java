package org.shsts.tinactory;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternCellData;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.logistics.DigitalCellData;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class AllDataComponents {
    private static final IEntryHandler<DataComponentType<?>> DATA_COMPONENT_TYPES =
        REGISTRATE.getHandler(Registries.DATA_COMPONENT_TYPE, BuiltInRegistries.DATA_COMPONENT_TYPE);
    private static final Codec<DigitalCellData> DIGITAL_CELL_CODEC = DigitalCellData.codec(StackHelper.KEY_CODEC);
    private static final Codec<PatternCellData> PATTERN_CELL_CODEC =
        PatternCellData.codec(new PatternNbtCodec(MachineConstraintHelper.CODEC, StackHelper.KEY_CODEC).patternCodec());

    public static final IEntry<DataComponentType<SimpleFluidContent>> FLUID_CELL_CONTENT =
        component("fluid_cell_content", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
    public static final IEntry<DataComponentType<DigitalCellData>> ME_ITEM_CELL_CONTENT =
        component("me_item_cell_content", DIGITAL_CELL_CODEC, streamCodec(DIGITAL_CELL_CODEC));
    public static final IEntry<DataComponentType<DigitalCellData>> ME_FLUID_CELL_CONTENT =
        component("me_fluid_cell_content", DIGITAL_CELL_CODEC, streamCodec(DIGITAL_CELL_CODEC));
    public static final IEntry<DataComponentType<PatternCellData>> ME_PATTERN_CELL_CONTENT =
        component("me_pattern_cell_content", PATTERN_CELL_CODEC, streamCodec(PATTERN_CELL_CODEC));

    private static <T> IEntry<DataComponentType<T>> component(
        String name,
        Codec<T> codec,
        StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return REGISTRATE.registryEntry(DATA_COMPONENT_TYPES, name, () -> DataComponentType.<T>builder()
            .persistent(codec)
            .networkSynchronized(streamCodec)
            .build());
    }

    private static <T> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Codec<T> codec) {
        return ByteBufCodecs.fromCodecWithRegistries(codec);
    }

    public static void init() {}
}
