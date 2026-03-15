package org.shsts.tinactory.integration.logistics;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class FluidPortAdapter implements IStackAdapter<FluidStack> {
    public static final FluidPortAdapter INSTANCE = new FluidPortAdapter();

    private static final Codec<FluidKeyData> KEY_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(FluidKeyData::id),
        Codec.STRING.fieldOf("nbt").forGetter(FluidKeyData::nbt)
    ).apply(instance, FluidKeyData::new));

    private static final Codec<IIngredientKey> KEY_CODEC = KEY_DATA_CODEC.comapFlatMap(
        FluidPortAdapter::decodeData,
        FluidPortAdapter::encodeData
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
        return new FluidKey(stack);
    }

    @Override
    public FluidStack stackOf(IIngredientKey key, long amount) {
        var typed = asFluidKey(key);
        return StackHelper.copyWithAmount(typed.stack(), Math.toIntExact(amount));
    }

    public static Codec<IIngredientKey> keyCodec() {
        return KEY_CODEC;
    }

    private static DataResult<IIngredientKey> decodeData(FluidKeyData data) {
        return stackFromResult(data.id(), data.nbt()).map(FluidKey::new);
    }

    private static FluidKeyData encodeData(IIngredientKey key) {
        var typed = asFluidKey(key);
        return new FluidKeyData(typed.id(), typed.nbt());
    }

    private static FluidKey asFluidKey(IIngredientKey key) {
        if (key instanceof FluidKey typed) {
            return typed;
        }
        throw new IllegalArgumentException("Expected fluid key but got: " + key.getClass().getName());
    }

    private static final class FluidKey implements IIngredientKey {
        private final String id;
        private final String nbt;
        private final FluidStack stack;

        private FluidKey(FluidStack stack) {
            this(fluidId(stack), nbtString(stack), StackHelper.copyWithAmount(stack, 1));
        }

        private FluidKey(String id, String nbt) {
            this(id, nbt, stackFrom(id, nbt));
        }

        private FluidKey(String id, String nbt, FluidStack stack) {
            this.id = id;
            this.nbt = nbt;
            this.stack = stack;
        }

        private String id() {
            return id;
        }

        private String nbt() {
            return nbt;
        }

        private FluidStack stack() {
            return stack;
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
            return nbt.compareTo(typed.nbt);
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof FluidKey key && id.equals(key.id) && nbt.equals(key.nbt));
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nbt);
        }
    }

    private record FluidKeyData(String id, String nbt) {}

    private static String fluidId(FluidStack stack) {
        var key = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (key == null) {
            throw new IllegalArgumentException("Fluid stack has no registry id");
        }
        return key.toString();
    }

    private static String nbtString(FluidStack stack) {
        return stack.hasTag() ? Objects.requireNonNull(stack.getTag()).toString() : "";
    }

    private static FluidStack stackFrom(String id, String nbt) {
        return stackFromResult(id, nbt).result().orElseThrow(() ->
            new IllegalArgumentException("Invalid fluid key: id=" + id + ", nbt=" + nbt));
    }

    private static DataResult<FluidStack> stackFromResult(String id, String nbt) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return DataResult.error("Invalid fluid id: " + id);
        }
        var fluid = ForgeRegistries.FLUIDS.getValue(location);
        if (fluid == null) {
            return DataResult.error("Unknown fluid id: " + id);
        }
        var stack = new FluidStack(fluid, 1);
        if (!nbt.isEmpty()) {
            try {
                stack.setTag(TagParser.parseTag(nbt));
            } catch (CommandSyntaxException ex) {
                return DataResult.error("Invalid fluid nbt: " + nbt + " (" + ex.getMessage() + ")");
            }
        }
        return DataResult.success(stack);
    }
}
