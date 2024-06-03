package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.sync.MenuEventHandler.SET_MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreAnalyzerRecipeBook extends MachineRecipeBook<OreVariant> {
    private final Layout.SlotInfo inputSlot;

    public OreAnalyzerRecipeBook(MenuScreen<? extends Menu<?, ?>> screen,
                                 int buttonX, int buttonY, Layout layout) {
        super(screen, buttonX, buttonY, layout.getXOffset());
        this.inputSlot = layout.slots.stream()
                .filter(slot -> slot.port() == 0)
                .findFirst().orElseThrow();
    }

    @Override
    protected void doRefreshRecipes() {
        var be = screen.getMenu().blockEntity;
        var voltage = (long) AllCapabilities.ELECTRIC_MACHINE.tryGet(be)
                .map(IElectricMachine::getVoltage)
                .orElse(0L);
        Arrays.stream(OreVariant.values())
                .filter(v -> v.voltage.value <= voltage)
                .forEach(v -> recipes.put(v.getLoc(), v));
    }

    @Override
    protected void selectRecipe(OreVariant recipe) {
        menu.triggerEvent(SET_MACHINE, SetMachinePacket.builder().set("targetRecipe", recipe.getLoc()));

        var ingredient = new ProcessingIngredients.ItemIngredient(new ItemStack(recipe.baseItem));
        ghostRecipe.addIngredient(inputSlot, ingredient);
    }


    @Override
    protected Optional<List<Component>> buttonToolTip(OreVariant recipe) {
        return Optional.of(List.of(I18n.tr("tinactory.gui.oreAnalyzer.recipeButton",
                I18n.name(recipe.baseItem))));
    }

    @Override
    protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
                                OreVariant recipe, Rect rect, int z) {
        var stack = new ItemStack(recipe.baseItem);
        var x = rect.x() + 2;
        var y = rect.y() + 2;
        RenderUtil.renderItem(stack, x, y);
    }
}
