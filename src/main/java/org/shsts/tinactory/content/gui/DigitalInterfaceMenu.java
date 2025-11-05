package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.multiblock.DigitalInterface;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingResults;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DigitalInterfaceMenu extends MachineMenu {
    private final DigitalInterface digitalInterface;

    public DigitalInterfaceMenu(Properties properties) {
        super(properties);
        this.digitalInterface = (DigitalInterface) MACHINE.get(blockEntity());
    }

    private class ItemSlot extends Slot {
        private final int port;
        private final int index;
        /**
         * This is only assigned on client.
         */
        @Nullable
        private ItemStack stack = null;

        public ItemSlot(int x, int y, int port, int index) {
            super(Menu.EMPTY_CONTAINER, 0, x, y);
            this.port = port;
            this.index = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public void set(ItemStack val) {
            stack = val;
        }

        @Override
        public ItemStack getItem() {
            if (stack != null) {
                return stack;
            }
            return getInfoItem(port, index);
        }
    }

    private void addFluidSlot(int slot, int port, int index) {
        addSyncSlot(FLUID_SYNC + slot, () -> new FluidSyncPacket(getInfoFluid(port, index)));
    }

    @Override
    protected void addLayoutSlots(Layout layout) {
        var xOffset = layout.getXOffset();
        for (var port = 0; port < layout.ports.size(); port++) {
            var info = layout.ports.get(port);
            var type = info.type().portType;
            var slots = info.slots();
            var slotInfos = layout.portSlots.get(port);
            if (type == PortType.NONE) {
                continue;
            }
            for (var i = 0; i < slots; i++) {
                var slot = slotInfos.get(i);
                if (type == PortType.ITEM) {
                    var x = xOffset + slot.x() + MARGIN_X + 1;
                    var y = slot.y() + MARGIN_TOP + 1;
                    addSlot(new ItemSlot(x, y, port, i));
                } else if (type == PortType.FLUID) {
                    addFluidSlot(slot.index(), port, i);
                }
            }
        }
    }

    /**
     * Already added in {@link #addLayoutSlots}
     */
    @Override
    protected void addFluidSlots() {}

    private ItemStack getInfoItem(int port, int index) {
        return digitalInterface.processor()
            .flatMap($ -> ((MachineProcessor) $).getInfo(port, index))
            .flatMap(object -> {
                if (object instanceof ProcessingIngredients.ItemIngredient ingredient) {
                    return Optional.of(ingredient.stack());
                } else if (object instanceof ProcessingResults.ItemResult result) {
                    return Optional.of(result.stack);
                }
                return Optional.empty();
            }).orElse(ItemStack.EMPTY);
    }

    private FluidStack getInfoFluid(int port, int index) {
        return digitalInterface.processor()
            .flatMap($ -> ((MachineProcessor) $).getInfo(port, index))
            .flatMap(object -> {
                if (object instanceof ProcessingIngredients.FluidIngredient ingredient) {
                    return Optional.of(ingredient.fluid());
                } else if (object instanceof ProcessingResults.FluidResult result) {
                    return Optional.of(result.stack);
                }
                return Optional.empty();
            }).orElse(FluidStack.EMPTY);
    }
}
