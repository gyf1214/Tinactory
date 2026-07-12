package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.logistics.WrapperItemHandler;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static net.minecraft.world.item.crafting.RecipeType.CRAFTING;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Workbench extends CapabilityProvider implements
    IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ID = "primitive/workbench_container";

    private record CraftingStack(IItemHandlerModifiable items, int width, int height)
        implements CraftingContainer {
        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public List<ItemStack> getItems() {
            return List.copyOf(getMutableItems());
        }

        public CraftingInput asCraftInput() {
            return CraftingInput.of(width, height, getMutableItems());
        }

        private List<ItemStack> getMutableItems() {
            return IntStream.range(0, getContainerSize())
                .mapToObj(items::getStackInSlot)
                .toList();
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

        @Override
        public void fillStackedContents(StackedContents contents) {
            for (var i = 0; i < getContainerSize(); i++) {
                contents.accountStack(items.getStackInSlot(i));
            }
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static class ToolItemHandler extends ItemStackHandler {
        public ToolItemHandler(int size) {
            super(size);
        }

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

    private Workbench(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;

        this.craftingView = new ItemStackHandler(9);
        this.craftingStack = new CraftingStack(craftingView, 3, 3);

        this.output = ItemStack.EMPTY;

        this.toolStorage = new ToolItemHandler(9);

        this.itemView = new WrapperItemHandler(
            new CombinedInvWrapper(toolStorage, craftingView));
        this.itemView.onUpdate(this::onUpdate);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.container(ID, Workbench::new);
    }

    private void onUpdate() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }

        var recipeManager = CORE.recipeManager(world);
        var vanillaRecipes = world.getRecipeManager();

        currentRecipe = recipeManager.getRecipeFor(TOOL_CRAFTING, this)
            .map($ -> (Object) $)
            .or(() -> vanillaRecipes.getRecipeFor(CRAFTING, craftingStack.asCraftInput(), world))
            .orElse(null);

        if (currentRecipe instanceof ToolRecipe tool) {
            output = tool.assemble(this);
        } else if (currentRecipe instanceof RecipeHolder<?> holder &&
            holder.value() instanceof CraftingRecipe crafting) {
            output = crafting.assemble(craftingStack.asCraftInput(), world.registryAccess());
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

    public Level world() {
        var ret = blockEntity.getLevel();
        assert ret != null;
        return ret;
    }

    public void onTake(Player player, ItemStack stack) {
        if (stack.isEmpty() || currentRecipe == null) {
            return;
        }

        var amount = stack.getCount();
        LOGGER.trace("{} on craft {}", this, stack);

        // vanilla logic of crafting triggers
        stack.onCraftedBy(player.level(), player, amount);
        EventHooks.firePlayerCraftingEvent(player, stack, craftingStack);
        if (currentRecipe instanceof RecipeHolder<?> holder && holder.value() instanceof CraftingRecipe crafting &&
            !crafting.isSpecial()) {
            player.awardRecipes(List.of(holder));
        }
        CommonHooks.setCraftingPlayer(player);
        List<ItemStack> remaining;
        if (currentRecipe instanceof ToolRecipe tool) {
            remaining = tool.getRemainingItems(this);
        } else if (currentRecipe instanceof RecipeHolder<?> holder &&
            holder.value() instanceof CraftingRecipe crafting) {
            remaining = crafting.getRemainingItems(craftingStack.asCraftInput());
        } else {
            throw new IllegalStateException();
        }
        CommonHooks.setCraftingPlayer(null);

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

    public CraftingInput getCraftingInput() {
        return craftingStack.asCraftInput();
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
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(MENU_ITEM_HANDLER, itemView);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return StackHelper.serializeItemHandler(provider, itemView);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        StackHelper.deserializeItemHandler(provider, itemView, tag);
    }

    public static Optional<Workbench> tryGet(BlockEntity be) {
        return tryGetContainer(be, ID, Workbench.class);
    }

    public static Workbench get(BlockEntity be) {
        return getContainer(be, ID, Workbench.class);
    }
}
