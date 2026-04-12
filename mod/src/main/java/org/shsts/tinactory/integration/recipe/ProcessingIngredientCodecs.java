package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.recipe.StackIngredient;

import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredientCodecs {
    private static final Codec<StackIngredient<ItemStack>> ITEM_INGREDIENT_CODEC =
        ProcessingStackHelper.itemIngredientCodec();
    private static final Codec<TagIngredient> TAG_INGREDIENT_CODEC = TagIngredient.codec();
    private static final Codec<StackIngredient<FluidStack>> FLUID_INGREDIENT_CODEC =
        ProcessingStackHelper.fluidIngredientCodec();
    private static final Map<String, Codec<? extends IProcessingIngredient>> CODECS;

    public static final Codec<IProcessingIngredient> CODEC;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ProcessingStackHelper.ITEM_INGREDIENT_CODEC_NAME, ITEM_INGREDIENT_CODEC);
        CODECS.put(TagIngredient.CODEC_NAME, TAG_INGREDIENT_CODEC);
        CODECS.put(ProcessingStackHelper.FLUID_INGREDIENT_CODEC_NAME, FLUID_INGREDIENT_CODEC);
        CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);
    }

    private ProcessingIngredientCodecs() {}

    public static Codec<IProcessingIngredient> codec() {
        return CODEC;
    }
}
