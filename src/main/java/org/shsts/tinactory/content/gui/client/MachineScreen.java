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

import static org.shsts.tinactory.content.gui.MachineMenu.BoilerMenu.BURN_SYNC;
import static org.shsts.tinactory.content.gui.MachineMenu.BoilerMenu.HEAT_SYNC;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_ANCHOR;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.PANEL_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.HEAT_EMPTY;
import static org.shsts.tinactory.core.gui.Texture.HEAT_FULL;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_BURN;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineScreen extends ProcessingScreen {
    // in between BG_Z and default
    protected static final int MACHINE_BOOK_Z = -5;

    private final PortPanel portPanel;
    @Nullable
    private final MachineRecipeBook recipeBook;

    protected MachineScreen(ProcessingMenu menu, Component title, boolean hasRecipeBook) {
        super(menu, title);

        this.recipeBook = hasRecipeBook ? new MachineRecipeBook(this) : null;

        var buttonY = layout.rect.endY() + SPACING;
        this.portPanel = new PortPanel(this, layout);
        addPanel(PANEL_ANCHOR, PANEL_OFFSET, portPanel);
        portPanel.setActive(false);

        PortPanel.addButton(menu, this, portPanel, RectD.corners(1d, 0d, 1d, 0d), -SLOT_SIZE, buttonY, () -> {
            if (portPanel.isActive() && recipeBook != null) {
                recipeBook.setBookActive(false);
            }
        });

        if (recipeBook != null) {
            addPanel(RectD.FULL, Rect.ZERO, MACHINE_BOOK_Z, recipeBook);
            MachineRecipeBook.addButton(menu, this, recipeBook, RectD.ZERO, 0, buttonY, () -> {
                if (recipeBook.isBookActive()) {
                    portPanel.setActive(false);
                }
            });
        }
    }

    public MachineScreen(ProcessingMenu menu, Component title) {
        this(menu, title, true);
    }

    @Override
    public void removed() {
        super.removed();
        if (recipeBook != null) {
            recipeBook.remove();
        }
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int button) {
        if (!super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, button)) {
            return false;
        }
        return (!portPanel.isActive() || !portPanel.mouseIn(mouseX, mouseY)) &&
            (recipeBook == null || !recipeBook.isBookActive() || !recipeBook.mouseIn(mouseX, mouseY));
    }

    public static class Boiler extends MachineScreen {
        public Boiler(ProcessingMenu menu, Component title) {
            super(menu, title, false);
            var burnBar = new ProgressBar(menu, PROGRESS_BURN, BURN_SYNC);
            burnBar.direction = ProgressBar.Direction.VERTICAL;
            layoutPanel.addWidget(new Rect(1, 1 + SLOT_SIZE, 16, 16), burnBar);

            var heatBar = new ProgressBar(menu, HEAT_EMPTY, HEAT_FULL, HEAT_SYNC);
            heatBar.direction = ProgressBar.Direction.VERTICAL;
            var rect = new Rect(SLOT_SIZE * 2, 1, HEAT_EMPTY.width(), HEAT_EMPTY.height());
            layoutPanel.addWidget(rect, heatBar);
        }
    }
}
