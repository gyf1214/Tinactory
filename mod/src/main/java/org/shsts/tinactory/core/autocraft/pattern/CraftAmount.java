package org.shsts.tinactory.core.autocraft.pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.logistics.IIngredientKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftAmount(IIngredientKey key, long amount) {
    public CraftAmount {
        if (amount <= 0L) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
