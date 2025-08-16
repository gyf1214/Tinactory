package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static net.minecraft.world.item.crafting.RecipeType.CRAFTING;
import static org.shsts.tinactory.Tinactory.CORE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Workbench extends CapabilityProvider implements
    IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "primitive/workbench_container";

    private static class CraftingStack extends CraftingContainer {
        private final IItemHandlerModifiable items;

        @SuppressWarnings("ConstantConditions")
        public CraftingStack(IItemHandlerModifiable items, int width, int height) {
            super(null, width, height);
            this.items = items;
        }

        @Override
        public int getContainerSize() {
            return getWidth() * getHeight();
        }

        @Override
        public boolean isEmpty() {
            for (var i = 0; i < items.getSlots(); i++) {
                if (!items.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return items.getStackInSlot(index);
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            var stack = getItem(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            setItem(index, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            var stack = items.getStackInSlot(index);
            return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            items.setStackInSlot(index, stack);
        }

        @Override
        public void clearContent() {
            for (var i = 0; i < items.getSlots(); i++) {
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
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
    private final ItemStackHandler craftingView;
    private final ToolItemHandler toolStorage;
    private ItemStack output;
    private final WrapperItemHandler itemView;
    @Nullable
    private Object currentRecipe = null;

    private final LazyOptional<?> itemHandlerCap;

    private Workbench(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.craftingView = new ItemStackHandler(9);
        this.craftingStack = new CraftingStack(craftingView, 3, 3);

        this.output = ItemStack.EMPTY;

        this.toolStorage = new ToolItemHandler(9);

        this.itemView = new WrapperItemHandler(
            new CombinedInvWrapper(toolStorage, craftingView));
        this.itemView.onUpdate(this::onUpdate);

        this.itemHandlerCap = LazyOptional.of(() -> itemView);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, Workbench::new);
    }

    private void onUpdate() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }

        var recipeManager = CORE.recipeManager(world);
        var vanillaRecipes = world.getRecipeManager();

        currentRecipe = recipeManager.getRecipeFor(TOOL_CRAFTING, this, world)
            .map($ -> (Object) $)
            .or(() -> vanillaRecipes.getRecipeFor(CRAFTING, craftingStack, world))
            .orElse(null);

        if (currentRecipe instanceof ToolRecipe tool) {
            output = tool.assemble();
        } else if (currentRecipe instanceof CraftingRecipe crafting) {
            output = crafting.assemble(craftingStack);
        } else {
            output = ItemStack.EMPTY;
        }
        blockEntity.setChanged();
    }

    public ItemStack getResult() {
        if (!initialized) {
            onUpdate();
            initialized = true;
        }
        return output;
    }

    /**
     * Only called on client for the purpose of syncing.
     */
    public void setResult(ItemStack stack) {
        var world = blockEntity.getLevel();
        if (world == null || !world.isClientSide) {
            return;
        }
        output = stack;
    }

    public void onTake(Player player, ItemStack stack) {
        if (stack.isEmpty() || currentRecipe == null) {
            return;
        }

        var amount = stack.getCount();
        LOGGER.trace("{} on craft {}", this, stack);

        // vanilla logic of crafting triggers
        stack.onCraftedBy(player.level, player, amount);
        ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftingStack);
        if (currentRecipe instanceof CraftingRecipe crafting && !crafting.isSpecial()) {
            player.awardRecipes(List.of(crafting));
        }
        ForgeHooks.setCraftingPlayer(player);
        List<ItemStack> remaining;
        if (currentRecipe instanceof ToolRecipe tool) {
            remaining = tool.getRemainingItems(this);
        } else if (currentRecipe instanceof CraftingRecipe crafting) {
            remaining = crafting.getRemainingItems(craftingStack);
        } else {
            throw new IllegalStateException();
        }
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

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), itemView));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == MENU_ITEM_HANDLER.get()) {
            return itemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return StackHelper.serializeItemHandler(itemView);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        StackHelper.deserializeItemHandler(itemView, tag);
    }

    public static Optional<Workbench> tryGet(BlockEntity be) {
        return tryGetProvider(be, ID, Workbench.class);
    }

    public static Workbench get(BlockEntity be) {
        return getProvider(be, ID, Workbench.class);
    }
}
