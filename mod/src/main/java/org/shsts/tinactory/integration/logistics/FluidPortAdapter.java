package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluidPortAdapter implements IStackAdapter<FluidStack> {
    public static final FluidPortAdapter INSTANCE = new FluidPortAdapter();

    private static final Codec<? extends IIngredientKey> KEY_CODEC =
        RecordCodecBuilder.<FluidKey>create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(FluidKey::id),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FluidKey::nbtOptional)
        ).apply(instance, FluidKey::new)).comapFlatMap(
            FluidPortAdapter::validateKey,
            FluidPortAdapter::asFluidKey
        );

    private FluidPortAdapter() {}

    @Override
    public FluidStack empty() {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean isEmpty(FluidStack stack) {
        return stack.isEmpty();
    }

    @Override
    public FluidStack copy(FluidStack stack) {
        return stack.copy();
    }

    @Override
    public int amount(FluidStack stack) {
        return stack.getAmount();
    }

    @Override
    public FluidStack withAmount(FluidStack stack, int amount) {
        return StackHelper.copyWithAmount(stack, amount);
    }

    @Override
    public boolean canStack(FluidStack left, FluidStack right) {
        return left.isFluidEqual(right);
    }

    @Override
    public IIngredientKey keyOf(FluidStack stack) {
        return FluidKey.of(stack);
    }

    @Override
    public FluidStack stackOf(IIngredientKey key, long amount) {
        var typed = asFluidKey(key);
        return stackFrom(typed.id(), typed.nbt(), Math.toIntExact(amount));
    }

    public static Codec<? extends IIngredientKey> keyCodec() {
        return KEY_CODEC;
    }

    private static FluidKey asFluidKey(IIngredientKey key) {
        if (key instanceof FluidKey typed) {
            return typed;
        }
        throw new IllegalArgumentException("Expected fluid key but got: " + key.getClass().getName());
    }

    private static DataResult<FluidKey> validateKey(FluidKey key) {
        var location = ResourceLocation.tryParse(key.id());
        if (location == null) {
            return DataResult.error("Invalid fluid id: " + key.id());
        }
        if (!ForgeRegistries.FLUIDS.containsKey(location)) {
            return DataResult.error("Unknown fluid id: " + key.id());
        }
        return DataResult.success(key);
    }

    private static final class FluidKey implements IIngredientKey {
        private final String id;
        @Nullable
        private final CompoundTag nbt;

        private FluidKey(String id, Optional<CompoundTag> nbt) {
            this(id, nbt.orElse(null));
        }

        private FluidKey(String id, @Nullable CompoundTag nbt) {
            this.id = id;
            this.nbt = normalizeNbt(nbt);
        }

        private static FluidKey of(FluidStack stack) {
            return new FluidKey(fluidId(stack), stack.getTag());
        }

        private String id() {
            return id;
        }

        @Nullable
        private CompoundTag nbt() {
            return nbt;
        }

        private Optional<CompoundTag> nbtOptional() {
            return Optional.ofNullable(copyNbt(nbt));
        }

        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public int compareTo(IIngredientKey other) {
            if (type() != other.type()) {
                return Integer.compare(type().ordinal(), other.type().ordinal());
            }
            if (!(other instanceof FluidKey typed)) {
                throw new IllegalArgumentException("Expected fluid key for FLUID type comparison");
            }
            var byId = id.compareTo(typed.id);
            if (byId != 0) {
                return byId;
            }
            return nbtString(nbt).compareTo(nbtString(typed.nbt));
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof FluidKey key &&
                id.equals(key.id) &&
                Objects.equals(nbt, key.nbt));
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nbt);
        }

        @Override
        public String toString() {
            return nbt == null ? id : id + nbt;
        }
    }

    private static String fluidId(FluidStack stack) {
        var key = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (key == null) {
            throw new IllegalArgumentException("Fluid stack has no registry id");
        }
        return key.toString();
    }

    @Nullable
    private static CompoundTag normalizeNbt(@Nullable CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return null;
        }
        return nbt.copy();
    }

    @Nullable
    private static CompoundTag copyNbt(@Nullable CompoundTag nbt) {
        return nbt != null ? nbt.copy() : null;
    }

    private static String nbtString(@Nullable CompoundTag nbt) {
        return nbt != null ? nbt.toString() : "";
    }

    private static FluidStack stackFrom(String id, @Nullable CompoundTag nbt, int amount) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid fluid id: " + id);
        }
        var fluid = ForgeRegistries.FLUIDS.getValue(location);
        if (fluid == null) {
            throw new IllegalArgumentException("Unknown fluid id: " + id);
        }
        var stack = new FluidStack(fluid, amount);
        if (nbt != null) {
            stack.setTag(nbt.copy());
        }
        return stack;
    }
}
