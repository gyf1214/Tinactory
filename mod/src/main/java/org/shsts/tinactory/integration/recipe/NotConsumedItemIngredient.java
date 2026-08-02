package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NotConsumedItemIngredient extends ItemsIngredient {
    public static final String CODEC_NAME = "not_consumed_item_ingredient";

    private final ItemStack stack;

    public NotConsumedItemIngredient(ItemStack stack) {
        super(Ingredient.of(stack), 0);
        this.stack = stack;
    }

    @Override
    public String codecName() {
        return CODEC_NAME;
    }

    public ItemStack stack() {
        return stack;
    }

    public static final MapCodec<NotConsumedItemIngredient> CODEC = ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").xmap(
        NotConsumedItemIngredient::new, NotConsumedItemIngredient::stack);
}
