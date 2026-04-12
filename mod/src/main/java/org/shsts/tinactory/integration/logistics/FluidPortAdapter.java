package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
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
            ForgeRegistries.FLUIDS.getCodec().fieldOf("id").forGetter(FluidKey::fluid),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FluidKey::nbtOptional)
        ).apply(instance, FluidKey::new));

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
        var stack = new FluidStack(typed.fluid(), Math.toIntExact(amount));
        if (typed.nbt() != null) {
            stack.setTag(typed.nbtOptional().orElseThrow());
        }
        return stack;
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

    private static final class FluidKey implements IIngredientKey {
        private final Fluid fluid;
        @Nullable
        private final CompoundTag nbt;

        private FluidKey(Fluid fluid, Optional<CompoundTag> nbt) {
            this(fluid, nbt.orElse(null));
        }

        private FluidKey(Fluid fluid, @Nullable CompoundTag nbt) {
            this.fluid = fluid;
            this.nbt = normalizeNbt(nbt);
        }

        private static FluidKey of(FluidStack stack) {
            return new FluidKey(stack.getFluid(), stack.getTag());
        }

        private Fluid fluid() {
            return fluid;
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
            var byId = fluidId(fluid).compareTo(fluidId(typed.fluid));
            if (byId != 0) {
                return byId;
            }
            return nbtString(nbt).compareTo(nbtString(typed.nbt));
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof FluidKey key &&
                fluid.equals(key.fluid) &&
                Objects.equals(nbt, key.nbt));
        }

        @Override
        public int hashCode() {
            return Objects.hash(fluid, nbt);
        }

        @Override
        public String toString() {
            var id = fluidId(fluid).toString();
            return nbt == null ? id : id + nbt;
        }
    }

    private static ResourceLocation fluidId(Fluid fluid) {
        var key = fluid.getRegistryName();
        if (key == null) {
            throw new IllegalArgumentException("Fluid has no registry id");
        }
        return key;
    }

    @Nullable
    private static CompoundTag copyNbt(@Nullable CompoundTag nbt) {
        return nbt != null ? nbt.copy() : null;
    }

    private static String nbtString(@Nullable CompoundTag nbt) {
        return nbt != null ? nbt.toString() : "";
    }

    @Nullable
    private static CompoundTag normalizeNbt(@Nullable CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return null;
        }
        return nbt.copy();
    }
}
