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
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Workbench extends CapabilityProvider implements INBTSerializable<CompoundTag>, IWorkbench {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static class CraftingStack extends CraftingContainer {
        @SuppressWarnings("ConstantConditions")
        public CraftingStack(int width, int height) {
            super(null, width, height);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            return ContainerHelper.removeItem(items, index, count);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            items.set(index, stack);
        }
    }

    private static class ToolItemHandler extends ItemStackHandler {
        public ToolItemHandler(int size) {
            super(size);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(AllTags.TOOL);
        }
    }

    private boolean initialized = false;
    private final BlockEntity blockEntity;
    private final CraftingStack craftingStack;
    private final WrapperItemHandler craftingView;
    private final ToolItemHandler toolStorage;
    private ItemStack output;
    private final WrapperItemHandler itemView;
    @Nullable
    private Recipe<?> currentRecipe = null;

    private final LazyOptional<?> itemHandlerCap;

    public Workbench(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.craftingStack = new CraftingStack(3, 3);
        this.craftingView = new WrapperItemHandler(craftingStack);

        this.output = ItemStack.EMPTY;

        this.toolStorage = new ToolItemHandler(9);

        this.itemView = new WrapperItemHandler(
                new CombinedInvWrapper(toolStorage, craftingView));
        this.itemView.onUpdate(this::onUpdate);

        this.itemHandlerCap = LazyOptional.of(() -> itemView);
    }

    @FunctionalInterface
    private interface RecipeFunction<C extends Container, R extends Recipe<C>, V> {
        V apply(R recipe, C container);
    }

    @SuppressWarnings("unchecked")
    private <C extends Container, R extends Recipe<C>, V>
    V applyRecipeFunc(RecipeFunction<C, R, V> func) {
        if (currentRecipe instanceof CraftingRecipe) {
            return func.apply((R) currentRecipe, (C) craftingStack);
        } else if (currentRecipe instanceof ToolRecipe) {
            return func.apply((R) currentRecipe, (C) new SmartRecipe.ContainerWrapper<>(this));
        } else {
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("rawtypes")
    private void onUpdate() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }

        var recipeManager = world.getRecipeManager();

        currentRecipe = SmartRecipe.getRecipeFor(AllRecipes.TOOL.get(), this, world)
                .map($ -> (Recipe) $)
                .or(() -> recipeManager.getRecipeFor(RecipeType.CRAFTING, craftingStack, world))
                .orElse(null);

        if (currentRecipe != null) {
            output = applyRecipeFunc(Recipe::assemble);
        } else {
            output = ItemStack.EMPTY;
        }
        blockEntity.setChanged();
    }

    @Override
    public ItemStack getResult() {
        if (!initialized) {
            onUpdate();
            initialized = true;
        }
        return output;
    }

    @Override
    public void setResult(ItemStack stack) {
        var world = blockEntity.getLevel();
        if (world == null || !world.isClientSide) {
            return;
        }
        output = stack;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (stack.isEmpty() || currentRecipe == null) {
            return;
        }

        var amount = stack.getCount();
        LOGGER.debug("{} on craft {}", this, stack);

        // vanilla logic of crafting triggers
        stack.onCraftedBy(player.level, player, amount);
        ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftingStack);
        if (!currentRecipe.isSpecial()) {
            player.awardRecipes(List.of(currentRecipe));
        }
        ForgeHooks.setCraftingPlayer(player);
        var remaining = applyRecipeFunc(Recipe::getRemainingItems);
        ForgeHooks.setCraftingPlayer(null);

        for (var i = 0; i < remaining.size(); i++) {
            // vanilla logic of decreasing material and set remaining items
            var slotItem = craftingView.getStackInSlot(i);
            var remainingItem = remaining.get(i);
            if (!slotItem.isEmpty()) {
                craftingView.extractItem(i, 1, false);
            }
            if (!remainingItem.isEmpty()) {
                var remainingItem1 = craftingView.insertItem(i, remainingItem, false);
                player.drop(remainingItem1, false);
            }
        }

        if (currentRecipe instanceof ToolRecipe toolRecipe) {
            // damage tool recipe
            toolRecipe.doDamage(toolStorage);
        }

        onUpdate();
    }

    public CraftingContainer getCraftingContainer() {
        return craftingStack;
    }

    public IItemHandlerModifiable getToolStorage() {
        return toolStorage;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        } else if (cap == AllCapabilities.WORKBENCH.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return ItemHelper.serializeItemHandler(itemView);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(itemView, tag);
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "primitive/workbench_container", Workbench::new);
    }
}
