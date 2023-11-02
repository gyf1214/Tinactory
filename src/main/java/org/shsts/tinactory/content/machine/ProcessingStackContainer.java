package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
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
import org.shsts.tinactory.content.logistics.WrapperItemHandler;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingStackContainer extends ProcessingContainer implements ICapabilityProvider {

    public record PortInfo(int slots, boolean output) {}

    protected final IItemHandlerModifiable combinedStack;
    protected final List<IItemCollection> ports;
    protected final List<IItemCollection> internalPorts;

    public ProcessingStackContainer(BlockEntity blockEntity, RecipeType<? extends ProcessingRecipe<?>> recipeType,
                                    PortInfo[] ports) {
        super(blockEntity, recipeType);
        this.ports = new ArrayList<>(ports.length);
        this.internalPorts = new ArrayList<>(ports.length);
        var views = new ArrayList<WrapperItemHandler>(ports.length);
        for (var port : ports) {
            var view = new WrapperItemHandler(port.slots);
            var collection = new ItemHandlerCollection(view);
            if (port.output) {
                view.allowInput = false;
                this.internalPorts.add(new ItemHandlerCollection(view.compose));
            } else {
                view.onUpdate(this::onInputUpdate);
                this.internalPorts.add(collection);
            }
            this.ports.add(collection);
            views.add(view);
        }
        this.combinedStack = new CombinedInvWrapper(views.toArray(WrapperItemHandler[]::new));
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
}
