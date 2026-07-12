package org.shsts.tinactory.integration.util;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.core.util.I18n;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ClientUtil {
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();
    public static final NumberFormat DOUBLE_FORMAT = new DecimalFormat("0.00");
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

    public static void playSound(Holder<SoundEvent> sound) {
        var soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.play(SimpleSoundInstance.forUI(sound, 1f));
    }

    public static LocalPlayer getPlayer() {
        var player = Minecraft.getInstance().player;
        assert player != null;
        return player;
    }

    private static Item.TooltipContext tooltipContext() {
        return Item.TooltipContext.of(Minecraft.getInstance().level);
    }

    public static RegistryAccess registryAccess() {
        var world = Minecraft.getInstance().level;
        assert world != null;
        return world.registryAccess();
    }

    public static <T> ResourceLocation getRegistryKey(ResourceKey<? extends Registry<T>> registryKey, T value) {
        return Objects.requireNonNull(registryAccess().registryOrThrow(registryKey).getKey(value));
    }

    public static <T> Optional<T> getRegistryObject(ResourceKey<? extends Registry<T>> registryKey,
        ResourceLocation loc) {
        return registryAccess().registryOrThrow(registryKey).getOptional(loc);
    }

    public static List<Component> itemTooltip(ItemStack stack) {
        return stack.getTooltipLines(tooltipContext(), getPlayer(),
            Minecraft.getInstance().options.advancedItemTooltips ?
                TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public static List<Component> itemTooltip(ItemStack stack, TooltipFlag tooltipFlag) {
        return stack.getTooltipLines(tooltipContext(), getPlayer(), tooltipFlag);
    }

    public static List<Component> tagTooltip(TagKey<Item> tag) {
        return List.of(I18n.tr("tinactory.tooltip.tag", tag.location()));
    }

    public static Optional<ItemStack> selectItemFromItems(List<ItemStack> items) {
        if (items.isEmpty()) {
            return Optional.empty();
        }

        var cycle = System.currentTimeMillis() / 1000L;
        var idx = (int) (cycle % items.size());
        return Optional.of(items.get(idx));
    }

    public static Optional<ItemStack> selectItemFromItems(Ingredient ingredient) {
        return selectItemFromItems(Arrays.asList(ingredient.getItems()));
    }

    public static String getNumberString(long count) {
        if (count < 1000) {
            return NUMBER_FORMAT.format(count);
        } else if (count < 10000) {
            return DOUBLE_FORMAT.format((double) count / 1e3d) + "k";
        } else if (count < 1000000) {
            return NUMBER_FORMAT.format(count / 1000) + "k";
        } else if (count < 10000000) {
            return DOUBLE_FORMAT.format((double) count / 1e6d) + "M";
        } else if (count < 1000000000) {
            return NUMBER_FORMAT.format(count / 1000000) + "M";
        } else if (count < 10000000000L) {
            return DOUBLE_FORMAT.format((double) count / 1e9d) + "G";
        } else {
            return NUMBER_FORMAT.format(count / 1000000000) + "G";
        }
    }

    public static String getItemCountString(int count) {
        return getNumberString(count);
    }

    public static String getFluidAmountString(int amount) {
        if (amount < 1000) {
            return NUMBER_FORMAT.format(amount);
        } else if (amount < 1000000) {
            return getNumberString(amount / 1000) + "B";
        } else {
            return getNumberString(amount / 1000);
        }
    }

    public static String getBytesString(long count) {
        return getNumberString(count) + "B";
    }

    public static MutableComponent fluidName(FluidStack stack) {
        return stack.isEmpty() ? I18n.tr("tinactory.gui.emptyFluid") : stack.getHoverName().copy();
    }

    public static MutableComponent fluidAmount(int amount) {
        return I18n.tr("tinactory.gui.fluidAmount", NUMBER_FORMAT.format(amount));
    }

    public static MutableComponent fluidAmount(FluidStack stack) {
        return fluidAmount(stack.getAmount());
    }

    public static List<Component> fluidTooltip(FluidStack stack, boolean showAmount) {
        var line1 = fluidName(stack);
        return showAmount ? List.of(line1, fluidAmount(stack).withStyle(ChatFormatting.GRAY)) :
            List.of(line1);
    }

    public static void addTooltip(List<Component> tooltip, MutableComponent line) {
        tooltip.add(line.withStyle(ChatFormatting.GRAY));
    }

    public static void addTooltip(List<Component> tooltip, String id, Object... args) {
        addTooltip(tooltip, I18n.tr("tinactory.tooltip." + id, args));
    }

    public static boolean keyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }

    public static boolean shiftDown() {
        return keyDown(InputConstants.KEY_LSHIFT) || keyDown(InputConstants.KEY_RSHIFT);
    }
}
