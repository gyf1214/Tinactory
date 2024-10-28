package org.shsts.tinactory.core.util;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ClientUtil {
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    public static final NumberFormat DOUBLE_FORMAT = new DecimalFormat("0.00");
    public static final NumberFormat INTEGER_FORMAT = new DecimalFormat("0");
    public static final NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("0%");

    public static RecipeManager getRecipeManager() {
        var connection = Minecraft.getInstance().getConnection();
        assert connection != null;
        return connection.getRecipeManager();
    }

    public static ItemRenderer getItemRenderer() {
        return Minecraft.getInstance().getItemRenderer();
    }

    public static Font getFont() {
        return Minecraft.getInstance().font;
    }

    public static void playSound(SoundEvent sound) {
        var soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(SimpleSoundInstance.forUI(sound, 1f));
    }

    public static LocalPlayer getPlayer() {
        var player = Minecraft.getInstance().player;
        assert player != null;
        return player;
    }

    public static List<Component> itemTooltip(ItemStack stack) {
        return stack.getTooltipLines(getPlayer(), Minecraft.getInstance().options.advancedItemTooltips ?
                TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public static List<Component> itemTooltip(ItemStack stack, TooltipFlag tooltipFlag) {
        return stack.getTooltipLines(getPlayer(), tooltipFlag);
    }

    public static String getFluidAmountString(int amount) {
        if (amount < 1000) {
            return NUMBER_FORMAT.format(amount);
        } else if (amount < 1000000) {
            return NUMBER_FORMAT.format(amount / 1000) + "B";
        } else {
            return NUMBER_FORMAT.format(amount / 1000000) + "k";
        }
    }

    public static List<Component> fluidTooltip(FluidStack stack, boolean showAmount) {
        var tooltip = new ArrayList<Component>();
        tooltip.add(stack.getDisplayName());
        if (showAmount) {
            var amountString = I18n.raw(NUMBER_FORMAT.format(stack.getAmount()) + " mB");
            tooltip.add(amountString.withStyle(ChatFormatting.GRAY));
        }
        return tooltip;
    }
}
