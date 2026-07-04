package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IStackAdapter;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
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
        return new ItemStack(Holder.direct(typed.item()), Math.toIntExact(amount), typed.components());
    }

    @Override
    public IRenderDescriptor display(ItemStack stack) {
        return new ItemRenderDescriptor(stack);
    }

    @Override
    public Component name(ItemStack stack) {
        return stack.getHoverName();
    }

    @Override
    public Optional<List<Component>> tooltip(ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(ClientUtil.itemTooltip(stack));
    }

    private record ItemKey(Item item, DataComponentPatch components) implements IStackKey {
        private static ItemKey of(ItemStack stack) {
            return new ItemKey(stack.getItem(), stack.getComponentsPatch());
        }

        private ResourceLocation id() {
            return BuiltInRegistries.ITEM.getKey(item);
        }

        private String componentsString() {
            return components.toString();
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public IStackAdapter<?> adapter() {
            return StackHelper.ITEM_ADAPTER;
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
            return componentsString().compareTo(typed.componentsString());
        }
    }

    public static final MapCodec<? extends IStackKey> KEY_CODEC =
        RecordCodecBuilder.<ItemKey>mapCodec(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ItemKey::item),
            DataComponentPatch.CODEC.fieldOf("components").forGetter(ItemKey::components)
        ).apply(instance, ItemKey::new));
}
