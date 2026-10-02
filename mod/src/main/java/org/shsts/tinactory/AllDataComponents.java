package org.shsts.tinactory;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.shsts.tinactory.core.autocraft.pattern.PatternCellData;
import org.shsts.tinactory.core.logistics.DigitalCellData;
import org.shsts.tinactory.integration.autocraft.PatternHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import java.util.UUID;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class AllDataComponents {
    private static final IEntryHandler<DataComponentType<?>> DATA_COMPONENT_TYPES =
        REGISTRATE.getHandler(Registries.DATA_COMPONENT_TYPE, BuiltInRegistries.DATA_COMPONENT_TYPE);
    private static final IEntryHandler<AttachmentType<?>> ATTACHMENT_TYPES =
        REGISTRATE.getHandler(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, NeoForgeRegistries.ATTACHMENT_TYPES);
    private static final Codec<DigitalCellData> DIGITAL_CELL_CODEC =
        DigitalCellData.codec(StackHelper.KEY_CODEC);
    private static final Codec<PatternCellData> PATTERN_CELL_CODEC =
        PatternCellData.codec(PatternHelper.PATTERN_CODEC);

    public static final IEntry<DataComponentType<SimpleFluidContent>> FLUID_CELL_CONTENT =
        component("fluid_cell_content", SimpleFluidContent.CODEC, SimpleFluidContent.STREAM_CODEC);
    public static final IEntry<DataComponentType<DigitalCellData>> ME_ITEM_CELL_CONTENT =
        component("me_item_cell_content", DIGITAL_CELL_CODEC);
    public static final IEntry<DataComponentType<DigitalCellData>> ME_FLUID_CELL_CONTENT =
        component("me_fluid_cell_content", DIGITAL_CELL_CODEC);
    public static final IEntry<DataComponentType<PatternCellData>> ME_PATTERN_CELL_CONTENT =
        component("me_pattern_cell_content", PATTERN_CELL_CODEC);
    public static final IEntry<DataComponentType<Long>> BATTERY = component("battery", Codec.LONG);
    public static final IEntry<DataComponentType<Long>> REACTIONS = component("reaction", Codec.LONG);
    public static final IEntry<DataComponentType<Unit>> HIDE_BAR = component("hide_bar", Unit.CODEC);
    public static final IEntry<DataComponentType<UUID>> UUID =
        component("uuid", UUIDUtil.CODEC, UUIDUtil.STREAM_CODEC);
    public static final IEntry<DataComponentType<Unit>> POWERED_ACTIVATED =
        component("powered_activated", Unit.CODEC);
    public static final IEntry<AttachmentType<Direction>> DRILL_HIT_FACE =
        attachment("drill_hit_face", () -> AttachmentType.builder(() -> Direction.UP).build());
    public static final IEntry<AttachmentType<Unit>> POWERED_TOOL_BREAK_GUARD =
        attachment("powered_tool_break_guard", () -> AttachmentType.builder(() -> Unit.INSTANCE).build());

    private static <T> IEntry<DataComponentType<T>> component(
        String name, Codec<T> codec,
        StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return REGISTRATE.registryEntry(DATA_COMPONENT_TYPES, name, () -> DataComponentType.<T>builder()
            .persistent(codec)
            .networkSynchronized(streamCodec)
            .build());
    }

    private static <T> IEntry<DataComponentType<T>> component(String name, Codec<T> codec) {
        return REGISTRATE.registryEntry(DATA_COMPONENT_TYPES, name, () -> DataComponentType.<T>builder()
            .persistent(codec)
            .build());
    }

    private static <T> IEntry<AttachmentType<T>> attachment(String name, Supplier<AttachmentType<T>> type) {
        return REGISTRATE.registryEntry(ATTACHMENT_TYPES, name, type);
    }

    public static void init() {}
}
