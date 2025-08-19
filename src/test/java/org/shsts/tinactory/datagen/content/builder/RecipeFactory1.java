package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.ISelf;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.content.AllMaterials.getMaterial;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RecipeFactory1<B extends ProcessingRecipe.BuilderBase<?, B>,
    RB extends ProcessingRecipeBuilder1<B, S, RB>, S extends RecipeFactory1<B, RB, S>>
    implements ISelf<S> {
    private final IRecipeType<B> recipeType;
    private Transformer<RB> defaults = $ -> $;

    public RecipeFactory1(IRecipeType<B> recipeType) {
        this.recipeType = recipeType;
    }

    public S defaults(Transformer<RB> trans) {
        defaults = defaults.chain(trans);
        return self();
    }

    protected abstract RB doCreateBuilder(B builder);

    private RB createBuilder(B builder) {
        return doCreateBuilder(builder).transform(defaults);
    }

    public RB recipe(String id) {
        return createBuilder(recipeType.recipe(DATA_GEN, id));
    }

    public RB recipe(ResourceLocation loc) {
        return createBuilder(recipeType.recipe(DATA_GEN, loc));
    }

    public RB recipe(IForgeRegistryEntry<?> entry) {
        var loc = entry.getRegistryName();
        assert loc != null;
        return recipe(loc);
    }

    private ResourceLocation matLoc(String name, String sub) {
        var mat = getMaterial(name);
        return mat.hasFluid(sub) ? mat.fluidLoc(sub) : mat.loc(sub);
    }

    public RB inputMaterial(String name, String sub, Number amount) {
        return recipe(matLoc(name, sub)).inputMaterial(name, sub, amount);
    }

    public RB outputItem(ItemLike item, int amount) {
        return recipe(item.asItem()).outputItem(() -> item, amount);
    }

    public RB outputMaterial(String name, String sub, Number amount) {
        return recipe(matLoc(name, sub)).outputMaterial(name, sub, amount);
    }
}
