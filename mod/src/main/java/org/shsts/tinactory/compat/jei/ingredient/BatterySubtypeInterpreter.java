package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.AllDataComponents;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum BatterySubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    INSTANCE;

    @Override
    public @Nullable Object getSubtypeData(ItemStack stack, UidContext context) {
        if (context == UidContext.Recipe) {
            return null;
        }
        return stack.get(AllDataComponents.BATTERY);
    }

    @Override
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "";
    }
}
