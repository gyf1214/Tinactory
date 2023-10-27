package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;
import org.shsts.tinactory.content.recipe.NullContainer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchContainer extends NullContainer implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    protected static class CraftingStack extends CraftingContainer {
        @SuppressWarnings("ConstantConditions")
        public CraftingStack(int width, int height) {
            super(null, width, height);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return ContainerHelper.removeItem(this.items, index, count);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            this.items.set(index, stack);
        }
    }

    protected static class ToolItemHandler extends ItemStackHandler {
        public ToolItemHandler(int size) {
            super(size);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(AllTags.item(AllTags.TOOL));
        }
    }

    protected final BlockEntity blockEntity;
    protected final CraftingStack craftingStack;
    protected final WrapperItemHandler craftingView;
    protected final ToolItemHandler toolStorage;
    protected final WrapperItemHandler output;
    protected final WrapperItemHandler itemView;

    public WorkbenchContainer(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.craftingStack = new CraftingStack(3, 3);
        this.craftingView = new WrapperItemHandler(this.craftingStack);
        this.craftingView.addListener($ -> this.onUpdateCrafting());

        this.output = new WrapperItemHandler(1);
        this.output.allowInput = false;

        this.toolStorage = new ToolItemHandler(9);
        this.itemView = new WrapperItemHandler(
                new CombinedInvWrapper(this.craftingView, this.output, this.toolStorage));
        this.itemView.addListener($ -> this.onUpdate());
    }

    protected void onUpdate() {
        this.blockEntity.setChanged();
    }

    protected void onUpdateCrafting() {
        var level = this.blockEntity.getLevel();
        if (level != null && !level.isClientSide) {
            var recipe = level.getRecipeManager()
                    .getRecipeFor(AllRecipes.TOOL_RECIPE_TYPE.getProperType(), this, level);
            if (recipe.isEmpty()) {
                this.output.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                this.output.setStackInSlot(0, recipe.get().assemble(this));
            }
            this.onUpdate();
        }
    }

    public CraftingContainer getCraftingContainer() {
        return this.craftingStack;
    }

    public IItemHandlerModifiable getToolStorage() {
        return this.toolStorage;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.itemView).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return ItemHelper.serializeItemHandler(this.itemView);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(this.itemView, tag);
    }
}
