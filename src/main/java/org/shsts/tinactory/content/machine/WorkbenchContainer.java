package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.logistics.ItemHelper;
import org.shsts.tinactory.content.logistics.NullContainer;
import org.shsts.tinactory.content.logistics.WrapperItemHandler;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static org.shsts.tinactory.gui.WorkbenchMenu.OUTPUT_SLOT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchContainer implements NullContainer, ICapabilityProvider, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

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
    @Nullable
    protected Recipe<?> currentRecipe = null;

    public WorkbenchContainer(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.craftingStack = new CraftingStack(3, 3);
        this.craftingView = new WrapperItemHandler(this.craftingStack);

        this.output = new WrapperItemHandler(1);
        this.output.allowInput = false;

        this.toolStorage = new ToolItemHandler(9);

        this.itemView = new WrapperItemHandler(
                new CombinedInvWrapper(this.output, this.toolStorage, this.craftingView));
        this.itemView.onUpdate(this::onUpdate);
        this.itemView.onTake(this::onCraft);
    }

    @FunctionalInterface
    protected interface RecipeFunction<C extends Container, R extends Recipe<C>, V> {
        V apply(R recipe, C container);
    }

    @SuppressWarnings("unchecked")
    protected <C extends Container, R extends Recipe<C>, V>
    V applyRecipeFunc(RecipeFunction<C, R, V> func) {
        if (this.currentRecipe instanceof CraftingRecipe) {
            return func.apply((R) this.currentRecipe, (C) this.craftingStack);
        } else if (this.currentRecipe instanceof ToolRecipe) {
            return func.apply((R) this.currentRecipe, (C) this);
        } else {
            throw new IllegalStateException();
        }
    }


    protected void onUpdate() {
        var world = this.blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }

        LOGGER.debug("{} check recipe", this);
        var recipeManager = world.getRecipeManager();
        var toolRecipe = recipeManager.getRecipeFor(AllRecipes.TOOL_RECIPE_TYPE.getProperType(), this, world);
        if (toolRecipe.isEmpty()) {
            var shapedRecipe = recipeManager.getRecipeFor(RecipeType.CRAFTING, this.craftingStack, world);
            if (shapedRecipe.isEmpty()) {
                this.currentRecipe = null;
            } else {
                this.currentRecipe = shapedRecipe.get();
            }
        } else {
            this.currentRecipe = toolRecipe.get();
        }
        if (this.currentRecipe != null) {
            this.output.setStackInSlot(0, this.applyRecipeFunc(Recipe::assemble));
        } else {
            this.output.setStackInSlot(0, ItemStack.EMPTY);
        }
        this.blockEntity.setChanged();
    }

    protected void onCraft(int slot, Player player, ItemStack stack) {
        if (slot != OUTPUT_SLOT || stack.isEmpty() || this.currentRecipe == null) {
            return;
        }

        var amount = stack.getCount();
        LOGGER.debug("{} on craft {}", this, stack);

        // vanilla logic of crafting triggers
        stack.onCraftedBy(player.level, player, amount);
        ForgeEventFactory.firePlayerCraftingEvent(player, stack, this.craftingStack);
        if (!this.currentRecipe.isSpecial()) {
            player.awardRecipes(List.of(this.currentRecipe));
        }
        ForgeHooks.setCraftingPlayer(player);
        var remaining = this.applyRecipeFunc(Recipe::getRemainingItems);
        ForgeHooks.setCraftingPlayer(null);

        for (var i = 0; i < remaining.size(); i++) {
            // vanilla logic of decreasing material and set remaining items
            var slotItem = this.craftingView.getStackInSlot(i);
            var remainingItem = remaining.get(i);
            if (!slotItem.isEmpty()) {
                this.craftingView.extractItem(i, 1, false);
            }
            if (!remainingItem.isEmpty()) {
                var remainingItem1 = this.craftingView.insertItem(i, remainingItem, false);
                player.drop(remainingItem1, false);
            }
        }

        if (this.currentRecipe instanceof ToolRecipe toolRecipe) {
            // damage tool recipe
            toolRecipe.doDamage(this.toolStorage);
        }

        this.onUpdate();
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
