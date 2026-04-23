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
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.IStackKey;

import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluidPortAdapter implements IStackAdapter<FluidStack> {
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
    public IStackKey keyOf(FluidStack stack) {
        return FluidKey.of(stack);
    }

    @Override
    public FluidStack stackOf(IStackKey key, long amount) {
        var typed = (FluidKey) key;
        var stack = new FluidStack(typed.fluid(), Math.toIntExact(amount));
        if (typed.nbt() != null) {
            stack.setTag(typed.nbt().copy());
        }
        return stack;
    }

    private static final class FluidKey implements IStackKey {
        private final Fluid fluid;
        @Nullable
        private final CompoundTag nbt;

        private FluidKey(Fluid fluid, Optional<CompoundTag> nbt) {
            this(fluid, nbt.orElse(null));
        }

        private FluidKey(Fluid fluid, @Nullable CompoundTag nbt) {
            this.fluid = fluid;
            this.nbt = nbt == null || nbt.isEmpty() ? null : nbt;
        }

        private static FluidKey of(FluidStack stack) {
            return new FluidKey(stack.getFluid(), stack.getTag());
        }

        private Fluid fluid() {
            return fluid;
        }

        private ResourceLocation id() {
            var id = fluid.getRegistryName();
            assert id != null;
            return id;
        }

        @Nullable
        private CompoundTag nbt() {
            return nbt;
        }

        private String nbtString() {
            return nbt != null ? nbt.toString() : "";
        }

        private Optional<CompoundTag> nbtOptional() {
            return Optional.ofNullable(nbt);
        }

        @Override
        public PortType type() {
            return PortType.FLUID;
        }

        @Override
        public int compareTo(IStackKey other) {
            if (type() != other.type()) {
                return Integer.compare(type().ordinal(), other.type().ordinal());
            }
            if (!(other instanceof FluidKey typed)) {
                throw new IllegalArgumentException("Expected fluid key for FLUID type comparison");
            }
            var byId = id().compareTo(typed.id());
            if (byId != 0) {
                return byId;
            }
            return nbtString().compareTo(typed.nbtString());
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
            var id = id().toString();
            return nbt == null ? id : id + nbt;
        }
    }

    public static final Codec<? extends IStackKey> KEY_CODEC =
        RecordCodecBuilder.<FluidKey>create(instance -> instance.group(
            ForgeRegistries.FLUIDS.getCodec().fieldOf("id").forGetter(FluidKey::fluid),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(FluidKey::nbtOptional)
        ).apply(instance, FluidKey::new));
}
