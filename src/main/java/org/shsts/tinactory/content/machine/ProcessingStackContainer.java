package org.shsts.tinactory.content.machine;

import com.mojang.datafixers.util.Either;
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
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.logistics.CombinedFluidTank;
import org.shsts.tinactory.content.logistics.IFluidCollection;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.content.logistics.ItemHandlerCollection;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.logistics.WrapperFluidTank;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.registrate.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingStackContainer extends ProcessingContainer implements ICapabilityProvider {
    // TODO: Config
    private static final int DEFAULT_FLUID_CAPACITY = 16000;

    protected record PortInfo(int slots, Layout.SlotType type) {}

    protected final IItemHandlerModifiable combinedItems;
    protected final CombinedFluidTank combinedFluids;
    protected final List<Either<IItemCollection, IFluidCollection>> ports;
    protected final List<Either<IItemCollection, IFluidCollection>> internalPorts;

    public ProcessingStackContainer(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe<?>> recipeType,
                                    Collection<PortInfo> ports) {
        super(blockEntity, recipeType);
        this.ports = new ArrayList<>(ports.size());
        this.internalPorts = new ArrayList<>(ports.size());
        var items = new ArrayList<WrapperItemHandler>(ports.size());
        var fluids = new ArrayList<WrapperFluidTank>();
        for (var port : ports) {
            if (ports.size() == 0) {
                this.internalPorts.add(Either.left(ItemHandlerCollection.EMPTY));
                this.ports.add(Either.left(ItemHandlerCollection.EMPTY));
                continue;
            }
            switch (port.type()) {
                case ITEM_INPUT -> {
                    var view = new WrapperItemHandler(port.slots);
                    view.onUpdate(this::onInputUpdate);
                    items.add(view);

                    var collection = new ItemHandlerCollection(view);
                    this.internalPorts.add(Either.left(collection));
                    this.ports.add(Either.left(collection));
                }
                case ITEM_OUTPUT -> {
                    var inner = new WrapperItemHandler(port.slots);
                    inner.onUpdate(this::onOutputUpdate);

                    var view = new WrapperItemHandler(inner);
                    view.allowInput = false;
                    items.add(view);

                    this.internalPorts.add(Either.left(new ItemHandlerCollection(inner)));
                    this.ports.add(Either.left(new ItemHandlerCollection(view)));
                }
                case FLUID_INPUT -> {
                    var views = new WrapperFluidTank[port.slots];
                    for (var i = 0; i < port.slots; i++) {
                        var view = new WrapperFluidTank(DEFAULT_FLUID_CAPACITY);
                        view.onUpdate(this::onInputUpdate);

                        views[i] = view;
                        fluids.add(view);
                    }

                    var collection = new CombinedFluidTank(views);
                    this.internalPorts.add(Either.right(collection));
                    this.ports.add(Either.right(collection));
                }
                case FLUID_OUTPUT -> {
                    var inners = new WrapperFluidTank[port.slots];
                    var views = new WrapperFluidTank[port.slots];

                    for (var i = 0; i < port.slots; i++) {
                        var inner = new WrapperFluidTank(DEFAULT_FLUID_CAPACITY);
                        inner.onUpdate(this::onInputUpdate);
                        inners[i] = inner;

                        var view = new WrapperFluidTank(inner);
                        view.allowInput = false;
                        views[i] = view;
                        fluids.add(view);
                    }

                    this.internalPorts.add(Either.right(new CombinedFluidTank(inners)));
                    this.ports.add(Either.right(new CombinedFluidTank(views)));
                }
            }
        }
        this.combinedItems = new CombinedInvWrapper(items.toArray(IItemHandlerModifiable[]::new));
        this.combinedFluids = new CombinedFluidTank(fluids.toArray(WrapperFluidTank[]::new));
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < this.ports.size() &&
                !this.ports.get(port).left().map(c -> c == ItemHandlerCollection.EMPTY).orElse(false);
    }

    @Override
    public Either<IItemCollection, IFluidCollection> getPort(int port, boolean internal) {
        if (!this.hasPort(port)) {
            return Either.left(ItemHandlerCollection.EMPTY);
        }
        return internal ? this.internalPorts.get(port) : this.ports.get(port);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.combinedItems).cast();
        } else if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return LazyOptional.of(() -> this.combinedFluids).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.put("stack", ItemHelper.serializeItemHandler(this.combinedItems));
        tag.put("fluid", this.combinedFluids.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        ItemHelper.deserializeItemHandler(this.combinedItems, tag.getCompound("stack"));
        this.combinedFluids.deserializeNBT(tag.getCompound("fluid"));
    }

    public static class Builder implements Function<BlockEntity, ICapabilityProvider> {
        @Nullable
        private RecipeType<? extends ProcessingRecipe<?>> recipeType = null;
        private final List<PortInfo> ports = new ArrayList<>();

        public Builder recipeType(RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType) {
            this.recipeType = recipeType.get();
            return this;
        }

        public Builder layout(Layout layout, Voltage voltage) {
            this.ports.clear();
            var slots = layout.getStackSlots(voltage);
            if (slots.isEmpty()) {
                return this;
            }
            var portCount = 1 + slots.stream().mapToInt(Layout.SlotInfo::port).max().getAsInt();
            var ports = new ArrayList<>(Collections.nCopies(portCount, new PortInfo(0, Layout.SlotType.NONE)));
            for (var slot : slots) {
                var info = ports.get(slot.port());
                ports.set(slot.port(), new PortInfo(info.slots + 1, slot.type()));
            }
            this.ports.addAll(ports);
            return this;
        }

        @Override
        public ICapabilityProvider apply(BlockEntity be) {
            assert this.recipeType != null;
            return new ProcessingStackContainer(be, this.recipeType, this.ports);
        }
    }
}
