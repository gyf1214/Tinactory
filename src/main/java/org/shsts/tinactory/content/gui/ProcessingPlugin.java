package org.shsts.tinactory.content.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.client.SwitchButton;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

import static org.shsts.tinactory.core.gui.Menu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingPlugin<M extends Menu<?, M>> implements IMenuPlugin<M> {
    private static final Texture SWITCH_TEX = Texture.SWITCH_BUTTON;

    private final int autoInputSlot;
    private final int autoDumpItemSlot;
    private final int autoDumpFluidSlot;
    private final int startY;

    private int syncSlot(M menu, String key) {
        return menu.addSyncSlot(MenuSyncPacket.Boolean::new, be ->
                AllCapabilities.MACHINE.get(be).config.getBoolean(key, false));
    }

    public ProcessingPlugin(M menu, boolean autoDumpItem, boolean autoDumpFluid) {
        this.autoInputSlot = syncSlot(menu, "autoInput");

        if (autoDumpItem) {
            this.autoDumpItemSlot = syncSlot(menu, "autoDumpItem");
        } else {
            this.autoDumpItemSlot = -1;
        }

        if (autoDumpFluid) {
            this.autoDumpFluidSlot = syncSlot(menu, "autoDumpFluid");
        } else {
            this.autoDumpFluidSlot = -1;
        }
        menu.onEventPacket(SET_MACHINE, p -> AllCapabilities.MACHINE.get(menu.blockEntity)
                .setConfig(p));

        this.startY = menu.getHeight() - Texture.SWITCH_BUTTON.height() / 2;
    }

    @OnlyIn(Dist.CLIENT)
    private void addSwitchButton(MenuScreen<M> screen, int x, Component tooltip, int syncSlot,
                                 String key) {
        var rect = new Rect(x, startY, SWITCH_TEX.width(), SWITCH_TEX.height() / 2);
        var button = new SwitchButton(screen.getMenu(), SWITCH_TEX, tooltip, syncSlot, (menu, val) ->
                menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set(key, val)));
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
        var x = CONTENT_WIDTH - SLOT_SIZE;
        if (autoDumpFluidSlot >= 0) {
            var autoDumpFluidTip = I18n.tr("tinactory.tooltip.autoDumpFluid");
            addSwitchButton(screen, x, autoDumpFluidTip, autoDumpFluidSlot, "autoDumpFluid");
            addStaticWidget(screen, x, Texture.FLUID_OUT_BUTTON);
            x -= SLOT_SIZE;
        }
        if (autoDumpItemSlot >= 0) {
            var autoDumpItemTip = I18n.tr("tinactory.tooltip.autoDumpItem");
            addSwitchButton(screen, x, autoDumpItemTip, autoDumpItemSlot, "autoDumpItem");
            addStaticWidget(screen, x, Texture.ITEM_OUT_BUTTON);
            x -= SLOT_SIZE;
        }
        var autoInputTip = I18n.tr("tinactory.tooltip.autoInput");
        addSwitchButton(screen, x, autoInputTip, autoInputSlot, "autoInput");
        addStaticWidget(screen, x, Texture.AUTO_IN_BUTTON);
    }

    public static <M extends Menu<?, M>> Function<M, IMenuPlugin<M>> builder(Layout layout) {
        var hasItemOutput = false;
        var hasFluidOutput = false;
        for (var slot : layout.slots) {
            if (slot.type() == SlotType.ITEM_OUTPUT) {
                hasItemOutput = true;
            }
            if (slot.type() == SlotType.FLUID_OUTPUT) {
                hasFluidOutput = true;
            }
        }
        var hasItem = hasItemOutput;
        var hasFluid = hasFluidOutput;
        return menu -> new ProcessingPlugin<>(menu, hasItem, hasFluid);
    }
}
