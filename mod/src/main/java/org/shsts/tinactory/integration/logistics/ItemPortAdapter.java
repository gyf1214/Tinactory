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
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.IStackKey;

import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemPortAdapter implements IStackAdapter<ItemStack> {
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
    public IStackKey keyOf(ItemStack stack) {
        return ItemKey.of(stack);
    }

    @Override
    public ItemStack stackOf(IStackKey key, long amount) {
        var typed = (ItemKey) key;
        var stack = new ItemStack(typed.item(), Math.toIntExact(amount));
        if (typed.nbt() != null) {
            stack.setTag(typed.nbt().copy());
        }
        return stack;
    }

    private static final class ItemKey implements IStackKey {
        private final Item item;
        @Nullable
        private final CompoundTag nbt;

        private ItemKey(Item item, Optional<CompoundTag> nbt) {
            this(item, nbt.orElse(null));
        }

        private ItemKey(Item item, @Nullable CompoundTag nbt) {
            this.item = item;
            this.nbt = nbt == null || nbt.isEmpty() ? null : nbt;
        }

        private static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getTag());
        }

        private Item item() {
            return item;
        }

        private ResourceLocation id() {
            var id = item.getRegistryName();
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
            return PortType.ITEM;
        }

        @Override
        public int compareTo(IStackKey other) {
            if (type() != other.type()) {
                return Integer.compare(type().ordinal(), other.type().ordinal());
            }
            if (!(other instanceof ItemKey typed)) {
                throw new IllegalArgumentException("Expected item key for ITEM type comparison");
            }
            var byId = id().compareTo(typed.id());
            if (byId != 0) {
                return byId;
            }
            return nbtString().compareTo(typed.nbtString());
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
            var id = id().toString();
            return nbt == null ? id : id + nbt;
        }
    }

    public static final Codec<? extends IStackKey> KEY_CODEC =
        RecordCodecBuilder.<ItemKey>create(instance -> instance.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("id").forGetter(ItemKey::item),
            CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(ItemKey::nbtOptional)
        ).apply(instance, ItemKey::new));
}
