package org.shsts.tinactory.content.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.core.machine.MachineConfigType;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record StorageDetectorConfig(@Nullable IStackKey key, long amount) {
    public static final Codec<StorageDetectorConfig> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            StackHelper.KEY_CODEC.optionalFieldOf("key").forGetter($ -> Optional.ofNullable($.key)),
            Codec.LONG.fieldOf("amount").forGetter(StorageDetectorConfig::amount)
        ).apply(instance, (key, amount) -> new StorageDetectorConfig(key.orElse(null), amount)));

    public static IMachineConfigType<StorageDetectorConfig> configType() {
        return new MachineConfigType<>(CODEC, null) {
            @Override
            public Optional<StorageDetectorConfig> fixLegacyConfig(HolderLookup.Provider provider,
                Map<String, Tag> unknownTags) {
                var amount = unknownTags.get("targetAmount") instanceof NumericTag amountTag ?
                    amountTag.getAsLong() : 0L;
                IStackKey key = null;
                if (unknownTags.get("targetItem") instanceof CompoundTag itemTag) {
                    var item = ItemStack.parseOptional(provider, itemTag);
                    if (!item.isEmpty()) {
                        key = StackHelper.ITEM_ADAPTER.keyOf(item);
                    }
                } else if (unknownTags.get("targetFluid") instanceof CompoundTag fluidTag) {
                    var fluid = FluidStack.parseOptional(provider, fluidTag);
                    if (!fluid.isEmpty()) {
                        key = StackHelper.FLUID_ADAPTER.keyOf(fluid);
                    }
                }
                if (key != null) {
                    unknownTags.remove("targetAmount");
                    unknownTags.remove("targetItem");
                    unknownTags.remove("targetFluid");
                    return Optional.of(new StorageDetectorConfig(key, amount));
                }
                return Optional.empty();
            }
        };
    }
}
