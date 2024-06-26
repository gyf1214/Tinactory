package org.shsts.tinactory.core.util;

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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ClientUtil {
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

    public static List<Component> getTooltipsFromStack(ItemStack stack) {
        return stack.getTooltipLines(getPlayer(), Minecraft.getInstance().options.advancedItemTooltips ?
                TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }
}
