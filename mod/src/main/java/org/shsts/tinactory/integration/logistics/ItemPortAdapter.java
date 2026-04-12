package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemPortAdapter implements IStackAdapter<ItemStack> {
    public static final ItemPortAdapter INSTANCE = new ItemPortAdapter();

    private static final Codec<? extends IIngredientKey> KEY_CODEC =
        RecordCodecBuilder.<ItemKey>create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ItemKey::id),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemKey::nbtOptional)
        ).apply(instance, ItemKey::new)).comapFlatMap(
            ItemPortAdapter::validateKey,
            ItemPortAdapter::asItemKey
        );

    private ItemPortAdapter() {}

    @Override
    public ItemStack empty() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public ItemStack copy(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public int amount(ItemStack stack) {
        return stack.getCount();
    }

    @Override
    public ItemStack withAmount(ItemStack stack, int amount) {
        return StackHelper.copyWithCount(stack, amount);
    }

    @Override
    public boolean canStack(ItemStack left, ItemStack right) {
        return StackHelper.canItemsStack(left, right);
    }

    @Override
    public IIngredientKey keyOf(ItemStack stack) {
        return ItemKey.of(stack);
    }

    @Override
    public ItemStack stackOf(IIngredientKey key, long amount) {
        var typed = asItemKey(key);
        return stackFrom(typed.id(), typed.nbt(), Math.toIntExact(amount));
    }

    public static Codec<? extends IIngredientKey> keyCodec() {
        return KEY_CODEC;
    }

    private static ItemKey asItemKey(IIngredientKey key) {
        if (key instanceof ItemKey typed) {
            return typed;
        }
        throw new IllegalArgumentException("Expected item key but got: " + key.getClass().getName());
    }

    private static DataResult<ItemKey> validateKey(ItemKey key) {
        var location = ResourceLocation.tryParse(key.id());
        if (location == null) {
            return DataResult.error("Invalid item id: " + key.id());
        }
        if (!ForgeRegistries.ITEMS.containsKey(location)) {
            return DataResult.error("Unknown item id: " + key.id());
        }
        return DataResult.success(key);
    }

    private static final class ItemKey implements IIngredientKey {
        private final String id;
        @Nullable
        private final CompoundTag nbt;

        private ItemKey(String id, Optional<CompoundTag> nbt) {
            this(id, nbt.orElse(null));
        }

        private ItemKey(String id, @Nullable CompoundTag nbt) {
            this.id = id;
            this.nbt = normalizeNbt(nbt);
        }

        private static ItemKey of(ItemStack stack) {
            return new ItemKey(itemId(stack), stack.getTag());
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
            return PortType.ITEM;
        }

        @Override
        public int compareTo(IIngredientKey other) {
            if (type() != other.type()) {
                return Integer.compare(type().ordinal(), other.type().ordinal());
            }
            if (!(other instanceof ItemKey typed)) {
                throw new IllegalArgumentException("Expected item key for ITEM type comparison");
            }
            var byId = id.compareTo(typed.id);
            if (byId != 0) {
                return byId;
            }
            return nbtString(nbt).compareTo(nbtString(typed.nbt));
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof ItemKey key && id.equals(key.id) && Objects.equals(nbt, key.nbt));
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

    private static String itemId(ItemStack stack) {
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            throw new IllegalArgumentException("Item stack has no registry id");
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

    private static ItemStack stackFrom(String id, @Nullable CompoundTag nbt, int amount) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid item id: " + id);
        }
        var item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null) {
            throw new IllegalArgumentException("Unknown item id: " + id);
        }
        var stack = new ItemStack(item, amount);
        if (nbt != null) {
            stack.setTag(nbt.copy());
        }
        return stack;
    }
}
