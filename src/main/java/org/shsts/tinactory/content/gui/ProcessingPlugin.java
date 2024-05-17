package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.client.SwitchButton;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.shsts.tinactory.core.gui.Menu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private static final int START_X = CONTENT_WIDTH - SLOT_SIZE * 2;
    private static final Texture SWITCH_TEX = Texture.SWITCH_BUTTON;

    private final int autoDumpItemSlot;
    private final int autoDumpFluidSlot;
    private final int startY;

    private int syncSlot(M menu, Predicate<Machine> getter) {
        return menu.addSyncSlot(MenuSyncPacket.Boolean::new, be -> getter.test(Machine.get(be)));
    }

    public ProcessingPlugin(M menu) {
        this.autoDumpItemSlot = syncSlot(menu, m -> m.config.getBoolean("autoDumpItem", false));
        this.autoDumpFluidSlot = syncSlot(menu, m -> m.config.getBoolean("autoDumpFluid", false));
        menu.onEventPacket(SET_MACHINE, p -> Machine.get(menu.blockEntity).setConfig(p));

        this.startY = menu.getHeight() + SPACING_VERTICAL;
        menu.setHeight(startY + Texture.SWITCH_BUTTON.height() / 2);
    }

    @OnlyIn(Dist.CLIENT)
    private void addSwitchButton(MenuScreen<M> screen, int x, Component tooltip, int syncSlot,
                                 BiConsumer<? extends Menu<?, ?>, Boolean> onSwitch) {
        var rect = new Rect(x, startY, SWITCH_TEX.width(), SWITCH_TEX.height() / 2);
        var button = new SwitchButton(screen.getMenu(), SWITCH_TEX, tooltip, syncSlot, onSwitch);
        screen.addWidget(rect, button);
    }

    @OnlyIn(Dist.CLIENT)
    private void addStaticWidget(MenuScreen<M> screen, int x, Texture tex) {
        var rect = new Rect(x, startY, tex.width(), tex.height());
        screen.addWidget(rect, new StaticWidget(screen.getMenu(), tex));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<M> screen) {
        var autoDumpItemTip = new TranslatableComponent("tinactory.tooltip.autoDumpItem");
        addSwitchButton(screen, START_X, autoDumpItemTip, autoDumpItemSlot, (menu, val) ->
                menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("autoDumpItem", val)));
        addStaticWidget(screen, START_X, Texture.ITEM_OUT_BUTTON);

        var autoDumpFluidTip = new TranslatableComponent("tinactory.tooltip.autoDumpFluid");
        addSwitchButton(screen, START_X + SLOT_SIZE, autoDumpFluidTip, autoDumpFluidSlot, (menu, val) ->
                menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("autoDumpFluid", val)));
        addStaticWidget(screen, START_X + SLOT_SIZE, Texture.FLUID_OUT_BUTTON);
    }
}
