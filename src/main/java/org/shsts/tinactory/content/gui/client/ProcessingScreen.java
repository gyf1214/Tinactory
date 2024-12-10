package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingScreen extends MenuScreen {
    public final Layout layout;
    protected final Panel layoutPanel;
    @Nullable
    private RecipeType<?> recipeType = null;

    public ProcessingScreen(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);

        this.layout = LAYOUT_PROVIDER.get(menu.blockEntity()).getLayout();
        this.layoutPanel = new Panel(this);

        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                var syncSlot = "fluidSlot_" + slot.index();
                var rect = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
                var rect1 = rect.offset(1, 1).enlarge(-2, -2);
                layoutPanel.addWidget(rect, new StaticWidget(menu, Texture.SLOT_BACKGROUND));
                layoutPanel.addWidget(rect1, new FluidSlot(menu, slot.index(), syncSlot));
            }
        }

        for (var image : layout.images) {
            layoutPanel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
        }

        var progressBar = layout.progressBar;
        if (progressBar != null) {
            var widget = new ProgressBar(menu, progressBar.texture(), "progress");
            layoutPanel.addWidget(progressBar.rect(), widget);
        }

        addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
    }

    public Optional<RecipeType<?>> getRecipeType() {
        return Optional.ofNullable(recipeType);
    }

    public void setRecipeType(RecipeType<?> recipeType) {
        this.recipeType = recipeType;
    }
}
