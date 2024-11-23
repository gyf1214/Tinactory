package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.AbstractRecipeBook;
import org.shsts.tinactory.content.gui.client.ElectricFurnaceRecipeBook;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.client.MarkerRecipeBook;
import org.shsts.tinactory.content.gui.client.MultiBlockRecipeBook;
import org.shsts.tinactory.content.gui.client.PortPanel;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_ANCHOR;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BUTTON;
import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE_CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MachinePlugin implements IMenuPlugin<ProcessingMenu> {
    private final int buttonY;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private AbstractRecipeBook<?> recipeBook = null;

    public MachinePlugin(ProcessingMenu menu) {
        this.buttonY = menu.getHeight() + MARGIN_VERTICAL;
        menu.setHeight(buttonY + SLOT_SIZE);

        menu.onEventPacket(SET_MACHINE_CONFIG, p ->
            AllCapabilities.MACHINE.get(menu.blockEntity).setConfig(p));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected abstract AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen);

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<ProcessingMenu> screen) {
        var menu = screen.getMenu();
        if (menu.layout == null) {
            return;
        }
        var portPanel = new PortPanel(screen, menu.layout);
        var button = new SimpleButton(menu, Texture.SWITCH_BUTTON,
            I18n.tr("tinactory.tooltip.openPortPanel"), 0, 0, 0, 0) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                portPanel.setActive(!portPanel.isActive());
                if (portPanel.isActive() && recipeBook != null) {
                    recipeBook.setBookActive(false);
                }
            }
        };
        var buttonOverlay = new StaticWidget(menu, Texture.GREGTECH_LOGO);

        screen.addPanel(PANEL_ANCHOR, PANEL_OFFSET, portPanel);
        portPanel.setActive(false);
        var buttonAnchor = RectD.corners(1d, 0d, 1d, 0d);
        var buttonOffset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        screen.addWidget(buttonAnchor, buttonOffset, button);
        screen.addWidget(buttonAnchor, buttonOffset.offset(1, 1).enlarge(-1, -1), buttonOverlay);

        recipeBook = createRecipeBook(screen);
        if (recipeBook != null) {
            var recipeBookButton = new SimpleButton(menu, RECIPE_BOOK_BUTTON,
                I18n.tr("tinactory.tooltip.openRecipeBook"), 0, 19) {
                @Override
                public void onMouseClicked(double mouseX, double mouseY, int button) {
                    super.onMouseClicked(mouseX, mouseY, button);
                    recipeBook.setBookActive(!recipeBook.isBookActive());
                    if (recipeBook.isActive()) {
                        portPanel.setActive(false);
                    }
                }
            };
            screen.addPanel(recipeBook);
            screen.addWidget(new Rect(0, buttonY, 20, 18), recipeBookButton);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onScreenRemoved() {
        if (recipeBook != null) {
            recipeBook.remove();
        }
    }

    public static IMenuPlugin.Factory<ProcessingMenu> processing(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType) {
        return menu -> new MachinePlugin(menu) {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen) {
                return new MachineRecipeBook(screen, recipeType.get());
            }
        };
    }

    public static IMenuPlugin.Factory<ProcessingMenu> marker(
        RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType, boolean includeNormal) {
        return menu -> new MachinePlugin(menu) {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen) {
                return new MarkerRecipeBook(screen, recipeType.get(), includeNormal);
            }
        };
    }

    public static IMenuPlugin<ProcessingMenu> noBook(ProcessingMenu menu) {
        return new MachinePlugin(menu) {
            @OnlyIn(Dist.CLIENT)
            @Nullable
            @Override
            protected AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen) {
                return null;
            }
        };
    }

    public static IMenuPlugin.Factory<ProcessingMenu> electricFurnace(Layout layout) {
        return menu -> new MachinePlugin(menu) {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen) {
                return new ElectricFurnaceRecipeBook(screen, layout);
            }
        };
    }

    public static IMenuPlugin<ProcessingMenu> multiBlock(ProcessingMenu menu) {
        return new MachinePlugin(menu) {
            @OnlyIn(Dist.CLIENT)
            @Override
            protected AbstractRecipeBook<?> createRecipeBook(MenuScreen<ProcessingMenu> screen) {
                return new MultiBlockRecipeBook(screen);
            }
        };
    }
}
