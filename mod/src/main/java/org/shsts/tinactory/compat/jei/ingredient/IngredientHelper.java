package org.shsts.tinactory.compat.jei.ingredient;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredientTypeWithSubtypes;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class IngredientHelper<V> implements IIngredientHelper<V> {
    private final IIngredientType<V> type;
    @Nullable
    private final ISubtypeManager subtypeManager;

    protected IngredientHelper(IIngredientType<V> type) {
        this.type = type;
        this.subtypeManager = null;
    }

    @Override
    public IIngredientType<V> getIngredientType() {
        return type;
    }

    @Override
    public abstract String getWildcardId(V ingredient);

    @Override
    public String getUniqueId(V ingredient, UidContext context) {
        var result = new StringBuilder(getWildcardId(ingredient));
        if (subtypeManager != null && type instanceof IIngredientTypeWithSubtypes<?, V> withSubtypes) {
            var subtypeInfo = subtypeManager.getSubtypeInfo(withSubtypes, ingredient, context);
            if (!subtypeInfo.isEmpty()) {
                result.append(":");
                result.append(subtypeInfo);
            }
        }
        return result.toString();
    }

    @Override
    public abstract ResourceLocation getResourceLocation(V ingredient);

    @SuppressWarnings("removal")
    @Override
    public String getModId(V ingredient) {
        return getResourceLocation(ingredient).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(V ingredient) {
        return getResourceLocation(ingredient).getPath();
    }

    @Override
    public String getErrorInfo(@Nullable V ingredient) {
        return Objects.toString(ingredient);
    }
}
