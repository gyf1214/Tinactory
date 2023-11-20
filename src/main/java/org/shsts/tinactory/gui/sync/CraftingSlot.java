package org.shsts.tinactory.gui.sync;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.content.machine.IWorkbench;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CraftingSlot extends Slot {
    private static final Container EMPTY_CONTAINER = new SimpleContainer(0);

    private final IWorkbench workbench;

    public CraftingSlot(IWorkbench workbench, int xPos, int yPos) {
        super(EMPTY_CONTAINER, 0, xPos, yPos);
        this.workbench = workbench;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return !this.getItem().isEmpty();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.getMaxStackSize();
    }

    @Override
    public ItemStack getItem() {
        return this.workbench.getResult();
    }

    @Override
    public void set(ItemStack stack) {
        this.workbench.setResult(stack);
    }

    @Override
    public ItemStack remove(int amount) {
        return ItemHandlerHelper.copyStackWithSize(this.getItem(), amount);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.workbench.onTake(player, stack);
    }
}
