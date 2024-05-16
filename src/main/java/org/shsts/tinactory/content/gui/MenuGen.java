package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TranslatableComponent;
import org.shsts.tinactory.content.gui.client.GhostRecipe;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.MenuBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.Menu.CONTENT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING_VERTICAL;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MenuGen {
    public static <T extends SmartBlockEntity, M extends Menu<T>, P extends BlockEntityBuilder<T, ?>>
    Transformer<MenuBuilder<T, M, P>> machineMenu(Layout layout) {
        var y = layout.rect.endY() + SPACING_VERTICAL;
        var x = CONTENT_WIDTH - SLOT_SIZE * 2;
        return $ -> $.switchButton(Texture.SWITCH_BUTTON, x, y,
                        new TranslatableComponent("tinactory.tooltip.autoDumpItem"),
                        be -> Machine.get(be).machineConfig.isAutoDumpItem(),
                        (menu, value) -> menu.triggerEvent(SET_MACHINE,
                                SetMachinePacket.builder().autoDumpItem(value)))
                .staticWidget(Texture.ITEM_OUT_BUTTON, x, y)
                .switchButton(Texture.SWITCH_BUTTON, x + SLOT_SIZE, y,
                        new TranslatableComponent("tinactory.tooltip.autoDumpFluid"),
                        be -> Machine.get(be).machineConfig.isAutoDumpFluid(),
                        (menu, value) -> menu.triggerEvent(SET_MACHINE,
                                SetMachinePacket.builder().autoDumpFluid(value)))
                .staticWidget(Texture.FLUID_OUT_BUTTON, x + SLOT_SIZE, y)
                .event(SET_MACHINE, (be, p) -> Machine.get(be).setMachineConfig(p));
    }

    public static <R extends ProcessingRecipe<R>, T extends SmartBlockEntity,
            M extends Menu<T>, P extends BlockEntityBuilder<T, ?>>
    Transformer<MenuBuilder<T, M, P>>
    machineRecipeBook(RecipeTypeEntry<R, ?> recipeType, Layout layout) {
        return $ -> {
            var slot = $.addSyncSlot(MenuSyncPacket.LocHolder.class, (containerId, index, be) ->
                    new MenuSyncPacket.LocHolder(containerId, index,
                            Machine.get(be).machineConfig.getTargetRecipeLoc()));
            return $.screenWidget(() -> (menu, cons) -> cons.addPanel(
                            new MachineRecipeBook(menu, slot.getAsInt(), recipeType.get(), 0, 0)))
                    .menuWidget(() -> (menu, cons) -> cons.addWidget(Rect.ZERO.offset(layout.getXOffset(), 0),
                            new GhostRecipe(menu, slot.getAsInt(), layout)));
        };
    }
}
