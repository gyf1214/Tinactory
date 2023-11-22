package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.ItemHandlerCollection;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingStackContainer extends ProcessingContainer implements ICapabilityProvider {

    protected record PortInfo(int slots, Layout.SlotType type) {}

    protected final IItemHandlerModifiable combinedStack;
    protected final List<IItemCollection> ports;
    protected final List<IItemCollection> internalPorts;

    public ProcessingStackContainer(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe<?>> recipeType,
                                    Collection<PortInfo> ports) {
        super(blockEntity, recipeType);
        this.ports = new ArrayList<>(ports.size());
        this.internalPorts = new ArrayList<>(ports.size());
        var views = new ArrayList<WrapperItemHandler>(ports.size());
        for (var port : ports) {
            switch (port.type()) {
                case ITEM_INPUT -> {
                    var view = new WrapperItemHandler(port.slots);
                    view.onUpdate(this::onInputUpdate);
                    views.add(view);

                    var collection = new ItemHandlerCollection(view);
                    this.internalPorts.add(collection);
                    this.ports.add(collection);
                }
                case ITEM_OUTPUT -> {
                    var inner = new WrapperItemHandler(port.slots);
                    inner.onUpdate(this::onOutputUpdate);

                    var view = new WrapperItemHandler(inner);
                    view.allowInput = false;
                    views.add(view);

                    this.internalPorts.add(new ItemHandlerCollection(inner));
                    this.ports.add(new ItemHandlerCollection(view));
                }
            }
        }
        this.combinedStack = new CombinedInvWrapper(views.toArray(IItemHandlerModifiable[]::new));
    }

    @Override
    public IItemCollection getPort(int port, boolean internal) {
        return internal ? this.internalPorts.get(port) : this.ports.get(port);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.combinedStack).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.put("stack", ItemHelper.serializeItemHandler(this.combinedStack));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        ItemHelper.deserializeItemHandler(this.combinedStack, tag.getCompound("stack"));
    }

    public static class Builder implements Function<BlockEntity, ICapabilityProvider> {
        @Nullable
        private RecipeType<? extends ProcessingRecipe<?>> recipeType = null;
        private final List<PortInfo> ports = new ArrayList<>();

        public Builder recipeType(RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType) {
            this.recipeType = recipeType.get();
            return this;
        }

        public Builder layout(Layout layout) {
            Map<Integer, PortInfo> map = new HashMap<>();
            for (var slot : layout.slots) {
                if (slot.type() == Layout.SlotType.NONE) {
                    continue;
                }
                map.merge(slot.port(), new PortInfo(1, slot.type()),
                        ($, v) -> new PortInfo(v.slots + 1, slot.type()));
            }
            this.ports.addAll(map.values());
            return this;
        }

        @Override
        public ICapabilityProvider apply(BlockEntity be) {
            assert this.recipeType != null;
            return new ProcessingStackContainer(be, this.recipeType, this.ports);
        }
    }
}
