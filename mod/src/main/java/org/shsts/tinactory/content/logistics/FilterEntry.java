package org.shsts.tinactory.content.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.compat.ftbfilter.ItemFilterIntegration;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinactory.integration.gui.client.ItemRenderDescriptor;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record FilterEntry(@Nullable IStackKey key, @Nullable TagKey<Item> tag) {
    public static final FilterEntry EMPTY = new FilterEntry(null, null);

    public static final Codec<FilterEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            TagKey.codec(Registries.ITEM).optionalFieldOf("tagFilter").forGetter($ -> Optional.ofNullable($.tag)),
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("itemFilter").forGetter(FilterEntry::item),
            StackHelper.SINGLE_FLUID_CODEC.optionalFieldOf("fluidFilter").forGetter(FilterEntry::fluid)
        ).apply(instance, FilterEntry::fromStacks));

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterEntry> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public FilterEntry {
        assert key == null || tag == null;
    }

    public enum Type {
        NONE(PortType.NONE),
        ITEM(PortType.ITEM),
        TAG(PortType.ITEM),
        FLUID(PortType.FLUID);

        public final PortType portType;

        Type(PortType portType) {
            this.portType = portType;
        }
    }

    public Type type() {
        if (tag != null) {
            return Type.TAG;
        } else if (key != null) {
            return switch (key.type()) {
                case ITEM -> Type.ITEM;
                case FLUID -> Type.FLUID;
                default -> Type.NONE;
            };
        } else {
            return Type.NONE;
        }
    }

    private Optional<ItemStack> item() {
        return key != null && key.type() == PortType.ITEM ?
            Optional.of(StackHelper.ITEM_ADAPTER.stackOf(key)) : Optional.empty();
    }

    private Optional<FluidStack> fluid() {
        return key != null && key.type() == PortType.FLUID ?
            Optional.of(StackHelper.FLUID_ADAPTER.stackOf(key)) : Optional.empty();
    }

    public static FilterEntry fromTag(TagKey<Item> tag) {
        return new FilterEntry(null, tag);
    }

    public static FilterEntry fromItem(ItemStack stack) {
        return new FilterEntry(StackHelper.ITEM_ADAPTER.keyOf(stack), null);
    }

    public static FilterEntry fromFluid(FluidStack stack) {
        return new FilterEntry(StackHelper.FLUID_ADAPTER.keyOf(stack), null);
    }

    private static FilterEntry fromStacks(Optional<TagKey<Item>> tag,
        Optional<ItemStack> item, Optional<FluidStack> fluid) {
        if (tag.isPresent()) {
            return fromTag(tag.get());
        } else if (item.isPresent()) {
            return fromItem(item.get());
        } else if (fluid.isPresent()) {
            return fromFluid(fluid.get());
        } else {
            return EMPTY;
        }
    }

    public boolean isIdentity(PortType portType) {
        if (key != null && key.type() == portType) {
            return portType == PortType.FLUID || (portType == PortType.ITEM &&
                !ItemFilterIntegration.isFilter(StackHelper.ITEM_ADAPTER.stackOf(key)));
        }
        return false;
    }

    public ItemStack asItem() {
        return item().orElseThrow();
    }

    public boolean testItem(ItemStack stack, HolderLookup.Provider provider) {
        if (tag != null) {
            return stack.is(tag);
        } else if (key != null && key.type() == PortType.ITEM) {
            var filter = StackHelper.ITEM_ADAPTER.stackOf(key);
            if (ItemFilterIntegration.isFilter(filter)) {
                return ItemFilterIntegration.matches(filter, stack, provider);
            } else {
                return StackHelper.canItemsStack(filter, stack);
            }
        }
        return true;
    }

    public FluidStack asFluid() {
        return fluid().orElseThrow();
    }

    public boolean testFluid(FluidStack stack) {
        return key == null || key.type() != PortType.FLUID ||
            FluidStack.isSameFluidSameComponents(asFluid(), stack);
    }

    public IRenderDescriptor display() {
        if (tag != null) {
            return ClientUtil.selectItemFromTag(tag)
                .<IRenderDescriptor>map(ItemRenderDescriptor::new)
                .orElse(EmptyRenderDescriptor.INSTANCE);
        }
        return key != null ? key.display() : EmptyRenderDescriptor.INSTANCE;
    }

    public Optional<List<Component>> tooltip() {
        if (tag != null) {
            return Optional.of(ClientUtil.tagTooltip(tag));
        }
        return key != null ? key.tooltip() : Optional.empty();
    }

    public FilterEntry click(int index, ClickHelper helper, int button, ItemStack carried,
        boolean allowItem, boolean allowFluid, boolean allowTag) {
        if (carried.isEmpty()) {
            var ret = EMPTY;
            if (allowTag && button == 1) {
                if (key != null && key.type() == PortType.ITEM) {
                    if (helper.init(index, StackHelper.ITEM_ADAPTER.stackOf(key))) {
                        ret = helper.next();
                    }
                } else if (tag != null && helper.canClick(index)) {
                    ret = helper.next();
                }
            }
            if (ret.type() == Type.NONE) {
                helper.reset();
            }
            return ret;
        } else {
            helper.reset();
            var fluid = (allowFluid && button == 0) ? StackHelper.getFluidFromItem(carried) :
                FluidStack.EMPTY;
            if (!fluid.isEmpty()) {
                return fromFluid(fluid);
            } else if (allowItem) {
                return fromItem(carried);
            } else {
                return EMPTY;
            }
        }
    }

    public static class ClickHelper {
        private int index = -1;
        private final List<TagKey<Item>> selections = new ArrayList<>();
        private int nextTag = 0;

        public void reset() {
            index = -1;
            selections.clear();
            nextTag = 0;
        }

        public boolean canClick(int index) {
            return this.index == index && !selections.isEmpty();
        }

        public FilterEntry next() {
            assert !selections.isEmpty();
            var tag = selections.get(nextTag);
            nextTag = (nextTag + 1) % selections.size();
            return fromTag(tag);
        }

        public boolean init(int index, ItemStack stack) {
            var tagList = stack.getItemHolder().tags()
                .sorted(Comparator.comparing(TagKey::location, LocHelper.LOC_DISPLAY_ORDER))
                .toList();
            if (tagList.isEmpty()) {
                return false;
            }
            this.index = index;
            selections.clear();
            selections.addAll(tagList);
            nextTag = 0;
            return true;
        }
    }
}
