package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
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
            ForgeRegistries.ITEMS.getCodec().fieldOf("id").forGetter(ItemKey::item),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemKey::nbtOptional)
        ).apply(instance, ItemKey::new));

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
        var stack = new ItemStack(typed.item(), Math.toIntExact(amount));
        if (typed.nbt() != null) {
            stack.setTag(typed.nbtOptional().orElseThrow());
        }
        return stack;
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

    private static final class ItemKey implements IIngredientKey {
        private final Item item;
        @Nullable
        private final CompoundTag nbt;

        private ItemKey(Item item, Optional<CompoundTag> nbt) {
            this(item, nbt.orElse(null));
        }

        private ItemKey(Item item, @Nullable CompoundTag nbt) {
            this.item = item;
            this.nbt = normalizeNbt(nbt);
        }

        private static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getTag());
        }

        private Item item() {
            return item;
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
            var byId = itemId(item).compareTo(itemId(typed.item));
            if (byId != 0) {
                return byId;
            }
            return nbtString(nbt).compareTo(nbtString(typed.nbt));
        }

        @Override
        public boolean equals(Object other) {
            return this == other ||
                (other instanceof ItemKey key && item.equals(key.item) && Objects.equals(nbt, key.nbt));
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, nbt);
        }

        @Override
        public String toString() {
            var id = itemId(item).toString();
            return nbt == null ? id : id + nbt;
        }
    }

    private static ResourceLocation itemId(Item item) {
        var key = item.getRegistryName();
        if (key == null) {
            throw new IllegalArgumentException("Item has no registry id");
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
