package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.AbstractRecipeBook;
import org.shsts.tinactory.content.gui.client.ElectricFurnaceRecipeBook;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.gui.client.MarkerRecipeBook;
import org.shsts.tinactory.content.gui.client.PortPanel;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.machine.MachineProcessor;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.gui.IMenuPlugin;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_ANCHOR;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BUTTON;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachinePlugin extends ProcessingPlugin {
    private final ValueHolder<AbstractRecipeBook<?>> recipeBookHolder
        = ValueHolder.create();

    public MachinePlugin(IMenu menu) {
        super(menu, SLOT_SIZE + SPACING);

        menu.onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(menu.blockEntity()).setConfig(p));
    }

    @OnlyIn(Dist.CLIENT)
    protected Optional<AbstractRecipeBook<?>> createRecipeBook(ProcessingScreen screen) {
        return Optional.of(new MachineRecipeBook(screen, layout));
    }

    protected Optional<IRecipeType<? extends IRecipeBuilderBase<? extends ProcessingRecipe>>> getRecipeType() {
        var processor = PROCESSOR.tryGet(menu.blockEntity()).orElse(null);
        if (processor instanceof MachineProcessor<?> machine) {
            return Optional.of(machine.recipeType);
        }
        return Optional.empty();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(ProcessingScreen screen) {
        super.applyMenuScreen(screen);
        getRecipeType().ifPresent(screen::setRecipeType);

        var buttonY = layout.rect.endY() + SPACING;

        var portPanel = new PortPanel(screen, layout);
        var button = new SimpleButton(menu, Texture.SWITCH_BUTTON,
            I18n.tr("tinactory.tooltip.openPortPanel"), 0, 0, 0, 0) {
            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                super.onMouseClicked(mouseX, mouseY, button);
                portPanel.setActive(!portPanel.isActive());
                if (portPanel.isActive()) {
                    recipeBookHolder.tryGet().ifPresent($ -> $.setBookActive(false));
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

        createRecipeBook(screen).ifPresent(recipeBook -> {
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
            recipeBookHolder.setValue(recipeBook);
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onScreenRemoved() {
        recipeBookHolder.tryGet().ifPresent(AbstractRecipeBook::remove);
    }

    public static Function<IMenu, IMenuPlugin<?>> marker(boolean includeNormal) {
        return menu -> new MachinePlugin(menu) {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected Optional<AbstractRecipeBook<?>> createRecipeBook(ProcessingScreen screen) {
                return Optional.of(new MarkerRecipeBook(screen, layout, includeNormal));
            }
        };
    }

    public static IMenuPlugin<ProcessingScreen> noBook(IMenu menu) {
        return new MachinePlugin(menu) {
            @Override
            protected Optional<AbstractRecipeBook<?>> createRecipeBook(ProcessingScreen screen) {
                return Optional.empty();
            }
        };
    }

    public static IMenuPlugin<ProcessingScreen> electricFurnace(IMenu menu) {
        return new MachinePlugin(menu) {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected Optional<AbstractRecipeBook<?>> createRecipeBook(ProcessingScreen screen) {
                return Optional.of(new ElectricFurnaceRecipeBook(screen, layout));
            }
        };
    }

    public static IMenuPlugin<ProcessingScreen> multiBlock(IMenu menu) {
        return new MachinePlugin(menu) {
            @Override
            protected Optional<IRecipeType<? extends IRecipeBuilderBase<? extends ProcessingRecipe>>> getRecipeType() {
                var multiBlockInterface = (MultiBlockInterface) MACHINE.get(menu.blockEntity());
                return multiBlockInterface.getRecipeType();
            }
        };
    }
}
