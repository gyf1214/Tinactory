package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.SimpleButton;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.gui.client.IMenuScreenFactory;

import java.util.function.Function;

import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_ANCHOR;
import static org.shsts.tinactory.content.gui.client.AbstractRecipeBook.PANEL_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.GREGTECH_LOGO;
import static org.shsts.tinactory.core.gui.Texture.HEAT_EMPTY;
import static org.shsts.tinactory.core.gui.Texture.HEAT_FULL;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_BURN;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineScreen extends ProcessingScreen {
    @Nullable
    protected final AbstractRecipeBook<?> recipeBook;

    protected MachineScreen(ProcessingMenu menu, Component title,
        @Nullable Function<MachineScreen, AbstractRecipeBook<?>> recipeBookFactory) {
        super(menu, title);

        var buttonY = layout.rect.endY() + SPACING;

        var portPanel = new PortPanel(this, layout);
        addPanel(PANEL_ANCHOR, PANEL_OFFSET, portPanel);
        portPanel.setActive(false);

        var portButton = new SimpleButton(menu, SWITCH_BUTTON,
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
        var portButtonOverlay = new StaticWidget(menu, GREGTECH_LOGO);
        var portButtonAnchor = RectD.corners(1d, 0d, 1d, 0d);
        var portButtonOffset = new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE);
        addWidget(portButtonAnchor, portButtonOffset, portButton);
        addWidget(portButtonAnchor, portButtonOffset.offset(1, 1).enlarge(-1, -1), portButtonOverlay);

        this.recipeBook = recipeBookFactory == null ? null : recipeBookFactory.apply(this);
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
            addPanel(recipeBook);
            addWidget(new Rect(0, buttonY, 20, 18), recipeBookButton);
        }
    }

    public MachineScreen(ProcessingMenu menu, Component title) {
        this(menu, title, MachineRecipeBook::new);
    }

    @Override
    public void removed() {
        super.removed();
        if (recipeBook != null) {
            recipeBook.remove();
        }
    }

    public static MachineScreen boiler(ProcessingMenu menu, Component title) {
        return new MachineScreen(menu, title, null) {
            {
                var burnBar = new ProgressBar(menu, PROGRESS_BURN, "burn");
                burnBar.direction = ProgressBar.Direction.VERTICAL;
                layoutPanel.addWidget(new Rect(1, 1 + SLOT_SIZE, 16, 16), burnBar);

                var heatBar = new ProgressBar(menu, HEAT_EMPTY, HEAT_FULL, "heat");
                heatBar.direction = ProgressBar.Direction.VERTICAL;
                var rect = new Rect(SLOT_SIZE * 2, 1, HEAT_EMPTY.width(), HEAT_EMPTY.height());
                layoutPanel.addWidget(rect, heatBar);
            }
        };
    }

    public static MachineScreen electricFurnace(ProcessingMenu menu, Component title) {
        return new MachineScreen(menu, title, ElectricFurnaceRecipeBook::new);
    }

    public static IMenuScreenFactory<ProcessingMenu, MachineScreen> marker(boolean includeNormal) {
        return (menu, title) -> new MachineScreen(menu, title,
            screen -> new MarkerRecipeBook(screen, includeNormal));
    }
}
