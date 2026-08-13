package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.shsts.tinactory.AllLayouts;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.integration.gui.LayoutMenu;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static net.minecraft.world.item.ItemStack.isSameItemSameComponents;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends LayoutMenu {
    private static final int PLAYER_SLOT_COUNT = 36;
    private static final int TOOL_SLOT_COUNT = 9;
    private static final int MATERIAL_SLOT_COUNT = 9;
    private static final int TOOL_SLOT_BEGIN = PLAYER_SLOT_COUNT;
    private static final int MATERIAL_SLOT_BEGIN = TOOL_SLOT_BEGIN + TOOL_SLOT_COUNT;

    private record TransferPlan(WorkbenchTransferResult result, List<ItemStack> finalStacks) {}

    private class ResultSlot extends Slot {
        public ResultSlot(int x, int y) {
            super(EMPTY_CONTAINER, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !getItem().isEmpty();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public ItemStack getItem() {
            return workbench.getResult();
        }

        @Override
        public void set(ItemStack stack) {
            workbench.setResult(stack);
        }

        @Override
        public ItemStack remove(int amount) {
            return getItem().copy();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            workbench.onTake(player, stack);
        }
    }

    private final Workbench workbench;

    public WorkbenchMenu(Properties properties) {
        super(properties, AllLayouts.WORKBENCH, 0);
        addLayoutSlots(layout);

        this.workbench = Workbench.get(blockEntity());
        for (var slot : layout.slots) {
            if (slot.type() == SlotType.NONE) {
                var x = slot.x() + layout.getXOffset() + MARGIN_X + 1;
                var y = slot.y() + MARGIN_TOP + 1;
                addSlot(new ResultSlot(x, y));
            }
        }
    }

    public List<Slot> playerSlots() {
        return slots.subList(0, PLAYER_SLOT_COUNT);
    }

    public List<Slot> toolSlots() {
        return slots.subList(TOOL_SLOT_BEGIN, MATERIAL_SLOT_BEGIN);
    }

    public List<Slot> materialSlots() {
        return slots.subList(MATERIAL_SLOT_BEGIN, MATERIAL_SLOT_BEGIN + MATERIAL_SLOT_COUNT);
    }

    public WorkbenchTransferResult planTransfer(ToolRecipe recipe, boolean maxTransfer) {
        return plan(recipe, maxTransfer).result();
    }

    public void transfer(ToolRecipe recipe, boolean maxTransfer) {
        var plan = plan(recipe, maxTransfer);
        if (plan.result().code() != WorkbenchTransferResult.Code.SUCCESS) {
            return;
        }
        for (var i = 0; i < plan.finalStacks().size(); i++) {
            getSlot(i).set(plan.finalStacks().get(i));
        }
        broadcastChanges();
    }

    private TransferPlan plan(ToolRecipe recipe, boolean maxTransfer) {
        var stacks = new ArrayList<ItemStack>();
        for (var i = 0; i < MATERIAL_SLOT_BEGIN + MATERIAL_SLOT_COUNT; i++) {
            stacks.add(getSlot(i).getItem().copy());
        }
        var grid = new ArrayList<ItemStack>();
        for (var i = 0; i < MATERIAL_SLOT_COUNT; i++) {
            grid.add(ItemStack.EMPTY);
            stacks.add(getSlot(MATERIAL_SLOT_BEGIN + i).getItem().copy());
        }
        var missing = allocateTools(stacks, recipe.toolIngredients);
        if (!missing.isEmpty()) {
            return new TransferPlan(WorkbenchTransferResult.missingInput(missing), List.of());
        }
        var sets = 0;
        while (true) {
            var nextStacks = copyStacks(stacks);
            var nextGrid = copyStacks(grid);
            missing = allocateMaterials(nextStacks, nextGrid, recipe);
            if (!missing.isEmpty()) {
                if (sets == 0) {
                    return new TransferPlan(WorkbenchTransferResult.missingInput(missing), List.of());
                }
                break;
            }
            stacks = nextStacks;
            grid = nextGrid;
            sets++;
            if (!maxTransfer) {
                break;
            }
        }
        if (!stowOldGrid(stacks)) {
            return new TransferPlan(WorkbenchTransferResult.inventoryFull(), List.of());
        }
        var finalStacks = new ArrayList<ItemStack>(MATERIAL_SLOT_BEGIN + MATERIAL_SLOT_COUNT);
        finalStacks.addAll(stacks.subList(0, MATERIAL_SLOT_BEGIN));
        finalStacks.addAll(grid);
        return new TransferPlan(WorkbenchTransferResult.success(), finalStacks);
    }

    private static List<Integer> allocateTools(List<ItemStack> stacks, List<Ingredient> ingredients) {
        var missing = new ArrayList<Integer>();
        for (var i = 0; i < ingredients.size() && i < TOOL_SLOT_COUNT; i++) {
            var ingredient = ingredients.get(i);
            if (findMatching(stacks, TOOL_SLOT_BEGIN, MATERIAL_SLOT_BEGIN, ingredient, null) >= 0) {
                continue;
            }
            var source = findMatching(stacks, 0, PLAYER_SLOT_COUNT, ingredient, null);
            var target = findEmpty(stacks, TOOL_SLOT_BEGIN, MATERIAL_SLOT_BEGIN);
            if (source < 0 || target < 0) {
                missing.add(10 + i);
                continue;
            }
            var sourceStack = stacks.get(source);
            stacks.set(target, sourceStack.copyWithCount(1));
            sourceStack.shrink(1);
        }
        return missing;
    }

    private static List<Integer> allocateMaterials(List<ItemStack> stacks, List<ItemStack> grid,
        ToolRecipe recipe) {
        var missing = new ArrayList<Integer>();
        var shaped = recipe.shapedRecipe;
        for (var row = 0; row < shaped.getHeight(); row++) {
            for (var column = 0; column < shaped.getWidth(); column++) {
                var gridIndex = row * 3 + column;
                var ingredient = shaped.getIngredients().get(row * shaped.getWidth() + column);
                if (ingredient.isEmpty()) {
                    continue;
                }
                var selected = grid.get(gridIndex);
                var source = findMatching(stacks, 0, PLAYER_SLOT_COUNT + MATERIAL_SLOT_COUNT, ingredient,
                    selected.isEmpty() ? null : selected);
                if (source < 0) {
                    missing.add(1 + gridIndex);
                    continue;
                }
                var sourceStack = stacks.get(source);
                if (selected.isEmpty()) {
                    grid.set(gridIndex, sourceStack.copyWithCount(1));
                } else {
                    selected.grow(1);
                }
                sourceStack.shrink(1);
            }
        }
        return missing;
    }

    private static boolean stowOldGrid(List<ItemStack> stacks) {
        for (var i = MATERIAL_SLOT_BEGIN + MATERIAL_SLOT_COUNT; i < stacks.size(); i++) {
            var source = stacks.get(i);
            for (var slot = 0; slot < PLAYER_SLOT_COUNT && !source.isEmpty(); slot++) {
                var target = stacks.get(slot);
                if (isSameItemSameComponents(target, source) && target.getCount() < target.getMaxStackSize()) {
                    var amount = Math.min(source.getCount(), target.getMaxStackSize() - target.getCount());
                    target.grow(amount);
                    source.shrink(amount);
                }
            }
            for (var slot = 0; slot < PLAYER_SLOT_COUNT && !source.isEmpty(); slot++) {
                if (stacks.get(slot).isEmpty()) {
                    stacks.set(slot, source.copy());
                    source.setCount(0);
                }
            }
            if (!source.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static int findMatching(List<ItemStack> stacks, int begin, int end, Ingredient ingredient,
        ItemStack requiredVariant) {
        for (var i = begin; i < end; i++) {
            var stack = stacks.get(i);
            if (ingredient.test(stack) && (requiredVariant == null || isSameItemSameComponents(stack, requiredVariant))) {
                return i;
            }
        }
        return -1;
    }

    private static int findEmpty(List<ItemStack> stacks, int begin, int end) {
        for (var i = begin; i < end; i++) {
            if (stacks.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private static ArrayList<ItemStack> copyStacks(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected boolean quickMoveStack(Slot slot) {
        var oldStack = slot.getItem().copy();
        if (!super.quickMoveStack(slot)) {
            return false;
        }
        return StackHelper.itemStackEqual(oldStack, slot.getItem());
    }
}
