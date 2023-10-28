package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Inventory;
import org.shsts.tinactory.core.SmartBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchMenu extends ContainerMenu<SmartBlockEntity> {
    private static final int OUTPUT_SLOT = 0;

    public WorkbenchMenu(ContainerMenuType<SmartBlockEntity, ?> type, int id,
                         Inventory inventory, SmartBlockEntity blockEntity) {
        super(type, id, inventory, blockEntity);
    }

    @Override
    public void initLayout() {
        this.addSlot(OUTPUT_SLOT, 6 * SLOT_SIZE, SLOT_SIZE);
        for (var j = 0; j < 9; j++) {
            this.addSlot(1 + j, j * SLOT_SIZE, 3 * SLOT_SIZE + SPACING_VERTICAL);
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 3; j++) {
                this.addSlot(10 + i * 3 + j, (2 + j) * SLOT_SIZE, i * SLOT_SIZE);
            }
        }
        this.height = 4 * SLOT_SIZE + SPACING_VERTICAL;
    }
}
