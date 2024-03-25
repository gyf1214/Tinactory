package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.gui.sync.SetMachineEventPacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.ContainerEventHandler;
import org.shsts.tinactory.registrate.builder.MenuBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.ContainerMenu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.ContainerMenu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.ContainerMenu.SPACING_VERTICAL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MenuGen {
    public static <S extends MenuBuilder<? extends Machine, ?, ?, S>>
    Transformer<S> machineMenu(Layout layout, Voltage voltage) {
        var y = layout.rect.endY() + SPACING_VERTICAL;
        var x = CONTENT_WIDTH - SLOT_SIZE * 2;
        return $ -> $.layout(layout, voltage)
                .switchButton(Texture.SWITCH_BUTTON, x, y, Machine::isAutoDumpItem,
                        (menu, value) -> menu.triggerEvent(ContainerEventHandler.SET_MACHINE,
                                SetMachineEventPacket.builder().autoDumpItem(value)))
                .staticWidget(Texture.ITEM_OUT_BUTTON, x, y)
                .switchButton(Texture.SWITCH_BUTTON, x + SLOT_SIZE, y, Machine::isAutoDumpFluid,
                        (menu, value) -> menu.triggerEvent(ContainerEventHandler.SET_MACHINE,
                                SetMachineEventPacket.builder().autoDumpFluid(value)))
                .staticWidget(Texture.FLUID_OUT_BUTTON, x + SLOT_SIZE, y)
                .registerEvent(ContainerEventHandler.SET_MACHINE, (menu, p) -> {
                    var be = menu.blockEntity;
                    p.getAutoDumpItem().ifPresent(be::setAutoDumpItem);
                    p.getAutoDumpFluid().ifPresent(be::setAutoDumpFluid);
                });
    }
}
