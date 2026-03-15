package org.shsts.tinactory.integration.logistics;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemPortAdapter implements IStackAdapter<ItemStack> {
    public static final ItemPortAdapter INSTANCE = new ItemPortAdapter();

    private static final Codec<ItemKeyData> KEY_DATA_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("id").forGetter(ItemKeyData::id),
        Codec.STRING.fieldOf("nbt").forGetter(ItemKeyData::nbt)
    ).apply(instance, ItemKeyData::new));

    private static final Codec<IIngredientKey> KEY_CODEC = KEY_DATA_CODEC.comapFlatMap(
        ItemPortAdapter::decodeData,
        ItemPortAdapter::encodeData
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
        return new ItemKey(stack);
    }

    @Override
    public ItemStack stackOf(IIngredientKey key, long amount) {
        var typed = asItemKey(key);
        return StackHelper.copyWithCount(typed.stack(), Math.toIntExact(amount));
    }

    public static Codec<IIngredientKey> keyCodec() {
        return KEY_CODEC;
    }

    private static DataResult<IIngredientKey> decodeData(ItemKeyData data) {
        return stackFromResult(data.id(), data.nbt()).map(ItemKey::new);
    }

    private static ItemKeyData encodeData(IIngredientKey key) {
        var typed = asItemKey(key);
        return new ItemKeyData(typed.id(), typed.nbt());
    }

    private static ItemKey asItemKey(IIngredientKey key) {
        if (key instanceof ItemKey typed) {
            return typed;
        }
        throw new IllegalArgumentException("Expected item key but got: " + key.getClass().getName());
    }

    private static final class ItemKey implements IIngredientKey {
        private final String id;
        private final String nbt;
        private final ItemStack stack;

        private ItemKey(ItemStack stack) {
            this(itemId(stack), nbtString(stack), StackHelper.copyWithCount(stack, 1));
        }

        private ItemKey(String id, String nbt) {
            this(id, nbt, stackFrom(id, nbt));
        }

        private ItemKey(String id, String nbt, ItemStack stack) {
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

        private ItemStack stack() {
            return stack;
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
            return nbt.compareTo(typed.nbt);
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof ItemKey key && id.equals(key.id) && nbt.equals(key.nbt));
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, nbt);
        }

        @Override
        public String toString() {
            return nbt.isEmpty() ? id : id + nbt;
        }
    }

    private record ItemKeyData(String id, String nbt) {}

    private static String itemId(ItemStack stack) {
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            throw new IllegalArgumentException("Item stack has no registry id");
        }
        return key.toString();
    }

    private static String nbtString(ItemStack stack) {
        return stack.hasTag() ? Objects.requireNonNull(stack.getTag()).toString() : "";
    }

    private static ItemStack stackFrom(String id, String nbt) {
        return stackFromResult(id, nbt).result().orElseThrow(() ->
            new IllegalArgumentException("Invalid item key: id=" + id + ", nbt=" + nbt));
    }

    private static DataResult<ItemStack> stackFromResult(String id, String nbt) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            return DataResult.error("Invalid item id: " + id);
        }
        var item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null) {
            return DataResult.error("Unknown item id: " + id);
        }
        var stack = new ItemStack(item, 1);
        if (!nbt.isEmpty()) {
            try {
                stack.setTag(TagParser.parseTag(nbt));
            } catch (CommandSyntaxException ex) {
                return DataResult.error("Invalid item nbt: " + nbt + " (" + ex.getMessage() + ")");
            }
        }
        return DataResult.success(stack);
    }
}
