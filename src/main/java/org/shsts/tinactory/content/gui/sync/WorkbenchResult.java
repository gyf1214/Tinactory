package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.machine.IWorkbench;
import org.shsts.tinactory.core.gui.Menu;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchResult extends Slot {
    private final IWorkbench workbench;

    public WorkbenchResult(IWorkbench workbench, int xPos, int yPos) {
        super(Menu.EMPTY_CONTAINER, 0, xPos, yPos);
        this.workbench = workbench;
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
